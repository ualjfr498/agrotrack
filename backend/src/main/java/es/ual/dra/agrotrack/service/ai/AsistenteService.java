package es.ual.dra.agrotrack.service.ai;

import es.ual.dra.agrotrack.dto.MensajeDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Asistente IA para el agricultor (Fase 7).
 *
 * Orquesta cuatro piezas:
 * 1. El modelo de lenguaje (servido vía API OpenAI-compatible; el proveedor
 * activo se elige por entorno: Groq, LM Studio... ver .env).
 * 2. Las tools del mcp-server, descubiertas por el cliente MCP y registradas
 * como toolCallbacks por defecto del ChatClient.
 * 3. La memoria de conversación: sin ella cada mensaje se procesaría aislado y
 * el asistente perdería el contexto ("¿dónde vendo las cerezas?" → "dentro
 * de dos semanas" no tendría sentido). Cada usuario tiene su propio hilo,
 * identificado por su email (conversationId), con una ventana de los últimos
 * mensajes para no disparar el límite de tokens del proveedor.
 * 4. La identidad del usuario, que se inyecta en el toolContext y viaja como
 * _meta MCP hasta las write-tools del mcp-server (claim source=backend +
 * actingUser). Sin este toolContext, las write-tools del mcp-server se
 * niegan a ejecutarse.
 *
 * El modelo decide solo cuándo invocar una tool; nosotros solo le damos el
 * catálogo, el contexto de identidad y el historial.
 */
@Service
@Slf4j
public class AsistenteService {

    private static final String SYSTEM_PROMPT = """
            Eres el asistente de AgroTrack para agricultores españoles. Ayudas con precios
            mayoristas de frutas y hortalizas (mercados Mercamadrid, Mercabarna, Mercabilbao,
            Mercavalencia, Mercasevilla) y con las parcelas y cultivos del usuario.

            REGLAS CRÍTICAS PARA USAR HERRAMIENTAS:
            1. Si el usuario pregunta sobre "mis cultivos", "mis parcelas", etc.: ejecuta
               misCultivos() o misParcelas() PRIMERO, sin preguntar.
            2. Si pregunta "dónde vender mis cultivos" o "dónde precio mejor":
               - Ejecuta misCultivos() para ver qué tiene
               - Para CADA cultivo, ejecuta historialPrecios con el nombre del producto
               - Compara y responde directamente (no preguntes "¿cuál quieres?")
            3. NOMBRES, NO IDs: las tools de producto (historialPrecios, obtenerProducto)
               y registrarCultivo reciben NOMBRES (de producto y de parcela), tal como los
               diga el usuario. El sistema resuelve los identificadores internamente. Pasa
               los nombres literalmente; no inventes ni traduzcas nombres ni manejes ids.
            4. Si una tool responde que hay VARIOS resultados que coinciden, pregunta al
               usuario a cuál se refiere. Si responde que NO existe, díselo; nunca registres
               ni consultes uno distinto al que pidió.
            5. Nunca digas "primero voy a...", "luego voy a..." ni pidas confirmaciones.
               Encadena tools sin anunciarlo y responde con el resultado final.
            6. Pregunta al usuario SOLO lo que no puedas obtener con tools.
            7. CONTEXTO: tienes memoria de los mensajes anteriores de esta conversación.
               Si el usuario responde algo breve ("dentro de dos semanas", "las cerezas"),
               interprétalo a la luz de lo que ya habíais hablado; no vuelvas a preguntar.
            8. FECHAS RELATIVAS: la fecha de hoy se te indica más abajo. Calcula tú las
               fechas relativas ("dentro de dos semanas", "hace dos días") a partir de
               ella; no pidas la fecha exacta si puedes deducirla.
            9. CONSEJO AGRONÓMICO: si el usuario pide consejo sobre qué sembrar o sobre
               una parcela suya, ejecuta misParcelas() y usa la DESCRIPCIÓN y la
               SUPERFICIE de esa parcela (p. ej. "tierra arenosa", "regadío", m²) para
               dar una recomendación concreta y razonada: qué cultivos van bien con ese
               tipo de suelo y en la temporada actual, y por qué. Eres un asistente
               agrícola: NO te excuses diciendo que no tienes datos de clima o terreno;
               aprovecha lo que sí sabes (descripción de la parcela, temporada del año,
               productos del catálogo) y da un consejo útil. Si la parcela no tiene
               descripción ni superficie, sugiere amablemente al usuario que las añada
               para afinar el consejo, pero da igualmente una recomendación general.

            Responde en español, en texto plano y natural: nada de Markdown (no te permito asteriscos,
            almohadillas, ni listas), ni IDs ni tecnicismos. Al dar precios, indica mercado
            y fecha.
            """;

    private final ChatClient chatClient;

    public AsistenteService(ChatClient.Builder chatClientBuilder, ToolCallbackProvider mcpTools) {
        this.chatClient = chatClientBuilder
                .defaultToolCallbacks(mcpTools)
                .build();
    }

    /**
     * Procesa una consulta del asistente.
     *
     * <p>
     * El contexto de la conversación (memoria) lo aporta el llamador a través de
     * {@code historial}: para usuarios registrados se lee de la BD (persistente);
     * para invitados, del propio frontend. Así el asistente recuerda el hilo y
     * sobrevive a reinicios sin estado en proceso.
     *
     * @param mensaje         pregunta en lenguaje natural
     * @param actingUserEmail email del usuario autenticado, o null si es un
     *                        invitado.
     *                        Si hay email, viaja como _meta MCP para que las
     *                        write-tools actúen en su nombre; si es null, esas
     *                        tools
     *                        se rechazan solas en el mcp-server.
     * @param historial       mensajes previos (rol "user"/"assistant") como
     *                        contexto
     * @return respuesta en lenguaje natural del modelo
     */
    public String consultar(String mensaje, String actingUserEmail, List<MensajeDto> historial) {
        boolean autenticado = actingUserEmail != null && !actingUserEmail.isBlank();
        log.debug("Consulta del asistente de {}: {}", autenticado ? actingUserEmail : "invitado", mensaje);

        // El system prompt se construye en cada llamada para inyectar la fecha de
        // hoy (necesaria para fechas relativas) y el estado de autenticación.
        String systemConFecha = SYSTEM_PROMPT
                + "\nFecha de hoy: " + LocalDate.now() + "."
                + (autenticado ? ""
                        : "\nEl usuario NO está registrado: solo puedes consultar precios y "
                                + "catálogo. Si pide gestionar parcelas o cultivos, o un consejo "
                                + "personalizado sobre sus tierras, invítale amablemente a registrarse.");

        // Sin actingUser para invitados: el mcp-server rechaza las tools privadas.
        Map<String, Object> toolContext = autenticado
                ? Map.of("source", "backend", "actingUser", actingUserEmail)
                : Map.of("source", "backend");

        // El historial previo se inyecta como mensajes; el nuevo va en .user().
        List<Message> mensajesPrevios = new ArrayList<>();
        if (historial != null) {
            for (MensajeDto m : historial) {
                if ("assistant".equalsIgnoreCase(m.rol())) {
                    mensajesPrevios.add(new AssistantMessage(m.texto()));
                } else {
                    mensajesPrevios.add(new UserMessage(m.texto()));
                }
            }
        }

        return chatClient.prompt()
                .system(systemConFecha)
                .messages(mensajesPrevios)
                .user(mensaje)
                .toolContext(toolContext)
                .call()
                .content();
    }
}

package es.ual.dra.agrotrack.service.ai;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Asistente IA para el agricultor (Fase 7).
 *
 * Orquesta tres piezas:
 *   1. El modelo de lenguaje (Qwen servido por LM Studio vía API OpenAI-compatible).
 *   2. Las tools del mcp-server, descubiertas por el cliente MCP y registradas
 *      como toolCallbacks por defecto del ChatClient.
 *   3. La identidad del usuario, que se inyecta en el toolContext y viaja como
 *      _meta MCP hasta las write-tools del mcp-server (claim source=backend +
 *      actingUser). Sin este toolContext, las write-tools del mcp-server se
 *      niegan a ejecutarse.
 *
 * El modelo decide solo cuándo invocar una tool; nosotros solo le damos el
 * catálogo y el contexto de identidad.
 */
@Service
@Slf4j
public class AsistenteService {

    private static final String SYSTEM_PROMPT = """
        Eres el asistente de AgroTrack, una aplicación para agricultores españoles.
        Ayudas a analizar precios mayoristas de frutas y hortalizas en los 5 mercados
        Mercasa (Mercamadrid, Mercabarna, Mercabilbao, Mercavalencia, Mercasevilla) y a
        gestionar las parcelas y cultivos del agricultor.

        Dispones de herramientas (tools) para consultar el catálogo, los precios y para
        registrar parcelas y cultivos. Úsalas siempre que necesites datos reales en lugar
        de inventarlos. Si una operación de escritura falla por falta de permisos, explica
        al usuario que esa acción solo está disponible desde el asistente de la aplicación
        web autenticada.

        Responde en español, de forma concreta y útil para alguien del campo. Cuando des
        cifras de precios, indica el mercado y la fecha. No inventes datos que no provengan
        de las herramientas.
        """;

    private final ChatClient chatClient;

    public AsistenteService(ChatClient.Builder chatClientBuilder, ToolCallbackProvider mcpTools) {
        this.chatClient = chatClientBuilder
            .defaultSystem(SYSTEM_PROMPT)
            .defaultToolCallbacks(mcpTools)
            .build();
    }

    /**
     * Procesa una consulta del agricultor.
     *
     * @param mensaje         pregunta en lenguaje natural
     * @param actingUserEmail email del usuario autenticado; viaja como _meta MCP
     *                        para que las write-tools sepan en nombre de quién actúan
     * @return respuesta en lenguaje natural del modelo
     */
    public String consultar(String mensaje, String actingUserEmail) {
        log.debug("Consulta del asistente de {}: {}", actingUserEmail, mensaje);
        return chatClient.prompt()
            .user(mensaje)
            .toolContext(Map.of(
                "source", "backend",
                "actingUser", actingUserEmail
            ))
            .call()
            .content();
    }
}

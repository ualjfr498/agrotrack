package es.ual.dra.agrotrack.mcp.tools;

import com.fasterxml.jackson.databind.ObjectMapper;
import es.ual.dra.agrotrack.mcp.client.BackendClient;
import es.ual.dra.agrotrack.mcp.dto.CultivoCreateData;
import es.ual.dra.agrotrack.mcp.dto.ParcelaCreateData;
import io.modelcontextprotocol.server.McpServerFeatures.SyncToolSpecification;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import io.modelcontextprotocol.spec.McpSchema;
import io.modelcontextprotocol.spec.McpSchema.CallToolRequest;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Tools de ESCRITURA y de LECTURA PRIVADA (datos del usuario).
 *
 * A diferencia de las read-tools públicas de {@link AgroTools} (que usan @Tool),
 * estas se registran con la API nativa del SDK MCP ({@link SyncToolSpecification}).
 * El motivo es técnico: el wrapper @Tool de Spring AI descarta el _meta entrante
 * del request, y aquí lo NECESITAMOS para aplicar la guarda de seguridad.
 *
 * Guarda (source=backend):
 *   El asistente del backend inyecta en el toolContext del ChatClient los claims
 *   {source=backend, actingUser=<email>}, que Spring AI propaga como _meta del
 *   CallToolRequest. Estas tools solo se ejecutan si _meta.source == "backend";
 *   así, un cliente MCP externo (LM Studio chat UI, Claude Desktop...) que no
 *   pasa por el backend autenticado NO puede registrar nada ni leer datos de un
 *   usuario. El email de actingUser se reenvía al backend en X-Acting-User.
 *
 * El bean List<SyncToolSpecification> que aquí se publica se SUMA a las read-tools
 * @Tool: el server MCP las concatena todas (ObjectProvider.stream().flatMap).
 */
@Configuration
@RequiredArgsConstructor
@Slf4j
public class WriteToolsConfig {

    private final BackendClient backend;
    private final ObjectMapper objectMapper;
    private final CatalogoResolver resolver;

    @Bean
    public List<SyncToolSpecification> agroWriteTools() {
        return List.of(
            registrarParcelaSpec(),
            misParcelasSpec(),
            registrarCultivoSpec(),
            misCultivosSpec()
        );
    }

    // ── Especificaciones de las tools ───────────────────────────────────

    private SyncToolSpecification registrarParcelaSpec() {
        var schema = objectSchema(
            Map.of(
                "nombre", prop("string", "Nombre de la parcela"),
                "superficieM2", propNullable("number", "Superficie en metros cuadrados (opcional)"),
                "descripcion", propNullable("string", "Descripción libre (opcional)")
            ),
            List.of("nombre"));
        return SyncToolSpecification.builder()
            .tool(tool("registrarParcela",
                "Registra una parcela del usuario.",
                schema))
            .callHandler(this::registrarParcela)
            .build();
    }

    private SyncToolSpecification misParcelasSpec() {
        return SyncToolSpecification.builder()
            .tool(tool("misParcelas",
                "Lista las parcelas del usuario.",
                objectSchema(Map.of(), List.of())))
            .callHandler(this::misParcelas)
            .build();
    }

    private SyncToolSpecification registrarCultivoSpec() {
        var schema = objectSchema(
            Map.of(
                "parcela", prop("string", "Nombre de la parcela donde se siembra"),
                "producto", prop("string", "Nombre del producto (fruta u hortaliza) que se cultiva"),
                "fechaSiembra", prop("string", "Fecha de siembra en formato ISO yyyy-MM-dd"),
                "estado", propNullable("string", "Estado del cultivo: SEMBRADO, CRECIENDO, COSECHADO o RETIRADO (opcional)"),
                "notas", propNullable("string", "Notas libres (opcional)")
            ),
            List.of("parcela", "producto", "fechaSiembra"));
        return SyncToolSpecification.builder()
            .tool(tool("registrarCultivo",
                "Registra un cultivo en una parcela del usuario. Recibe los NOMBRES de la "
                + "parcela y del producto tal como los diga el usuario; el sistema resuelve "
                + "internamente sus identificadores.",
                schema))
            .callHandler(this::registrarCultivo)
            .build();
    }

    private SyncToolSpecification misCultivosSpec() {
        return SyncToolSpecification.builder()
            .tool(tool("misCultivos",
                "Lista los cultivos del usuario.",
                objectSchema(Map.of(), List.of())))
            .callHandler(this::misCultivos)
            .build();
    }

    // ── Handlers ────────────────────────────────────────────────────────

    private CallToolResult registrarParcela(McpSyncServerExchange ex, CallToolRequest req) {
        String actingUser = actingUserOrNull(req.meta());
        if (actingUser == null) return denied();
        try {
            var body = objectMapper.convertValue(req.arguments(), ParcelaCreateData.class);
            return ok(backend.crearParcela(actingUser, body));
        } catch (Exception e) {
            return error("registrarParcela", e);
        }
    }

    private CallToolResult misParcelas(McpSyncServerExchange ex, CallToolRequest req) {
        String actingUser = actingUserOrNull(req.meta());
        if (actingUser == null) return denied();
        try {
            return ok(backend.misParcelas(actingUser));
        } catch (Exception e) {
            return error("misParcelas", e);
        }
    }

    private CallToolResult registrarCultivo(McpSyncServerExchange ex, CallToolRequest req) {
        String actingUser = actingUserOrNull(req.meta());
        if (actingUser == null) return denied();
        try {
            Map<String, Object> args = req.arguments();
            // Resolución determinista de nombre→id (nunca la hace el modelo).
            Long parcelaId = resolver.resolverParcelaId(actingUser, asString(args.get("parcela")));
            Long productoId = resolver.resolverProductoId(asString(args.get("producto")));
            String fecha = asString(args.get("fechaSiembra"));
            var body = new CultivoCreateData(
                parcelaId,
                productoId,
                fecha != null ? LocalDate.parse(fecha) : null,
                asString(args.get("estado")),
                asString(args.get("notas"))
            );
            return ok(backend.crearCultivo(actingUser, body));
        } catch (CatalogoResolver.ResolucionException e) {
            // No es un fallo técnico: es información para que el modelo pregunte o
            // avise al usuario, sin registrar nada incorrecto.
            return new CallToolResult(e.getMessage(), true);
        } catch (Exception e) {
            return error("registrarCultivo", e);
        }
    }

    private String asString(Object o) {
        return (o instanceof String s && !s.isBlank()) ? s : null;
    }

    private CallToolResult misCultivos(McpSyncServerExchange ex, CallToolRequest req) {
        String actingUser = actingUserOrNull(req.meta());
        if (actingUser == null) return denied();
        try {
            return ok(backend.misCultivos(actingUser));
        } catch (Exception e) {
            return error("misCultivos", e);
        }
    }

    // ── Guarda e helpers de resultado ───────────────────────────────────

    /**
     * Devuelve el email de actingUser SOLO si la metadata MCP prueba que la
     * llamada viene del backend autenticado (source=backend). En cualquier otro
     * caso devuelve null y la tool se rechaza.
     */
    private String actingUserOrNull(Map<String, Object> meta) {
        if (meta == null || !"backend".equals(meta.get("source"))) {
            return null;
        }
        Object u = meta.get("actingUser");
        return (u instanceof String s && !s.isBlank()) ? s : null;
    }

    private CallToolResult denied() {
        return new CallToolResult(
            "Esta operación requiere identidad de usuario y solo está disponible "
            + "desde el asistente autenticado de la aplicación web AgroTrack.",
            true);
    }

    private CallToolResult ok(Object value) {
        try {
            return new CallToolResult(objectMapper.writeValueAsString(value), false);
        } catch (Exception e) {
            return new CallToolResult(String.valueOf(value), false);
        }
    }

    private CallToolResult error(String tool, Exception e) {
        log.warn("Error en tool {}: {}", tool, e.getMessage());
        return new CallToolResult("No se pudo completar la operación: " + e.getMessage(), true);
    }

    // ── Construcción de schemas JSON ────────────────────────────────────

    private McpSchema.Tool tool(String name, String description, McpSchema.JsonSchema schema) {
        return McpSchema.Tool.builder()
            .name(name)
            .description(description)
            .inputSchema(schema)
            .build();
    }

    private McpSchema.JsonSchema objectSchema(Map<String, Object> properties, List<String> required) {
        return new McpSchema.JsonSchema("object", properties, required, Boolean.FALSE, null, null);
    }

    private Map<String, Object> prop(String type, String description) {
        return Map.of("type", type, "description", description);
    }

    /**
     * Propiedad opcional: el tipo admite null además del tipo base, para que los
     * proveedores que validan el schema de forma estricta (Groq) acepten que el
     * modelo pase el valor como null cuando el usuario no lo indica.
     */
    private Map<String, Object> propNullable(String type, String description) {
        return Map.of("type", List.of(type, "null"), "description", description);
    }
}

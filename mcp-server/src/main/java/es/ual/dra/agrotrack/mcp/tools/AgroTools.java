package es.ual.dra.agrotrack.mcp.tools;

import es.ual.dra.agrotrack.mcp.client.BackendClient;
import es.ual.dra.agrotrack.mcp.dto.CategoriaData;
import es.ual.dra.agrotrack.mcp.dto.PrecioData;
import es.ual.dra.agrotrack.mcp.dto.ProductoData;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Catálogo de tools que el mcp-server expone por el protocolo MCP.
 *
 * Todas las tools delegan al backend vía REST a través de {@link BackendClient}.
 *
 * Las tools de este fichero son LECTURAS DE DATOS PÚBLICOS (catálogo de
 * frutas/hortalizas Mercasa y precios mayoristas). NO requieren identidad
 * de usuario y, por tanto, están disponibles para CUALQUIER cliente MCP:
 * el backend (Spring AI ChatClient), LM Studio chat UI, Claude Desktop,
 * Cursor, etc.
 *
 * Las tools que dependen de identidad ("mis cultivos", "mis alertas") y
 * las de ESCRITURA se añadirán en pasos posteriores con una guarda sobre
 * el claim "source=backend" en {@code ToolContext}, para garantizar que
 * solo se disparan a través del asistente autenticado de la aplicación web.
 */
@Component
@RequiredArgsConstructor
public class AgroTools {

    private final BackendClient backend;
    private final CatalogoResolver resolver;

    @Tool(description = "Lista las dos categorías del catálogo: FRUTAS y HORTALIZAS")
    public List<CategoriaData> listarCategorias() {
        return backend.listarCategorias();
    }

    // Dos tools separadas en vez de una con parámetro opcional: los proveedores
    // que validan el schema de forma estricta (p. ej. Groq) rechazan un parámetro
    // tipado al que el modelo pasa null. Evitar opcionales nullable hace el
    // tool-calling robusto en cualquier proveedor.
    @Tool(description = "Lista todos los productos del catálogo.")
    public List<ProductoData> listarProductos() {
        return backend.listarProductos();
    }

    @Tool(description = "Lista los productos de una categoría (1=FRUTAS, 2=HORTALIZAS).")
    public List<ProductoData> listarProductosPorCategoria(
            @ToolParam(description = "Id de la categoría (1=FRUTAS, 2=HORTALIZAS)")
            Long categoriaId) {
        return backend.listarProductosPorCategoria(categoriaId);
    }

    @Tool(description = "Devuelve la ficha de un producto por su nombre.")
    public ProductoData obtenerProducto(
            @ToolParam(description = "Nombre del producto, tal como lo diga el usuario")
            String producto) {
        return backend.obtenerProducto(resolver.resolverProductoId(producto));
    }

    // Nombre en español puro (no "getHistorial...") y a juego con las demás tools:
    // los modelos tienden a "normalizar" a inglés los nombres mixtos (get + español)
    // y alucinan el nombre de la función, provocando tool-calls fallidos.
    // Recibe el NOMBRE del producto: el id lo resuelve CatalogoResolver, nunca el LLM.
    @Tool(description = "Histórico de precios (90 días) de un producto en los 5 mercados " +
                        "mayoristas. Recibe el NOMBRE del producto (p. ej. 'albaricoque'). " +
                        "Útil para comparar mercados y tendencias.")
    public List<PrecioData> historialPrecios(
            @ToolParam(description = "Nombre del producto, tal como lo diga el usuario")
            String producto) {
        return backend.historialPrecios(resolver.resolverProductoId(producto));
    }
}

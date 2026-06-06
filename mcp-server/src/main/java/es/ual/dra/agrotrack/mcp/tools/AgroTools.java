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

    @Tool(description = "Lista las dos categorías del catálogo: FRUTAS y HORTALIZAS")
    public List<CategoriaData> listarCategorias() {
        return backend.listarCategorias();
    }

    @Tool(description = "Lista los productos del catálogo (frutas y hortalizas con seguimiento de " +
                        "precio en mercados mayoristas). Si se indica un categoriaId filtra por esa " +
                        "categoría; si no, devuelve el catálogo entero.")
    public List<ProductoData> listarProductos(
            @ToolParam(required = false, description = "Id de categoría para filtrar (opcional)")
            Long categoriaId) {
        return categoriaId == null
            ? backend.listarProductos()
            : backend.listarProductosPorCategoria(categoriaId);
    }

    @Tool(description = "Devuelve la ficha completa de un producto a partir de su id: nombre, " +
                        "descripción, temporada óptima y categoría.")
    public ProductoData obtenerProducto(
            @ToolParam(description = "Id del producto")
            Long productoId) {
        return backend.obtenerProducto(productoId);
    }

    @Tool(description = "Devuelve el histórico de precios reales de un producto en los últimos " +
                        "90 días, con datos de los 5 mercados mayoristas españoles " +
                        "(Mercamadrid, Mercabarna, Mercabilbao, Mercavalencia, Mercasevilla). " +
                        "Útil para analizar tendencias, calcular medias y comparar mercados.")
    public List<PrecioData> getHistorialPrecios(
            @ToolParam(description = "Id del producto del que se quiere el histórico")
            Long productoId) {
        return backend.historialPrecios(productoId);
    }
}

package es.ual.dra.agrotrack.mcp.client;

import es.ual.dra.agrotrack.mcp.dto.CategoriaData;
import es.ual.dra.agrotrack.mcp.dto.PrecioData;
import es.ual.dra.agrotrack.mcp.dto.ProductoData;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

/**
 * Único punto de comunicación del mcp-server con el backend.
 *
 * Las tools NUNCA hablan directamente con la BD ni hacen HTTP por su cuenta;
 * delegan aquí. Esto centraliza el manejo de auth (X-Service-Token vía
 * RestClient.defaultHeader, X-Acting-User por llamada cuando aplica) y el
 * mapeo a los DTO espejo del mcp-server.
 */
@Component
@RequiredArgsConstructor
public class BackendClient {

    private final RestClient backendRestClient;

    private static final ParameterizedTypeReference<List<PrecioData>> PRECIO_LIST =
        new ParameterizedTypeReference<>() {};
    private static final ParameterizedTypeReference<List<ProductoData>> PRODUCTO_LIST =
        new ParameterizedTypeReference<>() {};
    private static final ParameterizedTypeReference<List<CategoriaData>> CATEGORIA_LIST =
        new ParameterizedTypeReference<>() {};

    // ── Lecturas públicas (no requieren X-Acting-User) ──────────────────

    public List<CategoriaData> listarCategorias() {
        return backendRestClient.get()
            .uri("/api/categorias")
            .retrieve()
            .body(CATEGORIA_LIST);
    }

    public List<ProductoData> listarProductos() {
        return backendRestClient.get()
            .uri("/api/productos")
            .retrieve()
            .body(PRODUCTO_LIST);
    }

    public List<ProductoData> listarProductosPorCategoria(Long categoriaId) {
        return backendRestClient.get()
            .uri(b -> b.path("/api/productos")
                .queryParam("categoriaId", categoriaId)
                .build())
            .retrieve()
            .body(PRODUCTO_LIST);
    }

    public ProductoData obtenerProducto(Long id) {
        return backendRestClient.get()
            .uri("/api/productos/{id}", id)
            .retrieve()
            .body(ProductoData.class);
    }

    public List<PrecioData> historialPrecios(Long productoId) {
        return backendRestClient.get()
            .uri("/api/precios/{id}", productoId)
            .retrieve()
            .body(PRECIO_LIST);
    }
}

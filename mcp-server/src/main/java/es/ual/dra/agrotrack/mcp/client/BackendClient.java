package es.ual.dra.agrotrack.mcp.client;

import es.ual.dra.agrotrack.mcp.dto.CategoriaData;
import es.ual.dra.agrotrack.mcp.dto.CultivoCreateData;
import es.ual.dra.agrotrack.mcp.dto.CultivoData;
import es.ual.dra.agrotrack.mcp.dto.ParcelaCreateData;
import es.ual.dra.agrotrack.mcp.dto.ParcelaData;
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
    private static final ParameterizedTypeReference<List<ParcelaData>> PARCELA_LIST =
        new ParameterizedTypeReference<>() {};
    private static final ParameterizedTypeReference<List<CultivoData>> CULTIVO_LIST =
        new ParameterizedTypeReference<>() {};

    /** Cabecera con el email del usuario en cuyo nombre actúa el mcp-server. */
    private static final String ACTING_USER = "X-Acting-User";

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

    // ── Operaciones con identidad (requieren X-Acting-User) ─────────────
    // Reads privadas y writes. El backend (ServiceTokenFilter) resuelve el
    // SecurityContext a partir del email de X-Acting-User.

    public List<ParcelaData> misParcelas(String actingUser) {
        return backendRestClient.get()
            .uri("/api/parcelas")
            .header(ACTING_USER, actingUser)
            .retrieve()
            .body(PARCELA_LIST);
    }

    public ParcelaData crearParcela(String actingUser, ParcelaCreateData body) {
        return backendRestClient.post()
            .uri("/api/parcelas")
            .header(ACTING_USER, actingUser)
            .body(body)
            .retrieve()
            .body(ParcelaData.class);
    }

    public List<CultivoData> misCultivos(String actingUser) {
        return backendRestClient.get()
            .uri("/api/cultivos")
            .header(ACTING_USER, actingUser)
            .retrieve()
            .body(CULTIVO_LIST);
    }

    public CultivoData crearCultivo(String actingUser, CultivoCreateData body) {
        return backendRestClient.post()
            .uri("/api/cultivos")
            .header(ACTING_USER, actingUser)
            .body(body)
            .retrieve()
            .body(CultivoData.class);
    }
}

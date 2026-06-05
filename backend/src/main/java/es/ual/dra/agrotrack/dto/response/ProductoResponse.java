package es.ual.dra.agrotrack.dto.response;

import es.ual.dra.agrotrack.model.entity.Producto;

public record ProductoResponse(
    Long id,
    String nombre,
    String descripcion,
    String imagenUrl,
    Integer temporadaInicio,
    Integer temporadaFin,
    CategoriaResponse categoria
) {
    public static ProductoResponse from(Producto p) {
        return new ProductoResponse(
            p.getId(),
            p.getNombre(),
            p.getDescripcion(),
            p.getImagenUrl(),
            p.getTemporadaInicio(),
            p.getTemporadaFin(),
            CategoriaResponse.from(p.getCategoria())
        );
    }
}

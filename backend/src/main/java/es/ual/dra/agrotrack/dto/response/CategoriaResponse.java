package es.ual.dra.agrotrack.dto.response;

import es.ual.dra.agrotrack.model.entity.Categoria;

public record CategoriaResponse(
    Long id,
    String nombre
) {
    public static CategoriaResponse from(Categoria c) {
        return new CategoriaResponse(c.getId(), c.getNombre());
    }
}

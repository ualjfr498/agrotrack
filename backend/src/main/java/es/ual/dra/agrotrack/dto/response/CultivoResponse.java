package es.ual.dra.agrotrack.dto.response;

import es.ual.dra.agrotrack.model.entity.CultivoParcela;
import es.ual.dra.agrotrack.model.enums.EstadoCultivo;

import java.time.LocalDate;

public record CultivoResponse(
    Long id,
    Long parcelaId,
    String parcelaNombre,
    Long productoId,
    String productoNombre,
    LocalDate fechaSiembra,
    EstadoCultivo estado,
    String notas
) {
    public static CultivoResponse from(CultivoParcela c) {
        return new CultivoResponse(
            c.getId(),
            c.getParcela().getId(),
            c.getParcela().getNombre(),
            c.getProducto().getId(),
            c.getProducto().getNombre(),
            c.getFechaSiembra(),
            c.getEstado(),
            c.getNotas()
        );
    }
}

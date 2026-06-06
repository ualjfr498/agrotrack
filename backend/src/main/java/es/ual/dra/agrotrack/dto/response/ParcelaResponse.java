package es.ual.dra.agrotrack.dto.response;

import es.ual.dra.agrotrack.model.entity.Parcela;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ParcelaResponse(
    Long id,
    String nombre,
    BigDecimal superficieM2,
    String descripcion,
    LocalDateTime fechaCreacion
) {
    public static ParcelaResponse from(Parcela p) {
        return new ParcelaResponse(
            p.getId(),
            p.getNombre(),
            p.getSuperficieM2(),
            p.getDescripcion(),
            p.getFechaCreacion()
        );
    }
}

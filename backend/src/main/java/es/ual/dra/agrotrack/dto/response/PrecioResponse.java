package es.ual.dra.agrotrack.dto.response;

import es.ual.dra.agrotrack.model.entity.PrecioMayorista;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PrecioResponse(
    Long id,
    Long productoId,
    String productoNombre,
    Long mercadoId,
    String mercadoNombre,
    LocalDate fecha,
    BigDecimal precioKg
) {
    public static PrecioResponse from(PrecioMayorista p) {
        return new PrecioResponse(
            p.getId(),
            p.getProducto().getId(),
            p.getProducto().getNombre(),
            p.getMercado().getId(),
            p.getMercado().getNombre(),
            p.getFecha(),
            p.getPrecioKg()
        );
    }
}

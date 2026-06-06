package es.ual.dra.agrotrack.mcp.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Espejo en el mcp-server del PrecioResponse del backend.
 */
public record PrecioData(
    Long id,
    Long productoId,
    String productoNombre,
    Long mercadoId,
    String mercadoNombre,
    LocalDate fecha,
    BigDecimal precioKg
) {}

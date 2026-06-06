package es.ual.dra.agrotrack.mcp.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Espejo en el mcp-server del ParcelaResponse del backend.
 */
public record ParcelaData(
    Long id,
    String nombre,
    BigDecimal superficieM2,
    String descripcion,
    LocalDateTime fechaCreacion
) {}

package es.ual.dra.agrotrack.mcp.dto;

import java.time.LocalDate;

/**
 * Espejo en el mcp-server del CultivoResponse del backend.
 */
public record CultivoData(
    Long id,
    Long parcelaId,
    String parcelaNombre,
    Long productoId,
    String productoNombre,
    LocalDate fechaSiembra,
    String estado,
    String notas
) {}

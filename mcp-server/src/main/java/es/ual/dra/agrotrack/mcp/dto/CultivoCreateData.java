package es.ual.dra.agrotrack.mcp.dto;

import java.time.LocalDate;

/**
 * Cuerpo que el mcp-server envía al backend para registrar un cultivo.
 * Espeja el CultivoRequest del backend.
 */
public record CultivoCreateData(
    Long parcelaId,
    Long productoId,
    LocalDate fechaSiembra,
    String estado,
    String notas
) {}

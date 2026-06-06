package es.ual.dra.agrotrack.mcp.dto;

import java.math.BigDecimal;

/**
 * Cuerpo que el mcp-server envía al backend para crear una parcela.
 * Espeja el ParcelaRequest del backend.
 */
public record ParcelaCreateData(
    String nombre,
    BigDecimal superficieM2,
    String descripcion
) {}

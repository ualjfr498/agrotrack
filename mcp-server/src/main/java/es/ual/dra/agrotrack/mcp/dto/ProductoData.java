package es.ual.dra.agrotrack.mcp.dto;

/**
 * Espejo en el mcp-server del ProductoResponse del backend.
 */
public record ProductoData(
    Long id,
    String nombre,
    String descripcion,
    String imagenUrl,
    Integer temporadaInicio,
    Integer temporadaFin,
    CategoriaData categoria
) {}

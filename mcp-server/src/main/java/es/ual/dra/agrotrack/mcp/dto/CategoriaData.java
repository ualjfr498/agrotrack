package es.ual.dra.agrotrack.mcp.dto;

/**
 * Espejo en el mcp-server del CategoriaResponse del backend.
 *
 * Es una clase paralela, no importada del backend: el contrato compartido
 * es la forma JSON, no el .class. Si el backend cambia la forma del DTO,
 * actualizar esta clase también.
 */
public record CategoriaData(
    Long id,
    String nombre
) {}

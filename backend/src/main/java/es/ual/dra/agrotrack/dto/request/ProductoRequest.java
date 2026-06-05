package es.ual.dra.agrotrack.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ProductoRequest(
    @NotBlank @Size(max = 80) String nombre,
    String descripcion,
    @Size(max = 255) String imagenUrl,
    @Min(1) @Max(12) Integer temporadaInicio,
    @Min(1) @Max(12) Integer temporadaFin,
    @NotNull Long categoriaId
) {}

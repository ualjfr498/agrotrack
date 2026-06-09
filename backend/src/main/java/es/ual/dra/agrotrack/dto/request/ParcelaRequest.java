package es.ual.dra.agrotrack.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record ParcelaRequest(
    @NotBlank @Size(max = 80) String nombre,
    @Positive BigDecimal superficieM2,
    String descripcion,
    // Imagen opcional como data URL base64. Null = no cambiar/limpiar.
    String imagen
) {}

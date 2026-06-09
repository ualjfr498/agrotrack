package es.ual.dra.agrotrack.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Edición del perfil propio: nombre, apellidos y foto (opcional). El email y la
 * contraseña no se cambian aquí.
 */
public record PerfilUpdateRequest(
    @NotBlank @Size(max = 80) String nombre,
    @NotBlank @Size(max = 120) String apellidos,
    // Foto de perfil opcional, como data URL base64.
    String foto
) {}

package es.ual.dra.agrotrack.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
    @NotBlank @Email @Size(max = 120) String email,
    @NotBlank @Size(min = 8, max = 128) String password,
    @NotBlank @Size(max = 80) String nombre,
    @NotBlank @Size(max = 120) String apellidos,
    // Foto de perfil opcional, como data URL base64.
    String foto
) {}

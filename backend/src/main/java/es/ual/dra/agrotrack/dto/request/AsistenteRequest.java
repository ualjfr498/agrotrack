package es.ual.dra.agrotrack.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AsistenteRequest(
    @NotBlank @Size(max = 2000) String mensaje
) {}

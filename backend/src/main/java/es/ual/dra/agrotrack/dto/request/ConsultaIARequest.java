package es.ual.dra.agrotrack.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ConsultaIARequest {

    @NotBlank
    @Size(max = 2000)
    private String pregunta;
}

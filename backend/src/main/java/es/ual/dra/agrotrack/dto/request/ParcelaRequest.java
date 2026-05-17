package es.ual.dra.agrotrack.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ParcelaRequest {

    @NotBlank
    @Size(min = 2, max = 150)
    private String nombre;

    private BigDecimal superficie;

    @Size(max = 500)
    private String descripcion;
}

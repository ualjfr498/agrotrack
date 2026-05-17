package es.ual.dra.agrotrack.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class AlertaRequest {

    @NotNull
    private Long productoId;

    @NotNull
    @DecimalMin(value = "0.01", message = "El umbral de precio debe ser mayor que 0")
    private BigDecimal precioUmbral;
}

package es.ual.dra.agrotrack.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class PrecioResponse {
    private Long id;
    private BigDecimal precio;
    private LocalDate fecha;
    private String mercado;
    private String ciudad;
    private Long productoId;
    private String productoNombre;
    private String categoria;
}

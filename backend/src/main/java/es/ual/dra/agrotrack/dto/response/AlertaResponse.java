package es.ual.dra.agrotrack.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class AlertaResponse {
    private Long id;
    private Long productoId;
    private String productoNombre;
    private BigDecimal precioUmbral;
    private boolean activa;
    private LocalDateTime ultimaNotificacion;
    private LocalDateTime createdAt;
}

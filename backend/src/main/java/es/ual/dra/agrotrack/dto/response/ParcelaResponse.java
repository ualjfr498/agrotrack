package es.ual.dra.agrotrack.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class ParcelaResponse {
    private Long id;
    private String nombre;
    private BigDecimal superficie;
    private String descripcion;
    private LocalDateTime createdAt;
    private List<CultivoResponse> cultivos;
}

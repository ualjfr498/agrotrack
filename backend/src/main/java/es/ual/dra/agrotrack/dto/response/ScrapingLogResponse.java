package es.ual.dra.agrotrack.dto.response;

import es.ual.dra.agrotrack.model.enums.ScrapingEstado;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class ScrapingLogResponse {
    private Long id;
    private LocalDateTime inicioAt;
    private LocalDateTime finAt;
    private ScrapingEstado estado;
    private int preciosGuardados;
    private String mensajeError;
    private boolean disparoManual;
}

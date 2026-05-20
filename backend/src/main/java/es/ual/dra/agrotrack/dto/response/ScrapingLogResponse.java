package es.ual.dra.agrotrack.dto.response;

import es.ual.dra.agrotrack.model.entity.ScrapingLog;
import es.ual.dra.agrotrack.model.enums.EstadoScraping;

import java.time.LocalDateTime;

/**
 * Vista de un ScrapingLog hacia el exterior. Existe para no exponer
 * la entidad JPA directamente en la API.
 */
public record ScrapingLogResponse(
    Long id,
    LocalDateTime fechaEjecucion,
    EstadoScraping estado,
    Integer filasInsertadas,
    Integer duracionMs,
    String mensaje
) {
    public static ScrapingLogResponse from(ScrapingLog log) {
        return new ScrapingLogResponse(
            log.getId(),
            log.getFechaEjecucion(),
            log.getEstado(),
            log.getFilasInsertadas(),
            log.getDuracionMs(),
            log.getMensaje()
        );
    }
}

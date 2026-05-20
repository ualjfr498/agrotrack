package es.ual.dra.agrotrack.controller;

import es.ual.dra.agrotrack.dto.response.ScrapingLogResponse;
import es.ual.dra.agrotrack.model.entity.ScrapingLog;
import es.ual.dra.agrotrack.service.scraping.ScrapingService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoints administrativos para gestión de precios.
 *
 * TODO (Fase 4): proteger con @PreAuthorize("hasRole('ADMIN')")
 * cuando esté operativa la seguridad por JWT.
 */
@RestController
@RequestMapping("/api/admin/precios")
@RequiredArgsConstructor
public class AdminPrecioController {

    private final ScrapingService scrapingService;

    /**
     * Dispara el scraping manualmente, sin esperar al cron.
     * Devuelve el log de la ejecución (estado, filas, duración).
     */
    @PostMapping("/actualizar")
    public ScrapingLogResponse actualizar() {
        ScrapingLog log = scrapingService.ejecutar();
        return ScrapingLogResponse.from(log);
    }
}

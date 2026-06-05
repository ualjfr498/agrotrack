package es.ual.dra.agrotrack.controller;

import es.ual.dra.agrotrack.dto.response.ScrapingLogResponse;
import es.ual.dra.agrotrack.model.entity.ScrapingLog;
import es.ual.dra.agrotrack.service.scraping.ScrapingService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoints administrativos para gestión de precios.
 * Requieren rol ADMIN.
 */
@RestController
@RequestMapping("/api/admin/precios")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
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

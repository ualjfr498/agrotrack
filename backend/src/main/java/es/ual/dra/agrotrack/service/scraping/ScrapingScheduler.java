package es.ual.dra.agrotrack.service.scraping;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Disparador automático del ScrapingService.
 *
 * Cron por defecto: lunes y jueves a las 07:00 (zona horaria del JVM,
 * configurada a Europe/Madrid vía datasource URL).
 * Mercasa publica precios mayoristas con esa cadencia.
 *
 * Para desactivar el cron en desarrollo, sobrescribir
 * `agrotrack.scraping.cron` a `-` (deshabilita el schedule).
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ScrapingScheduler {

    private final ScrapingService scrapingService;

    @Scheduled(cron = "${agrotrack.scraping.cron:0 0 7 * * MON,THU}", zone = "Europe/Madrid")
    public void ejecutarProgramado() {
        log.info("[Scheduler] Disparo cron del scraping");
        scrapingService.ejecutar();
    }
}

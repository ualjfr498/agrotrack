package es.ual.dra.agrotrack.service.scraping;

import es.ual.dra.agrotrack.model.entity.ScrapingLog;
import es.ual.dra.agrotrack.service.AlertaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Scheduler automático de scraping.
 * Se ejecuta los lunes y jueves a las 07:00 (zona horaria Europe/Madrid).
 *
 * Flujo completo:
 *  1. Jsoup parsea mercasa.es → extrae precios de frutas y hortalizas
 *  2. Mapea nombre → Producto en BD → guarda PrecioMayorista
 *  3. Registra en ScrapingLog (EXITOSO / FALLIDO / PARCIAL)
 *  4. AlertaService evalúa umbrales activos contra los nuevos precios
 *  5. NotificacionService envía email a usuarios con alertas disparadas
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class ScrapingScheduler {

    private final ScrapingStrategy scrapingStrategy;
    private final AlertaService alertaService;

    @Scheduled(cron = "0 0 7 * * MON,THU", zone = "Europe/Madrid")
    public void ejecutarScrapingYNotificar() {
        log.info(">>> Iniciando scraping automático de Mercasa");
        ejecutar(false);
    }

    /**
     * Punto de entrada compartido: scheduler automático y disparo manual desde AdminController.
     */
    public ScrapingLog ejecutar(boolean disparoManual) {
        ScrapingLog log = scrapingStrategy.ejecutar(disparoManual);

        if (log.getPreciosGuardados() > 0) {
            alertaService.evaluarAlertas();
        }

        return log;
    }
}

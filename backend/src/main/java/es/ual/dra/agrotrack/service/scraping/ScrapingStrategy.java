package es.ual.dra.agrotrack.service.scraping;

import es.ual.dra.agrotrack.model.entity.ScrapingLog;

/**
 * Patrón Strategy — interfaz de obtención de precios mayoristas.
 * La implementación actual (ScrapingService) parsea mercasa.es con Jsoup.
 * Podría sustituirse por una implementación basada en API si Mercasa
 * publicara una en el futuro, sin tocar el scheduler ni los servicios superiores.
 */
public interface ScrapingStrategy {

    ScrapingLog ejecutar(boolean disparoManual);
}

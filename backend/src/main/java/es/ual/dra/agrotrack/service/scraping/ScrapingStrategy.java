package es.ual.dra.agrotrack.service.scraping;

import java.util.List;

/**
 * Estrategia de obtención de precios mayoristas (patrón Strategy).
 *
 * Implementaciones posibles:
 *   - MercasaScrapingStrategy: parsea HTML de mercasa.es con Jsoup.
 *   - MercasaJsonStrategy (futuro): si Mercasa publicara JSON.
 *   - FixtureStrategy (tests): devuelve datos hardcoded sin red.
 *
 * El ScrapingService consume esta interfaz y no sabe de qué fuente
 * vienen los datos.
 */
public interface ScrapingStrategy {

    /**
     * @return precios crudos (sin mapear a entidades) o lista vacía si
     *         la fuente no devuelve nada. Lanza excepción si falla el
     *         acceso a la fuente (red, parseo crítico, etc.).
     */
    List<PrecioCrudo> obtenerPrecios();

    /**
     * Nombre legible de la estrategia, para logs y auditoría.
     */
    String nombre();
}

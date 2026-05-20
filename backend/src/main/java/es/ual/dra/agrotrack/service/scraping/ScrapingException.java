package es.ual.dra.agrotrack.service.scraping;

/**
 * Excepción que indica un fallo en la obtención de precios desde la
 * fuente externa (red caída, HTML cambiado, parseo crítico).
 * Es unchecked para no contaminar las firmas de la Strategy.
 */
public class ScrapingException extends RuntimeException {
    public ScrapingException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}

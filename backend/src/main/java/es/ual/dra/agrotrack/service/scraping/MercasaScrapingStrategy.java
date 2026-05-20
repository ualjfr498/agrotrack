package es.ual.dra.agrotrack.service.scraping;

import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementación Jsoup de la estrategia de scraping contra mercasa.es.
 *
 * IMPORTANTE — Selectores CSS placeholder:
 * Los selectores de abajo (.tabla-precios, td.producto, etc.) son
 * suposiciones plausibles, NO verificados contra el HTML real de
 * mercasa.es. La primera ejecución mostrará en los logs qué hay que
 * ajustar. Para inspeccionar el DOM real: abrir la página en el
 * navegador, F12 → pestaña Elements, copiar los selectores reales
 * de la tabla de precios y sustituirlos aquí.
 */
@Component
@Slf4j
public class MercasaScrapingStrategy implements ScrapingStrategy {

    private static final DateTimeFormatter FECHA_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final int TIMEOUT_MS = 15_000;
    private static final String USER_AGENT =
        "Mozilla/5.0 (Macintosh; Intel Mac OS X 13_0) AppleWebKit/605.1.15 AgroTrack/1.0";

    private final String url;

    public MercasaScrapingStrategy(
        @Value("${agrotrack.scraping.mercasa.url:https://www.mercasa.es/precios-y-mercados-mayoristas/}")
        String url
    ) {
        this.url = url;
    }

    @Override
    public String nombre() {
        return "MercasaJsoup";
    }

    @Override
    public List<PrecioCrudo> obtenerPrecios() {
        log.info("Descargando precios desde {}", url);

        Document doc;
        try {
            doc = Jsoup.connect(url)
                .userAgent(USER_AGENT)
                .timeout(TIMEOUT_MS)
                .get();
        } catch (IOException e) {
            throw new ScrapingException("No se pudo descargar la página de Mercasa: " + e.getMessage(), e);
        }

        Elements filas = doc.select("table.tabla-precios tbody tr");
        if (filas.isEmpty()) {
            log.warn("Selector 'table.tabla-precios tbody tr' no encontró filas. " +
                     "Probable cambio en el HTML de Mercasa — revisar selectores.");
            return List.of();
        }

        List<PrecioCrudo> resultado = new ArrayList<>();
        for (Element fila : filas) {
            try {
                resultado.add(parsearFila(fila));
            } catch (Exception e) {
                log.warn("Fila descartada por error de parseo: {} ({})", fila.text(), e.getMessage());
            }
        }
        log.info("Parseadas {} filas de precios", resultado.size());
        return resultado;
    }

    /**
     * Convierte una <tr> en un PrecioCrudo.
     * Estructura esperada: producto | mercado | fecha | precio.
     */
    private PrecioCrudo parsearFila(Element fila) {
        String producto  = fila.selectFirst("td.producto").text().trim().toLowerCase();
        String mercado   = fila.selectFirst("td.mercado").text().trim();
        LocalDate fecha  = LocalDate.parse(fila.selectFirst("td.fecha").text().trim(), FECHA_FMT);
        BigDecimal precio = parsearPrecioEuropeo(fila.selectFirst("td.precio").text().trim());
        return new PrecioCrudo(producto, mercado, fecha, precio);
    }

    /**
     * Convierte "1,20" (formato europeo) en BigDecimal("1.20").
     * Acepta también "1.20" por si alguna fila viene en formato US.
     */
    private BigDecimal parsearPrecioEuropeo(String texto) {
        String limpio = texto.replace("€", "").replace(" ", "").replace(",", ".");
        return new BigDecimal(limpio);
    }
}

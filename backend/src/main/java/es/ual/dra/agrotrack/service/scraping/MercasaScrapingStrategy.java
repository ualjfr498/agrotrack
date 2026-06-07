package es.ual.dra.agrotrack.service.scraping;

import lombok.extern.slf4j.Slf4j;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Estrategia de scraping contra mercasa.es (precios mayoristas).
 *
 * Mecánica real de la web (descubierta por ingeniería inversa, no es AJAX):
 *  - La página tiene un formulario POST (name="change-mercados-mayoristas")
 *    con dos selects: year-my (año) y date-my (fecha concreta, p.ej. 2026-06-02).
 *  - Al enviar ese POST, el servidor renderiza server-side la tabla
 *    #resultsTable YA rellena. Sin POST, la tabla viene vacía (por eso un
 *    simple GET no devolvía nada).
 *
 * La tabla es PIVOTE (cross-tab), no una fila por precio:
 *  - Se divide en secciones "Frutas" y "Hortalizas".
 *  - Cabecera de cada sección: [Sección] + 5 mercados (th colspan=2).
 *  - Subcabecera: [Productos] + 2 fechas (dd/MM) repetidas por cada mercado.
 *  - Cada fila de datos: [producto] + 10 precios = 5 mercados × 2 fechas.
 * El precio de la columna k corresponde a: mercado = mercados[k/2],
 * fecha = fechas[k%2].
 */
@Component
@Slf4j
public class MercasaScrapingStrategy implements ScrapingStrategy {

    private static final int TIMEOUT_MS = 20_000;
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

        // 1. GET inicial: averiguar la fecha más reciente disponible (1ª option).
        Document home;
        try {
            home = Jsoup.connect(url).userAgent(USER_AGENT).timeout(TIMEOUT_MS).get();
        } catch (IOException e) {
            throw new ScrapingException("No se pudo descargar la página de Mercasa: " + e.getMessage(), e);
        }

        String year = primeraOption(home, "#year-selector");
        String date = primeraOption(home, "#date-selector");
        if (year == null || date == null) {
            log.warn("No se encontraron selects de fecha (#year-selector/#date-selector). " +
                     "¿Cambió el formulario de Mercasa?");
            return List.of();
        }
        LocalDate fechaSeleccionada = LocalDate.parse(date); // formato ISO yyyy-MM-dd

        // 2. POST del formulario con esa fecha: el servidor devuelve la tabla rellena.
        Document doc;
        try {
            doc = Jsoup.connect(url)
                .userAgent(USER_AGENT)
                .timeout(TIMEOUT_MS)
                .data("year-my", year)
                .data("date-my", date)
                .method(Connection.Method.POST)
                .execute()
                .parse();
        } catch (IOException e) {
            throw new ScrapingException("Falló el POST de precios a Mercasa: " + e.getMessage(), e);
        }

        Element tabla = doc.selectFirst("#resultsTable");
        if (tabla == null) {
            log.warn("No se encontró #resultsTable tras el POST. ¿Cambió el HTML de Mercasa?");
            return List.of();
        }

        List<PrecioCrudo> resultado = parsearTablaPivote(tabla, fechaSeleccionada);
        log.info("Parseados {} precios para la fecha {}", resultado.size(), date);
        return resultado;
    }

    /** Recorre la tabla pivote manteniendo el contexto de mercados y fechas por sección. */
    private List<PrecioCrudo> parsearTablaPivote(Element tabla, LocalDate fechaSeleccionada) {
        List<PrecioCrudo> resultado = new ArrayList<>();
        List<String> mercados = List.of();
        List<LocalDate> fechas = List.of();

        for (Element fila : tabla.select("tr")) {
            Elements mercadoHeaders = fila.select("th[colspan]");
            if (!mercadoHeaders.isEmpty()) {
                // Cabecera de sección: extrae el orden de mercados.
                mercados = mercadoHeaders.eachText();
                continue;
            }

            Elements th = fila.select("th");
            if (!th.isEmpty() && fila.select("td").isEmpty()) {
                // Subcabecera de fechas: [Productos] dd/MM dd/MM ... (se repiten por mercado).
                fechas = parsearFechas(th, fechaSeleccionada);
                continue;
            }

            Elements td = fila.select("td");
            if (td.isEmpty() || mercados.isEmpty() || fechas.isEmpty()) {
                continue;
            }
            parsearFilaProducto(td, mercados, fechas, resultado);
        }
        return resultado;
    }

    /**
     * Una fila de producto: td[0]=nombre, td[1..]=precios.
     * La columna de precio k mapea a (mercado k/2, fecha k%2).
     */
    private void parsearFilaProducto(Elements td, List<String> mercados,
                                     List<LocalDate> fechas, List<PrecioCrudo> out) {
        String producto = limpiar(td.get(0).text());
        if (producto.isBlank()) {
            return; // filas separadoras (&nbsp;)
        }
        int fechasPorMercado = fechas.size();
        for (int k = 0; k + 1 < td.size(); k++) {
            BigDecimal precio = parsearPrecioEuropeo(td.get(k + 1).text());
            if (precio == null) {
                continue; // celda vacía o no numérica
            }
            int idxMercado = k / fechasPorMercado;
            int idxFecha = k % fechasPorMercado;
            if (idxMercado >= mercados.size()) {
                break;
            }
            out.add(new PrecioCrudo(
                producto.toLowerCase(), mercados.get(idxMercado), fechas.get(idxFecha), precio));
        }
    }

    /** Extrae las fechas distintas (dd/MM) de la subcabecera y les asigna el año correcto. */
    private List<LocalDate> parsearFechas(Elements th, LocalDate fechaSeleccionada) {
        List<LocalDate> fechas = new ArrayList<>();
        for (Element e : th) {
            String t = e.text().trim();
            if (!t.matches("\\d{2}/\\d{2}")) {
                continue; // salta el "Productos" inicial
            }
            int dia = Integer.parseInt(t.substring(0, 2));
            int mes = Integer.parseInt(t.substring(3, 5));
            LocalDate f = LocalDate.of(fechaSeleccionada.getYear(), mes, dia);
            // Si la fecha cae después de la seleccionada, es del año anterior (caso enero).
            if (f.isAfter(fechaSeleccionada)) {
                f = f.minusYears(1);
            }
            if (!fechas.contains(f)) {
                fechas.add(f);
            }
        }
        return fechas;
    }

    private String primeraOption(Document doc, String selectId) {
        for (Element opt : doc.select(selectId + " option")) {
            String v = opt.attr("value").trim();
            if (!v.isEmpty()) {
                return v;
            }
        }
        return null;
    }

    private String limpiar(String texto) {
        return texto.replace(' ', ' ').trim();
    }

    /**
     * Convierte "1,20" (formato europeo) en BigDecimal("1.20").
     * Devuelve null si la celda está vacía o no es numérica (p.ej. "-", "&nbsp;").
     */
    private BigDecimal parsearPrecioEuropeo(String texto) {
        String limpio = limpiar(texto).replace("€", "").replace(" ", "").replace(",", ".");
        if (limpio.isEmpty()) {
            return null;
        }
        try {
            return new BigDecimal(limpio);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}

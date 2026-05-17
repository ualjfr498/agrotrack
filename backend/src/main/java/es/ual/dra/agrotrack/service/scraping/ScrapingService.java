package es.ual.dra.agrotrack.service.scraping;

import es.ual.dra.agrotrack.model.entity.MercadoMayorista;
import es.ual.dra.agrotrack.model.entity.PrecioMayorista;
import es.ual.dra.agrotrack.model.entity.Producto;
import es.ual.dra.agrotrack.model.entity.ScrapingLog;
import es.ual.dra.agrotrack.model.enums.ScrapingEstado;
import es.ual.dra.agrotrack.repository.MercadoMayoristaRepository;
import es.ual.dra.agrotrack.repository.PrecioMayoristaRepository;
import es.ual.dra.agrotrack.repository.ProductoRepository;
import es.ual.dra.agrotrack.repository.ScrapingLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.text.Normalizer;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Servicio de scraping — patrón Strategy:
 * implementa la interfaz ScrapingStrategy para poder ser sustituido
 * por otras fuentes de datos sin cambiar el scheduler ni los servicios superiores.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ScrapingService implements ScrapingStrategy {

    private static final String MERCASA_URL =
        "https://www.mercasa.es/precios-y-mercados-mayoristas/";
    private static final int TIMEOUT_MS = 30_000;

    private final ProductoRepository productoRepository;
    private final MercadoMayoristaRepository mercadoRepository;
    private final PrecioMayoristaRepository precioRepository;
    private final ScrapingLogRepository scrapingLogRepository;

    @Override
    @Transactional
    public ScrapingLog ejecutar(boolean disparoManual) {
        LocalDateTime inicio = LocalDateTime.now();
        ScrapingLog log = ScrapingLog.builder()
            .inicioAt(inicio)
            .estado(ScrapingEstado.EXITOSO)
            .disparoManual(disparoManual)
            .build();

        int guardados = 0;
        try {
            Document doc = Jsoup.connect(MERCASA_URL)
                .userAgent("Mozilla/5.0 (compatible; AgroTrack/1.0)")
                .timeout(TIMEOUT_MS)
                .get();

            /*
             * Mercasa publica una tabla por cada mercado. La estructura típica es:
             *   <table class="precios-table"> o similar con cabecera de mercado.
             * La lógica parsea todas las tablas de la página y asocia filas al mercado
             * cuyo nombre aparece en la cabecera o en el bloque previo.
             */
            Elements tablas = doc.select("table");

            for (Element tabla : tablas) {
                String nombreMercado = resolverNombreMercado(tabla);
                if (nombreMercado == null) continue;

                Optional<MercadoMayorista> mercadoOpt = mercadoRepository.findByClaveScraping(nombreMercado);
                if (mercadoOpt.isEmpty()) {
                    log.info("Mercado no registrado, ignorado: {}", nombreMercado);
                    continue;
                }
                MercadoMayorista mercado = mercadoOpt.get();

                Elements filas = tabla.select("tr");
                for (Element fila : filas) {
                    Elements celdas = fila.select("td");
                    if (celdas.size() < 2) continue;

                    String nombreProducto = celdas.get(0).text().trim();
                    String precioTexto    = celdas.get(1).text().trim()
                        .replace(",", ".")
                        .replaceAll("[^0-9.]", "");

                    if (nombreProducto.isBlank() || precioTexto.isBlank()) continue;

                    Optional<Producto> productoOpt = resolverProducto(nombreProducto);
                    if (productoOpt.isEmpty()) continue;

                    Producto producto = productoOpt.get();
                    LocalDate hoy = LocalDate.now();

                    // Evitamos duplicar precios si el scraper se ejecuta más de una vez al día
                    if (precioRepository.existsByProductoIdAndMercadoIdAndFecha(
                            producto.getId(), mercado.getId(), hoy)) {
                        continue;
                    }

                    try {
                        BigDecimal precio = new BigDecimal(precioTexto);
                        PrecioMayorista pm = PrecioMayorista.builder()
                            .producto(producto)
                            .mercado(mercado)
                            .precio(precio)
                            .fecha(hoy)
                            .build();
                        precioRepository.save(pm);
                        guardados++;
                    } catch (NumberFormatException e) {
                        log.warn("Precio no parseable para '{}': '{}'", nombreProducto, precioTexto);
                    }
                }
            }

            log.info("Scraping completado: {} precios guardados", guardados);

        } catch (Exception e) {
            log.error("Error durante el scraping de Mercasa", e);
            log.setEstado(guardados > 0 ? ScrapingEstado.PARCIAL : ScrapingEstado.FALLIDO);
            log.setMensajeError(e.getMessage());
        }

        log.setFinAt(LocalDateTime.now());
        log.setPreciosGuardados(guardados);
        return scrapingLogRepository.save(log);
    }

    // ── Helpers ─────────────────────────────────────────────────────────────

    /**
     * Intenta determinar a qué mercado corresponde una tabla de precios.
     * Busca la clave del mercado (claveScraping) en el texto del header previo
     * o en el caption de la tabla.
     */
    private String resolverNombreMercado(Element tabla) {
        // Buscar caption
        Element caption = tabla.selectFirst("caption");
        if (caption != null) {
            String texto = normalizar(caption.text());
            return matchMercado(texto);
        }
        // Buscar encabezado hermano anterior
        Element prev = tabla.previousElementSibling();
        while (prev != null) {
            String tag = prev.tagName();
            if (tag.matches("h[1-6]") || tag.equals("p") || tag.equals("div")) {
                String texto = normalizar(prev.text());
                String match = matchMercado(texto);
                if (match != null) return match;
            }
            prev = prev.previousElementSibling();
        }
        return null;
    }

    private String matchMercado(String texto) {
        if (texto.contains("madrid"))    return "mercamadrid";
        if (texto.contains("barna") || texto.contains("barcelona")) return "mercabarna";
        if (texto.contains("bilbao"))    return "mercabilbao";
        if (texto.contains("valencia"))  return "mercavalencia";
        if (texto.contains("sevilla"))   return "mercasevilla";
        return null;
    }

    /**
     * Busca el producto en BD por nombre normalizado.
     * Normalizar = lowercase + quitar tildes + trim.
     */
    private Optional<Producto> resolverProducto(String nombreScrapeado) {
        String norm = normalizar(nombreScrapeado);
        return productoRepository.findByNombreNormalizado(norm);
    }

    public static String normalizar(String texto) {
        if (texto == null) return "";
        String s = Normalizer.normalize(texto.toLowerCase().trim(), Normalizer.Form.NFD);
        return s.replaceAll("\\p{InCombiningDiacriticalMarks}+", "").trim();
    }
}

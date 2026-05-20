package es.ual.dra.agrotrack.service.scraping;

import es.ual.dra.agrotrack.model.entity.MercadoMayorista;
import es.ual.dra.agrotrack.model.entity.PrecioMayorista;
import es.ual.dra.agrotrack.model.entity.Producto;
import es.ual.dra.agrotrack.model.entity.ScrapingLog;
import es.ual.dra.agrotrack.model.enums.EstadoScraping;
import es.ual.dra.agrotrack.repository.MercadoMayoristaRepository;
import es.ual.dra.agrotrack.repository.PrecioMayoristaRepository;
import es.ual.dra.agrotrack.repository.ProductoRepository;
import es.ual.dra.agrotrack.repository.ScrapingLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Orquesta el ciclo completo de scraping:
 *   1. Pide precios crudos a la Strategy actual.
 *   2. Mapea cada nombre de producto/mercado a su entidad en BD.
 *   3. Descarta duplicados (mismo producto+mercado+fecha).
 *   4. Persiste los precios nuevos.
 *   5. Registra el resultado en ScrapingLog (EXITOSO / PARCIAL / FALLIDO).
 *
 * Llamadores: ScrapingScheduler (cron) y AdminPrecioController (manual).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ScrapingService {

    private final ScrapingStrategy strategy;
    private final ProductoRepository productoRepo;
    private final MercadoMayoristaRepository mercadoRepo;
    private final PrecioMayoristaRepository precioRepo;
    private final ScrapingLogRepository logRepo;

    @Transactional
    public ScrapingLog ejecutar() {
        log.info("[Scraping] Inicio con estrategia {}", strategy.nombre());
        long t0 = System.currentTimeMillis();

        ScrapingLog logEntry = new ScrapingLog();
        logEntry.setFechaEjecucion(LocalDateTime.now());

        try {
            List<PrecioCrudo> crudos = strategy.obtenerPrecios();
            ResultadoMapeo res = mapearYGuardar(crudos);

            logEntry.setEstado(res.estado());
            logEntry.setFilasInsertadas(res.insertados());
            logEntry.setMensaje(res.mensaje());
        } catch (ScrapingException e) {
            log.error("[Scraping] Fallo total: {}", e.getMessage(), e);
            logEntry.setEstado(EstadoScraping.FALLIDO);
            logEntry.setFilasInsertadas(0);
            logEntry.setMensaje(e.getMessage());
        }

        logEntry.setDuracionMs((int) (System.currentTimeMillis() - t0));
        ScrapingLog guardado = logRepo.save(logEntry);
        log.info("[Scraping] Fin — estado={} insertados={} duración={}ms",
            guardado.getEstado(), guardado.getFilasInsertadas(), guardado.getDuracionMs());
        return guardado;
    }

    private ResultadoMapeo mapearYGuardar(List<PrecioCrudo> crudos) {
        if (crudos.isEmpty()) {
            return new ResultadoMapeo(EstadoScraping.FALLIDO, 0,
                "La fuente no devolvió ninguna fila (¿selectores HTML obsoletos?).");
        }

        // Cache en memoria para evitar N+1 queries — todos los mercados
        // y productos se cargan una vez al inicio.
        Map<String, MercadoMayorista> mercadosPorNombre = indexarMercados();
        Map<String, Producto> productosPorNombre = indexarProductos();

        int insertados = 0;
        int duplicados = 0;
        int sinMapeo = 0;

        for (PrecioCrudo crudo : crudos) {
            Producto producto = productosPorNombre.get(crudo.nombreProducto().toLowerCase());
            MercadoMayorista mercado = mercadosPorNombre.get(crudo.nombreMercado().toLowerCase());

            if (producto == null || mercado == null) {
                log.debug("Sin mapeo: producto='{}' mercado='{}'",
                    crudo.nombreProducto(), crudo.nombreMercado());
                sinMapeo++;
                continue;
            }

            boolean yaExiste = precioRepo.existsByProductoIdAndMercadoIdAndFecha(
                producto.getId(), mercado.getId(), crudo.fecha());
            if (yaExiste) {
                duplicados++;
                continue;
            }

            PrecioMayorista nuevo = new PrecioMayorista();
            nuevo.setProducto(producto);
            nuevo.setMercado(mercado);
            nuevo.setFecha(crudo.fecha());
            nuevo.setPrecioKg(crudo.precioKg());
            precioRepo.save(nuevo);
            insertados++;
        }

        String resumen = String.format(
            "Recibidos=%d insertados=%d duplicados=%d sin_mapeo=%d",
            crudos.size(), insertados, duplicados, sinMapeo);

        EstadoScraping estado = (sinMapeo == 0)
            ? EstadoScraping.EXITOSO
            : EstadoScraping.PARCIAL;

        return new ResultadoMapeo(estado, insertados, resumen);
    }

    private Map<String, MercadoMayorista> indexarMercados() {
        Map<String, MercadoMayorista> map = new HashMap<>();
        mercadoRepo.findAll().forEach(m -> map.put(m.getNombre().toLowerCase(), m));
        return map;
    }

    private Map<String, Producto> indexarProductos() {
        Map<String, Producto> map = new HashMap<>();
        productoRepo.findAll().forEach(p -> map.put(p.getNombre().toLowerCase(), p));
        return map;
    }

    public Optional<ScrapingLog> ultimoLog() {
        List<ScrapingLog> top = logRepo.findTop20ByOrderByFechaEjecucionDesc();
        return top.isEmpty() ? Optional.empty() : Optional.of(top.get(0));
    }

    private record ResultadoMapeo(EstadoScraping estado, int insertados, String mensaje) {}
}

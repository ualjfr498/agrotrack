package es.ual.dra.agrotrack.service;

import es.ual.dra.agrotrack.model.entity.AlertaPrecio;
import es.ual.dra.agrotrack.model.entity.PrecioMayorista;
import es.ual.dra.agrotrack.model.entity.Producto;
import es.ual.dra.agrotrack.repository.AlertaPrecioRepository;
import es.ual.dra.agrotrack.repository.PrecioMayoristaRepository;
import es.ual.dra.agrotrack.repository.ProductoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Patrón Observer — reacciona a cada actualización de precios.
 * El ScrapingScheduler notifica a AlertaService tras guardar nuevos precios;
 * AlertaService evalúa todos los umbrales activos y delega en NotificacionService
 * el envío de emails cuando el precio supera el umbral configurado.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class AlertaService {

    private final ProductoRepository productoRepository;
    private final PrecioMayoristaRepository precioRepository;
    private final AlertaPrecioRepository alertaRepository;
    private final NotificacionService notificacionService;

    @Transactional
    public void evaluarAlertas() {
        log.info("Evaluando alertas de precio activas...");
        List<Producto> productos = productoRepository.findAll();

        for (Producto producto : productos) {
            List<PrecioMayorista> preciosHoy =
                precioRepository.findByProductoIdAndFecha(producto.getId(), LocalDate.now());

            if (preciosHoy.isEmpty()) continue;

            // Precio medio entre todos los mercados hoy
            BigDecimal media = preciosHoy.stream()
                .map(PrecioMayorista::getPrecio)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(preciosHoy.size()), 2, java.math.RoundingMode.HALF_UP);

            List<AlertaPrecio> alertas =
                alertaRepository.findByProductoIdAndActivaTrue(producto.getId());

            for (AlertaPrecio alerta : alertas) {
                if (media.compareTo(alerta.getPrecioUmbral()) >= 0) {
                    notificacionService.enviarAlertaPrecio(alerta, media, producto);
                    alerta.setUltimaNotificacion(LocalDateTime.now());
                    alertaRepository.save(alerta);
                    log.info("Alerta disparada: usuario={} producto={} precio={}",
                        alerta.getUsuario().getEmail(), producto.getNombre(), media);
                }
            }
        }
    }

    // ── CRUD alertas ────────────────────────────────────────────────────────

    public List<AlertaPrecio> listarPorUsuario(Long usuarioId) {
        return alertaRepository.findByUsuarioId(usuarioId);
    }

    public AlertaPrecio crear(AlertaPrecio alerta) {
        return alertaRepository.save(alerta);
    }

    @Transactional
    public AlertaPrecio toggleActiva(Long alertaId, Long usuarioId) {
        AlertaPrecio alerta = alertaRepository.findByIdAndUsuarioId(alertaId, usuarioId)
            .orElseThrow(() -> new RuntimeException("Alerta no encontrada o no pertenece al usuario"));
        alerta.setActiva(!alerta.isActiva());
        return alertaRepository.save(alerta);
    }

    public void eliminar(Long alertaId, Long usuarioId) {
        AlertaPrecio alerta = alertaRepository.findByIdAndUsuarioId(alertaId, usuarioId)
            .orElseThrow(() -> new RuntimeException("Alerta no encontrada o no pertenece al usuario"));
        alertaRepository.delete(alerta);
    }
}

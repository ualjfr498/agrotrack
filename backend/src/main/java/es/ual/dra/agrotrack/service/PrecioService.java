package es.ual.dra.agrotrack.service;

import es.ual.dra.agrotrack.model.entity.PrecioMayorista;
import es.ual.dra.agrotrack.repository.PrecioMayoristaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PrecioService {

    private final PrecioMayoristaRepository precioRepository;

    /** Últimos precios de todos los productos (para la home) */
    public List<PrecioMayorista> findUltimos() {
        return precioRepository.findUltimosPrecios();
    }

    /** Historial de un producto para la gráfica (por defecto 90 días) */
    public List<PrecioMayorista> findHistorial(Long productoId, int dias) {
        LocalDate desde = LocalDate.now().minusDays(dias);
        return precioRepository.findByProductoIdAndFechaAfter(productoId, desde);
    }

    /** Precios actuales de un producto desglosados por mercado */
    public List<PrecioMayorista> findActualesByProducto(Long productoId) {
        return precioRepository.findPreciosActualesByProductoId(productoId);
    }
}

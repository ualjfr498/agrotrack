package es.ual.dra.agrotrack.service;

import es.ual.dra.agrotrack.dto.response.PrecioResponse;
import es.ual.dra.agrotrack.repository.PrecioMayoristaRepository;
import es.ual.dra.agrotrack.repository.ProductoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PrecioService {

    private static final int DIAS_HISTORICO_POR_DEFECTO = 90;

    private final PrecioMayoristaRepository precioRepo;
    private final ProductoRepository productoRepo;

    // readOnly = true: sesión abierta durante el mapeo para inicializar las
    // relaciones LAZY (producto y mercado) de cada PrecioMayorista sin saltar
    // LazyInitializationException (open-in-view está desactivado).
    @Transactional(readOnly = true)
    public List<PrecioResponse> historialDeProducto(Long productoId) {
        if (!productoRepo.existsById(productoId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no encontrado");
        }
        LocalDate desde = LocalDate.now().minusDays(DIAS_HISTORICO_POR_DEFECTO);
        return precioRepo
            .findByProductoIdAndFechaGreaterThanEqualOrderByFechaAsc(productoId, desde)
            .stream()
            .map(PrecioResponse::from)
            .toList();
    }

    @Transactional(readOnly = true)
    public List<PrecioResponse> ultimosDeProducto(Long productoId) {
        return precioRepo.findByProductoIdOrderByFechaDesc(productoId)
            .stream()
            .map(PrecioResponse::from)
            .toList();
    }
}

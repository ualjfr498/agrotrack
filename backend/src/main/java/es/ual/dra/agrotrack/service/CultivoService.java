package es.ual.dra.agrotrack.service;

import es.ual.dra.agrotrack.dto.request.CultivoRequest;
import es.ual.dra.agrotrack.dto.response.CultivoResponse;
import es.ual.dra.agrotrack.model.entity.CultivoParcela;
import es.ual.dra.agrotrack.model.entity.Parcela;
import es.ual.dra.agrotrack.model.entity.Producto;
import es.ual.dra.agrotrack.model.enums.EstadoCultivo;
import es.ual.dra.agrotrack.repository.CultivoParcelaRepository;
import es.ual.dra.agrotrack.repository.ProductoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

/**
 * Cultivos plantados en las parcelas del agricultor. Las operaciones validan
 * que la parcela implicada pertenece al usuario autenticado (vía ParcelaService).
 */
@Service
@RequiredArgsConstructor
public class CultivoService {

    private final CultivoParcelaRepository cultivoRepo;
    private final ProductoRepository productoRepo;
    private final ParcelaService parcelaService;

    // readOnly = true: sesión abierta durante el mapeo para inicializar las
    // relaciones LAZY (parcela y producto) de cada cultivo sin saltar
    // LazyInitializationException (open-in-view está desactivado).
    @Transactional(readOnly = true)
    public List<CultivoResponse> listarMios(Long usuarioId) {
        return cultivoRepo.findByParcelaUsuarioId(usuarioId).stream()
            .map(CultivoResponse::from)
            .toList();
    }

    @Transactional(readOnly = true)
    public List<CultivoResponse> listarDeParcela(Long usuarioId, Long parcelaId) {
        parcelaService.obtenerPropia(usuarioId, parcelaId); // valida pertenencia
        return cultivoRepo.findByParcelaId(parcelaId).stream()
            .map(CultivoResponse::from)
            .toList();
    }

    @Transactional
    public CultivoResponse crear(Long usuarioId, CultivoRequest req) {
        Parcela parcela = parcelaService.obtenerPropia(usuarioId, req.parcelaId());
        Producto producto = productoRepo.findById(req.productoId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Producto no encontrado"));

        CultivoParcela c = new CultivoParcela();
        c.setParcela(parcela);
        c.setProducto(producto);
        c.setFechaSiembra(req.fechaSiembra());
        c.setEstado(req.estado() != null ? req.estado() : EstadoCultivo.SEMBRADO);
        c.setNotas(req.notas());
        return CultivoResponse.from(cultivoRepo.save(c));
    }

    /** Elimina un cultivo, validando que su parcela pertenece al usuario. */
    @Transactional
    public void eliminar(Long usuarioId, Long cultivoId) {
        CultivoParcela c = cultivoRepo.findById(cultivoId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cultivo no encontrado"));
        // Reutiliza la guarda de pertenencia de la parcela (404 si es de otro).
        parcelaService.obtenerPropia(usuarioId, c.getParcela().getId());
        cultivoRepo.delete(c);
    }
}

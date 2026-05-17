package es.ual.dra.agrotrack.service;

import es.ual.dra.agrotrack.model.entity.AppUser;
import es.ual.dra.agrotrack.model.entity.CultivoParcela;
import es.ual.dra.agrotrack.model.entity.Parcela;
import es.ual.dra.agrotrack.model.entity.Producto;
import es.ual.dra.agrotrack.repository.CultivoParcelaRepository;
import es.ual.dra.agrotrack.repository.ParcelaRepository;
import es.ual.dra.agrotrack.repository.ProductoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ParcelaService {

    private final ParcelaRepository parcelaRepository;
    private final CultivoParcelaRepository cultivoRepository;
    private final ProductoRepository productoRepository;

    public List<Parcela> listarPorUsuario(Long usuarioId) {
        return parcelaRepository.findByUsuarioId(usuarioId);
    }

    @Transactional
    public Parcela crear(Parcela parcela) {
        return parcelaRepository.save(parcela);
    }

    @Transactional
    public Parcela actualizar(Long id, Long usuarioId, Parcela datos) {
        Parcela parcela = parcelaRepository.findByIdAndUsuarioId(id, usuarioId)
            .orElseThrow(() -> new RuntimeException("Parcela no encontrada"));
        parcela.setNombre(datos.getNombre());
        parcela.setSuperficie(datos.getSuperficie());
        parcela.setDescripcion(datos.getDescripcion());
        return parcelaRepository.save(parcela);
    }

    @Transactional
    public void eliminar(Long id, Long usuarioId) {
        Parcela parcela = parcelaRepository.findByIdAndUsuarioId(id, usuarioId)
            .orElseThrow(() -> new RuntimeException("Parcela no encontrada"));
        parcelaRepository.delete(parcela);
    }

    // ── Cultivos ─────────────────────────────────────────────────────────────

    @Transactional
    public CultivoParcela agregarCultivo(Long parcelaId, Long usuarioId, CultivoParcela cultivo) {
        Parcela parcela = parcelaRepository.findByIdAndUsuarioId(parcelaId, usuarioId)
            .orElseThrow(() -> new RuntimeException("Parcela no encontrada"));

        Producto producto = productoRepository.findById(cultivo.getProducto().getId())
            .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

        cultivo.setParcela(parcela);
        cultivo.setProducto(producto);
        return cultivoRepository.save(cultivo);
    }

    @Transactional
    public CultivoParcela actualizarCultivo(Long cultivoId, Long usuarioId, CultivoParcela datos) {
        CultivoParcela cultivo = cultivoRepository.findByIdAndParcelaUsuarioId(cultivoId, usuarioId)
            .orElseThrow(() -> new RuntimeException("Cultivo no encontrado"));
        if (datos.getEstado() != null)   cultivo.setEstado(datos.getEstado());
        if (datos.getNotas() != null)    cultivo.setNotas(datos.getNotas());
        if (datos.getFechaCosecha() != null) cultivo.setFechaCosecha(datos.getFechaCosecha());
        return cultivoRepository.save(cultivo);
    }

    @Transactional
    public void eliminarCultivo(Long cultivoId, Long usuarioId) {
        CultivoParcela cultivo = cultivoRepository.findByIdAndParcelaUsuarioId(cultivoId, usuarioId)
            .orElseThrow(() -> new RuntimeException("Cultivo no encontrado"));
        cultivoRepository.delete(cultivo);
    }

    public List<CultivoParcela> cultivosActivosPorUsuario(Long usuarioId) {
        return cultivoRepository.findActivosByUsuarioId(usuarioId);
    }
}

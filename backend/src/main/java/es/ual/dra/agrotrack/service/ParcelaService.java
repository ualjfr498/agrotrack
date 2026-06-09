package es.ual.dra.agrotrack.service;

import es.ual.dra.agrotrack.dto.request.ParcelaRequest;
import es.ual.dra.agrotrack.dto.response.ParcelaResponse;
import es.ual.dra.agrotrack.model.entity.AppUser;
import es.ual.dra.agrotrack.model.entity.Parcela;
import es.ual.dra.agrotrack.repository.AppUserRepository;
import es.ual.dra.agrotrack.repository.CultivoParcelaRepository;
import es.ual.dra.agrotrack.repository.ParcelaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

/**
 * Parcelas del agricultor. Todas las operaciones están acotadas al usuario
 * autenticado (su id llega resuelto desde el SecurityContext, sea por JWT del
 * propio usuario o por el ServiceTokenFilter cuando la llamada viene del
 * mcp-server actuando en su nombre).
 */
@Service
@RequiredArgsConstructor
public class ParcelaService {

    private final ParcelaRepository parcelaRepo;
    private final AppUserRepository userRepo;
    private final CultivoParcelaRepository cultivoRepo;

    public List<ParcelaResponse> listarMias(Long usuarioId) {
        return parcelaRepo.findByUsuarioId(usuarioId).stream()
            .map(ParcelaResponse::from)
            .toList();
    }

    @Transactional
    public ParcelaResponse crear(Long usuarioId, ParcelaRequest req) {
        AppUser usuario = userRepo.findById(usuarioId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario no encontrado"));

        Parcela p = new Parcela();
        p.setUsuario(usuario);
        p.setNombre(req.nombre());
        p.setSuperficieM2(req.superficieM2());
        p.setDescripcion(req.descripcion());
        p.setImagen(req.imagen());
        return ParcelaResponse.from(parcelaRepo.save(p));
    }

    /** Edita una parcela propia del usuario. */
    @Transactional
    public ParcelaResponse editar(Long usuarioId, Long parcelaId, ParcelaRequest req) {
        Parcela p = obtenerPropia(usuarioId, parcelaId);
        p.setNombre(req.nombre());
        p.setSuperficieM2(req.superficieM2());
        p.setDescripcion(req.descripcion());
        p.setImagen(req.imagen());
        return ParcelaResponse.from(parcelaRepo.save(p));
    }

    /** Elimina una parcela propia y, en cascada, sus cultivos. */
    @Transactional
    public void eliminar(Long usuarioId, Long parcelaId) {
        Parcela p = obtenerPropia(usuarioId, parcelaId);
        cultivoRepo.deleteByParcelaId(p.getId());
        parcelaRepo.delete(p);
    }

    /** Recupera una parcela verificando que pertenece al usuario; si no, 404. */
    public Parcela obtenerPropia(Long usuarioId, Long parcelaId) {
        Parcela p = parcelaRepo.findById(parcelaId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Parcela no encontrada"));
        if (!p.getUsuario().getId().equals(usuarioId)) {
            // No revelamos que existe pero es de otro: mismo 404.
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Parcela no encontrada");
        }
        return p;
    }
}

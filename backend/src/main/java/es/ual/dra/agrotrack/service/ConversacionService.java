package es.ual.dra.agrotrack.service;

import es.ual.dra.agrotrack.dto.MensajeDto;
import es.ual.dra.agrotrack.dto.response.ConversacionDetalle;
import es.ual.dra.agrotrack.dto.response.ConversacionResumen;
import es.ual.dra.agrotrack.model.entity.AppUser;
import es.ual.dra.agrotrack.model.entity.Conversacion;
import es.ual.dra.agrotrack.model.entity.MensajeChat;
import es.ual.dra.agrotrack.model.enums.RolMensaje;
import es.ual.dra.agrotrack.repository.AppUserRepository;
import es.ual.dra.agrotrack.repository.ConversacionRepository;
import es.ual.dra.agrotrack.repository.MensajeChatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

/**
 * Gestiona las conversaciones del asistente: listarlas, recuperarlas, crearlas,
 * añadir mensajes y borrarlas. Es la persistencia que permite al usuario retomar
 * un chat y conservar los consejos que le dio el asistente.
 */
@Service
@RequiredArgsConstructor
public class ConversacionService {

    /** Nº de mensajes recientes que se reenvían al modelo como contexto. */
    private static final int CONTEXTO_MAX = 10;

    private final ConversacionRepository conversacionRepo;
    private final MensajeChatRepository mensajeRepo;
    private final AppUserRepository userRepo;

    public List<ConversacionResumen> listar(Long usuarioId) {
        return conversacionRepo.findByUsuarioIdOrderByFechaActualizacionDesc(usuarioId).stream()
            .map(ConversacionResumen::from)
            .toList();
    }

    @Transactional(readOnly = true)
    public ConversacionDetalle obtener(Long usuarioId, Long conversacionId) {
        Conversacion c = obtenerPropia(usuarioId, conversacionId);
        List<MensajeDto> mensajes = mensajeRepo.findByConversacionIdOrderByFechaAsc(c.getId()).stream()
            .map(MensajeDto::from)
            .toList();
        return new ConversacionDetalle(c.getId(), c.getTitulo(), mensajes);
    }

    @Transactional
    public void eliminar(Long usuarioId, Long conversacionId) {
        Conversacion c = obtenerPropia(usuarioId, conversacionId);
        mensajeRepo.deleteByConversacionId(c.getId());
        conversacionRepo.delete(c);
    }

    /** Crea una conversación nueva con un título derivado del primer mensaje. */
    @Transactional
    public Conversacion crear(Long usuarioId, String primerMensaje) {
        AppUser usuario = userRepo.findById(usuarioId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario no encontrado"));
        Conversacion c = new Conversacion();
        c.setUsuario(usuario);
        c.setTitulo(tituloDesde(primerMensaje));
        return conversacionRepo.save(c);
    }

    /** Recupera una conversación verificando que pertenece al usuario; si no, 404. */
    public Conversacion obtenerPropia(Long usuarioId, Long conversacionId) {
        Conversacion c = conversacionRepo.findById(conversacionId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Conversación no encontrada"));
        if (!c.getUsuario().getId().equals(usuarioId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Conversación no encontrada");
        }
        return c;
    }

    @Transactional
    public void guardarMensaje(Conversacion c, RolMensaje rol, String contenido) {
        MensajeChat m = new MensajeChat();
        m.setConversacion(c);
        m.setRol(rol);
        m.setContenido(contenido);
        mensajeRepo.save(m);
        // Toca la conversación para que suba en la lista (ordenada por actualización).
        c.setFechaActualizacion(java.time.LocalDateTime.now());
        conversacionRepo.save(c);
    }

    /** Últimos N mensajes de la conversación, en orden cronológico, como contexto. */
    @Transactional(readOnly = true)
    public List<MensajeDto> contextoReciente(Long conversacionId) {
        List<MensajeChat> todos = mensajeRepo.findByConversacionIdOrderByFechaAsc(conversacionId);
        int desde = Math.max(0, todos.size() - CONTEXTO_MAX);
        return todos.subList(desde, todos.size()).stream()
            .map(MensajeDto::from)
            .toList();
    }

    private String tituloDesde(String mensaje) {
        String t = mensaje.strip();
        return t.length() <= 60 ? t : t.substring(0, 57) + "…";
    }
}

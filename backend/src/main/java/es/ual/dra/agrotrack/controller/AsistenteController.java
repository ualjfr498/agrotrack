package es.ual.dra.agrotrack.controller;

import es.ual.dra.agrotrack.dto.MensajeDto;
import es.ual.dra.agrotrack.dto.request.AsistenteRequest;
import es.ual.dra.agrotrack.dto.response.AsistenteResponse;
import es.ual.dra.agrotrack.model.entity.Conversacion;
import es.ual.dra.agrotrack.model.enums.RolMensaje;
import es.ual.dra.agrotrack.security.AppUserPrincipal;
import es.ual.dra.agrotrack.service.ConversacionService;
import es.ual.dra.agrotrack.service.ai.AsistenteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Endpoint del asistente IA. Es PÚBLICO: un invitado puede usarlo para consultar
 * precios y catálogo (sin persistencia; el contexto lo aporta el propio frontend).
 *
 * Si hay usuario autenticado, la conversación se PERSISTE: se crea o continúa una
 * {@link Conversacion}, se guardan los mensajes y el contexto del modelo se lee de
 * la BD, de modo que el usuario puede recuperar y continuar sus chats más tarde.
 */
@RestController
@RequestMapping("/api/asistente")
@RequiredArgsConstructor
public class AsistenteController {

    private final AsistenteService asistenteService;
    private final ConversacionService conversacionService;

    @PostMapping("/consulta")
    public AsistenteResponse consultar(
            @AuthenticationPrincipal AppUserPrincipal user,
            @Valid @RequestBody AsistenteRequest req) {

        // Invitado: sin persistencia; el contexto viene del frontend.
        if (user == null) {
            String respuesta = asistenteService.consultar(req.mensaje(), null, req.historial());
            return new AsistenteResponse(respuesta, null);
        }

        // Registrado: crea o continúa la conversación y persiste los mensajes.
        Conversacion conv = (req.conversacionId() != null)
            ? conversacionService.obtenerPropia(user.getId(), req.conversacionId())
            : conversacionService.crear(user.getId(), req.mensaje());

        // Contexto = mensajes previos de la conversación (antes de añadir el actual).
        List<MensajeDto> contexto = conversacionService.contextoReciente(conv.getId());
        String respuesta = asistenteService.consultar(req.mensaje(), user.getUsername(), contexto);

        conversacionService.guardarMensaje(conv, RolMensaje.USER, req.mensaje());
        conversacionService.guardarMensaje(conv, RolMensaje.ASSISTANT, respuesta);

        return new AsistenteResponse(respuesta, conv.getId());
    }
}

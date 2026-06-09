package es.ual.dra.agrotrack.controller;

import es.ual.dra.agrotrack.dto.response.ConversacionDetalle;
import es.ual.dra.agrotrack.dto.response.ConversacionResumen;
import es.ual.dra.agrotrack.security.AppUserPrincipal;
import es.ual.dra.agrotrack.service.ConversacionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Conversaciones del asistente para el usuario autenticado: listarlas, abrir una
 * y borrarlas. La creación y la adición de mensajes ocurren al usar el asistente.
 */
@RestController
@RequestMapping("/api/conversaciones")
@RequiredArgsConstructor
public class ConversacionController {

    private final ConversacionService conversacionService;

    @GetMapping
    public List<ConversacionResumen> listar(@AuthenticationPrincipal AppUserPrincipal user) {
        return conversacionService.listar(user.getId());
    }

    @GetMapping("/{id}")
    public ConversacionDetalle obtener(
            @AuthenticationPrincipal AppUserPrincipal user,
            @PathVariable Long id) {
        return conversacionService.obtener(user.getId(), id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(
            @AuthenticationPrincipal AppUserPrincipal user,
            @PathVariable Long id) {
        conversacionService.eliminar(user.getId(), id);
        return ResponseEntity.noContent().build();
    }
}

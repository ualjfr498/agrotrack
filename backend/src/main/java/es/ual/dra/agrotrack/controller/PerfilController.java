package es.ual.dra.agrotrack.controller;

import es.ual.dra.agrotrack.dto.request.PerfilUpdateRequest;
import es.ual.dra.agrotrack.dto.response.PerfilResponse;
import es.ual.dra.agrotrack.security.AppUserPrincipal;
import es.ual.dra.agrotrack.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Perfil del usuario autenticado: consultar y editar sus datos (nombre,
 * apellidos y foto). Requiere autenticación.
 */
@RestController
@RequestMapping("/api/perfil")
@RequiredArgsConstructor
public class PerfilController {

    private final AuthService authService;

    @GetMapping
    public PerfilResponse miPerfil(@AuthenticationPrincipal AppUserPrincipal user) {
        return authService.obtenerPerfil(user.getId());
    }

    @PutMapping
    public PerfilResponse editar(
            @AuthenticationPrincipal AppUserPrincipal user,
            @Valid @RequestBody PerfilUpdateRequest req) {
        return authService.editarPerfil(user.getId(), req);
    }
}

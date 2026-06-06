package es.ual.dra.agrotrack.controller;

import es.ual.dra.agrotrack.dto.request.AsistenteRequest;
import es.ual.dra.agrotrack.dto.response.AsistenteResponse;
import es.ual.dra.agrotrack.security.AppUserPrincipal;
import es.ual.dra.agrotrack.service.ai.AsistenteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoint del asistente IA. Requiere autenticación: la identidad del usuario
 * es lo que permite a las write-tools del mcp-server actuar en su nombre.
 */
@RestController
@RequestMapping("/api/asistente")
@RequiredArgsConstructor
public class AsistenteController {

    private final AsistenteService asistenteService;

    @PostMapping("/consulta")
    public AsistenteResponse consultar(
            @AuthenticationPrincipal AppUserPrincipal user,
            @Valid @RequestBody AsistenteRequest req) {
        String respuesta = asistenteService.consultar(req.mensaje(), user.getUsername());
        return new AsistenteResponse(respuesta);
    }
}

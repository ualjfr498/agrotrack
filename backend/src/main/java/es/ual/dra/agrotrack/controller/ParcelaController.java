package es.ual.dra.agrotrack.controller;

import es.ual.dra.agrotrack.dto.request.ParcelaRequest;
import es.ual.dra.agrotrack.dto.response.ParcelaResponse;
import es.ual.dra.agrotrack.security.AppUserPrincipal;
import es.ual.dra.agrotrack.service.ParcelaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Parcelas del usuario autenticado. Requiere autenticación (cualquier rol).
 *
 * El @AuthenticationPrincipal es indiferente al origen: lo rellena el
 * JwtAuthenticationFilter (usuario directo desde Angular) o el
 * ServiceTokenFilter (mcp-server actuando en nombre del usuario).
 */
@RestController
@RequestMapping("/api/parcelas")
@RequiredArgsConstructor
public class ParcelaController {

    private final ParcelaService parcelaService;

    @GetMapping
    public List<ParcelaResponse> misParcelas(@AuthenticationPrincipal AppUserPrincipal user) {
        return parcelaService.listarMias(user.getId());
    }

    @PostMapping
    public ResponseEntity<ParcelaResponse> crear(
            @AuthenticationPrincipal AppUserPrincipal user,
            @Valid @RequestBody ParcelaRequest req) {
        ParcelaResponse creada = parcelaService.crear(user.getId(), req);
        return ResponseEntity.status(HttpStatus.CREATED).body(creada);
    }

    @PutMapping("/{id}")
    public ParcelaResponse editar(
            @AuthenticationPrincipal AppUserPrincipal user,
            @PathVariable Long id,
            @Valid @RequestBody ParcelaRequest req) {
        return parcelaService.editar(user.getId(), id, req);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(
            @AuthenticationPrincipal AppUserPrincipal user,
            @PathVariable Long id) {
        parcelaService.eliminar(user.getId(), id);
        return ResponseEntity.noContent().build();
    }
}

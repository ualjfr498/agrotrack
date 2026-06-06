package es.ual.dra.agrotrack.controller;

import es.ual.dra.agrotrack.dto.request.CultivoRequest;
import es.ual.dra.agrotrack.dto.response.CultivoResponse;
import es.ual.dra.agrotrack.security.AppUserPrincipal;
import es.ual.dra.agrotrack.service.CultivoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * Cultivos del usuario autenticado. Requiere autenticación (cualquier rol).
 */
@RestController
@RequestMapping("/api/cultivos")
@RequiredArgsConstructor
public class CultivoController {

    private final CultivoService cultivoService;

    @GetMapping
    public List<CultivoResponse> misCultivos(
            @AuthenticationPrincipal AppUserPrincipal user,
            @RequestParam Optional<Long> parcelaId) {
        return parcelaId
            .map(pid -> cultivoService.listarDeParcela(user.getId(), pid))
            .orElseGet(() -> cultivoService.listarMios(user.getId()));
    }

    @PostMapping
    public ResponseEntity<CultivoResponse> crear(
            @AuthenticationPrincipal AppUserPrincipal user,
            @Valid @RequestBody CultivoRequest req) {
        CultivoResponse creado = cultivoService.crear(user.getId(), req);
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }
}

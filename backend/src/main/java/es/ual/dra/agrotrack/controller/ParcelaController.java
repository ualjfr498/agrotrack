package es.ual.dra.agrotrack.controller;

import es.ual.dra.agrotrack.dto.request.CultivoRequest;
import es.ual.dra.agrotrack.dto.request.ParcelaRequest;
import es.ual.dra.agrotrack.dto.response.CultivoResponse;
import es.ual.dra.agrotrack.dto.response.ParcelaResponse;
import es.ual.dra.agrotrack.model.entity.AppUser;
import es.ual.dra.agrotrack.model.entity.CultivoParcela;
import es.ual.dra.agrotrack.model.entity.Parcela;
import es.ual.dra.agrotrack.model.entity.Producto;
import es.ual.dra.agrotrack.repository.AppUserRepository;
import es.ual.dra.agrotrack.service.ParcelaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ParcelaController {

    private final ParcelaService parcelaService;
    private final AppUserRepository userRepository;

    // ── Parcelas ─────────────────────────────────────────────────────────────

    @GetMapping("/parcelas")
    public ResponseEntity<List<ParcelaResponse>> getMisParcelas(@AuthenticationPrincipal UserDetails ud) {
        AppUser usuario = resolveUser(ud);
        return ResponseEntity.ok(
            parcelaService.listarPorUsuario(usuario.getId())
                .stream().map(this::toParcelaResponse).collect(Collectors.toList())
        );
    }

    @PostMapping("/parcelas")
    public ResponseEntity<ParcelaResponse> crearParcela(
            @Valid @RequestBody ParcelaRequest req,
            @AuthenticationPrincipal UserDetails ud) {
        AppUser usuario = resolveUser(ud);
        Parcela parcela = Parcela.builder()
            .nombre(req.getNombre())
            .superficie(req.getSuperficie())
            .descripcion(req.getDescripcion())
            .usuario(usuario)
            .build();
        return ResponseEntity.ok(toParcelaResponse(parcelaService.crear(parcela)));
    }

    @PutMapping("/parcelas/{id}")
    public ResponseEntity<ParcelaResponse> actualizarParcela(
            @PathVariable Long id,
            @Valid @RequestBody ParcelaRequest req,
            @AuthenticationPrincipal UserDetails ud) {
        AppUser usuario = resolveUser(ud);
        Parcela datos = Parcela.builder()
            .nombre(req.getNombre())
            .superficie(req.getSuperficie())
            .descripcion(req.getDescripcion())
            .build();
        return ResponseEntity.ok(toParcelaResponse(parcelaService.actualizar(id, usuario.getId(), datos)));
    }

    @DeleteMapping("/parcelas/{id}")
    public ResponseEntity<Void> eliminarParcela(@PathVariable Long id, @AuthenticationPrincipal UserDetails ud) {
        parcelaService.eliminar(id, resolveUser(ud).getId());
        return ResponseEntity.noContent().build();
    }

    // ── Cultivos ─────────────────────────────────────────────────────────────

    @PostMapping("/parcelas/{id}/cultivos")
    public ResponseEntity<CultivoResponse> agregarCultivo(
            @PathVariable Long id,
            @RequestBody CultivoRequest req,
            @AuthenticationPrincipal UserDetails ud) {
        AppUser usuario = resolveUser(ud);
        CultivoParcela cultivo = CultivoParcela.builder()
            .producto(Producto.builder().id(req.getProductoId()).build())
            .fechaSiembra(req.getFechaSiembra())
            .notas(req.getNotas())
            .build();
        return ResponseEntity.ok(toCultivoResponse(parcelaService.agregarCultivo(id, usuario.getId(), cultivo)));
    }

    @PutMapping("/cultivos/{id}")
    public ResponseEntity<CultivoResponse> actualizarCultivo(
            @PathVariable Long id,
            @RequestBody CultivoRequest req,
            @AuthenticationPrincipal UserDetails ud) {
        AppUser usuario = resolveUser(ud);
        CultivoParcela datos = CultivoParcela.builder()
            .estado(req.getEstado())
            .notas(req.getNotas())
            .fechaCosecha(req.getFechaCosecha())
            .build();
        return ResponseEntity.ok(toCultivoResponse(parcelaService.actualizarCultivo(id, usuario.getId(), datos)));
    }

    @DeleteMapping("/cultivos/{id}")
    public ResponseEntity<Void> eliminarCultivo(@PathVariable Long id, @AuthenticationPrincipal UserDetails ud) {
        parcelaService.eliminarCultivo(id, resolveUser(ud).getId());
        return ResponseEntity.noContent().build();
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private AppUser resolveUser(UserDetails ud) {
        return userRepository.findByEmail(ud.getUsername())
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
    }

    private ParcelaResponse toParcelaResponse(Parcela p) {
        return ParcelaResponse.builder()
            .id(p.getId())
            .nombre(p.getNombre())
            .superficie(p.getSuperficie())
            .descripcion(p.getDescripcion())
            .createdAt(p.getCreatedAt())
            .cultivos(p.getCultivos().stream().map(this::toCultivoResponse).collect(Collectors.toList()))
            .build();
    }

    private CultivoResponse toCultivoResponse(CultivoParcela c) {
        return CultivoResponse.builder()
            .id(c.getId())
            .parcelaId(c.getParcela().getId())
            .parcelaNombre(c.getParcela().getNombre())
            .productoId(c.getProducto().getId())
            .productoNombre(c.getProducto().getNombre())
            .categoria(c.getProducto().getCategoria().getNombre().name())
            .fechaSiembra(c.getFechaSiembra())
            .fechaCosecha(c.getFechaCosecha())
            .estado(c.getEstado())
            .notas(c.getNotas())
            .updatedAt(c.getUpdatedAt())
            .build();
    }
}

package es.ual.dra.agrotrack.controller;

import es.ual.dra.agrotrack.dto.response.PrecioResponse;
import es.ual.dra.agrotrack.model.entity.PrecioMayorista;
import es.ual.dra.agrotrack.service.PrecioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/precios")
@RequiredArgsConstructor
public class PrecioController {

    private final PrecioService precioService;

    /** Últimos precios de todos los productos (para la home) */
    @GetMapping
    public ResponseEntity<List<PrecioResponse>> getUltimos() {
        return ResponseEntity.ok(
            precioService.findUltimos().stream().map(this::toResponse).collect(Collectors.toList())
        );
    }

    /** Historial de un producto para la gráfica de tendencias */
    @GetMapping("/{productoId}")
    public ResponseEntity<List<PrecioResponse>> getHistorial(
            @PathVariable Long productoId,
            @RequestParam(defaultValue = "90") int dias) {
        return ResponseEntity.ok(
            precioService.findHistorial(productoId, dias).stream()
                .map(this::toResponse).collect(Collectors.toList())
        );
    }

    // ── Mapper ───────────────────────────────────────────────────────────────

    private PrecioResponse toResponse(PrecioMayorista p) {
        return PrecioResponse.builder()
            .id(p.getId())
            .precio(p.getPrecio())
            .fecha(p.getFecha())
            .mercado(p.getMercado().getNombre())
            .ciudad(p.getMercado().getCiudad())
            .productoId(p.getProducto().getId())
            .productoNombre(p.getProducto().getNombre())
            .categoria(p.getProducto().getCategoria().getNombre().name())
            .build();
    }
}

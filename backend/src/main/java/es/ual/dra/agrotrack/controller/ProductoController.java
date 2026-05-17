package es.ual.dra.agrotrack.controller;

import es.ual.dra.agrotrack.dto.response.ProductoResponse;
import es.ual.dra.agrotrack.model.entity.Categoria;
import es.ual.dra.agrotrack.model.entity.Producto;
import es.ual.dra.agrotrack.model.enums.CategoriaEnum;
import es.ual.dra.agrotrack.service.ProductoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ProductoController {

    private final ProductoService productoService;

    // ── Catálogo (público) ───────────────────────────────────────────────────

    @GetMapping("/categorias")
    public ResponseEntity<List<Categoria>> getCategorias() {
        return ResponseEntity.ok(productoService.findAllCategorias());
    }

    @GetMapping("/productos")
    public ResponseEntity<List<ProductoResponse>> getProductos(
            @RequestParam(required = false) CategoriaEnum categoria) {
        List<Producto> productos = categoria != null
            ? productoService.findByCategoria(categoria)
            : productoService.findAll();
        return ResponseEntity.ok(productos.stream().map(this::toResponse).collect(Collectors.toList()));
    }

    @GetMapping("/productos/{id}")
    public ResponseEntity<ProductoResponse> getProducto(@PathVariable Long id) {
        return ResponseEntity.ok(toResponse(productoService.findById(id)));
    }

    // ── CRUD catálogo (solo ADMIN) ───────────────────────────────────────────

    @PostMapping("/productos")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductoResponse> crear(@RequestBody Producto producto) {
        return ResponseEntity.ok(toResponse(productoService.crear(producto)));
    }

    @PutMapping("/productos/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductoResponse> actualizar(@PathVariable Long id, @RequestBody Producto datos) {
        return ResponseEntity.ok(toResponse(productoService.actualizar(id, datos)));
    }

    @DeleteMapping("/productos/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        productoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    // ── Mapper ───────────────────────────────────────────────────────────────

    private ProductoResponse toResponse(Producto p) {
        return ProductoResponse.builder()
            .id(p.getId())
            .nombre(p.getNombre())
            .descripcion(p.getDescripcion())
            .unidad(p.getUnidad())
            .categoria(p.getCategoria().getNombre().name())
            .temporadaInicio(p.getTemporadaInicio())
            .temporadaFin(p.getTemporadaFin())
            .build();
    }
}

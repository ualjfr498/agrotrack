package es.ual.dra.agrotrack.controller;

import es.ual.dra.agrotrack.dto.response.PrecioResponse;
import es.ual.dra.agrotrack.service.PrecioService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/precios")
@RequiredArgsConstructor
public class PrecioController {

    private final PrecioService precioService;

    /**
     * Historial de precios del producto (últimos 90 días).
     * Sirve a las gráficas del frontend.
     */
    @GetMapping("/{productoId}")
    public List<PrecioResponse> historial(@PathVariable Long productoId) {
        return precioService.historialDeProducto(productoId);
    }
}

package es.ual.dra.agrotrack.service;

import es.ual.dra.agrotrack.dto.request.ProductoRequest;
import es.ual.dra.agrotrack.dto.response.ProductoResponse;
import es.ual.dra.agrotrack.model.entity.Categoria;
import es.ual.dra.agrotrack.model.entity.Producto;
import es.ual.dra.agrotrack.repository.CategoriaRepository;
import es.ual.dra.agrotrack.repository.ProductoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProductoService {

    private final ProductoRepository productoRepo;
    private final CategoriaRepository categoriaRepo;

    public List<ProductoResponse> listar(Optional<Long> categoriaId) {
        List<Producto> productos = categoriaId
            .map(productoRepo::findByCategoriaId)
            .orElseGet(productoRepo::findAll);
        return productos.stream().map(ProductoResponse::from).toList();
    }

    public ProductoResponse obtener(Long id) {
        return productoRepo.findById(id)
            .map(ProductoResponse::from)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no encontrado"));
    }

    @Transactional
    public ProductoResponse crear(ProductoRequest req) {
        Categoria cat = categoriaRepo.findById(req.categoriaId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Categoría no encontrada"));

        Producto p = new Producto();
        aplicar(p, req, cat);
        return ProductoResponse.from(productoRepo.save(p));
    }

    @Transactional
    public ProductoResponse actualizar(Long id, ProductoRequest req) {
        Producto p = productoRepo.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no encontrado"));
        Categoria cat = categoriaRepo.findById(req.categoriaId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Categoría no encontrada"));

        aplicar(p, req, cat);
        return ProductoResponse.from(productoRepo.save(p));
    }

    @Transactional
    public void borrar(Long id) {
        if (!productoRepo.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no encontrado");
        }
        productoRepo.deleteById(id);
    }

    private void aplicar(Producto p, ProductoRequest req, Categoria cat) {
        p.setNombre(req.nombre());
        p.setDescripcion(req.descripcion());
        p.setImagenUrl(req.imagenUrl());
        p.setTemporadaInicio(req.temporadaInicio());
        p.setTemporadaFin(req.temporadaFin());
        p.setCategoria(cat);
    }
}

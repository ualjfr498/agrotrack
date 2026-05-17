package es.ual.dra.agrotrack.service;

import es.ual.dra.agrotrack.model.entity.Categoria;
import es.ual.dra.agrotrack.model.entity.Producto;
import es.ual.dra.agrotrack.model.enums.CategoriaEnum;
import es.ual.dra.agrotrack.repository.CategoriaRepository;
import es.ual.dra.agrotrack.repository.ProductoRepository;
import es.ual.dra.agrotrack.service.scraping.ScrapingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Facade de la capa de catálogo.
 * Desacopla controllers de repositorios y lógica de normalización.
 */
@Service
@RequiredArgsConstructor
public class ProductoService {

    private final ProductoRepository productoRepository;
    private final CategoriaRepository categoriaRepository;

    public List<Producto> findAll() {
        return productoRepository.findAll();
    }

    public List<Producto> findByCategoria(CategoriaEnum categoria) {
        return productoRepository.findByCategoriaNombre(categoria);
    }

    public Producto findById(Long id) {
        return productoRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Producto no encontrado: " + id));
    }

    @Transactional
    public Producto crear(Producto producto) {
        producto.setNombreNormalizado(ScrapingService.normalizar(producto.getNombre()));
        return productoRepository.save(producto);
    }

    @Transactional
    public Producto actualizar(Long id, Producto datos) {
        Producto existente = findById(id);
        existente.setNombre(datos.getNombre());
        existente.setNombreNormalizado(ScrapingService.normalizar(datos.getNombre()));
        existente.setDescripcion(datos.getDescripcion());
        existente.setTemporadaInicio(datos.getTemporadaInicio());
        existente.setTemporadaFin(datos.getTemporadaFin());
        return productoRepository.save(existente);
    }

    @Transactional
    public void eliminar(Long id) {
        productoRepository.deleteById(id);
    }

    public List<Categoria> findAllCategorias() {
        return categoriaRepository.findAll();
    }
}

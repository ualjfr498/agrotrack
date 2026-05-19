package es.ual.dra.agrotrack.repository;

import es.ual.dra.agrotrack.model.entity.Producto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductoRepository extends JpaRepository<Producto, Long> {

    Optional<Producto> findByNombreIgnoreCase(String nombre);

    List<Producto> findByCategoriaId(Long categoriaId);
}

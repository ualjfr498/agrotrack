package es.ual.dra.agrotrack.repository;

import es.ual.dra.agrotrack.model.entity.AlertaPrecio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AlertaPrecioRepository extends JpaRepository<AlertaPrecio, Long> {

    List<AlertaPrecio> findByUsuarioId(Long usuarioId);

    /** Alertas activas para un producto (usadas por AlertaService después de cada scraping) */
    List<AlertaPrecio> findByProductoIdAndActivaTrue(Long productoId);

    Optional<AlertaPrecio> findByIdAndUsuarioId(Long id, Long usuarioId);
}

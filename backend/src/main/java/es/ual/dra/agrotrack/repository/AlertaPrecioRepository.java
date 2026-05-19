package es.ual.dra.agrotrack.repository;

import es.ual.dra.agrotrack.model.entity.AlertaPrecio;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AlertaPrecioRepository extends JpaRepository<AlertaPrecio, Long> {

    List<AlertaPrecio> findByUsuarioId(Long usuarioId);

    List<AlertaPrecio> findByActivaTrue();

    List<AlertaPrecio> findByActivaTrueAndProductoId(Long productoId);
}

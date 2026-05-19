package es.ual.dra.agrotrack.repository;

import es.ual.dra.agrotrack.model.entity.PrecioMayorista;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface PrecioMayoristaRepository extends JpaRepository<PrecioMayorista, Long> {

    List<PrecioMayorista> findByProductoIdAndFechaGreaterThanEqualOrderByFechaAsc(
        Long productoId, LocalDate desde);

    List<PrecioMayorista> findByProductoIdOrderByFechaDesc(Long productoId);

    boolean existsByProductoIdAndMercadoIdAndFecha(
        Long productoId, Long mercadoId, LocalDate fecha);
}

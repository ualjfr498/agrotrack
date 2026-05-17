package es.ual.dra.agrotrack.repository;

import es.ual.dra.agrotrack.model.entity.PrecioMayorista;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface PrecioMayoristaRepository extends JpaRepository<PrecioMayorista, Long> {

    /** Historial de precios de un producto en los últimos N días */
    @Query("""
        SELECT p FROM PrecioMayorista p
        WHERE p.producto.id = :productoId
          AND p.fecha >= :desde
        ORDER BY p.fecha DESC, p.mercado.nombre ASC
    """)
    List<PrecioMayorista> findByProductoIdAndFechaAfter(
        @Param("productoId") Long productoId,
        @Param("desde") LocalDate desde
    );

    /** Último precio registrado de cada producto (para la home) */
    @Query("""
        SELECT p FROM PrecioMayorista p
        WHERE p.fecha = (
            SELECT MAX(p2.fecha) FROM PrecioMayorista p2
            WHERE p2.producto.id = p.producto.id
        )
        ORDER BY p.producto.nombre ASC
    """)
    List<PrecioMayorista> findUltimosPrecios();

    /** Últimos precios de un producto por mercado */
    @Query("""
        SELECT p FROM PrecioMayorista p
        WHERE p.producto.id = :productoId
          AND p.fecha = (
              SELECT MAX(p2.fecha) FROM PrecioMayorista p2
              WHERE p2.producto.id = :productoId
          )
        ORDER BY p.mercado.nombre ASC
    """)
    List<PrecioMayorista> findPreciosActualesByProductoId(@Param("productoId") Long productoId);

    /** Comprueba si ya existe un precio para producto+mercado+fecha (evita duplicados) */
    boolean existsByProductoIdAndMercadoIdAndFecha(Long productoId, Long mercadoId, LocalDate fecha);

    /** Alertas: precios guardados hoy para un producto */
    List<PrecioMayorista> findByProductoIdAndFecha(Long productoId, LocalDate fecha);
}

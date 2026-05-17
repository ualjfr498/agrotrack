package es.ual.dra.agrotrack.repository;

import es.ual.dra.agrotrack.model.entity.Producto;
import es.ual.dra.agrotrack.model.enums.CategoriaEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long> {

    List<Producto> findByCategoriaNombre(CategoriaEnum categoriaNombre);

    Optional<Producto> findByNombreNormalizado(String nombreNormalizado);

    Optional<Producto> findByNombre(String nombre);

    /**
     * Productos cuya temporada engloba el mes indicado.
     * Null en temporadaInicio/Fin = disponible todo el año.
     */
    @Query("""
        SELECT p FROM Producto p
        WHERE p.temporadaInicio IS NULL
           OR (p.temporadaInicio <= :mes AND p.temporadaFin >= :mes)
           OR (p.temporadaInicio > p.temporadaFin
               AND (p.temporadaInicio <= :mes OR p.temporadaFin >= :mes))
    """)
    List<Producto> findByTemporada(@Param("mes") int mes);
}

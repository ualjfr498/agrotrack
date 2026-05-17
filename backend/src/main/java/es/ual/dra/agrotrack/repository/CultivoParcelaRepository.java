package es.ual.dra.agrotrack.repository;

import es.ual.dra.agrotrack.model.entity.CultivoParcela;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CultivoParcelaRepository extends JpaRepository<CultivoParcela, Long> {

    List<CultivoParcela> findByParcelaId(Long parcelaId);

    Optional<CultivoParcela> findByIdAndParcelaUsuarioId(Long id, Long usuarioId);

    /** Cultivos activos del agricultor con su producto y parcela */
    @Query("""
        SELECT c FROM CultivoParcela c
        JOIN FETCH c.producto
        JOIN FETCH c.parcela
        WHERE c.parcela.usuario.id = :usuarioId
          AND c.estado NOT IN (
            es.ual.dra.agrotrack.model.enums.EstadoCultivo.COSECHADO,
            es.ual.dra.agrotrack.model.enums.EstadoCultivo.PERDIDO
          )
        ORDER BY c.fechaSiembra DESC
    """)
    List<CultivoParcela> findActivosByUsuarioId(@Param("usuarioId") Long usuarioId);
}

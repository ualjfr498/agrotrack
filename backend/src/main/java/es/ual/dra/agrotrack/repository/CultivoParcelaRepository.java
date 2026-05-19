package es.ual.dra.agrotrack.repository;

import es.ual.dra.agrotrack.model.entity.CultivoParcela;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CultivoParcelaRepository extends JpaRepository<CultivoParcela, Long> {

    List<CultivoParcela> findByParcelaId(Long parcelaId);

    List<CultivoParcela> findByParcelaUsuarioId(Long usuarioId);
}

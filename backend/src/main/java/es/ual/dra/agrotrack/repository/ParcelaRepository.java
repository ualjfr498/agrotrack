package es.ual.dra.agrotrack.repository;

import es.ual.dra.agrotrack.model.entity.Parcela;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ParcelaRepository extends JpaRepository<Parcela, Long> {

    List<Parcela> findByUsuarioId(Long usuarioId);
}

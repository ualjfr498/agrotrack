package es.ual.dra.agrotrack.repository;

import es.ual.dra.agrotrack.model.entity.Parcela;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ParcelaRepository extends JpaRepository<Parcela, Long> {

    List<Parcela> findByUsuarioId(Long usuarioId);

    Optional<Parcela> findByIdAndUsuarioId(Long id, Long usuarioId);
}

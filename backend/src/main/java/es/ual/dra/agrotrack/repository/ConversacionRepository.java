package es.ual.dra.agrotrack.repository;

import es.ual.dra.agrotrack.model.entity.Conversacion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ConversacionRepository extends JpaRepository<Conversacion, Long> {

    /** Conversaciones del usuario, de la más reciente a la más antigua. */
    List<Conversacion> findByUsuarioIdOrderByFechaActualizacionDesc(Long usuarioId);
}

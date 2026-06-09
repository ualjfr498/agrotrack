package es.ual.dra.agrotrack.repository;

import es.ual.dra.agrotrack.model.entity.MensajeChat;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MensajeChatRepository extends JpaRepository<MensajeChat, Long> {

    /** Mensajes de una conversación en orden cronológico. */
    List<MensajeChat> findByConversacionIdOrderByFechaAsc(Long conversacionId);

    void deleteByConversacionId(Long conversacionId);
}

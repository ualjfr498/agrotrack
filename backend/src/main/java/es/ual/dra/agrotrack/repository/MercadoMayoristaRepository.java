package es.ual.dra.agrotrack.repository;

import es.ual.dra.agrotrack.model.entity.MercadoMayorista;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MercadoMayoristaRepository extends JpaRepository<MercadoMayorista, Long> {

    Optional<MercadoMayorista> findByNombre(String nombre);
}

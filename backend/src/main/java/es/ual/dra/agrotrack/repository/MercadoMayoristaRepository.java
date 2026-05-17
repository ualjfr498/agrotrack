package es.ual.dra.agrotrack.repository;

import es.ual.dra.agrotrack.model.entity.MercadoMayorista;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MercadoMayoristaRepository extends JpaRepository<MercadoMayorista, Long> {

    Optional<MercadoMayorista> findByClaveScraping(String claveScraping);

    Optional<MercadoMayorista> findByNombre(String nombre);
}

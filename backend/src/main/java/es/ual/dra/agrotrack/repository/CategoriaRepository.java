package es.ual.dra.agrotrack.repository;

import es.ual.dra.agrotrack.model.entity.Categoria;
import es.ual.dra.agrotrack.model.enums.CategoriaEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CategoriaRepository extends JpaRepository<Categoria, Long> {

    Optional<Categoria> findByNombre(CategoriaEnum nombre);
}

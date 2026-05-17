package es.ual.dra.agrotrack.repository;

import es.ual.dra.agrotrack.model.entity.ScrapingLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ScrapingLogRepository extends JpaRepository<ScrapingLog, Long> {

    Page<ScrapingLog> findAllByOrderByInicioAtDesc(Pageable pageable);
}

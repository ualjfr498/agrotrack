package es.ual.dra.agrotrack.repository;

import es.ual.dra.agrotrack.model.entity.ScrapingLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ScrapingLogRepository extends JpaRepository<ScrapingLog, Long> {

    List<ScrapingLog> findTop20ByOrderByFechaEjecucionDesc();
}

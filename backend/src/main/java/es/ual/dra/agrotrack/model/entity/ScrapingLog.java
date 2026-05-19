package es.ual.dra.agrotrack.model.entity;

import es.ual.dra.agrotrack.model.enums.EstadoScraping;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "scraping_log")
@Getter
@Setter
@NoArgsConstructor
public class ScrapingLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "fecha_ejecucion", nullable = false)
    private LocalDateTime fechaEjecucion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoScraping estado;

    @Column(name = "filas_insertadas")
    private Integer filasInsertadas;

    @Column(name = "duracion_ms")
    private Integer duracionMs;

    @Column(columnDefinition = "TEXT")
    private String mensaje;
}

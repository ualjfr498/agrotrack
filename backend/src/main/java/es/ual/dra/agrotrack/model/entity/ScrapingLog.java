package es.ual.dra.agrotrack.model.entity;

import es.ual.dra.agrotrack.model.enums.ScrapingEstado;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "scraping_log")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class ScrapingLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "inicio_at", nullable = false)
    private LocalDateTime inicioAt;

    @Column(name = "fin_at")
    private LocalDateTime finAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ScrapingEstado estado;

    /** Número de precios guardados en esta ejecución */
    @Column(name = "precios_guardados")
    @Builder.Default
    private int preciosGuardados = 0;

    /** Mensaje de error si el estado es FALLIDO o PARCIAL */
    @Column(name = "mensaje_error", length = 2000)
    private String mensajeError;

    /** Indica si fue disparado manualmente por un ADMIN */
    @Column(name = "disparo_manual")
    @Builder.Default
    private boolean disparoManual = false;
}

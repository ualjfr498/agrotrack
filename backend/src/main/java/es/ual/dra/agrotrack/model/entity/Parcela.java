package es.ual.dra.agrotrack.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "parcela")
@Getter
@Setter
@NoArgsConstructor
public class Parcela {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private AppUser usuario;

    @Column(nullable = false, length = 80)
    private String nombre;

    @Column(name = "superficie_m2", precision = 10, scale = 2)
    private BigDecimal superficieM2;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    // Imagen opcional de la parcela, guardada como data URL en base64
    // (p. ej. "data:image/jpeg;base64,..."). LONGTEXT para no truncar.
    @Column(columnDefinition = "LONGTEXT")
    private String imagen;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @PrePersist
    void onCreate() {
        if (fechaCreacion == null) {
            fechaCreacion = LocalDateTime.now();
        }
    }
}

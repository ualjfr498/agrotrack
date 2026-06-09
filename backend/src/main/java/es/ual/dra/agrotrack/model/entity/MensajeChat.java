package es.ual.dra.agrotrack.model.entity;

import es.ual.dra.agrotrack.model.enums.RolMensaje;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Un mensaje dentro de una {@link Conversacion}: lo escribe el usuario (USER) o
 * lo genera el asistente (ASSISTANT).
 */
@Entity
@Table(name = "mensaje_chat")
@Getter
@Setter
@NoArgsConstructor
public class MensajeChat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "conversacion_id", nullable = false)
    private Conversacion conversacion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RolMensaje rol;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String contenido;

    @Column(nullable = false, updatable = false)
    private LocalDateTime fecha;

    @PrePersist
    void onCreate() {
        if (fecha == null) fecha = LocalDateTime.now();
    }
}

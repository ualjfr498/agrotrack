package es.ual.dra.agrotrack.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "alertas_precio")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class AlertaPrecio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** El usuario recibirá email cuando el precio supere este umbral (€/kg) */
    @Column(name = "precio_umbral", nullable = false, precision = 10, scale = 2)
    private BigDecimal precioUmbral;

    @Column(nullable = false)
    @Builder.Default
    private boolean activa = true;

    @Column(name = "ultima_notificacion")
    private LocalDateTime ultimaNotificacion;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    // ── Relaciones ──────────────────────────────────────────────────────────
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private AppUser usuario;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "producto_id", nullable = false)
    private Producto producto;
}

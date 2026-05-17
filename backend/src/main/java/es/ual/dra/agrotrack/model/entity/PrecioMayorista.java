package es.ual.dra.agrotrack.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(
    name = "precios_mayoristas",
    indexes = {
        @Index(name = "idx_precio_producto_fecha", columnList = "producto_id, fecha DESC"),
        @Index(name = "idx_precio_mercado_fecha",  columnList = "mercado_id, fecha DESC")
    }
)
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class PrecioMayorista {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Precio en €/kg */
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal precio;

    @Column(nullable = false)
    private LocalDate fecha;

    // ── Relaciones ──────────────────────────────────────────────────────────
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "producto_id", nullable = false)
    private Producto producto;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "mercado_id", nullable = false)
    private MercadoMayorista mercado;
}

package es.ual.dra.agrotrack.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(
    name = "precio_mayorista",
    uniqueConstraints = @UniqueConstraint(
        name = "uq_precio_producto_mercado_fecha",
        columnNames = {"producto_id", "mercado_id", "fecha"}
    ),
    indexes = @Index(
        name = "idx_precio_producto_fecha",
        columnList = "producto_id, fecha"
    )
)
@Getter
@Setter
@NoArgsConstructor
public class PrecioMayorista {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "producto_id", nullable = false)
    private Producto producto;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "mercado_id", nullable = false)
    private MercadoMayorista mercado;

    @Column(nullable = false)
    private LocalDate fecha;

    @Column(name = "precio_kg", nullable = false, precision = 8, scale = 3)
    private BigDecimal precioKg;
}

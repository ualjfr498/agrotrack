package es.ual.dra.agrotrack.model.entity;

import es.ual.dra.agrotrack.model.enums.EstadoCultivo;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "cultivo_parcela")
@Getter
@Setter
@NoArgsConstructor
public class CultivoParcela {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "parcela_id", nullable = false)
    private Parcela parcela;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "producto_id", nullable = false)
    private Producto producto;

    @Column(name = "fecha_siembra", nullable = false)
    private LocalDate fechaSiembra;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoCultivo estado = EstadoCultivo.SEMBRADO;

    @Column(columnDefinition = "TEXT")
    private String notas;
}

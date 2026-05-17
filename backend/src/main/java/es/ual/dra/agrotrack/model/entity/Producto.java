package es.ual.dra.agrotrack.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "productos")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Nombre exacto tal y como aparece en Mercasa para mapear el scraping */
    @Column(nullable = false, unique = true, length = 150)
    private String nombre;

    /** Nombre normalizado para búsquedas (lowercase, sin tildes) */
    @Column(name = "nombre_normalizado", length = 150)
    private String nombreNormalizado;

    @Column(length = 1000)
    private String descripcion;

    /** Unidad de medida publicada por Mercasa (normalmente kg) */
    @Column(length = 20)
    @Builder.Default
    private String unidad = "kg";

    /** Mes de inicio de temporada (1-12, null = disponible todo el año) */
    @Column(name = "temporada_inicio")
    private Integer temporadaInicio;

    /** Mes de fin de temporada (1-12, null = disponible todo el año) */
    @Column(name = "temporada_fin")
    private Integer temporadaFin;

    // ── Relaciones ──────────────────────────────────────────────────────────
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "categoria_id", nullable = false)
    private Categoria categoria;

    @OneToMany(mappedBy = "producto", cascade = CascadeType.ALL)
    @Builder.Default
    private List<PrecioMayorista> precios = new ArrayList<>();

    @OneToMany(mappedBy = "producto")
    @Builder.Default
    private List<CultivoParcela> cultivosParcela = new ArrayList<>();

    @OneToMany(mappedBy = "producto")
    @Builder.Default
    private List<AlertaPrecio> alertas = new ArrayList<>();
}

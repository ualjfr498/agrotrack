package es.ual.dra.agrotrack.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "mercados_mayoristas")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class MercadoMayorista {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String nombre;

    @Column(length = 100)
    private String ciudad;

    /** Clave usada para el mapeo en el scraping de Mercasa */
    @Column(name = "clave_scraping", unique = true, length = 50)
    private String claveScraping;

    // ── Relaciones ──────────────────────────────────────────────────────────
    @OneToMany(mappedBy = "mercado", cascade = CascadeType.ALL)
    @Builder.Default
    private List<PrecioMayorista> precios = new ArrayList<>();
}

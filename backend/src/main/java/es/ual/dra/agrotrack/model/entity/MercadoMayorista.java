package es.ual.dra.agrotrack.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "mercado_mayorista")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MercadoMayorista {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 60, unique = true)
    private String nombre;

    @Column(length = 60)
    private String ciudad;
}

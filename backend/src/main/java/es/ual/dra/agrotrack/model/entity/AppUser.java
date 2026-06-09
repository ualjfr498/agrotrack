package es.ual.dra.agrotrack.model.entity;

import es.ual.dra.agrotrack.model.enums.Rol;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "app_user")
@Getter
@Setter
@NoArgsConstructor
public class AppUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120, unique = true)
    private String email;

    // Nullable a nivel de columna para no romper filas previas al añadir el campo
    // (ddl-auto update). En el registro se exigen vía RegisterRequest.
    @Column(length = 80)
    private String nombre;

    @Column(length = 120)
    private String apellidos;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    // Foto de perfil opcional, como data URL base64. LONGTEXT para no truncar.
    @Column(columnDefinition = "LONGTEXT")
    private String foto;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Rol rol;

    @Column(name = "fecha_alta", nullable = false, updatable = false)
    private LocalDateTime fechaAlta;

    @PrePersist
    void onCreate() {
        if (fechaAlta == null) {
            fechaAlta = LocalDateTime.now();
        }
    }
}

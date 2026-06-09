package es.ual.dra.agrotrack.dto.response;

import es.ual.dra.agrotrack.model.entity.AppUser;
import es.ual.dra.agrotrack.model.enums.Rol;

/**
 * Datos del perfil del usuario autenticado (sin la contraseña).
 */
public record PerfilResponse(
    Long id,
    String email,
    String nombre,
    String apellidos,
    Rol rol,
    String foto
) {
    public static PerfilResponse from(AppUser u) {
        return new PerfilResponse(
            u.getId(),
            u.getEmail(),
            u.getNombre(),
            u.getApellidos(),
            u.getRol(),
            u.getFoto()
        );
    }
}

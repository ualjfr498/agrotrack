package es.ual.dra.agrotrack.dto.response;

import es.ual.dra.agrotrack.model.enums.Rol;

public record JwtResponse(
    String token,
    String email,
    Rol rol,
    long expiresAt
) {}

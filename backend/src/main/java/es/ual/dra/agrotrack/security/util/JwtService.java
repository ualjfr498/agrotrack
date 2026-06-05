package es.ual.dra.agrotrack.security.util;

import es.ual.dra.agrotrack.model.entity.AppUser;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * Genera y verifica JWTs HS256.
 *
 * El SECRET vive en variable de entorno (JWT_SECRET) y nunca sale del
 * backend. La firma usa HMAC-SHA-256, así que el secret debe tener
 * al menos 256 bits = 32 bytes.
 */
@Service
public class JwtService {

    private final SecretKey clave;
    private final long expiracionMs;

    public JwtService(
        @Value("${agrotrack.jwt.secret}") String secret,
        @Value("${agrotrack.jwt.expiration-ms:86400000}") long expiracionMs
    ) {
        byte[] bytes = secret.getBytes(StandardCharsets.UTF_8);
        if (bytes.length < 32) {
            throw new IllegalStateException(
                "JWT_SECRET debe tener al menos 32 bytes (256 bits). Actual: " + bytes.length);
        }
        this.clave = Keys.hmacShaKeyFor(bytes);
        this.expiracionMs = expiracionMs;
    }

    public String generar(AppUser user) {
        Date ahora = new Date();
        Date exp = new Date(ahora.getTime() + expiracionMs);

        return Jwts.builder()
            .subject(user.getEmail())
            .claim("rol", user.getRol().name())
            .claim("uid", user.getId())
            .issuedAt(ahora)
            .expiration(exp)
            .signWith(clave)
            .compact();
    }

    /**
     * Parsea y verifica el token. Lanza JwtException (subtipos) si
     * la firma es inválida o el token ha caducado.
     */
    public Claims parsear(String token) {
        Jws<Claims> jws = Jwts.parser()
            .verifyWith(clave)
            .build()
            .parseSignedClaims(token);
        return jws.getPayload();
    }

    public long getExpiracionMs() {
        return expiracionMs;
    }
}

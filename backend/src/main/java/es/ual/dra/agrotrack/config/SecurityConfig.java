package es.ual.dra.agrotrack.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Configuración TEMPORAL de seguridad (Fase 3).
 *
 * Estado actual: permite todas las peticiones SIN autenticación.
 * Existe únicamente para anular el comportamiento por defecto de
 * Spring Security (HTTP Basic en todos los endpoints), que bloquearía
 * el POST /api/admin/precios/actualizar y cualquier otro endpoint.
 *
 * Se sustituirá en la Fase 4 por una configuración real con:
 *   - JWT filter (JwtAuthenticationFilter)
 *   - Whitelist solo para /api/auth/** y endpoints públicos
 *   - @PreAuthorize por rol en los controllers
 */
@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
        return http.build();
    }
}

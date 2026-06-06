package es.ual.dra.agrotrack.config;

import es.ual.dra.agrotrack.security.filter.JwtAuthenticationFilter;
import es.ual.dra.agrotrack.security.filter.ServiceTokenFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Configuración de seguridad real (Fase 4).
 *
 * Política:
 *   - Stateless (no sesiones HTTP). El JWT lleva todo el contexto.
 *   - Públicos: /api/auth/**, GET de /api/productos, /api/precios, /api/categorias.
 *   - Resto: requiere JWT válido en cabecera Authorization.
 *   - @PreAuthorize en métodos para roles concretos (ADMIN).
 *
 * El JwtAuthenticationFilter se inserta antes del filtro de
 * UsernamePasswordAuthenticationFilter para que cualquier petición
 * con Authorization quede autenticada antes de evaluar las reglas.
 */
@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtFilter;
    private final ServiceTokenFilter serviceTokenFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/productos/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/precios/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/categorias/**").permitAll()
                .anyRequest().authenticated()
            )
            // El service token se resuelve primero; si no hay, cae al flujo JWT.
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
            .addFilterBefore(serviceTokenFilter, JwtAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /*
     * Los filtros son @Component y OncePerRequestFilter, así que Spring Boot
     * los auto-registraría también como filtros del servlet container, fuera de
     * la cadena de Spring Security. Eso duplica su ejecución e interfiere con el
     * manejo de respuestas (p. ej. un sendError(401) acababa convertido en 403).
     * Estos FilterRegistrationBean deshabilitados anulan ese auto-registro: los
     * filtros viven SOLO donde los coloca addFilterBefore.
     */
    @Bean
    public FilterRegistrationBean<ServiceTokenFilter> serviceTokenFilterRegistration(ServiceTokenFilter filter) {
        FilterRegistrationBean<ServiceTokenFilter> reg = new FilterRegistrationBean<>(filter);
        reg.setEnabled(false);
        return reg;
    }

    @Bean
    public FilterRegistrationBean<JwtAuthenticationFilter> jwtFilterRegistration(JwtAuthenticationFilter filter) {
        FilterRegistrationBean<JwtAuthenticationFilter> reg = new FilterRegistrationBean<>(filter);
        reg.setEnabled(false);
        return reg;
    }
}

package es.ual.dra.agrotrack.security.filter;

import es.ual.dra.agrotrack.security.AppUserDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/**
 * Autentica las llamadas internas del mcp-server.
 *
 * Cuando el mcp-server ejecuta una write-tool o una read privada, llama al
 * backend con dos cabeceras:
 *   - X-Service-Token: secreto compartido que prueba que la llamada viene del
 *     mcp-server (no de un cliente arbitrario).
 *   - X-Acting-User: email del usuario en cuyo nombre se actúa, que el
 *     mcp-server obtuvo del _meta MCP (claim source=backend + actingUser que
 *     el asistente del backend inyectó en el toolContext).
 *
 * Si el token es válido, este filtro carga ese usuario y lo deja en el
 * SecurityContext, de modo que los controllers (@AuthenticationPrincipal) lo
 * ven exactamente igual que si hubiera llegado un JWT del propio usuario.
 *
 * Va ANTES del JwtAuthenticationFilter: si una petición trae service token, se
 * resuelve aquí; si no, cae al flujo JWT normal.
 */
@Component
@Slf4j
public class ServiceTokenFilter extends OncePerRequestFilter {

    private static final String TOKEN_HEADER = "X-Service-Token";
    private static final String ACTING_USER_HEADER = "X-Acting-User";

    private final byte[] expectedToken;
    private final AppUserDetailsService userDetailsService;

    public ServiceTokenFilter(
            @Value("${agrotrack.service-token}") String serviceToken,
            AppUserDetailsService userDetailsService) {
        this.expectedToken = serviceToken.getBytes(StandardCharsets.UTF_8);
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(
        @NonNull HttpServletRequest request,
        @NonNull HttpServletResponse response,
        @NonNull FilterChain chain
    ) throws ServletException, IOException {

        String presented = request.getHeader(TOKEN_HEADER);
        if (presented == null) {
            chain.doFilter(request, response); // no es llamada de servicio
            return;
        }

        boolean tokenOk = MessageDigest.isEqual(
            presented.getBytes(StandardCharsets.UTF_8), expectedToken);
        if (!tokenOk) {
            log.warn("X-Service-Token inválido en {}", request.getRequestURI());
            unauthorized(response, "Service token inválido");
            return;
        }

        String actingUser = request.getHeader(ACTING_USER_HEADER);
        if (actingUser == null || actingUser.isBlank()) {
            // Token de servicio válido pero sin identidad de usuario: para las
            // operaciones que requieren un usuario (parcelas, cultivos) esto no
            // sirve. Lo rechazamos explícitamente.
            unauthorized(response, "Falta X-Acting-User para una operación que requiere identidad");
            return;
        }

        try {
            UserDetails user = userDetailsService.loadUserByUsername(actingUser);
            var auth = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
            auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(auth);
        } catch (UsernameNotFoundException e) {
            log.warn("X-Acting-User desconocido: {}", actingUser);
            unauthorized(response, "Usuario actuante desconocido");
            return;
        }

        chain.doFilter(request, response);
    }

    /**
     * Escribe un 401 directamente con setStatus (no sendError): sendError dispara
     * un ERROR dispatch a /error que vuelve a pasar por Spring Security y, al no
     * estar autenticado, acabaría devolviendo 403 en lugar del 401 pretendido.
     */
    private void unauthorized(HttpServletResponse response, String mensaje) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"error\":\"" + mensaje + "\"}");
    }
}

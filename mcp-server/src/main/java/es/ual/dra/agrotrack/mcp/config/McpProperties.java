package es.ual.dra.agrotrack.mcp.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuración del mcp-server externa al código: la URL del backend al que
 * delega y el token de servicio que prueba que las llamadas REST vienen de aquí.
 */
@Data
@ConfigurationProperties(prefix = "agrotrack.mcp")
public class McpProperties {

    /** URL base del backend (sin trailing slash). */
    private String backendBaseUrl;

    /**
     * Secreto compartido con el backend.
     * En producción debe inyectarse desde un secret manager; en dev viene del
     * application.yml con un valor placeholder.
     */
    private String serviceToken;
}

package es.ual.dra.agrotrack.mcp.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

/**
 * Define el {@link RestClient} que usan todas las tools para hablar con el backend.
 * Adjunta por defecto el header X-Service-Token. El X-Acting-User se añade
 * por llamada cuando la operación lo requiere (writes y reads privadas).
 */
@Configuration
@EnableConfigurationProperties(McpProperties.class)
public class RestClientConfig {

    @Bean
    public RestClient backendRestClient(McpProperties props) {
        return RestClient.builder()
            .baseUrl(props.getBackendBaseUrl())
            .defaultHeader("X-Service-Token", props.getServiceToken())
            .build();
    }
}

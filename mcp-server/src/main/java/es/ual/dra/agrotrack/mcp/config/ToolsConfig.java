package es.ual.dra.agrotrack.mcp.config;

import es.ual.dra.agrotrack.mcp.tools.AgroTools;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Publica las tools de {@link AgroTools} como un {@link ToolCallbackProvider}
 * para que el MCP server starter de Spring AI las exponga por el protocolo MCP.
 */
@Configuration
public class ToolsConfig {

    @Bean
    public ToolCallbackProvider agroToolsProvider(AgroTools agroTools) {
        return MethodToolCallbackProvider.builder()
            .toolObjects(agroTools)
            .build();
    }
}

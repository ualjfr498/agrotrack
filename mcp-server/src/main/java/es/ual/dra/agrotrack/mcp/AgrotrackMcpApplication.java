package es.ual.dra.agrotrack.mcp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * MCP server de AgroTrack.
 *
 * Publica un catálogo de tools por MCP (JSON-RPC) que cualquier cliente compatible
 * puede consumir: el propio backend (Spring AI ChatClient), LM Studio chat UI,
 * Claude Desktop, Cursor, etc.
 *
 * Las tools NO tocan la BD ni comparten código Java con backend; delegan en su
 * API REST a través de un cliente HTTP con service token.
 *
 * Reglas de auth aplicadas en las tools (Fase 7):
 *  - Reads de datos públicos (precios, productos, mercados)
 *      → cualquier cliente MCP.
 *  - Reads de datos del usuario (mis cultivos, mis alertas)
 *  - Writes (registrar cultivo, crear alerta...)
 *      → SOLO si la metadata MCP incluye claim "source=backend"
 *        (es decir, llegan vía Angular → backend → mcp-server,
 *        no directamente desde LM Studio chat UI).
 */
@SpringBootApplication
public class AgrotrackMcpApplication {

    public static void main(String[] args) {
        SpringApplication.run(AgrotrackMcpApplication.class, args);
    }
}

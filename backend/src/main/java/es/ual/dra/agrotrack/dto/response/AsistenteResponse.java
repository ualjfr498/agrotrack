package es.ual.dra.agrotrack.dto.response;

/**
 * Respuesta del asistente. conversacionId es el id de la conversación donde se
 * guardó (usuarios registrados); null para invitados (sin persistencia).
 */
public record AsistenteResponse(
    String respuesta,
    Long conversacionId
) {}

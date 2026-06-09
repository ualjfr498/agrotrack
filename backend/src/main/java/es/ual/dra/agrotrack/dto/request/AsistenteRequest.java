package es.ual.dra.agrotrack.dto.request;

import es.ual.dra.agrotrack.dto.MensajeDto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * Consulta al asistente.
 *
 * @param mensaje        pregunta del usuario
 * @param conversacionId conversación a continuar (usuarios registrados); null crea una nueva
 * @param historial      historial reciente para dar contexto a invitados (sin persistencia);
 *                       en usuarios registrados se ignora, porque el contexto se lee de la BD
 */
public record AsistenteRequest(
    @NotBlank @Size(max = 2000) String mensaje,
    Long conversacionId,
    List<MensajeDto> historial
) {}

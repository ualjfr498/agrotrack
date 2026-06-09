package es.ual.dra.agrotrack.dto;

import es.ual.dra.agrotrack.model.entity.MensajeChat;

import java.time.LocalDateTime;

/**
 * Un mensaje del chat tal como lo intercambian frontend y backend.
 * El rol va en minúsculas ("user"/"assistant") para encajar con el frontend.
 */
public record MensajeDto(
    String rol,
    String texto,
    LocalDateTime fecha
) {
    public static MensajeDto from(MensajeChat m) {
        return new MensajeDto(
            m.getRol().name().toLowerCase(),
            m.getContenido(),
            m.getFecha()
        );
    }
}

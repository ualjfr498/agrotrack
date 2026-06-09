package es.ual.dra.agrotrack.dto.response;

import es.ual.dra.agrotrack.model.entity.Conversacion;

import java.time.LocalDateTime;

/** Resumen de una conversación para la lista lateral (sin los mensajes). */
public record ConversacionResumen(
    Long id,
    String titulo,
    LocalDateTime fechaActualizacion
) {
    public static ConversacionResumen from(Conversacion c) {
        return new ConversacionResumen(c.getId(), c.getTitulo(), c.getFechaActualizacion());
    }
}

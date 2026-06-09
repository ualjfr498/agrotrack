package es.ual.dra.agrotrack.dto.response;

import es.ual.dra.agrotrack.dto.MensajeDto;

import java.util.List;

/** Una conversación con todos sus mensajes, para abrirla y continuarla. */
public record ConversacionDetalle(
    Long id,
    String titulo,
    List<MensajeDto> mensajes
) {}

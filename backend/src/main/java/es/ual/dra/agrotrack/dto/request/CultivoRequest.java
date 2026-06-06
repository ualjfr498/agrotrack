package es.ual.dra.agrotrack.dto.request;

import es.ual.dra.agrotrack.model.enums.EstadoCultivo;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record CultivoRequest(
    @NotNull Long parcelaId,
    @NotNull Long productoId,
    @NotNull LocalDate fechaSiembra,
    EstadoCultivo estado,
    String notas
) {}

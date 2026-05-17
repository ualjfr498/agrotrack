package es.ual.dra.agrotrack.dto.request;

import es.ual.dra.agrotrack.model.enums.EstadoCultivo;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class CultivoRequest {

    /** ID del producto del catálogo Mercasa (obligatorio al crear) */
    private Long productoId;

    private LocalDate fechaSiembra;

    private LocalDate fechaCosecha;

    private EstadoCultivo estado;

    private String notas;
}

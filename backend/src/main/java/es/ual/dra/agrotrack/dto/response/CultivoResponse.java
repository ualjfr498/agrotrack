package es.ual.dra.agrotrack.dto.response;

import es.ual.dra.agrotrack.model.enums.EstadoCultivo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class CultivoResponse {
    private Long id;
    private Long parcelaId;
    private String parcelaNombre;
    private Long productoId;
    private String productoNombre;
    private String categoria;
    private LocalDate fechaSiembra;
    private LocalDate fechaCosecha;
    private EstadoCultivo estado;
    private String notas;
    private LocalDateTime updatedAt;
}

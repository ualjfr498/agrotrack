package es.ual.dra.agrotrack.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class ProductoResponse {
    private Long id;
    private String nombre;
    private String descripcion;
    private String unidad;
    private String categoria;
    private Integer temporadaInicio;
    private Integer temporadaFin;
}

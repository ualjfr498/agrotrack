package es.ual.dra.agrotrack.service.scraping;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO inmutable que representa un precio "tal cual" sale de la fuente
 * externa (Mercasa), antes de mapearlo a las entidades de la BD.
 *
 * El nombre del producto y del mercado son strings sueltos; el
 * ScrapingService los traduce a entidades buscando en sus repos.
 */
public record PrecioCrudo(
    String nombreProducto,
    String nombreMercado,
    LocalDate fecha,
    BigDecimal precioKg
) {}

package es.ual.dra.agrotrack.mcp.tools;

import es.ual.dra.agrotrack.mcp.client.BackendClient;
import es.ual.dra.agrotrack.mcp.dto.ParcelaData;
import es.ual.dra.agrotrack.mcp.dto.ProductoData;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.text.Normalizer;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Resuelve NOMBRES (de producto o de parcela) a sus identificadores numéricos de
 * forma determinista, contra los datos reales del backend.
 *
 * Existe para blindar el punto más frágil del asistente: que el modelo de lenguaje
 * "adivine" un id. Antes las tools recibían un productoId/parcelaId numérico y el
 * LLM podía alucinarlo (registró Aguacate=1 cuando el usuario pidió Tomate maduro).
 * Ahora las tools reciben el nombre tal cual lo dijo el usuario y la resolución a
 * id ocurre AQUÍ, en código, no en la mente del modelo.
 *
 * Estrategia de coincidencia (sobre el nombre normalizado: sin acentos, en
 * minúsculas, espacios colapsados):
 *   1. Coincidencia exacta.
 *   2. Coincidencia parcial en cualquier dirección (cubre plural/singular y nombres
 *      compuestos: "albaricoque"→"Albaricoques", "tomate maduro"→"Tomate maduro").
 * Si 0 coincidencias o más de 1, lanza {@link ResolucionException} con un mensaje
 * pensado para que el asistente pregunte o avise al usuario, en vez de registrar
 * algo incorrecto.
 */
@Component
@RequiredArgsConstructor
public class CatalogoResolver {

    private final BackendClient backend;

    /** Resuelve el id de un producto del catálogo público por su nombre. */
    public Long resolverProductoId(String nombre) {
        return resolver(nombre, backend.listarProductos(),
            ProductoData::nombre, ProductoData::id, "producto");
    }

    /** Resuelve el id de una parcela del usuario autenticado por su nombre. */
    public Long resolverParcelaId(String actingUser, String nombre) {
        return resolver(nombre, backend.misParcelas(actingUser),
            ParcelaData::nombre, ParcelaData::id, "parcela");
    }

    private <T> Long resolver(String termino, List<T> items,
                              Function<T, String> nombreFn, Function<T, Long> idFn,
                              String tipo) {
        if (termino == null || termino.isBlank()) {
            throw new ResolucionException("Debes indicar el nombre del " + tipo + ".");
        }
        String t = clave(termino);

        List<T> exactas = items.stream()
            .filter(i -> clave(nombreFn.apply(i)).equals(t))
            .toList();
        if (exactas.size() == 1) {
            return idFn.apply(exactas.get(0));
        }

        List<T> parciales = items.stream()
            .filter(i -> {
                String n = clave(nombreFn.apply(i));
                return n.contains(t) || t.contains(n);
            })
            .toList();

        if (parciales.isEmpty()) {
            throw new ResolucionException(
                "No existe ningún " + tipo + " que coincida con \"" + termino + "\".");
        }
        if (parciales.size() > 1) {
            String opciones = parciales.stream().map(nombreFn).collect(Collectors.joining(", "));
            throw new ResolucionException(
                "Hay varios resultados para \"" + termino + "\" (" + opciones
                + "). Pregunta al usuario a cuál se refiere exactamente.");
        }
        return idFn.apply(parciales.get(0));
    }

    private String normalizar(String s) {
        String sinAcentos = Normalizer.normalize(s, Normalizer.Form.NFD)
            .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
        return sinAcentos.toLowerCase().trim().replaceAll("\\s+", " ");
    }

    /**
     * Clave de comparación: normaliza y singulariza cada palabra. Así "tomates"
     * casa con "tomate maduro" y "albaricoque" con "Albaricoques", sin que la "s"
     * del plural rompa la coincidencia.
     */
    private String clave(String s) {
        return Arrays.stream(normalizar(s).split(" "))
            .map(this::singular)
            .collect(Collectors.joining(" "));
    }

    /** Singular aproximado: quita la "s" final (suficiente para el catálogo). */
    private String singular(String palabra) {
        return (palabra.length() > 3 && palabra.endsWith("s"))
            ? palabra.substring(0, palabra.length() - 1)
            : palabra;
    }

    /**
     * Señala que un nombre no se pudo resolver a un id único. Su mensaje está
     * redactado para devolverse al modelo y que este pregunte o avise al usuario.
     */
    public static class ResolucionException extends RuntimeException {
        public ResolucionException(String message) {
            super(message);
        }
    }
}

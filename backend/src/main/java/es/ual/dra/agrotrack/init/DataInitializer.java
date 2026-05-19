package es.ual.dra.agrotrack.init;

import es.ual.dra.agrotrack.model.entity.Categoria;
import es.ual.dra.agrotrack.model.entity.MercadoMayorista;
import es.ual.dra.agrotrack.model.entity.Producto;
import es.ual.dra.agrotrack.repository.CategoriaRepository;
import es.ual.dra.agrotrack.repository.MercadoMayoristaRepository;
import es.ual.dra.agrotrack.repository.ProductoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * Bootstrapper que puebla el catálogo inicial al arrancar el backend
 * por primera vez (o cualquier vez que la BD esté vacía).
 *
 * Idempotente: si ya hay categorías en BD, no hace nada. Para re-sembrar,
 * vaciar la BD primero (drop schema o `docker-compose down -v`).
 *
 * Nota: los nombres de producto deben coincidir EXACTAMENTE con los que
 * Mercasa publica en su tabla de precios. El ScrapingService los usa
 * como clave de mapeo. Revisar contra mercasa.es si el scraper deja
 * productos sin precios asociados.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements ApplicationRunner {

    private final CategoriaRepository categoriaRepo;
    private final MercadoMayoristaRepository mercadoRepo;
    private final ProductoRepository productoRepo;

    private static final List<String> FRUTAS = List.of(
        "aguacate", "albaricoques", "caqui", "castañas", "cerezas",
        "chirimoyas", "ciruelas", "fresones", "higos", "kiwi",
        "limones", "mandarina clementina", "mandarinas", "mango",
        "manzana golden", "manzana roja", "manzana starking",
        "melocotones", "melón piel de sapo", "naranja navel",
        "naranja navelina", "nectarinas", "nísperos", "papaya",
        "pera blanquilla", "pera ercolini", "piñas", "plátanos",
        "pomelos", "sandías", "uva blanca", "uva italia", "uva moscatel"
    );

    private static final List<String> HORTALIZAS = List.of(
        "ajo", "alcachofas", "berenjenas", "brócoli", "calabacines",
        "calabaza", "cebollas", "coliflor", "endivia", "escarola",
        "espárragos", "judías verdes", "lechugas", "lombarda",
        "patatas", "pepinos", "pimientos verdes", "puerro", "repollo",
        "tomate maduro", "tomate verde", "zanahorias"
    );

    private static final List<MercadoMayorista> MERCADOS = List.of(
        new MercadoMayorista(null, "Mercamadrid",   "Madrid"),
        new MercadoMayorista(null, "Mercabarna",    "Barcelona"),
        new MercadoMayorista(null, "Mercabilbao",   "Bilbao"),
        new MercadoMayorista(null, "Mercavalencia", "Valencia"),
        new MercadoMayorista(null, "Mercasevilla",  "Sevilla")
    );

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (categoriaRepo.count() > 0) {
            log.info("DataInitializer: catálogo ya poblado, omito siembra.");
            return;
        }

        log.info("DataInitializer: sembrando catálogo inicial de AgroTrack...");

        Categoria frutas     = categoriaRepo.save(new Categoria(null, "FRUTAS"));
        Categoria hortalizas = categoriaRepo.save(new Categoria(null, "HORTALIZAS"));

        mercadoRepo.saveAll(MERCADOS);

        List<Producto> productos = new ArrayList<>(FRUTAS.size() + HORTALIZAS.size());
        FRUTAS.forEach(nombre -> productos.add(crearProducto(nombre, frutas)));
        HORTALIZAS.forEach(nombre -> productos.add(crearProducto(nombre, hortalizas)));
        productoRepo.saveAll(productos);

        log.info("DataInitializer: siembra completa — 2 categorías, {} mercados, {} productos ({} frutas + {} hortalizas).",
            MERCADOS.size(), productos.size(), FRUTAS.size(), HORTALIZAS.size());
    }

    private Producto crearProducto(String nombre, Categoria categoria) {
        Producto p = new Producto();
        p.setNombre(nombre);
        p.setCategoria(categoria);
        return p;
    }
}

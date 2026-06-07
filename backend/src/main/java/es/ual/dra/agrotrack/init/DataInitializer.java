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
 * Nota: los nombres de producto deben coincidir con los que Mercasa publica
 * en su tabla de precios, pero el ScrapingService los compara en minúsculas,
 * así que la capitalización aquí es libre (se usa sentence-case para mostrarlos
 * bien en la UI). Revisar contra mercasa.es si el scraper deja productos sin
 * precios asociados.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements ApplicationRunner {

    private final CategoriaRepository categoriaRepo;
    private final MercadoMayoristaRepository mercadoRepo;
    private final ProductoRepository productoRepo;

    private static final List<String> FRUTAS = List.of(
        "Aguacate", "Albaricoques", "Caqui", "Castañas", "Cerezas",
        "Chirimoyas", "Ciruelas", "Fresones", "Higos", "Kiwi",
        "Limones", "Mandarina clementina", "Mandarinas", "Mango",
        "Manzana golden", "Manzana roja", "Manzana starking",
        "Melocotones", "Melón piel de sapo", "Naranja navel",
        "Naranja navelina", "Nectarinas", "Nísperos", "Papaya",
        "Pera blanquilla", "Pera ercolini", "Piñas", "Plátanos",
        "Pomelos", "Sandías", "Uva blanca", "Uva italia", "Uva moscatel"
    );

    private static final List<String> HORTALIZAS = List.of(
        "Ajo", "Alcachofas", "Berenjenas", "Brócoli", "Calabacines",
        "Calabaza", "Cebollas", "Coliflor", "Endivia", "Escarola",
        "Espárragos", "Judías verdes", "Lechugas", "Lombarda",
        "Patatas", "Pepinos", "Pimientos verdes", "Puerro", "Repollo",
        "Tomate maduro", "Tomate verde", "Zanahorias"
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

import { Component, computed, inject, signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { forkJoin } from 'rxjs';
import { CategoriaResponse } from '../../models/response/categoria-response';
import { ProductoResponse } from '../../models/response/producto-response';
import { CategoriaService } from '../../services/categoria.service';
import { ProductoService } from '../../services/producto.service';

@Component({
  selector: 'app-catalogo',
  imports: [RouterLink],
  templateUrl: './catalogo.html',
  styleUrl: './catalogo.scss'
})
export class Catalogo {
  private readonly productoService = inject(ProductoService);
  private readonly categoriaService = inject(CategoriaService);
  private readonly route = inject(ActivatedRoute);

  protected readonly categorias = signal<CategoriaResponse[]>([]);
  protected readonly productos = signal<ProductoResponse[]>([]);
  protected readonly loading = signal(true);
  protected readonly error = signal<string | null>(null);
  protected readonly categoriaSeleccionada = signal<number | null>(null);
  protected readonly busqueda = signal('');

  protected readonly productosFiltrados = computed(() => {
    const cat = this.categoriaSeleccionada();
    const texto = this.busqueda().trim().toLowerCase();
    return this.productos().filter(p => {
      const coincideCategoria = cat === null || p.categoria.id === cat;
      const coincideTexto = texto === '' || p.nombre.toLowerCase().includes(texto);
      return coincideCategoria && coincideTexto;
    });
  });

  constructor() {
    const initialCategoria = this.route.snapshot.queryParamMap.get('categoriaId');
    if (initialCategoria) {
      this.categoriaSeleccionada.set(Number(initialCategoria));
    }

    forkJoin({
      categorias: this.categoriaService.listar(),
      productos: this.productoService.listar()
    }).subscribe({
      next: ({ categorias, productos }) => {
        this.categorias.set(categorias);
        this.productos.set(productos);
        this.loading.set(false);
      },
      error: () => {
        this.error.set('No se pudo cargar el catálogo.');
        this.loading.set(false);
      }
    });
  }

  filtrar(catId: number | null): void {
    this.categoriaSeleccionada.set(catId);
  }

  buscar(valor: string): void {
    this.busqueda.set(valor);
  }

  /**
   * Muestra la categoría en singular para la etiqueta del producto:
   * "Frutas" → "Fruta", "Hortalizas" → "Hortaliza".
   */
  categoriaSingular(nombre: string): string {
    const limpio = nombre.trim();
    return limpio.toLowerCase().endsWith('s') ? limpio.slice(0, -1) : limpio;
  }
}

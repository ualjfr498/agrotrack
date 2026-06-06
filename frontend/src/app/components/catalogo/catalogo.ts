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

  protected readonly productosFiltrados = computed(() => {
    const cat = this.categoriaSeleccionada();
    const todos = this.productos();
    return cat === null ? todos : todos.filter(p => p.categoria.id === cat);
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
}

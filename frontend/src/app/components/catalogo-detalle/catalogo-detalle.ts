import { DecimalPipe } from '@angular/common';
import { Component, OnInit, computed, inject, input, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { forkJoin } from 'rxjs';
import { PrecioResponse } from '../../models/response/precio-response';
import { ProductoResponse } from '../../models/response/producto-response';
import { PrecioService } from '../../services/precio.service';
import { ProductoService } from '../../services/producto.service';

@Component({
  selector: 'app-catalogo-detalle',
  imports: [DecimalPipe, RouterLink],
  templateUrl: './catalogo-detalle.html',
  styleUrl: './catalogo-detalle.scss'
})
export class CatalogoDetalle implements OnInit {
  private readonly productoService = inject(ProductoService);
  private readonly precioService = inject(PrecioService);

  readonly id = input.required<string>();

  protected readonly producto = signal<ProductoResponse | null>(null);
  protected readonly precios = signal<PrecioResponse[]>([]);
  protected readonly loading = signal(true);
  protected readonly error = signal<string | null>(null);

  protected readonly precioMedio = computed(() => {
    const items = this.precios();
    if (items.length === 0) return null;
    const total = items.reduce((acc, p) => acc + Number(p.precioKg), 0);
    return total / items.length;
  });

  protected readonly precioUltimo = computed(() => {
    const items = this.precios();
    if (items.length === 0) return null;
    return [...items].sort((a, b) => b.fecha.localeCompare(a.fecha))[0];
  });

  ngOnInit(): void {
    const productoId = Number(this.id());
    forkJoin({
      producto: this.productoService.obtener(productoId),
      precios: this.precioService.historial(productoId)
    }).subscribe({
      next: ({ producto, precios }) => {
        this.producto.set(producto);
        this.precios.set(precios);
        this.loading.set(false);
      },
      error: () => {
        this.error.set('No se pudo cargar el producto.');
        this.loading.set(false);
      }
    });
  }
}

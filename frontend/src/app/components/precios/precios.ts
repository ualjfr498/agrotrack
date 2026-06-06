import { DecimalPipe } from '@angular/common';
import { Component, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { PrecioResponse } from '../../models/response/precio-response';
import { ProductoResponse } from '../../models/response/producto-response';
import { PrecioService } from '../../services/precio.service';
import { ProductoService } from '../../services/producto.service';

interface ComparativaMercado {
  mercadoId: number;
  mercadoNombre: string;
  registros: number;
  ultimoPrecio: number;
  ultimaFecha: string;
  media: number;
}

@Component({
  selector: 'app-precios',
  imports: [FormsModule, DecimalPipe],
  templateUrl: './precios.html',
  styleUrl: './precios.scss'
})
export class Precios {
  private readonly productoService = inject(ProductoService);
  private readonly precioService = inject(PrecioService);

  protected readonly productos = signal<ProductoResponse[]>([]);
  protected readonly precios = signal<PrecioResponse[]>([]);
  protected readonly productoIdSeleccionado = signal<number | null>(null);
  protected readonly loadingProductos = signal(true);
  protected readonly loadingPrecios = signal(false);
  protected readonly error = signal<string | null>(null);

  protected readonly comparativa = computed<ComparativaMercado[]>(() => {
    const agrupado = new Map<number, PrecioResponse[]>();
    for (const p of this.precios()) {
      const lista = agrupado.get(p.mercadoId) ?? [];
      lista.push(p);
      agrupado.set(p.mercadoId, lista);
    }

    const result: ComparativaMercado[] = [];
    for (const [mercadoId, items] of agrupado) {
      const ordenados = [...items].sort((a, b) => b.fecha.localeCompare(a.fecha));
      const ultimo = ordenados[0];
      const total = items.reduce((acc, p) => acc + Number(p.precioKg), 0);
      result.push({
        mercadoId,
        mercadoNombre: ultimo.mercadoNombre,
        registros: items.length,
        ultimoPrecio: Number(ultimo.precioKg),
        ultimaFecha: ultimo.fecha,
        media: total / items.length
      });
    }
    return result.sort((a, b) => a.ultimoPrecio - b.ultimoPrecio);
  });

  constructor() {
    this.productoService.listar().subscribe({
      next: data => {
        this.productos.set(data);
        this.loadingProductos.set(false);
        if (data.length > 0) {
          this.seleccionarProducto(data[0].id);
        }
      },
      error: () => {
        this.error.set('No se pudo cargar la lista de productos.');
        this.loadingProductos.set(false);
      }
    });
  }

  onProductoChange(value: string): void {
    this.seleccionarProducto(Number(value));
  }

  private seleccionarProducto(id: number): void {
    this.productoIdSeleccionado.set(id);
    this.loadingPrecios.set(true);
    this.precios.set([]);

    this.precioService.historial(id).subscribe({
      next: data => {
        this.precios.set(data);
        this.loadingPrecios.set(false);
      },
      error: () => {
        this.error.set('No se pudieron cargar los precios.');
        this.loadingPrecios.set(false);
      }
    });
  }
}

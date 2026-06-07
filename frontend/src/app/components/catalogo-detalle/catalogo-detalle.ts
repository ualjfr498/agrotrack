import { DecimalPipe } from '@angular/common';
import {
  Component, ElementRef, OnDestroy, OnInit,
  computed, effect, inject, input, signal, viewChild
} from '@angular/core';
import { RouterLink } from '@angular/router';
import { forkJoin } from 'rxjs';
import { Chart, registerables } from 'chart.js';
import { PrecioResponse } from '../../models/response/precio-response';
import { ProductoResponse } from '../../models/response/producto-response';
import { PrecioService } from '../../services/precio.service';
import { ProductoService } from '../../services/producto.service';

Chart.register(...registerables);

/** Color fijo por mercado para que la línea sea reconocible entre vistas. */
const COLOR_MERCADO: Record<string, string> = {
  Mercamadrid: '#2e7d32',
  Mercabarna: '#1565c0',
  Mercabilbao: '#c62828',
  Mercavalencia: '#f9a825',
  Mercasevilla: '#6a1b9a'
};
const COLOR_FALLBACK = '#607d8b';

@Component({
  selector: 'app-catalogo-detalle',
  imports: [DecimalPipe, RouterLink],
  templateUrl: './catalogo-detalle.html',
  styleUrl: './catalogo-detalle.scss'
})
export class CatalogoDetalle implements OnInit, OnDestroy {
  private readonly productoService = inject(ProductoService);
  private readonly precioService = inject(PrecioService);

  readonly id = input.required<string>();

  protected readonly producto = signal<ProductoResponse | null>(null);
  protected readonly precios = signal<PrecioResponse[]>([]);
  protected readonly loading = signal(true);
  protected readonly error = signal<string | null>(null);

  private readonly chartCanvas = viewChild<ElementRef<HTMLCanvasElement>>('priceChart');
  private chart?: Chart;

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

  constructor() {
    // Cuando el canvas esté en el DOM y haya precios, (re)dibuja el gráfico.
    effect(() => {
      const canvasRef = this.chartCanvas();
      const datos = this.precios();
      if (canvasRef && datos.length > 0) {
        this.dibujarGrafico(canvasRef.nativeElement, datos);
      }
    });
  }

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

  ngOnDestroy(): void {
    this.chart?.destroy();
  }

  /**
   * Pivota la lista de precios a una serie por mercado y la pinta como gráfico
   * de líneas. La leyenda de Chart.js es clicable: permite ocultar/mostrar cada
   * mercado (o aislar uno solo) sin código extra.
   */
  private dibujarGrafico(canvas: HTMLCanvasElement, datos: PrecioResponse[]): void {
    const fechas = [...new Set(datos.map(p => p.fecha))].sort();
    const mercados = [...new Set(datos.map(p => p.mercadoNombre))];

    const datasets = mercados.map(mercado => ({
      label: mercado,
      data: fechas.map(f => {
        const p = datos.find(x => x.mercadoNombre === mercado && x.fecha === f);
        return p ? Number(p.precioKg) : null;
      }),
      borderColor: COLOR_MERCADO[mercado] ?? COLOR_FALLBACK,
      backgroundColor: COLOR_MERCADO[mercado] ?? COLOR_FALLBACK,
      tension: 0.3,
      spanGaps: true,
      pointRadius: 4,
      pointHoverRadius: 6
    }));

    this.chart?.destroy();
    this.chart = new Chart(canvas, {
      type: 'line',
      data: { labels: fechas.map(f => this.formatearFecha(f)), datasets },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        interaction: { mode: 'index', intersect: false },
        plugins: {
          legend: { position: 'bottom' },
          tooltip: { callbacks: { label: ctx => `${ctx.dataset.label}: ${ctx.parsed.y} €/kg` } }
        },
        scales: {
          y: { title: { display: true, text: '€/kg' }, beginAtZero: false }
        }
      }
    });
  }

  /** "2026-06-02" -> "02/06" */
  private formatearFecha(iso: string): string {
    const [, mes, dia] = iso.split('-');
    return `${dia}/${mes}`;
  }
}

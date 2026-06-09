import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { forkJoin } from 'rxjs';
import { EstadoCultivo } from '../../models/enums/estado-cultivo';
import { CultivoResponse } from '../../models/response/cultivo-response';
import { ParcelaResponse } from '../../models/response/parcela-response';
import { ProductoResponse } from '../../models/response/producto-response';
import { CultivoService } from '../../services/cultivo.service';
import { ParcelaService } from '../../services/parcela.service';
import { ProductoService } from '../../services/producto.service';
import { imagenADataUrl } from '../../utils/imagen.util';

@Component({
  selector: 'app-mi-parcela',
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './mi-parcela.html',
  styleUrl: './mi-parcela.scss'
})
export class MiParcela {
  private readonly fb = inject(FormBuilder);
  private readonly parcelaService = inject(ParcelaService);
  private readonly cultivoService = inject(CultivoService);
  private readonly productoService = inject(ProductoService);

  protected readonly parcelas = signal<ParcelaResponse[]>([]);
  protected readonly cultivos = signal<CultivoResponse[]>([]);
  protected readonly productos = signal<ProductoResponse[]>([]);
  protected readonly loading = signal(true);
  protected readonly error = signal<string | null>(null);
  protected readonly estados = Object.values(EstadoCultivo);

  // null = creando una parcela nueva; un id = editando esa parcela.
  protected readonly editandoId = signal<number | null>(null);
  // Imagen seleccionada (data URL base64), compartida por crear y editar.
  protected readonly imagenParcela = signal<string | null>(null);

  protected readonly parcelaForm = this.fb.group({
    nombre: ['', [Validators.required, Validators.maxLength(80)]],
    superficieM2: [null as number | null],
    descripcion: ['']
  });

  protected readonly cultivoForm = this.fb.group({
    parcelaId: [null as number | null, [Validators.required]],
    productoId: [null as number | null, [Validators.required]],
    fechaSiembra: ['', [Validators.required]],
    estado: [EstadoCultivo.SEMBRADO],
    notas: ['']
  });

  constructor() {
    forkJoin({
      parcelas: this.parcelaService.listar(),
      cultivos: this.cultivoService.listar(),
      productos: this.productoService.listar()
    }).subscribe({
      next: ({ parcelas, cultivos, productos }) => {
        this.parcelas.set(parcelas);
        this.cultivos.set(cultivos);
        this.productos.set(productos);
        this.loading.set(false);
      },
      error: () => {
        this.error.set('No se pudieron cargar tus parcelas.');
        this.loading.set(false);
      }
    });
  }

  cultivosDe(parcelaId: number): CultivoResponse[] {
    return this.cultivos().filter(c => c.parcelaId === parcelaId);
  }

  async onImagen(event: Event): Promise<void> {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0];
    if (!file) return;
    try {
      this.imagenParcela.set(await imagenADataUrl(file));
      this.error.set(null);
    } catch (e) {
      this.error.set((e as Error).message);
    }
  }

  quitarImagen(): void {
    this.imagenParcela.set(null);
  }

  editar(p: ParcelaResponse): void {
    this.editandoId.set(p.id);
    this.imagenParcela.set(p.imagen);
    this.parcelaForm.setValue({
      nombre: p.nombre,
      superficieM2: p.superficieM2,
      descripcion: p.descripcion ?? ''
    });
    this.error.set(null);
  }

  cancelarEdicion(): void {
    this.editandoId.set(null);
    this.imagenParcela.set(null);
    this.parcelaForm.reset();
  }

  guardarParcela(): void {
    if (this.parcelaForm.invalid) {
      this.parcelaForm.markAllAsTouched();
      return;
    }
    const v = this.parcelaForm.getRawValue();
    const req = {
      nombre: v.nombre!,
      superficieM2: v.superficieM2 ?? undefined,
      descripcion: v.descripcion?.trim() || undefined,
      imagen: this.imagenParcela() ?? undefined
    };
    const id = this.editandoId();

    if (id !== null) {
      this.parcelaService.editar(id, req).subscribe({
        next: actualizada => {
          this.parcelas.update(list => list.map(p => p.id === id ? actualizada : p));
          this.cancelarEdicion();
        },
        error: () => this.error.set('No se pudo guardar la parcela.')
      });
    } else {
      this.parcelaService.crear(req).subscribe({
        next: parcela => {
          this.parcelas.update(list => [...list, parcela]);
          this.cancelarEdicion();
        },
        error: () => this.error.set('No se pudo crear la parcela.')
      });
    }
  }

  eliminarParcela(p: ParcelaResponse): void {
    if (!confirm(`¿Eliminar la parcela "${p.nombre}" y todos sus cultivos?`)) return;
    this.parcelaService.eliminar(p.id).subscribe({
      next: () => {
        this.parcelas.update(list => list.filter(x => x.id !== p.id));
        this.cultivos.update(list => list.filter(c => c.parcelaId !== p.id));
        if (this.editandoId() === p.id) this.cancelarEdicion();
      },
      error: () => this.error.set('No se pudo eliminar la parcela.')
    });
  }

  eliminarCultivo(c: CultivoResponse): void {
    if (!confirm(`¿Eliminar el cultivo de ${c.productoNombre}?`)) return;
    this.cultivoService.eliminar(c.id).subscribe({
      next: () => this.cultivos.update(list => list.filter(x => x.id !== c.id)),
      error: () => this.error.set('No se pudo eliminar el cultivo.')
    });
  }

  crearCultivo(): void {
    if (this.cultivoForm.invalid) {
      this.cultivoForm.markAllAsTouched();
      return;
    }
    const v = this.cultivoForm.getRawValue();
    this.cultivoService.crear({
      parcelaId: v.parcelaId!,
      productoId: v.productoId!,
      fechaSiembra: v.fechaSiembra!,
      estado: v.estado ?? undefined,
      notas: v.notas?.trim() || undefined
    }).subscribe({
      next: cultivo => {
        this.cultivos.update(list => [...list, cultivo]);
        this.cultivoForm.reset({ estado: EstadoCultivo.SEMBRADO });
      },
      error: () => this.error.set('No se pudo registrar el cultivo.')
    });
  }
}

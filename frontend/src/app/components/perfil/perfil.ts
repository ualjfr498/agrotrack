import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { PerfilResponse } from '../../models/response/perfil-response';
import { PerfilService } from '../../services/perfil.service';
import { imagenADataUrl } from '../../utils/imagen.util';

@Component({
  selector: 'app-perfil',
  imports: [ReactiveFormsModule],
  templateUrl: './perfil.html',
  styleUrl: './perfil.scss'
})
export class Perfil {
  private readonly fb = inject(FormBuilder);
  private readonly perfilService = inject(PerfilService);

  protected readonly perfil = signal<PerfilResponse | null>(null);
  protected readonly foto = signal<string | null>(null);
  protected readonly loading = signal(true);
  protected readonly error = signal<string | null>(null);
  protected readonly guardado = signal(false);

  protected readonly form = this.fb.group({
    nombre: ['', [Validators.required, Validators.maxLength(80)]],
    apellidos: ['', [Validators.required, Validators.maxLength(120)]]
  });

  constructor() {
    this.perfilService.obtener().subscribe({
      next: p => {
        this.perfil.set(p);
        this.foto.set(p.foto);
        this.form.setValue({ nombre: p.nombre, apellidos: p.apellidos });
        this.loading.set(false);
      },
      error: () => {
        this.error.set('No se pudo cargar tu perfil.');
        this.loading.set(false);
      }
    });
  }

  async onFoto(event: Event): Promise<void> {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0];
    if (!file) return;
    try {
      this.foto.set(await imagenADataUrl(file));
      this.error.set(null);
    } catch (e) {
      this.error.set((e as Error).message);
    }
  }

  quitarFoto(): void {
    this.foto.set(null);
  }

  guardar(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    const v = this.form.getRawValue();
    this.guardado.set(false);
    this.perfilService.editar({
      nombre: v.nombre!,
      apellidos: v.apellidos!,
      foto: this.foto() ?? undefined
    }).subscribe({
      next: p => {
        this.perfil.set(p);
        this.foto.set(p.foto);
        this.guardado.set(true);
        this.error.set(null);
      },
      error: () => this.error.set('No se pudieron guardar los cambios.')
    });
  }
}

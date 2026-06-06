import { Component, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { CategoriaResponse } from '../../models/response/categoria-response';
import { CategoriaService } from '../../services/categoria.service';

@Component({
  selector: 'app-home',
  imports: [RouterLink],
  templateUrl: './home.html',
  styleUrl: './home.scss'
})
export class Home {
  private readonly categoriaService = inject(CategoriaService);

  protected readonly categorias = signal<CategoriaResponse[]>([]);
  protected readonly loading = signal(true);
  protected readonly error = signal<string | null>(null);

  constructor() {
    this.categoriaService.listar().subscribe({
      next: data => {
        this.categorias.set(data);
        this.loading.set(false);
      },
      error: () => {
        this.error.set('No se pudieron cargar las categorías.');
        this.loading.set(false);
      }
    });
  }
}

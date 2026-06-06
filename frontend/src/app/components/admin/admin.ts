import { Component, inject, signal } from '@angular/core';
import { EstadoScraping } from '../../models/enums/estado-scraping';
import { ScrapingLogResponse } from '../../models/response/scraping-log-response';
import { AdminPrecioService } from '../../services/admin-precio.service';

@Component({
  selector: 'app-admin',
  imports: [],
  templateUrl: './admin.html',
  styleUrl: './admin.scss'
})
export class Admin {
  private readonly adminPrecioService = inject(AdminPrecioService);

  protected readonly EstadoScraping = EstadoScraping;
  protected readonly ultimaEjecucion = signal<ScrapingLogResponse | null>(null);
  protected readonly ejecutando = signal(false);
  protected readonly error = signal<string | null>(null);

  dispararScraping(): void {
    this.error.set(null);
    this.ejecutando.set(true);
    this.adminPrecioService.dispararScraping().subscribe({
      next: log => {
        this.ultimaEjecucion.set(log);
        this.ejecutando.set(false);
      },
      error: () => {
        this.error.set('El scraping falló. Mira los logs del backend.');
        this.ejecutando.set(false);
      }
    });
  }
}

import { Component, effect, inject, signal } from '@angular/core';
import { Router, RouterLink, RouterLinkActive } from '@angular/router';
import { AuthService } from '../../../services/auth.service';
import { PerfilService } from '../../../services/perfil.service';

@Component({
  selector: 'app-header',
  imports: [RouterLink, RouterLinkActive],
  templateUrl: './header.html',
  styleUrl: './header.scss'
})
export class Header {
  protected readonly auth = inject(AuthService);
  private readonly perfilService = inject(PerfilService);
  private readonly router = inject(Router);

  // Foto de perfil para la barra; se recarga al iniciar/cerrar sesión.
  protected readonly foto = signal<string | null>(null);

  constructor() {
    effect(() => {
      if (this.auth.isAuthenticated()) {
        this.perfilService.obtener().subscribe({
          next: p => this.foto.set(p.foto),
          error: () => this.foto.set(null)
        });
      } else {
        this.foto.set(null);
      }
    });
  }

  logout(): void {
    this.auth.logout();
    this.router.navigate(['/']);
  }
}

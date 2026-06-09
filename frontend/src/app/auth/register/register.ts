import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { imagenADataUrl } from '../../utils/imagen.util';

@Component({
  selector: 'app-register',
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './register.html',
  styleUrl: './register.scss'
})
export class Register {
  private readonly fb = inject(FormBuilder);
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);

  protected readonly form = this.fb.nonNullable.group({
    nombre: ['', [Validators.required, Validators.maxLength(80)]],
    apellidos: ['', [Validators.required, Validators.maxLength(120)]],
    email: ['', [Validators.required, Validators.email, Validators.maxLength(120)]],
    password: ['', [Validators.required, Validators.minLength(8), Validators.maxLength(128)]]
  });

  protected readonly error = signal<string | null>(null);
  protected readonly loading = signal(false);
  protected readonly foto = signal<string | null>(null);

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

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.error.set(null);
    this.loading.set(true);

    const credentials = { ...this.form.getRawValue(), foto: this.foto() ?? undefined };
    this.auth.register(credentials).subscribe({
      next: () => {
        this.auth.login(credentials).subscribe({
          next: () => this.router.navigateByUrl('/'),
          error: () => {
            this.loading.set(false);
            this.router.navigateByUrl('/login');
          }
        });
      },
      error: err => {
        this.loading.set(false);
        this.error.set(
          err?.status === 409
            ? 'Ese email ya está registrado.'
            : 'No se pudo completar el registro. Revisa los datos.'
        );
      }
    });
  }
}

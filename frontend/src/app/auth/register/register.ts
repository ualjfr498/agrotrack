import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../services/auth.service';

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
    email: ['', [Validators.required, Validators.email, Validators.maxLength(120)]],
    password: ['', [Validators.required, Validators.minLength(8), Validators.maxLength(128)]]
  });

  protected readonly error = signal<string | null>(null);
  protected readonly loading = signal(false);

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.error.set(null);
    this.loading.set(true);

    const credentials = this.form.getRawValue();
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

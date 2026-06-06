import { HttpClient } from '@angular/common/http';
import { Injectable, computed, inject, signal } from '@angular/core';
import { Observable, tap } from 'rxjs';
import { Rol } from '../models/enums/rol';
import { LoginRequest } from '../models/request/login-request';
import { RegisterRequest } from '../models/request/register-request';
import { JwtResponse } from '../models/response/jwt-response';

const STORAGE_KEY = 'agrotrack.session';

interface SessionData {
  token: string;
  email: string;
  rol: Rol;
  expiresAt: number;
}

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);

  private readonly session = signal<SessionData | null>(this.loadFromStorage());

  readonly currentUser = computed(() => this.session());
  readonly isAuthenticated = computed(() => {
    const s = this.session();
    if (!s) return false;
    return s.expiresAt > Date.now();
  });
  readonly isAdmin = computed(() => this.session()?.rol === Rol.ADMIN);

  login(req: LoginRequest): Observable<JwtResponse> {
    return this.http.post<JwtResponse>('/api/auth/login', req).pipe(
      tap(res => this.persistSession(res))
    );
  }

  register(req: RegisterRequest): Observable<void> {
    return this.http.post<void>('/api/auth/register', req);
  }

  logout(): void {
    this.session.set(null);
    localStorage.removeItem(STORAGE_KEY);
  }

  getToken(): string | null {
    const s = this.session();
    return s && s.expiresAt > Date.now() ? s.token : null;
  }

  private persistSession(res: JwtResponse): void {
    const data: SessionData = {
      token: res.token,
      email: res.email,
      rol: res.rol,
      expiresAt: res.expiresAt
    };
    this.session.set(data);
    localStorage.setItem(STORAGE_KEY, JSON.stringify(data));
  }

  private loadFromStorage(): SessionData | null {
    const raw = typeof localStorage !== 'undefined' ? localStorage.getItem(STORAGE_KEY) : null;
    if (!raw) return null;
    try {
      const parsed = JSON.parse(raw) as SessionData;
      if (parsed.expiresAt <= Date.now()) {
        localStorage.removeItem(STORAGE_KEY);
        return null;
      }
      return parsed;
    } catch {
      localStorage.removeItem(STORAGE_KEY);
      return null;
    }
  }
}

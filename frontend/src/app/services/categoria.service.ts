import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { CategoriaResponse } from '../models/response/categoria-response';

@Injectable({ providedIn: 'root' })
export class CategoriaService {
  private readonly http = inject(HttpClient);

  listar(): Observable<CategoriaResponse[]> {
    return this.http.get<CategoriaResponse[]>('/api/categorias');
  }
}

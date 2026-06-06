import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { ProductoResponse } from '../models/response/producto-response';

@Injectable({ providedIn: 'root' })
export class ProductoService {
  private readonly http = inject(HttpClient);

  listar(categoriaId?: number): Observable<ProductoResponse[]> {
    let params = new HttpParams();
    if (categoriaId !== undefined) {
      params = params.set('categoriaId', categoriaId);
    }
    return this.http.get<ProductoResponse[]>('/api/productos', { params });
  }

  obtener(id: number): Observable<ProductoResponse> {
    return this.http.get<ProductoResponse>(`/api/productos/${id}`);
  }
}

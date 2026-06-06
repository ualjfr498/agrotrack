import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { PrecioResponse } from '../models/response/precio-response';

@Injectable({ providedIn: 'root' })
export class PrecioService {
  private readonly http = inject(HttpClient);

  historial(productoId: number): Observable<PrecioResponse[]> {
    return this.http.get<PrecioResponse[]>(`/api/precios/${productoId}`);
  }
}

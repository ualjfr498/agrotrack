import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { ConversacionDetalle } from '../models/response/conversacion-detalle';
import { ConversacionResumen } from '../models/response/conversacion-resumen';

@Injectable({ providedIn: 'root' })
export class ConversacionService {
  private readonly http = inject(HttpClient);

  listar(): Observable<ConversacionResumen[]> {
    return this.http.get<ConversacionResumen[]>('/api/conversaciones');
  }

  obtener(id: number): Observable<ConversacionDetalle> {
    return this.http.get<ConversacionDetalle>(`/api/conversaciones/${id}`);
  }

  eliminar(id: number): Observable<void> {
    return this.http.delete<void>(`/api/conversaciones/${id}`);
  }
}

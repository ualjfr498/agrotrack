import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { ParcelaRequest } from '../models/request/parcela-request';
import { ParcelaResponse } from '../models/response/parcela-response';

@Injectable({ providedIn: 'root' })
export class ParcelaService {
  private readonly http = inject(HttpClient);

  listar(): Observable<ParcelaResponse[]> {
    return this.http.get<ParcelaResponse[]>('/api/parcelas');
  }

  crear(req: ParcelaRequest): Observable<ParcelaResponse> {
    return this.http.post<ParcelaResponse>('/api/parcelas', req);
  }

  editar(id: number, req: ParcelaRequest): Observable<ParcelaResponse> {
    return this.http.put<ParcelaResponse>(`/api/parcelas/${id}`, req);
  }

  eliminar(id: number): Observable<void> {
    return this.http.delete<void>(`/api/parcelas/${id}`);
  }
}

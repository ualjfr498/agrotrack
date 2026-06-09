import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { CultivoRequest } from '../models/request/cultivo-request';
import { CultivoResponse } from '../models/response/cultivo-response';

@Injectable({ providedIn: 'root' })
export class CultivoService {
  private readonly http = inject(HttpClient);

  listar(): Observable<CultivoResponse[]> {
    return this.http.get<CultivoResponse[]>('/api/cultivos');
  }

  crear(req: CultivoRequest): Observable<CultivoResponse> {
    return this.http.post<CultivoResponse>('/api/cultivos', req);
  }

  eliminar(id: number): Observable<void> {
    return this.http.delete<void>(`/api/cultivos/${id}`);
  }
}

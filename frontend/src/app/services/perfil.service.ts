import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { PerfilUpdateRequest } from '../models/request/perfil-update-request';
import { PerfilResponse } from '../models/response/perfil-response';

@Injectable({ providedIn: 'root' })
export class PerfilService {
  private readonly http = inject(HttpClient);

  obtener(): Observable<PerfilResponse> {
    return this.http.get<PerfilResponse>('/api/perfil');
  }

  editar(req: PerfilUpdateRequest): Observable<PerfilResponse> {
    return this.http.put<PerfilResponse>('/api/perfil', req);
  }
}

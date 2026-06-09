import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { AsistenteRequest } from '../models/request/asistente-request';
import { AsistenteResponse } from '../models/response/asistente-response';

@Injectable({ providedIn: 'root' })
export class AsistenteService {
  private readonly http = inject(HttpClient);

  consultar(req: AsistenteRequest): Observable<AsistenteResponse> {
    return this.http.post<AsistenteResponse>('/api/asistente/consulta', req);
  }
}

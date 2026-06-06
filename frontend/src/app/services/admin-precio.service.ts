import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { ScrapingLogResponse } from '../models/response/scraping-log-response';

@Injectable({ providedIn: 'root' })
export class AdminPrecioService {
  private readonly http = inject(HttpClient);

  dispararScraping(): Observable<ScrapingLogResponse> {
    return this.http.post<ScrapingLogResponse>('/api/admin/precios/actualizar', {});
  }
}

import { EstadoScraping } from '../enums/estado-scraping';

export interface ScrapingLogResponse {
  id: number;
  fechaEjecucion: string;
  estado: EstadoScraping;
  filasInsertadas: number | null;
  duracionMs: number | null;
  mensaje: string | null;
}

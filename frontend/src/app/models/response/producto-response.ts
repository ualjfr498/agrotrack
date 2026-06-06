import { CategoriaResponse } from './categoria-response';

export interface ProductoResponse {
  id: number;
  nombre: string;
  descripcion: string | null;
  imagenUrl: string | null;
  temporadaInicio: number | null;
  temporadaFin: number | null;
  categoria: CategoriaResponse;
}

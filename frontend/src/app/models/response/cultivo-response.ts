import { EstadoCultivo } from '../enums/estado-cultivo';

export interface CultivoResponse {
  id: number;
  parcelaId: number;
  parcelaNombre: string;
  productoId: number;
  productoNombre: string;
  fechaSiembra: string;
  estado: EstadoCultivo;
  notas: string | null;
}

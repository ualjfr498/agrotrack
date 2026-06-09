import { EstadoCultivo } from '../enums/estado-cultivo';

export interface CultivoRequest {
  parcelaId: number;
  productoId: number;
  fechaSiembra: string;
  estado?: EstadoCultivo;
  notas?: string;
}

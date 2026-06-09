import { Rol } from '../enums/rol';

export interface PerfilResponse {
  id: number;
  email: string;
  nombre: string;
  apellidos: string;
  rol: Rol;
  foto: string | null;
}

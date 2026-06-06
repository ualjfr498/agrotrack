import { Rol } from '../enums/rol';

export interface JwtResponse {
  token: string;
  email: string;
  rol: Rol;
  expiresAt: number;
}

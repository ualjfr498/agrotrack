import { MensajeChat } from '../mensaje-chat';

export interface AsistenteRequest {
  mensaje: string;
  conversacionId?: number | null;
  historial?: MensajeChat[];
}

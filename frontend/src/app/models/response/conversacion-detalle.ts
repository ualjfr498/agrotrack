import { MensajeChat } from '../mensaje-chat';

export interface ConversacionDetalle {
  id: number;
  titulo: string;
  mensajes: MensajeChat[];
}

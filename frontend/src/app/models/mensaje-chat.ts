export interface MensajeChat {
  rol: 'user' | 'assistant';
  texto: string;
  fecha?: string;
}

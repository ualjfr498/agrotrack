import { Component, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { MensajeChat } from '../../models/mensaje-chat';
import { ConversacionResumen } from '../../models/response/conversacion-resumen';
import { AsistenteService } from '../../services/asistente.service';
import { AuthService } from '../../services/auth.service';
import { ConversacionService } from '../../services/conversacion.service';

@Component({
  selector: 'app-asistente',
  imports: [FormsModule, RouterLink],
  templateUrl: './asistente.html',
  styleUrl: './asistente.scss'
})
export class Asistente {
  private readonly asistenteService = inject(AsistenteService);
  private readonly conversacionService = inject(ConversacionService);
  protected readonly auth = inject(AuthService);

  protected readonly mensajes = signal<MensajeChat[]>([]);
  protected readonly entrada = signal('');
  protected readonly enviando = signal(false);
  protected readonly error = signal<string | null>(null);

  // Solo para usuarios registrados: lista de chats persistidos y el chat abierto.
  protected readonly conversaciones = signal<ConversacionResumen[]>([]);
  protected readonly conversacionActualId = signal<number | null>(null);

  protected readonly sugerencias = computed(() =>
    this.auth.isAuthenticated()
      ? [
          '¿Dónde me pagarían mejor por mis cultivos?',
          '¿Qué me recomiendas sembrar en mi parcela?',
          'Registra un cultivo de tomate en mi huerta sembrado hoy'
        ]
      : [
          '¿Cuántas categorías de productos hay?',
          'Compara el precio del albaricoque entre los mercados',
          '¿En qué mercado está más barata la cereza?'
        ]
  );

  constructor() {
    if (this.auth.isAuthenticated()) {
      this.cargarConversaciones();
    }
  }

  private cargarConversaciones(): void {
    this.conversacionService.listar().subscribe({
      next: list => this.conversaciones.set(list),
      error: () => {}
    });
  }

  usarSugerencia(texto: string): void {
    this.entrada.set(texto);
  }

  nuevoChat(): void {
    this.mensajes.set([]);
    this.conversacionActualId.set(null);
    this.error.set(null);
  }

  abrirConversacion(id: number): void {
    if (this.enviando()) return;
    this.conversacionService.obtener(id).subscribe({
      next: det => {
        this.mensajes.set(det.mensajes);
        this.conversacionActualId.set(det.id);
        this.error.set(null);
      },
      error: () => this.error.set('No se pudo abrir la conversación.')
    });
  }

  eliminarConversacion(c: ConversacionResumen, event: Event): void {
    event.stopPropagation();
    if (!confirm(`¿Eliminar la conversación "${c.titulo}"?`)) return;
    this.conversacionService.eliminar(c.id).subscribe({
      next: () => {
        this.conversaciones.update(list => list.filter(x => x.id !== c.id));
        if (this.conversacionActualId() === c.id) this.nuevoChat();
      },
      error: () => this.error.set('No se pudo eliminar la conversación.')
    });
  }

  enviar(): void {
    const texto = this.entrada().trim();
    if (texto === '' || this.enviando()) {
      return;
    }
    this.error.set(null);
    this.mensajes.update(m => [...m, { rol: 'user', texto }]);
    this.entrada.set('');
    this.enviando.set(true);

    const autenticado = this.auth.isAuthenticated();
    this.asistenteService.consultar({
      mensaje: texto,
      conversacionId: autenticado ? this.conversacionActualId() : null,
      // El historial solo hace falta para invitados (sin persistencia en servidor).
      historial: autenticado ? undefined : this.mensajes()
    }).subscribe({
      next: res => {
        this.mensajes.update(m => [...m, { rol: 'assistant', texto: res.respuesta }]);
        this.enviando.set(false);
        if (autenticado && res.conversacionId != null) {
          const esNueva = this.conversacionActualId() == null;
          this.conversacionActualId.set(res.conversacionId);
          if (esNueva) this.cargarConversaciones();
        }
      },
      error: () => {
        this.enviando.set(false);
        this.error.set('No se pudo obtener respuesta. Inténtalo de nuevo en unos segundos.');
      }
    });
  }
}

import { WS_URL } from '@/lib/constants';
import type { WsEvent } from '@/types';

type EventHandler = (event: WsEvent) => void;

export class WebSocketService {
  private ws: WebSocket | null = null;
  private handlers: EventHandler[] = [];
  private sessionId: number | null = null;
  private reconnectTimer: NodeJS.Timeout | null = null;

  connect(sessionId: number) {
    this.sessionId = sessionId;
    this.ws = new WebSocket(`${WS_URL}/ws/cart/${sessionId}`);

    this.ws.onmessage = (event) => {
      try {
        const data: WsEvent = JSON.parse(event.data);
        this.handlers.forEach((h) => h(data));
      } catch (e) { /* ignore parse errors */ }
    };

    this.ws.onclose = () => {
      this.reconnectTimer = setTimeout(() => {
        if (this.sessionId) this.connect(this.sessionId);
      }, 3000);
    };

    this.ws.onerror = () => this.ws?.close();
  }

  disconnect() {
    if (this.reconnectTimer) clearTimeout(this.reconnectTimer);
    this.reconnectTimer = null;
    this.sessionId = null;
    this.ws?.close();
    this.ws = null;
  }

  onEvent(handler: EventHandler) {
    this.handlers.push(handler);
    return () => { this.handlers = this.handlers.filter((h) => h !== handler); };
  }
}

export const wsService = new WebSocketService();

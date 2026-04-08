import type { WsEvent } from '@/types';

type EventHandler = (event: WsEvent) => void;

function getWsUrl(sessionId: number, profileId?: number): string {
  if (typeof window === 'undefined') return '';
  const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:';
  const host = window.location.hostname;
  // WebSocket은 백엔드(8080)에 직접 연결
  let url = `${protocol}//${host}:8080/ws/cart/${sessionId}`;
  if (profileId) url += `?profileId=${profileId}`;
  return url;
}

export class WebSocketService {
  private ws: WebSocket | null = null;
  private handlers: EventHandler[] = [];
  private sessionId: number | null = null;
  private profileId: number | undefined;
  private reconnectTimer: NodeJS.Timeout | null = null;

  connect(sessionId: number, profileId?: number) {
    this.sessionId = sessionId;
    this.profileId = profileId;
    const url = getWsUrl(sessionId, profileId);
    if (!url) return;
    this.ws = new WebSocket(url);

    this.ws.onmessage = (event) => {
      try {
        const data: WsEvent = JSON.parse(event.data);
        this.handlers.forEach((h) => h(data));
      } catch (e) { /* ignore parse errors */ }
    };

    this.ws.onclose = () => {
      this.reconnectTimer = setTimeout(() => {
        if (this.sessionId) this.connect(this.sessionId, this.profileId);
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

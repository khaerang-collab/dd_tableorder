import { API_URL } from '@/lib/constants';

type SseHandler = (eventType: string, data: unknown) => void;

export class SseService {
  private source: EventSource | null = null;
  private handlers: SseHandler[] = [];

  connect(storeId: number) {
    const token = localStorage.getItem('token');
    this.source = new EventSource(`${API_URL}/api/admin/stores/${storeId}/orders/stream?token=${token}`);

    const events = ['NEW_ORDER', 'ORDER_STATUS_CHANGED', 'ORDER_DELETED', 'TABLE_COMPLETED', 'STAFF_CALL', 'CONNECTED'];
    events.forEach((type) => {
      this.source?.addEventListener(type, (e) => {
        try {
          const data = JSON.parse((e as MessageEvent).data);
          this.handlers.forEach((h) => h(type, data));
        } catch { /* ignore */ }
      });
    });

    this.source.onerror = () => {
      this.source?.close();
      setTimeout(() => this.connect(storeId), 3000);
    };
  }

  disconnect() {
    this.source?.close();
    this.source = null;
  }

  onEvent(handler: SseHandler) {
    this.handlers.push(handler);
    return () => { this.handlers = this.handlers.filter((h) => h !== handler); };
  }
}

export const sseService = new SseService();

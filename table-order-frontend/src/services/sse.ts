type SseHandler = (eventType: string, data: unknown) => void;

function getSseUrl(storeId: number): string {
  if (typeof window === 'undefined') return '';
  const host = window.location.hostname;
  const token = localStorage.getItem('token');
  // SSE는 백엔드(8080)에 직접 연결
  return `http://${host}:8080/api/admin/stores/${storeId}/orders/stream?token=${token}`;
}

export class SseService {
  private source: EventSource | null = null;
  private handlers: SseHandler[] = [];

  connect(storeId: number) {
    const url = getSseUrl(storeId);
    if (!url) return;
    this.source = new EventSource(url);

    const events = ['NEW_ORDER', 'ORDER_STATUS_CHANGED', 'ORDER_DELETED', 'TABLE_COMPLETED', 'CONNECTED', 'STAFF_CALL'];
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

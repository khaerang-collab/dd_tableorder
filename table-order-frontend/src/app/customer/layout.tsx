'use client';

import { useEffect, useState } from 'react';
import { authService } from '@/services/auth';
import { wsService } from '@/services/websocket';

export default function CustomerLayout({ children }: { children: React.ReactNode }) {
  const [storeName, setStoreName] = useState('');
  const [tableNumber, setTableNumber] = useState(0);

  useEffect(() => {
    const session = authService.getSession();
    if (session) {
      setStoreName(session.storeName as string || '');
      setTableNumber(session.tableNumber as number || 0);
      const sessionId = session.sessionId as number;
      if (sessionId) wsService.connect(sessionId);
    }
    return () => wsService.disconnect();
  }, []);

  return (
    <div className="mobile-container">
      {/* 상단 네비게이션 바 */}
      <header className="sticky top-0 z-20 bg-white border-b border-coolGray-200 px-4 py-3 flex items-center justify-between"
              style={{ paddingTop: 'calc(var(--safe-area-top) + 12px)' }}>
        <button className="text-t5 text-coolGray-700 flex items-center gap-1" data-testid="staff-call-button">
          🔔 직원호출
        </button>
        <a href="/customer/orders" className="text-t5 text-coolGray-900" data-testid="order-history-link">
          주문내역
        </a>
      </header>

      {/* 매장 정보 헤더 */}
      {storeName && (
        <div className="bg-white px-6 py-4 border-b border-coolGray-200">
          <div className="flex items-center justify-between">
            <h1 className="text-t3 text-coolGray-900">{storeName}</h1>
            <span className="text-t7 text-coolGray-500">{tableNumber}번 테이블</span>
          </div>
          <p className="text-t5 text-coolGray-700 mt-1">
            멤버도 QR 찍고 <span className="font-bold bg-yellow-400 px-1 rounded">함께 주문</span>해요
          </p>
        </div>
      )}

      {children}
    </div>
  );
}

'use client';

import { useEffect, useState } from 'react';
import Link from 'next/link';
import { authService } from '@/services/auth';
import { wsService } from '@/services/websocket';
import StaffCallSheet from '@/components/customer/StaffCallSheet';

export default function CustomerLayout({ children }: { children: React.ReactNode }) {
  const [storeName, setStoreName] = useState('');
  const [tableNumber, setTableNumber] = useState(0);
  const [userCount, setUserCount] = useState(0);
  const [showStaffCall, setShowStaffCall] = useState(false);
  const [isLoggedIn, setIsLoggedIn] = useState(false);

  useEffect(() => {
    setIsLoggedIn(authService.isLoggedIn());
    const session = authService.getSession();
    if (session) {
      setStoreName(session.storeName as string || '');
      setTableNumber(session.tableNumber as number || 0);
      const sessionId = session.sessionId as number;
      if (sessionId) wsService.connect(sessionId);
    }

    const unsub = wsService.onEvent((event) => {
      if (event.type === 'USER_JOINED' || event.type === 'USER_LEFT') {
        setUserCount((event.data as any).activeUserCount || 0);
      }
    });

    return () => { unsub(); wsService.disconnect(); };
  }, []);

  return (
    <div className="mobile-container">
      <header className="sticky top-0 z-20 bg-white border-b border-coolGray-200 px-4 py-3 flex items-center justify-between"
              style={{ paddingTop: 'calc(var(--safe-area-top) + 12px)' }}>
        {isLoggedIn ? (
          <>
            <button onClick={() => setShowStaffCall(true)}
                    className="text-t5 text-coolGray-700 flex items-center gap-1" data-testid="staff-call-button">
              🔔 직원호출
            </button>
            <Link href="/customer/orders" className="text-t5 text-coolGray-900" data-testid="order-history-link">
              주문내역
            </Link>
          </>
        ) : (
          <div />
        )}
      </header>

      {storeName && (
        <div className="bg-white px-6 py-4 border-b border-coolGray-200">
          <div className="flex items-center justify-between">
            <h1 className="text-t3 text-coolGray-900">{storeName}</h1>
            <span className="text-t7 text-coolGray-500">{tableNumber}번 테이블</span>
          </div>
          <p className="text-t5 text-coolGray-700 mt-1">
            {userCount > 1
              ? <span>현재 <span className="font-bold text-blue-300">{userCount}명</span>이 함께 주문 중</span>
              : <span>멤버도 QR 찍고 <span className="font-bold bg-yellow-400 px-1 rounded">함께 주문</span>해요</span>
            }
          </p>
        </div>
      )}

      {children}

      {showStaffCall && <StaffCallSheet onClose={() => setShowStaffCall(false)} />}
    </div>
  );
}

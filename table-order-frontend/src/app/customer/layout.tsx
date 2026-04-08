'use client';

import { useEffect, useState } from 'react';
import Link from 'next/link';
import { authService } from '@/services/auth';
import { wsService } from '@/services/websocket';

export default function CustomerLayout({ children }: { children: React.ReactNode }) {
  const [storeName, setStoreName] = useState('');
  const [tableNumber, setTableNumber] = useState(0);
  const [userCount, setUserCount] = useState(0);

  useEffect(() => {
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

  const [showCallSheet, setShowCallSheet] = useState(false);
  const [callCooldown, setCallCooldown] = useState(false);

  const handleStaffCall = (reason: string) => {
    setShowCallSheet(false);
    setCallCooldown(true);
    setTimeout(() => setCallCooldown(false), 30000);
    // TODO: API 호출 - 직원호출 요청 전송
  };

  return (
    <div className="mobile-container">
      {/* 상단 네비게이션 바 (직원호출 제거, 주문내역만) */}
      <header className="sticky top-0 z-20 bg-white border-b border-coolGray-200 px-4 py-3 flex items-center justify-end"
              style={{ paddingTop: 'calc(var(--safe-area-top) + 12px)' }}>
        <Link href="/customer/orders" className="text-t5 text-coolGray-900" data-testid="order-history-link">
          주문내역
        </Link>
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

      {/* 직원호출 플로팅 버튼 (컨테이너 내부, 화면 하단 고정) */}
      <button
        onClick={() => !callCooldown && setShowCallSheet(true)}
        disabled={callCooldown}
        className={`absolute right-4 bottom-24 z-30 w-14 h-14 rounded-full shadow-lg flex items-center justify-center text-2xl transition-all
          ${callCooldown
            ? 'bg-coolGray-200 text-coolGray-400 cursor-not-allowed'
            : 'bg-yellow-400 text-coolGray-900 hover:bg-yellow-500 active:scale-95'
          }`}
        style={{ position: 'fixed', right: 'max(1rem, calc((100vw - 600px) / 2 + 1rem))' }}
        data-testid="staff-call-floating-button"
      >
        🛎️
      </button>

      {/* 직원호출 바텀시트 */}
      {showCallSheet && (
        <div className="fixed inset-0 z-50">
          {/* 딤드 배경 */}
          <div className="absolute inset-0 bg-black/40" onClick={() => setShowCallSheet(false)} />
          {/* 바텀시트 */}
          <div className="absolute bottom-0 left-0 right-0 bg-white rounded-t-2xl p-6 max-w-[600px] mx-auto animate-slide-up">
            <h3 className="text-t5 font-bold text-coolGray-900 mb-4">무엇을 도와드릴까요?</h3>
            <div className="flex flex-col gap-3">
              <button
                onClick={() => handleStaffCall('물/물티슈 요청')}
                className="w-full py-3 px-4 bg-coolGray-50 rounded-xl text-t6 text-coolGray-900 text-left hover:bg-coolGray-100 active:bg-coolGray-200 transition-colors"
              >
                💧 물/물티슈 요청
              </button>
              <button
                onClick={() => handleStaffCall('수저/접시 요청')}
                className="w-full py-3 px-4 bg-coolGray-50 rounded-xl text-t6 text-coolGray-900 text-left hover:bg-coolGray-100 active:bg-coolGray-200 transition-colors"
              >
                🍴 수저/접시 요청
              </button>
              <button
                onClick={() => handleStaffCall('기타 요청')}
                className="w-full py-3 px-4 bg-coolGray-50 rounded-xl text-t6 text-coolGray-900 text-left hover:bg-coolGray-100 active:bg-coolGray-200 transition-colors"
              >
                💬 기타 요청
              </button>
            </div>
            <button
              onClick={() => setShowCallSheet(false)}
              className="w-full mt-4 py-3 text-t6 text-coolGray-500"
            >
              닫기
            </button>
          </div>
        </div>
      )}
    </div>
  );
}

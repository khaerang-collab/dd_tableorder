'use client';

import { useEffect, useState } from 'react';
import Link from 'next/link';
import { authService } from '@/services/auth';
import { wsService } from '@/services/websocket';
import { api } from '@/services/api';
import Toast from '@/components/shared/Toast';

const CALL_REASONS = [
  { label: '물 / 물티슈', value: '물/물티슈' },
  { label: '수저 / 접시', value: '수저/접시' },
  { label: '기타 요청', value: '기타' },
];

export default function CustomerLayout({ children }: { children: React.ReactNode }) {
  const [storeName, setStoreName] = useState('');
  const [tableNumber, setTableNumber] = useState(0);
  const [userCount, setUserCount] = useState(0);
  const [showCallSheet, setShowCallSheet] = useState(false);
  const [customMessage, setCustomMessage] = useState('');
  const [toast, setToast] = useState('');
  const [cooldown, setCooldown] = useState(false);

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

  const handleCallStaff = async (reason: string) => {
    const session = authService.getSession();
    if (!session?.sessionId) return;

    try {
      const msg = reason === '기타' ? customMessage : '';
      await api.createStaffCall(session.sessionId as number, reason, msg);
      setShowCallSheet(false);
      setCustomMessage('');
      setToast('직원을 호출했어요!');
      setCooldown(true);
      setTimeout(() => setCooldown(false), 30000); // 30초 쿨다운
    } catch {
      setToast('호출에 실패했습니다');
    }
  };

  return (
    <div className="mobile-container">
      <header className="sticky top-0 z-20 bg-white border-b border-coolGray-200 px-4 py-3 flex items-center justify-between"
              style={{ paddingTop: 'calc(var(--safe-area-top) + 12px)' }}>
        <button
          className={`text-t5 flex items-center gap-1 ${cooldown ? 'text-coolGray-300' : 'text-coolGray-700'}`}
          data-testid="staff-call-button"
          disabled={cooldown}
          onClick={() => !cooldown && setShowCallSheet(true)}
        >
          🔔 {cooldown ? '호출완료' : '직원호출'}
        </button>
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

      {/* 직원호출 바텀시트 */}
      {showCallSheet && (
        <div className="fixed inset-0 z-50 bg-black/40 flex items-end justify-center"
             onClick={() => setShowCallSheet(false)}>
          <div className="bg-white rounded-t-2xl w-full max-w-lg p-6 pb-8"
               onClick={(e) => e.stopPropagation()}
               style={{ paddingBottom: 'calc(var(--safe-area-bottom, 0px) + 24px)' }}>
            <h3 className="text-t3 text-coolGray-900 mb-2">무엇을 도와드릴까요?</h3>
            <p className="text-t7 text-coolGray-500 mb-4">{tableNumber}번 테이블</p>

            <div className="space-y-3">
              {CALL_REASONS.map((r) => (
                <button
                  key={r.value}
                  className="w-full py-3 px-4 rounded-xl border border-coolGray-200 text-left text-t5 text-coolGray-700 active:bg-coolGray-50"
                  onClick={() => r.value !== '기타' && handleCallStaff(r.value)}
                >
                  {r.label}
                </button>
              ))}
            </div>

            {/* 기타 요청 입력 */}
            <div className="mt-3">
              <input
                type="text"
                placeholder="기타 요청을 입력해주세요"
                value={customMessage}
                onChange={(e) => setCustomMessage(e.target.value)}
                className="w-full px-4 py-3 rounded-xl border border-coolGray-200 text-t7"
              />
              <button
                className="w-full mt-3 py-3 rounded-xl bg-yellow-400 text-t6 font-bold"
                onClick={() => handleCallStaff('기타')}
                disabled={!customMessage.trim()}
              >
                요청 보내기
              </button>
            </div>
          </div>
        </div>
      )}

      {toast && <Toast message={toast} onClose={() => setToast('')} />}
    </div>
  );
}

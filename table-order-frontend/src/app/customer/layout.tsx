'use client';

import { useEffect, useState, useCallback } from 'react';
import Link from 'next/link';
import { authService } from '@/services/auth';
import { wsService } from '@/services/websocket';
import StaffCallSheet from '@/components/customer/StaffCallSheet';

const EMOJIS = ['😊', '🐶', '🐱', '🦊', '🐻', '🐼', '🐨', '🐯', '🦁', '🐸', '🐵', '🐰', '🐙', '🦄', '🐳'];
const profileEmoji = (profileId: number) => EMOJIS[profileId % EMOJIS.length];

export default function CustomerLayout({ children }: { children: React.ReactNode }) {
  const [storeName, setStoreName] = useState('');
  const [tableNumber, setTableNumber] = useState(0);
  const [userCount, setUserCount] = useState(0);
  const [connectedProfiles, setConnectedProfiles] = useState<number[]>([]);
  const [showStaffCall, setShowStaffCall] = useState(false);
  const [isLoggedIn, setIsLoggedIn] = useState(false);
  const [cartToast, setCartToast] = useState('');
  const [myProfileId, setMyProfileId] = useState<number | null>(null);

  const showCartToast = useCallback((msg: string) => {
    setCartToast(msg);
    setTimeout(() => setCartToast(''), 3000);
  }, []);

  useEffect(() => {
    setIsLoggedIn(authService.isLoggedIn());
    const session = authService.getSession();
    if (session) {
      setStoreName(session.storeName as string || '');
      setTableNumber(session.tableNumber as number || 0);
      const pid = session.profileId as number;
      setMyProfileId(pid || null);
      const sessionId = session.sessionId as number;
      if (sessionId) wsService.connect(sessionId, pid);
    }

    const unsub = wsService.onEvent((event) => {
      const data = event.data as any;

      if (event.type === 'USER_JOINED' || event.type === 'USER_LEFT') {
        setUserCount(data.activeUserCount || 0);
        if (data.connectedProfileIds) setConnectedProfiles(data.connectedProfileIds);
      }

      if (['CART_ITEM_ADDED', 'CART_ITEM_REMOVED', 'CART_ITEM_UPDATED'].includes(event.type)) {
        const pid = data.profileId as number;
        if (pid && pid !== (authService.getSession()?.profileId as number)) {
          const emoji = profileEmoji(pid);
          const menuName = data.menuName || '메뉴';
          const action = event.type === 'CART_ITEM_ADDED' ? '추가했어요'
            : event.type === 'CART_ITEM_REMOVED' ? '삭제했어요'
            : '수량을 변경했어요';
          showCartToast(`${emoji}님이 ${menuName}를 ${action}`);
        }
      }
      if (event.type === 'SESSION_COMPLETED') {
        authService.logout();
        alert('이용이 완료되었습니다. 다시 QR을 스캔해주세요.');
        window.location.href = '/customer/table-login?storeId=1&table=1';
      }
    });

    return () => { unsub(); wsService.disconnect(); };
  }, [showCartToast]);

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

      {storeName && tableNumber > 0 && (
        <div className="bg-white px-6 py-4 border-b border-coolGray-200">
          <div className="flex items-center justify-between">
            <h1 className="text-t3 text-coolGray-900">{storeName}</h1>
            <span className="text-t7 text-coolGray-500">{tableNumber}번 테이블</span>
          </div>
          <div className="flex items-center gap-2 mt-1">
            <p className="text-t5 text-coolGray-700">
              {userCount > 1
                ? <span>현재 <span className="font-bold text-blue-300">{userCount}명</span>이 함께 주문 중</span>
                : <span>멤버도 QR 찍고 <span className="font-bold bg-yellow-400 px-1 rounded">함께 주문</span>해요</span>
              }
            </p>
            {connectedProfiles.length > 0 && (
              <div className="flex items-center -space-x-1">
                {connectedProfiles.map((pid) => (
                  <div key={pid}
                       className={`w-7 h-7 rounded-full flex items-center justify-center text-sm border-2 border-white shadow-sm ${
                         pid === myProfileId ? 'bg-yellow-100 ring-2 ring-yellow-400' : 'bg-coolGray-100'
                       }`}>
                    {profileEmoji(pid)}
                  </div>
                ))}
              </div>
            )}
          </div>
        </div>
      )}

      {children}

      {showStaffCall && <StaffCallSheet onClose={() => setShowStaffCall(false)} />}

      {cartToast && (
        <div className="fixed bottom-24 left-1/2 -translate-x-1/2 z-50 max-w-[90%] animate-bounce-in"
             data-testid="cart-action-toast">
          <div className="bg-white shadow-lg border border-coolGray-200 px-4 py-3 rounded-2xl text-t6 text-coolGray-900 text-center">
            {cartToast}
          </div>
        </div>
      )}
    </div>
  );
}

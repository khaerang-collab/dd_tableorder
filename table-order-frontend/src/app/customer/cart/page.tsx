'use client';

import { useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import { api } from '@/services/api';
import { authService } from '@/services/auth';
import { wsService } from '@/services/websocket';
import Toast from '@/components/shared/Toast';
import { formatPrice } from '@/lib/utils';
import type { Cart, CartItem } from '@/types';

export default function CartPage() {
  const router = useRouter();
  const [cart, setCart] = useState<Cart | null>(null);
  const [toast, setToast] = useState('');
  const [ordering, setOrdering] = useState(false);
  const sessionId = authService.getSessionId();
  const session = authService.getSession();
  const myProfileId = session?.profileId as number | undefined;

  useEffect(() => {
    if (sessionId) api.getCart(sessionId).then(setCart);
  }, [sessionId]);

  useEffect(() => {
    const unsub = wsService.onEvent((event) => {
      if (sessionId && ['CART_ITEM_ADDED', 'CART_ITEM_UPDATED', 'CART_ITEM_REMOVED', 'CART_CLEARED'].includes(event.type)) {
        api.getCart(sessionId).then(setCart);
      }
    });
    return unsub;
  }, [sessionId]);

  const updateQuantity = async (itemId: number, qty: number) => {
    if (!sessionId) return;
    if (qty < 1) { await api.removeCartItem(sessionId, itemId); }
    else { await api.updateCartItem(sessionId, itemId, qty); }
    setCart(await api.getCart(sessionId));
  };

  const removeItem = async (itemId: number) => {
    if (!sessionId) return;
    setCart(await api.removeCartItem(sessionId, itemId));
  };

  const placeOrder = async () => {
    if (!sessionId || ordering) return;
    setOrdering(true);
    try {
      await api.createOrder(sessionId);
      router.push('/customer/order-complete');
    } catch (e: any) {
      setToast(e.message || '주문에 실패했습니다');
      setOrdering(false);
    }
  };

  const items = cart?.items || [];
  const isMyItem = (item: CartItem) => item.customerProfileId === myProfileId;

  return (
    <div className="bg-white min-h-screen pb-32">
      <div className="px-6 py-4 border-b border-coolGray-200">
        <h2 className="text-t3 text-coolGray-900">장바구니</h2>
        <p className="text-t7 text-coolGray-500 mt-1">{items.length}개 메뉴</p>
      </div>

      {items.length === 0 ? (
        <div className="flex flex-col items-center justify-center py-20 text-coolGray-400">
          <p className="text-t5">장바구니가 비어 있어요</p>
          <a href="/customer" className="mt-4 text-blue-300 text-t6">메뉴 보러 가기</a>
        </div>
      ) : (
        <div>
          {items.map((item) => (
            <div key={item.id} className="flex items-center px-6 py-4 border-b border-coolGray-200"
                 data-testid={`cart-item-${item.id}`}>
              {/* 아바타 */}
              <div className="flex-shrink-0 mr-3">
                {item.profileImageUrl ? (
                  <img src={item.profileImageUrl} alt="" className="w-8 h-8 rounded-full" />
                ) : (
                  <div className="w-8 h-8 rounded-full bg-coolGray-100 flex items-center justify-center text-sm">
                    {item.nickname?.charAt(0) || '👤'}
                  </div>
                )}
                {isMyItem(item) && (
                  <span className="block text-center text-[10px] text-blue-300 mt-0.5">나</span>
                )}
              </div>

              {/* 메뉴 정보 */}
              <div className="flex-1">
                <p className="text-t5 text-coolGray-900">{item.menuName}</p>
                <p className="text-t6 text-coolGray-900">{formatPrice(item.menuPrice * item.quantity)}</p>
              </div>

              {/* 수량 조절 */}
              <div className="flex items-center gap-2">
                <button onClick={() => updateQuantity(item.id, item.quantity - 1)}
                        className="w-8 h-8 rounded-full bg-coolGray-100 text-coolGray-700 text-lg"
                        data-testid={`cart-item-decrease-${item.id}`}>-</button>
                <span className="text-t6 w-6 text-center">{item.quantity}</span>
                <button onClick={() => updateQuantity(item.id, item.quantity + 1)}
                        className="w-8 h-8 rounded-full bg-coolGray-100 text-coolGray-700 text-lg"
                        data-testid={`cart-item-increase-${item.id}`}>+</button>
                <button onClick={() => removeItem(item.id)}
                        className="ml-2 text-red-300 text-t7"
                        data-testid={`cart-item-remove-${item.id}`}>삭제</button>
              </div>
            </div>
          ))}
        </div>
      )}

      {/* 하단 주문 바 */}
      {items.length > 0 && (
        <div className="fixed bottom-0 left-1/2 -translate-x-1/2 w-full max-w-mobile z-30 bg-white border-t border-coolGray-200 p-4"
             style={{ paddingBottom: 'calc(var(--safe-area-bottom) + 16px)' }}>
          <div className="flex justify-between mb-3 px-2">
            <span className="text-t5 text-coolGray-700">총 금액</span>
            <span className="text-t3 text-coolGray-900">{formatPrice(cart?.totalAmount || 0)}</span>
          </div>
          <button onClick={placeOrder} disabled={ordering}
                  className="w-full py-4 rounded-2xl bg-yellow-400 text-coolGray-900 text-t6 font-bold disabled:opacity-50"
                  data-testid="place-order-button">
            {ordering ? '주문 중...' : '주문하기'}
          </button>
        </div>
      )}

      {toast && <Toast message={toast} onClose={() => setToast('')} />}
    </div>
  );
}

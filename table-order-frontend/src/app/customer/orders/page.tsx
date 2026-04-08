'use client';

import { useEffect, useState } from 'react';
import Link from 'next/link';
import { api } from '@/services/api';
import { authService } from '@/services/auth';
import { formatPrice, formatDateTime } from '@/lib/utils';
import { ORDER_STATUS_LABEL, ORDER_STATUS_COLOR } from '@/lib/constants';
import type { Order } from '@/types';

export default function OrderHistoryPage() {
  const [orders, setOrders] = useState<Order[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const sessionId = authService.getSessionId();
    if (sessionId) {
      api.getOrders(sessionId).then((data) => {
        setOrders(data);
        setLoading(false);
      }).catch(() => setLoading(false));
    } else {
      setLoading(false);
    }
  }, []);

  return (
    <div className="bg-white min-h-screen pb-24">
      <div className="px-6 py-4 border-b border-coolGray-200">
        <h2 className="text-t3 text-coolGray-900">주문 내역</h2>
        <p className="text-t7 text-coolGray-500 mt-1">총 {orders.length}건</p>
      </div>

      {loading ? (
        <div className="flex justify-center py-20 text-coolGray-400">
          <p className="text-t5">로딩 중...</p>
        </div>
      ) : orders.length === 0 ? (
        <div className="flex flex-col items-center justify-center py-20 text-coolGray-400">
          <p className="text-t5">주문 내역이 없어요</p>
        </div>
      ) : (
        orders.map((order) => (
          <div key={order.id} className="px-6 py-4 border-b border-coolGray-200" data-testid={`order-${order.id}`}>
            <div className="flex items-center justify-between">
              <span className="text-t7 text-coolGray-500">{formatDateTime(order.createdAt)}</span>
              <span className={`px-2 py-0.5 rounded-full text-[11px] font-bold ${ORDER_STATUS_COLOR[order.status] || 'bg-coolGray-100 text-coolGray-700'}`}>
                {ORDER_STATUS_LABEL[order.status] || order.status}
              </span>
            </div>
            <p className="text-t7 text-coolGray-400 mt-1">#{order.orderNumber}</p>
            {order.items && order.items.length > 0 ? (
              <div className="mt-2 space-y-1">
                {order.items.map((item, idx) => (
                  <div key={item.id || idx} className="flex justify-between text-t7">
                    <span className="text-coolGray-700">
                      {item.menuName} x {item.quantity}
                      {item.customerNickname && (
                        <span className="ml-1 text-blue-300 text-[11px]">({item.customerNickname})</span>
                      )}
                    </span>
                    <span className="text-coolGray-900">{formatPrice(item.unitPrice * item.quantity)}</span>
                  </div>
                ))}
              </div>
            ) : (
              <p className="mt-2 text-t7 text-coolGray-400">메뉴 상세 정보 없음</p>
            )}
            <div className="flex justify-end mt-2 pt-2 border-t border-coolGray-100">
              <span className="text-t6 text-coolGray-900">{formatPrice(order.totalAmount)}</span>
            </div>
          </div>
        ))
      )}

      <div className="fixed bottom-0 left-1/2 -translate-x-1/2 w-full max-w-mobile z-30 p-4 bg-white border-t border-coolGray-200"
           style={{ paddingBottom: 'calc(var(--safe-area-bottom) + 16px)' }}>
        <Link href="/customer"
              className="block w-full py-4 rounded-2xl bg-yellow-400 text-coolGray-900 text-t6 font-bold text-center"
              data-testid="back-to-menu-button">
          추가 주문하기
        </Link>
      </div>
    </div>
  );
}

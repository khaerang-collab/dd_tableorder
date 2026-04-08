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
  const sessionId = authService.getSessionId();

  useEffect(() => {
    if (sessionId) api.getOrders(sessionId).then(setOrders);
  }, [sessionId]);

  return (
    <div className="bg-white min-h-screen pb-24">
      <div className="px-6 py-4 border-b border-coolGray-200">
        <h2 className="text-t3 text-coolGray-900">주문 내역</h2>
        <p className="text-t7 text-coolGray-500 mt-1">총 {orders.length}건</p>
      </div>

      {orders.length === 0 ? (
        <div className="flex flex-col items-center justify-center py-20 text-coolGray-400">
          <p className="text-t5">주문 내역이 없어요</p>
        </div>
      ) : (
        orders.map((order) => (
          <div key={order.id} className="px-6 py-4 border-b border-coolGray-200" data-testid={`order-${order.id}`}>
            <div className="flex items-center justify-between">
              <span className="text-t7 text-coolGray-500">{formatDateTime(order.createdAt)}</span>
              <span className={`px-2 py-0.5 rounded-full text-[11px] font-bold ${ORDER_STATUS_COLOR[order.status]}`}>
                {ORDER_STATUS_LABEL[order.status]}
              </span>
            </div>
            <p className="text-t7 text-coolGray-400 mt-1">#{order.orderNumber}</p>
            <div className="mt-2 space-y-1">
              {order.items.map((item) => (
                <div key={item.id} className="flex justify-between text-t7">
                  <span className="text-coolGray-700">{item.menuName} × {item.quantity}</span>
                  <span className="text-coolGray-900">{formatPrice(item.unitPrice * item.quantity)}</span>
                </div>
              ))}
            </div>
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

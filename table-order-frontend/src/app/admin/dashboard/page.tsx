'use client';

import { useEffect, useState, useCallback } from 'react';
import { api } from '@/services/api';
import { authService } from '@/services/auth';
import { sseService } from '@/services/sse';
import { formatPrice, formatDateTime } from '@/lib/utils';
import { ORDER_STATUS_LABEL, ORDER_STATUS_COLOR } from '@/lib/constants';
import ConfirmDialog from '@/components/shared/ConfirmDialog';
import Toast from '@/components/shared/Toast';
import type { Dashboard, Order, TableOrderSummary } from '@/types';

export default function DashboardPage() {
  const [dashboard, setDashboard] = useState<Dashboard | null>(null);
  const [selectedTable, setSelectedTable] = useState<TableOrderSummary | null>(null);
  const [confirmAction, setConfirmAction] = useState<{ title: string; message: string; action: () => void } | null>(null);
  const [toast, setToast] = useState('');
  const storeId = authService.getStoreId();

  const loadDashboard = useCallback(() => {
    if (storeId) api.getDashboard(storeId).then(setDashboard);
  }, [storeId]);

  useEffect(() => {
    loadDashboard();
    if (storeId) {
      sseService.connect(storeId);
      const unsub = sseService.onEvent(() => loadDashboard());
      return () => { unsub(); sseService.disconnect(); };
    }
  }, [storeId, loadDashboard]);

  const updateStatus = async (orderId: number, status: string) => {
    try {
      await api.updateOrderStatus(orderId, status);
      loadDashboard();
      setToast('주문 상태가 변경되었습니다');
    } catch (e: any) { setToast(e.message); }
  };

  const deleteOrder = (orderId: number) => {
    setConfirmAction({
      title: '주문 삭제', message: '이 주문을 삭제하시겠습니까?',
      action: async () => {
        await api.deleteOrder(orderId);
        loadDashboard(); setConfirmAction(null);
        setToast('주문이 삭제되었습니다');
      },
    });
  };

  const completeTable = (tableId: number, tableNumber: number) => {
    setConfirmAction({
      title: '이용 완료', message: `${tableNumber}번 테이블 이용을 완료하시겠습니까?`,
      action: async () => {
        if (storeId) await api.completeTable(storeId, tableId);
        loadDashboard(); setConfirmAction(null); setSelectedTable(null);
        setToast('이용 완료 처리되었습니다');
      },
    });
  };

  const nextStatus = (status: string) => status === 'PENDING' ? 'PREPARING' : status === 'PREPARING' ? 'COMPLETED' : null;

  return (
    <div>
      <h2 className="text-t5 font-bold text-coolGray-900 mb-4">주문 대시보드</h2>

      <div className="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-3">
        {dashboard?.tables.map((table) => (
          <div key={table.tableId} onClick={() => setSelectedTable(table)}
               className={`bg-white rounded-xl p-4 shadow-depth1 cursor-pointer border-2 transition ${
                 table.orderCount > 0 && table.recentOrders.some(o => o.status === 'PENDING')
                   ? 'border-yellow-400 animate-pulse' : 'border-transparent'}`}
               data-testid={`dashboard-table-${table.tableNumber}`}>
            <div className="flex justify-between items-center mb-2">
              <span className="text-t6 font-bold">{table.tableNumber}번</span>
              {table.sessionId && <span className="w-2 h-2 rounded-full bg-green-300" />}
            </div>
            <p className="text-t3 text-coolGray-900">{formatPrice(table.totalAmount)}</p>
            <p className="text-t7 text-coolGray-500">{table.orderCount}건</p>
          </div>
        ))}
      </div>

      {/* 테이블 상세 모달 */}
      {selectedTable && (
        <div className="fixed inset-0 z-40 bg-black/40 flex items-end md:items-center justify-center"
             onClick={() => setSelectedTable(null)}>
          <div className="bg-white rounded-t-2xl md:rounded-2xl w-full max-w-lg max-h-[80vh] overflow-y-auto p-6"
               onClick={(e) => e.stopPropagation()} data-testid="table-detail-modal">
            <div className="flex justify-between items-center mb-4">
              <h3 className="text-t3">{selectedTable.tableNumber}번 테이블</h3>
              <button onClick={() => setSelectedTable(null)} className="text-coolGray-400 text-xl">✕</button>
            </div>

            {selectedTable.recentOrders.length === 0 ? (
              <p className="text-coolGray-400 text-center py-8">주문이 없습니다</p>
            ) : (
              selectedTable.recentOrders.map((order) => (
                <div key={order.id} className="border border-coolGray-200 rounded-xl p-4 mb-3"
                     data-testid={`modal-order-${order.id}`}>
                  <div className="flex justify-between items-center">
                    <span className="text-t7 text-coolGray-500">#{order.orderNumber}</span>
                    <span className={`px-2 py-0.5 rounded-full text-[11px] font-bold ${ORDER_STATUS_COLOR[order.status]}`}>
                      {ORDER_STATUS_LABEL[order.status]}
                    </span>
                  </div>
                  <div className="mt-2 space-y-1">
                    {order.items.map((item) => (
                      <p key={item.id} className="text-t7 text-coolGray-700">{item.menuName} × {item.quantity}</p>
                    ))}
                  </div>
                  <div className="flex justify-between items-center mt-3 pt-2 border-t border-coolGray-100">
                    <span className="text-t6">{formatPrice(order.totalAmount)}</span>
                    <div className="flex gap-2">
                      {nextStatus(order.status) && (
                        <button onClick={() => updateStatus(order.id, nextStatus(order.status)!)}
                                className="px-3 py-1.5 rounded-lg bg-blue-300 text-white text-t7"
                                data-testid={`order-next-status-${order.id}`}>
                          {ORDER_STATUS_LABEL[nextStatus(order.status)!]}
                        </button>
                      )}
                      <button onClick={() => deleteOrder(order.id)}
                              className="px-3 py-1.5 rounded-lg bg-red-10 text-red-300 text-t7"
                              data-testid={`order-delete-${order.id}`}>
                        삭제
                      </button>
                    </div>
                  </div>
                </div>
              ))
            )}

            {selectedTable.sessionId && (
              <button onClick={() => completeTable(selectedTable.tableId, selectedTable.tableNumber)}
                      className="w-full mt-4 py-3 rounded-xl bg-coolGray-100 text-coolGray-700 text-t6"
                      data-testid="complete-table-button">
                이용 완료
              </button>
            )}
          </div>
        </div>
      )}

      {confirmAction && (
        <ConfirmDialog title={confirmAction.title} message={confirmAction.message}
                       onConfirm={confirmAction.action} onCancel={() => setConfirmAction(null)} />
      )}
      {toast && <Toast message={toast} onClose={() => setToast('')} />}
    </div>
  );
}

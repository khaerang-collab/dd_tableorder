'use client';

import { useEffect, useState } from 'react';
import { api } from '@/services/api';
import { authService } from '@/services/auth';
import Toast from '@/components/shared/Toast';
import type { RestaurantTable } from '@/types';

export default function TableManagePage() {
  const [tables, setTables] = useState<RestaurantTable[]>([]);
  const [newNumber, setNewNumber] = useState('');
  const [toast, setToast] = useState('');
  const storeId = authService.getStoreId();

  const loadTables = () => { if (storeId) api.getTables(storeId).then(setTables); };
  useEffect(loadTables, [storeId]);

  const createTable = async () => {
    if (!storeId || !newNumber) return;
    try {
      await api.createTable(storeId, Number(newNumber), window.location.origin);
      setNewNumber('');
      loadTables();
      setToast('테이블이 생성되었습니다');
    } catch (e: any) { setToast(e.message); }
  };

  return (
    <div>
      <h2 className="text-t5 font-bold text-coolGray-900 mb-4">테이블 관리</h2>

      <div className="bg-white rounded-xl p-4 shadow-depth1 mb-4 flex gap-2">
        <input type="number" placeholder="테이블 번호" value={newNumber}
               onChange={(e) => setNewNumber(e.target.value)} min="1"
               className="flex-1 px-3 py-2 rounded-lg border border-coolGray-200 text-t5"
               data-testid="new-table-number" />
        <button onClick={createTable}
                className="px-4 py-2 rounded-lg bg-blue-300 text-white text-t6"
                data-testid="create-table-button">추가</button>
      </div>

      <div className="grid grid-cols-2 md:grid-cols-3 gap-3">
        {tables.map((table) => (
          <div key={table.id} className="bg-white rounded-xl p-4 shadow-depth1"
               data-testid={`manage-table-${table.tableNumber}`}>
            <p className="text-t5 font-bold">{table.tableNumber}번 테이블</p>
            <p className="text-t7 text-coolGray-500 mt-1 break-all">{table.qrCodeUrl}</p>
          </div>
        ))}
      </div>

      {toast && <Toast message={toast} onClose={() => setToast('')} />}
    </div>
  );
}

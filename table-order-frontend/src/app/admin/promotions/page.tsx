'use client';

import { useEffect, useState } from 'react';
import { authService } from '@/services/auth';
import { formatPrice } from '@/lib/utils';
import ConfirmDialog from '@/components/shared/ConfirmDialog';
import Toast from '@/components/shared/Toast';

interface Promotion { id: number; minOrderAmount: number; rewardDescription: string; isActive: boolean; }

export default function PromotionManagePage() {
  const [promotions, setPromotions] = useState<Promotion[]>([]);
  const [showForm, setShowForm] = useState(false);
  const [form, setForm] = useState({ minOrderAmount: '', rewardDescription: '' });
  const [confirmAction, setConfirmAction] = useState<{ title: string; message: string; action: () => void } | null>(null);
  const [toast, setToast] = useState('');
  const [storeId, setStoreId] = useState<number | null>(null);

  useEffect(() => { setStoreId(authService.getStoreId()); }, []);

  const token = () => authService.getToken();
  const headers = () => ({ 'Content-Type': 'application/json', Authorization: 'Bearer ' + token() });

  const loadPromotions = () => {
    if (!storeId) return;
    fetch(`/api/admin/stores/${storeId}/promotions`, { headers: headers() })
      .then(r => r.json()).then(setPromotions).catch(() => {});
  };
  useEffect(loadPromotions, [storeId]);

  const createPromotion = async () => {
    if (!storeId || !form.minOrderAmount || !form.rewardDescription) return;
    try {
      await fetch(`/api/admin/stores/${storeId}/promotions`, {
        method: 'POST', headers: headers(),
        body: JSON.stringify({ minOrderAmount: Number(form.minOrderAmount), rewardDescription: form.rewardDescription }),
      });
      setShowForm(false); setForm({ minOrderAmount: '', rewardDescription: '' });
      loadPromotions(); setToast('프로모션이 추가되었습니다');
    } catch (e: any) { setToast(e.message || '추가 실패'); }
  };

  const togglePromotion = async (id: number) => {
    await fetch(`/api/admin/promotions/${id}/toggle?storeId=${storeId}`, { method: 'POST', headers: headers() });
    loadPromotions();
  };

  const deletePromotion = (id: number, desc: string) => {
    setConfirmAction({
      title: '프로모션 삭제', message: `"${desc}" 프로모션을 삭제하시겠습니까?`,
      action: async () => {
        await fetch(`/api/admin/promotions/${id}?storeId=${storeId}`, { method: 'DELETE', headers: headers() });
        loadPromotions(); setConfirmAction(null); setToast('삭제되었습니다');
      },
    });
  };

  return (
    <div>
      <h2 className="text-t5 font-bold text-coolGray-900 mb-4">프로모션 관리</h2>

      <button onClick={() => setShowForm(true)}
              className="w-full mb-4 py-3 rounded-xl bg-yellow-400 text-coolGray-900 text-t6 font-bold"
              data-testid="add-promotion-button">
        + 프로모션 추가
      </button>

      {showForm && (
        <div className="bg-white rounded-xl p-4 shadow-depth1 mb-4 space-y-3">
          <input type="number" placeholder="목표 금액 (원)" value={form.minOrderAmount}
                 onChange={(e) => setForm({ ...form, minOrderAmount: e.target.value })}
                 className="w-full px-3 py-2 rounded-lg border border-coolGray-200 text-t7"
                 data-testid="promotion-amount-input" />
          <input placeholder="혜택 설명 (예: 마카롱 1개 서비스)" value={form.rewardDescription}
                 onChange={(e) => setForm({ ...form, rewardDescription: e.target.value })}
                 className="w-full px-3 py-2 rounded-lg border border-coolGray-200 text-t7"
                 data-testid="promotion-reward-input" />
          <div className="flex gap-2">
            <button onClick={() => setShowForm(false)} className="flex-1 py-2 rounded-lg bg-coolGray-100 text-t7">취소</button>
            <button onClick={createPromotion} className="flex-1 py-2 rounded-lg bg-blue-300 text-white text-t7"
                    data-testid="promotion-submit">추가</button>
          </div>
        </div>
      )}

      {promotions.length === 0 ? (
        <p className="text-coolGray-400 text-center py-8 text-t5">등록된 프로모션이 없습니다</p>
      ) : (
        promotions.map((p) => (
          <div key={p.id} className="bg-white rounded-xl p-4 shadow-depth1 mb-3 flex items-center justify-between"
               data-testid={`promotion-${p.id}`}>
            <div>
              <p className="text-t6 text-coolGray-900">{formatPrice(p.minOrderAmount)} 이상 주문 시</p>
              <p className="text-t5 font-bold text-coolGray-900">{p.rewardDescription}</p>
            </div>
            <div className="flex items-center gap-2">
              <button onClick={() => togglePromotion(p.id)}
                      className={`px-3 py-1.5 rounded-lg text-t7 ${p.isActive ? 'bg-green-50 text-green-300' : 'bg-coolGray-100 text-coolGray-400'}`}
                      data-testid={`promotion-toggle-${p.id}`}>
                {p.isActive ? '활성' : '비활성'}
              </button>
              <button onClick={() => deletePromotion(p.id, p.rewardDescription)}
                      className="px-3 py-1.5 rounded-lg bg-red-10 text-red-300 text-t7"
                      data-testid={`promotion-delete-${p.id}`}>삭제</button>
            </div>
          </div>
        ))
      )}

      {confirmAction && (
        <ConfirmDialog title={confirmAction.title} message={confirmAction.message}
                       onConfirm={confirmAction.action} onCancel={() => setConfirmAction(null)} />
      )}
      {toast && <Toast message={toast} onClose={() => setToast('')} />}
    </div>
  );
}

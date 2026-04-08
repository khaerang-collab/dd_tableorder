'use client';

import { useState } from 'react';
import { authService } from '@/services/auth';
import Toast from '@/components/shared/Toast';

interface Props { onClose: () => void; }

const REASONS = [
  { value: 'WATER', label: '물/물티슈 요청' },
  { value: 'UTENSIL', label: '수저/접시 요청' },
  { value: 'OTHER', label: '기타 요청' },
];

export default function StaffCallSheet({ onClose }: Props) {
  const [selected, setSelected] = useState('');
  const [customMsg, setCustomMsg] = useState('');
  const [toast, setToast] = useState('');
  const [sending, setSending] = useState(false);

  const handleSend = async () => {
    if (!selected || sending) return;
    setSending(true);
    const session = authService.getSession();
    try {
      await fetch('/api/customer/stores/' + session?.storeId + '/staff-call', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json', Authorization: 'Bearer ' + authService.getToken() },
        body: JSON.stringify({
          tableId: session?.tableId,
          tableNumber: session?.tableNumber,
          reason: selected,
          customMessage: selected === 'OTHER' ? customMsg : null,
        }),
      });
      setToast('호출했어요!');
      setTimeout(onClose, 1500);
    } catch (e: any) {
      setToast(e.message || '호출에 실패했습니다');
      setSending(false);
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-end justify-center bg-black/40" onClick={onClose}>
      <div className="bg-white rounded-t-2xl w-full max-w-mobile p-6 pb-8" onClick={(e) => e.stopPropagation()}
           data-testid="staff-call-sheet">
        <h3 className="text-t5 font-bold text-coolGray-900 mb-4">무엇을 도와드릴까요?</h3>
        <div className="space-y-2 mb-4">
          {REASONS.map((r) => (
            <button key={r.value} onClick={() => setSelected(r.value)}
                    className={`w-full py-3 px-4 rounded-xl text-t5 text-left ${
                      selected === r.value ? 'bg-yellow-400 text-coolGray-900 font-bold' : 'bg-coolGray-50 text-coolGray-700'}`}
                    data-testid={`staff-call-reason-${r.value}`}>
              {r.label}
            </button>
          ))}
        </div>
        {selected === 'OTHER' && (
          <input placeholder="요청 사항을 입력해주세요" value={customMsg}
                 onChange={(e) => setCustomMsg(e.target.value)}
                 className="w-full px-4 py-3 rounded-xl border border-coolGray-200 text-t7 mb-4"
                 data-testid="staff-call-custom-input" />
        )}
        <button onClick={handleSend} disabled={!selected || sending}
                className="w-full py-4 rounded-2xl bg-yellow-400 text-coolGray-900 text-t6 font-bold disabled:opacity-50"
                data-testid="staff-call-submit">
          {sending ? '호출 중...' : '직원 호출하기'}
        </button>
      </div>
      {toast && <Toast message={toast} onClose={() => setToast('')} />}
    </div>
  );
}

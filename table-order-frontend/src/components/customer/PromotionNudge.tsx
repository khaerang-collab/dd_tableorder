'use client';

import { useEffect, useState } from 'react';
import { authService } from '@/services/auth';
import { formatPrice } from '@/lib/utils';

interface Nudge {
  minOrderAmount: number;
  rewardDescription: string;
  currentAmount: number;
  remainingAmount: number;
  achieved: boolean;
}

interface Props { cartAmount: number; }

export default function PromotionNudge({ cartAmount }: Props) {
  const [nudges, setNudges] = useState<Nudge[]>([]);
  const [storeId, setStoreId] = useState<number | null>(null);

  useEffect(() => { setStoreId(authService.getStoreId()); }, []);

  useEffect(() => {
    if (!storeId) { return; }
    fetch(`/api/stores/${storeId}/promotions/nudge?cartAmount=${cartAmount}`)
      .then((r) => r.json()).then(setNudges).catch(() => {});
  }, [storeId, cartAmount]);

  if (nudges.length === 0) return null;

  // 가장 가까운 미달성 프로모션 표시
  const nextNudge = nudges.find((n) => !n.achieved);
  const achievedNudge = nudges.filter((n) => n.achieved).pop();

  return (
    <div className="mx-4 mb-2">
      {achievedNudge && (
        <div className="bg-green-50 rounded-xl px-4 py-3 mb-2 text-center" data-testid="promotion-achieved">
          <p className="text-t6 text-green-300 font-bold">🎉 {achievedNudge.rewardDescription}</p>
        </div>
      )}
      {nextNudge && (
        <div className="bg-yellow-50 rounded-xl px-4 py-3" data-testid="promotion-nudge">
          <p className="text-t7 text-coolGray-700">
            <span className="font-bold text-yellow-700">{formatPrice(nextNudge.remainingAmount)}</span>만 더 주문하면{' '}
            <span className="font-bold">{nextNudge.rewardDescription}</span>!
          </p>
          <div className="mt-2 bg-coolGray-200 rounded-full h-2 overflow-hidden">
            <div className="bg-yellow-400 h-full rounded-full transition-all"
                 style={{ width: `${Math.min(100, (nextNudge.currentAmount / nextNudge.minOrderAmount) * 100)}%` }} />
          </div>
        </div>
      )}
    </div>
  );
}

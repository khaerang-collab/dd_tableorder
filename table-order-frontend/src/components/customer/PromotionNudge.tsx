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

interface Props { cartAmount: number; hasCartItems?: boolean; }

export default function PromotionNudge({ cartAmount, hasCartItems = false }: Props) {
  const [nudges, setNudges] = useState<Nudge[]>([]);
  const [storeId, setStoreId] = useState<number | null>(null);
  const [showAchieved, setShowAchieved] = useState(false);
  const [achievedReward, setAchievedReward] = useState('');

  useEffect(() => { setStoreId(authService.getStoreId()); }, []);

  useEffect(() => {
    if (!storeId) { return; }
    const baseUrl = `${window.location.protocol}//${window.location.hostname}:8080`;
    fetch(`${baseUrl}/api/stores/${storeId}/promotions/nudge?cartAmount=${cartAmount}`)
      .then((r) => r.json()).then((data: Nudge[]) => {
        const newAchieved = data.filter((n) => n.achieved);
        const prevAchieved = nudges.filter((n) => n.achieved);
        // 새로 달성된 프로모션이 있으면 축하 토스트 표시
        if (newAchieved.length > prevAchieved.length && newAchieved.length > 0) {
          const latest = newAchieved[newAchieved.length - 1];
          setAchievedReward(latest.rewardDescription);
          setShowAchieved(true);
        }
        setNudges(data);
      }).catch(() => {});
  }, [storeId, cartAmount]);

  // 축하 토스트 5초 후 자동 사라짐
  useEffect(() => {
    if (!showAchieved) return;
    const timer = setTimeout(() => setShowAchieved(false), 5000);
    return () => clearTimeout(timer);
  }, [showAchieved]);

  if (nudges.length === 0) return null;

  const nextNudge = nudges.find((n) => !n.achieved);

  return (
    <div className="fixed left-1/2 -translate-x-1/2 w-full max-w-mobile z-30 px-4 transition-all"
         style={{ bottom: hasCartItems ? 'calc(var(--safe-area-bottom) + 84px)' : 'calc(var(--safe-area-bottom) + 16px)' }}>
      {showAchieved && (
        <div className="bg-green-50 rounded-xl px-4 py-3 mb-2 text-center shadow-depth1 animate-fade-in-out" data-testid="promotion-achieved">
          <p className="text-t6 text-green-300 font-bold">🎉 {achievedReward}</p>
        </div>
      )}
      {nextNudge && (
        <div className="bg-yellow-50 rounded-xl px-4 py-3 shadow-depth1" data-testid="promotion-nudge">
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

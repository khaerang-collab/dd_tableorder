'use client';

import { useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';

export default function OrderCompletePage() {
  const router = useRouter();
  const [countdown, setCountdown] = useState(5);

  useEffect(() => {
    const timer = setInterval(() => {
      setCountdown((c) => {
        if (c <= 1) { clearInterval(timer); router.push('/customer'); return 0; }
        return c - 1;
      });
    }, 1000);
    return () => clearInterval(timer);
  }, [router]);

  return (
    <div className="bg-white min-h-screen flex flex-col items-center justify-center px-6">
      <div className="text-6xl mb-6">✅</div>
      <h1 className="text-t3 text-coolGray-900">주문 완료!</h1>
      <p className="text-t5 text-coolGray-500 mt-2">{countdown}초 후 메뉴판으로 이동합니다</p>
      <a href="/customer"
         className="mt-8 px-8 py-4 rounded-2xl bg-yellow-400 text-coolGray-900 text-t6 font-bold"
         data-testid="back-to-menu-button">
        메뉴판으로
      </a>
    </div>
  );
}

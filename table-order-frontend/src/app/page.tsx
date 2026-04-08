'use client';

import { useEffect } from 'react';
import { useRouter, useSearchParams } from 'next/navigation';

export default function RootPage() {
  const router = useRouter();
  const searchParams = useSearchParams();

  useEffect(() => {
    const storeId = searchParams.get('storeId');
    const table = searchParams.get('table');
    if (storeId && table) {
      // QR 코드 접속 → 카카오 로그인 페이지로 이동
      router.replace(`/customer/table-login?storeId=${storeId}&table=${table}`);
    } else {
      router.replace('/customer');
    }
  }, [router, searchParams]);

  return (
    <div className="flex items-center justify-center min-h-screen">
      <p className="text-coolGray-500">로딩 중...</p>
    </div>
  );
}

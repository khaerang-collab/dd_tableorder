'use client';

import { useSearchParams, useRouter } from 'next/navigation';
import { Suspense, useState } from 'react';
import { api } from '@/services/api';
import { authService } from '@/services/auth';
import { KAKAO_JS_KEY } from '@/lib/constants';

function TableLoginContent() {
  const searchParams = useSearchParams();
  const router = useRouter();
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const storeId = Number(searchParams.get('storeId')) || 1;
  const tableNumber = Number(searchParams.get('table')) || 1;

  const handleKakaoLogin = async () => {
    setLoading(true);
    setError('');
    try {
      const kakaoAuthUrl = `https://kauth.kakao.com/oauth/authorize?client_id=${KAKAO_JS_KEY}&redirect_uri=${encodeURIComponent(window.location.origin + '/customer/auth/kakao/callback')}&response_type=code&state=${storeId}_${tableNumber}`;
      window.location.href = kakaoAuthUrl;
    } catch (e) {
      setError('카카오 로그인에 실패했습니다');
      setLoading(false);
    }
  };

  const handleDevAccess = async () => {
    setLoading(true);
    setError('');
    try {
      const res = await api.kakaoLogin(storeId, tableNumber, 'dev-test-token');
      authService.saveCustomerSession(res);
      window.location.href = '/customer';
    } catch (e: any) {
      console.error('Dev login error:', e);
      setError(`오류: ${e.message} (storeId=${storeId}, table=${tableNumber})`);
      setLoading(false);
    }
  };

  return (
    <div className="mobile-container min-h-screen flex flex-col items-center justify-center px-6 bg-white">
      <div className="text-center mb-10">
        <h1 className="text-t3 text-coolGray-900 mb-2">테이블오더</h1>
        <p className="text-t5 text-coolGray-500">
          {storeId > 0 ? `${tableNumber}번 테이블` : '환영합니다'}
        </p>
      </div>

      <button onClick={handleKakaoLogin} disabled={loading}
              className="w-full max-w-xs py-4 rounded-2xl bg-[#FEE500] text-[#191919] text-t6 font-bold flex items-center justify-center gap-2 disabled:opacity-50"
              data-testid="kakao-login-button">
        💬 카카오로 시작하기
      </button>

      {error && <p className="text-red-300 text-t7 mt-4">{error}</p>}

      <button onClick={handleDevAccess} disabled={loading}
              className="mt-4 text-coolGray-400 text-t7 underline"
              data-testid="dev-access-button">
        (개발용) 카카오 없이 접속
      </button>
    </div>
  );
}

export default function TableLoginPage() {
  return (
    <Suspense fallback={<div className="mobile-container min-h-screen flex items-center justify-center bg-white"><p>로딩 중...</p></div>}>
      <TableLoginContent />
    </Suspense>
  );
}

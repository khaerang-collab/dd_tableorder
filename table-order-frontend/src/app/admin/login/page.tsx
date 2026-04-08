'use client';

import { useState } from 'react';
import { useRouter } from 'next/navigation';
import { api } from '@/services/api';
import { authService } from '@/services/auth';

export default function AdminLoginPage() {
  const router = useRouter();
  const [storeId, setStoreId] = useState('');
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const handleLogin = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(''); setLoading(true);
    try {
      const res = await api.adminLogin(Number(storeId), username, password);
      authService.saveAdminSession(res);
      router.push('/admin/dashboard');
    } catch (err: any) {
      setError(err.message || '로그인에 실패했습니다');
    } finally { setLoading(false); }
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-coolGray-50 px-4">
      <div className="bg-white rounded-2xl shadow-depth1 p-8 w-full max-w-sm">
        <h1 className="text-t3 text-coolGray-900 text-center mb-6">관리자 로그인</h1>
        <form onSubmit={handleLogin} className="space-y-4">
          <input type="number" placeholder="매장 ID" value={storeId} onChange={(e) => setStoreId(e.target.value)}
                 className="w-full px-4 py-3 rounded-xl border border-coolGray-200 text-t5 focus:outline-none focus:border-blue-300"
                 required data-testid="admin-login-store-id" />
          <input type="text" placeholder="사용자명" value={username} onChange={(e) => setUsername(e.target.value)}
                 className="w-full px-4 py-3 rounded-xl border border-coolGray-200 text-t5 focus:outline-none focus:border-blue-300"
                 required data-testid="admin-login-username" />
          <input type="password" placeholder="비밀번호" value={password} onChange={(e) => setPassword(e.target.value)}
                 className="w-full px-4 py-3 rounded-xl border border-coolGray-200 text-t5 focus:outline-none focus:border-blue-300"
                 required data-testid="admin-login-password" />
          {error && <p className="text-red-300 text-t7">{error}</p>}
          <button type="submit" disabled={loading}
                  className="w-full py-4 rounded-xl bg-blue-300 text-white text-t6 font-bold disabled:opacity-50"
                  data-testid="admin-login-submit">
            {loading ? '로그인 중...' : '로그인'}
          </button>
        </form>
      </div>
    </div>
  );
}

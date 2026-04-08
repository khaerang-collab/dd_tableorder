import type { KakaoLoginResponse, AdminLoginResponse } from '@/types';

const TOKEN_KEY = 'token';
const SESSION_KEY = 'session';

function isBrowser(): boolean {
  return typeof window !== 'undefined';
}

export const authService = {
  saveCustomerSession(data: KakaoLoginResponse) {
    if (!isBrowser()) return;
    localStorage.setItem(TOKEN_KEY, data.token);
    localStorage.setItem(SESSION_KEY, JSON.stringify({
      storeId: data.storeId, storeName: data.storeName,
      tableNumber: data.tableNumber, tableId: data.tableId,
      sessionId: data.sessionId, profileId: data.profileId,
      nickname: data.nickname, profileImageUrl: data.profileImageUrl,
    }));
  },

  saveAdminSession(data: AdminLoginResponse) {
    if (!isBrowser()) return;
    localStorage.setItem(TOKEN_KEY, data.token);
    localStorage.setItem(SESSION_KEY, JSON.stringify({
      storeId: data.storeId, storeName: data.storeName,
      adminUsername: data.adminUsername, role: 'ADMIN',
    }));
  },

  getToken(): string | null {
    if (!isBrowser()) return null;
    return localStorage.getItem(TOKEN_KEY);
  },

  getSession(): Record<string, unknown> | null {
    if (!isBrowser()) return null;
    const s = localStorage.getItem(SESSION_KEY);
    return s ? JSON.parse(s) : null;
  },

  getSessionId(): number | null {
    const s = this.getSession();
    return s?.sessionId as number | null;
  },

  getStoreId(): number | null {
    const s = this.getSession();
    return s?.storeId as number | null;
  },

  isLoggedIn(): boolean {
    return !!this.getToken();
  },

  logout() {
    if (!isBrowser()) return;
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(SESSION_KEY);
  },
};

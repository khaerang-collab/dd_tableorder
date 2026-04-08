import type { KakaoLoginResponse, AdminLoginResponse } from '@/types';

const TOKEN_KEY = 'token';
const SESSION_KEY = 'session';

export const authService = {
  saveCustomerSession(data: KakaoLoginResponse) {
    localStorage.setItem(TOKEN_KEY, data.token);
    localStorage.setItem(SESSION_KEY, JSON.stringify({
      storeId: data.storeId, storeName: data.storeName,
      tableNumber: data.tableNumber, tableId: data.tableId,
      sessionId: data.sessionId, profileId: data.profileId,
      nickname: data.nickname, profileImageUrl: data.profileImageUrl,
    }));
  },

  saveAdminSession(data: AdminLoginResponse) {
    localStorage.setItem(TOKEN_KEY, data.token);
    localStorage.setItem(SESSION_KEY, JSON.stringify({
      storeId: data.storeId, storeName: data.storeName,
      adminUsername: data.adminUsername, role: 'ADMIN',
    }));
  },

  getToken(): string | null { return localStorage.getItem(TOKEN_KEY); },

  getSession(): Record<string, unknown> | null {
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

  isLoggedIn(): boolean { return !!this.getToken(); },

  logout() {
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(SESSION_KEY);
  },
};

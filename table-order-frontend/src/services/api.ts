import type { Cart, Dashboard, KakaoLoginResponse, AdminLoginResponse, MenuCategory, Order, RestaurantTable, OrderHistory } from '@/types';

function getToken(): string | null {
  if (typeof window === 'undefined') return null;
  return localStorage.getItem('token');
}

async function request<T>(path: string, options: RequestInit = {}): Promise<T> {
  const token = getToken();
  const headers: Record<string, string> = { 'Content-Type': 'application/json', ...options.headers as Record<string, string> };
  if (token) headers['Authorization'] = `Bearer ${token}`;

  const res = await fetch(path, { ...options, headers });
  if (!res.ok) {
    const err = await res.json().catch(() => ({ message: '요청에 실패했습니다' }));
    throw new Error(err.message || `HTTP ${res.status}`);
  }
  if (res.status === 204) return undefined as T;
  return res.json();
}

// Auth
export const api = {
  kakaoLogin: (storeId: number, tableNumber: number, kakaoAccessToken: string) =>
    request<KakaoLoginResponse>('/api/customer/auth/kakao-login', {
      method: 'POST', body: JSON.stringify({ storeId, tableNumber, kakaoAccessToken }),
    }),

  adminLogin: (storeId: number, username: string, password: string) =>
    request<AdminLoginResponse>('/api/admin/auth/login', {
      method: 'POST', body: JSON.stringify({ storeId, username, password }),
    }),

  // Menu (public)
  getMenus: (storeId: number) => request<MenuCategory[]>(`/api/stores/${storeId}/menus`),

  // Cart
  getCart: (sessionId: number) => request<Cart>(`/api/customer/sessions/${sessionId}/cart`),
  addCartItem: (sessionId: number, menuId: number, quantity: number) =>
    request<Cart>(`/api/customer/sessions/${sessionId}/cart/items`, {
      method: 'POST', body: JSON.stringify({ menuId, quantity }),
    }),
  updateCartItem: (sessionId: number, itemId: number, quantity: number) =>
    request<Cart>(`/api/customer/sessions/${sessionId}/cart/items/${itemId}`, {
      method: 'PUT', body: JSON.stringify({ quantity }),
    }),
  removeCartItem: (sessionId: number, itemId: number) =>
    request<Cart>(`/api/customer/sessions/${sessionId}/cart/items/${itemId}`, { method: 'DELETE' }),

  // Orders (customer)
  createOrder: (sessionId: number) =>
    request<Order>(`/api/customer/sessions/${sessionId}/orders`, { method: 'POST' }),
  getOrders: (sessionId: number) => request<Order[]>(`/api/customer/sessions/${sessionId}/orders`),

  // Admin - Dashboard
  getDashboard: (storeId: number) => request<Dashboard>(`/api/admin/stores/${storeId}/dashboard`),
  updateOrderStatus: (orderId: number, status: string) =>
    request<Order>(`/api/admin/orders/${orderId}/status`, {
      method: 'PUT', body: JSON.stringify({ status }),
    }),
  deleteOrder: (orderId: number) =>
    request<void>(`/api/admin/orders/${orderId}`, { method: 'DELETE' }),

  // Admin - Tables
  getTables: (storeId: number) => request<RestaurantTable[]>(`/api/admin/stores/${storeId}/tables`),
  createTable: (storeId: number, tableNumber: number, baseUrl: string) =>
    request<RestaurantTable>(`/api/admin/stores/${storeId}/tables`, {
      method: 'POST', body: JSON.stringify({ tableNumber, baseUrl }),
    }),
  completeTable: (storeId: number, tableId: number) =>
    request<void>(`/api/admin/stores/${storeId}/tables/${tableId}/complete`, { method: 'POST' }),

  // Admin - Menus
  createMenu: (storeId: number, data: Record<string, unknown>) =>
    request(`/api/admin/stores/${storeId}/menus`, { method: 'POST', body: JSON.stringify(data) }),
  deleteMenu: (menuId: number) => request<void>(`/api/admin/menus/${menuId}`, { method: 'DELETE' }),

  // Admin - Categories
  getCategories: (storeId: number) => request(`/api/admin/stores/${storeId}/categories`),
  createCategory: (storeId: number, name: string) =>
    request(`/api/admin/stores/${storeId}/categories`, { method: 'POST', body: JSON.stringify({ name }) }),
  deleteCategory: (storeId: number, categoryId: number) =>
    request<void>(`/api/admin/stores/${storeId}/categories/${categoryId}`, { method: 'DELETE' }),

  // Admin - Order History
  getOrderHistory: (storeId: number, tableId?: number) =>
    request<OrderHistory[]>(`/api/admin/stores/${storeId}/orders/history${tableId ? `?tableId=${tableId}` : ''}`),

  // Image
  uploadImage: async (storeId: number, file: File): Promise<{ imageUrl: string }> => {
    const token = getToken();
    const formData = new FormData();
    formData.append('file', file);
    const res = await fetch(`/api/admin/stores/${storeId}/images`, {
      method: 'POST', headers: token ? { Authorization: `Bearer ${token}` } : {},
      body: formData,
    });
    return res.json();
  },
};

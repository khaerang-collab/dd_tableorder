// Store
export interface Store {
  id: number;
  name: string;
  address?: string;
  phone?: string;
  notice?: string;
  originInfo?: string;
}

// Menu
export interface MenuCategory {
  categoryId: number;
  categoryName: string;
  displayOrder: number;
  menus: MenuItem[];
}

export interface MenuItem {
  id: number;
  categoryId: number;
  name: string;
  price: number;
  description?: string;
  imageUrl?: string;
  badgeType?: 'POPULAR' | 'NEW' | 'EVENT' | 'SOLD_OUT';
  displayOrder: number;
  isAvailable: boolean;
}

// Cart
export interface CartItem {
  id: number;
  menuId: number;
  menuName: string;
  menuPrice: number;
  imageUrl?: string;
  customerProfileId?: number;
  nickname?: string;
  profileImageUrl?: string;
  deviceId: string;
  quantity: number;
  createdAt: string;
}

export interface Cart {
  sessionId: number;
  items: CartItem[];
  totalAmount: number;
}

// Order
export interface OrderItem {
  id: number;
  menuName: string;
  quantity: number;
  unitPrice: number;
  customerProfileId?: number;
  customerNickname?: string;
}

export interface Order {
  id: number;
  orderNumber: string;
  totalAmount: number;
  status: 'PENDING' | 'PREPARING' | 'COMPLETED';
  items: OrderItem[];
  createdAt: string;
}

// Dashboard
export interface TableOrderSummary {
  tableId: number;
  tableNumber: number;
  sessionId?: number;
  totalAmount: number;
  orderCount: number;
  recentOrders: Order[];
}

export interface Dashboard {
  tables: TableOrderSummary[];
}

// Table
export interface RestaurantTable {
  id: number;
  storeId: number;
  tableNumber: number;
  qrCodeUrl: string;
}

// Auth
export interface AuthToken {
  token: string;
  storeId: number;
  storeName: string;
}

export interface KakaoLoginResponse extends AuthToken {
  tableNumber: number;
  tableId: number;
  sessionId: number;
  profileId: number;
  nickname: string;
  profileImageUrl?: string;
}

export interface AdminLoginResponse extends AuthToken {
  adminUsername: string;
}

// WebSocket Event
export interface WsEvent {
  type: string;
  data: Record<string, unknown>;
}

// Customer Profile
export interface CustomerProfile {
  id: number;
  nickname: string;
  gender?: string;
  ageRange?: string;
  profileImageUrl?: string;
  visitCount: number;
  totalOrderAmount: number;
  lastVisitAt?: string;
}

// OrderHistory
export interface OrderHistory {
  id: number;
  storeId: number;
  tableId: number;
  tableNumber: number;
  sessionId: number;
  orderDataJson: string;
  totalAmount: number;
  sessionStartedAt: string;
  completedAt: string;
}

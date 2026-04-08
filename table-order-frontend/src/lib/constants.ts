export const API_URL = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080';
export const WS_URL = process.env.NEXT_PUBLIC_WS_URL || 'ws://localhost:8080';
export const KAKAO_JS_KEY = process.env.NEXT_PUBLIC_KAKAO_JS_KEY || '';

export const ORDER_STATUS_LABEL: Record<string, string> = {
  PENDING: '대기중',
  PREPARING: '준비중',
  COMPLETED: '완료',
};

export const ORDER_STATUS_COLOR: Record<string, string> = {
  PENDING: 'bg-orange-10 text-orange-300',
  PREPARING: 'bg-blue-50 text-blue-300',
  COMPLETED: 'bg-green-50 text-green-300',
};

export const BADGE_STYLES: Record<string, string> = {
  POPULAR: 'bg-blue-300 text-white',
  NEW: 'bg-red-300 text-white',
  EVENT: 'bg-green-300 text-white',
  SOLD_OUT: 'bg-coolGray-400 text-white',
};

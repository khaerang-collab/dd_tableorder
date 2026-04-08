import { formatPrice } from '@/lib/utils';

interface Props { totalAmount: number; itemCount: number; }

export default function CartFloatingBar({ totalAmount, itemCount }: Props) {
  if (itemCount === 0) return null;
  return (
    <div className="fixed bottom-0 left-1/2 -translate-x-1/2 w-full max-w-mobile z-30 p-4"
         style={{ paddingBottom: 'calc(var(--safe-area-bottom) + 16px)' }}>
      <a href="/customer/cart"
         className="flex items-center justify-center gap-2 w-full py-4 rounded-2xl bg-yellow-400 text-coolGray-900 text-t6 shadow-depth1"
         data-testid="cart-floating-bar">
        🛒 {formatPrice(totalAmount)} 장바구니 보기
      </a>
    </div>
  );
}

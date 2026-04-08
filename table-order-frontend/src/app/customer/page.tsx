'use client';

import { useEffect, useState, useRef, useCallback } from 'react';
import { useRouter } from 'next/navigation';
import { api } from '@/services/api';
import { authService } from '@/services/auth';
import { wsService } from '@/services/websocket';
import CategoryTabs from '@/components/shared/CategoryTabs';
import MenuListItem from '@/components/shared/MenuListItem';
import CartFloatingBar from '@/components/shared/CartFloatingBar';
import Toast from '@/components/shared/Toast';
import PromotionNudge from '@/components/customer/PromotionNudge';
import type { MenuCategory, Cart } from '@/types';

export default function MenuPage() {
  const router = useRouter();
  const [categories, setCategories] = useState<MenuCategory[]>([]);
  const [activeCategory, setActiveCategory] = useState<number | null>(null);
  const [cart, setCart] = useState<Cart | null>(null);
  const [toast, setToast] = useState('');
  const sectionRefs = useRef<Record<number, HTMLDivElement | null>>({});
  const [storeId, setStoreId] = useState<number | null>(null);
  const [sessionId, setSessionId] = useState<number | null>(null);

  useEffect(() => {
    if (!authService.isLoggedIn()) {
      router.replace('/customer/table-login?storeId=1&table=1');
      return;
    }
    setStoreId(authService.getStoreId());
    setSessionId(authService.getSessionId());
  }, [router]);

  useEffect(() => {
    if (!storeId) return;
    api.getMenus(storeId).then((data) => {
      setCategories(data);
      if (data.length > 0) setActiveCategory(data[0].categoryId);
    });
  }, [storeId]);

  useEffect(() => {
    if (!sessionId) return;
    api.getCart(sessionId).then(setCart).catch(() => {});
  }, [sessionId]);

  useEffect(() => {
    const unsub = wsService.onEvent((event) => {
      if (['CART_ITEM_ADDED', 'CART_ITEM_UPDATED', 'CART_ITEM_REMOVED', 'CART_CLEARED'].includes(event.type)) {
        if (sessionId) api.getCart(sessionId).then(setCart);
      }
    });
    return unsub;
  }, [sessionId]);

  const handleCategorySelect = useCallback((id: number) => {
    setActiveCategory(id);
    sectionRefs.current[id]?.scrollIntoView({ behavior: 'smooth', block: 'start' });
  }, []);

  const handleAddToCart = async (menuId: number, menuName: string) => {
    if (!sessionId) {
      setToast('로그인이 필요합니다');
      router.replace('/customer/table-login?storeId=1&table=1');
      return;
    }
    try {
      const updated = await api.addCartItem(sessionId, menuId, 1);
      setCart(updated);
      setToast(`${menuName} 메뉴를 담았어요`);
    } catch (e) {
      setToast('메뉴 추가에 실패했습니다');
    }
  };

  const tabCategories = categories.map((c) => ({ id: c.categoryId, name: c.categoryName }));

  return (
    <div className="pb-24">
      <CategoryTabs categories={tabCategories} activeId={activeCategory} onSelect={handleCategorySelect} />

      {categories.map((cat) => (
        <div key={cat.categoryId} ref={(el) => { sectionRefs.current[cat.categoryId] = el; }}>
          <div className="px-6 py-3 bg-white">
            <h2 className="text-t5 font-bold text-coolGray-900">{cat.categoryName}</h2>
          </div>
          {cat.menus.map((menu) => (
            <div key={menu.id} className="bg-white border-b border-coolGray-200">
              <MenuListItem menu={menu} onClick={() => handleAddToCart(menu.id, menu.name)} />
            </div>
          ))}
          <div className="h-4 bg-coolGray-100" />
        </div>
      ))}

      <div className="bg-white px-6 py-6">
        <p className="text-t7 font-semibold text-coolGray-700">가게정보·원산지</p>
        <p className="text-t7 text-coolGray-500 mt-1">매장 정보는 관리자에게 문의하세요.</p>
      </div>

      <PromotionNudge cartAmount={cart?.totalAmount || 0} hasCartItems={(cart?.items.length || 0) > 0} />
      <CartFloatingBar totalAmount={cart?.totalAmount || 0} itemCount={cart?.items.length || 0} />
      {toast && <Toast message={toast} onClose={() => setToast('')} />}
    </div>
  );
}

'use client';

import { useEffect, useState } from 'react';
import { api } from '@/services/api';
import { authService } from '@/services/auth';
import { formatPrice } from '@/lib/utils';
import ConfirmDialog from '@/components/shared/ConfirmDialog';
import Toast from '@/components/shared/Toast';
import type { MenuCategory } from '@/types';

export default function MenuManagePage() {
  const [categories, setCategories] = useState<MenuCategory[]>([]);
  const [newCatName, setNewCatName] = useState('');
  const [showMenuForm, setShowMenuForm] = useState(false);
  const [menuForm, setMenuForm] = useState({ categoryId: 0, name: '', price: '', description: '' });
  const [confirmAction, setConfirmAction] = useState<{ title: string; message: string; action: () => void } | null>(null);
  const [toast, setToast] = useState('');
  const [storeId, setStoreId] = useState<number | null>(null);

  useEffect(() => {
    setStoreId(authService.getStoreId());
  }, []);

  const loadMenus = () => { if (storeId) api.getMenus(storeId).then(setCategories).catch(() => {}); };
  useEffect(loadMenus, [storeId]);

  const createCategory = async () => {
    if (!storeId || !newCatName.trim()) return;
    try {
      await api.createCategory(storeId, newCatName.trim());
      setNewCatName(''); loadMenus();
      setToast('카테고리가 추가되었습니다');
    } catch (e: any) { setToast(e.message || '카테고리 추가 실패'); }
  };

  const deleteCategory = (catId: number, catName: string) => {
    setConfirmAction({
      title: '카테고리 삭제', message: `"${catName}" 카테고리를 삭제하시겠습니까?`,
      action: async () => {
        try {
          if (storeId) await api.deleteCategory(storeId, catId);
          loadMenus(); setConfirmAction(null); setToast('삭제되었습니다');
        } catch (e: any) { setToast(e.message); setConfirmAction(null); }
      },
    });
  };

  const createMenu = async () => {
    if (!storeId || !menuForm.name || !menuForm.price || !menuForm.categoryId) return;
    try {
      await api.createMenu(storeId, {
        categoryId: menuForm.categoryId, name: menuForm.name,
        price: Number(menuForm.price), description: menuForm.description,
      });
      setShowMenuForm(false);
      setMenuForm({ categoryId: 0, name: '', price: '', description: '' });
      loadMenus(); setToast('메뉴가 추가되었습니다');
    } catch (e: any) { setToast(e.message || '메뉴 추가 실패'); }
  };

  const deleteMenu = (menuId: number, menuName: string) => {
    setConfirmAction({
      title: '메뉴 삭제', message: `"${menuName}"을 삭제하시겠습니까?`,
      action: async () => {
        await api.deleteMenu(menuId);
        loadMenus(); setConfirmAction(null); setToast('삭제되었습니다');
      },
    });
  };

  return (
    <div>
      <h2 className="text-t5 font-bold text-coolGray-900 mb-4">메뉴 관리</h2>

      {/* 카테고리 추가 */}
      <div className="bg-white rounded-xl p-4 shadow-depth1 mb-4">
        <h3 className="text-t6 text-coolGray-900 mb-2">카테고리</h3>
        <div className="flex gap-2">
          <input type="text" placeholder="카테고리명" value={newCatName}
                 onChange={(e) => setNewCatName(e.target.value)}
                 className="flex-1 px-3 py-2 rounded-lg border border-coolGray-200 text-t7"
                 data-testid="new-category-name" />
          <button onClick={createCategory} className="px-4 py-2 rounded-lg bg-blue-300 text-white text-t7"
                  data-testid="create-category-button">추가</button>
        </div>
      </div>

      {/* 메뉴 추가 버튼 */}
      <button onClick={() => setShowMenuForm(true)}
              className="w-full mb-4 py-3 rounded-xl bg-yellow-400 text-coolGray-900 text-t6 font-bold"
              data-testid="open-menu-form-button">
        + 메뉴 추가
      </button>

      {/* 메뉴 추가 폼 모달 */}
      {showMenuForm && (
        <div className="fixed inset-0 z-40 bg-black/40 flex items-center justify-center"
             onClick={() => setShowMenuForm(false)}>
          <div className="bg-white rounded-2xl p-6 mx-4 max-w-sm w-full" onClick={(e) => e.stopPropagation()}
               data-testid="menu-form-modal">
            <h3 className="text-t5 font-bold mb-4">메뉴 추가</h3>
            <div className="space-y-3">
              <select value={menuForm.categoryId} onChange={(e) => setMenuForm({ ...menuForm, categoryId: Number(e.target.value) })}
                      className="w-full px-3 py-2 rounded-lg border border-coolGray-200 text-t7" data-testid="menu-form-category">
                <option value={0}>카테고리 선택</option>
                {categories.map((c) => <option key={c.categoryId} value={c.categoryId}>{c.categoryName}</option>)}
              </select>
              <input placeholder="메뉴명" value={menuForm.name}
                     onChange={(e) => setMenuForm({ ...menuForm, name: e.target.value })}
                     className="w-full px-3 py-2 rounded-lg border border-coolGray-200 text-t7" data-testid="menu-form-name" />
              <input type="number" placeholder="가격" value={menuForm.price}
                     onChange={(e) => setMenuForm({ ...menuForm, price: e.target.value })}
                     className="w-full px-3 py-2 rounded-lg border border-coolGray-200 text-t7" data-testid="menu-form-price" />
              <input placeholder="설명 (선택)" value={menuForm.description}
                     onChange={(e) => setMenuForm({ ...menuForm, description: e.target.value })}
                     className="w-full px-3 py-2 rounded-lg border border-coolGray-200 text-t7" data-testid="menu-form-desc" />
              <div className="flex gap-3 pt-2">
                <button onClick={() => setShowMenuForm(false)} className="flex-1 py-3 rounded-xl bg-coolGray-100 text-t6">취소</button>
                <button onClick={createMenu} className="flex-1 py-3 rounded-xl bg-blue-300 text-white text-t6"
                        data-testid="menu-form-submit">추가</button>
              </div>
            </div>
          </div>
        </div>
      )}

      {/* 카테고리별 메뉴 목록 */}
      {categories.map((cat) => (
        <div key={cat.categoryId} className="bg-white rounded-xl shadow-depth1 mb-3 overflow-hidden">
          <div className="flex justify-between items-center px-4 py-3 bg-coolGray-50">
            <span className="text-t6 text-coolGray-900">{cat.categoryName}</span>
            <button onClick={() => deleteCategory(cat.categoryId, cat.categoryName)}
                    className="text-t7 text-red-300" data-testid={`delete-category-${cat.categoryId}`}>삭제</button>
          </div>
          {cat.menus.map((menu) => (
            <div key={menu.id} className="flex justify-between items-center px-4 py-3 border-t border-coolGray-100"
                 data-testid={`manage-menu-${menu.id}`}>
              <div>
                <p className="text-t7 text-coolGray-900">{menu.name}</p>
                <p className="text-t7 text-coolGray-500">{formatPrice(menu.price)}</p>
              </div>
              <button onClick={() => deleteMenu(menu.id, menu.name)}
                      className="text-t7 text-red-300" data-testid={`delete-menu-${menu.id}`}>삭제</button>
            </div>
          ))}
        </div>
      ))}

      {confirmAction && (
        <ConfirmDialog title={confirmAction.title} message={confirmAction.message}
                       onConfirm={confirmAction.action} onCancel={() => setConfirmAction(null)} />
      )}
      {toast && <Toast message={toast} onClose={() => setToast('')} />}
    </div>
  );
}

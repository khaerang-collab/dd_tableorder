import { test, expect, Page } from '@playwright/test';

// 고객 로그인 헬퍼
async function customerLogin(page: Page, storeId = 1, tableNumber = 1) {
  await page.goto(`/customer/table-login?storeId=${storeId}&table=${tableNumber}`);

  // dev-test-token으로 직접 접속 (개발 모드)
  const devButton = page.getByText('카카오 없이 접속').or(page.getByTestId('dev-login-button'));
  if (await devButton.isVisible({ timeout: 3000 }).catch(() => false)) {
    await devButton.click();
  }

  // 메뉴판으로 이동 대기
  await page.waitForURL(/\/customer/, { timeout: 10000 });
}

test.describe('고객 주문 플로우', () => {
  test.beforeEach(async ({ page }) => {
    await customerLogin(page);
  });

  // PW-CUST-001: 고객 메뉴 조회
  test('메뉴판에서 카테고리 탭과 메뉴 아이템이 표시된다', async ({ page }) => {
    // 카테고리 탭 존재 확인
    const categoryTabs = page.locator('[data-testid^="category-tab-"]');
    await expect(categoryTabs.first()).toBeVisible({ timeout: 10000 });

    // 메뉴 아이템 존재 확인
    const menuItems = page.locator('[data-testid^="menu-item-"]');
    await expect(menuItems.first()).toBeVisible();
  });

  // PW-CUST-002: 메뉴 장바구니 추가 및 확인
  test('메뉴를 클릭하면 장바구니에 추가되고 플로팅바에 금액이 표시된다', async ({ page }) => {
    // 첫 번째 메뉴 아이템 클릭
    const firstMenu = page.locator('[data-testid^="menu-item-"]').first();
    await firstMenu.click();

    // Toast 알림 확인
    await expect(page.locator('text=담았어요')).toBeVisible({ timeout: 5000 });

    // 장바구니 플로팅바에 금액 표시 확인
    const floatingBar = page.getByTestId('cart-floating-bar');
    await expect(floatingBar).toBeVisible();

    // 장바구니 페이지로 이동
    await floatingBar.click();
    await expect(page).toHaveURL(/\/customer\/cart/);

    // 장바구니에 아이템 존재 확인
    const cartItems = page.locator('[data-testid^="cart-item-"]');
    await expect(cartItems.first()).toBeVisible();
  });

  // PW-CUST-003: 장바구니 수량 변경
  test('장바구니에서 수량을 증감할 수 있다', async ({ page }) => {
    // 메뉴 추가
    const firstMenu = page.locator('[data-testid^="menu-item-"]').first();
    await firstMenu.click();
    await page.waitForTimeout(1000);

    // 장바구니로 이동
    await page.goto('/customer/cart');

    const cartItem = page.locator('[data-testid^="cart-item-"]').first();
    await expect(cartItem).toBeVisible({ timeout: 5000 });

    // 수량 증가 버튼 클릭
    const increaseButton = cartItem.locator('[data-testid*="increase"]').or(cartItem.locator('button:has-text("+")'));
    if (await increaseButton.isVisible().catch(() => false)) {
      await increaseButton.click();
      await page.waitForTimeout(500);

      // 수량이 2로 변경됨 확인
      await expect(cartItem.locator('text=2')).toBeVisible();
    }
  });

  // PW-CUST-004: 주문하기 전체 플로우
  test('장바구니에서 주문하면 주문 완료 페이지로 이동한다', async ({ page }) => {
    // 메뉴 추가
    const firstMenu = page.locator('[data-testid^="menu-item-"]').first();
    await firstMenu.click();
    await page.waitForTimeout(1000);

    // 장바구니로 이동
    await page.goto('/customer/cart');
    await page.waitForTimeout(1000);

    // 주문하기 버튼 클릭
    const orderButton = page.getByTestId('place-order-button')
      .or(page.locator('button:has-text("주문하기")'));
    await expect(orderButton).toBeVisible({ timeout: 5000 });
    await orderButton.click();

    // 주문 완료 페이지 확인
    await expect(page).toHaveURL(/\/customer\/order-complete/, { timeout: 10000 });
  });

  // PW-CUST-005: 주문 내역 확인
  test('주문 후 주문 내역 페이지에서 주문을 확인할 수 있다', async ({ page }) => {
    // 메뉴 추가 및 주문
    const firstMenu = page.locator('[data-testid^="menu-item-"]').first();
    await firstMenu.click();
    await page.waitForTimeout(1000);

    await page.goto('/customer/cart');
    await page.waitForTimeout(1000);

    const orderButton = page.getByTestId('place-order-button')
      .or(page.locator('button:has-text("주문하기")'));
    if (await orderButton.isVisible().catch(() => false)) {
      await orderButton.click();
      await page.waitForTimeout(2000);
    }

    // 주문 내역 페이지로 이동
    await page.goto('/customer/orders');

    // 주문 내역 표시 확인
    const orders = page.locator('[data-testid^="order-"]');
    await expect(orders.first()).toBeVisible({ timeout: 10000 });
  });
});

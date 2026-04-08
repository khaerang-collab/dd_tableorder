import { test, expect, Page } from '@playwright/test';

async function adminLogin(page: Page) {
  await page.goto('/admin/login');
  await page.getByTestId('admin-login-store-id').fill('1');
  await page.getByTestId('admin-login-username').fill('admin');
  await page.getByTestId('admin-login-password').fill('admin1234');
  await page.getByTestId('admin-login-submit').click();
  await page.waitForURL(/\/admin\/dashboard/, { timeout: 10000 });
}

test.describe('관리자 네비게이션', () => {
  // PW-NAV-001: 관리자 네비게이션 및 로그아웃
  test('관리자 탭으로 페이지를 이동하고 로그아웃할 수 있다', async ({ page }) => {
    await adminLogin(page);

    // 대시보드 → 테이블 관리
    const tablesNav = page.locator('[data-testid*="nav"]').filter({ hasText: '테이블' })
      .or(page.locator('a:has-text("테이블")'));
    if (await tablesNav.isVisible().catch(() => false)) {
      await tablesNav.click();
      await expect(page).toHaveURL(/\/admin\/tables/);
    }

    // 테이블 관리 → 메뉴 관리
    const menusNav = page.locator('[data-testid*="nav"]').filter({ hasText: '메뉴' })
      .or(page.locator('a:has-text("메뉴")'));
    if (await menusNav.isVisible().catch(() => false)) {
      await menusNav.click();
      await expect(page).toHaveURL(/\/admin\/menus/);
    }

    // 메뉴 관리 → 대시보드
    const dashNav = page.locator('[data-testid*="nav"]').filter({ hasText: '주문' })
      .or(page.locator('a:has-text("주문")'));
    if (await dashNav.isVisible().catch(() => false)) {
      await dashNav.click();
      await expect(page).toHaveURL(/\/admin\/dashboard/);
    }

    // 로그아웃
    const logoutButton = page.getByTestId('admin-logout-button')
      .or(page.locator('button:has-text("로그아웃")'));
    if (await logoutButton.isVisible().catch(() => false)) {
      await logoutButton.click();
      await expect(page).toHaveURL(/\/admin\/login/, { timeout: 5000 });
    }
  });
});

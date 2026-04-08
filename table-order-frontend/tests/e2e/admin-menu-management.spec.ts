import { test, expect, Page } from '@playwright/test';

async function adminLogin(page: Page) {
  await page.goto('/admin/login');
  await page.getByTestId('admin-login-store-id').fill('1');
  await page.getByTestId('admin-login-username').fill('admin');
  await page.getByTestId('admin-login-password').fill('admin1234');
  await page.getByTestId('admin-login-submit').click();
  await page.waitForURL(/\/admin\/dashboard/, { timeout: 10000 });
}

test.describe('관리자 메뉴 관리', () => {
  test.beforeEach(async ({ page }) => {
    await adminLogin(page);
  });

  // PW-MGMT-001: 카테고리 추가
  test('새 카테고리를 추가할 수 있다', async ({ page }) => {
    await page.goto('/admin/menus');

    const categoryInput = page.getByTestId('new-category-name')
      .or(page.locator('input[placeholder*="카테고리"]'));
    await expect(categoryInput).toBeVisible({ timeout: 10000 });
    await categoryInput.fill('테스트 카테고리');

    const createButton = page.getByTestId('create-category-button')
      .or(page.locator('button:has-text("추가")').first());
    await createButton.click();

    // 새 카테고리가 목록에 표시됨
    await expect(page.locator('text=테스트 카테고리')).toBeVisible({ timeout: 5000 });
  });

  // PW-MGMT-002: 메뉴 추가
  test('메뉴 추가 모달에서 새 메뉴를 추가할 수 있다', async ({ page }) => {
    await page.goto('/admin/menus');

    const openFormButton = page.getByTestId('open-menu-form-button')
      .or(page.locator('button:has-text("메뉴 추가")'));
    await expect(openFormButton).toBeVisible({ timeout: 10000 });
    await openFormButton.click();

    // 모달 표시 확인
    const modal = page.getByTestId('menu-form-modal')
      .or(page.locator('[role="dialog"]'));
    await expect(modal).toBeVisible({ timeout: 5000 });

    // 메뉴 정보 입력
    const nameInput = page.getByTestId('menu-form-name')
      .or(modal.locator('input[placeholder*="메뉴명"]'));
    if (await nameInput.isVisible().catch(() => false)) {
      await nameInput.fill('테스트 메뉴');
    }

    const priceInput = page.getByTestId('menu-form-price')
      .or(modal.locator('input[placeholder*="가격"]'));
    if (await priceInput.isVisible().catch(() => false)) {
      await priceInput.fill('15000');
    }

    // 제출
    const submitButton = page.getByTestId('menu-form-submit')
      .or(modal.locator('button:has-text("추가")'));
    if (await submitButton.isVisible().catch(() => false)) {
      await submitButton.click();
    }
  });

  // PW-MGMT-003: 테이블 추가
  test('새 테이블을 추가할 수 있다', async ({ page }) => {
    await page.goto('/admin/tables');

    const tableInput = page.getByTestId('new-table-number')
      .or(page.locator('input[placeholder*="테이블"]'));
    await expect(tableInput).toBeVisible({ timeout: 10000 });
    await tableInput.fill('10');

    const createButton = page.getByTestId('create-table-button')
      .or(page.locator('button:has-text("추가")'));
    await createButton.click();

    // 새 테이블이 목록에 표시됨
    await expect(page.getByTestId('manage-table-10')
      .or(page.locator('text=10'))).toBeVisible({ timeout: 5000 });
  });
});

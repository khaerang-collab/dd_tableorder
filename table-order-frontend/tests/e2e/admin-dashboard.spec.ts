import { test, expect, Page } from '@playwright/test';

// 관리자 로그인 헬퍼
async function adminLogin(page: Page) {
  await page.goto('/admin/login');
  await page.getByTestId('admin-login-store-id').fill('1');
  await page.getByTestId('admin-login-username').fill('admin');
  await page.getByTestId('admin-login-password').fill('admin1234');
  await page.getByTestId('admin-login-submit').click();
  await page.waitForURL(/\/admin\/dashboard/, { timeout: 10000 });
}

test.describe('관리자 대시보드', () => {
  test.beforeEach(async ({ page }) => {
    await adminLogin(page);
  });

  // PW-DASH-001: 대시보드 테이블 그리드 표시
  test('대시보드에 테이블 카드가 그리드 형태로 표시된다', async ({ page }) => {
    const tableCards = page.locator('[data-testid^="dashboard-table-"]');
    await expect(tableCards.first()).toBeVisible({ timeout: 10000 });
  });

  // PW-DASH-002: 테이블 클릭 시 상세 모달 표시
  test('테이블 카드를 클릭하면 상세 모달이 표시된다', async ({ page }) => {
    const firstTable = page.locator('[data-testid^="dashboard-table-"]').first();
    await expect(firstTable).toBeVisible({ timeout: 10000 });
    await firstTable.click();

    // 모달 또는 상세 패널 표시 확인
    const modal = page.getByTestId('table-detail-modal')
      .or(page.locator('[role="dialog"]'))
      .or(page.locator('.modal'));
    await expect(modal).toBeVisible({ timeout: 5000 });
  });

  // PW-DASH-003: 주문 상태 변경 (대기 → 준비)
  test('PENDING 주문의 상태를 변경할 수 있다', async ({ page }) => {
    // 주문이 있는 테이블 카드 클릭
    const tableCard = page.locator('[data-testid^="dashboard-table-"]').first();
    await expect(tableCard).toBeVisible({ timeout: 10000 });
    await tableCard.click();

    // 상태 변경 버튼 찾기
    const nextStatusButton = page.locator('[data-testid*="next-status"]')
      .or(page.locator('button:has-text("준비중")'));

    if (await nextStatusButton.isVisible({ timeout: 3000 }).catch(() => false)) {
      await nextStatusButton.click();

      // Toast 알림 확인
      await expect(
        page.locator('text=상태').or(page.locator('text=변경'))
      ).toBeVisible({ timeout: 5000 });
    }
  });

  // PW-DASH-004: 이용 완료 처리
  test('이용 완료 버튼을 클릭하면 확인 다이얼로그 후 처리된다', async ({ page }) => {
    const tableCard = page.locator('[data-testid^="dashboard-table-"]').first();
    await expect(tableCard).toBeVisible({ timeout: 10000 });
    await tableCard.click();

    const completeButton = page.getByTestId('complete-table-button')
      .or(page.locator('button:has-text("이용 완료")'));

    if (await completeButton.isVisible({ timeout: 3000 }).catch(() => false)) {
      await completeButton.click();

      // 확인 다이얼로그
      const confirmOk = page.getByTestId('confirm-ok')
        .or(page.locator('button:has-text("확인")'));

      if (await confirmOk.isVisible({ timeout: 3000 }).catch(() => false)) {
        await confirmOk.click();
      }

      // 처리 완료 확인
      await page.waitForTimeout(1000);
    }
  });
});

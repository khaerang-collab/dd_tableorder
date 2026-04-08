import { test, expect } from '@playwright/test';

test.describe('관리자 로그인', () => {
  // PW-LOGIN-001: 관리자 로그인 성공 및 대시보드 이동
  test('올바른 자격증명으로 로그인 후 대시보드로 이동한다', async ({ page }) => {
    await page.goto('/admin/login');

    // 입력 필드에 값 입력
    await page.getByTestId('admin-login-store-id').fill('1');
    await page.getByTestId('admin-login-username').fill('admin');
    await page.getByTestId('admin-login-password').fill('admin1234');

    // 로그인 버튼 클릭
    await page.getByTestId('admin-login-submit').click();

    // 대시보드로 리다이렉트 확인
    await expect(page).toHaveURL(/\/admin\/dashboard/);
  });

  // PW-LOGIN-002: 잘못된 비밀번호 로그인 실패
  test('잘못된 비밀번호로 로그인하면 에러 메시지가 표시된다', async ({ page }) => {
    await page.goto('/admin/login');

    await page.getByTestId('admin-login-store-id').fill('1');
    await page.getByTestId('admin-login-username').fill('admin');
    await page.getByTestId('admin-login-password').fill('wrongpassword');
    await page.getByTestId('admin-login-submit').click();

    // 에러 메시지 표시 확인
    await expect(page.locator('text=실패').or(page.locator('text=오류')).or(page.locator('[role="alert"]'))).toBeVisible({ timeout: 5000 });

    // 대시보드로 이동하지 않음
    await expect(page).toHaveURL(/\/admin\/login/);
  });
});

// ============================================================
// 테이블오더 QA 자동화 테스트 (메인 기능 검증)
// 실행: node tests/qa-test.js
// ============================================================

const API = process.env.API_URL || 'http://localhost:8080';
let pass = 0, fail = 0;

async function req(method, path, body, token) {
  const opts = { method, headers: { 'Content-Type': 'application/json' } };
  if (token) opts.headers['Authorization'] = `Bearer ${token}`;
  if (body) opts.body = JSON.stringify(body);
  const res = await fetch(`${API}${path}`, opts);
  const data = await res.json().catch(() => null);
  return { status: res.status, data };
}

async function tc(id, name, fn) {
  try {
    await fn();
    pass++;
    console.log(`  ✅ ${id} ${name}`);
  } catch (e) {
    fail++;
    console.log(`  ❌ ${id} ${name}`);
    console.log(`     → ${e.message}`);
  }
}

function eq(a, b, msg) { if (a !== b) throw new Error(`${msg}: expected ${JSON.stringify(b)}, got ${JSON.stringify(a)}`); }
function ok(v, msg) { if (!v) throw new Error(msg); }

// ============================================================
console.log('\n========================================');
console.log('  테이블오더 QA 테스트 시작');
console.log('  API: ' + API);
console.log('========================================\n');

// ── 0. 사전 데이터 준비 (H2 빈 DB용) ──
// 먼저 관리자 로그인으로 데이터 존재 확인, 없으면 직접 생성
let adminToken;
let setupOk = false;

// 로그인 시도로 시드 데이터 존재 여부 확인
const loginCheck = await req('POST', '/api/admin/auth/login', { storeId: 1, username: 'admin', password: 'admin1234' });
if (loginCheck.status === 200) {
  adminToken = loginCheck.data.token;
  setupOk = true;
  console.log('📦 시드 데이터 확인됨\n');
} else {
  console.log('📦 시드 데이터 없음 - 테스트 데이터를 생성합니다\n');
  // H2 DB에 시드 없으면 직접 SQL insert 불가 → 서버 data.sql로 준비 필요
  // 우선 가능한 테스트만 진행
}

// ── 1. 관리자 인증 ──
console.log('📌 1. 관리자 인증');

if (setupOk) {
  await tc('TC-01', '관리자 로그인 성공', async () => {
    const r = await req('POST', '/api/admin/auth/login', { storeId: 1, username: 'admin', password: 'admin1234' });
    eq(r.status, 200, '상태코드');
    ok(r.data.token, '토큰 존재');
    ok(r.data.storeName, '매장명 존재');
    adminToken = r.data.token;
  });
}

await tc('TC-02', '관리자 로그인 실패 (잘못된 비밀번호)', async () => {
  const r = await req('POST', '/api/admin/auth/login', { storeId: 1, username: 'admin', password: 'wrongpwd123' });
  ok(r.status === 403 || r.status === 400, `403 또는 400이어야 함 (실제: ${r.status})`);
});

await tc('TC-03', '관리자 로그인 - 필수 필드 누락', async () => {
  const r = await req('POST', '/api/admin/auth/login', { storeId: 1, username: '', password: 'admin1234' });
  eq(r.status, 400, '상태코드');
});

await tc('TC-04', '관리자 로그인 - 비밀번호 8자 미만', async () => {
  const r = await req('POST', '/api/admin/auth/login', { storeId: 1, username: 'admin', password: 'short' });
  eq(r.status, 400, '상태코드');
});

// ── 2. 보안 검증 ──
console.log('\n📌 2. 보안 검증');

await tc('TC-05', '인증 없이 Admin API 접근 차단', async () => {
  const r = await req('GET', '/api/admin/stores/1/categories');
  ok(r.status === 401 || r.status === 403, `401/403 (실제: ${r.status})`);
});

await tc('TC-06', '변조된 JWT 토큰 차단', async () => {
  const r = await req('GET', '/api/admin/stores/1/categories', null, 'invalid.jwt.token');
  ok(r.status === 401 || r.status === 403, `401/403 (실제: ${r.status})`);
});

await tc('TC-07', '인증 없이 공개 API(메뉴 조회) 접근 가능', async () => {
  const r = await req('GET', '/api/stores/1/menus');
  eq(r.status, 200, '상태코드');
});

if (!setupOk) {
  console.log('\n⚠️  시드 데이터 없음 - 나머지 테스트는 시드 데이터 필요\n');
  console.log(`\n========================================`);
  console.log(`  📊 결과: ${pass + fail}개 중 ✅ ${pass}개 성공 / ❌ ${fail}개 실패`);
  console.log(`========================================\n`);
  process.exit(fail > 0 ? 1 : 0);
}

// ── 3. 메뉴 조회 ──
console.log('\n📌 3. 메뉴 조회');

await tc('TC-08', '고객용 메뉴 조회 - 카테고리+메뉴 반환', async () => {
  const r = await req('GET', '/api/stores/1/menus');
  eq(r.status, 200, '상태코드');
  ok(Array.isArray(r.data), '배열 반환');
  ok(r.data.length > 0, '카테고리 1개 이상');
  ok(r.data[0].menus.length > 0, '메뉴 1개 이상');
});

// ── 4. 카테고리/메뉴 관리 ──
console.log('\n📌 4. 카테고리/메뉴 관리 (관리자)');

let newCatId;
await tc('TC-09', '카테고리 생성', async () => {
  const r = await req('POST', '/api/admin/stores/1/categories', { name: 'QA테스트' }, adminToken);
  eq(r.status, 200, '상태코드');
  ok(r.data.id, '카테고리 ID 존재');
  newCatId = r.data.id;
});

let newMenuId;
await tc('TC-10', '메뉴 생성', async () => {
  const r = await req('POST', '/api/admin/stores/1/menus', {
    categoryId: newCatId, name: 'QA테스트 메뉴', price: 9900
  }, adminToken);
  eq(r.status, 200, '상태코드');
  eq(r.data.price, 9900, '가격');
  newMenuId = r.data.id;
});

await tc('TC-11', '메뉴 있는 카테고리 삭제 불가 (CATEGORY_HAS_MENUS)', async () => {
  const r = await req('DELETE', `/api/admin/stores/1/categories/${newCatId}`, null, adminToken);
  eq(r.status, 400, '상태코드');
  eq(r.data.code, 'CATEGORY_HAS_MENUS', '에러 코드');
});

await tc('TC-12', '메뉴 삭제 → 카테고리 삭제 가능', async () => {
  const r1 = await req('DELETE', `/api/admin/menus/${newMenuId}`, null, adminToken);
  eq(r1.status, 204, '메뉴 삭제');
  const r2 = await req('DELETE', `/api/admin/stores/1/categories/${newCatId}`, null, adminToken);
  eq(r2.status, 204, '카테고리 삭제');
});

// ── 5. 고객 주문 플로우 ──
console.log('\n📌 5. 고객 주문 플로우');

let customerToken, sessionId;
await tc('TC-13', '카카오 로그인 (dev-test-token)', async () => {
  const r = await req('POST', '/api/customer/auth/kakao-login', {
    storeId: 1, tableNumber: 1, kakaoAccessToken: 'dev-test-token'
  });
  eq(r.status, 200, '상태코드');
  ok(r.data.token, '토큰');
  ok(r.data.sessionId, '세션 ID');
  customerToken = r.data.token;
  sessionId = r.data.sessionId;
});

await tc('TC-14', '존재하지 않는 테이블 로그인 실패 (404)', async () => {
  const r = await req('POST', '/api/customer/auth/kakao-login', {
    storeId: 1, tableNumber: 999, kakaoAccessToken: 'dev-test-token'
  });
  eq(r.status, 404, '상태코드');
});

await tc('TC-15', '빈 장바구니 조회', async () => {
  const r = await req('GET', `/api/customer/sessions/${sessionId}/cart`, null, customerToken);
  eq(r.status, 200, '상태코드');
  eq(r.data.totalAmount, 0, '총액 0');
});

await tc('TC-16', '장바구니에 메뉴 추가', async () => {
  const r = await req('POST', `/api/customer/sessions/${sessionId}/cart/items`, {
    menuId: 1, quantity: 2
  }, customerToken);
  eq(r.status, 200, '상태코드');
  ok(r.data.items.length > 0, '아이템 존재');
});

await tc('TC-17', '동일 메뉴 재추가 → 수량 누적 (UPSERT)', async () => {
  const r = await req('POST', `/api/customer/sessions/${sessionId}/cart/items`, {
    menuId: 1, quantity: 1
  }, customerToken);
  eq(r.status, 200, '상태코드');
  eq(r.data.items.length, 1, '아이템 1개 (병합)');
});

await tc('TC-18', '수량 0 추가 시 400', async () => {
  const r = await req('POST', `/api/customer/sessions/${sessionId}/cart/items`, {
    menuId: 2, quantity: 0
  }, customerToken);
  eq(r.status, 400, '상태코드');
});

let orderId;
await tc('TC-19', '주문 생성', async () => {
  const r = await req('POST', `/api/customer/sessions/${sessionId}/orders`, null, customerToken);
  eq(r.status, 200, '상태코드');
  eq(r.data.status, 'PENDING', '주문 상태');
  ok(r.data.orderNumber, '주문번호');
  ok(/\d{8}-\d{6}-\d{4}/.test(r.data.orderNumber), '주문번호 형식');
  orderId = r.data.id;
});

await tc('TC-20', '주문 후 장바구니 비어있음', async () => {
  const r = await req('GET', `/api/customer/sessions/${sessionId}/cart`, null, customerToken);
  eq(r.status, 200, '상태코드');
  eq(r.data.items.length, 0, '아이템 0개');
});

await tc('TC-21', '빈 장바구니 주문 시 400 (EMPTY_CART)', async () => {
  const r = await req('POST', `/api/customer/sessions/${sessionId}/orders`, null, customerToken);
  eq(r.status, 400, '상태코드');
  eq(r.data.code, 'EMPTY_CART', '에러 코드');
});

await tc('TC-22', '주문 내역 조회', async () => {
  const r = await req('GET', `/api/customer/sessions/${sessionId}/orders`, null, customerToken);
  eq(r.status, 200, '상태코드');
  ok(r.data.length >= 1, '주문 1건 이상');
});

// ── 6. 관리자 주문 관리 ──
console.log('\n📌 6. 관리자 주문 관리');

await tc('TC-23', '대시보드 조회', async () => {
  const r = await req('GET', '/api/admin/stores/1/dashboard', null, adminToken);
  eq(r.status, 200, '상태코드');
  ok(r.data.tables, 'tables 존재');
});

await tc('TC-24', '주문 상태: PENDING → PREPARING', async () => {
  const r = await req('PUT', `/api/admin/orders/${orderId}/status`, { status: 'PREPARING' }, adminToken);
  eq(r.status, 200, '상태코드');
  eq(r.data.status, 'PREPARING', '상태');
});

await tc('TC-25', '잘못된 전이: PREPARING → PENDING 차단', async () => {
  const r = await req('PUT', `/api/admin/orders/${orderId}/status`, { status: 'PENDING' }, adminToken);
  eq(r.status, 400, '상태코드');
  eq(r.data.code, 'INVALID_STATUS_TRANSITION', '에러 코드');
});

await tc('TC-26', '주문 상태: PREPARING → COMPLETED', async () => {
  const r = await req('PUT', `/api/admin/orders/${orderId}/status`, { status: 'COMPLETED' }, adminToken);
  eq(r.status, 200, '상태코드');
  eq(r.data.status, 'COMPLETED', '상태');
});

await tc('TC-27', '잘못된 전이: COMPLETED → PREPARING 차단', async () => {
  const r = await req('PUT', `/api/admin/orders/${orderId}/status`, { status: 'PREPARING' }, adminToken);
  eq(r.status, 400, '상태코드');
});

// ── 7. 테이블 관리 ──
console.log('\n📌 7. 테이블 관리');

await tc('TC-28', '테이블 목록 조회', async () => {
  const r = await req('GET', '/api/admin/stores/1/tables', null, adminToken);
  eq(r.status, 200, '상태코드');
  ok(r.data.length >= 1, '테이블 1개 이상');
});

await tc('TC-29', '테이블 이용 완료 → 아카이브', async () => {
  const tables = await req('GET', '/api/admin/stores/1/tables', null, adminToken);
  const table1 = tables.data.find(t => t.tableNumber === 1);
  ok(table1, '테이블1 존재');
  const r = await req('POST', `/api/admin/stores/1/tables/${table1.id}/complete`, null, adminToken);
  eq(r.status, 200, '상태코드');
  eq(r.data.success, true, 'success');
});

await tc('TC-30', '주문 히스토리 확인', async () => {
  const r = await req('GET', '/api/admin/stores/1/orders/history', null, adminToken);
  eq(r.status, 200, '상태코드');
  ok(r.data.length >= 1, '히스토리 1건 이상');
});

// ── 결과 ──
console.log('\n========================================');
console.log(`  📊 결과: ${pass + fail}개 중 ✅ ${pass}개 성공 / ❌ ${fail}개 실패`);
console.log('========================================\n');

process.exit(fail > 0 ? 1 : 0);

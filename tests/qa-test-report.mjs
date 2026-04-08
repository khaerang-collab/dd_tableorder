// ============================================================
// 테이블오더 QA 자동화 테스트 + HTML 보고서 생성
// 실행: node tests/qa-test-report.mjs
// ============================================================
import { writeFileSync } from 'fs';

const API = process.env.API_URL || 'http://localhost:8080';
let pass = 0, fail = 0;
const results = [];
const startTime = Date.now();

async function req(method, path, body, token) {
  const opts = { method, headers: { 'Content-Type': 'application/json' } };
  if (token) opts.headers['Authorization'] = `Bearer ${token}`;
  if (body) opts.body = JSON.stringify(body);
  const res = await fetch(`${API}${path}`, opts);
  const data = await res.json().catch(() => null);
  return { status: res.status, data };
}

async function tc(id, name, category, fn) {
  const t0 = Date.now();
  try {
    await fn();
    pass++;
    const ms = Date.now() - t0;
    results.push({ id, name, category, status: 'PASS', ms });
    console.log(`  ✅ ${id} ${name} (${ms}ms)`);
  } catch (e) {
    fail++;
    const ms = Date.now() - t0;
    results.push({ id, name, category, status: 'FAIL', ms, error: e.message });
    console.log(`  ❌ ${id} ${name} (${ms}ms)`);
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

// 시드 데이터 확인
const loginCheck = await req('POST', '/api/admin/auth/login', { storeId: 1, username: 'admin', password: 'admin1234' });
let adminToken;
if (loginCheck.status !== 200) {
  console.log('❌ 시드 데이터 없음 - 서버에 data-h2.sql 로드 필요');
  process.exit(1);
}
adminToken = loginCheck.data.token;
console.log('📦 시드 데이터 확인됨\n');

// ── 1. 관리자 인증 ──
console.log('📌 1. 관리자 인증');

await tc('TC-01', '관리자 로그인 성공', '관리자 인증', async () => {
  const r = await req('POST', '/api/admin/auth/login', { storeId: 1, username: 'admin', password: 'admin1234' });
  eq(r.status, 200, '상태코드');
  ok(r.data.token, '토큰 존재');
  ok(r.data.storeName, '매장명 존재');
});

await tc('TC-02', '관리자 로그인 실패 (잘못된 비밀번호)', '관리자 인증', async () => {
  const r = await req('POST', '/api/admin/auth/login', { storeId: 1, username: 'admin', password: 'wrongpwd123' });
  eq(r.status, 403, '상태코드');
});

await tc('TC-03', '입력 검증 - username 빈값', '관리자 인증', async () => {
  const r = await req('POST', '/api/admin/auth/login', { storeId: 1, username: '', password: 'admin1234' });
  eq(r.status, 400, '상태코드');
});

await tc('TC-04', '입력 검증 - 비밀번호 8자 미만', '관리자 인증', async () => {
  const r = await req('POST', '/api/admin/auth/login', { storeId: 1, username: 'admin', password: 'short' });
  eq(r.status, 400, '상태코드');
});

// ── 2. 보안 검증 ──
console.log('\n📌 2. 보안 검증');

await tc('TC-05', '인증 없이 Admin API 접근 차단 (401/403)', '보안', async () => {
  const r = await req('GET', '/api/admin/stores/1/categories');
  ok(r.status === 401 || r.status === 403, `401/403 (실제: ${r.status})`);
});

await tc('TC-06', '변조된 JWT 토큰 차단', '보안', async () => {
  const r = await req('GET', '/api/admin/stores/1/categories', null, 'invalid.jwt.token');
  ok(r.status === 401 || r.status === 403, `401/403 (실제: ${r.status})`);
});

await tc('TC-07', '공개 API(메뉴 조회) 인증 없이 접근 가능', '보안', async () => {
  const r = await req('GET', '/api/stores/1/menus');
  eq(r.status, 200, '상태코드');
});

await tc('TC-08', 'Customer 토큰으로 Admin API 접근 차단', '보안', async () => {
  const cLogin = await req('POST', '/api/customer/auth/kakao-login', {
    storeId: 1, tableNumber: 2, kakaoAccessToken: 'dev-test-token'
  });
  const r = await req('GET', '/api/admin/stores/1/categories', null, cLogin.data.token);
  eq(r.status, 403, '상태코드');
});

// ── 3. 메뉴 조회 ──
console.log('\n📌 3. 메뉴 조회');

await tc('TC-09', '고객용 메뉴 조회 - 카테고리+메뉴 반환', '메뉴', async () => {
  const r = await req('GET', '/api/stores/1/menus');
  eq(r.status, 200, '상태코드');
  ok(Array.isArray(r.data), '배열 반환');
  ok(r.data.length > 0, '카테고리 1개 이상');
  ok(r.data[0].menus.length > 0, '메뉴 1개 이상');
});

// ── 4. 고객 주문 플로우 ──
console.log('\n📌 4. 고객 주문 플로우');

let customerToken, sessionId;
await tc('TC-10', '카카오 로그인 (dev-test-token)', '고객 주문', async () => {
  const r = await req('POST', '/api/customer/auth/kakao-login', {
    storeId: 1, tableNumber: 1, kakaoAccessToken: 'dev-test-token'
  });
  eq(r.status, 200, '상태코드');
  ok(r.data.token, '토큰');
  ok(r.data.sessionId, '세션 ID');
  customerToken = r.data.token;
  sessionId = r.data.sessionId;
});

await tc('TC-11', '존재하지 않는 테이블 로그인 실패 (404)', '고객 주문', async () => {
  const r = await req('POST', '/api/customer/auth/kakao-login', {
    storeId: 1, tableNumber: 999, kakaoAccessToken: 'dev-test-token'
  });
  eq(r.status, 404, '상태코드');
});

await tc('TC-12', '빈 장바구니 조회', '고객 주문', async () => {
  const r = await req('GET', `/api/customer/sessions/${sessionId}/cart`, null, customerToken);
  eq(r.status, 200, '상태코드');
  eq(r.data.totalAmount, 0, '총액 0');
});

await tc('TC-13', '장바구니에 메뉴 추가 (수량 2)', '고객 주문', async () => {
  const r = await req('POST', `/api/customer/sessions/${sessionId}/cart/items`, {
    menuId: 1, quantity: 2
  }, customerToken);
  eq(r.status, 200, '상태코드');
  ok(r.data.items.length > 0, '아이템 존재');
});

await tc('TC-14', '동일 메뉴 재추가 → 수량 누적 (UPSERT)', '고객 주문', async () => {
  const r = await req('POST', `/api/customer/sessions/${sessionId}/cart/items`, {
    menuId: 1, quantity: 1
  }, customerToken);
  eq(r.status, 200, '상태코드');
  eq(r.data.items.length, 1, '아이템 1개 (병합)');
});

await tc('TC-15', '수량 0 추가 시 400 (입력 검증)', '고객 주문', async () => {
  const r = await req('POST', `/api/customer/sessions/${sessionId}/cart/items`, {
    menuId: 2, quantity: 0
  }, customerToken);
  eq(r.status, 400, '상태코드');
});

let orderId;
await tc('TC-16', '주문 생성 (PENDING)', '고객 주문', async () => {
  const r = await req('POST', `/api/customer/sessions/${sessionId}/orders`, null, customerToken);
  eq(r.status, 200, '상태코드');
  eq(r.data.status, 'PENDING', '주문 상태');
  ok(r.data.orderNumber, '주문번호');
  ok(/\d{8}-\d{6}-\d{4}/.test(r.data.orderNumber), '주문번호 형식');
  orderId = r.data.id;
});

await tc('TC-17', '주문 후 장바구니 비어있음', '고객 주문', async () => {
  const r = await req('GET', `/api/customer/sessions/${sessionId}/cart`, null, customerToken);
  eq(r.status, 200, '상태코드');
  eq(r.data.items.length, 0, '아이템 0개');
});

await tc('TC-18', '빈 장바구니 주문 시 400 (EMPTY_CART)', '고객 주문', async () => {
  const r = await req('POST', `/api/customer/sessions/${sessionId}/orders`, null, customerToken);
  eq(r.status, 400, '상태코드');
  eq(r.data.code, 'EMPTY_CART', '에러 코드');
});

// ── 5. 관리자 주문 관리 ──
console.log('\n📌 5. 관리자 주문 관리');

await tc('TC-19', '주문 상태: PENDING → PREPARING', '주문 관리', async () => {
  const r = await req('PUT', `/api/admin/orders/${orderId}/status`, { status: 'PREPARING' }, adminToken);
  eq(r.status, 200, '상태코드');
  eq(r.data.status, 'PREPARING', '상태');
});

await tc('TC-20', '잘못된 전이: PREPARING → PENDING 차단', '주문 관리', async () => {
  const r = await req('PUT', `/api/admin/orders/${orderId}/status`, { status: 'PENDING' }, adminToken);
  eq(r.status, 400, '상태코드');
  eq(r.data.code, 'INVALID_STATUS_TRANSITION', '에러 코드');
});

await tc('TC-21', '주문 상태: PREPARING → COMPLETED', '주문 관리', async () => {
  const r = await req('PUT', `/api/admin/orders/${orderId}/status`, { status: 'COMPLETED' }, adminToken);
  eq(r.status, 200, '상태코드');
  eq(r.data.status, 'COMPLETED', '상태');
});

await tc('TC-22', '잘못된 전이: COMPLETED → PREPARING 차단', '주문 관리', async () => {
  const r = await req('PUT', `/api/admin/orders/${orderId}/status`, { status: 'PREPARING' }, adminToken);
  eq(r.status, 400, '상태코드');
});

// ── 6. 테이블 관리 ──
console.log('\n📌 6. 테이블 관리');

await tc('TC-23', '테이블 목록 조회', '테이블 관리', async () => {
  const r = await req('GET', '/api/admin/stores/1/tables', null, adminToken);
  eq(r.status, 200, '상태코드');
  ok(r.data.length >= 1, '테이블 1개 이상');
});

await tc('TC-24', '테이블 이용 완료 → 세션 종료 + 아카이브', '테이블 관리', async () => {
  const tables = await req('GET', '/api/admin/stores/1/tables', null, adminToken);
  const table1 = tables.data.find(t => t.tableNumber === 1);
  ok(table1, '테이블1 존재');
  const r = await req('POST', `/api/admin/stores/1/tables/${table1.id}/complete`, null, adminToken);
  eq(r.status, 200, '상태코드');
  eq(r.data.success, true, 'success');
});

await tc('TC-25', '주문 히스토리 확인 (아카이브)', '테이블 관리', async () => {
  const r = await req('GET', '/api/admin/stores/1/orders/history', null, adminToken);
  eq(r.status, 200, '상태코드');
  ok(r.data.length >= 1, '히스토리 1건 이상');
});

// ── 결과 출력 ──
const totalMs = Date.now() - startTime;
console.log('\n========================================');
console.log(`  📊 결과: ${pass + fail}개 중 ✅ ${pass}개 성공 / ❌ ${fail}개 실패`);
console.log(`  ⏱️  소요 시간: ${(totalMs / 1000).toFixed(1)}초`);
console.log('========================================\n');

// ── HTML 보고서 생성 ──
const now = new Date().toLocaleString('ko-KR', { timeZone: 'Asia/Seoul' });
const categories = [...new Set(results.map(r => r.category))];

const html = `<!DOCTYPE html>
<html lang="ko">
<head>
<meta charset="UTF-8">
<title>QA 테스트 보고서 - 테이블오더</title>
<style>
  * { margin: 0; padding: 0; box-sizing: border-box; }
  body { font-family: -apple-system, 'Noto Sans KR', sans-serif; background: #f5f5f5; padding: 24px; }
  .container { max-width: 900px; margin: 0 auto; }
  h1 { font-size: 24px; margin-bottom: 8px; }
  .meta { color: #666; margin-bottom: 24px; font-size: 14px; }
  .summary { display: flex; gap: 16px; margin-bottom: 24px; }
  .card { background: #fff; border-radius: 12px; padding: 20px; flex: 1; text-align: center; box-shadow: 0 1px 3px rgba(0,0,0,0.1); }
  .card .num { font-size: 36px; font-weight: 700; }
  .card.total .num { color: #333; }
  .card.pass .num { color: #22c55e; }
  .card.fail .num { color: #ef4444; }
  .card .label { color: #888; font-size: 13px; margin-top: 4px; }
  .rate { background: #fff; border-radius: 12px; padding: 20px; margin-bottom: 24px; box-shadow: 0 1px 3px rgba(0,0,0,0.1); }
  .bar-bg { height: 24px; background: #fee2e2; border-radius: 12px; overflow: hidden; }
  .bar-fill { height: 100%; background: #22c55e; border-radius: 12px; transition: width 0.5s; }
  .bar-text { text-align: center; margin-top: 8px; font-weight: 600; font-size: 18px; }
  .section { background: #fff; border-radius: 12px; margin-bottom: 16px; box-shadow: 0 1px 3px rgba(0,0,0,0.1); overflow: hidden; }
  .section-title { padding: 14px 20px; font-weight: 700; font-size: 15px; background: #fafafa; border-bottom: 1px solid #eee; }
  table { width: 100%; border-collapse: collapse; }
  th { background: #f9fafb; text-align: left; padding: 10px 16px; font-size: 12px; color: #666; border-bottom: 1px solid #eee; }
  td { padding: 10px 16px; font-size: 14px; border-bottom: 1px solid #f3f3f3; }
  tr:last-child td { border-bottom: none; }
  .badge { display: inline-block; padding: 2px 10px; border-radius: 10px; font-size: 12px; font-weight: 600; }
  .badge.pass { background: #dcfce7; color: #16a34a; }
  .badge.fail { background: #fee2e2; color: #dc2626; }
  .error-msg { color: #dc2626; font-size: 12px; margin-top: 4px; }
  .time { color: #999; font-size: 12px; }
</style>
</head>
<body>
<div class="container">
  <h1>📋 QA 테스트 보고서</h1>
  <div class="meta">테이블오더 시스템 | ${now} | 소요 시간: ${(totalMs / 1000).toFixed(1)}초</div>

  <div class="summary">
    <div class="card total"><div class="num">${pass + fail}</div><div class="label">전체</div></div>
    <div class="card pass"><div class="num">${pass}</div><div class="label">성공</div></div>
    <div class="card fail"><div class="num">${fail}</div><div class="label">실패</div></div>
  </div>

  <div class="rate">
    <div class="bar-bg"><div class="bar-fill" style="width:${((pass / (pass + fail)) * 100).toFixed(0)}%"></div></div>
    <div class="bar-text">${((pass / (pass + fail)) * 100).toFixed(1)}% 통과</div>
  </div>

  ${categories.map(cat => {
    const items = results.filter(r => r.category === cat);
    const catPass = items.filter(r => r.status === 'PASS').length;
    return `<div class="section">
    <div class="section-title">${cat} (${catPass}/${items.length})</div>
    <table>
      <tr><th>ID</th><th>테스트명</th><th>결과</th><th>시간</th></tr>
      ${items.map(r => `<tr>
        <td>${r.id}</td>
        <td>${r.name}${r.error ? `<div class="error-msg">→ ${r.error}</div>` : ''}</td>
        <td><span class="badge ${r.status.toLowerCase()}">${r.status === 'PASS' ? '✅ PASS' : '❌ FAIL'}</span></td>
        <td class="time">${r.ms}ms</td>
      </tr>`).join('')}
    </table>
  </div>`;
  }).join('')}
</div>
</body>
</html>`;

const reportPath = new URL('./qa-report.html', import.meta.url).pathname;
writeFileSync(reportPath, html);
console.log(`📄 보고서 생성: ${reportPath}`);

process.exit(fail > 0 ? 1 : 0);

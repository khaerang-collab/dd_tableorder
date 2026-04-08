# Business Rules - Unit 1: Backend

---

## 1. 인증/보안 규칙

| ID | 규칙 | 적용 대상 |
|----|------|-----------|
| BR-AUTH-01 | 관리자 로그인 시도 5회 연속 실패 → 15분 계정 잠금 | AdminUser |
| BR-AUTH-02 | JWT 토큰 만료 시간: 16시간 (Customer, Admin 동일) | 전체 |
| BR-AUTH-03 | 비밀번호는 bcrypt로 해싱 저장 (cost factor: 10) | AdminUser |
| BR-AUTH-04 | Customer는 QR/태블릿 접속 시 인증 불필요 (토큰만 발급) | Customer |
| BR-AUTH-05 | Admin API는 role=ADMIN 토큰 필수 | Admin endpoints |
| BR-AUTH-06 | Customer API는 role=CUSTOMER 토큰 필수 | Customer endpoints |
| BR-AUTH-07 | Public API (메뉴 조회, QR 접속)는 토큰 불필요 | Public endpoints |

---

## 2. 테이블/세션 규칙

| ID | 규칙 | 적용 대상 |
|----|------|-----------|
| BR-TABLE-01 | 한 테이블에 ACTIVE 세션은 최대 1개 | TableSession |
| BR-TABLE-02 | 같은 매장 내 테이블 번호는 고유 | RestaurantTable |
| BR-TABLE-03 | 세션은 첫 주문 생성 시 자동 시작 | TableSession |
| BR-TABLE-04 | 세션 종료(이용 완료)는 관리자만 가능 | TableSession |
| BR-TABLE-05 | 이용 완료 시 주문 데이터 → OrderHistory로 아카이브 | OrderHistory |
| BR-TABLE-06 | 이용 완료 후 장바구니 전체 초기화 | Cart |
| BR-TABLE-07 | QR URL 형식: `{baseUrl}/customer?storeId={id}&table={number}` | RestaurantTable |

---

## 3. 메뉴/카테고리 규칙

| ID | 규칙 | 적용 대상 |
|----|------|-----------|
| BR-MENU-01 | 메뉴명 필수, 100자 이하 | Menu |
| BR-MENU-02 | 가격 필수, 0 이상 정수 | Menu |
| BR-MENU-03 | 카테고리 삭제 시 소속 메뉴 존재하면 삭제 불가 | Category |
| BR-MENU-04 | 메뉴/카테고리는 매장 단위 격리 (store_id 기반) | Menu, Category |
| BR-MENU-05 | 고객에게는 is_available=true인 메뉴만 표시 | Menu |
| BR-MENU-06 | display_order로 노출 순서 제어 (오름차순) | Menu, Category |
| BR-MENU-07 | 이미지는 JPG, PNG만 허용, 최대 5MB | Image |
| BR-MENU-08 | 카테고리명 필수, 50자 이하 | Category |

---

## 4. 장바구니 규칙

| ID | 규칙 | 적용 대상 |
|----|------|-----------|
| BR-CART-01 | 장바구니는 테이블 세션 단위로 공유 | Cart |
| BR-CART-02 | 자신이 담은 메뉴만 수정/삭제 가능 (deviceId 검증) | CartItem |
| BR-CART-03 | 같은 기기에서 같은 메뉴를 담으면 수량 누적 (UPSERT) | CartItem |
| BR-CART-04 | 수량은 1 이상이어야 함 | CartItem |
| BR-CART-05 | 장바구니 변경 시 해당 세션 전체에 WebSocket 브로드캐스트 | CartItem |
| BR-CART-06 | 주문 확정 시 장바구니 전체 비우기 (모든 기기) | Cart |
| BR-CART-07 | ACTIVE 세션에서만 장바구니 조작 가능 | Cart |
| BR-CART-08 | 장바구니 데이터는 서버에 저장 (공유를 위해) | Cart |

---

## 5. 주문 규칙

| ID | 규칙 | 적용 대상 |
|----|------|-----------|
| BR-ORDER-01 | 빈 장바구니로 주문 불가 | Order |
| BR-ORDER-02 | 주문 상태 전이: PENDING → PREPARING → COMPLETED (순방향만) | Order |
| BR-ORDER-03 | 주문 삭제는 관리자만 가능 | Order |
| BR-ORDER-04 | 주문 항목에 메뉴명/단가 비정규화 저장 (주문 시점 기준) | OrderItem |
| BR-ORDER-05 | 주문 번호 형식: yyyyMMdd-HHmmss-XXXX (UNIQUE) | Order |
| BR-ORDER-06 | 주문 생성 시 SSE로 관리자에게 실시간 알림 | Order |
| BR-ORDER-07 | 주문 상태 변경 시 SSE + WebSocket으로 실시간 반영 | Order |
| BR-ORDER-08 | ACTIVE 세션에서만 주문 생성 가능 | Order |
| BR-ORDER-09 | 주문 총액 = SUM(수량 * 단가) | Order |

---

## 6. 멀티테넌트 규칙

| ID | 규칙 | 적용 대상 |
|----|------|-----------|
| BR-MT-01 | 모든 데이터 조회/조작은 store_id로 격리 | 전체 |
| BR-MT-02 | 관리자는 자신의 매장 데이터만 접근 가능 | Admin API |
| BR-MT-03 | Customer JWT에 storeId 포함, 해당 매장 데이터만 접근 | Customer API |

---

## 7. 실시간 통신 규칙

| ID | 규칙 | 적용 대상 |
|----|------|-----------|
| BR-RT-01 | WebSocket: 세션 ID 기반 구독 (장바구니 동기화) | WebSocket |
| BR-RT-02 | SSE: 매장 ID 기반 구독 (주문 모니터링) | SSE |
| BR-RT-03 | SSE heartbeat: 30초 간격 | SSE |
| BR-RT-04 | SSE 타임아웃: 16시간 (세션과 동일) | SSE |
| BR-RT-05 | 주문 알림 지연: 2초 이내 | SSE |
| BR-RT-06 | WebSocket 연결/해제 시 접속자 수 업데이트 | WebSocket |

---

## 8. 입력값 검증 규칙

| 대상 | 필드 | 규칙 |
|------|------|------|
| 관리자 로그인 | username | 필수, 2~50자 |
| 관리자 로그인 | password | 필수, 8~100자 |
| 메뉴 등록 | name | 필수, 1~100자 |
| 메뉴 등록 | price | 필수, 0 이상 정수, 최대 10,000,000 |
| 메뉴 등록 | description | 선택, 최대 500자 |
| 카테고리 등록 | name | 필수, 1~50자 |
| 장바구니 추가 | quantity | 필수, 1 이상 정수, 최대 99 |
| 이미지 업로드 | file | JPG/PNG, 최대 5MB |
| 테이블 등록 | tableNumber | 필수, 1 이상 정수 |

---

## 9. 에러 코드 체계

| HTTP | 코드 | 설명 |
|------|------|------|
| 400 | `INVALID_INPUT` | 입력값 검증 실패 |
| 400 | `EMPTY_CART` | 빈 장바구니로 주문 시도 |
| 400 | `INVALID_STATUS_TRANSITION` | 허용되지 않는 주문 상태 변경 |
| 400 | `CATEGORY_HAS_MENUS` | 소속 메뉴가 있는 카테고리 삭제 시도 |
| 401 | `UNAUTHORIZED` | 인증 실패 (토큰 없음/만료/무효) |
| 403 | `FORBIDDEN` | 권한 없음 (다른 기기의 장바구니 수정 등) |
| 404 | `NOT_FOUND` | 리소스 없음 |
| 423 | `ACCOUNT_LOCKED` | 계정 잠금 (로그인 시도 초과) |
| 429 | `RATE_LIMITED` | API 호출 제한 초과 |
| 500 | `INTERNAL_ERROR` | 서버 내부 오류 |

### 에러 응답 형식
```json
{
  "code": "INVALID_INPUT",
  "message": "가격은 0 이상이어야 합니다.",
  "timestamp": "2026-04-08T12:00:00Z"
}
```

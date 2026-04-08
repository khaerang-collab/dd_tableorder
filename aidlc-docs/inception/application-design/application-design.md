# Application Design 통합 문서 - 테이블오더 서비스

---

## 1. 아키텍처 개요

| 계층 | 기술 | 역할 |
|------|------|------|
| **Frontend** | Next.js | 고객 UI (`/customer/*`) + 관리자 UI (`/admin/*`) |
| **Backend** | Spring Boot | REST API + WebSocket + SSE |
| **Database** | PostgreSQL | 데이터 저장 |
| **실시간** | WebSocket (장바구니) + SSE (주문 모니터링) | 양방향/단방향 실시간 통신 |

### 시스템 구성도

```
+------------------+     +------------------+
|  Customer UI     |     |   Admin UI       |
|  (Next.js)       |     |   (Next.js)      |
|  /customer/*     |     |   /admin/*       |
+------+-----------+     +------+-----------+
       |                        |
  REST | WebSocket         REST | SSE
       |                        |
+------v------------------------v-----------+
|         Spring Boot API Server            |
|  +------+ +------+ +------+ +------+     |
|  | Auth | | Menu | | Cart | | Order|     |
|  +------+ +------+ +------+ +------+     |
|  +------+ +--------+                     |
|  | Table| | Common |                     |
|  +------+ +--------+                     |
+-----------+-------------------------------+
            |
     +------v------+
     |  PostgreSQL  |
     +-------------+
```

---

## 2. 백엔드 패키지 구조 (도메인 기반)

```
com.tableorder/
+-- auth/           # 인증/인가 (JWT, 로그인, Security)
+-- store/          # 매장 관리
+-- table/          # 테이블 관리, 세션 라이프사이클, QR
+-- menu/           # 메뉴/카테고리 CRUD, 이미지 업로드
+-- cart/           # 공유 장바구니, WebSocket 동기화
+-- order/          # 주문 생성/관리, SSE 모니터링, 이력
+-- common/         # 전역 예외처리, 보안설정, 필터, 설정
```

각 도메인 패키지 내부:
```
{domain}/
+-- controller/     # REST Controller
+-- service/        # Business Logic
+-- repository/     # Data Access (JPA)
+-- entity/         # JPA Entity
+-- dto/            # Request/Response DTO
```

---

## 3. 핵심 설계 결정

| 결정 사항 | 선택 | 이유 |
|-----------|------|------|
| **장바구니 동기화** | WebSocket | 양방향 실시간 통신, 장바구니 변경 즉시 반영 |
| **주문 모니터링** | SSE | 서버→클라이언트 단방향, 관리자 대시보드에 적합 |
| **패키지 구조** | 도메인 기반 | 도메인 응집도 높음, 향후 마이크로서비스 분리 용이 |
| **인증** | JWT | Stateless, 다중 기기 지원, 16시간 세션 |
| **장바구니 저장** | 서버 (DB) | 테이블 공유를 위해 서버 동기화 필수 |

---

## 4. 데이터 모델 (11개 테이블)

| 엔티티 | 테이블명 | 핵심 필드 |
|--------|----------|-----------|
| Store | `stores` | id, name, address |
| AdminUser | `admin_users` | id, store_id, username, password_hash |
| RestaurantTable | `restaurant_tables` | id, store_id, table_number, qr_url |
| TableSession | `table_sessions` | id, table_id, status, started_at, completed_at |
| Category | `categories` | id, store_id, name, display_order |
| Menu | `menus` | id, category_id, name, price, description, image_url, display_order |
| Cart | `carts` | id, table_session_id |
| CartItem | `cart_items` | id, cart_id, menu_id, quantity, device_id |
| Order | `orders` | id, table_session_id, order_number, total_amount, status |
| OrderItem | `order_items` | id, order_id, menu_id, menu_name, quantity, unit_price |
| OrderHistory | `order_histories` | id, store_id, table_id, session_id, order_data_json, completed_at |

---

## 5. API 엔드포인트 요약

### Public (인증 불필요)
| Method | Path | 설명 |
|--------|------|------|
| POST | `/api/customer/auth/qr-access` | QR 코드 접속 |
| POST | `/api/customer/auth/table-login` | 태블릿 로그인 |
| GET | `/api/stores/{storeId}/menus` | 메뉴 목록 조회 |

### Customer (고객 JWT)
| Method | Path | 설명 |
|--------|------|------|
| GET | `/api/customer/sessions/{id}/cart` | 장바구니 조회 |
| POST | `/api/customer/sessions/{id}/cart/items` | 장바구니 추가 |
| PUT | `/api/customer/cart/items/{id}` | 수량 변경 |
| DELETE | `/api/customer/cart/items/{id}` | 항목 삭제 |
| POST | `/api/customer/sessions/{id}/orders` | 주문 생성 |
| GET | `/api/customer/sessions/{id}/orders` | 주문 내역 |
| WS | `/ws/cart/{sessionId}` | 장바구니 WebSocket |

### Admin (관리자 JWT)
| Method | Path | 설명 |
|--------|------|------|
| POST | `/api/admin/auth/login` | 관리자 로그인 |
| GET | `/api/admin/stores/{id}/orders/stream` | SSE 주문 스트림 |
| PUT | `/api/admin/orders/{id}/status` | 주문 상태 변경 |
| DELETE | `/api/admin/orders/{id}` | 주문 삭제 |
| POST | `/api/admin/stores/{id}/tables` | 테이블 등록 |
| POST | `/api/admin/stores/{id}/tables/{id}/complete` | 이용 완료 |
| CRUD | `/api/admin/stores/{id}/menus` | 메뉴 관리 |
| CRUD | `/api/admin/stores/{id}/categories` | 카테고리 관리 |

---

## 6. 상세 문서 참조

- **컴포넌트 상세**: `components.md`
- **메서드 시그니처**: `component-methods.md`
- **서비스 오케스트레이션**: `services.md`
- **의존성 매핑**: `component-dependency.md`

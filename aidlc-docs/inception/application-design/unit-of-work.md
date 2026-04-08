# Unit of Work - 테이블오더 서비스

---

## 유닛 분해 전략

- **분해 기준**: 기술 스택 경계 (Backend vs Frontend)
- **배포 모델**: 독립 배포 가능한 2개 유닛
- **개발 순서**: Unit 1 (Backend) 완료 → Unit 2 (Frontend) 순차

---

## Unit 1: Backend (Spring Boot + PostgreSQL)

| 항목 | 내용 |
|------|------|
| **이름** | `table-order-backend` |
| **기술 스택** | Java 17+, Spring Boot 3.x, PostgreSQL 16, Gradle |
| **배포 형태** | 단일 Spring Boot JAR (모놀리식) |
| **범위** | REST API, WebSocket, SSE, DB 스키마, 인증/보안 |

### 포함 도메인

| 도메인 | 책임 | 주요 컴포넌트 |
|--------|------|---------------|
| **auth** | JWT 인증/인가, QR 접속, 관리자 로그인, 로그인 시도 제한 | AuthController, AuthService, JwtTokenProvider, JwtAuthFilter |
| **store** | 매장 정보 관리 | StoreController, StoreService, StoreRepository, Store |
| **table** | 테이블 CRUD, QR 생성, 세션 라이프사이클 (시작/종료/이용완료) | TableController, TableService, TableSessionService, RestaurantTable, TableSession |
| **menu** | 메뉴/카테고리 CRUD, 노출 순서, 이미지 업로드 | MenuController, CategoryController, MenuService, CategoryService, ImageService |
| **cart** | 공유 장바구니 CRUD, WebSocket 실시간 동기화 | CartController, CartWebSocketHandler, CartService, Cart, CartItem |
| **order** | 주문 생성/상태 변경/삭제, SSE 모니터링, 이력 관리 | OrderController, OrderSseController, OrderService, OrderHistoryService, Order, OrderItem, OrderHistory |
| **common** | 전역 예외처리, Security 설정, WebSocket 설정, Rate Limiting, 로깅 | GlobalExceptionHandler, SecurityConfig, WebSocketConfig, RateLimitFilter, RequestLoggingFilter |

### 데이터베이스 (PostgreSQL)

| 테이블 | 엔티티 | 설명 |
|--------|--------|------|
| `stores` | Store | 매장 정보 |
| `admin_users` | AdminUser | 관리자 계정 (매장 소속) |
| `restaurant_tables` | RestaurantTable | 테이블 정보 (매장 소속) |
| `table_sessions` | TableSession | 테이블 이용 세션 |
| `categories` | Category | 메뉴 카테고리 (매장 소속) |
| `menus` | Menu | 메뉴 항목 (카테고리 소속) |
| `carts` | Cart | 공유 장바구니 (세션별) |
| `cart_items` | CartItem | 장바구니 항목 (담은 사용자 구분) |
| `orders` | Order | 주문 |
| `order_items` | OrderItem | 주문 항목 |
| `order_histories` | OrderHistory | 과거 주문 이력 |

### 코드 조직 (Greenfield)

```
table-order-backend/
+-- build.gradle
+-- settings.gradle
+-- src/
    +-- main/
    |   +-- java/com/tableorder/
    |   |   +-- TableOrderApplication.java
    |   |   +-- auth/
    |   |   |   +-- controller/
    |   |   |   +-- service/
    |   |   |   +-- repository/
    |   |   |   +-- entity/
    |   |   |   +-- dto/
    |   |   +-- store/
    |   |   |   +-- controller/
    |   |   |   +-- service/
    |   |   |   +-- repository/
    |   |   |   +-- entity/
    |   |   |   +-- dto/
    |   |   +-- table/
    |   |   |   +-- controller/
    |   |   |   +-- service/
    |   |   |   +-- repository/
    |   |   |   +-- entity/
    |   |   |   +-- dto/
    |   |   +-- menu/
    |   |   |   +-- controller/
    |   |   |   +-- service/
    |   |   |   +-- repository/
    |   |   |   +-- entity/
    |   |   |   +-- dto/
    |   |   +-- cart/
    |   |   |   +-- controller/
    |   |   |   +-- service/
    |   |   |   +-- repository/
    |   |   |   +-- entity/
    |   |   |   +-- dto/
    |   |   |   +-- websocket/
    |   |   +-- order/
    |   |   |   +-- controller/
    |   |   |   +-- service/
    |   |   |   +-- repository/
    |   |   |   +-- entity/
    |   |   |   +-- dto/
    |   |   |   +-- sse/
    |   |   +-- common/
    |   |       +-- config/
    |   |       +-- exception/
    |   |       +-- filter/
    |   |       +-- security/
    |   +-- resources/
    |       +-- application.yml
    |       +-- db/migration/       # Flyway 마이그레이션
    +-- test/
        +-- java/com/tableorder/
            +-- auth/
            +-- store/
            +-- table/
            +-- menu/
            +-- cart/
            +-- order/
```

---

## Unit 2: Frontend (Next.js)

| 항목 | 내용 |
|------|------|
| **이름** | `table-order-frontend` |
| **기술 스택** | Next.js 14+ (App Router), TypeScript, Tailwind CSS |
| **배포 형태** | 단일 Next.js 앱 (고객 + 관리자 라우팅 분리) |
| **범위** | 고객 UI, 관리자 UI, WebSocket/SSE 클라이언트, 디자인 시스템 |

### 포함 영역

| 영역 | 책임 | 주요 컴포넌트 |
|------|------|---------------|
| **Customer Pages** (`/customer/*`) | QR 접속, 메뉴 탐색, 장바구니, 주문, 주문 내역 | MenuPage, MenuDetailPage, CartPage, OrderHistoryPage, OrderCompletePage, TableLoginPage |
| **Admin Pages** (`/admin/*`) | 관리자 로그인, 주문 대시보드, 테이블/메뉴 관리 | AdminLoginPage, DashboardPage, TableDetailModal, TableManagePage, MenuManagePage, MenuFormModal, OrderHistoryModal |
| **Shared Components** | 재사용 UI 컴포넌트 | CategoryTabs, MenuListItem, CartFloatingBar, Toast, ConfirmDialog, Badge |
| **Client Services** | API 통신, 실시간 연결, 인증 관리 | WebSocketService, SSEService, AuthService, ApiClient |
| **Design System** | ddocdoc 컬러 토큰, 타이포그래피, 토스오더 참조 디자인 | 색상 변수, 폰트 설정, 공통 스타일 |

### 코드 조직 (Greenfield)

```
table-order-frontend/
+-- package.json
+-- next.config.js
+-- tailwind.config.ts
+-- tsconfig.json
+-- src/
    +-- app/
    |   +-- layout.tsx
    |   +-- customer/
    |   |   +-- layout.tsx
    |   |   +-- page.tsx                    # 메뉴 (홈)
    |   |   +-- menu/[id]/page.tsx          # 메뉴 상세
    |   |   +-- cart/page.tsx               # 장바구니
    |   |   +-- orders/page.tsx             # 주문 내역
    |   |   +-- order-complete/page.tsx     # 주문 완료
    |   |   +-- table-login/page.tsx        # 태블릿 설정
    |   +-- admin/
    |       +-- layout.tsx
    |       +-- login/page.tsx              # 관리자 로그인
    |       +-- dashboard/page.tsx          # 주문 대시보드
    |       +-- tables/page.tsx             # 테이블 관리
    |       +-- menus/page.tsx              # 메뉴 관리
    +-- components/
    |   +-- customer/                       # 고객 전용 컴포넌트
    |   +-- admin/                          # 관리자 전용 컴포넌트
    |   +-- shared/                         # 공통 컴포넌트
    |       +-- CategoryTabs.tsx
    |       +-- MenuListItem.tsx
    |       +-- CartFloatingBar.tsx
    |       +-- Toast.tsx
    |       +-- ConfirmDialog.tsx
    |       +-- Badge.tsx
    +-- services/
    |   +-- api.ts                          # REST API 클라이언트
    |   +-- websocket.ts                    # WebSocket 서비스
    |   +-- sse.ts                          # SSE 서비스
    |   +-- auth.ts                         # 인증 서비스
    +-- hooks/
    |   +-- useCart.ts
    |   +-- useOrders.ts
    |   +-- useWebSocket.ts
    |   +-- useSSE.ts
    +-- types/
    |   +-- menu.ts
    |   +-- cart.ts
    |   +-- order.ts
    |   +-- store.ts
    +-- styles/
    |   +-- globals.css                     # ddocdoc 디자인 토큰
    +-- lib/
        +-- constants.ts
        +-- utils.ts
```

---

## 유닛 요약

| 유닛 | 기술 | 스토리 수 | 개발 순서 |
|------|------|-----------|-----------|
| **Unit 1: Backend** | Spring Boot + PostgreSQL | 23 (전체) | 1차 |
| **Unit 2: Frontend** | Next.js + TypeScript | 23 (전체) | 2차 |

> **참고**: 모든 스토리는 백엔드 API와 프론트엔드 UI 모두에 구현이 필요하므로, 양쪽 유닛에 매핑됩니다.
> Unit 1에서는 API/비즈니스 로직/DB를, Unit 2에서는 UI/클라이언트 로직을 구현합니다.

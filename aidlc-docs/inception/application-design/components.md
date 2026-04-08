# Components - 테이블오더 서비스

---

## 1. Frontend (Next.js)

### 1.1 Customer Pages (`/customer/*`)

| 컴포넌트 | 책임 |
|----------|------|
| **MenuPage** | 메뉴 목록 화면 (홈). 카테고리 탭, 메뉴 리스트, 장바구니 플로팅 바 |
| **MenuDetailPage** | 메뉴 상세 + 수량 선택 + 장바구니 담기 |
| **CartPage** | 공유 장바구니 (내 메뉴/멤버 메뉴 구분), 주문 확정 |
| **OrderHistoryPage** | 현재 세션 주문 내역 조회 |
| **OrderCompletePage** | 주문 완료 안내 (5초 후 메뉴 리다이렉트) |
| **TableLoginPage** | 태블릿 초기 설정 (매장ID + 테이블번호) |

### 1.2 Admin Pages (`/admin/*`)

| 컴포넌트 | 책임 |
|----------|------|
| **AdminLoginPage** | 관리자 로그인 (매장 식별자 + 계정) |
| **DashboardPage** | 테이블별 주문 모니터링 그리드 (SSE 실시간) |
| **TableDetailModal** | 테이블 주문 상세 + 상태 변경 + 주문 삭제 |
| **TableManagePage** | 테이블 초기 설정, QR 생성, 이용 완료 |
| **OrderHistoryModal** | 테이블 과거 주문 내역 조회 |
| **MenuManagePage** | 메뉴 CRUD + 카테고리 CRUD |
| **MenuFormModal** | 메뉴 등록/수정 폼 (이미지 업로드 포함) |

### 1.3 Shared Components

| 컴포넌트 | 책임 |
|----------|------|
| **CategoryTabs** | 상단 고정 카테고리 탭 (Sticky, 수평 스크롤) |
| **MenuListItem** | 메뉴 리스트 아이템 (리스트형: 텍스트 좌 + 썸네일 우) |
| **CartFloatingBar** | 하단 고정 장바구니 바 (총액 + 장바구니 보기) |
| **Toast** | 토스트 알림 (장바구니 변경, 에러 등) |
| **ConfirmDialog** | 확인 팝업 (주문 삭제, 이용 완료 등) |
| **Badge** | 뱃지 (인기/신규/품절) |

### 1.4 Frontend Services (Client-side)

| 서비스 | 책임 |
|--------|------|
| **WebSocketService** | WebSocket 연결 관리 (공유 장바구니 실시간 동기화) |
| **SSEService** | SSE 연결 관리 (관리자 주문 모니터링) |
| **AuthService** | JWT 토큰 관리, 세션 유지/만료 처리 |
| **ApiClient** | REST API 호출 (axios/fetch 래퍼) |

---

## 2. Backend (Spring Boot) - 도메인 기반 패키지

### 2.1 Store 도메인

| 컴포넌트 | 책임 |
|----------|------|
| **StoreController** | 매장 관련 API 엔드포인트 |
| **StoreService** | 매장 비즈니스 로직 |
| **StoreRepository** | 매장 데이터 접근 |
| **Store** | 매장 엔티티 |

### 2.2 Auth 도메인

| 컴포넌트 | 책임 |
|----------|------|
| **AuthController** | 인증 API (관리자 로그인, 테이블 접속) |
| **AuthService** | JWT 발급/검증, 로그인 시도 제한 |
| **AdminUser** | 관리자 계정 엔티티 |
| **JwtTokenProvider** | JWT 토큰 생성/파싱/검증 |
| **JwtAuthFilter** | JWT 인증 필터 (Spring Security) |

### 2.3 Table 도메인

| 컴포넌트 | 책임 |
|----------|------|
| **TableController** | 테이블 관련 API 엔드포인트 |
| **TableService** | 테이블 CRUD, QR 생성, 세션 관리 |
| **TableSessionService** | 테이블 세션 라이프사이클 (시작/종료/이용완료) |
| **TableRepository** | 테이블 데이터 접근 |
| **TableSessionRepository** | 테이블 세션 데이터 접근 |
| **RestaurantTable** | 테이블 엔티티 |
| **TableSession** | 테이블 세션 엔티티 |

### 2.4 Menu 도메인

| 컴포넌트 | 책임 |
|----------|------|
| **MenuController** | 메뉴 관련 API 엔드포인트 |
| **CategoryController** | 카테고리 관련 API 엔드포인트 |
| **MenuService** | 메뉴 CRUD, 노출 순서 관리 |
| **CategoryService** | 카테고리 CRUD, 노출 순서 관리 |
| **ImageService** | 이미지 업로드/저장/조회 |
| **MenuRepository** | 메뉴 데이터 접근 |
| **CategoryRepository** | 카테고리 데이터 접근 |
| **Menu** | 메뉴 엔티티 |
| **Category** | 카테고리 엔티티 |

### 2.5 Cart 도메인

| 컴포넌트 | 책임 |
|----------|------|
| **CartController** | 장바구니 REST API |
| **CartWebSocketHandler** | WebSocket 핸들러 (장바구니 실시간 동기화) |
| **CartService** | 공유 장바구니 비즈니스 로직 (추가/삭제/조회) |
| **CartRepository** | 장바구니 데이터 접근 |
| **Cart / CartItem** | 장바구니 엔티티 |

### 2.6 Order 도메인

| 컴포넌트 | 책임 |
|----------|------|
| **OrderController** | 주문 관련 API 엔드포인트 |
| **OrderSseController** | SSE 주문 실시간 스트림 (관리자) |
| **OrderService** | 주문 생성, 상태 변경, 삭제 |
| **OrderHistoryService** | 이용 완료 시 주문 이력 이동/조회 |
| **OrderRepository** | 주문 데이터 접근 |
| **OrderHistoryRepository** | 주문 이력 데이터 접근 |
| **Order / OrderItem** | 주문 엔티티 |
| **OrderHistory** | 주문 이력 엔티티 |

### 2.7 공통 (Common/Global)

| 컴포넌트 | 책임 |
|----------|------|
| **GlobalExceptionHandler** | 전역 에러 핸들러 (안전한 에러 응답) |
| **SecurityConfig** | Spring Security 설정 (CORS, 헤더, 필터) |
| **WebSocketConfig** | WebSocket 설정 |
| **RateLimitFilter** | Rate Limiting 필터 |
| **RequestLoggingFilter** | 구조화된 로깅 필터 |

---

## 3. Database (PostgreSQL)

| 엔티티 | 테이블명 | 설명 |
|--------|----------|------|
| **Store** | `stores` | 매장 정보 |
| **AdminUser** | `admin_users` | 관리자 계정 |
| **RestaurantTable** | `restaurant_tables` | 테이블 정보 |
| **TableSession** | `table_sessions` | 테이블 세션 |
| **Category** | `categories` | 메뉴 카테고리 |
| **Menu** | `menus` | 메뉴 항목 |
| **Cart** | `carts` | 공유 장바구니 (테이블 세션별) |
| **CartItem** | `cart_items` | 장바구니 항목 (담은 사용자 구분) |
| **Order** | `orders` | 주문 |
| **OrderItem** | `order_items` | 주문 항목 |
| **OrderHistory** | `order_histories` | 과거 주문 이력 |

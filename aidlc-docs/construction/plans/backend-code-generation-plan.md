# Code Generation Plan - Unit 1: Backend (Spring Boot + PostgreSQL)

## Unit Context
- **기술 스택**: Java 17, Spring Boot 3.2, PostgreSQL 16, Gradle
- **패키지 구조**: 도메인 기반 (`com.tableorder.{domain}`)
- **코드 위치**: `table-order-backend/` (workspace root 하위)
- **스토리**: 26개 전체 (백엔드 API + 비즈니스 로직)

## Key Changes from Design Phase
- 카카오 OAuth 2.0 필수 로그인 추가 (CustomerProfile 엔티티)
- 장바구니 수정 권한: 누구나 모든 항목 수정/삭제 가능 (deviceId 소유권 제거)
- 고객 주문 이력 누적, 고객 통계 조회 기능 추가

---

## Generation Steps

### Step 1: Project Structure Setup
- [ ] Gradle 프로젝트 초기화 (build.gradle.kts, settings.gradle.kts)
- [ ] 의존성 설정 (Spring Boot starters, JWT, PostgreSQL, Flyway, QR, OAuth2)
- [ ] application.yml + application-local.yml 설정
- [ ] 메인 클래스 `TableOrderApplication.java`

### Step 2: Database Migration Scripts (Flyway)
- [ ] V1__create_stores.sql
- [ ] V2__create_admin_users.sql
- [ ] V3__create_restaurant_tables.sql
- [ ] V4__create_table_sessions.sql
- [ ] V5__create_categories.sql
- [ ] V6__create_menus.sql
- [ ] V7__create_carts_and_cart_items.sql
- [ ] V8__create_orders_and_order_items.sql
- [ ] V9__create_order_histories.sql
- [ ] V10__create_customer_profiles.sql

### Step 3: Common Layer
- [ ] GlobalExceptionHandler + 커스텀 예외 계층
- [ ] ErrorResponse DTO
- [ ] SecurityConfig (Spring Security, CORS, 헤더, 경로별 접근 제어)
- [ ] JwtTokenProvider (HS256, 생성/검증/파싱)
- [ ] JwtAuthFilter (OncePerRequestFilter)
- [ ] RateLimitFilter (Token Bucket)
- [ ] RequestLoggingFilter (MDC requestId)
- [ ] WebSocketConfig

### Step 4: Store Domain
- [ ] Store 엔티티
- [ ] StoreRepository
- [ ] StoreService
- [ ] StoreController (관리자: 매장 정보 조회)
- [ ] Store DTOs

### Step 5: Auth Domain (관리자 + 카카오 OAuth)
- [ ] AdminUser 엔티티
- [ ] AdminUserRepository
- [ ] AuthService (관리자 로그인, brute-force 방어)
- [ ] KakaoOAuthService (카카오 토큰 교환, 프로필 조회)
- [ ] AuthController (관리자 로그인, QR 접속 + 카카오 OAuth, 태블릿 로그인)
- [ ] Auth DTOs (LoginRequest/Response, QrAccessRequest, KakaoCallbackRequest)

### Step 6: Customer Profile Domain
- [ ] CustomerProfile 엔티티 (kakaoId, nickname, gender, ageRange, profileImage, visitCount, totalOrderAmount)
- [ ] CustomerProfileRepository
- [ ] CustomerProfileService (프로필 생성/조회/업데이트, 통계)
- [ ] CustomerProfileController (관리자: 고객 목록, 통계)
- [ ] CustomerProfile DTOs

### Step 7: Table Domain
- [ ] RestaurantTable 엔티티
- [ ] TableSession 엔티티
- [ ] TableRepository, TableSessionRepository
- [ ] TableService (테이블 CRUD, QR 생성)
- [ ] TableSessionService (세션 라이프사이클: 생성/종료/이용완료, 접속자 관리)
- [ ] TableController (관리자: 테이블 관리, 이용 완료)
- [ ] Table DTOs

### Step 8: Menu Domain
- [ ] Category 엔티티
- [ ] Menu 엔티티
- [ ] CategoryRepository, MenuRepository
- [ ] CategoryService (CRUD, 순서 관리)
- [ ] MenuService (CRUD, 순서 관리)
- [ ] ImageService (파일 업로드/저장)
- [ ] CategoryController, MenuController
- [ ] ImageController (이미지 서빙)
- [ ] Menu/Category DTOs

### Step 9: Cart Domain (WebSocket 포함)
- [ ] Cart 엔티티
- [ ] CartItem 엔티티 (customerProfileId 참조)
- [ ] CartRepository, CartItemRepository
- [ ] CartService (조회/추가/수량변경/삭제/비우기 - 소유권 검증 없음)
- [ ] CartController (REST API)
- [ ] CartWebSocketHandler (브로드캐스트, 접속자 수 관리)
- [ ] Cart DTOs (아바타 아이콘 포함)

### Step 10: Order Domain (SSE 포함)
- [ ] Order 엔티티
- [ ] OrderItem 엔티티
- [ ] OrderHistory 엔티티
- [ ] OrderRepository, OrderItemRepository, OrderHistoryRepository
- [ ] OrderService (생성, 상태 변경, 삭제, 주문번호 생성)
- [ ] OrderHistoryService (아카이브, 조회)
- [ ] OrderSseService (SseEmitter 관리, heartbeat)
- [ ] OrderController (고객: 주문 생성/내역, 관리자: 상태변경/삭제/대시보드)
- [ ] OrderSseController (관리자: SSE 스트림)
- [ ] Order DTOs

### Step 11: Unit Tests
- [ ] AuthService 테스트 (관리자 로그인, brute-force, JWT)
- [ ] CartService 테스트 (CRUD, 장바구니 비우기)
- [ ] OrderService 테스트 (주문 생성, 상태 전이, 삭제)
- [ ] TableSessionService 테스트 (세션 라이프사이클)
- [ ] MenuService 테스트 (CRUD, 순서)
- [ ] JwtTokenProvider 테스트

### Step 12: Documentation & Deployment
- [ ] `aidlc-docs/construction/backend/code/backend-code-summary.md`
- [ ] Dockerfile
- [ ] .gitignore 업데이트
- [ ] data.sql (초기 테스트 데이터)

---

## Story Coverage

| Step | Stories |
|------|---------|
| Step 5 | US-6.1 (관리자 로그인), US-1.1 (QR 접속), US-11.1 (카카오 로그인) |
| Step 6 | US-11.1, US-11.3 (주문 이력 누적), US-12.1 (고객 통계) |
| Step 7 | US-1.2 (동시 접속), US-1.3 (태블릿), US-8.1 (테이블 설정), US-8.3 (이용 완료) |
| Step 8 | US-2.1 (메뉴 탐색), US-2.2 (메뉴 상세), US-9.1 (메뉴 CRUD), US-9.2 (카테고리 CRUD), US-10.1 (이미지) |
| Step 9 | US-3.1 (장바구니), US-3.2 (실시간 동기화) |
| Step 10 | US-4.1 (주문 확정), US-4.2 (실패 처리), US-5.1 (내역 조회), US-5.2 (상태 업데이트), US-7.1~7.3 (대시보드), US-8.2 (주문 삭제), US-8.4 (과거 내역) |
| Step 11 | 전체 스토리 테스트 커버리지 |

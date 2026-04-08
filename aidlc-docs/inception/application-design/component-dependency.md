# Component Dependencies - 테이블오더 서비스

---

## 시스템 구성 의존성

```
+-------------------+        +-------------------+
|   Customer UI     |        |    Admin UI       |
|   (Next.js)       |        |    (Next.js)      |
+--------+----------+        +--------+----------+
         |                            |
    REST | WebSocket             REST | SSE
         |                            |
+--------v----------------------------v----------+
|           Spring Boot API Server               |
|                                                |
|  +----------+  +----------+  +----------+     |
|  |   Auth   |  |  Table   |  |   Menu   |     |
|  |  Domain  |  |  Domain  |  |  Domain  |     |
|  +----+-----+  +----+-----+  +----+-----+     |
|       |              |              |           |
|  +----v-----+  +-----v----+               |    |
|  |   Cart   |  |  Order   |               |    |
|  |  Domain  |  |  Domain  |               |    |
|  +----+-----+  +----+-----+               |    |
|       |              |                     |    |
+-------+--------------+---------------------+---+
         |              |
    +----v--------------v----+
    |      PostgreSQL        |
    +------------------------+
```

---

## 도메인 간 의존성 매트릭스

| 도메인 (의존하는) → | Auth | Store | Table | Menu | Cart | Order |
|---------------------|------|-------|-------|------|------|-------|
| **Auth** | - | Read | Read | - | - | - |
| **Store** | - | - | - | - | - | - |
| **Table** | - | Read | - | - | Clear | Archive |
| **Menu** | - | Read | - | - | - | - |
| **Cart** | Verify | - | Read | Read | - | - |
| **Order** | Verify | Read | Read | Read | Read+Clear | - |

### 의존성 설명

| 관계 | 설명 |
|------|------|
| Auth → Store | 매장 식별자로 매장 존재 확인 |
| Auth → Table | 테이블 접속 시 테이블 존재 확인 |
| Table → Cart | 이용 완료 시 장바구니 비우기 |
| Table → Order | 이용 완료 시 주문 이력 이동 |
| Menu → Store | 매장 소속 메뉴 관리 |
| Cart → Auth | 요청자 deviceId 검증 (본인 메뉴만 수정) |
| Cart → Table | 테이블 세션 유효성 확인 |
| Cart → Menu | 메뉴 정보 조회 (메뉴명, 가격) |
| Order → Auth | 요청자 인증 확인 |
| Order → Store | 매장 소속 확인 |
| Order → Table | 테이블/세션 유효성 확인 |
| Order → Menu | 주문 항목 메뉴 정보 참조 |
| Order → Cart | 장바구니 → 주문 변환 시 장바구니 조회 + 비우기 |

---

## 실시간 통신 의존성

```
+------------------+     WebSocket      +---------------------+
| Customer Device  | <================> | CartWebSocketHandler |
| (Browser)        |                    | (Spring Boot)       |
+------------------+                    +----------+----------+
                                                   |
                                          +--------v--------+
                                          |   CartService   |
                                          +-----------------+

+------------------+       SSE          +---------------------+
| Admin Browser    | <================= | OrderSseController  |
| (Dashboard)      |                    | (Spring Boot)       |
+------------------+                    +----------+----------+
                                                   |
                                          +--------v--------+
                                          |  OrderService   |
                                          +-----------------+
```

---

## 데이터 흐름

### 고객 주문 데이터 흐름
```
QR Scan → Auth(JWT) → Menu(조회) → Cart(담기/WebSocket동기화) → Order(확정) → Admin(SSE알림)
```

### 관리자 관리 데이터 흐름
```
Login → Auth(JWT) → Dashboard(SSE구독) → Order(상태변경) → Table(이용완료) → OrderHistory(이력이동)
```

---

## 외부 의존성

| 의존성 | 용도 | 비고 |
|--------|------|------|
| **PostgreSQL** | 데이터 저장 | 모든 도메인 |
| **Spring Security** | 인증/인가 | Auth 도메인 |
| **Spring WebSocket** | 실시간 장바구니 | Cart 도메인 |
| **Spring SSE (SseEmitter)** | 실시간 주문 모니터링 | Order 도메인 |
| **BCrypt** | 비밀번호 해싱 | Auth 도메인 |
| **JJWT** | JWT 토큰 처리 | Auth 도메인 |
| **QR Code Library** | QR 코드 생성 | Table 도메인 |

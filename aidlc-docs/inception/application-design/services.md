# Services - 테이블오더 서비스

---

## 서비스 아키텍처 개요

```
[Customer UI] ←→ [WebSocket] ←→ [Spring Boot API] ←→ [PostgreSQL]
[Customer UI] ←→ [REST API]  ←→ [Spring Boot API] ←→ [PostgreSQL]
[Admin UI]    ←→ [REST API]  ←→ [Spring Boot API] ←→ [PostgreSQL]
[Admin UI]    ←  [SSE]       ←  [Spring Boot API]
```

| 통신 방식 | 용도 | 방향 |
|-----------|------|------|
| **REST API** | 메뉴 조회, 주문 생성, CRUD 등 일반 요청 | 양방향 (요청-응답) |
| **WebSocket** | 공유 장바구니 실시간 동기화 | 양방향 (실시간) |
| **SSE** | 관리자 주문 모니터링 실시간 알림 | 단방향 (서버→클라이언트) |

---

## 핵심 서비스 오케스트레이션

### 1. QR 접속 플로우
```
Customer QR Scan
  → AuthService.accessByQr(storeId, tableNumber)
    → TableSessionService.getOrCreateSession(tableId)
    → TableSessionService.registerUser(tableId, deviceId)
    → JwtTokenProvider.createToken(customerSession)
  → WebSocketService.connect(tableSessionId)
  → Return: JWT + 메뉴 화면
```

### 2. 공유 장바구니 플로우
```
Customer adds menu
  → CartController.addToCart(sessionId, menuId, quantity)
    → CartService.addItem(sessionId, menuId, quantity, deviceId)
    → CartRepository.save(cartItem)
    → CartWebSocketHandler.broadcastCartUpdate(sessionId, ADD_EVENT)
  → All connected clients receive WebSocket message
  → UI updates: 내 메뉴/멤버 메뉴 갱신 + 토스트 알림
```

### 3. 주문 생성 플로우
```
Customer confirms order
  → OrderController.createOrder(sessionId)
    → OrderService.createOrder(sessionId)
      → CartService.getCart(sessionId)     // 장바구니 조회
      → Order 엔티티 생성 + OrderItem 매핑
      → OrderRepository.save(order)
      → CartService.clearCart(sessionId)   // 장바구니 비우기
      → CartWebSocketHandler.broadcastCartUpdate(sessionId, CLEAR_EVENT)
      → SseEmitter.send(newOrderEvent)     // 관리자에게 SSE 알림
  → Return: OrderResponse (주문 번호)
```

### 4. 관리자 실시간 모니터링 플로우
```
Admin opens dashboard
  → OrderSseController.streamOrders(storeId)
    → SseEmitter 생성 + 등록
  
When new order arrives:
  → OrderService.createOrder() triggers
    → SseEmitter.send(OrderEvent.NEW_ORDER, orderData)
  → Admin UI updates: 해당 테이블 카드 강조 + 주문 추가

When status changes:
  → OrderService.updateStatus() triggers
    → SseEmitter.send(OrderEvent.STATUS_CHANGED, orderData)
  → Admin UI updates: 상태 뱃지 변경
  → Customer UI도 주문 상태 갱신 (WebSocket 또는 polling)
```

### 5. 이용 완료 플로우
```
Admin completes table
  → TableController.completeTable(storeId, tableId)
    → TableSessionService.completeSession(tableId)
      → OrderHistoryService.archiveOrders(sessionId)  // 주문→이력 이동
      → CartService.clearCart(sessionId)               // 장바구니 비우기
      → TableSession.status = COMPLETED                // 세션 종료
      → CartWebSocketHandler.broadcastSessionComplete(sessionId)
  → Customer devices: 세션 종료 안내 표시
  → Admin dashboard: 테이블 카드 리셋 (주문 0, 총액 0)
```

---

## 실시간 통신 서비스 상세

### WebSocket Service (공유 장바구니)

| 이벤트 타입 | 방향 | Payload | 설명 |
|-------------|------|---------|------|
| `CART_ITEM_ADDED` | Server→Clients | `{deviceId, menuName, quantity}` | 멤버가 메뉴 추가 |
| `CART_ITEM_REMOVED` | Server→Clients | `{deviceId, menuName}` | 멤버가 메뉴 삭제 |
| `CART_ITEM_UPDATED` | Server→Clients | `{deviceId, menuName, quantity}` | 멤버가 수량 변경 |
| `CART_CLEARED` | Server→Clients | `{reason: "ORDER_PLACED"}` | 주문 확정으로 장바구니 비움 |
| `SESSION_COMPLETED` | Server→Clients | `{}` | 관리자가 이용 완료 처리 |
| `USER_JOINED` | Server→Clients | `{activeUserCount}` | 접속자 수 변경 |
| `USER_LEFT` | Server→Clients | `{activeUserCount}` | 접속자 수 변경 |

### SSE Service (관리자 모니터링)

| 이벤트 타입 | Payload | 설명 |
|-------------|---------|------|
| `NEW_ORDER` | `{orderId, tableNumber, items, totalAmount}` | 신규 주문 접수 |
| `ORDER_STATUS_CHANGED` | `{orderId, tableNumber, newStatus}` | 주문 상태 변경 |
| `ORDER_DELETED` | `{orderId, tableNumber}` | 주문 삭제 |
| `TABLE_COMPLETED` | `{tableNumber}` | 테이블 이용 완료 |

---

## 보안 서비스

### JWT 인증 흐름
```
Request → JwtAuthFilter
  → Token 추출 (Authorization: Bearer ...)
  → JwtTokenProvider.validateToken(token)
  → SecurityContext에 인증 정보 설정
  → Controller 진입

Public endpoints (인증 불필요):
  - POST /api/customer/auth/qr-access
  - GET  /api/stores/{storeId}/menus
  - POST /api/customer/auth/table-login

Admin endpoints (관리자 JWT 필요):
  - /api/admin/**

Customer endpoints (고객 JWT 필요):
  - /api/customer/** (qr-access, table-login 제외)
```

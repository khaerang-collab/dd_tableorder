# Business Logic Model - Unit 1: Backend

---

## 1. 인증 (Auth) 로직

### 1.1 QR 코드 접속 (Customer)
```
INPUT: storeId, tableNumber
PROCESS:
  1. Store 존재 확인 → 없으면 404
  2. Table 존재 확인 (storeId + tableNumber) → 없으면 404
  3. ACTIVE 세션 조회 또는 생성:
     - ACTIVE 세션 있으면 해당 세션 사용
     - 없으면 새 세션은 생성하지 않음 (첫 주문 시 자동 생성)
     - 단, 장바구니/메뉴 조회를 위해 임시 세션 ID 할당
  4. deviceId 생성 (UUID, 클라이언트에서 생성 후 전달 또는 서버 발급)
  5. activeUserCount 증가
  6. WebSocket으로 USER_JOINED 이벤트 브로드캐스트
  7. JWT 토큰 발급 (payload: storeId, tableId, sessionId, deviceId, role=CUSTOMER)
OUTPUT: { token, storeId, storeName, tableNumber, sessionId, deviceId }
```

### 1.2 태블릿 로그인 (Customer)
```
INPUT: storeId, tableNumber
PROCESS:
  1. QR 코드 접속과 동일한 로직
  2. 추가: 태블릿 식별을 위한 고정 deviceId 사용
OUTPUT: { token, storeId, storeName, tableNumber, sessionId, deviceId }
```

### 1.3 관리자 로그인 (Admin)
```
INPUT: storeIdentifier, username, password
PROCESS:
  1. Store 조회 (storeIdentifier) → 없으면 401
  2. AdminUser 조회 (storeId + username) → 없으면 401
  3. 계정 잠금 확인:
     - locked_until > NOW() → 423 "계정이 잠겼습니다. N분 후 재시도"
  4. 비밀번호 검증 (bcrypt.matches):
     - 실패: login_attempts++
       - 5회 연속 실패 → locked_until = NOW() + 15분
     - 성공: login_attempts = 0, locked_until = null
  5. JWT 토큰 발급 (payload: storeId, adminUserId, role=ADMIN, exp=16h)
OUTPUT: { token, storeId, storeName, adminUsername }
```

### 1.4 JWT 토큰 관리
```
토큰 구조:
  - header: { alg: HS256, typ: JWT }
  - payload: { sub, storeId, role, deviceId(customer), iat, exp }
  - Customer 만료: 16시간
  - Admin 만료: 16시간

검증 로직:
  1. Authorization 헤더에서 "Bearer " 추출
  2. 서명 검증 → 실패시 401
  3. 만료 확인 → 만료시 401
  4. SecurityContext에 인증 정보 설정
```

---

## 2. 테이블 세션 라이프사이클

### 2.1 세션 상태 머신
```
[없음] ──(첫 주문 생성)──> [ACTIVE]
[ACTIVE] ──(관리자 이용 완료)──> [COMPLETED]
```

### 2.2 세션 시작 (자동)
```
TRIGGER: 첫 주문 생성 시
PROCESS:
  1. 테이블의 ACTIVE 세션 확인
  2. 없으면 새 TableSession 생성 (status=ACTIVE)
  3. Cart 자동 생성 (1:1)
  4. 세션 ID 반환
```

### 2.3 세션 종료 (이용 완료)
```
INPUT: storeId, tableId
PROCESS:
  1. 테이블의 ACTIVE 세션 조회 → 없으면 404
  2. 해당 세션의 모든 주문 조회
  3. 주문 데이터를 JSON으로 직렬화
  4. OrderHistory 레코드 생성
  5. 장바구니 비우기 (CartItem 전체 삭제 → Cart 삭제)
  6. 세션 상태 → COMPLETED, completed_at = NOW()
  7. WebSocket으로 SESSION_COMPLETED 이벤트 브로드캐스트
  8. SSE로 TABLE_COMPLETED 이벤트 발행
OUTPUT: { success: true }
```

### 2.4 접속자 수 관리
```
접속자 증가:
  TRIGGER: WebSocket 연결 시
  PROCESS: activeUserCount++ → USER_JOINED 이벤트 (activeUserCount 포함)

접속자 감소:
  TRIGGER: WebSocket 연결 해제 시
  PROCESS: activeUserCount-- → USER_LEFT 이벤트 (activeUserCount 포함)
```

---

## 3. 메뉴 관리 로직

### 3.1 메뉴 목록 조회 (Customer)
```
INPUT: storeId
PROCESS:
  1. Store 존재 확인
  2. 카테고리 목록 조회 (display_order 순)
  3. 카테고리별 메뉴 조회 (display_order 순, is_available=true만)
  4. 카테고리+메뉴 계층 구조로 응답
OUTPUT: [{ categoryId, categoryName, menus: [{ id, name, price, description, imageUrl, badgeType }] }]
```

### 3.2 메뉴 CRUD (Admin)
```
CREATE:
  INPUT: storeId, categoryId, name, price, description?, imageUrl?, badgeType?
  VALIDATE: name 필수, price >= 0, categoryId는 해당 store 소속
  PROCESS: display_order = 기존 최대값 + 1

UPDATE:
  INPUT: menuId, 변경 필드들
  VALIDATE: 동일 검증, menuId는 해당 store 소속 확인
  
DELETE:
  INPUT: menuId
  PROCESS: 해당 메뉴 삭제 (장바구니에 있으면 장바구니에서도 제거)

순서 변경:
  INPUT: storeId, categoryId, orderedMenuIds[]
  PROCESS: 배열 순서대로 display_order 재할당 (0, 1, 2, ...)
```

### 3.3 카테고리 CRUD (Admin)
```
CREATE:
  INPUT: storeId, name
  PROCESS: display_order = 기존 최대값 + 1

UPDATE:
  INPUT: categoryId, name
  VALIDATE: categoryId는 해당 store 소속

DELETE:
  INPUT: categoryId
  VALIDATE: 소속 메뉴가 있으면 삭제 불가 → 400 "소속 메뉴를 먼저 삭제하세요"

순서 변경:
  INPUT: storeId, orderedCategoryIds[]
  PROCESS: 배열 순서대로 display_order 재할당
```

---

## 4. 공유 장바구니 로직

### 4.1 장바구니 조회
```
INPUT: sessionId, deviceId
PROCESS:
  1. 세션의 Cart 조회 (없으면 빈 장바구니 반환)
  2. CartItem 목록 조회 (Menu 정보 JOIN)
  3. 각 항목에 isOwner 플래그 추가 (item.deviceId == 요청 deviceId)
  4. 그룹핑: myItems (isOwner=true), memberItems (isOwner=false)
  5. 총 금액 계산: SUM(item.quantity * menu.price)
OUTPUT: { myItems[], memberItems[], totalAmount }
```

### 4.2 장바구니 항목 추가
```
INPUT: sessionId, menuId, quantity, deviceId
PROCESS:
  1. 세션 ACTIVE 확인
  2. Menu 존재 및 is_available 확인
  3. Cart 조회 (없으면 생성)
  4. 동일 cart_id + menu_id + device_id 항목 확인:
     - 있으면: quantity += 입력 quantity (UPSERT)
     - 없으면: 새 CartItem 생성
  5. WebSocket 브로드캐스트: CART_ITEM_ADDED { deviceId, menuName, quantity }
OUTPUT: { cartItem, totalAmount }
```

### 4.3 장바구니 항목 수량 변경
```
INPUT: cartItemId, quantity, deviceId
PROCESS:
  1. CartItem 조회
  2. 소유권 확인 (item.deviceId == deviceId) → 불일치시 403
  3. quantity 업데이트
  4. WebSocket 브로드캐스트: CART_ITEM_UPDATED { deviceId, menuName, quantity }
OUTPUT: { cartItem, totalAmount }
```

### 4.4 장바구니 항목 삭제
```
INPUT: cartItemId, deviceId
PROCESS:
  1. CartItem 조회
  2. 소유권 확인 → 불일치시 403
  3. CartItem 삭제
  4. WebSocket 브로드캐스트: CART_ITEM_REMOVED { deviceId, menuName }
OUTPUT: { success, totalAmount }
```

### 4.5 장바구니 비우기 (내 메뉴만)
```
INPUT: sessionId, deviceId
PROCESS:
  1. Cart 조회
  2. device_id == deviceId인 CartItem 전체 삭제
  3. WebSocket 브로드캐스트
OUTPUT: { success, totalAmount }
```

---

## 5. 주문 로직

### 5.1 주문 생성
```
INPUT: sessionId
PROCESS:
  1. 세션 ACTIVE 확인
  2. 장바구니 조회 → 비어있으면 400 "장바구니가 비어 있습니다"
  3. @Transactional:
     a. 주문 번호 생성 (yyyyMMdd-HHmmss-XXXX 형식, XXXX=랜덤 4자리)
     b. Order 생성 (status=PENDING)
     c. CartItem → OrderItem 변환 (메뉴명/단가 비정규화 복사)
     d. totalAmount 계산: SUM(quantity * unit_price)
     e. 장바구니 전체 비우기 (모든 기기의 CartItem 삭제)
  4. WebSocket 브로드캐스트: CART_CLEARED { reason: "ORDER_PLACED" }
  5. SSE 발행: NEW_ORDER { orderId, tableNumber, items, totalAmount }
OUTPUT: { orderId, orderNumber, totalAmount, status }
```

### 5.2 주문 상태 변경 (Admin)
```
INPUT: orderId, newStatus
PROCESS:
  1. Order 조회 → 없으면 404
  2. 상태 전이 검증:
     - PENDING → PREPARING ✅
     - PREPARING → COMPLETED ✅
     - 그 외 → 400 "허용되지 않는 상태 변경"
  3. status 업데이트
  4. SSE 발행: ORDER_STATUS_CHANGED { orderId, tableNumber, newStatus }
OUTPUT: { orderId, status }
```

### 5.3 주문 삭제 (Admin)
```
INPUT: orderId
PROCESS:
  1. Order 조회 (OrderItem 포함)
  2. 확인 (이미 프론트에서 확인 팝업 처리)
  3. @Transactional:
     a. OrderItem 전체 삭제
     b. Order 삭제
  4. SSE 발행: ORDER_DELETED { orderId, tableNumber }
OUTPUT: { success }
```

### 5.4 주문 내역 조회 (Customer)
```
INPUT: sessionId
PROCESS:
  1. 세션의 주문 목록 조회 (created_at ASC)
  2. 각 주문의 OrderItem 포함
OUTPUT: [{ orderId, orderNumber, items[], totalAmount, status, createdAt }]
```

### 5.5 주문 대시보드 조회 (Admin)
```
INPUT: storeId
PROCESS:
  1. Store의 모든 테이블 조회
  2. 각 테이블의 ACTIVE 세션 조회
  3. 각 세션의 주문 목록 조회
  4. 테이블별 그룹핑: { tableNumber, totalOrderAmount, orders[], activeSession }
OUTPUT: [{ tableId, tableNumber, sessionId, totalAmount, recentOrders[], orderCount }]
```

---

## 6. 이용 완료 / 이력 관리 로직

### 6.1 과거 주문 내역 조회 (Admin)
```
INPUT: storeId, tableId?, startDate?, endDate?
PROCESS:
  1. OrderHistory 조회 (store_id 필터)
  2. 선택적 필터: table_id, 날짜 범위
  3. completed_at DESC 정렬
  4. 페이지네이션 적용
OUTPUT: [{ historyId, tableNumber, orderDataJson, totalAmount, sessionStartedAt, completedAt }]
```

---

## 7. 이미지 업로드 로직

### 7.1 이미지 업로드
```
INPUT: MultipartFile (이미지)
PROCESS:
  1. 파일 타입 검증 (JPG, PNG만 허용)
  2. 파일 크기 검증 (최대 5MB)
  3. 고유 파일명 생성: {UUID}.{확장자}
  4. 서버 로컬 디렉토리에 저장 (/uploads/images/)
  5. 접근 가능한 URL 반환 (/api/images/{filename})
OUTPUT: { imageUrl }
```

---

## 8. 실시간 통신 로직

### 8.1 WebSocket (장바구니 동기화)
```
연결: /ws/cart/{sessionId}
  - 연결 시: activeUserCount++, USER_JOINED 브로드캐스트
  - 해제 시: activeUserCount--, USER_LEFT 브로드캐스트

메시지 방향: Server → Client (브로드캐스트)
  - 장바구니 변경 시 해당 세션의 모든 연결에 이벤트 전송
  - 발신자 포함 전체 브로드캐스트 (클라이언트에서 자신 이벤트 필터링)
```

### 8.2 SSE (관리자 주문 모니터링)
```
연결: GET /api/admin/stores/{storeId}/orders/stream
  - SseEmitter 생성 (timeout=16시간)
  - 매장별 emitter 목록 관리

이벤트 발행:
  - 주문 생성/상태 변경/삭제/이용 완료 시
  - 해당 매장의 모든 emitter에 이벤트 전송

연결 관리:
  - 타임아웃/에러 시 emitter 제거
  - 주기적 heartbeat (30초 간격) 전송으로 연결 유지
```

---

## 9. 주문 번호 생성 알고리즘

```
형식: yyyyMMdd-HHmmss-XXXX
  - yyyyMMdd: 날짜 (20260408)
  - HHmmss: 시각 (143025)
  - XXXX: 랜덤 4자리 숫자 (0000~9999)
예시: 20260408-143025-7291

충돌 처리:
  - UNIQUE 제약으로 DB 레벨 보호
  - 충돌 시 재생성 (최대 3회 시도)
```

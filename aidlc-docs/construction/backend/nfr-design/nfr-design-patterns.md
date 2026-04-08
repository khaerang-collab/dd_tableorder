# NFR Design Patterns - Unit 1: Backend

---

## 1. 인증/인가 패턴

### 1.1 JWT Filter Chain 패턴
```
HTTP Request
  → RateLimitFilter (공개 API rate limiting)
  → RequestLoggingFilter (구조화 로깅)
  → JwtAuthFilter (토큰 검증 + SecurityContext 설정)
  → Controller (비즈니스 로직)
```

**구현**:
- `OncePerRequestFilter` 상속
- Public 경로 (QR 접속, 메뉴 조회) → 필터 스킵
- Customer 경로 → role=CUSTOMER 검증
- Admin 경로 → role=ADMIN 검증

### 1.2 IDOR 방지 패턴
```
모든 리소스 접근:
  1. JWT에서 storeId/sessionId 추출
  2. 요청 리소스의 소유 매장/세션 확인
  3. 불일치 시 403 Forbidden
```

**적용 대상**:
- Admin API: `jwt.storeId == resource.storeId`
- Customer API: `jwt.sessionId == resource.tableSessionId`
- Cart API: `jwt.deviceId == cartItem.deviceId` (수정/삭제 시)

---

## 2. 실시간 통신 패턴

### 2.1 WebSocket 브로드캐스트 패턴
```
CartWebSocketHandler:
  - sessions: Map<Long, Set<WebSocketSession>>  (sessionId → 연결 목록)
  
  onConnect(sessionId, wsSession):
    sessions.computeIfAbsent(sessionId, ConcurrentHashMap.newKeySet()).add(wsSession)
    activeUserCount++
    broadcast(sessionId, USER_JOINED)
    
  onDisconnect(sessionId, wsSession):
    sessions.get(sessionId).remove(wsSession)
    activeUserCount--
    broadcast(sessionId, USER_LEFT)
    
  broadcast(sessionId, event):
    for each session in sessions.get(sessionId):
      session.sendMessage(TextMessage(json(event)))
```

### 2.2 SSE Emitter 관리 패턴
```
OrderSseService:
  - emitters: Map<Long, List<SseEmitter>>  (storeId → emitter 목록)
  
  subscribe(storeId):
    emitter = new SseEmitter(16h)
    emitters.computeIfAbsent(storeId, CopyOnWriteArrayList::new).add(emitter)
    emitter.onCompletion(() -> remove(storeId, emitter))
    emitter.onTimeout(() -> remove(storeId, emitter))
    emitter.onError(() -> remove(storeId, emitter))
    // heartbeat 스케줄링
    return emitter
    
  publish(storeId, event):
    deadEmitters = []
    for each emitter in emitters.get(storeId):
      try: emitter.send(event)
      catch: deadEmitters.add(emitter)
    remove deadEmitters
```

### 2.3 Heartbeat 패턴
```
@Scheduled(fixedRate = 30000)  // 30초
heartbeat():
  for each (storeId, emitterList) in emitters:
    for each emitter in emitterList:
      try: emitter.send(SseEmitter.event().comment("heartbeat"))
      catch: remove dead emitter
```

---

## 3. 데이터 접근 패턴

### 3.1 멀티테넌트 쿼리 패턴
```java
// Repository 레벨에서 store_id 필터링 강제
@Query("SELECT m FROM Menu m WHERE m.category.store.id = :storeId")
List<Menu> findByStoreId(@Param("storeId") Long storeId);

// Service 레벨에서 소유권 검증
public Menu getMenu(Long menuId, Long storeId) {
    Menu menu = menuRepository.findById(menuId)
        .orElseThrow(() -> new NotFoundException("Menu not found"));
    if (!menu.getCategory().getStore().getId().equals(storeId)) {
        throw new ForbiddenException("Access denied");
    }
    return menu;
}
```

### 3.2 트랜잭션 패턴
```
주문 생성 (Critical Path):
  @Transactional(isolation = READ_COMMITTED)
  - 장바구니 조회
  - 주문 생성
  - 주문 항목 생성
  - 장바구니 비우기
  → 실패 시 전체 롤백

이용 완료 (Critical Path):
  @Transactional
  - 주문 이력 아카이브
  - 장바구니 삭제
  - 세션 종료
  → 실패 시 전체 롤백
```

### 3.3 비정규화 패턴
```
OrderItem에 메뉴명/단가 복사:
  - menu_name: 주문 시점 메뉴명
  - unit_price: 주문 시점 가격
  → 메뉴 수정/삭제 후에도 주문 이력 정확성 보장

OrderHistory에 JSON 직렬화:
  - order_data_json: 세션 전체 주문 데이터
  → 이력 조회 시 단일 쿼리로 전체 데이터 반환
```

---

## 4. 보안 패턴

### 4.1 Rate Limiting 패턴
```
RateLimitFilter:
  - store: ConcurrentHashMap<String, TokenBucket>  (clientIP → bucket)
  
  doFilter(request):
    clientIp = request.getRemoteAddr()
    bucket = store.computeIfAbsent(clientIp, () -> new TokenBucket(60, 1min))
    if (!bucket.tryConsume()):
      response.status = 429
      return
    chain.doFilter(request, response)

  @Scheduled(fixedRate = 60000)
  cleanup():
    remove expired buckets
```

### 4.2 Brute-Force 방어 패턴
```
AdminUser 로그인:
  실패 시: login_attempts++
  5회 실패: locked_until = NOW() + 15min
  성공 시: login_attempts = 0, locked_until = null
  
  잠금 확인: locked_until != null && locked_until > NOW()
```

### 4.3 HTTP 보안 헤더 패턴
```java
// SecurityConfig에서 설정
http.headers(headers -> headers
    .contentSecurityPolicy(csp -> csp.policyDirectives("default-src 'self'"))
    .httpStrictTransportSecurity(hsts -> hsts.maxAgeInSeconds(31536000).includeSubDomains(true))
    .contentTypeOptions(Customizer.withDefaults())  // nosniff
    .frameOptions(frame -> frame.deny())
    .referrerPolicy(ref -> ref.policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN))
);
```

---

## 5. 에러 처리 패턴

### 5.1 Global Exception Handler 패턴
```
@RestControllerAdvice
GlobalExceptionHandler:

  @ExceptionHandler(MethodArgumentNotValidException)
  → 400 INVALID_INPUT + 필드별 에러 목록

  @ExceptionHandler(NotFoundException)
  → 404 NOT_FOUND

  @ExceptionHandler(ForbiddenException)
  → 403 FORBIDDEN

  @ExceptionHandler(AccountLockedException)
  → 423 ACCOUNT_LOCKED

  @ExceptionHandler(BusinessException)
  → 400 + 비즈니스 에러 코드

  @ExceptionHandler(Exception)  // catch-all
  → 500 INTERNAL_ERROR (스택 트레이스 로그만, 응답에 노출 안함)
```

### 5.2 커스텀 예외 계층
```
RuntimeException
  +-- BusinessException (추상)
      +-- NotFoundException
      +-- ForbiddenException
      +-- AccountLockedException
      +-- InvalidStatusTransitionException
      +-- EmptyCartException
      +-- CategoryHasMenusException
```

---

## 6. 로깅 패턴

### 6.1 구조화 로깅
```
Logback JSON 포맷:
{
  "timestamp": "2026-04-08T12:00:00.000Z",
  "level": "INFO",
  "logger": "com.tableorder.order.service.OrderService",
  "requestId": "req-abc123",
  "storeId": 1,
  "message": "Order created",
  "orderId": 42,
  "totalAmount": 25000
}
```

### 6.2 Request Logging Filter
```
RequestLoggingFilter:
  - 요청: method, path, clientIp, requestId(UUID 생성)
  - 응답: status, duration(ms)
  - MDC에 requestId 설정 (모든 로그에 자동 포함)
  - 민감 데이터 필터: password, token, authorization 헤더 마스킹
```

---

## 7. 이미지 서빙 패턴

### 7.1 정적 리소스 서빙
```
업로드: POST /api/admin/stores/{id}/images
  → 파일 저장: /uploads/images/{uuid}.{ext}
  → 반환: imageUrl = /api/images/{uuid}.{ext}

조회: GET /api/images/{filename}
  → ResourceHttpRequestHandler로 정적 파일 서빙
  → Cache-Control: public, max-age=86400 (1일)
```

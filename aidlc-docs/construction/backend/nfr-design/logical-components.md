# Logical Components - Unit 1: Backend

---

## 시스템 컴포넌트 다이어그램

```
+----------------------------------------------------------------+
|                    Spring Boot Application                      |
|                                                                 |
|  +------------------+  +------------------+  +---------------+  |
|  | Security Layer   |  | WebSocket Layer  |  | SSE Layer     |  |
|  | - JwtAuthFilter  |  | - CartWSHandler  |  | - SseService  |  |
|  | - RateLimitFilter|  | - WSConfig       |  | - Heartbeat   |  |
|  | - SecurityConfig |  | - Session Mgmt   |  | - Emitter Mgmt|  |
|  +--------+---------+  +--------+---------+  +-------+-------+  |
|           |                      |                    |          |
|  +--------v---------------------------------------------v-----+  |
|  |                  Service Layer (Domain)                    |  |
|  |  +------+ +------+ +------+ +------+ +------+ +--------+ |  |
|  |  | Auth | | Store| | Table| | Menu | | Cart | | Order  | |  |
|  |  +------+ +------+ +------+ +------+ +------+ +--------+ |  |
|  +----+------+------+------+------+------+------+------+-----+  |
|       |      |      |      |      |      |      |      |        |
|  +----v------v------v------v------v------v------v------v-----+  |
|  |                  Repository Layer (JPA)                    |  |
|  +----+------+------+------+------+------+------+------+-----+  |
|       |                                                          |
+-------|----------------------------------------------------------+
        |
+-------v-------+
|  PostgreSQL    |
|  11 Tables     |
|  + Flyway      |
+---------------+
```

---

## 1. Security Layer

### JwtAuthFilter
| 항목 | 내용 |
|------|------|
| **타입** | OncePerRequestFilter |
| **책임** | JWT 토큰 검증, SecurityContext 설정 |
| **스킵 경로** | `/api/customer/auth/**`, `/api/stores/*/menus`, `/api/images/**`, `/ws/**` |
| **입력** | Authorization: Bearer {token} |
| **출력** | SecurityContext에 인증 정보 설정 |

### RateLimitFilter
| 항목 | 내용 |
|------|------|
| **타입** | OncePerRequestFilter |
| **책임** | IP 기반 요청 빈도 제한 |
| **알고리즘** | Token Bucket (분당 60회) |
| **저장** | ConcurrentHashMap (인메모리) |
| **초과 시** | 429 Too Many Requests |

### SecurityConfig
| 항목 | 내용 |
|------|------|
| **타입** | @Configuration |
| **책임** | Spring Security 설정, CORS, HTTP 보안 헤더, 경로별 접근 제어 |
| **CORS** | 환경 변수 기반 허용 origin |
| **세션** | STATELESS (JWT 사용) |

### RequestLoggingFilter
| 항목 | 내용 |
|------|------|
| **타입** | OncePerRequestFilter |
| **책임** | 요청/응답 로깅, requestId(MDC) 설정 |
| **마스킹** | password, token, authorization 필드 |

---

## 2. WebSocket Layer

### WebSocketConfig
| 항목 | 내용 |
|------|------|
| **타입** | @Configuration, WebSocketConfigurer |
| **엔드포인트** | `/ws/cart/{sessionId}` |
| **핸드셰이크** | JWT 토큰 검증 (쿼리 파라미터 또는 프로토콜 헤더) |
| **Allowed Origins** | 환경 변수 기반 |

### CartWebSocketHandler
| 항목 | 내용 |
|------|------|
| **타입** | TextWebSocketHandler |
| **세션 관리** | `Map<Long, Set<WebSocketSession>>` |
| **이벤트** | CART_ITEM_ADDED, REMOVED, UPDATED, CART_CLEARED, SESSION_COMPLETED, USER_JOINED, USER_LEFT |
| **동시성** | ConcurrentHashMap + CopyOnWriteArraySet |

---

## 3. SSE Layer

### OrderSseService
| 항목 | 내용 |
|------|------|
| **타입** | @Service |
| **Emitter 관리** | `Map<Long, List<SseEmitter>>` (storeId별) |
| **이벤트** | NEW_ORDER, ORDER_STATUS_CHANGED, ORDER_DELETED, TABLE_COMPLETED |
| **Heartbeat** | @Scheduled, 30초 간격 |
| **타임아웃** | 16시간 |
| **정리** | onCompletion/onTimeout/onError 콜백으로 자동 제거 |

---

## 4. Domain Services

### AuthService
| 메서드 | 설명 |
|--------|------|
| `accessByQr(storeId, tableNumber)` | QR 접속: 세션 조회/생성 + JWT 발급 |
| `tableLogin(storeId, tableNumber)` | 태블릿 로그인 |
| `adminLogin(storeId, username, password)` | 관리자 로그인 + brute-force 방어 |

### TableSessionService
| 메서드 | 설명 |
|--------|------|
| `getOrCreateSession(tableId)` | ACTIVE 세션 조회 또는 생성 |
| `completeSession(storeId, tableId)` | 이용 완료: 아카이브 + 장바구니 초기화 + 세션 종료 |
| `registerDevice(sessionId, deviceId)` | 접속자 등록 |

### MenuService / CategoryService
| 메서드 | 설명 |
|--------|------|
| `getMenusByStore(storeId)` | 카테고리+메뉴 계층 조회 |
| `createMenu(storeId, dto)` | 메뉴 생성 |
| `updateMenu(menuId, storeId, dto)` | 메뉴 수정 (소유권 검증) |
| `deleteMenu(menuId, storeId)` | 메뉴 삭제 |
| `reorderMenus(categoryId, orderedIds)` | 순서 변경 |
| CRUD 카테고리 동일 패턴 | |

### CartService
| 메서드 | 설명 |
|--------|------|
| `getCart(sessionId, deviceId)` | 장바구니 조회 (내/멤버 구분) |
| `addItem(sessionId, menuId, quantity, deviceId)` | 항목 추가 (UPSERT) |
| `updateItemQuantity(cartItemId, quantity, deviceId)` | 수량 변경 (소유권 검증) |
| `removeItem(cartItemId, deviceId)` | 항목 삭제 (소유권 검증) |
| `clearMyItems(sessionId, deviceId)` | 내 메뉴만 비우기 |
| `clearAll(sessionId)` | 전체 비우기 (주문 확정 시) |

### OrderService
| 메서드 | 설명 |
|--------|------|
| `createOrder(sessionId)` | 주문 생성 (장바구니→주문 변환) |
| `updateStatus(orderId, storeId, newStatus)` | 상태 변경 (전이 규칙 검증) |
| `deleteOrder(orderId, storeId)` | 주문 삭제 |
| `getOrdersBySession(sessionId)` | 세션 주문 목록 |
| `getDashboard(storeId)` | 대시보드 데이터 |

### OrderHistoryService
| 메서드 | 설명 |
|--------|------|
| `archiveOrders(sessionId)` | 이용 완료 시 주문→이력 이동 |
| `getHistory(storeId, tableId, dateRange)` | 과거 이력 조회 |

### ImageService
| 메서드 | 설명 |
|--------|------|
| `upload(file)` | 파일 검증 + 저장 + URL 반환 |

---

## 5. Cross-Cutting Components

### GlobalExceptionHandler
| 항목 | 내용 |
|------|------|
| **타입** | @RestControllerAdvice |
| **책임** | 모든 예외 → 표준 ErrorResponse 변환 |
| **로깅** | 500 에러: 스택 트레이스 로그, 400/401/403/404: 요약 로그 |

### JwtTokenProvider
| 항목 | 내용 |
|------|------|
| **타입** | @Component |
| **책임** | JWT 생성, 파싱, 검증 |
| **알고리즘** | HS256 |
| **비밀 키** | 환경 변수 `JWT_SECRET` |

---

## 6. 외부 인터페이스

| 인터페이스 | 프로토콜 | 방향 | 설명 |
|-----------|----------|------|------|
| REST API | HTTP/JSON | In/Out | 클라이언트 ↔ 서버 |
| WebSocket | WS | Bidirectional | 장바구니 실시간 동기화 |
| SSE | HTTP Stream | Server→Client | 주문 모니터링 |
| PostgreSQL | JDBC/TLS | Server→DB | 데이터 저장 |
| File System | Local I/O | Server→Disk | 이미지 저장 |

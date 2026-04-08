# Unit of Work Dependency - 테이블오더 서비스

---

## 의존성 매트릭스

| | Unit 1: Backend | Unit 2: Frontend |
|---|---|---|
| **Unit 1: Backend** | - | 없음 |
| **Unit 2: Frontend** | **의존** (REST API, WebSocket, SSE) | - |

---

## 의존성 상세

### Unit 2 → Unit 1 의존성

| 인터페이스 | 프로토콜 | 설명 |
|-----------|----------|------|
| REST API | HTTP/JSON | 메뉴 조회, 주문 생성, CRUD 등 모든 데이터 요청 |
| WebSocket | WS | 공유 장바구니 실시간 동기화 (`/ws/cart/{sessionId}`) |
| SSE | HTTP Stream | 관리자 주문 모니터링 (`/api/admin/stores/{id}/orders/stream`) |
| JWT Auth | HTTP Header | `Authorization: Bearer {token}` 인증 |

---

## 개발 순서

```
+-------------------+          +--------------------+
| Unit 1: Backend   |  ------> | Unit 2: Frontend   |
| (Spring Boot+DB)  |  REST    | (Next.js)          |
|                   |  WS/SSE  |                    |
| 개발 순서: 1차    |          | 개발 순서: 2차     |
+-------------------+          +--------------------+
```

### 순서 근거
1. **Unit 1 먼저**: Frontend가 Backend API에 의존하므로, API가 먼저 완성되어야 실제 연동 가능
2. **Unit 2 이후**: 완성된 API 기반으로 UI 구현 및 실시간 통신 연결

---

## API 계약 (Contract)

Unit 2가 Unit 1에 의존하는 핵심 API:

### Public Endpoints (인증 불필요)
| Method | Path | 용도 |
|--------|------|------|
| POST | `/api/customer/auth/qr-access` | QR 코드 접속 |
| POST | `/api/customer/auth/table-login` | 태블릿 로그인 |
| GET | `/api/stores/{storeId}/menus` | 메뉴 목록 조회 |

### Customer Endpoints (고객 JWT)
| Method | Path | 용도 |
|--------|------|------|
| GET | `/api/customer/sessions/{id}/cart` | 장바구니 조회 |
| POST | `/api/customer/sessions/{id}/cart/items` | 장바구니 추가 |
| PUT | `/api/customer/cart/items/{id}` | 수량 변경 |
| DELETE | `/api/customer/cart/items/{id}` | 항목 삭제 |
| POST | `/api/customer/sessions/{id}/orders` | 주문 생성 |
| GET | `/api/customer/sessions/{id}/orders` | 주문 내역 |

### Admin Endpoints (관리자 JWT)
| Method | Path | 용도 |
|--------|------|------|
| POST | `/api/admin/auth/login` | 관리자 로그인 |
| GET | `/api/admin/stores/{id}/orders/stream` | SSE 주문 스트림 |
| PUT | `/api/admin/orders/{id}/status` | 주문 상태 변경 |
| DELETE | `/api/admin/orders/{id}` | 주문 삭제 |
| POST | `/api/admin/stores/{id}/tables` | 테이블 등록 |
| POST | `/api/admin/stores/{id}/tables/{id}/complete` | 이용 완료 |
| CRUD | `/api/admin/stores/{id}/menus` | 메뉴 관리 |
| CRUD | `/api/admin/stores/{id}/categories` | 카테고리 관리 |

### 실시간 통신
| 프로토콜 | Endpoint | 용도 |
|----------|----------|------|
| WebSocket | `/ws/cart/{sessionId}` | 장바구니 실시간 동기화 |
| SSE | `/api/admin/stores/{id}/orders/stream` | 주문 모니터링 |

---

## 통합 테스트 포인트

Unit 1 + Unit 2 통합 시 검증할 핵심 시나리오:
1. **QR 접속 → 메뉴 조회**: QR 스캔 → JWT 발급 → 메뉴 API 호출
2. **공유 장바구니 동기화**: 다중 기기 WebSocket 연결 → 장바구니 변경 브로드캐스트
3. **주문 → 관리자 알림**: 주문 생성 → SSE로 관리자 대시보드 실시간 반영
4. **이용 완료 플로우**: 관리자 세션 종료 → WebSocket으로 고객 알림 → 데이터 이력 이동

# Unit of Work - Story Map - 테이블오더 서비스

---

## 매핑 원칙

모든 스토리는 **Backend API 구현** + **Frontend UI 구현** 양쪽이 필요합니다.
- **Unit 1 (Backend)**: API 엔드포인트, 비즈니스 로직, 데이터 모델, 실시간 서버측 구현
- **Unit 2 (Frontend)**: UI 페이지, 클라이언트 로직, 실시간 클라이언트측 구현

---

## Epic 1: 테이블 접속 (FR-C01)

| Story | Unit 1 (Backend) | Unit 2 (Frontend) |
|-------|-----------------|-------------------|
| **US-1.1** QR 코드로 테이블 접속 [P0] | AuthService.accessByQr(), JWT 발급, TableSessionService | QR 랜딩 페이지, 메뉴 화면 라우팅 |
| **US-1.2** 다중 사용자 동시 접속 [P0] | 접속자 수 트래킹, WebSocket USER_JOINED/LEFT 이벤트 | "N명이 함께 주문 중" 안내, 실시간 업데이트 |
| **US-1.3** 태블릿 자동 로그인 [P1] | AuthService.tableLogin(), 16시간 JWT | TableLoginPage, 로컬 저장 + 자동 로그인 |

## Epic 2: 메뉴 조회 (FR-C02)

| Story | Unit 1 (Backend) | Unit 2 (Frontend) |
|-------|-----------------|-------------------|
| **US-2.1** 카테고리별 메뉴 탐색 [P0] | GET /api/stores/{id}/menus (카테고리 포함) | MenuPage, CategoryTabs (Sticky), 스크롤 연동 |
| **US-2.2** 메뉴 상세 정보 확인 [P0] | 메뉴 상세 데이터 포함 응답 | MenuListItem (리스트형), MenuDetailPage, Badge |

## Epic 3: 장바구니 관리 (FR-C03)

| Story | Unit 1 (Backend) | Unit 2 (Frontend) |
|-------|-----------------|-------------------|
| **US-3.1** 공유 장바구니에 메뉴 담기 [P0] | CartService CRUD, 내/멤버 메뉴 구분 (deviceId) | CartPage (내 메뉴/멤버 메뉴), CartFloatingBar, Toast |
| **US-3.2** 장바구니 실시간 동기화 [P0] | CartWebSocketHandler 브로드캐스트, DB 저장 | WebSocketService, useWebSocket, Toast 알림 |

## Epic 4: 주문 생성 (FR-C04)

| Story | Unit 1 (Backend) | Unit 2 (Frontend) |
|-------|-----------------|-------------------|
| **US-4.1** 주문 확정 [P0] | OrderService.createOrder(), 장바구니→주문 변환, SSE 발행 | 주문 확인 UI, OrderCompletePage (5초 리다이렉트) |
| **US-4.2** 주문 실패 처리 [P0] | 에러 응답 (세션 만료, 서버 오류 등) | 에러 메시지 표시, 장바구니 유지, 재시도 안내 |

## Epic 5: 주문 내역 조회 (FR-C05)

| Story | Unit 1 (Backend) | Unit 2 (Frontend) |
|-------|-----------------|-------------------|
| **US-5.1** 현재 세션 주문 내역 조회 [P0] | GET /api/customer/sessions/{id}/orders | OrderHistoryPage (주문 목록, 상태 표시) |
| **US-5.2** 주문 상태 실시간 업데이트 [P1] | WebSocket ORDER_STATUS_CHANGED 이벤트 | 주문 상태 실시간 반영 (색상/아이콘) |

## Epic 6: 매장 인증 (FR-A01)

| Story | Unit 1 (Backend) | Unit 2 (Frontend) |
|-------|-----------------|-------------------|
| **US-6.1** 관리자 로그인 [P0] | AuthService.adminLogin(), bcrypt, 시도 제한 | AdminLoginPage, JWT 저장, 세션 유지 |

## Epic 7: 실시간 주문 모니터링 (FR-A02)

| Story | Unit 1 (Backend) | Unit 2 (Frontend) |
|-------|-----------------|-------------------|
| **US-7.1** 테이블별 주문 대시보드 [P0] | GET 주문 목록, 테이블별 그룹핑 | DashboardPage (그리드 카드), 필터링 |
| **US-7.2** 주문 상태 관리 [P0] | PUT /api/admin/orders/{id}/status, SSE 발행 | TableDetailModal, 상태 변경 UI |
| **US-7.3** 신규 주문 실시간 알림 [P0] | OrderSseController, SseEmitter | SSEService, useSSE, 카드 강조 애니메이션 |

## Epic 8: 테이블 관리 (FR-A03)

| Story | Unit 1 (Backend) | Unit 2 (Frontend) |
|-------|-----------------|-------------------|
| **US-8.1** 테이블 초기 설정 [P0] | POST 테이블 등록, QR URL 생성 | TableManagePage, QR 코드 표시 |
| **US-8.2** 주문 삭제 [P0] | DELETE /api/admin/orders/{id}, 총액 재계산 | ConfirmDialog, 삭제 후 피드백 |
| **US-8.3** 테이블 이용 완료 처리 [P0] | TableSessionService.completeSession(), 이력 이동 | ConfirmDialog, 테이블 리셋 UI |
| **US-8.4** 과거 주문 내역 조회 [P1] | OrderHistoryService 조회, 날짜 필터링 | OrderHistoryModal, 날짜 필터 |

## Epic 9: 메뉴/카테고리 관리 (FR-A04)

| Story | Unit 1 (Backend) | Unit 2 (Frontend) |
|-------|-----------------|-------------------|
| **US-9.1** 메뉴 CRUD [P0] | MenuService CRUD, 순서 관리, 검증 | MenuManagePage, MenuFormModal |
| **US-9.2** 카테고리 CRUD [P0] | CategoryService CRUD, 순서 관리 | 카테고리 관리 UI (MenuManagePage 내) |

## Epic 10: 이미지 업로드 (FR-A05)

| Story | Unit 1 (Backend) | Unit 2 (Frontend) |
|-------|-----------------|-------------------|
| **US-10.1** 메뉴 이미지 업로드 [P1] | ImageService, 파일 저장, URL 반환 | MenuFormModal 이미지 업로드, 미리보기 |

## Edge Cases

| Story | Unit 1 (Backend) | Unit 2 (Frontend) |
|-------|-----------------|-------------------|
| **US-EC.1** 네트워크 끊김 시 장바구니 보존 [P1] | 재접속 시 서버 장바구니 복원 | 오프라인 상태 감지, 로컬 데이터 유지, 복원 |
| **US-EC.2** 세션 만료 처리 [P1] | 16시간 토큰 만료, 세션 종료 이벤트 | 만료 안내 메시지, 재접속 안내 |
| **US-EC.3** 동시 주문 시 데이터 정합성 [P1] | 트랜잭션 격리, 동시성 제어 | 주문 결과 표시 (성공/실패) |

---

## 요약

| 유닛 | P0 스토리 | P1 스토리 | 합계 |
|------|-----------|-----------|------|
| **Unit 1 (Backend)** | 17 | 6 | **23** |
| **Unit 2 (Frontend)** | 17 | 6 | **23** |

> 모든 23개 스토리가 양쪽 유닛에 매핑되었습니다. ✅

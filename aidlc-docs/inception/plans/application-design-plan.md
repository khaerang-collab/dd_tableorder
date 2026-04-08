# Application Design Plan - 테이블오더 서비스

## Design Steps

- [x] Step 1: 컴포넌트 식별 및 책임 정의 (components.md)
  - [x] 프론트엔드 컴포넌트 (Next.js 페이지/컴포넌트)
  - [x] 백엔드 컴포넌트 (Spring Boot 도메인 기반)
  - [x] 데이터베이스 컴포넌트 (PostgreSQL 11개 테이블)
- [x] Step 2: 컴포넌트 메서드 시그니처 정의 (component-methods.md)
  - [x] API Controller 메서드 (6개 도메인)
  - [x] Service 메서드
  - [x] WebSocket/SSE 핸들러 메서드
- [x] Step 3: 서비스 레이어 설계 (services.md)
  - [x] 서비스 정의 및 오케스트레이션 (5개 핵심 플로우)
  - [x] 실시간 통신 (WebSocket + SSE) 서비스
- [x] Step 4: 의존성 매핑 (component-dependency.md)
- [x] Step 5: 통합 문서 생성 (application-design.md)

---

## Design Clarification Questions

아래 질문에 답변해 주세요. 각 질문의 `[Answer]:` 태그 뒤에 선택지 알파벳을 기입해 주세요.

### Question 1
공유 장바구니의 실시간 동기화 방식을 어떻게 하시겠습니까?

A) SSE (Server-Sent Events) - 주문 모니터링과 동일한 방식으로 통일
B) WebSocket - 양방향 통신으로 장바구니 변경 즉시 반영
C) Polling - 클라이언트가 주기적으로 서버에 장바구니 상태 요청 (가장 단순)
X) Other (please describe after [Answer]: tag below)

[Answer]: B

### Question 2
백엔드 패키지 구조를 어떻게 하시겠습니까?

A) 레이어드 아키텍처 (controller/service/repository 패키지로 분리) - 가장 전통적
B) 도메인 기반 패키지 (store/menu/order/table 도메인별 분리) - 도메인 응집도 높음
X) Other (please describe after [Answer]: tag below)

[Answer]: B

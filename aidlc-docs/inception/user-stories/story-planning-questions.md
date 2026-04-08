# User Stories Planning Questions

아래 질문에 답변해 주세요. 각 질문의 `[Answer]:` 태그 뒤에 선택지 알파벳을 기입해 주세요.

---

## Question 1
스토리의 세분화(Granularity) 수준을 어떻게 설정하시겠습니까?

A) 큰 단위 (Epic 수준, 기능 하나당 1개 스토리) - 빠른 개발 시작에 적합
B) 중간 단위 (기능당 2~4개 스토리) - 균형 잡힌 접근
C) 세밀한 단위 (기능당 5개 이상 스토리, 세부 시나리오별 분리) - 정밀한 관리
X) Other (please describe after [Answer]: tag below)

[Answer]: B

## Question 2
수용 기준(Acceptance Criteria) 형식을 어떻게 하시겠습니까?

A) Given/When/Then (BDD 스타일) - 테스트 자동화에 유리
B) 체크리스트 형식 (간결한 조건 나열) - 빠른 검증에 적합
C) 혼합 (핵심 스토리는 Given/When/Then, 단순 스토리는 체크리스트)
X) Other (please describe after [Answer]: tag below)

[Answer]: B

## Question 3
스토리 우선순위 기준에서, 다음 중 MVP 1차 구현에서 가장 중요한 영역은 무엇입니까?

A) 고객 주문 플로우 (메뉴 조회 → 장바구니 → 주문) 최우선
B) 관리자 모니터링 (실시간 주문 확인 + 테이블 관리) 최우선
C) 고객과 관리자 기능 균등하게 병행
X) Other (please describe after [Answer]: tag below)

[Answer]: A

## Question 4
다중 사용자 동시 주문 시나리오에서 엣지 케이스 스토리를 얼마나 상세하게 다루시겠습니까?

A) 기본 시나리오만 (QR 접속 → 주문, N명 동시 접속 안내)
B) 기본 + 주요 엣지 케이스 (네트워크 끊김 시, 세션 만료 시, 동시 주문 충돌 시)
C) 포괄적 (기본 + 엣지 케이스 + 에러 복구 시나리오 + 사용자 이탈 시나리오)
X) Other (please describe after [Answer]: tag below)

[Answer]: B

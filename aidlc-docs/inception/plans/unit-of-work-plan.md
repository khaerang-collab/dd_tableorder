# Unit of Work Plan - 테이블오더 서비스

## Decomposition Approach

이 시스템은 **모놀리식 백엔드 (Spring Boot)** + **단일 프론트엔드 앱 (Next.js)** + **PostgreSQL** 구조입니다.
Application Design에서 정의된 아키텍처를 기반으로 개발 유닛을 분해합니다.

## Generation Steps

- [x] Step 1: 유닛 정의 및 책임 할당 (`unit-of-work.md`)
  - [x] 유닛별 범위, 기술 스택, 포함 도메인/컴포넌트 정의
  - [x] 코드 조직 전략 문서화 (Greenfield)
- [x] Step 2: 유닛 간 의존성 매트릭스 (`unit-of-work-dependency.md`)
  - [x] 개발 순서 결정
  - [x] 유닛 간 인터페이스/계약 정의
- [x] Step 3: 스토리-유닛 매핑 (`unit-of-work-story-map.md`)
  - [x] 23개 스토리를 유닛에 할당
  - [x] 모든 스토리가 할당되었는지 검증
- [x] Step 4: 유닛 경계 및 의존성 검증
- [x] Step 5: 산출물 최종 확인

---

## Decomposition Questions

아래 질문에 답변해 주세요. 각 질문의 `[Answer]:` 태그 뒤에 선택지 알파벳을 기입해 주세요.

### Question 1
시스템을 몇 개의 개발 유닛으로 분해하시겠습니까?

A) 2개 유닛 - Backend(Spring Boot + DB 스키마) / Frontend(Next.js 고객+관리자)
B) 3개 유닛 - DB 스키마 / Backend API / Frontend
C) 4개 유닛 - DB 스키마 / Backend API / Frontend Customer / Frontend Admin
X) Other (please describe after [Answer]: tag below)

[Answer]: A

### Question 2
개발 우선순위는 어떻게 하시겠습니까?

A) Backend 먼저 → Frontend 순차 (API가 완성된 후 UI 연결)
B) Backend + Frontend 동시 병렬 개발 (Mock API 활용)
X) Other (please describe after [Answer]: tag below)

[Answer]: A

### Question 3
Construction Phase에서 유닛별로 Functional Design / NFR 단계를 어떻게 진행하시겠습니까?

A) 유닛별 독립 진행 (Unit 1 전체 완료 → Unit 2 전체 완료)
B) 단계별 일괄 진행 (모든 유닛 FD → 모든 유닛 NFR → 모든 유닛 Code Gen)
X) Other (please describe after [Answer]: tag below)

[Answer]: A

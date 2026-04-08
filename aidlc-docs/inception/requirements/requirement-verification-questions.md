# Requirements Verification Questions

테이블오더 서비스 구축을 위해 아래 질문에 답변해 주세요.
각 질문의 `[Answer]:` 태그 뒤에 선택지 알파벳을 기입해 주세요.

---

## Question 1
백엔드(서버) 기술 스택으로 어떤 것을 사용하시겠습니까?

A) Node.js + Express
B) Node.js + Fastify
C) Python + FastAPI
D) Java + Spring Boot
X) Other (please describe after [Answer]: tag below)

[Answer]: D

## Question 2
프론트엔드(고객용 + 관리자용 UI) 기술 스택으로 어떤 것을 사용하시겠습니까?

A) React (Vite)
B) Next.js
C) Vue.js (Vite)
D) Vanilla HTML/CSS/JavaScript (프레임워크 미사용)
X) Other (please describe after [Answer]: tag below)

[Answer]: B

## Question 3
데이터베이스로 어떤 것을 사용하시겠습니까?

A) PostgreSQL
B) MySQL
C) SQLite (경량, 개발/소규모 매장 적합)
D) Amazon DynamoDB (NoSQL)
X) Other (please describe after [Answer]: tag below)

[Answer]: A

## Question 4
고객용 UI와 관리자용 UI를 어떻게 구성하시겠습니까?

A) 하나의 프론트엔드 앱에서 라우팅으로 분리 (예: /customer, /admin)
B) 별도의 프론트엔드 앱 2개로 분리
X) Other (please describe after [Answer]: tag below)

[Answer]: A

## Question 5
배포 환경은 어떻게 계획하고 계십니까?

A) AWS 클라우드 (EC2, ECS 등)
B) 로컬 서버 / On-premises
C) Docker 컨테이너 기반 (배포 환경 미정, 컨테이너화만 우선)
D) 배포는 나중에 결정, 지금은 로컬 개발 환경만 구성
X) Other (please describe after [Answer]: tag below)

[Answer]: A

## Question 6
메뉴 이미지 관리는 어떻게 처리하시겠습니까?

A) 이미지 URL만 저장 (외부 호스팅된 이미지 링크 입력)
B) 서버에 이미지 파일 업로드 및 로컬 저장
C) 클라우드 스토리지(S3 등)에 업로드
X) Other (please describe after [Answer]: tag below)

[Answer]: B

## Question 7
동시 접속 매장 규모를 어떻게 예상하십니까? (초기 MVP 기준)

A) 소규모 - 단일 매장 (테이블 1~20개)
B) 중규모 - 소수 매장 (2~5개 매장)
C) 대규모 - 다수 매장 지원 (10개 이상)
X) Other (please describe after [Answer]: tag below)

[Answer]: C

## Question 8
관리자가 메뉴를 관리할 때, 카테고리 자체도 동적으로 추가/수정/삭제할 수 있어야 합니까?

A) Yes - 카테고리도 관리자가 자유롭게 CRUD 가능
B) No - 카테고리는 사전 정의된 목록 사용 (코드에서 고정)
X) Other (please describe after [Answer]: tag below)

[Answer]: A

## Question 9
프로젝트의 언어(코드 내 변수명, 주석, API 등)를 어떻게 하시겠습니까?

A) 영어 (변수명, API 경로, 주석 모두 영어)
B) 한영 혼합 (API/변수명은 영어, 주석/UI 텍스트는 한국어)
X) Other (please describe after [Answer]: tag below)

[Answer]: B

## Question 10: Security Extensions
이 프로젝트에 보안 확장 규칙을 적용하시겠습니까?

A) Yes - 모든 SECURITY 규칙을 blocking constraint로 적용 (운영 환경 수준 권장)
B) No - SECURITY 규칙 건너뛰기 (PoC, 프로토타입, 실험적 프로젝트에 적합)
X) Other (please describe after [Answer]: tag below)

[Answer]: A

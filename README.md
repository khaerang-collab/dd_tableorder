# dd_tableorder

QR 코드 기반 디지털 테이블오더 서비스 (MVP)

매장 방문 고객이 QR 코드를 스캔하여 메뉴를 조회하고, 동행자와 함께 장바구니를 공유하며 주문할 수 있는 시스템입니다. 관리자는 실시간 주문 모니터링, 메뉴 관리, 테이블 운영을 웹 대시보드에서 수행합니다.

---

## 기술 스택

### Backend

| 분류 | 기술 | 버전 |
|------|------|------|
| Language | Java | 17 |
| Framework | Spring Boot | 3.2.5 |
| Security | Spring Security | 6.1 |
| ORM | Spring Data JPA / Hibernate | 6.4 |
| Validation | Spring Boot Starter Validation | 3.2.5 |
| WebSocket | Spring Boot Starter WebSocket | 3.2.5 |
| Monitoring | Spring Boot Actuator | 3.2.5 |
| JWT | jjwt (io.jsonwebtoken) | 0.12.5 |
| Database | PostgreSQL | 42.7.3 |
| DB Migration | Flyway | 10.15.0 |
| Test DB | H2 (인메모리) | - |
| API 문서 | Springdoc OpenAPI (Swagger UI) | 2.3.0 |
| QR 코드 | Google ZXing | 3.5.3 |
| Utility | Lombok | - |
| Build Tool | Gradle (Kotlin DSL) | 8.7 |

### Frontend

| 분류 | 기술 | 버전 |
|------|------|------|
| Framework | Next.js | 14.2.5 |
| UI Library | React | 18.3.1 |
| Language | TypeScript | 5.5.3 |
| CSS | Tailwind CSS | 3.4.6 |
| CSS 처리 | PostCSS / Autoprefixer | 8.4 / 10.4 |
| Lint | ESLint + eslint-config-next | 8.57.0 |

### 인프라 / 배포

| 분류 | 기술 |
|------|------|
| Frontend 배포 | AWS Amplify |
| Backend 배포 | AWS EC2 + Elastic IP |
| Database | PostgreSQL (EC2) |
| API 통신 | Next.js API Routes 프록시 (CORS 불필요) |

### 테스트

| 분류 | 기술 | 용도 |
|------|------|------|
| 단위/통합 테스트 | JUnit 5 + Spring Boot Test | 백엔드 서비스/컨트롤러 테스트 (100개) |
| 보안 테스트 | Spring Security Test | 인증/인가 검증 |
| E2E 테스트 | Playwright | 프론트엔드 브라우저 테스트 (5개 spec) |
| QA 자동화 | Node.js (Fetch API) | API 통합 테스트 (30개 TC) |

### 실시간 통신

| 채널 | 방향 | 용도 |
|------|------|------|
| SSE (주문) | 서버 → 관리자 | 신규 주문 알림, 실시간 갱신 |
| SSE (상태) | 서버 → 고객 | 주문 상태 변경 실시간 반영 |
| SSE (직원호출) | 서버 → 관리자 | 직원 호출 알림 (테이블 + 사유) |
| SSE (장바구니) | 서버 → 고객 | 동행자 장바구니 실시간 동기화 |

---

## 프로젝트 구조

```
dd_tableorder/
├── table-order-backend/        # Spring Boot 백엔드
│   ├── src/main/java/com/tableorder/
│   │   ├── auth/               # 관리자 인증, 카카오 로그인
│   │   ├── cart/               # 장바구니 (테이블 공유)
│   │   ├── menu/               # 메뉴/카테고리 관리
│   │   ├── order/              # 주문 생성/상태 관리
│   │   ├── table/              # 테이블/세션 관리
│   │   ├── store/              # 매장 정보
│   │   ├── customer/           # 고객 프로필
│   │   ├── staffcall/          # 직원 호출
│   │   ├── promotion/          # 프로모션
│   │   ├── recommend/          # 메뉴 추천
│   │   └── common/             # 공통 (보안, 필터, 예외 처리)
│   └── src/main/resources/
│       └── db/migration/       # Flyway 마이그레이션
│
├── table-order-frontend/       # Next.js 프론트엔드
│   └── src/app/
│       ├── customer/           # 고객 화면 (메뉴, 장바구니, 주문)
│       └── admin/              # 관리자 화면 (대시보드, 메뉴관리)
│
├── tests/                      # QA 자동화 테스트
└── aidlc-docs/                 # 프로젝트 문서
```

---

## 실행 방법

### 사전 요구사항

- Java 17+
- Node.js 18+
- PostgreSQL 15+ (로컬 개발 시)

### 1. 백엔드 실행

```bash
cd table-order-backend

# 환경 변수 설정 (선택 - 기본값 있음)
export DB_USERNAME=postgres
export DB_PASSWORD=postgres
export JWT_SECRET=your-secret-key-must-be-at-least-256-bits-long
export KAKAO_CLIENT_ID=your-kakao-client-id

# 로컬 개발 모드 (PostgreSQL 필요)
./gradlew bootRun

# 서버 실행 확인
curl http://localhost:8080/actuator/health
```

백엔드는 기본적으로 **8080 포트**에서 실행됩니다.

### 2. 프론트엔드 실행

```bash
cd table-order-frontend

# 의존성 설치
npm install

# 개발 서버 실행
npm run dev
```

프론트엔드는 기본적으로 **3000 포트**에서 실행됩니다.

### 3. API 문서 확인

백엔드 실행 후 Swagger UI에서 API 명세를 확인할 수 있습니다:

```
http://localhost:8080/swagger-ui/index.html
```

---

## 사용 방법

### 고객 (Customer)

1. **테이블 접속**: QR 코드 스캔 또는 `/customer?storeId={매장ID}&table={테이블번호}` 접속
2. **카카오 로그인**: "카카오로 시작" 버튼으로 로그인 (필수)
3. **메뉴 탐색**: 카테고리별 메뉴 조회, 상세 정보 확인
4. **장바구니**: 메뉴 담기 (동행자와 실시간 공유), 수량 조절
5. **주문**: "주문하기" 버튼으로 주문 확정, 주문 상태 실시간 확인
6. **직원 호출**: 상단 "직원호출" 버튼으로 도움 요청

### 관리자 (Admin)

1. **로그인**: `/admin/login`에서 매장 식별자 + 계정 정보 입력
2. **주문 모니터링**: `/admin/dashboard`에서 테이블별 주문 현황 실시간 확인
3. **주문 상태 관리**: 대기중 → 준비중 → 완료 순서로 상태 변경
4. **테이블 관리**: `/admin/tables`에서 테이블 등록, QR 생성, 이용 완료 처리
5. **메뉴 관리**: `/admin/menus`에서 카테고리/메뉴 CRUD, 이미지 업로드
6. **프로모션 설정**: `/admin/promotions`에서 서비스 프로모션 등록

### 기본 관리자 계정 (시드 데이터)

| 항목 | 값 |
|------|------|
| 매장 ID | 1 |
| 사용자명 | admin |
| 비밀번호 | admin1234 |

---

## 테스트 실행

### 백엔드 단위/통합 테스트 (JUnit)

```bash
cd table-order-backend
./gradlew test

# 테스트 리포트 확인 (브라우저)
# build/reports/tests/test/index.html
```

### QA API 자동화 테스트

```bash
# 백엔드가 실행 중인 상태에서
node tests/qa-test.mjs

# 다른 서버 주소 지정 시
API_URL=http://your-server:8080 node tests/qa-test.mjs
```

### 프론트엔드 E2E 테스트 (Playwright)

```bash
cd table-order-frontend
npx playwright install    # 브라우저 설치 (최초 1회)
npm run test:e2e          # 테스트 실행
npm run test:e2e:report   # 리포트 확인
```

---

## 주요 화면

| 화면 | 경로 | 설명 |
|------|------|------|
| 메인 | `/` | 고객/관리자 진입 |
| 고객 메뉴 | `/customer` | 메뉴 조회, 카테고리 탐색 |
| 장바구니 | `/customer/cart` | 공유 장바구니, 주문 확정 |
| 주문 내역 | `/customer/orders` | 주문 상태 실시간 조회 |
| 관리자 로그인 | `/admin/login` | 매장 인증 |
| 주문 대시보드 | `/admin/dashboard` | 실시간 주문 모니터링 |
| 테이블 관리 | `/admin/tables` | QR 생성, 세션 관리 |
| 메뉴 관리 | `/admin/menus` | 메뉴/카테고리 CRUD |
| 프로모션 | `/admin/promotions` | 프로모션 설정 |

---

## 팀 구성

| GitHub 계정 | 역할 | 담당 영역 |
|------------|------|----------|
| [khaerang-collab](https://github.com/khaerang-collab) | QA | 테스트 자동화, 품질 보증 |
| [95jinhong](https://github.com/95jinhong) | 개발자 | 백엔드 개발 (Spring Boot API) |
| [ongddree](https://github.com/ongddree) | DevOps, 프론트엔드 | CI/CD, 인프라, 프론트엔드 개발 |
| [bomanbom](https://github.com/bomanbom) | PO, 디자이너 | 기획, UI/UX 디자인 |

---

## 관련 문서

- [서비스 플로우](aidlc-docs/service-flow.md) - 고객/관리자 서비스 흐름도
- [요구사항 정의서](aidlc-docs/inception/requirements/requirements.md) - 기능/비기능 요구사항
- [유저 스토리](aidlc-docs/inception/user-stories/stories.md) - 33개 유저 스토리
- [Git 세팅 가이드](GIT_SETUP_GUIDE.md) - 프로젝트 초기 설정

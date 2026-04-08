# NFR Requirements - Unit 1: Backend

---

## 1. 성능 (Performance)

| ID | 요구사항 | 목표값 | 측정 방법 |
|----|----------|--------|-----------|
| NFR-PERF-01 | API 응답 시간 | 500ms 이내 (p95) | Spring Boot Actuator + 로그 |
| NFR-PERF-02 | SSE 주문 알림 지연 | 2초 이내 | 주문 생성 → SSE 수신 시간 측정 |
| NFR-PERF-03 | WebSocket 장바구니 동기화 | 1초 이내 | 변경 → 브로드캐스트 수신 시간 |
| NFR-PERF-04 | DB 쿼리 실행 시간 | 100ms 이내 (p95) | 쿼리 로그 |
| NFR-PERF-05 | 이미지 업로드 | 3초 이내 (5MB 기준) | 업로드 완료 시간 |
| NFR-PERF-06 | 동시 WebSocket 연결 | 매장당 100개 이상 | 부하 테스트 |
| NFR-PERF-07 | 동시 SSE 연결 | 매장당 10개 이상 | 부하 테스트 |

---

## 2. 확장성 (Scalability)

| ID | 요구사항 | 설명 |
|----|----------|------|
| NFR-SCALE-01 | 멀티테넌트 | 10개 이상 매장 동시 지원 |
| NFR-SCALE-02 | 데이터 격리 | store_id 기반 논리적 테넌트 분리 |
| NFR-SCALE-03 | 커넥션 풀 | HikariCP 기본 10, 최대 20 커넥션 |
| NFR-SCALE-04 | 수평 확장 대비 | Stateless API 설계 (세션 = JWT) |

---

## 3. 가용성 (Availability)

| ID | 요구사항 | 목표값 |
|----|----------|--------|
| NFR-AVAIL-01 | 서비스 가동 시간 | 99.5% |
| NFR-AVAIL-02 | SSE 재연결 | 연결 끊김 시 자동 재연결 (클라이언트) |
| NFR-AVAIL-03 | WebSocket 재연결 | 연결 끊김 시 자동 재연결 (클라이언트) |
| NFR-AVAIL-04 | DB 헬스체크 | Spring Boot Actuator /health 엔드포인트 |
| NFR-AVAIL-05 | Graceful Shutdown | 진행 중 요청 완료 후 종료 |

---

## 4. 보안 (Security) - Security Baseline 적용

### SECURITY-01: 암호화
- PostgreSQL: TLS 연결 강제 (`sslmode=require`)
- API: HTTPS 전용 (운영 환경)
- 이미지 저장: 로컬 파일 시스템 (MVP에서는 별도 암호화 없음, N/A 처리)

### SECURITY-03: 애플리케이션 레벨 로깅
- SLF4J + Logback 구조화 로깅
- 로그 포맷: `timestamp, requestId, level, logger, message`
- 민감 데이터 (password, token) 로그 금지

### SECURITY-04: HTTP 보안 헤더
- `Content-Security-Policy: default-src 'self'`
- `Strict-Transport-Security: max-age=31536000; includeSubDomains`
- `X-Content-Type-Options: nosniff`
- `X-Frame-Options: DENY`
- `Referrer-Policy: strict-origin-when-cross-origin`

### SECURITY-05: 입력값 검증
- Jakarta Validation (`@Valid`, `@NotBlank`, `@Min`, `@Max`, `@Size`)
- 모든 API 파라미터 검증
- 요청 본문 크기 제한 (10MB)

### SECURITY-06: 최소 권한
- Customer: 자신 세션의 장바구니/주문만 접근
- Admin: 자신 매장 데이터만 접근
- Public: 메뉴 조회 + 접속만 가능

### SECURITY-07: 네트워크 설정
- CORS: 명시적 허용 origin만 (`Access-Control-Allow-Origin` 와일드카드 금지)
- MVP 단계: 단일 서버 배포 (VPC는 운영 단계에서 구성)

### SECURITY-08: 애플리케이션 레벨 접근 제어
- JWT 필터로 모든 요청 인증 (Public 엔드포인트 제외)
- IDOR 방지: 리소스 접근 시 store_id/session_id 소유권 검증
- Admin/Customer 역할 기반 엔드포인트 분리

### SECURITY-09: 보안 하드닝
- 기본 자격 증명 금지 (초기 데이터에 기본 비밀번호 X)
- 에러 응답에 스택 트레이스 노출 금지 (운영 환경)
- Spring Boot Actuator 엔드포인트 제한

### SECURITY-10: 소프트웨어 공급망
- Gradle 의존성 버전 고정
- `gradle.lockfile` 사용
- 의존성 취약점 스캔 지침 포함

### SECURITY-11: 보안 설계 원칙
- Auth 모듈 분리 (auth 도메인 패키지)
- Rate Limiting: Public API에 적용
- 방어 심층: 검증 + 인가 + 암호화 레이어링

### SECURITY-12: 인증 및 자격 증명 관리
- bcrypt (cost factor 10) 비밀번호 해싱
- 로그인 시도 5회 실패 → 15분 잠금
- JWT HttpOnly 쿠키 또는 Authorization 헤더
- 하드코딩된 자격 증명 금지 (환경 변수 사용)

### SECURITY-13: 소프트웨어/데이터 무결성
- Jackson JSON 역직렬화: 알려진 타입만 허용
- 감사 추적: 주문 상태 변경 로그

### SECURITY-14: 알림 및 모니터링
- 로그인 실패 연속 발생 시 로그 경고
- 인가 실패 로그 기록
- 로그 보관: 최소 90일 (운영 환경 설정)

### SECURITY-15: 예외 처리
- GlobalExceptionHandler로 모든 예외 포착
- 운영 환경: 일반적 에러 메시지만 반환
- 리소스 정리: @Transactional 롤백, try-with-resources
- Fail Closed: 인증/인가 오류 시 접근 거부

---

## 5. 배포 및 환경 (Deployment & Environment)

| ID | 요구사항 | 설명 |
|----|----------|------|
| NFR-DEPLOY-01 | QA 환경 | 로컬 환경에서 QA 검증 수행 (localhost, H2/로컬 PostgreSQL) |
| NFR-DEPLOY-02 | 운영 Backend 배포 | AWS EC2 + Elastic IP에 Spring Boot JAR 배포 |
| NFR-DEPLOY-03 | 운영 Frontend 배포 | AWS Amplify를 통한 Next.js 앱 배포 |
| NFR-DEPLOY-04 | API 통신 | Next.js API Routes 프록시 패턴 (브라우저→Amplify→EC2, CORS 불필요) |
| NFR-DEPLOY-05 | 환경 분리 | local / prod 프로파일 분리 (application-local.yml, application-prod.yml) |
| NFR-DEPLOY-06 | EC2 사양 | t3.small 이상 (MVP 기준, 트래픽 증가 시 스케일업) |
| NFR-DEPLOY-07 | 데이터베이스 | EC2 내 PostgreSQL 또는 Amazon RDS (prod) |
| NFR-DEPLOY-08 | 네트워크 | EC2 Elastic IP 할당 (Amplify API Routes → EC2 통신용 고정 공인 IP) |

---

## 6. 유지보수성 (Maintainability)

| ID | 요구사항 | 설명 |
|----|----------|------|
| NFR-MAINT-01 | 도메인 기반 패키지 | auth/store/table/menu/cart/order/common |
| NFR-MAINT-02 | DB 마이그레이션 | Flyway 버전 관리 |
| NFR-MAINT-03 | API 문서화 | SpringDoc OpenAPI (Swagger UI) |
| NFR-MAINT-04 | 환경별 설정 | application.yml + application-{profile}.yml |
| NFR-MAINT-05 | 테스트 구조 | 도메인별 단위 테스트 + 통합 테스트 |

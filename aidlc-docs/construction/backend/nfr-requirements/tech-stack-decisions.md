# Tech Stack Decisions - Unit 1: Backend

---

## 핵심 기술 스택

| 계층 | 기술 | 버전 | 선택 이유 |
|------|------|------|-----------|
| **Language** | Java | 17+ | LTS, Spring Boot 3 최소 요구 |
| **Framework** | Spring Boot | 3.2.x | 생산성, 생태계, WebSocket/SSE 내장 지원 |
| **Database** | PostgreSQL | 16.x | JSONB 지원 (OrderHistory), 안정성, 확장성 |
| **ORM** | Spring Data JPA + Hibernate | - | 생산성, 도메인 모델 매핑 |
| **Build** | Gradle (Kotlin DSL) | 8.x | 빠른 빌드, 의존성 관리 |
| **Migration** | Flyway | 9.x | DB 스키마 버전 관리 |

---

## 의존성 상세

### Spring Boot Starters

| Starter | 용도 |
|---------|------|
| `spring-boot-starter-web` | REST API, MVC |
| `spring-boot-starter-data-jpa` | JPA, Hibernate |
| `spring-boot-starter-security` | Spring Security, JWT 필터 |
| `spring-boot-starter-websocket` | WebSocket 지원 |
| `spring-boot-starter-validation` | Jakarta Validation |
| `spring-boot-starter-actuator` | 헬스체크, 메트릭 |

### 추가 라이브러리

| 라이브러리 | 버전 | 용도 |
|-----------|------|------|
| `jjwt-api` + `jjwt-impl` + `jjwt-jackson` | 0.12.x | JWT 토큰 생성/검증 |
| `spring-security-crypto` | (Spring Boot 내장) | bcrypt 비밀번호 해싱 |
| `postgresql` | 42.7.x | PostgreSQL JDBC 드라이버 |
| `flyway-core` + `flyway-database-postgresql` | 9.x | DB 마이그레이션 |
| `springdoc-openapi-starter-webmvc-ui` | 2.3.x | OpenAPI/Swagger UI |
| `zxing-core` + `zxing-javase` | 3.5.x | QR 코드 생성 |
| `lombok` | 최신 | 보일러플레이트 감소 |

### 테스트 의존성

| 라이브러리 | 용도 |
|-----------|------|
| `spring-boot-starter-test` | JUnit 5, Mockito, AssertJ |
| `spring-security-test` | Security 테스트 유틸 |
| `h2` (test scope) | 인메모리 DB 단위 테스트 |
| `testcontainers-postgresql` | PostgreSQL 통합 테스트 |

---

## 커넥션 풀 설정

```yaml
spring:
  datasource:
    hikari:
      minimum-idle: 5
      maximum-pool-size: 20
      idle-timeout: 300000       # 5분
      max-lifetime: 1800000      # 30분
      connection-timeout: 20000  # 20초
```

---

## WebSocket 설정

| 설정 | 값 | 이유 |
|------|-----|------|
| Message Size Limit | 64KB | 장바구니 이벤트 크기 제한 |
| Send Buffer Size | 512KB | 다중 클라이언트 브로드캐스트 |
| Idle Timeout | 16시간 | 세션 만료와 동일 |
| Allowed Origins | 설정 기반 | CORS 보안 |

---

## SSE 설정

| 설정 | 값 | 이유 |
|------|-----|------|
| Timeout | 16시간 | 관리자 세션과 동일 |
| Heartbeat Interval | 30초 | 연결 유지 |
| Reconnect Delay | 3초 | 클라이언트 재연결 대기 |

---

## 환경 설정 구조

```
src/main/resources/
+-- application.yml              # 공통 설정
+-- application-local.yml        # 로컬 개발 (H2 또는 로컬 PostgreSQL)
+-- application-dev.yml          # 개발 서버
+-- application-prod.yml         # 운영 서버
```

### 주요 설정 항목

```yaml
# application.yml (공통)
server:
  port: 8080
  servlet:
    context-path: /

spring:
  jpa:
    hibernate:
      ddl-auto: validate    # Flyway로 관리, validate만
    properties:
      hibernate:
        format_sql: true
  servlet:
    multipart:
      max-file-size: 5MB
      max-request-size: 10MB

app:
  jwt:
    secret: ${JWT_SECRET}
    expiration: 57600000     # 16시간 (ms)
  upload:
    path: ${UPLOAD_PATH:/uploads/images}
  cors:
    allowed-origins: ${CORS_ORIGINS:http://localhost:3000}  # prod: Amplify 도메인
  rate-limit:
    requests-per-minute: 60
  login:
    max-attempts: 5
    lock-duration-minutes: 15
```

---

## 보안 설정 결정

| 항목 | 결정 | 이유 |
|------|------|------|
| 비밀번호 해싱 | bcrypt (cost 10) | 적절한 보안/성능 균형 |
| JWT 알고리즘 | HS256 | 단일 서비스, 비대칭 키 불필요 |
| JWT 저장 | Authorization Header (Bearer) | 모바일 브라우저 호환, CSRF 미필요 |
| 비밀 관리 | 환경 변수 | MVP 단계 적합, 추후 Secrets Manager |
| Rate Limiting | 인메모리 (ConcurrentHashMap) | MVP 단계, 추후 Redis 전환 가능 |

---

## 배포 인프라 결정

| 항목 | 결정 | 이유 |
|------|------|------|
| QA 환경 | 로컬 (localhost) | 빠른 피드백 루프, 비용 절감 |
| Backend 운영 | AWS EC2 | Spring Boot JAR 직접 배포, 유연한 서버 설정 |
| Frontend 운영 | AWS Amplify | Next.js 최적 배포, CI/CD 자동화, CDN 내장 |
| Database (운영) | EC2 내 PostgreSQL 또는 Amazon RDS | MVP: EC2 내 PostgreSQL, 추후 RDS 전환 가능 |
| 이미지 저장 (운영) | EC2 로컬 파일시스템 | MVP 단계 적합, 추후 S3 전환 가능 |

### 환경별 배포 구성

```
[QA - 로컬]
  Browser → localhost:3000 (Next.js dev) → localhost:8080 (Spring Boot)
                                              → localhost:5432 (PostgreSQL)

[운영 - AWS]
  Browser → AWS Amplify (Next.js)  → EC2 (Spring Boot :8080)
            (CDN + HTTPS)              → PostgreSQL (EC2 내부 or RDS)
                                       → /uploads/images (EC2 로컬)
```

### 환경별 설정 파일

| 프로파일 | 파일 | 주요 설정 |
|----------|------|-----------|
| local | `application-local.yml` | H2 인메모리 또는 로컬 PostgreSQL, CORS: localhost:3000 |
| prod | `application-prod.yml` | EC2 PostgreSQL/RDS, CORS: Amplify 도메인, HTTPS 강제 |

---

## 프로젝트 구조 결정

| 항목 | 결정 | 이유 |
|------|------|------|
| 패키지 구조 | 도메인 기반 | 도메인 응집도, 향후 MSA 분리 용이 |
| DTO 전략 | Request/Response DTO 분리 | 엔티티 노출 방지, 검증 분리 |
| 예외 처리 | GlobalExceptionHandler | 일관된 에러 응답 |
| 로깅 | SLF4J + Logback (JSON) | 구조화 로깅, 중앙 수집 대비 |
| API 버전 | 미적용 (MVP) | 단일 버전, 추후 /v2 추가 가능 |

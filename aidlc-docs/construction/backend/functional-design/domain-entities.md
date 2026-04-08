# Domain Entities - Unit 1: Backend

---

## Entity Relationship Diagram (Text)

```
Store (1) ──< AdminUser (N)
Store (1) ──< RestaurantTable (N)
Store (1) ──< Category (N)

RestaurantTable (1) ──< TableSession (N)

Category (1) ──< Menu (N)

TableSession (1) ──< Cart (1)
TableSession (1) ──< Order (N)

Cart (1) ──< CartItem (N)
CartItem (N) >── Menu (1)

Order (1) ──< OrderItem (N)
OrderItem (N) >── Menu (참조, 비정규화)

Store (1) ──< OrderHistory (N)
```

---

## 1. Store (매장)

| 필드 | 타입 | 제약조건 | 설명 |
|------|------|----------|------|
| `id` | BIGINT (PK) | AUTO_INCREMENT | 매장 고유 ID |
| `name` | VARCHAR(100) | NOT NULL | 매장명 |
| `address` | VARCHAR(255) | | 매장 주소 |
| `phone` | VARCHAR(20) | | 매장 전화번호 |
| `notice` | VARCHAR(500) | | 매장 공지 메시지 |
| `origin_info` | TEXT | | 원산지 정보 |
| `created_at` | TIMESTAMP | NOT NULL, DEFAULT NOW() | 생성 시각 |
| `updated_at` | TIMESTAMP | NOT NULL, DEFAULT NOW() | 수정 시각 |

**인덱스**: PK(`id`)

---

## 2. AdminUser (관리자)

| 필드 | 타입 | 제약조건 | 설명 |
|------|------|----------|------|
| `id` | BIGINT (PK) | AUTO_INCREMENT | 관리자 ID |
| `store_id` | BIGINT (FK) | NOT NULL, REFERENCES stores(id) | 소속 매장 |
| `username` | VARCHAR(50) | NOT NULL, UNIQUE per store | 사용자명 |
| `password_hash` | VARCHAR(255) | NOT NULL | bcrypt 해싱된 비밀번호 |
| `login_attempts` | INT | NOT NULL, DEFAULT 0 | 연속 로그인 실패 횟수 |
| `locked_until` | TIMESTAMP | | 계정 잠금 해제 시각 |
| `created_at` | TIMESTAMP | NOT NULL, DEFAULT NOW() | 생성 시각 |
| `updated_at` | TIMESTAMP | NOT NULL, DEFAULT NOW() | 수정 시각 |

**인덱스**: PK(`id`), UNIQUE(`store_id`, `username`), FK(`store_id`)

---

## 3. RestaurantTable (테이블)

| 필드 | 타입 | 제약조건 | 설명 |
|------|------|----------|------|
| `id` | BIGINT (PK) | AUTO_INCREMENT | 테이블 ID |
| `store_id` | BIGINT (FK) | NOT NULL, REFERENCES stores(id) | 소속 매장 |
| `table_number` | INT | NOT NULL | 테이블 번호 |
| `qr_code_url` | VARCHAR(500) | | QR 코드에 인코딩된 URL |
| `created_at` | TIMESTAMP | NOT NULL, DEFAULT NOW() | 생성 시각 |
| `updated_at` | TIMESTAMP | NOT NULL, DEFAULT NOW() | 수정 시각 |

**인덱스**: PK(`id`), UNIQUE(`store_id`, `table_number`), FK(`store_id`)

---

## 4. TableSession (테이블 세션)

| 필드 | 타입 | 제약조건 | 설명 |
|------|------|----------|------|
| `id` | BIGINT (PK) | AUTO_INCREMENT | 세션 ID |
| `table_id` | BIGINT (FK) | NOT NULL, REFERENCES restaurant_tables(id) | 테이블 |
| `status` | VARCHAR(20) | NOT NULL, DEFAULT 'ACTIVE' | 세션 상태 |
| `active_user_count` | INT | NOT NULL, DEFAULT 0 | 현재 접속자 수 |
| `started_at` | TIMESTAMP | NOT NULL, DEFAULT NOW() | 세션 시작 시각 |
| `completed_at` | TIMESTAMP | | 이용 완료 시각 |

**상태값**: `ACTIVE`, `COMPLETED`

**인덱스**: PK(`id`), FK(`table_id`), INDEX(`table_id`, `status`)

**비즈니스 규칙**: 한 테이블에 ACTIVE 세션은 최대 1개

---

## 5. Category (카테고리)

| 필드 | 타입 | 제약조건 | 설명 |
|------|------|----------|------|
| `id` | BIGINT (PK) | AUTO_INCREMENT | 카테고리 ID |
| `store_id` | BIGINT (FK) | NOT NULL, REFERENCES stores(id) | 소속 매장 |
| `name` | VARCHAR(50) | NOT NULL | 카테고리명 |
| `display_order` | INT | NOT NULL, DEFAULT 0 | 노출 순서 |
| `created_at` | TIMESTAMP | NOT NULL, DEFAULT NOW() | 생성 시각 |
| `updated_at` | TIMESTAMP | NOT NULL, DEFAULT NOW() | 수정 시각 |

**인덱스**: PK(`id`), FK(`store_id`), INDEX(`store_id`, `display_order`)

---

## 6. Menu (메뉴)

| 필드 | 타입 | 제약조건 | 설명 |
|------|------|----------|------|
| `id` | BIGINT (PK) | AUTO_INCREMENT | 메뉴 ID |
| `category_id` | BIGINT (FK) | NOT NULL, REFERENCES categories(id) | 소속 카테고리 |
| `name` | VARCHAR(100) | NOT NULL | 메뉴명 |
| `price` | INT | NOT NULL, CHECK(price >= 0) | 가격 (원) |
| `description` | VARCHAR(500) | | 메뉴 설명 |
| `image_url` | VARCHAR(500) | | 이미지 파일 경로/URL |
| `badge_type` | VARCHAR(20) | | 뱃지 타입 |
| `display_order` | INT | NOT NULL, DEFAULT 0 | 노출 순서 |
| `is_available` | BOOLEAN | NOT NULL, DEFAULT TRUE | 판매 가능 여부 |
| `created_at` | TIMESTAMP | NOT NULL, DEFAULT NOW() | 생성 시각 |
| `updated_at` | TIMESTAMP | NOT NULL, DEFAULT NOW() | 수정 시각 |

**badge_type 값**: `POPULAR`, `NEW`, `EVENT`, `SOLD_OUT`, NULL(없음)

**인덱스**: PK(`id`), FK(`category_id`), INDEX(`category_id`, `display_order`)

---

## 7. Cart (장바구니)

| 필드 | 타입 | 제약조건 | 설명 |
|------|------|----------|------|
| `id` | BIGINT (PK) | AUTO_INCREMENT | 장바구니 ID |
| `table_session_id` | BIGINT (FK) | NOT NULL, UNIQUE, REFERENCES table_sessions(id) | 테이블 세션 |
| `created_at` | TIMESTAMP | NOT NULL, DEFAULT NOW() | 생성 시각 |
| `updated_at` | TIMESTAMP | NOT NULL, DEFAULT NOW() | 수정 시각 |

**인덱스**: PK(`id`), UNIQUE(`table_session_id`)

**비즈니스 규칙**: 세션당 장바구니 1개 (1:1 관계)

---

## 8. CartItem (장바구니 항목)

| 필드 | 타입 | 제약조건 | 설명 |
|------|------|----------|------|
| `id` | BIGINT (PK) | AUTO_INCREMENT | 항목 ID |
| `cart_id` | BIGINT (FK) | NOT NULL, REFERENCES carts(id) | 장바구니 |
| `menu_id` | BIGINT (FK) | NOT NULL, REFERENCES menus(id) | 메뉴 |
| `device_id` | VARCHAR(100) | NOT NULL | 담은 기기 식별자 |
| `quantity` | INT | NOT NULL, CHECK(quantity >= 1) | 수량 |
| `created_at` | TIMESTAMP | NOT NULL, DEFAULT NOW() | 생성 시각 |
| `updated_at` | TIMESTAMP | NOT NULL, DEFAULT NOW() | 수정 시각 |

**인덱스**: PK(`id`), FK(`cart_id`), FK(`menu_id`), UNIQUE(`cart_id`, `menu_id`, `device_id`)

**비즈니스 규칙**: 같은 기기에서 같은 메뉴를 다시 담으면 수량 증가 (UPSERT)

---

## 9. Order (주문)

| 필드 | 타입 | 제약조건 | 설명 |
|------|------|----------|------|
| `id` | BIGINT (PK) | AUTO_INCREMENT | 주문 ID |
| `table_session_id` | BIGINT (FK) | NOT NULL, REFERENCES table_sessions(id) | 테이블 세션 |
| `order_number` | VARCHAR(20) | NOT NULL, UNIQUE | 주문 번호 (표시용) |
| `total_amount` | INT | NOT NULL, CHECK(total_amount >= 0) | 총 금액 |
| `status` | VARCHAR(20) | NOT NULL, DEFAULT 'PENDING' | 주문 상태 |
| `created_at` | TIMESTAMP | NOT NULL, DEFAULT NOW() | 주문 시각 |
| `updated_at` | TIMESTAMP | NOT NULL, DEFAULT NOW() | 수정 시각 |

**상태값**: `PENDING`(대기중), `PREPARING`(준비중), `COMPLETED`(완료)

**상태 전이**: `PENDING` → `PREPARING` → `COMPLETED` (역방향 불가)

**인덱스**: PK(`id`), UNIQUE(`order_number`), FK(`table_session_id`), INDEX(`table_session_id`, `status`)

---

## 10. OrderItem (주문 항목)

| 필드 | 타입 | 제약조건 | 설명 |
|------|------|----------|------|
| `id` | BIGINT (PK) | AUTO_INCREMENT | 항목 ID |
| `order_id` | BIGINT (FK) | NOT NULL, REFERENCES orders(id) | 주문 |
| `menu_id` | BIGINT (FK) | REFERENCES menus(id) | 원본 메뉴 참조 |
| `menu_name` | VARCHAR(100) | NOT NULL | 주문 시점 메뉴명 (비정규화) |
| `quantity` | INT | NOT NULL, CHECK(quantity >= 1) | 수량 |
| `unit_price` | INT | NOT NULL, CHECK(unit_price >= 0) | 주문 시점 단가 (비정규화) |
| `created_at` | TIMESTAMP | NOT NULL, DEFAULT NOW() | 생성 시각 |

**인덱스**: PK(`id`), FK(`order_id`), FK(`menu_id`)

**비정규화 이유**: 메뉴명/가격이 나중에 변경되어도 주문 시점 정보 유지

---

## 11. OrderHistory (과거 주문 이력)

| 필드 | 타입 | 제약조건 | 설명 |
|------|------|----------|------|
| `id` | BIGINT (PK) | AUTO_INCREMENT | 이력 ID |
| `store_id` | BIGINT (FK) | NOT NULL, REFERENCES stores(id) | 매장 |
| `table_id` | BIGINT (FK) | NOT NULL, REFERENCES restaurant_tables(id) | 테이블 |
| `table_number` | INT | NOT NULL | 테이블 번호 (비정규화) |
| `session_id` | BIGINT | NOT NULL | 원본 세션 ID (참조용) |
| `order_data_json` | JSONB | NOT NULL | 주문 전체 데이터 (주문+항목) |
| `total_amount` | INT | NOT NULL | 세션 총 주문액 |
| `session_started_at` | TIMESTAMP | NOT NULL | 세션 시작 시각 |
| `completed_at` | TIMESTAMP | NOT NULL, DEFAULT NOW() | 이용 완료 시각 |

**인덱스**: PK(`id`), FK(`store_id`), FK(`table_id`), INDEX(`store_id`, `completed_at DESC`)

**비즈니스 규칙**: 이용 완료 시 해당 세션의 모든 주문을 JSON으로 직렬화하여 저장

---

## 멀티테넌트 데이터 격리

모든 쿼리는 `store_id`로 필터링하여 매장 간 데이터 격리:
- Category, Menu: `store_id` 기반 조회
- RestaurantTable, TableSession: `store_id` 기반 조회
- Order: `table_session_id` → `table_id` → `store_id` 조인
- AdminUser: `store_id`로 매장 소속 확인

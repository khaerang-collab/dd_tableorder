CREATE TABLE customer_profiles (
    id BIGSERIAL PRIMARY KEY,
    kakao_id BIGINT NOT NULL UNIQUE,
    nickname VARCHAR(100),
    gender VARCHAR(10),
    age_range VARCHAR(20),
    profile_image_url VARCHAR(500),
    visit_count INT NOT NULL DEFAULT 0,
    total_order_amount BIGINT NOT NULL DEFAULT 0,
    last_visit_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_customer_profiles_kakao ON customer_profiles(kakao_id);

-- FR-A07: 서비스 프로모션 설정
CREATE TABLE promotions (
    id BIGSERIAL PRIMARY KEY,
    store_id BIGINT NOT NULL REFERENCES stores(id),
    min_order_amount INT NOT NULL CHECK(min_order_amount > 0),
    reward_description VARCHAR(200) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_promotions_store_active ON promotions(store_id, is_active);

-- FR-C09: 직원 호출
CREATE TABLE staff_calls (
    id BIGSERIAL PRIMARY KEY,
    store_id BIGINT NOT NULL REFERENCES stores(id),
    table_id BIGINT NOT NULL REFERENCES restaurant_tables(id),
    table_number INT NOT NULL,
    reason VARCHAR(50) NOT NULL,
    custom_message VARCHAR(200),
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    resolved_at TIMESTAMP
);
CREATE INDEX idx_staff_calls_store_status ON staff_calls(store_id, status);

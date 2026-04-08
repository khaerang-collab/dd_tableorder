CREATE TABLE order_histories (
    id BIGSERIAL PRIMARY KEY,
    store_id BIGINT NOT NULL REFERENCES stores(id),
    table_id BIGINT NOT NULL REFERENCES restaurant_tables(id),
    table_number INT NOT NULL,
    session_id BIGINT NOT NULL,
    order_data_json JSONB NOT NULL,
    total_amount INT NOT NULL,
    session_started_at TIMESTAMP NOT NULL,
    completed_at TIMESTAMP NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_order_histories_store_date ON order_histories(store_id, completed_at DESC);

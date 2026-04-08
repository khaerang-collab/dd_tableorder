CREATE TABLE restaurant_tables (
    id BIGSERIAL PRIMARY KEY,
    store_id BIGINT NOT NULL REFERENCES stores(id),
    table_number INT NOT NULL,
    qr_code_url VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    UNIQUE(store_id, table_number)
);
CREATE INDEX idx_tables_store ON restaurant_tables(store_id);

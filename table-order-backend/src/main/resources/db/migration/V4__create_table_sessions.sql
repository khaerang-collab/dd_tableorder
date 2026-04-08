CREATE TABLE table_sessions (
    id BIGSERIAL PRIMARY KEY,
    table_id BIGINT NOT NULL REFERENCES restaurant_tables(id),
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    active_user_count INT NOT NULL DEFAULT 0,
    started_at TIMESTAMP NOT NULL DEFAULT NOW(),
    completed_at TIMESTAMP
);
CREATE INDEX idx_sessions_table_status ON table_sessions(table_id, status);

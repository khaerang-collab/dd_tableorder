CREATE TABLE orders (
    id BIGSERIAL PRIMARY KEY,
    table_session_id BIGINT NOT NULL REFERENCES table_sessions(id),
    order_number VARCHAR(20) NOT NULL UNIQUE,
    total_amount INT NOT NULL CHECK(total_amount >= 0),
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_orders_session_status ON orders(table_session_id, status);

CREATE TABLE order_items (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    menu_id BIGINT REFERENCES menus(id),
    menu_name VARCHAR(100) NOT NULL,
    quantity INT NOT NULL CHECK(quantity >= 1),
    unit_price INT NOT NULL CHECK(unit_price >= 0),
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_order_items_order ON order_items(order_id);

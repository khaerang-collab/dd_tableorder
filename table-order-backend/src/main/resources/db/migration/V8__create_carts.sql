CREATE TABLE carts (
    id BIGSERIAL PRIMARY KEY,
    table_session_id BIGINT NOT NULL UNIQUE REFERENCES table_sessions(id),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE cart_items (
    id BIGSERIAL PRIMARY KEY,
    cart_id BIGINT NOT NULL REFERENCES carts(id) ON DELETE CASCADE,
    menu_id BIGINT NOT NULL REFERENCES menus(id),
    customer_profile_id BIGINT REFERENCES customer_profiles(id),
    device_id VARCHAR(100) NOT NULL,
    quantity INT NOT NULL CHECK(quantity >= 1),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_cart_items_cart ON cart_items(cart_id);

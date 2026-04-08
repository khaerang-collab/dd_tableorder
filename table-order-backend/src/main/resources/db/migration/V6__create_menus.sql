CREATE TABLE menus (
    id BIGSERIAL PRIMARY KEY,
    category_id BIGINT NOT NULL REFERENCES categories(id),
    name VARCHAR(100) NOT NULL,
    price INT NOT NULL CHECK(price >= 0),
    description VARCHAR(500),
    image_url VARCHAR(500),
    badge_type VARCHAR(20),
    display_order INT NOT NULL DEFAULT 0,
    is_available BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_menus_category_order ON menus(category_id, display_order);

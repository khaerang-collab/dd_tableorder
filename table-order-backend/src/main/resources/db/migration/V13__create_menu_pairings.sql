-- FR-C07: 메뉴 추천 (페어링 데이터)
CREATE TABLE menu_pairings (
    id BIGSERIAL PRIMARY KEY,
    store_id BIGINT NOT NULL REFERENCES stores(id),
    menu_id BIGINT NOT NULL REFERENCES menus(id),
    paired_menu_id BIGINT NOT NULL REFERENCES menus(id),
    pair_count INT NOT NULL DEFAULT 0,
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    UNIQUE(store_id, menu_id, paired_menu_id)
);
CREATE INDEX idx_menu_pairings_menu ON menu_pairings(menu_id, pair_count DESC);

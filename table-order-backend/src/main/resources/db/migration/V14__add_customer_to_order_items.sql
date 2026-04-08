-- 주문 항목에 주문자 정보 추가
ALTER TABLE order_items ADD COLUMN customer_profile_id BIGINT REFERENCES customer_profiles(id);
ALTER TABLE order_items ADD COLUMN customer_nickname VARCHAR(100);

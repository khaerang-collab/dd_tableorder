-- H2 호환 시드 데이터
INSERT INTO stores (id, name, address, phone, notice, origin_info) VALUES
(1, '맛있는 식당', '서울시 강남구 테헤란로 123', '02-1234-5678', '주문 후 10분 내 서빙됩니다!', '쌀: 국내산');

INSERT INTO admin_users (id, store_id, username, password_hash, login_attempts) VALUES
(1, 1, 'admin', '$2a$10$AjyJKZajOwBKw.8gF2kQgeEuK/3iQRFTuRTKQFtTD7POB20cuagGC', 0);

INSERT INTO restaurant_tables (id, store_id, table_number, qr_code_url) VALUES
(1, 1, 1, '/customer?storeId=1&table=1'),
(2, 1, 2, '/customer?storeId=1&table=2'),
(3, 1, 3, '/customer?storeId=1&table=3'),
(4, 1, 4, '/customer?storeId=1&table=4'),
(5, 1, 5, '/customer?storeId=1&table=5');

INSERT INTO categories (id, store_id, name, display_order) VALUES
(1, 1, '추천 메뉴', 0),
(2, 1, '메인 메뉴', 1),
(3, 1, '사이드', 2),
(4, 1, '음료', 3);

INSERT INTO menus (id, category_id, name, price, description, badge_type, display_order, is_available) VALUES
(1, 1, '오늘의 스페셜 정식', 12000, '매일 바뀌는 특별 정식 메뉴', 'POPULAR', 0, true),
(2, 1, '한우 불고기 정식', 18000, '국내산 한우로 만든 불고기 정식', 'NEW', 1, true),
(3, 2, '김치찌개', 9000, '깊은 맛의 김치찌개', NULL, 0, true),
(4, 2, '된장찌개', 8000, '구수한 된장찌개', NULL, 1, true),
(5, 2, '비빔밥', 10000, '신선한 나물 비빔밥', NULL, 2, true),
(6, 3, '계란말이', 6000, '부드러운 계란말이', NULL, 0, true),
(7, 4, '아메리카노', 3000, '깔끔한 아메리카노', NULL, 0, true);

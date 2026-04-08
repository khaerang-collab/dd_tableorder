-- 초기 테스트 데이터 (local 프로파일에서만 사용)
-- spring.sql.init.mode=always 설정 필요

-- 매장
INSERT INTO stores (id, name, address, phone, notice, origin_info) VALUES
(1, '맛있는 식당', '서울시 강남구 테헤란로 123', '02-1234-5678', '주문 후 10분 내 서빙됩니다!', '쌀: 국내산, 김치: 국내산, 돼지고기: 국내산')
ON CONFLICT DO NOTHING;

-- 관리자 (비밀번호: admin1234)
INSERT INTO admin_users (id, store_id, username, password_hash) VALUES
(1, 1, 'admin', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy')
ON CONFLICT DO NOTHING;

-- 테이블
INSERT INTO restaurant_tables (id, store_id, table_number, qr_code_url) VALUES
(1, 1, 1, '/customer?storeId=1&table=1'),
(2, 1, 2, '/customer?storeId=1&table=2'),
(3, 1, 3, '/customer?storeId=1&table=3'),
(4, 1, 4, '/customer?storeId=1&table=4'),
(5, 1, 5, '/customer?storeId=1&table=5')
ON CONFLICT DO NOTHING;

-- 카테고리
INSERT INTO categories (id, store_id, name, display_order) VALUES
(1, 1, '추천 메뉴', 0),
(2, 1, '메인 메뉴', 1),
(3, 1, '사이드', 2),
(4, 1, '음료', 3)
ON CONFLICT DO NOTHING;

-- 메뉴
INSERT INTO menus (id, category_id, name, price, description, badge_type, display_order) VALUES
(1, 1, '오늘의 스페셜 정식', 12000, '매일 바뀌는 특별 정식 메뉴', 'POPULAR', 0),
(2, 1, '한우 불고기 정식', 18000, '국내산 한우로 만든 불고기 정식', 'NEW', 1),
(3, 2, '김치찌개', 9000, '깊은 맛의 김치찌개', NULL, 0),
(4, 2, '된장찌개', 8000, '구수한 된장찌개', NULL, 1),
(5, 2, '비빔밥', 10000, '신선한 나물 비빔밥', NULL, 2),
(6, 2, '제육볶음', 11000, '매콤한 제육볶음 정식', NULL, 3),
(7, 3, '계란말이', 6000, '부드러운 계란말이', NULL, 0),
(8, 3, '김치전', 7000, '바삭한 김치전', NULL, 1),
(9, 4, '아메리카노', 3000, '깔끔한 아메리카노', NULL, 0),
(10, 4, '콜라', 2000, '시원한 콜라', NULL, 1),
(11, 4, '사이다', 2000, '청량한 사이다', NULL, 2)
ON CONFLICT DO NOTHING;

-- 시퀀스 조정
SELECT setval('stores_id_seq', (SELECT MAX(id) FROM stores));
SELECT setval('admin_users_id_seq', (SELECT MAX(id) FROM admin_users));
SELECT setval('restaurant_tables_id_seq', (SELECT MAX(id) FROM restaurant_tables));
SELECT setval('categories_id_seq', (SELECT MAX(id) FROM categories));
SELECT setval('menus_id_seq', (SELECT MAX(id) FROM menus));

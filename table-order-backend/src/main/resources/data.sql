-- 초기 테스트 데이터 (local 프로파일에서만 사용)
-- spring.sql.init.mode=always 설정 필요

-- 매장
INSERT INTO stores (id, name, address, phone, notice, origin_info) VALUES
(1, 'Bistro ddocdoc', '서울시 강남구 테헤란로 123', '02-1234-5678', '모든 파스타는 주문 후 바로 조리합니다!', '소고기: 호주산, 올리브오일: 이탈리아산, 치즈: 프랑스산')
ON CONFLICT DO NOTHING;

-- 관리자 (비밀번호: admin1234)
INSERT INTO admin_users (id, store_id, username, password_hash) VALUES
(1, 1, 'admin', '$2b$10$AjyJKZajOwBKw.8gF2kQgeEuK/3iQRFTuRTKQFtTD7POB20cuagGC')
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
(1, 1, '시그니처', 0),
(2, 1, '파스타', 1),
(3, 1, '사이드', 2),
(4, 1, '음료', 3)
ON CONFLICT DO NOTHING;

-- 메뉴
INSERT INTO menus (id, category_id, name, price, description, badge_type, display_order, image_url) VALUES
(1, 1, '트러플 크림 리조또', 19000, '블랙 트러플과 파르메산 치즈의 진한 크림 리조또', 'POPULAR', 0, 'https://images.unsplash.com/photo-1476124369491-e7addf5db371?w=200&h=200&fit=crop'),
(2, 1, '토마토 바질 브루스케타', 14000, '구운 치아바타 위에 신선한 토마토와 바질', 'NEW', 1, 'https://images.unsplash.com/photo-1572695157366-5e585ab2b69f?w=200&h=200&fit=crop'),
(3, 2, '까르보나라', 16000, '정통 로마식 까르보나라, 판체타와 계란 노른자', NULL, 0, 'https://images.unsplash.com/photo-1612874742237-6526221588e3?w=200&h=200&fit=crop'),
(4, 2, '봉골레 파스타', 17000, '싱싱한 바지락이 듬뿍 들어간 화이트 와인 봉골레', NULL, 1, 'https://images.unsplash.com/photo-1563379926898-05f4575a45d8?w=200&h=200&fit=crop'),
(5, 2, '감바스 알 아히요', 18000, '새우와 마늘이 가득한 올리브오일 감바스', NULL, 2, 'https://images.unsplash.com/photo-1633504581786-316c8002b1b9?w=200&h=200&fit=crop'),
(6, 2, '마르게리타 피자', 15000, '모짜렐라, 토마토소스, 바질의 클래식 피자', NULL, 3, 'https://images.unsplash.com/photo-1574071318508-1cdbab80d002?w=200&h=200&fit=crop'),
(7, 3, '시저 샐러드', 12000, '로메인, 파르메산, 크루통의 클래식 시저 샐러드', NULL, 0, 'https://images.unsplash.com/photo-1550304943-4f24f54ddde9?w=200&h=200&fit=crop'),
(8, 3, '감자튀김', 8000, '바삭한 트러플 감자튀김', NULL, 1, 'https://images.unsplash.com/photo-1573080496219-bb080dd4f877?w=200&h=200&fit=crop'),
(9, 4, '아메리카노', 4500, '싱글 오리진 원두 아메리카노', NULL, 0, 'https://images.unsplash.com/photo-1509042239860-f550ce710b93?w=200&h=200&fit=crop'),
(10, 4, '레몬에이드', 6000, '생레몬을 짜서 만든 수제 레몬에이드', NULL, 1, 'https://images.unsplash.com/photo-1621263764928-df1444c5e859?w=200&h=200&fit=crop'),
(11, 4, '스파클링 워터', 3000, '산 펠레그리노 스파클링 워터', NULL, 2, 'https://images.unsplash.com/photo-1625772299848-391b6a87d7b3?w=200&h=200&fit=crop')
ON CONFLICT DO NOTHING;

-- 시퀀스 조정
SELECT setval('stores_id_seq', (SELECT MAX(id) FROM stores));
SELECT setval('admin_users_id_seq', (SELECT MAX(id) FROM admin_users));
SELECT setval('restaurant_tables_id_seq', (SELECT MAX(id) FROM restaurant_tables));
SELECT setval('categories_id_seq', (SELECT MAX(id) FROM categories));
SELECT setval('menus_id_seq', (SELECT MAX(id) FROM menus));

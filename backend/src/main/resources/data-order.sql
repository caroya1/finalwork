-- Order module seed data
USE dianping_order;

INSERT INTO dp_order (user_id, shop_id, amount, status, created_at, updated_at) VALUES
(1, 1,  3800, 2, NOW(), NOW()),
(1, 3,  15800, 2, NOW(), NOW()),
(1, 5,  6600, 2, NOW(), NOW()),
(2, 8,  12800, 2, NOW(), NOW()),
(2, 9,  8900, 1, NOW(), NOW()),
(3, 11, 9500, 2, NOW(), NOW()),
(3, 12, 29800, 2, NOW(), NOW()),
(4, 13, 6800, 2, NOW(), NOW()),
(4, 14, 5500, 1, NOW(), NOW()),
(5, 7,  7800, 2, NOW(), NOW()),
(5, 6,  45000, 2, NOW(), NOW()),
(1, 7,  5600, 0, NOW(), NOW());

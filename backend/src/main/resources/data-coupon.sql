-- Coupon module seed data
USE dianping_coupon;

INSERT INTO dp_coupon (shop_id, type, title, description, discount_amount, price, total_stock, remaining_stock, start_time, end_time, created_at, updated_at) VALUES
(1, 'normal',  '10元平价券', '满50可用', 10.00, 5.00, NULL, NULL, NULL, NULL, NOW(), NOW()),
(1, 'seckill', '30元特价券', '限时秒杀', 30.00, 1.00, 50, 50, DATE_ADD(NOW(), INTERVAL -1 HOUR), DATE_ADD(NOW(), INTERVAL 6 HOUR), NOW(), NOW()),
(3, 'normal',  '20元平价券', '满100可用', 20.00, 8.00, NULL, NULL, NULL, NULL, NOW(), NOW()),
(3, 'seckill', '50元特价券', '限量抢购', 50.00, 2.00, 30, 30, DATE_ADD(NOW(), INTERVAL -30 MINUTE), DATE_ADD(NOW(), INTERVAL 2 HOUR), NOW(), NOW());

INSERT INTO dp_coupon_purchase (coupon_id, user_id, amount, status, created_at) VALUES
(1, 1, 5.00, 'paid', NOW()),
(2, 1, 1.00, 'paid', NOW()),
(3, 2, 8.00, 'paid', NOW());

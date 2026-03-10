SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS `dp_coupon`;
CREATE TABLE `dp_coupon` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `shop_id` BIGINT NOT NULL COMMENT '店铺ID',
  `type` VARCHAR(32) NOT NULL COMMENT '类型: normal / seckill',
  `title` VARCHAR(128) NOT NULL COMMENT '标题',
  `description` VARCHAR(255) DEFAULT NULL COMMENT '描述',
  `discount_amount` DECIMAL(10,2) NOT NULL COMMENT '优惠金额',
  `price` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '售价',
  `total_stock` INT DEFAULT NULL COMMENT '总库存(seckill)',
  `remaining_stock` INT DEFAULT NULL COMMENT '剩余库存(seckill)',
  `start_time` DATETIME DEFAULT NULL COMMENT '秒杀开始时间',
  `end_time` DATETIME DEFAULT NULL COMMENT '秒杀结束时间',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_coupon_shop` (`shop_id`),
  KEY `idx_coupon_type` (`type`),
  KEY `idx_coupon_time` (`start_time`,`end_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='优惠券表';

DROP TABLE IF EXISTS `dp_coupon_purchase`;
CREATE TABLE `dp_coupon_purchase` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `coupon_id` BIGINT NOT NULL COMMENT '优惠券ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `amount` DECIMAL(10,2) NOT NULL COMMENT '支付金额',
  `status` VARCHAR(32) NOT NULL COMMENT '状态: paid / processing / refunded',
  `refund_reason` VARCHAR(255) DEFAULT NULL COMMENT '退款原因',
  `refunded_at` DATETIME DEFAULT NULL COMMENT '退款时间',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_coupon_purchase_coupon` (`coupon_id`),
  KEY `idx_coupon_purchase_user` (`user_id`),
  KEY `idx_coupon_purchase_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='优惠券购买记录';

INSERT INTO `dp_coupon` (`id`, `shop_id`, `type`, `title`, `description`, `discount_amount`, `price`, `total_stock`, `remaining_stock`, `start_time`, `end_time`) VALUES
(1, 1, 'normal', '10元平价券', '满50可用', 10.00, 5.00, NULL, NULL, NULL, NULL),
(2, 2, 'seckill', '30元特价券', '限时秒杀', 30.00, 1.00, 50, 50, '2026-03-10 10:00:00', '2026-04-10 22:00:00'),
(3, 3, 'normal', '20元平价券', '满100可用', 20.00, 8.00, NULL, NULL, NULL, NULL),
(4, 4, 'normal', '15元代金券', '粤菜通用', 15.00, 6.00, NULL, NULL, NULL, NULL),
(5, 5, 'seckill', '50元咖啡券', '周末秒杀', 50.00, 9.90, 80, 74, '2026-03-12 09:00:00', '2026-04-12 23:00:00'),
(6, 8, 'normal', '30元烤鸭券', '满200可用', 30.00, 12.00, NULL, NULL, NULL, NULL),
(7, 11, 'normal', '25元杭帮菜券', '晚市可用', 25.00, 9.90, NULL, NULL, NULL, NULL),
(8, 13, 'seckill', '18元咖啡秒杀', '工作日午间', 18.00, 3.90, 120, 119, '2026-03-11 11:00:00', '2026-04-11 14:00:00');

INSERT INTO `dp_coupon_purchase` (`id`, `coupon_id`, `user_id`, `amount`, `status`) VALUES
(1, 1, 1, 5.00, 'paid'),
(2, 2, 1, 1.00, 'paid'),
(3, 3, 2, 8.00, 'processing'),
(4, 4, 3, 6.00, 'paid'),
(5, 5, 4, 9.90, 'paid'),
(6, 6, 5, 12.00, 'refunded'),
(7, 7, 6, 9.90, 'paid'),
(8, 8, 7, 3.90, 'processing'),
(9, 2, 8, 1.00, 'paid'),
(10, 1, 9, 5.00, 'paid');

SET FOREIGN_KEY_CHECKS = 1;

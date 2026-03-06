-- Coupon module schema
CREATE DATABASE IF NOT EXISTS dianping_coupon DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE dianping_coupon;

DROP TABLE IF EXISTS dp_coupon_purchase;
DROP TABLE IF EXISTS dp_coupon;

CREATE TABLE dp_coupon (
  id              BIGINT       AUTO_INCREMENT PRIMARY KEY,
  shop_id         BIGINT       NOT NULL        COMMENT '店铺ID',
  type            VARCHAR(32)  NOT NULL        COMMENT '类型: normal / seckill',
  title           VARCHAR(128) NOT NULL        COMMENT '标题',
  description     VARCHAR(255) DEFAULT NULL    COMMENT '描述',
  discount_amount DECIMAL(10,2) NOT NULL       COMMENT '优惠金额',
  price           DECIMAL(10,2) DEFAULT 0.00   COMMENT '售价',
  total_stock     INT          DEFAULT NULL    COMMENT '总库存(seckill)',
  remaining_stock INT          DEFAULT NULL    COMMENT '剩余库存(seckill)',
  start_time      DATETIME     DEFAULT NULL    COMMENT '秒杀开始时间',
  end_time        DATETIME     DEFAULT NULL    COMMENT '秒杀结束时间',
  created_at      DATETIME     NOT NULL,
  updated_at      DATETIME     NOT NULL,
  INDEX idx_coupon_shop (shop_id),
  INDEX idx_coupon_type (type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='优惠券表';

CREATE TABLE dp_coupon_purchase (
  id          BIGINT       AUTO_INCREMENT PRIMARY KEY,
  coupon_id   BIGINT       NOT NULL        COMMENT '优惠券ID',
  user_id     BIGINT       NOT NULL        COMMENT '用户ID',
  amount      DECIMAL(10,2) NOT NULL       COMMENT '支付金额',
  status      VARCHAR(32)  NOT NULL        COMMENT '状态: paid / processing / refunded',
  refund_reason VARCHAR(255) DEFAULT NULL  COMMENT '退款原因',
  refunded_at DATETIME     DEFAULT NULL   COMMENT '退款时间',
  created_at  DATETIME     NOT NULL,
  INDEX idx_coupon_purchase_coupon (coupon_id),
  INDEX idx_coupon_purchase_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='优惠券购买记录';

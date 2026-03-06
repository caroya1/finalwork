-- Order module schema
CREATE DATABASE IF NOT EXISTS dianping_order DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE dianping_order;

DROP TABLE IF EXISTS dp_order;

CREATE TABLE dp_order (
  id            BIGINT       AUTO_INCREMENT PRIMARY KEY,
  user_id       BIGINT       NOT NULL        COMMENT '下单用户ID',
  shop_id       BIGINT       NOT NULL        COMMENT '店铺ID',
  amount        INT          NOT NULL        COMMENT '订单金额（分）',
  status        INT          NOT NULL DEFAULT 0 COMMENT '订单状态: 0-待支付 1-已支付 2-已完成 3-已取消',
  created_at    DATETIME     NOT NULL,
  updated_at    DATETIME     NOT NULL,
  INDEX idx_order_user (user_id),
  INDEX idx_order_shop (shop_id),
  INDEX idx_order_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单表';

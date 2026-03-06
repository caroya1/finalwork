-- Merchant module schema
CREATE DATABASE IF NOT EXISTS dianping_merchant DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE dianping_merchant;

DROP TABLE IF EXISTS dp_merchant;

CREATE TABLE dp_merchant (
  id            BIGINT       AUTO_INCREMENT PRIMARY KEY,
  name          VARCHAR(128) NOT NULL        COMMENT '商户名称',
  category      VARCHAR(64)  DEFAULT NULL    COMMENT '类别: 美食 / 酒店 / 休闲 等',
  city          VARCHAR(32)  DEFAULT NULL    COMMENT '所在城市',
  status        VARCHAR(32)  DEFAULT 'active' COMMENT '状态: active / disabled',
  rating        DECIMAL(3,2) DEFAULT 0.00    COMMENT '综合评分 0.00~5.00',
  created_at    DATETIME     NOT NULL,
  updated_at    DATETIME     NOT NULL,
  INDEX idx_merchant_city (city),
  INDEX idx_merchant_category (category)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商户表';

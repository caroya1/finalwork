-- Shop module schema
CREATE DATABASE IF NOT EXISTS dianping_shop DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE dianping_shop;

DROP TABLE IF EXISTS dp_shop_rating;
DROP TABLE IF EXISTS dp_shop_dish;
DROP TABLE IF EXISTS dp_shop;

CREATE TABLE dp_shop (
  id            BIGINT       AUTO_INCREMENT PRIMARY KEY,
  name          VARCHAR(128) NOT NULL        COMMENT '店铺名称',
  category      VARCHAR(64)  DEFAULT NULL    COMMENT '分类',
  tags          VARCHAR(128) DEFAULT NULL    COMMENT '标签，逗号分隔',
  city          VARCHAR(32)  DEFAULT NULL    COMMENT '所在城市',
  address       VARCHAR(255) DEFAULT NULL    COMMENT '详细地址',
  longitude     DOUBLE       DEFAULT NULL    COMMENT '经度',
  latitude      DOUBLE       DEFAULT NULL    COMMENT '纬度',
  merchant_id   BIGINT       DEFAULT NULL    COMMENT '所属商户ID',
  rating        DECIMAL(3,2) DEFAULT 0.00    COMMENT '店铺评分（实时均分）',
  created_at    DATETIME     NOT NULL,
  updated_at    DATETIME     NOT NULL,
  INDEX idx_shop_city (city),
  INDEX idx_shop_merchant (merchant_id),
  INDEX idx_shop_category (category)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='店铺表';

CREATE TABLE dp_shop_rating (
  id         BIGINT  AUTO_INCREMENT PRIMARY KEY,
  shop_id    BIGINT  NOT NULL COMMENT '店铺ID',
  user_id    BIGINT  NOT NULL COMMENT '评分用户ID',
  rating     DECIMAL(3,2) NOT NULL COMMENT '评分 0.0~5.0',
  created_at DATETIME NOT NULL,
  updated_at DATETIME NOT NULL,
  UNIQUE KEY uk_shop_user (shop_id, user_id),
  INDEX idx_shop_rating_shop (shop_id),
  INDEX idx_shop_rating_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商铺评分表';

CREATE TABLE dp_shop_dish (
  id            BIGINT       AUTO_INCREMENT PRIMARY KEY,
  shop_id       BIGINT       NOT NULL        COMMENT '店铺ID',
  user_id       BIGINT       DEFAULT NULL    COMMENT '添加者用户ID',
  name          VARCHAR(128) NOT NULL        COMMENT '菜品名称',
  price         DECIMAL(10,2) DEFAULT NULL   COMMENT '价格（元）',
  description   VARCHAR(500) DEFAULT NULL    COMMENT '菜品描述',
  image_url     VARCHAR(255) DEFAULT NULL    COMMENT '菜品图片',
  created_at    DATETIME     NOT NULL,
  INDEX idx_dish_shop (shop_id),
  INDEX idx_dish_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='店铺菜品表';

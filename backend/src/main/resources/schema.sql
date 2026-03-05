-- ============================================================
-- 类大众点评系统 - 数据库初始化脚本
-- 数据库: dianping
-- 字符集: utf8mb4
-- ============================================================

CREATE DATABASE IF NOT EXISTS dianping DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE dianping;

-- 按外键依赖顺序先删除子表，再删除父表
DROP TABLE IF EXISTS dp_user_follow;
DROP TABLE IF EXISTS dp_post_comment;
DROP TABLE IF EXISTS dp_post_like;
DROP TABLE IF EXISTS dp_shop_rating;
DROP TABLE IF EXISTS dp_shop_dish;
DROP TABLE IF EXISTS dp_coupon_purchase;
DROP TABLE IF EXISTS dp_coupon;
DROP TABLE IF EXISTS dp_recommendation_log;
DROP TABLE IF EXISTS dp_order;
DROP TABLE IF EXISTS dp_post;
DROP TABLE IF EXISTS dp_shop;
DROP TABLE IF EXISTS dp_merchant;
DROP TABLE IF EXISTS dp_user;

-- ============================================================
-- 1. 用户表
-- ============================================================
CREATE TABLE dp_user (
  id            BIGINT       AUTO_INCREMENT PRIMARY KEY,
  username      VARCHAR(64)  NOT NULL UNIQUE COMMENT '用户名（唯一）',
  email         VARCHAR(128) DEFAULT NULL    COMMENT '邮箱',
  phone         VARCHAR(20)  DEFAULT NULL    COMMENT '手机号',
  password_hash VARCHAR(128) NOT NULL        COMMENT 'BCrypt 加密后的密码',
  avatar_url    VARCHAR(255) DEFAULT NULL    COMMENT '头像地址',
  user_role     VARCHAR(32)  NOT NULL DEFAULT 'user' COMMENT '角色: user / merchant / admin',
  city          VARCHAR(64)  DEFAULT '上海'  COMMENT '用户默认城市',
  balance       DECIMAL(10,2) DEFAULT 0.00 COMMENT '账户余额',
  created_at    DATETIME     NOT NULL        COMMENT '创建时间',
  updated_at    DATETIME     NOT NULL        COMMENT '更新时间',
  INDEX idx_user_role (user_role),
  INDEX idx_user_city (city)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- ============================================================
-- 1.1 用户关注表
-- ============================================================
CREATE TABLE dp_user_follow (
  id            BIGINT       AUTO_INCREMENT PRIMARY KEY,
  follower_id   BIGINT       NOT NULL COMMENT '关注者用户ID',
  following_id  BIGINT       NOT NULL COMMENT '被关注用户ID',
  created_at    DATETIME     NOT NULL COMMENT '关注时间',
  UNIQUE KEY uk_follow (follower_id, following_id),
  INDEX idx_follow_follower (follower_id),
  INDEX idx_following (following_id),
  CONSTRAINT fk_follow_follower FOREIGN KEY (follower_id) REFERENCES dp_user(id) ON DELETE CASCADE,
  CONSTRAINT fk_following FOREIGN KEY (following_id) REFERENCES dp_user(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户关注表';

-- ============================================================
-- 2. 商户表
-- ============================================================
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

-- ============================================================
-- 3. 店铺表
-- ============================================================
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
  INDEX idx_shop_category (category),
  CONSTRAINT fk_shop_merchant FOREIGN KEY (merchant_id) REFERENCES dp_merchant(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='店铺表';

-- ============================================================
-- 4. 优惠券表
-- ============================================================
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
  INDEX idx_coupon_type (type),
  CONSTRAINT fk_coupon_shop FOREIGN KEY (shop_id) REFERENCES dp_shop(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='优惠券表';

-- ============================================================
-- 5. 优惠券购买记录
-- ============================================================
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
  INDEX idx_coupon_purchase_user (user_id),
  CONSTRAINT fk_coupon_purchase_coupon FOREIGN KEY (coupon_id) REFERENCES dp_coupon(id) ON DELETE CASCADE,
  CONSTRAINT fk_coupon_purchase_user FOREIGN KEY (user_id) REFERENCES dp_user(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='优惠券购买记录';

-- ============================================================
-- 6. 订单表
-- ============================================================
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
  INDEX idx_order_status (status),
  CONSTRAINT fk_order_user FOREIGN KEY (user_id) REFERENCES dp_user(id) ON DELETE CASCADE,
  CONSTRAINT fk_order_shop FOREIGN KEY (shop_id) REFERENCES dp_shop(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单表';

-- ============================================================
-- 7. 推荐日志表（只写不更新）
-- ============================================================
CREATE TABLE dp_recommendation_log (
  id            BIGINT       AUTO_INCREMENT PRIMARY KEY,
  user_id       BIGINT       NOT NULL        COMMENT '用户ID',
  shop_id       BIGINT       NOT NULL        COMMENT '被推荐店铺ID',
  scene         VARCHAR(255) NOT NULL        COMMENT '推荐场景描述',
  action        VARCHAR(64)  NOT NULL        COMMENT '动作: recommend / click / convert',
  created_at    DATETIME     NOT NULL,
  INDEX idx_reclog_user (user_id),
  INDEX idx_reclog_shop (shop_id),
  CONSTRAINT fk_reclog_user FOREIGN KEY (user_id) REFERENCES dp_user(id) ON DELETE CASCADE,
  CONSTRAINT fk_reclog_shop FOREIGN KEY (shop_id) REFERENCES dp_shop(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='推荐日志表';

-- ============================================================
-- 8. 帖子/笔记表
-- ============================================================
CREATE TABLE dp_post (
  id            BIGINT       AUTO_INCREMENT PRIMARY KEY,
  user_id       BIGINT       DEFAULT NULL    COMMENT '发布者ID',
  shop_id       BIGINT       DEFAULT NULL    COMMENT '关联店铺ID',
  title         VARCHAR(255) NOT NULL        COMMENT '标题',
  content       TEXT         DEFAULT NULL    COMMENT '正文内容',
  cover_url     VARCHAR(255) DEFAULT NULL    COMMENT '封面图地址',
  city          VARCHAR(64)  DEFAULT NULL    COMMENT '所属城市',
  tags          VARCHAR(128) DEFAULT NULL    COMMENT '标签，逗号分隔',
  likes         INT          DEFAULT 0       COMMENT '点赞数（实时统计）',
  created_at    DATETIME     NOT NULL,
  updated_at    DATETIME     NOT NULL,
  INDEX idx_post_city (city),
  INDEX idx_post_user (user_id),
  INDEX idx_post_shop (shop_id),
  CONSTRAINT fk_post_user FOREIGN KEY (user_id) REFERENCES dp_user(id) ON DELETE SET NULL,
  CONSTRAINT fk_post_shop FOREIGN KEY (shop_id) REFERENCES dp_shop(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='帖子/笔记表';

-- ============================================================
-- 9. 帖子点赞表（一人一次）
-- ============================================================
CREATE TABLE dp_post_like (
  id         BIGINT  AUTO_INCREMENT PRIMARY KEY,
  post_id    BIGINT  NOT NULL COMMENT '帖子ID',
  user_id    BIGINT  NOT NULL COMMENT '点赞用户ID',
  created_at DATETIME NOT NULL,
  UNIQUE KEY uk_post_user (post_id, user_id),
  INDEX idx_post_like_post (post_id),
  INDEX idx_post_like_user (user_id),
  CONSTRAINT fk_post_like_post FOREIGN KEY (post_id) REFERENCES dp_post(id) ON DELETE CASCADE,
  CONSTRAINT fk_post_like_user FOREIGN KEY (user_id) REFERENCES dp_user(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='帖子点赞表';

-- ============================================================
-- 10. 帖子评论表
-- ============================================================
CREATE TABLE dp_post_comment (
  id         BIGINT  AUTO_INCREMENT PRIMARY KEY,
  post_id    BIGINT  NOT NULL COMMENT '帖子ID',
  user_id    BIGINT  NOT NULL COMMENT '评论用户ID',
  content    VARCHAR(500) NOT NULL COMMENT '评论内容',
  created_at DATETIME NOT NULL,
  INDEX idx_post_comment_post (post_id),
  INDEX idx_post_comment_user (user_id),
  CONSTRAINT fk_post_comment_post FOREIGN KEY (post_id) REFERENCES dp_post(id) ON DELETE CASCADE,
  CONSTRAINT fk_post_comment_user FOREIGN KEY (user_id) REFERENCES dp_user(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='帖子评论表';

-- ============================================================
-- 11. 商铺评分表（一人一次，可更新）
-- ============================================================
CREATE TABLE dp_shop_rating (
  id         BIGINT  AUTO_INCREMENT PRIMARY KEY,
  shop_id    BIGINT  NOT NULL COMMENT '店铺ID',
  user_id    BIGINT  NOT NULL COMMENT '评分用户ID',
  rating     DECIMAL(3,2) NOT NULL COMMENT '评分 0.0~5.0',
  created_at DATETIME NOT NULL,
  updated_at DATETIME NOT NULL,
  UNIQUE KEY uk_shop_user (shop_id, user_id),
  INDEX idx_shop_rating_shop (shop_id),
  INDEX idx_shop_rating_user (user_id),
  CONSTRAINT fk_shop_rating_shop FOREIGN KEY (shop_id) REFERENCES dp_shop(id) ON DELETE CASCADE,
  CONSTRAINT fk_shop_rating_user FOREIGN KEY (user_id) REFERENCES dp_user(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商铺评分表';

-- ============================================================
-- 12. 店铺菜品/菜谱表（食客可添加）
-- ============================================================
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
  INDEX idx_dish_user (user_id),
  CONSTRAINT fk_dish_shop FOREIGN KEY (shop_id) REFERENCES dp_shop(id) ON DELETE CASCADE,
  CONSTRAINT fk_dish_user FOREIGN KEY (user_id) REFERENCES dp_user(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='店铺菜品表';

-- User/Auth module schema
CREATE DATABASE IF NOT EXISTS dianping_user DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE dianping_user;

DROP TABLE IF EXISTS dp_user_follow;
DROP TABLE IF EXISTS dp_user;

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

CREATE TABLE dp_user_follow (
  id            BIGINT       AUTO_INCREMENT PRIMARY KEY,
  follower_id   BIGINT       NOT NULL COMMENT '关注者用户ID',
  following_id  BIGINT       NOT NULL COMMENT '被关注用户ID',
  created_at    DATETIME     NOT NULL COMMENT '关注时间',
  UNIQUE KEY uk_follow (follower_id, following_id),
  INDEX idx_follow_follower (follower_id),
  INDEX idx_following (following_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户关注表';

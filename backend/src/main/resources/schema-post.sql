-- Post module schema
CREATE DATABASE IF NOT EXISTS dianping_post DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE dianping_post;

DROP TABLE IF EXISTS dp_post_comment;
DROP TABLE IF EXISTS dp_post_like;
DROP TABLE IF EXISTS dp_post;

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
  INDEX idx_post_shop (shop_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='帖子/笔记表';

CREATE TABLE dp_post_like (
  id         BIGINT  AUTO_INCREMENT PRIMARY KEY,
  post_id    BIGINT  NOT NULL COMMENT '帖子ID',
  user_id    BIGINT  NOT NULL COMMENT '点赞用户ID',
  created_at DATETIME NOT NULL,
  UNIQUE KEY uk_post_user (post_id, user_id),
  INDEX idx_post_like_post (post_id),
  INDEX idx_post_like_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='帖子点赞表';

CREATE TABLE dp_post_comment (
  id         BIGINT  AUTO_INCREMENT PRIMARY KEY,
  post_id    BIGINT  NOT NULL COMMENT '帖子ID',
  user_id    BIGINT  NOT NULL COMMENT '评论用户ID',
  content    VARCHAR(500) NOT NULL COMMENT '评论内容',
  created_at DATETIME NOT NULL,
  INDEX idx_post_comment_post (post_id),
  INDEX idx_post_comment_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='帖子评论表';

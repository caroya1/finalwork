-- Recommendation module schema
CREATE DATABASE IF NOT EXISTS dianping_recommendation DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE dianping_recommendation;

DROP TABLE IF EXISTS dp_recommendation_log;

CREATE TABLE dp_recommendation_log (
  id            BIGINT       AUTO_INCREMENT PRIMARY KEY,
  user_id       BIGINT       NOT NULL        COMMENT '用户ID',
  shop_id       BIGINT       NOT NULL        COMMENT '被推荐店铺ID',
  scene         VARCHAR(255) NOT NULL        COMMENT '推荐场景描述',
  action        VARCHAR(64)  NOT NULL        COMMENT '动作: recommend / click / convert',
  created_at    DATETIME     NOT NULL,
  INDEX idx_reclog_user (user_id),
  INDEX idx_reclog_shop (shop_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='推荐日志表';

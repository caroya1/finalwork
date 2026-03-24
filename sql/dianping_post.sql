SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS `dp_post`;
CREATE TABLE `dp_post` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT DEFAULT NULL COMMENT '发布者ID',
  `shop_id` BIGINT DEFAULT NULL COMMENT '关联店铺ID',
  `title` VARCHAR(255) NOT NULL COMMENT '标题',
  `content` TEXT COMMENT '正文内容',
  `cover_url` VARCHAR(500) DEFAULT NULL COMMENT '封面图地址',
  `city` VARCHAR(64) DEFAULT NULL COMMENT '所属城市',
  `tags` VARCHAR(255) DEFAULT NULL COMMENT '标签，逗号分隔',
  `likes` INT NOT NULL DEFAULT 0 COMMENT '点赞数',
  `audit_status` TINYINT NOT NULL DEFAULT 1 COMMENT '审核状态:0-待审核,1-通过,2-拒绝',
  `audit_remark` VARCHAR(500) DEFAULT NULL COMMENT '审核备注',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_post_city` (`city`),
  KEY `idx_post_user` (`user_id`),
  KEY `idx_post_shop` (`shop_id`),
  KEY `idx_post_audit_status` (`audit_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='帖子/笔记表';

DROP TABLE IF EXISTS `dp_post_comment`;
CREATE TABLE `dp_post_comment` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `post_id` BIGINT NOT NULL COMMENT '帖子ID',
  `user_id` BIGINT NOT NULL COMMENT '评论用户ID',
  `content` VARCHAR(500) NOT NULL COMMENT '评论内容',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_post_comment_post` (`post_id`),
  KEY `idx_post_comment_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='帖子评论表';

DROP TABLE IF EXISTS `dp_post_like`;
CREATE TABLE `dp_post_like` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `post_id` BIGINT NOT NULL COMMENT '帖子ID',
  `user_id` BIGINT NOT NULL COMMENT '点赞用户ID',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_post_user` (`post_id`,`user_id`),
  KEY `idx_post_like_post` (`post_id`),
  KEY `idx_post_like_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='帖子点赞表';

INSERT INTO `dp_post` (`id`, `user_id`, `shop_id`, `title`, `content`, `city`, `tags`, `likes`, `audit_status`) VALUES
(1, 1, 1, '老盛昌夜宵体验', '深夜也能吃到热腾腾的汤包。', '上海', '夜宵,面食', 3, 1),
(2, 2, 2, '海底捞服务体验', '服务很好，适合朋友聚餐。', '上海', '火锅,聚餐', 2, 1),
(3, 2, 3, '全聚德烤鸭', '皮脆肉嫩，很推荐。', '北京', '烤鸭,美食', 1, 0),
(4, 3, 4, '广州早茶攻略', '陶陶居早茶选择很多。', '广州', '早茶,粤菜', 4, 1),
(5, 4, 5, '咖啡打卡地推荐', '烘焙工坊很适合拍照。', '上海', '咖啡,打卡', 7, 1),
(6, 5, 6, '外滩夜景酒店', '夜景不错，适合短住。', '上海', '酒店,夜景', 2, 1),
(7, 6, 8, '北京烤鸭二刷', '王府井店排队快一些。', '北京', '美食,旅游', 1, 1),
(8, 7, 9, '天安门周边住宿', '交通方便，周边景点多。', '北京', '酒店,旅游', 2, 1),
(9, 8, 10, '长隆带娃路线', '大马戏和欢乐世界都不错。', '广州', '亲子,游乐园', 5, 1),
(10, 9, 11, '杭州排队美食', '外婆家菜品稳定。', '杭州', '杭帮菜,排队', 2, 1),
(11, 10, 12, '龙井路景色', '环境很好适合约会。', '杭州', '环境,创意菜', 1, 0),
(12, 11, 13, '科技园下午茶', '安静适合办公。', '深圳', '咖啡,商务', 3, 1);

INSERT INTO `dp_post_comment` (`id`, `post_id`, `user_id`, `content`) VALUES
(1, 1, 2, '这家确实不错'),
(2, 1, 3, '已收藏'),
(3, 2, 1, '周末准备去'),
(4, 4, 5, '这份攻略很有用'),
(5, 5, 6, '打卡成功'),
(6, 9, 7, '带娃实测很棒'),
(7, 10, 8, '建议错峰去'),
(8, 12, 9, '适合远程办公');

INSERT INTO `dp_post_like` (`id`, `post_id`, `user_id`) VALUES
(1, 1, 2),
(2, 1, 3),
(3, 2, 1),
(4, 4, 1),
(5, 4, 2),
(6, 5, 1),
(7, 5, 3),
(8, 5, 4),
(9, 9, 5),
(10, 9, 6),
(11, 9, 7),
(12, 10, 8),
(13, 12, 9),
(14, 12, 10);

SET FOREIGN_KEY_CHECKS = 1;

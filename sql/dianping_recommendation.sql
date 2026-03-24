SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS `dp_recommendation_log`;
CREATE TABLE `dp_recommendation_log` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `shop_id` BIGINT NOT NULL COMMENT '被推荐店铺ID',
  `scene` VARCHAR(255) NOT NULL COMMENT '推荐场景描述',
  `action` VARCHAR(64) NOT NULL COMMENT '动作: recommend / click / convert',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_reclog_user` (`user_id`),
  KEY `idx_reclog_shop` (`shop_id`),
  KEY `idx_reclog_action` (`action`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='推荐日志表';

INSERT INTO `dp_recommendation_log` (`id`, `user_id`, `shop_id`, `scene`, `action`) VALUES
(1, 1, 1, '早餐推荐', 'recommend'),
(2, 1, 2, '朋友聚餐', 'recommend'),
(3, 1, 2, '朋友聚餐', 'click'),
(4, 2, 3, '北京必吃', 'recommend'),
(5, 2, 3, '北京必吃', 'convert'),
(6, 3, 4, '广州早茶', 'recommend'),
(7, 3, 4, '广州早茶', 'click'),
(8, 4, 5, '下午茶约会', 'recommend'),
(9, 4, 5, '下午茶约会', 'click'),
(10, 4, 5, '下午茶约会', 'convert'),
(11, 5, 6, '外滩夜景', 'recommend'),
(12, 6, 8, '北京旅游', 'recommend'),
(13, 7, 10, '亲子周末', 'recommend'),
(14, 8, 11, '杭州美食', 'recommend'),
(15, 9, 13, '深圳咖啡', 'recommend'),
(16, 10, 2, '火锅热榜', 'recommend'),
(17, 11, 1, '本地早餐', 'recommend'),
(18, 12, 9, '城市酒店', 'recommend');

SET FOREIGN_KEY_CHECKS = 1;

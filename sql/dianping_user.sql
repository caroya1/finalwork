SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS `dp_user`;
CREATE TABLE `dp_user` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `username` VARCHAR(64) NOT NULL COMMENT '用户名（唯一）',
  `email` VARCHAR(128) DEFAULT NULL COMMENT '邮箱',
  `phone` VARCHAR(20) DEFAULT NULL COMMENT '手机号',
  `password_hash` VARCHAR(128) NOT NULL COMMENT 'BCrypt加密密码',
  `avatar_url` VARCHAR(255) DEFAULT NULL COMMENT '头像地址',
  `user_role` VARCHAR(32) NOT NULL DEFAULT 'user' COMMENT '角色: user/merchant/admin',
  `city` VARCHAR(64) DEFAULT '上海' COMMENT '默认城市',
  `balance` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '账户余额',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 0-禁用,1-正常',
  `last_login_at` DATETIME DEFAULT NULL COMMENT '最后登录时间',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`),
  KEY `idx_user_role` (`user_role`),
  KEY `idx_user_city` (`city`),
  KEY `idx_user_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

DROP TABLE IF EXISTS `dp_admin`;
CREATE TABLE `dp_admin` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `username` VARCHAR(50) NOT NULL COMMENT '用户名',
  `password_hash` VARCHAR(255) NOT NULL COMMENT '密码哈希',
  `name` VARCHAR(50) DEFAULT NULL COMMENT '姓名',
  `email` VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
  `phone` VARCHAR(20) DEFAULT NULL COMMENT '手机号',
  `role` VARCHAR(20) NOT NULL DEFAULT 'admin' COMMENT '角色: super_admin/admin/operator/auditor',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 0-禁用,1-正常',
  `last_login_at` DATETIME DEFAULT NULL COMMENT '最后登录时间',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_admin_username` (`username`),
  KEY `idx_admin_status` (`status`),
  KEY `idx_admin_role` (`role`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='管理员表';

DROP TABLE IF EXISTS `dp_user_follow`;
CREATE TABLE `dp_user_follow` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `follower_id` BIGINT NOT NULL COMMENT '关注者用户ID',
  `following_id` BIGINT NOT NULL COMMENT '被关注用户ID',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_follow` (`follower_id`,`following_id`),
  KEY `idx_follow_follower` (`follower_id`),
  KEY `idx_following` (`following_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户关注表';

INSERT INTO `dp_user` (`id`, `username`, `email`, `phone`, `password_hash`, `user_role`, `city`, `balance`, `status`) VALUES
(1, 'zhangsan', 'zhangsan@qq.com', '13800001001', '$2a$10$mhdpd90RWnW78aD53o6Tt.tEXGuz1Agka6jQBeHleYOVeyqEwnyii', 'user', '上海', 200.00, 1),
(2, 'lisi', 'lisi@qq.com', '13800001002', '$2a$10$mhdpd90RWnW78aD53o6Tt.tEXGuz1Agka6jQBeHleYOVeyqEwnyii', 'user', '北京', 102.00, 1),
(3, 'wangwu', 'wangwu@qq.com', '13800001003', '$2a$10$mhdpd90RWnW78aD53o6Tt.tEXGuz1Agka6jQBeHleYOVeyqEwnyii', 'user', '广州', 80.00, 1),
(4, 'zhaoliu', 'zhaoliu@qq.com', '13800001004', '$2a$10$mhdpd90RWnW78aD53o6Tt.tEXGuz1Agka6jQBeHleYOVeyqEwnyii', 'user', '杭州', 50.00, 1),
(5, 'sunqi', 'sunqi@qq.com', '13800001005', '$2a$10$mhdpd90RWnW78aD53o6Tt.tEXGuz1Agka6jQBeHleYOVeyqEwnyii', 'user', '上海', 30.00, 1),
(6, 'zhouba', 'zhouba@qq.com', '13800001006', '$2a$10$mhdpd90RWnW78aD53o6Tt.tEXGuz1Agka6jQBeHleYOVeyqEwnyii', 'user', '成都', 410.00, 1),
(7, 'wujiu', 'wujiu@qq.com', '13800001007', '$2a$10$mhdpd90RWnW78aD53o6Tt.tEXGuz1Agka6jQBeHleYOVeyqEwnyii', 'user', '深圳', 65.50, 1),
(8, 'zhengshi', 'zhengshi@qq.com', '13800001008', '$2a$10$mhdpd90RWnW78aD53o6Tt.tEXGuz1Agka6jQBeHleYOVeyqEwnyii', 'user', '武汉', 125.00, 1),
(9, 'qianer', 'qianer@qq.com', '13800001009', '$2a$10$mhdpd90RWnW78aD53o6Tt.tEXGuz1Agka6jQBeHleYOVeyqEwnyii', 'user', '西安', 89.00, 1),
(10, 'sunshi', 'sunshi@qq.com', '13800001010', '$2a$10$mhdpd90RWnW78aD53o6Tt.tEXGuz1Agka6jQBeHleYOVeyqEwnyii', 'user', '重庆', 145.00, 1),
(11, 'liuqi', 'liuqi@qq.com', '13800001011', '$2a$10$mhdpd90RWnW78aD53o6Tt.tEXGuz1Agka6jQBeHleYOVeyqEwnyii', 'user', '南京', 75.00, 1),
(12, 'zhoumo', 'zhoumo@qq.com', '13800001012', '$2a$10$mhdpd90RWnW78aD53o6Tt.tEXGuz1Agka6jQBeHleYOVeyqEwnyii', 'user', '天津', 15.00, 1);

INSERT INTO `dp_admin` (`id`, `username`, `password_hash`, `name`, `email`, `phone`, `role`, `status`) VALUES
(1, 'admin', '$2a$10$mhdpd90RWnW78aD53o6Tt.tEXGuz1Agka6jQBeHleYOVeyqEwnyii', '系统管理员', 'admin@dianping.com', '13700001001', 'super_admin', 1),
(2, 'ops001', '$2a$10$mhdpd90RWnW78aD53o6Tt.tEXGuz1Agka6jQBeHleYOVeyqEwnyii', '运营一组', 'ops001@dianping.com', '13700001002', 'admin', 1),
(3, 'ops002', '$2a$10$mhdpd90RWnW78aD53o6Tt.tEXGuz1Agka6jQBeHleYOVeyqEwnyii', '运营二组', 'ops002@dianping.com', '13700001003', 'admin', 1),
(4, 'audit001', '$2a$10$mhdpd90RWnW78aD53o6Tt.tEXGuz1Agka6jQBeHleYOVeyqEwnyii', '内容审核A', 'audit001@dianping.com', '13700001004', 'auditor', 1),
(5, 'audit002', '$2a$10$mhdpd90RWnW78aD53o6Tt.tEXGuz1Agka6jQBeHleYOVeyqEwnyii', '内容审核B', 'audit002@dianping.com', '13700001005', 'auditor', 1),
(6, 'risk001', '$2a$10$mhdpd90RWnW78aD53o6Tt.tEXGuz1Agka6jQBeHleYOVeyqEwnyii', '风控专员', 'risk001@dianping.com', '13700001006', 'operator', 1),
(7, 'disabled01', '$2a$10$mhdpd90RWnW78aD53o6Tt.tEXGuz1Agka6jQBeHleYOVeyqEwnyii', '停用账号', 'disabled01@dianping.com', '13700001007', 'admin', 0);

INSERT INTO `dp_user_follow` (`follower_id`, `following_id`) VALUES
(1,2),(1,3),(1,4),(2,1),(2,3),(3,1),(3,2),(4,1),(5,1),(6,1),(7,2),(8,3),(9,4),(10,5),(11,6);

SET FOREIGN_KEY_CHECKS = 1;

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS `dp_merchant`;
CREATE TABLE `dp_merchant` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(100) NOT NULL COMMENT '商户名称',
  `contact_name` VARCHAR(50) DEFAULT NULL COMMENT '联系人姓名',
  `contact_phone` VARCHAR(20) DEFAULT NULL COMMENT '联系电话',
  `email` VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
  `password_hash` VARCHAR(255) DEFAULT NULL COMMENT '密码哈希',
  `status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态: 0-待审核,1-正常,2-禁用',
  `category` VARCHAR(50) DEFAULT NULL COMMENT '分类',
  `city` VARCHAR(50) DEFAULT NULL COMMENT '城市',
  `rating` DECIMAL(3,2) NOT NULL DEFAULT 0.00 COMMENT '评分',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_email` (`email`),
  KEY `idx_status` (`status`),
  KEY `idx_city` (`city`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商户表';

INSERT INTO `dp_merchant` (`id`, `name`, `contact_name`, `contact_phone`, `email`, `password_hash`, `status`, `category`, `city`, `rating`) VALUES
(1, '老盛昌汤包馆', '张经理', '13900001111', 'merchant1@dianping.com', '$2a$10$mhdpd90RWnW78aD53o6Tt.tEXGuz1Agka6jQBeHleYOVeyqEwnyii', 1, '美食', '上海', 4.50),
(2, '海底捞火锅', '李经理', '13900002222', 'merchant2@dianping.com', '$2a$10$mhdpd90RWnW78aD53o6Tt.tEXGuz1Agka6jQBeHleYOVeyqEwnyii', 1, '美食', '上海', 4.70),
(3, '全聚德烤鸭', '王经理', '13900003333', 'merchant3@dianping.com', '$2a$10$mhdpd90RWnW78aD53o6Tt.tEXGuz1Agka6jQBeHleYOVeyqEwnyii', 0, '美食', '北京', 4.60),
(4, '陶陶居', '陈经理', '13900004444', 'merchant4@dianping.com', '$2a$10$mhdpd90RWnW78aD53o6Tt.tEXGuz1Agka6jQBeHleYOVeyqEwnyii', 1, '美食', '广州', 4.55),
(5, '星巴克臻选', '赵经理', '13900005555', 'merchant5@dianping.com', '$2a$10$mhdpd90RWnW78aD53o6Tt.tEXGuz1Agka6jQBeHleYOVeyqEwnyii', 1, '咖啡', '上海', 4.30),
(6, '亚朵酒店', '孙经理', '13900006666', 'merchant6@dianping.com', '$2a$10$mhdpd90RWnW78aD53o6Tt.tEXGuz1Agka6jQBeHleYOVeyqEwnyii', 1, '酒店', '上海', 4.40),
(7, '如家精选', '周经理', '13900007777', 'merchant7@dianping.com', '$2a$10$mhdpd90RWnW78aD53o6Tt.tEXGuz1Agka6jQBeHleYOVeyqEwnyii', 1, '酒店', '北京', 4.10),
(8, '长隆欢乐世界', '吴经理', '13900008888', 'merchant8@dianping.com', '$2a$10$mhdpd90RWnW78aD53o6Tt.tEXGuz1Agka6jQBeHleYOVeyqEwnyii', 1, '休闲', '广州', 4.80),
(9, '外婆家', '郑经理', '13900009999', 'merchant9@dianping.com', '$2a$10$mhdpd90RWnW78aD53o6Tt.tEXGuz1Agka6jQBeHleYOVeyqEwnyii', 1, '美食', '杭州', 4.35),
(10, '绿茶餐厅', '钱经理', '13900001010', 'merchant10@dianping.com', '$2a$10$mhdpd90RWnW78aD53o6Tt.tEXGuz1Agka6jQBeHleYOVeyqEwnyii', 2, '美食', '杭州', 4.25),
(11, '万达影城', '冯经理', '13900001111', 'merchant11@dianping.com', '$2a$10$mhdpd90RWnW78aD53o6Tt.tEXGuz1Agka6jQBeHleYOVeyqEwnyii', 1, '电影', '上海', 4.20),
(12, '太平洋咖啡', '褚经理', '13900001212', 'merchant12@dianping.com', '$2a$10$mhdpd90RWnW78aD53o6Tt.tEXGuz1Agka6jQBeHleYOVeyqEwnyii', 1, '咖啡', '深圳', 4.15);

SET FOREIGN_KEY_CHECKS = 1;

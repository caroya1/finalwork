SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS `dp_shop`;
CREATE TABLE `dp_shop` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(128) NOT NULL COMMENT '店铺名称',
  `category` VARCHAR(64) DEFAULT NULL COMMENT '分类',
  `tags` VARCHAR(255) DEFAULT NULL COMMENT '标签，逗号分隔',
  `city` VARCHAR(32) DEFAULT NULL COMMENT '所在城市',
  `address` VARCHAR(255) DEFAULT NULL COMMENT '详细地址',
  `longitude` DOUBLE DEFAULT NULL COMMENT '经度',
  `latitude` DOUBLE DEFAULT NULL COMMENT '纬度',
  `merchant_id` BIGINT DEFAULT NULL COMMENT '所属商户ID',
  `image_url` VARCHAR(500) DEFAULT NULL COMMENT '店铺主图',
  `images` TEXT COMMENT '店铺图片JSON数组',
  `business_hours` VARCHAR(100) DEFAULT NULL COMMENT '营业时间',
  `contact_phone` VARCHAR(20) DEFAULT NULL COMMENT '联系电话',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 0-待审核,1-正常,2-禁用',
  `audit_status` TINYINT NOT NULL DEFAULT 0 COMMENT '审核状态: 0-待审核,1-通过,2-拒绝',
  `audit_remark` VARCHAR(500) DEFAULT NULL COMMENT '审核备注',
  `rating` DECIMAL(3,2) DEFAULT 0.00 COMMENT '店铺评分',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_shop_city` (`city`),
  KEY `idx_shop_merchant` (`merchant_id`),
  KEY `idx_shop_category` (`category`),
  KEY `idx_shop_audit_status` (`audit_status`),
  KEY `idx_shop_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='店铺表';

DROP TABLE IF EXISTS `dp_shop_dish`;
CREATE TABLE `dp_shop_dish` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `shop_id` BIGINT NOT NULL COMMENT '店铺ID',
  `user_id` BIGINT DEFAULT NULL COMMENT '添加者ID(商户)',
  `name` VARCHAR(128) NOT NULL COMMENT '菜品名称',
  `price` DECIMAL(10,2) DEFAULT NULL COMMENT '价格(元)',
  `description` VARCHAR(500) DEFAULT NULL COMMENT '菜品描述',
  `image_url` VARCHAR(500) DEFAULT NULL COMMENT '菜品图片',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态:0-下架,1-上架',
  `sales` INT NOT NULL DEFAULT 0 COMMENT '销量',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_dish_shop` (`shop_id`),
  KEY `idx_dish_user` (`user_id`),
  KEY `idx_dish_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='店铺菜品表';

DROP TABLE IF EXISTS `dp_shop_rating`;
CREATE TABLE `dp_shop_rating` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `shop_id` BIGINT NOT NULL COMMENT '店铺ID',
  `user_id` BIGINT NOT NULL COMMENT '评分用户ID',
  `rating` DECIMAL(3,2) NOT NULL COMMENT '评分 0.0~5.0',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_shop_user` (`shop_id`,`user_id`),
  KEY `idx_shop_rating_shop` (`shop_id`),
  KEY `idx_shop_rating_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商铺评分表';

INSERT INTO `dp_shop` (`id`, `name`, `category`, `tags`, `city`, `address`, `longitude`, `latitude`, `merchant_id`, `status`, `audit_status`, `rating`) VALUES
(1, '老盛昌·南京路店', '美食', '汤包,早餐,老字号', '上海', '南京东路步行街128号', 121.4737, 31.2304, 1, 1, 1, 4.43),
(2, '海底捞·徐汇店', '美食', '火锅,聚餐,服务好', '上海', '漕溪北路339号', 121.4368, 31.1888, 2, 1, 1, 4.70),
(3, '全聚德·前门店', '美食', '烤鸭,老字号,必吃', '北京', '前门大街30号', 116.3972, 39.8994, 3, 1, 0, 4.55),
(4, '陶陶居·上下九店', '美食', '早茶,粤菜,老字号', '广州', '上下九步行街12号', 113.2445, 23.1219, 4, 1, 1, 4.10),
(5, '星巴克臻选·烘焙工坊', '咖啡', '精品咖啡,下午茶,打卡', '上海', '南京西路789号', 121.4488, 31.2230, 5, 1, 1, 4.25),
(6, '亚朵·外滩店', '酒店', '商务,外滩景观', '上海', '中山东二路88号', 121.4908, 31.2355, 6, 1, 1, 4.45),
(7, '万达影城·五角场店', '电影', 'IMAX,热映', '上海', '淞沪路77号', 121.5110, 31.2980, 11, 1, 1, 4.10),
(8, '全聚德·王府井店', '美食', '烤鸭,旅游', '北京', '王府井大街88号', 116.4104, 39.9114, 3, 1, 1, 4.50),
(9, '如家·天安门店', '酒店', '经济型,交通便利', '北京', '西长安街28号', 116.3912, 39.9042, 7, 1, 1, 4.10),
(10, '长隆欢乐世界', '休闲', '游乐园,亲子,刺激', '广州', '大石街道迎宾路', 113.3290, 22.9979, 8, 1, 1, 4.10),
(11, '外婆家·西湖银泰店', '美食', '杭帮菜,家常菜,排队', '杭州', '延安路98号', 120.1689, 30.2567, 9, 1, 1, 4.20),
(12, '绿茶·龙井路店', '美食', '创意菜,环境好', '杭州', '龙井路83号', 120.1312, 30.2371, 10, 1, 0, 4.00),
(13, '太平洋咖啡·科技园店', '咖啡', '商务,安静', '深圳', '南山区科技南路18号', 113.9487, 22.5377, 12, 1, 1, 4.05);

INSERT INTO `dp_shop_dish` (`id`, `shop_id`, `user_id`, `name`, `price`, `description`, `status`, `sales`) VALUES
(1, 1, 1, '鲜肉汤包', 12.00, '招牌鲜肉汤包，皮薄馅大', 1, 120),
(2, 2, 2, '毛肚', 38.00, '口感脆爽', 1, 90),
(3, 3, 3, '挂炉烤鸭', 298.00, '百年挂炉工艺', 1, 60),
(4, 1, 1, '蟹黄汤包', 28.00, '蟹黄与鲜肉完美融合', 1, 80),
(5, 1, 1, '小馄饨', 10.00, '鲜肉小馄饨', 1, 75),
(6, 2, 2, '番茄锅底', 68.00, '酸甜可口番茄浓汤', 1, 66),
(7, 2, 2, '虾滑', 42.00, '手打虾滑Q弹鲜美', 1, 58),
(8, 4, 4, '虾饺皇', 38.00, '水晶虾饺，馅料饱满', 1, 42),
(9, 4, 4, '凤爪', 32.00, '豉汁蒸凤爪', 1, 51),
(10, 5, 5, '手冲咖啡', 58.00, '现场手冲单品咖啡', 1, 38),
(11, 5, 5, '抹茶拿铁', 42.00, '日式抹茶融合咖啡', 1, 44),
(12, 8, 3, '京酱肉丝', 58.00, '经典京菜', 1, 29),
(13, 11, 9, '东坡肉', 48.00, '杭帮菜经典', 1, 33),
(14, 11, 9, '龙井虾仁', 68.00, '龙井茶香配鲜虾仁', 1, 25),
(15, 13, 12, '美式咖啡', 28.00, '经典美式', 1, 50);

SET FOREIGN_KEY_CHECKS = 1;

-- Merchant module seed data
USE dianping_merchant;

INSERT INTO dp_merchant (name, category, city, status, rating, created_at, updated_at) VALUES
('老盛昌汤包馆',   '美食', '上海', 'active', 4.50, NOW(), NOW()),
('海底捞火锅',     '美食', '上海', 'active', 4.70, NOW(), NOW()),
('全聚德烤鸭',     '美食', '北京', 'active', 4.60, NOW(), NOW()),
('陶陶居',         '美食', '广州', 'active', 4.55, NOW(), NOW()),
('星巴克臻选',     '咖啡', '上海', 'active', 4.30, NOW(), NOW()),
('亚朵酒店',       '酒店', '上海', 'active', 4.40, NOW(), NOW()),
('如家精选',       '酒店', '北京', 'active', 4.10, NOW(), NOW()),
('长隆欢乐世界',   '休闲', '广州', 'active', 4.80, NOW(), NOW()),
('外婆家',         '美食', '杭州', 'active', 4.35, NOW(), NOW()),
('绿茶餐厅',       '美食', '杭州', 'active', 4.25, NOW(), NOW()),
('万达影城',       '电影', '上海', 'active', 4.20, NOW(), NOW()),
('太平洋咖啡',     '咖啡', '深圳', 'active', 4.15, NOW(), NOW());

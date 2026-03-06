-- User/Auth module seed data
USE dianping_user;

INSERT INTO dp_user (username, email, phone, password_hash, avatar_url, user_role, city, balance, created_at, updated_at) VALUES
('zhangsan',  'zhangsan@qq.com',  '13800001001', '$2a$10$mhdpd90RWnW78aD53o6Tt.tEXGuz1Agka6jQBeHleYOVeyqEwnyii', NULL, 'user',     '上海', 200.00, NOW(), NOW()),
('lisi',      'lisi@qq.com',      '13800001002', '$2a$10$mhdpd90RWnW78aD53o6Tt.tEXGuz1Agka6jQBeHleYOVeyqEwnyii', NULL, 'user',     '北京', 120.00, NOW(), NOW()),
('wangwu',    'wangwu@qq.com',    '13800001003', '$2a$10$mhdpd90RWnW78aD53o6Tt.tEXGuz1Agka6jQBeHleYOVeyqEwnyii', NULL, 'user',     '广州', 80.00, NOW(), NOW()),
('zhaoliu',   'zhaoliu@qq.com',   '13800001004', '$2a$10$mhdpd90RWnW78aD53o6Tt.tEXGuz1Agka6jQBeHleYOVeyqEwnyii', NULL, 'user',     '杭州', 50.00, NOW(), NOW()),
('sunqi',     'sunqi@qq.com',     '13800001005', '$2a$10$mhdpd90RWnW78aD53o6Tt.tEXGuz1Agka6jQBeHleYOVeyqEwnyii', NULL, 'user',     '上海', 30.00, NOW(), NOW()),
('merchant1', 'merchant1@qq.com', '13900001001', '$2a$10$mhdpd90RWnW78aD53o6Tt.tEXGuz1Agka6jQBeHleYOVeyqEwnyii', NULL, 'merchant', '上海', 0.00, NOW(), NOW()),
('merchant2', 'merchant2@qq.com', '13900001002', '$2a$10$mhdpd90RWnW78aD53o6Tt.tEXGuz1Agka6jQBeHleYOVeyqEwnyii', NULL, 'merchant', '北京', 0.00, NOW(), NOW()),
('merchant3', 'merchant3@qq.com', '13900001003', '$2a$10$mhdpd90RWnW78aD53o6Tt.tEXGuz1Agka6jQBeHleYOVeyqEwnyii', NULL, 'merchant', '广州', 0.00, NOW(), NOW()),
('admin',     'admin@dianping.com','13700001001','$2a$10$mhdpd90RWnW78aD53o6Tt.tEXGuz1Agka6jQBeHleYOVeyqEwnyii', NULL, 'admin',    '上海', 0.00, NOW(), NOW());

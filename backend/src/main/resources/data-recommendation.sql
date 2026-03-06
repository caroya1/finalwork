-- Recommendation module seed data
USE dianping_recommendation;

INSERT INTO dp_recommendation_log (user_id, shop_id, scene, action, created_at) VALUES
(1, 1,  '早餐推荐',   'recommend', NOW()),
(1, 3,  '朋友聚餐',   'recommend', NOW()),
(1, 3,  '朋友聚餐',   'click',     NOW()),
(2, 8,  '北京必吃',   'recommend', NOW()),
(2, 8,  '北京必吃',   'click',     NOW()),
(2, 8,  '北京必吃',   'convert',   NOW()),
(3, 11, '广州早茶',   'recommend', NOW()),
(3, 12, '带娃出游',   'recommend', NOW()),
(4, 13, '杭州美食',   'recommend', NOW()),
(5, 5,  '下午茶约会', 'recommend', NOW()),
(5, 5,  '下午茶约会', 'click',     NOW());

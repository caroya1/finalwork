-- ==========================================
-- 到店消费功能数据库修复脚本
-- 执行时间: 2025-04-07
-- ==========================================

-- 1. 修复 coupon-service: dp_coupon_purchase 表
-- 添加 used_at 字段用于记录优惠券核销时间
USE dianping_coupon;

ALTER TABLE dp_coupon_purchase 
ADD COLUMN IF NOT EXISTS used_at DATETIME NULL COMMENT '优惠券使用时间' 
AFTER status;

-- 验证修改
DESCRIBE dp_coupon_purchase;

-- ==========================================

-- 2. 修复 order-service: dp_order 表
-- 添加折扣金额和优惠券购买记录ID字段
USE dianping_order;

ALTER TABLE dp_order 
ADD COLUMN IF NOT EXISTS discount_amount INT NULL COMMENT '优惠金额（分）' 
AFTER amount,
ADD COLUMN IF NOT EXISTS coupon_purchase_id BIGINT NULL COMMENT '使用的优惠券购买记录ID' 
AFTER coupon_id;

-- 验证修改
DESCRIBE dp_order;

-- ==========================================
-- 修复完成
-- 请重启以下服务:
-- 1. coupon-service
-- 2. order-service
-- ==========================================

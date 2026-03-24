# 数据库索引优化建议

## 1. 用户相关表

### tb_user (用户表)
```sql
-- 已有主键索引 id
-- 建议添加的索引
CREATE UNIQUE INDEX idx_user_phone ON tb_user(phone);          -- 手机号唯一索引（登录用）
CREATE INDEX idx_user_nickname ON tb_user(nickname);           -- 昵称搜索
CREATE INDEX idx_user_created_at ON tb_user(created_at);       -- 注册时间排序
CREATE INDEX idx_user_status ON tb_user(status);               -- 状态筛选
```

### tb_user_follow (用户关注表)
```sql
-- 已有主键索引 id
-- 建议添加的索引
CREATE UNIQUE INDEX idx_follow_user_target ON tb_user_follow(user_id, target_user_id);  -- 防止重复关注
CREATE INDEX idx_follow_user_id ON tb_user_follow(user_id);      -- 查询用户的关注列表
CREATE INDEX idx_follow_target_id ON tb_user_follow(target_user_id);  -- 查询用户的粉丝列表
```

## 2. 商户相关表

### tb_merchant (商户表)
```sql
-- 已有主键索引 id
-- 建议添加的索引
CREATE UNIQUE INDEX idx_merchant_phone ON tb_merchant(phone);   -- 手机号唯一索引
CREATE INDEX idx_merchant_status ON tb_merchant(status);        -- 状态筛选
CREATE INDEX idx_merchant_created_at ON tb_merchant(created_at); -- 入驻时间排序
```

### tb_shop (店铺表)
```sql
-- 已有主键索引 id
-- 建议添加的索引
CREATE INDEX idx_shop_merchant_id ON tb_shop(merchant_id);      -- 商户查询店铺
CREATE INDEX idx_shop_category_id ON tb_shop(category_id);      -- 分类筛选
CREATE INDEX idx_shop_city ON tb_shop(city);                    -- 城市筛选
CREATE INDEX idx_shop_district ON tb_shop(district);            -- 区域筛选
CREATE INDEX idx_shop_score ON tb_shop(score DESC);             -- 评分排序
CREATE INDEX idx_shop_sales ON tb_shop(monthly_sales DESC);     -- 销量排序
CREATE INDEX idx_shop_status ON tb_shop(status);                -- 状态筛选
-- 复合索引（地理位置查询）
CREATE INDEX idx_shop_location ON tb_shop(city, district, area);
```

### tb_shop_dish (店铺菜品表)
```sql
-- 已有主键索引 id
-- 建议添加的索引
CREATE INDEX idx_dish_shop_id ON tb_shop_dish(shop_id);         -- 店铺查询菜品
CREATE INDEX idx_dish_category ON tb_shop_dish(category);       -- 菜品分类
```

## 3. 订单相关表

### tb_order (订单表)
```sql
-- 已有主键索引 id
-- 建议添加的索引
CREATE UNIQUE INDEX idx_order_no ON tb_order(order_no);         -- 订单号唯一索引
CREATE INDEX idx_order_user_id ON tb_order(user_id);            -- 用户查询订单
CREATE INDEX idx_order_shop_id ON tb_order(shop_id);            -- 店铺查询订单
CREATE INDEX idx_order_status ON tb_order(status);              -- 状态筛选
CREATE INDEX idx_order_created_at ON tb_order(created_at);      -- 时间排序
-- 复合索引（常用查询组合）
CREATE INDEX idx_order_user_status ON tb_order(user_id, status, created_at DESC);
CREATE INDEX idx_order_shop_status ON tb_order(shop_id, status, created_at DESC);
```

## 4. 优惠券相关表

### tb_coupon (优惠券表)
```sql
-- 已有主键索引 id
-- 建议添加的索引
CREATE INDEX idx_coupon_shop_id ON tb_coupon(shop_id);          -- 店铺查询优惠券
CREATE INDEX idx_coupon_status ON tb_coupon(status);            -- 状态筛选
CREATE INDEX idx_coupon_type ON tb_coupon(type);                -- 类型筛选
```

### tb_user_coupon (用户优惠券表)
```sql
-- 已有主键索引 id
-- 建议添加的索引
CREATE INDEX idx_uc_user_id ON tb_user_coupon(user_id);         -- 用户查询优惠券
CREATE INDEX idx_uc_coupon_id ON tb_user_coupon(coupon_id);     -- 优惠券查询
CREATE INDEX idx_uc_status ON tb_user_coupon(status);           -- 状态筛选
CREATE INDEX idx_uc_expire_time ON tb_user_coupon(expire_time); -- 过期时间筛选
-- 复合索引
CREATE INDEX idx_uc_user_status ON tb_user_coupon(user_id, status);
```

## 5. 帖子相关表

### tb_post (帖子表)
```sql
-- 已有主键索引 id
-- 建议添加的索引
CREATE INDEX idx_post_user_id ON tb_post(user_id);              -- 用户查询帖子
CREATE INDEX idx_post_shop_id ON tb_post(shop_id);              -- 店铺查询帖子
CREATE INDEX idx_post_status ON tb_post(status);                -- 状态筛选
CREATE INDEX idx_post_created_at ON tb_post(created_at DESC);   -- 时间排序
CREATE INDEX idx_post_likes ON tb_post(likes DESC);             -- 点赞数排序
```

### tb_post_comment (帖子评论表)
```sql
-- 已有主键索引 id
-- 建议添加的索引
CREATE INDEX idx_comment_post_id ON tb_post_comment(post_id);   -- 帖子查询评论
CREATE INDEX idx_comment_user_id ON tb_post_comment(user_id);   -- 用户评论
CREATE INDEX idx_comment_parent_id ON tb_post_comment(parent_id); -- 回复查询
CREATE INDEX idx_comment_created_at ON tb_post_comment(created_at);
```

## 6. 统计类表

### tb_shop_rating (店铺评分表)
```sql
-- 已有主键索引 id
-- 建议添加的索引
CREATE INDEX idx_rating_shop_id ON tb_shop_rating(shop_id);     -- 店铺查询评分
CREATE INDEX idx_rating_user_id ON tb_shop_rating(user_id);     -- 用户评分
CREATE INDEX idx_rating_order_id ON tb_shop_rating(order_id);   -- 订单评分
```

## 7. 系统配置表

### tb_admin (管理员表)
```sql
-- 已有主键索引 id
-- 建议添加的索引
CREATE UNIQUE INDEX idx_admin_username ON tb_admin(username);   -- 用户名唯一索引
CREATE INDEX idx_admin_role ON tb_admin(role);                  -- 角色筛选
```

## 索引优化原则

1. **最左前缀原则**: 复合索引查询条件要从最左列开始
2. **选择性**: 高选择性列（如user_id）放在复合索引前面
3. **避免冗余**: 已有复合索引 (a,b) 则不需要单独索引 (a)
4. **控制数量**: 单表索引不超过5个，过多影响写入性能
5. **定期维护**: 使用 `ANALYZE TABLE` 更新统计信息

## 查询优化建议

```sql
-- 使用 EXPLAIN 分析查询
EXPLAIN SELECT * FROM tb_order WHERE user_id = 1 AND status = 1;

-- 关注以下字段:
-- type: 访问类型，至少达到 range，最好是 ref 或 const
-- key: 实际使用的索引
-- rows: 扫描行数，越小越好
-- Extra: 避免 Using filesort 和 Using temporary
```

## 慢查询优化

```sql
-- 开启慢查询日志
SET GLOBAL slow_query_log = 'ON';
SET GLOBAL long_query_time = 2;  -- 超过2秒的查询记录

-- 查看慢查询
SELECT * FROM mysql.slow_log ORDER BY start_time DESC LIMIT 10;
```

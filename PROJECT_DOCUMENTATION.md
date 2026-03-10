# 类大众点评系统 - 完整项目文档

## 目录
1. [项目概述](#1-项目概述)
2. [系统架构](#2-系统架构)
3. [技术栈](#3-技术栈)
4. [项目结构](#4-项目结构)
5. [后端微服务](#5-后端微服务)
6. [前端应用](#6-前端应用)
7. [数据库设计](#7-数据库设计)
8. [核心功能实现](#8-核心功能实现)
9. [部署与运行](#9-部署与运行)
10. [API接口文档](#10-api接口文档)

---

## 1. 项目概述

### 1.1 项目简介
本项目是一个仿大众点评的本地生活服务平台，采用微服务架构，支持用户端、商户端和管理员端三端应用。系统核心聚焦于**大数据个性化推荐**与**高并发场景处理**，为用户提供商户发现、优惠券购买、帖子发布、社交互动等功能。

### 1.2 核心特性
- **个性化推荐引擎**：基于用户行为数据、地理位置、场景需求生成精准推荐
- **秒杀系统**：基于Redis+Lua脚本+消息队列的高并发秒杀实现
- **分布式微服务**：Spring Cloud Alibaba 生态，支持服务注册发现与配置中心
- **多端支持**：用户端(Vue3)、商户端(Vue3)、管理员端(Vue3)
- **高并发优化**：Redis缓存、异步处理、分布式锁

### 1.3 功能模块
| 模块 | 说明 |
|------|------|
| 用户系统 | 注册登录、个人信息、余额管理、关注功能 |
| 商户系统 | 商户入驻、店铺管理、菜品管理、评价管理 |
| 店铺系统 | 店铺搜索、分类筛选、评分系统、菜品展示 |
| 优惠券系统 | 普通券/秒杀券、购买/退款、库存管理 |
| 订单系统 | 订单创建、状态管理、核销处理 |
| 帖子系统 | 帖子发布、点赞、评论、关联店铺 |
| 推荐系统 | 个性化推荐、缓存优化、推荐日志 |

---

## 2. 系统架构

### 2.1 整体架构图

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                              前端层 (Frontend)                               │
├──────────────┬──────────────┬───────────────────────────────────────────────┤
│  用户端       │   商户端      │                  管理员端                      │
│  (Port 5173) │  (Port 5174) │                 (Port 5175)                    │
└──────┬───────┴──────┬───────┴───────────────────┬───────────────────────────┘
       │              │                           │
       └──────────────┴───────────┬───────────────┘
                                  │
                    ┌─────────────▼─────────────┐
                    │     API Gateway (8081)    │
                    │   Spring Cloud Gateway    │
                    └─────────────┬─────────────┘
                                  │
       ┌──────────────────────────┼──────────────────────────┐
       │                          │                          │
┌──────▼──────┐  ┌───────────────▼────────┐  ┌──────────────▼────────┐
│  Nacos      │  │     微服务层            │  │     基础设施层         │
│ (8848)      │  │  (Spring Boot 3.x)     │  │                       │
│ 注册/配置   │  │                        │  │  - MySQL 8.x          │
└─────────────┘  │  - auth-service        │  │  - Redis 6.x          │
                 │  - user-service        │  │  - RabbitMQ           │
                 │  - merchant-service    │  └───────────────────────┘
                 │  - shop-service        │
                 │  - coupon-service      │
                 │  - order-service       │
                 │  - post-service        │
                 │  - recommendation-svc  │
                 └────────────────────────┘
```

### 2.2 服务调用关系

```
                    ┌─────────────┐
                    │   Gateway   │
                    └──────┬──────┘
                           │
       ┌───────────────────┼───────────────────┐
       │                   │                   │
┌──────▼──────┐   ┌────────▼────────┐   ┌──────▼──────┐
│ Auth Service│   │  User Service   │   │ Shop Service│
└─────────────┘   └────────┬────────┘   └──────┬──────┘
                           │                   │
              ┌────────────┼────────────┐      │
              │            │            │      │
       ┌──────▼───┐ ┌──────▼───┐ ┌──────▼───┐  │
       │Post Port │ │CouponPort│ │Password  │  │
       │(Feign)   │ │(Feign)   │ │Port      │  │
       └──────────┘ └──────────┘ └──────────┘  │
                                               │
                                    ┌──────────▼──────────┐
                                    │   Post Service      │
                                    │   (Internal API)    │
                                    └─────────────────────┘
```

---

## 3. 技术栈

### 3.1 后端技术栈

| 技术 | 版本 | 用途 |
|------|------|------|
| Java | 17 | 编程语言 |
| Spring Boot | 3.2.5 | 应用框架 |
| Spring Cloud | 2023.0.2 | 微服务框架 |
| Spring Cloud Alibaba | 2023.0.1.0 | 阿里云服务集成 |
| Spring Cloud Gateway | 4.x | API网关 |
| Nacos | 2.x | 服务注册发现与配置中心 |
| MyBatis-Plus | 3.5.x | ORM框架 |
| MySQL | 8.x | 关系型数据库 |
| Redis | 6.x | 缓存数据库 |
| Redisson | 3.x | Redis分布式锁 |
| RabbitMQ | 3.x | 消息队列 |
| JWT | 0.11.5 | 身份认证 |
| Maven | 3.x | 构建工具 |

### 3.2 前端技术栈

| 技术 | 版本 | 用途 |
|------|------|------|
| Vue.js | 3.x | 前端框架 |
| Vite | 5.x | 构建工具 |
| Vue Router | 4.x | 路由管理 |
| Axios | 1.x | HTTP客户端 |

---

## 4. 项目结构

```
finalWork/
├── README.md                          # 项目简介
├── PROJECT_DOCUMENTATION.md           # 完整项目文档
├── 结合大模型的基于springboot和vue的类大众点评系统.md  # 需求文档
├── dianping_prompts.md                # AI提示词文档
│
├── backend/                           # 旧版后端（已废弃）
│   └── src/
│
├── gateway/                           # API网关服务
│   ├── pom.xml
│   └── src/main/java/com/dianping/gateway/
│       └── GatewayApplication.java
│
├── services/                          # 微服务模块
│   ├── pom.xml                        # 父POM
│   ├── common/                        # 公共模块
│   │   └── src/main/java/com/dianping/common/
│   │       ├── api/ApiResponse.java   # 统一响应封装
│   │       ├── dto/                   # 数据传输对象
│   │       ├── exception/             # 异常定义
│   │       ├── context/               # 用户上下文
│   │       └── port/                  # 服务端口接口
│   │
│   ├── auth-service/                  # 认证服务 (Port: 8082)
│   │   ├── pom.xml
│   │   └── src/main/java/com/dianping/auth/
│   │       ├── controller/AuthController.java
│   │       ├── service/AuthService.java
│   │       └── dto/
│   │
│   ├── user-service/                  # 用户服务 (Port: 8083)
│   │   └── src/main/java/com/dianping/user/
│   │       ├── controller/UserController.java
│   │       ├── service/UserService.java
│   │       ├── entity/User.java
│   │       ├── mapper/UserMapper.java
│   │       └── client/                # Feign客户端
│   │
│   ├── merchant-service/              # 商户服务 (Port: 8084)
│   │   └── src/main/java/com/dianping/merchant/
│   │       ├── controller/MerchantController.java
│   │       ├── service/MerchantService.java
│   │       └── entity/Merchant.java
│   │
│   ├── shop-service/                  # 店铺服务 (Port: 8085)
│   │   └── src/main/java/com/dianping/shop/
│   │       ├── controller/ShopController.java
│   │       ├── service/
│   │       │   ├── ShopService.java
│   │       │   ├── ShopRatingService.java
│   │       │   └── ShopDishService.java
│   │       ├── entity/
│   │       │   ├── Shop.java
│   │       │   ├── ShopRating.java
│   │       │   └── ShopDish.java
│   │       └── client/PostClient.java
│   │
│   ├── coupon-service/                # 优惠券服务 (Port: 8086)
│   │   └── src/main/java/com/dianping/coupon/
│   │       ├── controller/CouponController.java
│   │       ├── service/
│   │       │   ├── CouponService.java          # 秒杀核心逻辑
│   │       │   ├── UserCouponService.java
│   │       │   └── SeckillOrderConsumer.java   # 消息队列消费者
│   │       ├── entity/
│   │       │   ├── Coupon.java
│   │       │   └── CouponPurchase.java
│   │       ├── mapper/
│   │       └── config/
│   │           ├── RabbitMqConfig.java
│   │           └── StringRedisConfig.java
│   │
│   ├── order-service/                 # 订单服务 (Port: 8087)
│   │   └── src/main/java/com/dianping/order/
│   │       ├── controller/OrderController.java
│   │       ├── service/OrderService.java
│   │       └── entity/Order.java
│   │
│   ├── post-service/                  # 帖子服务 (Port: 8088)
│   │   └── src/main/java/com/dianping/post/
│   │       ├── controller/
│   │       │   ├── PostController.java
│   │       │   └── PostInternalController.java
│   │       ├── service/
│   │       │   ├── PostService.java
│   │       │   ├── PostLikeService.java
│   │       │   └── PostCommentService.java
│   │       ├── entity/
│   │       │   ├── Post.java
│   │       │   ├── PostLike.java
│   │       │   └── PostComment.java
│   │       └── mapper/
│   │
│   └── recommendation-service/        # 推荐服务 (Port: 8089)
│       └── src/main/java/com/dianping/recommendation/
│           ├── controller/RecommendationController.java
│           ├── service/RecommendationService.java
│           ├── entity/RecommendationLog.java
│           └── dto/RecommendationRequest.java
│
├── nacos-config/                      # Nacos配置文件
│   ├── dianping-gateway.yml           # 网关路由配置
│   ├── auth-service.yml
│   ├── user-service.yml
│   ├── merchant-service.yml
│   ├── shop-service.yml
│   ├── coupon-service.yml
│   ├── order-service.yml
│   ├── post-service.yml
│   └── recommendation-service.yml
│
├── frontend-user/                     # 用户端前端
│   ├── package.json
│   ├── vite.config.js
│   ├── index.html
│   └── src/
│       ├── main.js
│       ├── App.vue                    # 主应用组件
│       ├── api/                       # API接口封装
│       ├── router/                    # 路由配置
│       ├── styles/                    # 样式文件
│       └── views/
│           ├── UserHome.vue           # 首页
│           ├── ShopDetail.vue         # 店铺详情
│           ├── PostDetail.vue         # 帖子详情
│           ├── PostCreate.vue         # 发布帖子
│           ├── SearchResults.vue      # 搜索结果
│           ├── CategoryShops.vue      # 分类店铺
│           └── UserProfile.vue        # 个人中心
│
├── frontend-merchant/                 # 商户端前端
│   └── src/
│       ├── App.vue
│       └── views/MerchantHome.vue
│
└── frontend-admin/                    # 管理员端前端
    └── src/
        ├── App.vue
        └── views/AdminHome.vue
```

---

## 5. 后端微服务

### 5.1 服务端口分配

| 服务名 | 端口 | 说明 |
|--------|------|------|
| gateway | 8081 | API网关 |
| auth-service | 8082 | 认证服务 |
| user-service | 8083 | 用户服务 |
| merchant-service | 8084 | 商户服务 |
| shop-service | 8085 | 店铺服务 |
| coupon-service | 8086 | 优惠券服务 |
| order-service | 8087 | 订单服务 |
| post-service | 8088 | 帖子服务 |
| recommendation-service | 8089 | 推荐服务 |

### 5.2 认证服务 (auth-service)

**功能**：JWT认证、Token刷新、登出处理

**核心接口**：
```java
POST /api/auth/login          # 用户登录
POST /api/auth/refresh        # 刷新Token
POST /api/auth/logout         # 用户登出
```

**实现亮点**：
- 使用JJWT库生成和验证JWT Token
- 支持Access Token和Refresh Token双Token机制
- Token黑名单机制实现登出

### 5.3 用户服务 (user-service)

**功能**：用户CRUD、余额管理、城市切换、关注功能

**核心接口**：
```java
POST   /api/users                    # 创建用户
GET    /api/users                    # 用户列表
PUT    /api/users/{id}/city          # 更新城市
POST   /api/users/{id}/recharge      # 余额充值
GET    /api/users/{id}/profile       # 用户详情（聚合帖子、优惠券）
POST   /api/users/{id}/follow        # 关注用户
DELETE /api/users/{id}/follow        # 取消关注
```

**技术特点**：
- 使用CompletableFuture并行查询用户帖子列表和优惠券列表
- 自定义线程池`appTaskExecutor`处理异步任务

### 5.4 店铺服务 (shop-service)

**功能**：店铺管理、评分系统、菜品管理

**核心接口**：
```java
POST   /api/shops                    # 创建店铺
GET    /api/shops                    # 店铺列表（支持城市、分类、关键词筛选）
GET    /api/shops/{id}               # 店铺详情（聚合菜品、帖子）
POST   /api/shops/{id}/rate          # 评分
GET    /api/shops/{id}/dishes        # 获取菜品列表
POST   /api/shops/{id}/dishes        # 添加菜品
```

### 5.5 优惠券服务 (coupon-service) ⭐核心模块

**功能**：优惠券管理、秒杀系统、购买/退款

**核心接口**：
```java
POST   /api/coupons                  # 创建优惠券
GET    /api/coupons?shopId={id}      # 查询店铺优惠券
POST   /api/coupons/{id}/purchase    # 购买优惠券
POST   /api/coupons/purchase/{id}/refund  # 退款
```

**秒杀系统实现**：

1. **Redis Lua脚本原子操作**：
```java
// Lua脚本保证原子性
"local stock = tonumber(redis.call('get', KEYS[1]));" +
"if not stock then return -1 end;" +      // -1: 未初始化
"if stock <= 0 then return 0 end;" +      // 0: 库存不足
"if redis.call('exists', KEYS[2]) == 1 then return 2 end;" +  // 2: 已购买
"redis.call('decr', KEYS[1]);" +
"redis.call('set', KEYS[2], '1', 'EX', ARGV[1]);" +  // 标记用户已购买
"return 1;"  // 1: 成功
```

2. **秒杀流程**：
```
用户请求 → Redis预减库存 → 发送MQ消息 → 异步处理订单 → 更新数据库
                ↓
         库存不足/已购买 → 直接返回错误
```

3. **消息队列削峰**：
```java
// 生产者
rabbitTemplate.convertAndSend(seckillQueueName,
    new SeckillOrderMessage(coupon.getId(), userId));

// 消费者
@RabbitListener(queues = "${app.seckill.queue-name}")
public void consume(SeckillOrderMessage message) {
    couponService.handleSeckillOrder(message.getCouponId(), message.getUserId());
}
```

4. **分布式锁**：使用Redisson实现，防止重复消费

### 5.6 帖子服务 (post-service)

**功能**：帖子发布、点赞、评论

**核心接口**：
```java
GET    /api/posts                    # 帖子列表
GET    /api/posts/{id}               # 帖子详情
POST   /api/posts                    # 创建帖子
DELETE /api/posts/{id}               # 删除帖子
POST   /api/posts/{id}/comments      # 添加评论
POST   /api/posts/{id}/like          # 点赞
DELETE /api/posts/{id}/like          # 取消点赞
```

**技术特点**：
- 帖子详情聚合店铺信息、点赞状态、关注状态
- 使用缓存加速热门帖子访问

### 5.7 推荐服务 (recommendation-service) ⭐核心模块

**功能**：个性化推荐、推荐缓存、推荐日志

**核心接口**：
```java
POST /api/recommendations              # 获取推荐列表
```

**推荐算法**：
```java
public List<ShopSummary> recommend(RecommendationRequest request) {
    // 1. 构建缓存Key
    String cacheKey = "dp:rec:" + userId + ":" + city + ":" + scene;

    // 2. 先查缓存
    Object cached = redisTemplate.opsForValue().get(cacheKey);
    if (cached != null) return castList(cached);

    // 3. 查询店铺列表（当前实现为随机打乱）
    List<ShopSummary> shops = shopPort.listSummaries(city, null);
    Collections.shuffle(shops);
    List<ShopSummary> result = shops.subList(0, Math.min(10, shops.size()));

    // 4. 写入缓存
    redisTemplate.opsForValue().set(cacheKey, result, cacheTtlSeconds, TimeUnit.SECONDS);

    // 5. 异步记录推荐日志
    appTaskExecutor.execute(() -> logRecommendation(userId, result));

    return result;
}
```

---

## 6. 前端应用

### 6.1 用户端 (frontend-user)

**技术栈**：Vue 3 + Vite + Vue Router

**页面结构**：
```
/                    # 首页 - 推荐列表 + 探索发现
/search              # 搜索结果页
/category            # 分类店铺页
/shops/:id           # 店铺详情页
/posts/:id           # 帖子详情页
/posts/new           # 发布帖子页
/profile             # 个人中心
```

**核心组件**：

1. **App.vue** - 主应用组件
   - 顶部导航栏（Logo、城市选择、搜索框、登录/用户信息）
   - 搜索模式切换（普通搜索/智能推荐）
   - 登录/注册弹窗
   - 城市选择弹窗

2. **UserHome.vue** - 首页
   - 推荐结果列表
   - 分类快捷入口（美食、景点、酒店等）
   - 探索发现（帖子流）

3. **ShopDetail.vue** - 店铺详情
   - 店铺Banner展示
   - 评分功能
   - 菜品列表（支持添加）
   - 优惠券列表（支持购买）
   - 相关帖子

4. **PostDetail.vue** - 帖子详情
   - 帖子内容展示
   - 点赞/取消点赞
   - 关注/取消关注作者
   - 评论列表
   - 关联店铺信息

### 6.2 商户端 (frontend-merchant)

**功能**：商户管理后台（基础框架）

### 6.3 管理员端 (frontend-admin)

**功能**：平台管理后台（基础框架）

---

## 7. 数据库设计

### 7.1 数据库列表

| 数据库名 | 说明 |
|----------|------|
| dianping_user | 用户数据 |
| dianping_merchant | 商户数据 |
| dianping_shop | 店铺数据 |
| dianping_coupon | 优惠券数据 |
| dianping_order | 订单数据 |
| dianping_post | 帖子数据 |
| dianping_recommendation | 推荐日志 |

### 7.2 核心表结构

#### 用户表 (user)
```sql
CREATE TABLE user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    email VARCHAR(100),
    phone VARCHAR(20),
    city VARCHAR(50) DEFAULT '上海',
    balance DECIMAL(10,2) DEFAULT 0.00,
    user_role VARCHAR(20) DEFAULT 'user',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

#### 店铺表 (shop)
```sql
CREATE TABLE shop (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    merchant_id BIGINT,
    name VARCHAR(100) NOT NULL,
    category VARCHAR(50),
    city VARCHAR(50),
    address VARCHAR(255),
    tags VARCHAR(255),
    rating DECIMAL(2,1) DEFAULT 5.0,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

#### 优惠券表 (coupon)
```sql
CREATE TABLE coupon (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    shop_id BIGINT NOT NULL,
    type VARCHAR(20) NOT NULL,  -- 'normal' 或 'seckill'
    title VARCHAR(100) NOT NULL,
    description TEXT,
    discount_amount DECIMAL(10,2) NOT NULL,
    price DECIMAL(10,2) DEFAULT 0.00,
    total_stock INT,            -- 秒杀券总库存
    remaining_stock INT,        -- 秒杀券剩余库存
    start_time DATETIME,        -- 秒杀开始时间
    end_time DATETIME,          -- 秒杀结束时间
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

#### 优惠券购买记录表 (coupon_purchase)
```sql
CREATE TABLE coupon_purchase (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    coupon_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    amount DECIMAL(10,2),
    status VARCHAR(20) DEFAULT 'paid',  -- 'processing', 'paid', 'refunded'
    refund_reason TEXT,
    refunded_at DATETIME,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

#### 帖子表 (post)
```sql
CREATE TABLE post (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    shop_id BIGINT,
    title VARCHAR(200) NOT NULL,
    content TEXT,
    cover_url VARCHAR(500),
    city VARCHAR(50),
    tags VARCHAR(255),
    likes_count INT DEFAULT 0,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

#### 帖子点赞表 (post_like)
```sql
CREATE TABLE post_like (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    post_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_post_user (post_id, user_id)
);
```

#### 帖子评论表 (post_comment)
```sql
CREATE TABLE post_comment (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    post_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    content TEXT NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);
```

#### 推荐日志表 (recommendation_log)
```sql
CREATE TABLE recommendation_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    shop_id BIGINT NOT NULL,
    scene VARCHAR(100),
    action VARCHAR(50),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);
```

---

## 8. 核心功能实现

### 8.1 高并发秒杀系统

**架构设计**：
```
┌─────────────┐     ┌─────────────┐     ┌─────────────┐
│   用户请求   │────▶│  Redis预检  │────▶│  返回结果   │
└─────────────┘     └──────┬──────┘     └─────────────┘
                           │
                    ┌──────▼──────┐
                    │   发送MQ    │
                    └──────┬──────┘
                           │
                    ┌──────▼──────┐
                    │  消费者处理  │
                    │  分布式锁    │
                    │  数据库更新  │
                    └─────────────┘
```

**关键技术点**：
1. **Redis原子操作**：Lua脚本保证库存检查和扣减的原子性
2. **消息队列削峰**：RabbitMQ暂存请求，平滑处理高峰流量
3. **分布式锁**：Redisson防止重复消费和超卖
4. **缓存预热**：秒杀活动开始前将库存加载到Redis

### 8.2 个性化推荐系统

**当前实现**：
- 基于城市的店铺随机推荐
- Redis缓存推荐结果（默认5分钟）
- 异步记录推荐日志

**扩展方向**：
- 协同过滤算法
- 用户画像建模
- 场景匹配算法

### 8.3 服务间通信

**Feign客户端**：
```java
@FeignClient(name = "post-service", configuration = FeignConfig.class)
public interface PostClient {
    @GetMapping("/internal/posts")
    List<PostSummary> listSummaries(@RequestParam("city") String city,
                                     @RequestParam("shopId") Long shopId);
}
```

**内部API**：
- `/internal/posts` - 帖子服务内部接口
- `/internal/coupons` - 优惠券服务内部接口
- `/internal/password/verify` - 密码验证内部接口

---

## 9. 部署与运行

### 9.1 环境要求

- JDK 17+
- Maven 3.8+
- MySQL 8.0+
- Redis 6.0+
- RabbitMQ 3.8+
- Nacos 2.0+
- Node.js 18+

### 9.2 后端部署

**1. 启动基础设施**：
```bash
# 启动MySQL、Redis、RabbitMQ、Nacos
```

**2. 创建数据库**：
```sql
CREATE DATABASE dianping_user;
CREATE DATABASE dianping_merchant;
CREATE DATABASE dianping_shop;
CREATE DATABASE dianping_coupon;
CREATE DATABASE dianping_order;
CREATE DATABASE dianping_post;
CREATE DATABASE dianping_recommendation;
```

**3. 配置Nacos**：
在Nacos控制台添加各服务的YAML配置文件（见`nacos-config/`目录）

**4. 编译安装**：
```bash
# 安装common模块
mvn -f services/pom.xml -pl common -am clean install

# 编译全部服务
mvn -f services/pom.xml -DskipTests clean install
```

**5. 启动服务**：
```bash
# 按顺序启动各服务
mvn -f services/auth-service/pom.xml spring-boot:run
mvn -f services/user-service/pom.xml spring-boot:run
mvn -f services/merchant-service/pom.xml spring-boot:run
mvn -f services/shop-service/pom.xml spring-boot:run
mvn -f services/coupon-service/pom.xml spring-boot:run
mvn -f services/order-service/pom.xml spring-boot:run
mvn -f services/post-service/pom.xml spring-boot:run
mvn -f services/recommendation-service/pom.xml spring-boot:run

# 启动网关
mvn -f gateway/pom.xml spring-boot:run
```

### 9.3 前端部署

**用户端**：
```bash
cd frontend-user
npm install
npm run dev
```

**商户端**：
```bash
cd frontend-merchant
npm install
npm run dev
```

**管理员端**：
```bash
cd frontend-admin
npm install
npm run dev
```

---

## 10. API接口文档

### 10.1 认证接口

#### 登录
```http
POST /api/auth/login
Content-Type: application/json

{
  "username": "string",
  "password": "string"
}

Response:
{
  "success": true,
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "refreshToken": "dGhpcyBpcyBhIHJlZnJlc2g...",
    "userId": 1,
    "username": "user1",
    "role": "user",
    "balance": 100.00,
    "city": "上海"
  }
}
```

#### 刷新Token
```http
POST /api/auth/refresh
Content-Type: application/json

{
  "refreshToken": "dGhpcyBpcyBhIHJlZnJlc2g..."
}
```

#### 登出
```http
POST /api/auth/logout
Authorization: Bearer {token}
Content-Type: application/json

{
  "refreshToken": "dGhpcyBpcyBhIHJlZnJlc2g..."
}
```

### 10.2 用户接口

#### 创建用户
```http
POST /api/users
Content-Type: application/json

{
  "username": "string",
  "password": "string",
  "email": "string",
  "phone": "string",
  "city": "上海",
  "role": "user"
}
```

#### 获取用户详情
```http
GET /api/users/{id}/profile
Authorization: Bearer {token}

Response:
{
  "success": true,
  "data": {
    "id": 1,
    "username": "user1",
    "role": "user",
    "city": "上海",
    "balance": 100.00,
    "posts": [...],
    "coupons": [...]
  }
}
```

#### 余额充值
```http
POST /api/users/{id}/recharge
Authorization: Bearer {token}
Content-Type: application/json

{
  "amount": 100.00
}
```

### 10.3 店铺接口

#### 获取店铺列表
```http
GET /api/shops?city=上海&category=美食&keyword=火锅

Response:
{
  "success": true,
  "data": [
    {
      "id": 1,
      "name": "海底捞火锅",
      "category": "火锅",
      "city": "上海",
      "rating": 4.8
    }
  ]
}
```

#### 获取店铺详情
```http
GET /api/shops/{id}

Response:
{
  "success": true,
  "data": {
    "shop": {...},
    "dishes": [...],
    "posts": [...]
  }
}
```

#### 评分
```http
POST /api/shops/{id}/rate
Authorization: Bearer {token}
Content-Type: application/json

{
  "rating": 4.5,
  "comment": "服务很好"
}
```

### 10.4 优惠券接口

#### 创建优惠券
```http
POST /api/coupons
Content-Type: application/json

{
  "shopId": 1,
  "type": "seckill",
  "title": "限时秒杀券",
  "description": "全场通用",
  "discountAmount": 50.00,
  "price": 9.90,
  "totalStock": 100,
  "remainingStock": 100,
  "startTime": "2024-01-01T10:00:00",
  "endTime": "2024-01-01T12:00:00"
}
```

#### 购买优惠券
```http
POST /api/coupons/{id}/purchase
Authorization: Bearer {token}
Content-Type: application/json

{
  "userId": 1
}
```

### 10.5 帖子接口

#### 获取帖子列表
```http
GET /api/posts?city=上海&keyword=美食
```

#### 创建帖子
```http
POST /api/posts
Authorization: Bearer {token}
Content-Type: application/json

{
  "title": "帖子标题",
  "content": "帖子内容",
  "shopId": 1,
  "city": "上海",
  "tags": "美食,探店"
}
```

#### 点赞
```http
POST /api/posts/{id}/like
Authorization: Bearer {token}
```

#### 评论
```http
POST /api/posts/{id}/comments
Authorization: Bearer {token}
Content-Type: application/json

{
  "content": "评论内容"
}
```

### 10.6 推荐接口

#### 获取推荐
```http
POST /api/recommendations
Authorization: Bearer {token}
Content-Type: application/json

{
  "userId": 1,
  "city": "上海",
  "scene": "朋友聚餐"
}

Response:
{
  "success": true,
  "data": [
    {
      "id": 1,
      "name": "推荐店铺1",
      "category": "火锅",
      "city": "上海",
      "rating": 4.8
    }
  ]
}
```

---

## 附录

### A. 项目截图

（待补充）

### B. 性能测试

（待补充）

### C. 常见问题

**Q: 如何解决端口冲突？**
A: 修改各服务`application.yml`或Nacos配置中的`server.port`

**Q: 如何修改数据库连接？**
A: 在Nacos对应服务的配置文件中修改`spring.datasource`配置

**Q: Redis密码错误？**
A: 确保Nacos配置中的`spring.redis.password`与实际Redis密码一致

**Q: 如何配置腾讯云OSS？**
A: 在Nacos配置文件中修改以下配置：
```yaml
app:
  oss:
    tencent:
      secret-id: 你的SecretId
      secret-key: 你的SecretKey
      bucket-name: 你的存储桶名称
      region: ap-shanghai  # 根据实际地域修改
      max-size: 10485760  # 10MB
      allowed-types: image/jpeg,image/png,image/gif,image/webp
```

**Q: OSS文件上传失败？**
A: 检查以下几点：
1. SecretId和SecretKey是否正确
2. 存储桶名称和地域是否正确
3. 存储桶是否允许公网访问
4. 文件大小是否超过限制
5. 文件类型是否在允许列表中

---

## 11. 文件上传与OSS

### 11.1 腾讯云OSS集成

系统已集成腾讯云COS（对象存储）用于文件上传和存储。

**支持的文件类型**：
- 图片：JPG、PNG、GIF、WebP
- 单文件最大：10MB

**存储目录结构**：
```
posts/    # 帖子封面图片
shops/    # 店铺图片
users/    # 用户头像
dishes/   # 菜品图片
common/   # 其他通用图片
```

### 11.2 上传接口

#### 单文件上传
```http
POST /api/oss/upload/image
Content-Type: multipart/form-data

file: [图片文件]
dir: posts  # 可选，默认为common
```

**响应**：
```json
{
  "success": true,
  "data": {
    "url": "https://bucket.cos.ap-shanghai.myqcloud.com/posts/2026/03/10/xxx.jpg",
    "filename": "original.jpg",
    "size": 102400,
    "contentType": "image/jpeg"
  }
}
```

#### 批量上传
```http
POST /api/oss/upload/images
Content-Type: multipart/form-data

files: [图片文件1, 图片文件2, ...]
dir: posts  # 可选
```

#### 删除文件
```http
POST /api/oss/delete?fileUrl=xxx
```

### 11.3 前端使用示例

```vue
<template>
  <ImageUpload
    v-model="coverUrl"
    dir="posts"
    placeholder="上传封面图片"
    @success="onSuccess"
    @error="onError"
  />
</template>

<script setup>
import { ref } from 'vue';
import ImageUpload from '../components/ImageUpload.vue';

const coverUrl = ref('');

const onSuccess = (data) => {
  console.log('上传成功:', data.url);
};

const onError = (error) => {
  console.error('上传失败:', error);
};
</script>
```

---

**文档版本**: 1.1
**最后更新**: 2026-03-10
**作者**: Claude Code

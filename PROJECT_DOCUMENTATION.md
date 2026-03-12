# 类大众点评系统 - 完整项目文档

## 目录

1. [项目概述](#1-项目概述)
2. [系统架构](#2-系统架构)
3. [技术栈](#3-技术栈)
4. [项目结构](#4-项目结构)
5. [核心功能详解](#5-核心功能详解)
6. [部署与运行](#6-部署与运行)
7. [API接口文档](#7-api接口文档)
8. [性能优化与压测](#8-性能优化与压测)
9. [安全设计](#9-安全设计)
10. [开发规范](#10-开发规范)

---

## 1. 项目概述

### 1.1 项目简介

本项目是一个仿大众点评的本地生活服务平台，采用微服务架构，支持用户端、商户端和管理员端三端应用。系统核心聚焦于**大数据个性化推荐**与**高并发场景处理**，为用户提供商户发现、优惠券购买、帖子发布、社交互动等功能。

### 1.2 核心特性

- **个性化推荐引擎**：基于用户行为数据、地理位置、场景需求生成精准推荐
  - 新用户：热门推荐(80%) + 附近推荐(20%)
  - 老用户：协同过滤(70%) + 热门推荐(30%)
  - 定时预计算：每8分钟更新推荐数据
  
- **高并发秒杀系统**：基于Redis+Lua脚本+消息队列+分布式锁的高并发秒杀实现
  - 1000并发抢50库存测试通过
  - TPS达到363.50
  - 零超卖、零重复购买
  
- **分布式微服务**：Spring Cloud Alibaba 生态，支持服务注册发现与配置中心
- **多端支持**：用户端(Vue3)、商户端(Vue3)、管理员端(Vue3)
- **高可用设计**：熔断降级、限流、健康检查、手动确认MQ

### 1.3 功能模块完成情况

| 模块 | 功能 | 状态 |
|------|------|------|
| **用户系统** | 注册登录、个人信息、余额管理、关注功能 | ✅ 已完成 |
| **商户系统** | 商户入驻、店铺管理、菜品管理、评价管理 | ✅ 已完成 |
| **店铺系统** | 店铺搜索、分类筛选、评分系统、菜品展示 | ✅ 已完成 |
| **优惠券系统** | 普通券/秒杀券、购买/退款、库存管理 | ✅ 已完成 |
| **订单系统** | 订单创建、状态管理、核销处理 | ✅ 已完成 |
| **帖子系统** | 帖子发布、点赞、评论、关联店铺 | ✅ 已完成 |
| **推荐系统** | 个性化推荐、协同过滤、热门推荐 | ✅ 已完成 |
| **文件存储** | 腾讯云OSS集成、图片上传 | ✅ 已完成 |
| **大模型** | 智能内容审核、问答、情感分析 | ⏳ 待实现 |

**项目进度**: 14项任务中 **12项已完成** (85.7%)

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
┌───────▼───────┐  ┌───────────────▼────────┐  ┌──────────────▼────────┐
│  Nacos        │  │     微服务层            │  │     基础设施层         │
│ (8848)        │  │  (Spring Boot 3.x)     │  │                       │
│ 注册/配置     │  │                        │  │  - MySQL 8.x          │
└───────────────┘  │  - auth-service        │  │  - Redis 6.x          │
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
        ┌──────────────────┼───────────────────┐
        │                  │                   │
┌───────▼───────┐   ┌───────▼───────┐   ┌──────▼──────┐
│ Auth Service  │   │  User Service │   │ Shop Service│
└───────────────┘   └───────┬───────┘   └──────┬──────┘
                            │                  │
               ┌────────────┼────────────┐     │
               │            │            │     │
        ┌──────▼───┐ ┌──────▼───┐ ┌──────▼───┐ │
        │Post Port │ │CouponPort│ │Password  │ │
        │(Feign)   │ │(Feign)   │ │Port      │ │
        └──────────┘ └──────────┘ └──────────┘ │
                                               │
                                    ┌──────────▼──────────┐
                                    │   Post Service      │
                                    │   (Internal API)    │
                                    └─────────────────────┘
```

### 2.3 秒杀系统架构

```
┌─────────────┐     ┌─────────────┐     ┌─────────────┐
│  用户请求   │────▶│  Redis预检  │────▶│  返回结果   │
│  (1000并发) │     │  Lua原子脚本 │     │  (50成功)   │
└─────────────┘     └──────┬──────┘     └─────────────┘
                           │
                    ┌──────▼──────┐
                    │   发送MQ    │
                    │  (削峰填谷) │
                    └──────┬──────┘
                           │
                    ┌──────▼──────┐
                    │  消费者处理  │
                    │  分布式锁    │
                    │  SQL原子扣减 │
                    │  数据库更新  │
                    └─────────────┘
```

### 2.4 推荐系统架构

```
┌─────────────────────────────────────────────────────────────┐
│                      推荐服务层                              │
│  ┌──────────────────┐  ┌──────────────────┐                │
│  │  新用户策略      │  │  老用户策略      │                │
│  │  - 热门: 80%     │  │  - 协同过滤: 70% │                │
│  │  - 附近: 20%     │  │  - 热门: 30%     │                │
│  └────────┬─────────┘  └────────┬─────────┘                │
│           │                     │                          │
│           └──────────┬──────────┘                          │
│                      │                                      │
│              ┌───────▼────────┐                            │
│              │ 混合推荐引擎    │                            │
│              └───────┬────────┘                            │
└──────────────────────┼──────────────────────────────────────┘
                       │
              ┌────────▼────────┐
              │ 定时预计算任务   │ ◀── 每8分钟执行
              │ (8分钟缓存)     │
              └─────────────────┘
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
| Redisson | 3.27.1 | Redis分布式锁 |
| RabbitMQ | 3.x | 消息队列 |
| Sentinel | 1.8.x | 熔断降级 |
| JWT | 0.11.5 | 身份认证 |
| Maven | 3.x | 构建工具 |

### 3.2 前端技术栈

| 技术 | 版本 | 用途 |
|------|------|------|
| Vue.js | 3.x | 前端框架 |
| Vite | 5.x | 构建工具 |
| Vue Router | 4.x | 路由管理 |
| Axios | 1.x | HTTP客户端 |
| IntersectionObserver | Native | 图片懒加载 |

### 3.3 中间件与基础设施

| 组件 | 版本 | 部署地址 |
|------|------|----------|
| MySQL | 8.0 | localhost:3306 |
| Redis | 6.x | 192.168.145.128:6379 |
| RabbitMQ | 3.x | localhost:5672 |
| Nacos | 2.x | 192.168.145.128:8848 |
| 腾讯云COS | - | 对象存储服务 |

---

## 4. 项目结构

```
finalWork/
├── README.md                          # 项目简介
├── PROJECT_DOCUMENTATION.md           # 完整项目文档
├── TODO.md                            # 任务清单与进度
│
├── gateway/                           # API网关服务 (Port 8081)
│   └── src/main/java/com/dianping/gateway/
│
├── services/                          # 微服务模块
│   ├── pom.xml                        # 父POM
│   ├── common/                        # 公共模块
│   │   └── src/main/java/com/dianping/common/
│   │       ├── api/ApiResponse.java   # 统一响应封装
│   │       ├── dto/                   # 数据传输对象
│   │       ├── exception/             # 异常定义
│   │       ├── context/               # 用户上下文
│   │       ├── port/                  # 服务端口接口
│   │       ├── util/                  # 工具类
│   │       │   ├── RateLimit.java     # 限流注解
│   │       │   ├── SensitiveWordFilter.java  # 敏感词过滤
│   │       │   └── TrackingSDK.java   # 埋点SDK
│   │       └── config/                # 公共配置
│   │
│   ├── auth-service/                  # 认证服务 (Port 8082)
│   ├── user-service/                  # 用户服务 (Port 8083)
│   ├── merchant-service/              # 商户服务 (Port 8084)
│   ├── shop-service/                  # 店铺服务 (Port 8085)
│   ├── coupon-service/                # 优惠券服务 (Port 8095)
│   │   └── service/
│   │       ├── CouponService.java          # 秒杀核心逻辑
│   │       ├── SeckillOrderHandler.java    # 秒杀订单处理器
│   │       ├── SeckillOrderConsumer.java   # MQ消费者
│   │       └── SeckillTestController.java  # 压力测试接口
│   ├── order-service/                 # 订单服务 (Port 8087)
│   ├── post-service/                  # 帖子服务 (Port 8088)
│   └── recommendation-service/        # 推荐服务 (Port 8089)
│       ├── strategy/                  # 推荐策略
│       │   ├── RecommendStrategy.java
│       │   ├── HotRecommendStrategy.java
│       │   ├── CollaborativeFilterStrategy.java
│       │   └── HybridRecommendStrategy.java
│       ├── algorithm/
│       │   └── ItemCollaborativeFilter.java  # ItemCF算法
│       └── task/
│           └── RecommendPrecomputeTask.java  # 定时预计算
│
├── nacos-config/                      # Nacos配置文件
│   ├── dianping-gateway.yml
│   ├── auth-service.yml
│   ├── user-service.yml
│   ├── merchant-service.yml
│   ├── shop-service.yml
│   ├── coupon-service.yml
│   ├── order-service.yml
│   ├── post-service.yml
│   ├── recommendation-service.yml
│   └── post-service-secrets.yml       # OSS密钥（不提交Git）
│
├── frontend-user/                     # 用户端前端 (Port 5173)
├── frontend-merchant/                 # 商户端前端 (Port 5174)
└── frontend-admin/                    # 管理员端前端 (Port 5175)

├── docs/                              # 技术文档
│   ├── database-index-optimization.md
│   └── multi-instance-setup.md
```

---

## 5. 核心功能详解

### 5.1 高并发秒杀系统 ⭐核心亮点

#### 5.1.1 架构设计

```
用户请求 → Redis Lua脚本预检 → 发送MQ消息 → 异步处理 → 数据库更新
                ↓
        库存不足/已购买 → 直接返回错误
```

#### 5.1.2 Redis Lua原子脚本

```java
// Lua脚本保证库存检查和扣减的原子性
String SECKILL_SCRIPT = 
    "local stock = tonumber(redis.call('get', KEYS[1]));" +
    "if not stock then return -1 end;" +      // -1: 未初始化
    "if stock <= 0 then return 0 end;" +      // 0: 库存不足
    "if redis.call('exists', KEYS[2]) == 1 then return 2 end;" +  // 2: 已购买
    "redis.call('decr', KEYS[1]);" +
    "redis.call('set', KEYS[2], '1', 'EX', ARGV[1]);" +  // 标记用户已购买
    "return 1;";  // 1: 成功
```

**Lua脚本优势**：
- 原子性：整个脚本在Redis单线程中执行，无并发问题
- 高性能：减少网络往返，一次执行完成所有判断和操作

#### 5.1.3 消息队列削峰

```java
// 生产者：快速响应用户请求
@Transactional
public CouponPurchase purchase(Long couponId, Long userId) {
    // ... Redis预减库存逻辑 ...
    
    CouponPurchase purchase = new CouponPurchase();
    purchase.setStatus("processing");
    couponPurchaseMapper.insert(purchase);
    
    // 发送MQ消息，异步处理后续逻辑
    rabbitTemplate.convertAndSend(seckillQueueName, 
        new SeckillOrderMessage(couponId, userId));
    return purchase;
}

// 消费者：异步处理订单
@RabbitListener(
    queues = "${app.seckill.queue-name}",
    concurrency = "10-20",  // 并发消费
    containerFactory = "rabbitListenerContainerFactory"
)
public void handleSeckillOrder(SeckillOrderMessage message, 
                               Channel channel, 
                               Message amqpMessage) {
    try {
        seckillOrderHandler.handle(couponId, userId);
        channel.basicAck(deliveryTag, false);  // 手动确认
    } catch (Exception e) {
        // 重试机制：最多3次
        if (retryCount < 3) {
            channel.basicReject(deliveryTag, true);  // 重新入队
        } else {
            channel.basicReject(deliveryTag, false); // 进入死信队列
        }
    }
}
```

#### 5.1.4 分布式锁保证幂等

```java
@Component
public class SeckillOrderHandler {
    
    // 优惠券级别锁（串行化同一优惠券的库存扣减）
    private String buildLockKey(Long couponId, Long userId) {
        return "dp:seckill:lock:v2:" + couponId;
    }
    
    @Transactional(rollbackFor = Exception.class)
    public void handle(Long couponId, Long userId) {
        RLock lock = redissonClient.getLock(buildLockKey(couponId, userId));
        boolean locked = lock.tryLock(10, 30, TimeUnit.SECONDS);
        
        if (!locked) {
            throw new BusinessException("活动太火爆了，请稍后重试");
        }
        
        try {
            doHandle(couponId, userId);
        } finally {
            releaseLock(lock, locked, couponId, userId);
        }
    }
}
```

#### 5.1.5 SQL原子扣减

```java
@Mapper
public interface CouponMapper extends BaseMapper<Coupon> {
    
    /**
     * 原子性扣减库存
     * 使用 SQL 的 WHERE remaining_stock > 0 条件保证并发安全
     */
    @Update("UPDATE dp_coupon SET remaining_stock = remaining_stock - 1, updated_at = NOW() " +
            "WHERE id = #{couponId} AND remaining_stock > 0")
    int decrementStock(Long couponId);
}
```

**为什么不用 updateById？**
- `updateById`：先查询再更新，高并发下多个线程读到相同库存，导致更新丢失
- `decrementStock`：数据库层面的原子操作，利用行锁保证并发安全

#### 5.1.6 退款与复购支持

```java
public CouponPurchase refund(Long purchaseId, Long userId, String reason) {
    // ... 退款逻辑 ...
    
    if (TYPE_SECKILL.equals(coupon.getType())) {
        // 1. 恢复数据库库存
        coupon.setRemainingStock(coupon.getRemainingStock() + 1);
        couponMapper.updateById(coupon);
        
        // 2. 清除Redis用户购买标记，允许重新购买
        String userKey = SECKILL_USER_KEY_PREFIX + coupon.getId() + ":" + userId;
        stringRedisTemplate.delete(userKey);
        
        // 3. 恢复Redis库存
        String stockKey = SECKILL_STOCK_KEY_PREFIX + coupon.getId();
        stringRedisTemplate.opsForValue().increment(stockKey);
        
        // 4. 清理分布式锁
        stringRedisTemplate.delete("dp:seckill:lock:v2:" + coupon.getId() + ":" + userId);
    }
    return purchase;
}
```

---

### 5.2 个性化推荐系统 ⭐核心亮点

#### 5.2.1 推荐策略设计

```java
public interface RecommendStrategy {
    List<ShopDTO> recommend(RecommendationRequest request);
}

// 新用户：热门 + 附近
@Component
public class HybridRecommendStrategy implements RecommendStrategy {
    
    public List<ShopDTO> recommend(RecommendationRequest request) {
        if (isNewUser(request.getUserId())) {
            // 新用户：80%热门 + 20%附近
            List<ShopDTO> hot = hotStrategy.recommend(request);
            List<ShopDTO> nearby = nearbyStrategy.recommend(request);
            return merge(0.8, hot, 0.2, nearby);
        } else {
            // 老用户：70%协同过滤 + 30%热门
            List<ShopDTO> cf = cfStrategy.recommend(request);
            List<ShopDTO> hot = hotStrategy.recommend(request);
            return merge(0.7, cf, 0.3, hot);
        }
    }
}
```

#### 5.2.2 协同过滤算法（ItemCF）

```java
@Component
public class ItemCollaborativeFilter {
    
    /**
     * 计算店铺之间的相似度
     * 基于用户购买历史：如果两个店铺经常被同一用户购买，则相似度高
     */
    public Map<Long, List<ShopSimilarity>> computeSimilarity() {
        // 1. 构建用户-店铺购买矩阵
        Map<Long, Set<Long>> userShopMatrix = buildUserShopMatrix();
        
        // 2. 计算店铺共现次数
        Map<Long, Map<Long, Integer>> coOccurrence = new HashMap<>();
        for (Set<Long> shops : userShopMatrix.values()) {
            for (Long shop1 : shops) {
                for (Long shop2 : shops) {
                    if (!shop1.equals(shop2)) {
                        coOccurrence.computeIfAbsent(shop1, k -> new HashMap<>())
                                    .merge(shop2, 1, Integer::sum);
                    }
                }
            }
        }
        
        // 3. 计算相似度（余弦相似度）
        return calculateCosineSimilarity(coOccurrence);
    }
}
```

#### 5.2.3 定时预计算

```java
@Component
public class RecommendPrecomputeTask {
    
    // 每8分钟执行一次
    @Scheduled(fixedRate = 480000)
    public void precomputeRecommendations() {
        log.info("开始预计算推荐数据...");
        
        // 1. 预计算热门推荐
        List<HotShopDTO> hotShops = calculateHotShops();
        redisTemplate.opsForValue().set("dp:rec:hot", hotShops, 8, TimeUnit.MINUTES);
        
        // 2. 预计算协同过滤结果
        Map<Long, List<ShopSimilarity>> similarities = itemCF.computeSimilarity();
        redisTemplate.opsForValue().set("dp:rec:similarity", similarities, 8, TimeUnit.MINUTES);
        
        log.info("推荐数据预计算完成");
    }
}
```

#### 5.2.4 首页热门推荐展示

用户端首页采用横向卡片式展示热门推荐店铺，包含：
- 店铺封面图片
- 店铺名称和评分
- 店铺标签（分类、特色）
- 地址信息
- 点击跳转到店铺详情

---

### 5.3 数据埋点与分析

```java
@Component
public class TrackingSDK {
    
    // 页面浏览事件
    public void trackPageView(String pageName, Map<String, Object> params) {
        TrackingEvent event = new TrackingEvent();
        event.setEventType("page_view");
        event.setPageName(pageName);
        event.setParams(params);
        event.setTimestamp(System.currentTimeMillis());
        sendToServer(event);
    }
    
    // 点击事件
    public void trackClick(String elementId, String pageName) {
        TrackingEvent event = new TrackingEvent();
        event.setEventType("click");
        event.setElementId(elementId);
        event.setPageName(pageName);
        sendToServer(event);
    }
}
```

**埋点数据用途**：
1. **用户画像构建**：分析用户浏览、点击、购买行为
2. **推荐效果评估**：点击率(CTR)、转化率(CVR)
3. **漏斗分析**：从浏览到购买的转化路径
4. **热力图**：页面元素点击热度

---

### 5.4 安全加固

#### 5.4.1 限流保护

```java
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimit {
    int requests() default 100;      // 请求次数
    int seconds() default 60;        // 时间窗口（秒）
}

@Aspect
@Component
public class RateLimitAspect {
    
    @Around("@annotation(rateLimit)")
    public Object around(ProceedingJoinPoint point, RateLimit rateLimit) {
        String key = buildKey(point);
        
        // 使用Redis计数器
        Long count = redisTemplate.opsForValue().increment(key);
        if (count == 1) {
            redisTemplate.expire(key, rateLimit.seconds(), TimeUnit.SECONDS);
        }
        
        if (count > rateLimit.requests()) {
            throw new BusinessException("请求过于频繁，请稍后重试");
        }
        
        return point.proceed();
    }
}

// 使用示例
@RateLimit(requests = 10, seconds = 60)
@PostMapping("/purchase")
public ApiResponse purchase(@RequestBody PurchaseRequest request) {
    // ...
}
```

#### 5.4.2 敏感词过滤

```java
@Component
public class SensitiveWordFilter {
    
    private TrieNode root = new TrieNode();
    
    // 基于DFA算法构建敏感词树
    public void init(Set<String> sensitiveWords) {
        for (String word : sensitiveWords) {
            insertWord(word);
        }
    }
    
    public String filter(String text) {
        // DFA算法高效检测和替换敏感词
        // 时间复杂度：O(n)，n为文本长度
    }
}
```

#### 5.4.3 JWT Token自动续期

```java
@Service
public class JwtService {
    
    public AuthResponse refreshToken(String refreshToken) {
        // 验证refreshToken有效性
        Claims claims = parseToken(refreshToken);
        
        // 生成新的accessToken和refreshToken
        String newAccessToken = generateAccessToken(claims);
        String newRefreshToken = generateRefreshToken(claims);
        
        // 将旧refreshToken加入黑名单（防止重复使用）
        blacklistToken(refreshToken);
        
        return new AuthResponse(newAccessToken, newRefreshToken);
    }
}
```

---

### 5.5 熔断降级（Sentinel）

```java
@Configuration
public class SentinelConfig {
    
    @PostConstruct
    public void init() {
        // 配置熔断规则
        List<DegradeRule> rules = new ArrayList<>();
        
        DegradeRule rule = new DegradeRule();
        rule.setResource("purchase");
        rule.setGrade(CircuitBreakerStrategy.ERROR_RATIO);  // 异常比例熔断
        rule.setCount(0.5);                                  // 异常比例阈值50%
        rule.setTimeWindow(30);                              // 熔断时长30秒
        rule.setMinRequestAmount(10);                        // 最小请求数
        
        rules.add(rule);
        DegradeRuleManager.loadRules(rules);
    }
}
```

---

## 6. 部署与运行

### 6.1 环境要求

- JDK 17+
- Maven 3.8+
- MySQL 8.0+
- Redis 6.0+（密码：123456）
- RabbitMQ 3.8+（端口：5672）
- Nacos 2.0+（地址：192.168.145.128:8848）
- Node.js 18+

### 6.2 数据库初始化

```sql
-- 创建数据库
CREATE DATABASE dianping_user CHARACTER SET utf8mb4;
CREATE DATABASE dianping_merchant CHARACTER SET utf8mb4;
CREATE DATABASE dianping_shop CHARACTER SET utf8mb4;
CREATE DATABASE dianping_coupon CHARACTER SET utf8mb4;
CREATE DATABASE dianping_order CHARACTER SET utf8mb4;
CREATE DATABASE dianping_post CHARACTER SET utf8mb4;
CREATE DATABASE dianping_recommendation CHARACTER SET utf8mb4;
```

### 6.3 Nacos配置

在Nacos控制台导入`nacos-config/`目录下的所有配置文件：

1. `dianping-gateway.yml` - 网关路由
2. `auth-service.yml` - 认证服务
3. `user-service.yml` - 用户服务
4. `merchant-service.yml` - 商户服务
5. `shop-service.yml` - 店铺服务
6. `coupon-service.yml` - 优惠券服务
7. `order-service.yml` - 订单服务
8. `post-service.yml` - 帖子服务
9. `recommendation-service.yml` - 推荐服务

**注意**：`post-service-secrets.yml`包含腾讯云OSS密钥，**不要提交到Git**，仅在本地配置。

### 6.4 后端启动

```bash
# 1. 安装公共模块
mvn -f services/pom.xml -pl common -am clean install

# 2. 编译全部服务
mvn -f services/pom.xml -DskipTests clean install

# 3. 按顺序启动服务（推荐顺序）
mvn -f services/auth-service/pom.xml spring-boot:run
mvn -f services/user-service/pom.xml spring-boot:run
mvn -f services/merchant-service/pom.xml spring-boot:run
mvn -f services/shop-service/pom.xml spring-boot:run
mvn -f services/coupon-service/pom.xml spring-boot:run
mvn -f services/order-service/pom.xml spring-boot:run
mvn -f services/post-service/pom.xml spring-boot:run
mvn -f services/recommendation-service/pom.xml spring-boot:run

# 4. 启动网关
mvn -f gateway/pom.xml spring-boot:run
```

### 6.5 前端启动

```bash
# 用户端
cd frontend-user
npm install
npm run dev

# 商户端
cd frontend-merchant
npm install
npm run dev

# 管理员端
cd frontend-admin
npm install
npm run dev
```

---

## 7. API接口文档

### 7.1 压力测试接口

```http
POST /api/stress-test/seckill-stress
Content-Type: application/x-www-form-urlencoded

userCount=1000&stockCount=50&couponId=999

Response:
{
  "success": true,
  "duration": 2751,
  "tps": "363.50",
  "concurrentUsers": 1000,
  "stockCount": 50,
  "successCount": 50,
  "finalStock": 0,
  "redisStock": "0",
  "duplicateUsers": 0,
  "allPassed": true,
  "validations": [
    "✅ 成功购买数正确",
    "✅ 最终库存为0",
    "✅ 无重复购买",
    "✅ Redis和数据库一致"
  ]
}
```

### 7.2 推荐接口

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
      "rating": 4.8,
      "tags": ["网红", "必吃"],
      "coverUrl": "https://xxx.jpg"
    }
  ]
}
```

### 7.3 其他核心接口

详见`PROJECT_DOCUMENTATION.md`第10章。

---

## 8. 性能优化与压测

### 8.1 压力测试结果

| 指标 | 数值 | 说明 |
|------|------|------|
| 并发用户数 | 1000 | 同时抢购 |
| 库存数量 | 50 | 秒杀券 |
| 成功购买 | 50 | 正确 |
| 最终库存 | 0 | 无超卖 |
| 重复购买 | 0 | 幂等性保证 |
| TPS | 363.50 | 每秒事务数 |
| 耗时 | 2751ms | 总耗时 |
| 结果 | ✅ 全部通过 | 4项验证 |

### 8.2 性能优化措施

1. **Redis缓存**
   - 热点数据缓存（店铺信息、用户信息）
   - 推荐结果缓存（8分钟TTL）
   - 缓存穿透防护（布隆过滤器）

2. **数据库优化**
   - 索引优化（见`docs/database-index-optimization.md`）
   - SQL原子扣减（避免更新丢失）
   - 连接池调优（HikariCP）

3. **异步处理**
   - 推荐日志异步记录
   - 订单超时检查定时任务
   - 多线程池配置

4. **消息队列**
   - 秒杀订单异步处理
   - 削峰填谷
   - 手动确认保证可靠消费

5. **前端优化**
   - 图片懒加载
   - 无限滚动
   - 骨架屏
   - 错误边界

---

## 9. 安全设计

### 9.1 敏感信息保护

- **腾讯云OSS密钥**：存储在`post-service-secrets.yml`，不提交Git
- **数据库密码**：通过Nacos配置中心管理
- **JWT密钥**：服务端配置，定期轮换

### 9.2 防护措施

1. **限流**：接口级别限流，防止暴力请求
2. **敏感词过滤**：帖子、评论内容审核
3. **JWT续期**：双Token机制，无感知刷新
4. **熔断降级**：服务异常时自动熔断，保护系统
5. **分布式锁**：防止重复提交和超卖
6. **SQL注入防护**：MyBatis参数化查询

---

## 10. 开发规范

### 10.1 代码规范

- 遵循阿里巴巴Java开发手册
- 使用Lombok简化代码
- 统一返回`ApiResponse`封装
- 异常统一处理

### 10.2 Git规范

- **分支管理**：main分支保护，开发使用feature分支
- **提交信息**：`type: description`格式
  - `feat`: 新功能
  - `fix`: 修复
  - `docs`: 文档
  - `security`: 安全问题
- **敏感信息**：禁止提交密钥、密码

### 10.3 文档规范

- API变更需更新接口文档
- 核心功能需补充技术文档
- 架构调整需更新架构图

---

## 附录

### A. 常见问题

**Q: 秒杀测试失败？**
A: 检查以下几点：
1. Redis是否正常运行
2. RabbitMQ是否正常运行
3. 数据库连接是否正常
4. 优惠券ID是否存在

**Q: 推荐不生效？**
A: 检查：
1. 推荐服务是否正常启动
2. Redis缓存是否命中
3. 用户行为数据是否足够（老用户需要历史数据）

**Q: 图片上传失败？**
A: 检查`post-service-secrets.yml`是否正确配置OSS密钥

### B. 后续规划

- **P0**: 大模型集成（智能审核、问答）
- **P1**: 支付流程完善（微信/支付宝）
- **P2**: 多语言支持、移动端APP

---

**文档版本**: 2.0  
**最后更新**: 2026-03-11  
**作者**: Claude Code  
**项目进度**: 85.7% (12/14)

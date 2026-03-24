# 系统架构设计

## 整体架构

```
┌─────────────────────────────────────────────────────────────────┐
│                         客户端层                                  │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐          │
│  │  用户前端     │  │  商家前端     │  │  管理员前端   │          │
│  │ (Vue3+5173)  │  │ (Vue3+5174)  │  │ (Vue3+5175)  │          │
│  └──────────────┘  └──────────────┘  └──────────────┘          │
└─────────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────────┐
│                         网关层                                    │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │              API Gateway (端口 8081)                      │  │
│  │  - JWT Token 验证                                         │  │
│  │  - 路由转发                                               │  │
│  │  - CORS 处理                                              │  │
│  │  - 用户信息传递                                           │  │
│  └──────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────────┐
│                        微服务层                                   │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐       │
│  │  auth    │  │  user    │  │ merchant │  │  shop    │       │
│  │ :8091    │  │ :8092    │  │ :8093    │  │ :8094    │       │
│  └──────────┘  └──────────┘  └──────────┘  └──────────┘       │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐       │
│  │  coupon  │  │  order   │  │  post    │  │recommend │       │
│  │ :8095    │  │ :8096    │  │ :8086    │  │ :8098    │       │
│  └──────────┘  └──────────┘  └──────────┘  └──────────┘       │
└─────────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────────┐
│                      基础设施层                                   │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐       │
│  │  Nacos   │  │  MySQL   │  │  Redis   │  │ RabbitMQ │       │
│  │  :8848   │  │  :3306   │  │  :6379   │  │  :5672   │       │
│  └──────────┘  └──────────┘  └──────────┘  └──────────┘       │
│  ┌──────────┐                                                  │
│  │腾讯云COS │  对象存储（图片）                                │
│  └──────────┘                                                  │
└─────────────────────────────────────────────────────────────────┘
```

## 核心设计理念

### 1. 认证授权架构

#### JWT Token 流程

```
┌──────────┐          ┌──────────┐          ┌──────────┐
│  Client  │          │ Gateway  │          │ Services │
└────┬─────┘          └────┬─────┘          └────┬─────┘
     │                     │                     │
     │ 1. Login Request    │                     │
     ├────────────────────>│                     │
     │                     │ 2. Forward          │
     │                     ├────────────────────>│
     │                     │                     │ auth-service
     │                     │ 3. JWT Token        │
     │                     │<────────────────────┤
     │ 4. Token Response   │                     │
     │<────────────────────┤                     │
     │                     │                     │
     │ 5. API Request      │                     │
     │ + Authorization     │                     │
     ├────────────────────>│                     │
     │                     │ 6. Validate Token   │
     │                     │ 7. Extract Claims   │
     │                     │ 8. Add Headers      │
     │                     │   X-User-Id         │
     │                     │   X-User-Role       │
     │                     │   X-Username        │
     │                     ├────────────────────>│
     │                     │                     │
     │                     │ 9. Process Request  │
     │                     │<────────────────────┤
     │ 10. Response        │                     │
     │<────────────────────┤                     │
```

#### Token 结构

```json
{
  "sub": "1",                    // 用户ID
  "username": "zhangsan",        // 用户名
  "role": "user",                // 角色：user, merchant, admin
  "type": "access",              // 类型：access, refresh
  "iat": 1774325683,             // 签发时间
  "exp": 1774332883              // 过期时间
}
```

### 2. 服务间通信

#### Feign 调用流程

```
┌─────────────┐                    ┌─────────────┐
│ user-service│                    │post-service │
└──────┬──────┘                    └──────┬──────┘
       │                                  │
       │ 1. 调用 PostClient 方法          │
       │    postPort.listSummariesByUser(id)
       ├─────────────────────────────────>│
       │                                  │
       │ 2. FeignConfig 拦截器            │
       │    添加用户上下文 Headers:       │
       │    - X-User-Id                   │
       │    - X-User-Role                 │
       │    - X-Username                  │
       │                                  │
       │                                  │ 3. UserContextFilter
       │                                  │    解析 Headers
       │                                  │    设置 SecurityContext
       │                                  │
       │ 4. 返回结果                      │
       │<─────────────────────────────────┤
```

#### Port 接口设计

服务间通信通过 Port 接口定义：

```java
// 定义在 common 模块
public interface PostPort {
    List<PostSummary> listSummariesByUser(Long userId);
    PostDetail getDetail(Long postId);
}

// 实现在调用方服务
@FeignClient(name = "post-service")
public interface PostClient extends PostPort {
    @GetMapping("/internal/posts/user")
    List<PostSummary> listSummariesByUser(@RequestParam("userId") Long userId);
}
```

### 3. Security 配置架构

#### 过滤器链

```
请求 → UserContextFilter → SecurityFilterChain → Controller
        │                      │
        │                      └─ permitAll() 所有请求
        │
        └─ 解析 Headers
           设置 UserContext
           设置 SecurityContext
           权限：ROLE_USER / ROLE_MERCHANT / ROLE_ADMIN
```

#### 关键设计点

1. **完全依赖 UserContextFilter**
   - 所有 SecurityConfig 使用 `.anyRequest().permitAll()`
   - 认证完全由 UserContextFilter 处理
   - 避免 Spring Security 默认过滤器覆盖 SecurityContext

2. **SecurityContext 继承**
   ```java
   @PostConstruct
   public void init() {
       SecurityContextHolder.setStrategyName(
           SecurityContextHolder.MODE_INHERITABLETHREADLOCAL
       );
   }
   ```

3. **权限格式**
   - Token 中 role: `user`, `merchant`, `admin`
   - SecurityContext 权限: `ROLE_USER`, `ROLE_MERCHANT`, `ROLE_ADMIN`
   - 自动转换：`ROLE_` + role.toUpperCase()

### 4. 数据流转

#### 用户请求完整流程

```
1. 前端发起请求
   ↓
2. Gateway 接收请求
   - 提取 Authorization Header
   - 验证 JWT Token
   - 解析用户信息
   - 设置 Headers: X-User-Id, X-User-Role, X-Username
   ↓
3. 路由到目标服务
   ↓
4. UserContextFilter 拦截
   - 解析 Headers
   - 创建 UserSession
   - 设置 UserContext (ThreadLocal)
   - 设置 SecurityContext
   ↓
5. Controller 处理
   - 从 UserContext 获取当前用户
   - 或从 SecurityContext 获取 Authentication
   ↓
6. Service 层
   - 业务逻辑处理
   - 调用其他服务（Feign）
   ↓
7. Feign 拦截器
   - 从 UserContext 读取用户信息
   - 添加到请求 Headers
   ↓
8. 被调服务
   - 重复步骤 4-7
   ↓
9. 响应返回
   - 清理 ThreadLocal
   - 清理 SecurityContext
```

## 数据库设计

### 分库策略

每个微服务独立数据库：

| 数据库 | 所属服务 | 说明 |
|--------|----------|------|
| dianping_user | user-service | 用户数据 |
| dianping_merchant | merchant-service | 商家数据 |
| dianping_shop | shop-service | 店铺数据 |
| dianping_coupon | coupon-service | 优惠券数据 |
| dianping_order | order-service | 订单数据 |
| dianping_post | post-service | 帖子数据 |
| dianping_recommendation | recommendation-service | 推荐数据 |

### 核心表设计

#### 用户表 (user)

```sql
CREATE TABLE user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    user_role VARCHAR(20) DEFAULT 'user',
    city VARCHAR(50),
    balance DECIMAL(10, 2) DEFAULT 0.00,
    avatar_url VARCHAR(255),
    status TINYINT DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

#### 店铺表 (shop)

```sql
CREATE TABLE shop (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    merchant_id BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL,
    category VARCHAR(50),
    address VARCHAR(255),
    latitude DECIMAL(10, 8),
    longitude DECIMAL(11, 8),
    avg_rating DECIMAL(3, 2) DEFAULT 0.00,
    status TINYINT DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

## 缓存策略

### Redis 缓存使用

1. **Token 管理**
   - Access Token 存储
   - Refresh Token 存储
   - Token 黑名单（登出）

2. **用户 Session**
   ```
   Key: dp:token:access:{token}
   Value: {
     "userId": "1",
     "username": "zhangsan",
     "role": "user",
     "city": "上海"
   }
   TTL: 2小时
   ```

3. **权限缓存**
   ```
   Key: dp:permission:user:{userId}
   Value: ["perm1", "perm2", ...]
   TTL: 30分钟
   ```

## 限流熔断

### Sentinel 配置

- 服务限流：QPS 限制
- 服务熔断：异常比例/慢调用比例
- 热点参数限流：热点数据保护

## 配置中心

### Nacos 配置结构

```
Namespace: public
├── auth-service.yml
├── user-service.yml
├── merchant-service.yml
├── shop-service.yml
├── coupon-service.yml
├── order-service.yml
├── post-service.yml
├── post-service-secrets.yml (加密配置)
├── recommendation-service.yml
└── dianping-gateway.yml
```

### 动态配置刷新

支持动态刷新的配置项：
- JWT 密钥
- Token 过期时间
- Redis 连接信息
- 限流规则

## 监控告警

### 健康检查

每个服务提供：
- `/actuator/health` - 健康状态
- `/actuator/info` - 服务信息
- `/actuator/prometheus` - Prometheus 指标

### 关键指标

- 请求 QPS
- 响应时间 P95/P99
- 错误率
- JVM 内存使用
- 数据库连接池状态
- Redis 连接状态

## 扩展性设计

### 水平扩展

所有微服务无状态设计，支持：
- 多实例部署
- 负载均衡（Ribbon）
- 会话保持（Redis）

### 垂直扩展

- 数据库读写分离
- 缓存分片
- 消息队列削峰

## 安全设计

### 网络安全

- CORS 配置
- XSS 防护
- CSRF 防护（已禁用，使用 Token）

### 数据安全

- 密码加密（BCrypt）
- Token 签名验证
- 敏感信息加密存储

### 接口安全

- JWT 认证
- 内部接口保护（InternalApiFilter）
- 限流保护

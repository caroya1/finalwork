# GATEWAY KNOWLEDGE BASE

**Location**: `gateway/`
**Parent**: Root (独立Maven项目)
**Type**: Spring Cloud Gateway
**Port**: 8081

---

## OVERVIEW

API网关 - 所有请求的统一入口，负责路由转发、JWT认证、限流。

---

## STRUCTURE

```
gateway/
├── pom.xml                   # 独立Maven配置
└── src/main/
    ├── java/com/dianping/gateway/
    │   ├── GatewayApplication.java
    │   ├── config/
    │   │   └── GatewayConfig.java
    │   └── filter/
    │       ├── JwtGatewayFilter.java      # JWT验证
    │       └── RateLimitFilter.java       # 限流
    └── resources/
        ├── bootstrap.yml      # Nacos配置
        └── application.yml    # 本地配置
```

---

## CONVENTIONS

### 路由配置
```yaml
# 示例路由规则
spring:
  cloud:
    gateway:
      routes:
        - id: user-service
          uri: lb://user-service
          predicates:
            - Path=/api/users/**
```

### JWT验证
- **白名单**: `/api/auth/**`, `/api/shops` (GET)
- **Token位置**: `Authorization: Bearer {token}`
- **RefreshToken**: `X-Refresh-Token` header

### 自定义Filter
- `JwtGatewayFilter` - 顺序: Ordered.HIGHEST_PRECEDENCE
- `RateLimitFilter` - 基于Guava RateLimiter

---

## COMMANDS

```bash
# 独立构建 (不在services/pom.xml中)
mvn clean install -DskipTests

# 运行
mvn spring-boot:run

# 或
java -jar target/dianping-gateway-1.0.0.jar
```

---

## WHERE TO LOOK

| Task | Location | Notes |
|------|----------|-------|
| JWT验证 | `filter/JwtGatewayFilter.java` | Token解析/验证 |
| 路由配置 | Nacos `dianping-gateway.yml` | 动态路由 |
| 限流 | `filter/RateLimitFilter.java` | Guava限流 |

---

## NOTES

- **独立Maven项目**: 不包含在 `services/pom.xml` 中
- **最先启动**: 其他服务注册后会自动路由
- **Nacos配置**: `dianping-gateway.yml` (注意命名)
- **依赖服务**: 无 (但需Nacos运行)
- **前端访问**: 所有前端请求通过 `localhost:8081`

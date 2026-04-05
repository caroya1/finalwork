# SERVICES MODULE KNOWLEDGE BASE

**Location**: `services/`
**Parent**: Root
**Type**: Maven Multi-Module

---

## OVERVIEW

9个微服务 + 1公共模块的Maven多模块项目。

---

## STRUCTURE

```
services/
├── pom.xml                    # Parent POM (依赖管理)
├── common/                    # 公共模块 (必须先build)
│   ├── src/main/java/com/dianping/common/
│   │   ├── dto/              # 共享DTO
│   │   ├── port/             # Feign接口定义
│   │   ├── util/             # 工具类
│   │   ├── config/           # 共享配置
│   │   ├── security/         # 安全组件
│   │   └── exception/        # 异常类
│   └── pom.xml
├── auth-service/              # 认证服务 (8091)
├── user-service/              # 用户服务 (8092)
├── merchant-service/          # 商户服务 (8093)
├── shop-service/              # 店铺服务 (8094)
├── coupon-service/            # 优惠券服务 (8095)
├── order-service/             # 订单服务 (8096)
├── post-service/              # 帖子服务 (8097)
├── recommendation-service/    # 推荐服务 (8098)
└── ai-service/                # AI服务 (8099)
```

---

## CONVENTIONS

### 服务标准结构
```
{service}/
├── src/main/java/com/dianping/{service}/
│   ├── {Service}Application.java
│   ├── controller/           # REST API
│   ├── service/              # 业务逻辑
│   ├── mapper/               # MyBatis接口
│   ├── entity/               # 实体类
│   ├── dto/                  # 数据传输对象
│   ├── config/               # 配置类
│   ├── client/               # Feign客户端
│   └── security/             # 安全相关
└── src/main/resources/
    ├── bootstrap.yml         # Nacos配置
    └── mapper/               # XML映射文件
```

### 依赖关系
```
common → 所有服务
shop-service → merchant-service
coupon-service → shop-service
order-service → user, shop, coupon
post-service → user, shop
recommendation-service → shop
ai-service → shop
```

---

## COMMANDS

```bash
# 必须先安装common
cd services/common && mvn clean install -DskipTests

# 编译所有服务
cd .. && mvn clean install -DskipTests

# 运行单个服务
cd {service-name} && mvn spring-boot:run

# 运行测试
cd {service-name} && mvn test

# 压力测试 (coupon-service)
cd coupon-service
mvn spring-boot:run -Dspring-boot.run.arguments=stress-test
```

---

## WHERE TO LOOK

| Task | Location | Notes |
|------|----------|-------|
| 共享DTO | `common/dto/` | ApiResponse, PageResult等 |
| Port接口 | `common/port/` | Feign调用接口定义 |
| Feign实现 | `{service}/client/` | 调用其他服务的客户端 |
| 全局异常 | `common/exception/` | 通用异常类 |
| 内部API安全 | `common/security/InternalApiFilter.java` | 生产环境需启用 |

---

## ANTI-PATTERNS

| Issue | Location | Severity |
|-------|----------|----------|
| Internal API未启用 | `common/security/InternalApiFilter.java:52` | HIGH |
| @SuppressWarnings | `merchant/MerchantService.java:148`, `recommendation/AiRecommendStrategy.java:64` | LOW |
| @Deprecated | `coupon/dto/UserCouponView.java` | LOW |

---

## NOTES

- **必须先build common**: 其他服务依赖common模块
- **Port/Adapter模式**: 服务间通信通过common/port/接口定义
- **Nacos配置**: 每个服务有自己的bootstrap.yml指向Nacos
- **测试框架**: JUnit 5 + Mockito
- **无Docker**: 纯手动部署

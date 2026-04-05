# PROJECT KNOWLEDGE BASE

**Project**: Dianping Clone (类大众点评系统)
**Stack**: Spring Cloud Alibaba + Vue3
**Generated**: 2025-04-03

---

## OVERVIEW

本地生活服务平台微服务架构，涵盖用户端、商户端、管理员端三端。基于 Spring Cloud Alibaba 2023 + Vue 3.4 + MySQL + Redis + Nacos 构建。

---

## STRUCTURE

```
.
├── gateway/                    # API网关 (Port: 8081)
├── services/                   # 微服务模块 (9 services)
│   ├── common/                # 公共模块 (DTO, Utils, Port接口)
│   ├── auth-service/          # 认证服务 (8091)
│   ├── user-service/          # 用户服务 (8092)
│   ├── merchant-service/      # 商户服务 (8093)
│   ├── shop-service/          # 店铺服务 (8094)
│   ├── coupon-service/        # 优惠券服务 (8095)
│   ├── order-service/         # 订单服务 (8096)
│   ├── post-service/          # 帖子服务 (8097)
│   ├── recommendation-service/# 推荐服务 (8098)
│   └── ai-service/            # AI服务 (8099)
├── frontend-user/             # 用户端前端 (Port: 5173)
├── frontend-merchant/         # 商户端前端 (Port: 5174)
├── frontend-admin/            # 管理端前端 (Port: 5175)
├── nacos-config/              # Nacos配置文件
└── sql/                       # 数据库初始化脚本
```

---

## WHERE TO LOOK

| Task | Location | Notes |
|------|----------|-------|
| API Gateway | `gateway/` | 独立项目，JWT验证，路由转发 |
| 微服务入口 | `services/*/src/main/java/com/dianping/*/*Application.java` | 10个Spring Boot应用 |
| 公共组件 | `services/common/` | DTO, Port接口, 工具类, 安全配置 |
| Feign Client | `services/*/client/` | 服务间调用接口 |
| 前端入口 | `frontend-*/src/main.js` | Vue3 + Vite |
| 数据库脚本 | `sql/` | 8个服务对应8个数据库 |
| Nacos配置 | `nacos-config/*.yml` | 集中配置管理 |

---

## CONVENTIONS

### Java Backend
- **Package**: `com.dianping.{service}.{module}`
- **Ports**: 8091-8099 (auth→ai), Gateway: 8081
- **Response**: `ApiResponse<T>` from common module
- **Internal API**: `/internal/**` 路径用于服务间调用
- **JWT**: Authorization: Bearer {token}, X-Refresh-Token 头部
- **Feign**: `@FeignClient` + Port接口定义在common模块
- **User Context**: X-User-Id, X-User-Role, X-Username 头部传递

### Frontend (Vue3)
- **Style**: Composition API with `<script setup>`
- **Components**: PascalCase (e.g., `SmartRecommendDialog.vue`)
- **API Modules**: `./api/` 目录
- **Storage**: `dp_` 前缀 (dp_token, dp_user_id)
- **CSS**: CSS Custom Properties 变量 (无外部UI库)

### Database
- **Naming**: `dianping_{service}` (每个服务独立数据库)
- **ORM**: MyBatis-Plus 3.5+
- **Migration**: 无Flyway/Liquibase，自动建表

### Git
- **Commits**: `feat:`, `fix:`, `docs:`, `style:`, `refactor:`, `test:`, `chore:`
- **Branch**: master, develop, feature/xxx, bugfix/xxx

---

## ANTI-PATTERNS (THIS PROJECT)

| Issue | Location | Severity |
|-------|----------|----------|
| **内部API安全未启用** | `common/InternalApiFilter.java:52` | HIGH |
| **@SuppressWarnings** | `MerchantService.java`, `AiRecommendStrategy.java` | LOW |
| **@Deprecated 类** | `coupon/dto/UserCouponView.java` | LOW |

**Critical**: 生产环境需启用 `InternalApiFilter` 中的 token 验证 (当前TODO注释)。

---

## UNIQUE STYLES

1. **Port/Adapter Pattern**: Service间通过Port接口 + FeignClient通信
2. **Nacos-first Config**: 所有配置托管Nacos，bootstrap.yml仅加载Nacos地址
3. **No Docker**: 纯手动部署，无容器化
4. **No CI/CD**: 无GitHub Actions/Jenkins
5. **Secrets 管理**: 环境变量 > Nacos配置，`secrets.yml` 不提交git
6. **Custom CSS**: 无Element Plus/Ant Design，纯自定义CSS变量

---

## COMMANDS

```bash
# 构建 (必须先安装common)
cd services/common && mvn clean install -DskipTests
cd ../.. && mvn clean install -DskipTests

# 启动顺序 (重要)
# 1. Gateway
cd gateway && mvn spring-boot:run

# 2. 基础服务 (无依赖)
cd services/auth-service && mvn spring-boot:run
cd services/user-service && mvn spring-boot:run
cd services/merchant-service && mvn spring-boot:run
cd services/shop-service && mvn spring-boot:run

# 3. 业务服务 (有依赖)
cd services/coupon-service && mvn spring-boot:run
cd services/order-service && mvn spring-boot:run
cd services/post-service && mvn spring-boot:run
cd services/recommendation-service && mvn spring-boot:run
cd services/ai-service && mvn spring-boot:run

# 前端
cd frontend-user && npm install && npm run dev     # Port 5173
cd frontend-merchant && npm install && npm run dev # Port 5174
cd frontend-admin && npm install && npm run dev    # Port 5175

# 导入Nacos配置
cd nacos-config
./import-to-nacos.sh http://192.168.145.128:8848   # Linux/Mac
import-to-nacos.bat http://192.168.145.128:8848    # Windows
```

---

## DEPENDENCIES

| Service | Address | Credentials |
|---------|---------|-------------|
| Nacos | 192.168.145.128:8848 | nacos/nacos |
| MySQL | 192.168.145.128:3306 | root/123456 |
| Redis | 192.168.145.128:6379 | 123456 |

---

## NOTES

- **Gateway 位置特殊**: 在根目录而非 services/ 下
- **Common 必须先安装**: 否则其他服务编译失败
- **无 root pom.xml**: services/pom.xml 是实际parent
- **无 TypeScript**: 纯 JavaScript (ES6+)
- **日志位置**: `./logs/` 目录
- **压力测试**: `SeckillStressTest` 在 coupon-service，启动参数 `stress-test`

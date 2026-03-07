# 类大众点评系统（SpringBoot + Vue3）

当前版本为不含大模型模块的可运行骨架，覆盖用户端/商户端/管理员端页面与后端推荐、订单、商户、用户模块。

## 环境要求
- JDK 17
- Spring Boot 2.7.x
- Vue 3 + Vite
- MySQL（本地，root/123456）
- Redis（192.168.145.128:6379，密码123456）
- rabbitMQ（本地 5672）
- Nacos（192.168.145.128:8848）

## 后端启动（微服务）
1. 创建各模块数据库：`dianping_user` / `dianping_merchant` / `dianping_shop` / `dianping_coupon` / `dianping_order` / `dianping_post` / `dianping_recommendation`
2. 启动 Redis 与 RabbitMQ
3. 启动 Nacos（`192.168.145.128:8848`）
4. 在 Nacos 中新增配置（见 `nacos-config/*.yml`）：
   - `auth-service.yml`
   - `user-service.yml`
   - `merchant-service.yml`
   - `shop-service.yml`
   - `coupon-service.yml`
   - `order-service.yml`
   - `post-service.yml`
   - `recommendation-service.yml`
5. 按模块初始化数据库（`backend/src/main/resources/schema-*.sql`、`backend/src/main/resources/data-*.sql`）
6. 安装公共依赖（common）：

```bash
mvn -f services/pom.xml -pl common -am clean install
```

7. 编译全部服务：

```bash
mvn -f services/pom.xml -DskipTests clean install
```

8. 依次启动服务（示例）：

```bash
mvn -f services/auth-service/pom.xml spring-boot:run
mvn -f services/user-service/pom.xml spring-boot:run
mvn -f services/merchant-service/pom.xml spring-boot:run
mvn -f services/shop-service/pom.xml spring-boot:run
mvn -f services/coupon-service/pom.xml spring-boot:run
mvn -f services/order-service/pom.xml spring-boot:run
mvn -f services/post-service/pom.xml spring-boot:run
mvn -f services/recommendation-service/pom.xml spring-boot:run
```

## 网关启动
1. 在 Nacos 中新增配置 `dianping-gateway.yml`（见 `nacos-config/dianping-gateway.yml`）
2. 进入 `gateway` 目录运行：

```bash
mvn spring-boot:run
```

默认网关端口为 `8081`，路由到各微服务（`auth-service` / `user-service` / `merchant-service` / `shop-service` / `coupon-service` / `order-service` / `post-service` / `recommendation-service`）。

## 前端启动
### 用户端
进入 `frontend-user` 目录运行：

```bash
npm install
npm run dev
```

### 商户端
进入 `frontend-merchant` 目录运行：

```bash
npm install
npm run dev
```

### 管理端
进入 `frontend-admin` 目录运行：

```bash
npm install
npm run dev
```

## 主要接口
- 推荐：`POST /api/recommendations`
- 用户：`POST /api/users` / `GET /api/users`
- 商户：`POST /api/merchants` / `GET /api/merchants`
- 门店：`POST /api/shops` / `GET /api/shops?city=上海`
- 订单：`POST /api/orders` / `GET /api/orders?userId=1`

大模型相关模块已预留位置，按需后续接入。

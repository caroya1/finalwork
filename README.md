# 类大众点评系统（SpringBoot + Vue3）

当前版本为不含大模型模块的可运行骨架，覆盖用户端/商户端/管理员端页面与后端推荐、订单、商户、用户模块。

## 环境要求
- JDK 17
- Spring Boot 2.7.x
- Vue 3 + Vite
- MySQL（本地，root/123456）
- Redis（192.168.145.128:6379，密码123456）
- rabbitMQ（本地 5672）

## 后端启动
1. 创建数据库 `dianping`
2. 启动 Redis 与 rabbitMQ
3. 进入 `backend` 目录运行：

```bash
mvn spring-boot:run
```

## 网关启动
进入 `gateway` 目录运行：

```bash
npm install
npm start
```

默认网关端口为 `8081`，后端目标为 `http://localhost:8080`。

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

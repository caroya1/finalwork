# 大众点评微服务系统

基于 Spring Cloud 微服务架构的大众点评平台，提供服务发现、API 网关、认证授权、分布式配置等功能。

## 项目概述

本项目是一个完整的大众点评平台微服务系统，包含以下核心功能：

- **用户服务**：用户注册、登录、个人信息管理
- **商家服务**：商家入驻、商家信息管理
- **店铺服务**：店铺信息管理、店铺搜索
- **优惠券服务**：优惠券发放、核销、管理
- **订单服务**：订单创建、支付、管理
- **帖子服务**：点评发布、图片上传（腾讯云 COS）
- **推荐服务**：基于地理位置和用户偏好的推荐算法
- **认证服务**：JWT 认证、Token 管理
- **API 网关**：统一入口、JWT 验证、路由转发

## 技术栈

### 后端技术

| 技术 | 版本 | 说明 |
|------|------|------|
| Spring Boot | 3.2.5 | 基础框架 |
| Spring Cloud | 2023.0.2 | 微服务框架 |
| Spring Cloud Alibaba | 2023.0.1.0 | 阿里巴巴微服务套件 |
| Nacos | 2.3.2 | 服务发现与配置中心 |
| Spring Security | 6.2.4 | 安全框架 |
| JWT | 0.11.5 | Token 认证 |
| MyBatis Plus | 3.5.7 | ORM 框架 |
| MySQL | 8.0 | 关系型数据库 |
| Redis | 7.x | 缓存数据库 |
| OpenFeign | 4.1.1 | 服务间调用 |
| Gateway | 4.1.3 | API 网关 |

### 前端技术

- Vue 3 + Vite
- Element Plus
- Pinia 状态管理
- Axios HTTP 客户端

## 项目结构

```
finalWork/
├── docs/                          # 项目文档
│   ├── README.md                  # 项目总览
│   ├── ARCHITECTURE.md            # 架构设计
│   ├── SERVICES.md                # 服务说明
│   ├── CONFIGURATION.md           # 配置说明
│   ├── API.md                     # API 文档
│   ├── DEPLOYMENT.md              # 部署指南
│   └── CHANGELOG.md               # 修复记录
├── gateway/                       # API 网关
├── services/                      # 微服务模块
│   ├── common/                    # 公共模块
│   ├── auth-service/              # 认证服务
│   ├── user-service/              # 用户服务
│   ├── merchant-service/          # 商家服务
│   ├── shop-service/              # 店铺服务
│   ├── coupon-service/            # 优惠券服务
│   ├── order-service/             # 订单服务
│   ├── post-service/              # 帖子服务
│   └── recommendation-service/    # 推荐服务
├── nacos-config/                  # Nacos 配置文件
├── frontend-user/                 # 用户前端
├── frontend-merchant/             # 商家前端
└── frontend-admin/                # 管理员前端
```

## 快速开始

### 环境要求

- JDK 17+
- Maven 3.8+
- MySQL 8.0+
- Redis 7.x+
- Nacos 2.3.2+

### 启动步骤

1. **启动基础设施**
   ```bash
   # 启动 Nacos
   # 访问 http://localhost:8848/nacos
   # 用户名: nacos, 密码: nacos
   
   # 启动 MySQL
   # 启动 Redis
   ```

2. **导入 Nacos 配置**
   ```bash
   # 所有配置文件已导入到 Nacos
   # 配置文件位于 nacos-config/ 目录
   ```

3. **启动微服务**
   ```bash
   # 按顺序启动服务
   cd services/auth-service && mvn spring-boot:run
   cd services/user-service && mvn spring-boot:run
   cd services/merchant-service && mvn spring-boot:run
   cd services/shop-service && mvn spring-boot:run
   cd services/coupon-service && mvn spring-boot:run
   cd services/order-service && mvn spring-boot:run
   cd services/post-service && mvn spring-boot:run
   cd services/recommendation-service && mvn spring-boot:run
   cd gateway && mvn spring-boot:run
   ```

4. **启动前端**
   ```bash
   cd frontend-user && npm install && npm run dev
   cd frontend-merchant && npm install && npm run dev
   cd frontend-admin && npm install && npm run dev
   ```

### 访问地址

- **网关**: http://localhost:8081
- **Nacos**: http://localhost:8848/nacos
- **用户前端**: http://localhost:5173
- **商家前端**: http://localhost:5174
- **管理员前端**: http://localhost:5175

## 核心特性

### 1. 统一认证授权

- 基于 JWT 的无状态认证
- 网关层统一 Token 验证
- Token 自动续期机制
- 用户信息在服务间自动传递

### 2. 服务间通信

- OpenFeign 声明式调用
- 用户上下文自动传递
- 服务降级与熔断（Sentinel）

### 3. 分布式配置

- Nacos 配置中心
- 动态配置刷新
- 环境隔离

### 4. API 网关

- 统一入口
- JWT 验证
- 路由转发
- CORS 配置
- 限流熔断

## 服务列表

| 服务 | 端口 | 说明 |
|------|------|------|
| dianping-gateway | 8081 | API 网关 |
| auth-service | 8091 | 认证服务 |
| user-service | 8092 | 用户服务 |
| merchant-service | 8093 | 商家服务 |
| shop-service | 8094 | 店铺服务 |
| coupon-service | 8095 | 优惠券服务 |
| order-service | 8096 | 订单服务 |
| post-service | 8086 | 帖子服务 |
| recommendation-service | 8098 | 推荐服务 |

## 文档导航

- [架构设计](./ARCHITECTURE.md) - 系统架构与设计理念
- [服务说明](./SERVICES.md) - 各服务详细说明
- [配置说明](./CONFIGURATION.md) - 配置项详解
- [API 文档](./API.md) - 接口文档
- [部署指南](./DEPLOYMENT.md) - 生产环境部署
- [修复记录](./CHANGELOG.md) - 问题修复历史

## 开发团队

- 架构设计：微服务架构
- 后端开发：Spring Cloud Alibaba
- 前端开发：Vue 3 + Element Plus
- 运维部署：Docker + Kubernetes（规划中）

## 许可证

本项目仅供学习交流使用。

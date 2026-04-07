# 大众点评系统（Dianping Clone）

基于 Spring Cloud Alibaba + Vue3 构建的本地生活服务平台，涵盖用户端、商户端、管理员端三大模块，支持店铺推荐、优惠券、订单管理等功能。

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.5-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Vue.js](https://img.shields.io/badge/Vue.js-3.4-blue.svg)](https://vuejs.org/)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

## 📋 目录

- [项目简介](#项目简介)
- [技术架构](#技术架构)
- [环境要求](#环境要求)
- [快速开始](#快速开始)
- [项目结构](#项目结构)
- [服务说明](#服务说明)
- [功能详解](#功能详解)
- [接口文档](#接口文档)
- [开发规范](#开发规范)
- [常见问题](#常见问题)

## 🎯 项目简介

### 功能特性

**用户端（端口 5173）**
- 🔍 店铺搜索与筛选（按城市、分类、评分）
- 🤖 AI智能助手（支持美食、酒店、电影、景点等多场景推荐）
- 📍 附近店铺推荐
- 🎫 优惠券领取与使用
- 📝 帖子发布与互动（点赞、评论）
- ⭐ 店铺评分与评价
- 🛒 **到店消费** - 在店铺页面直接下单，自动选择最优优惠券，创建订单并支付
- 📦 **订单管理** - 查看订单列表、支付订单、取消待支付订单

**商户端（端口 5174）**
- 🏠 **工作台** - 查看统计数据（有效订单数、营业额）
- 🏪 **门店管理** - 创建和编辑门店信息
- 🍽️ **菜品管理** - 添加和管理门店菜品
- 🎟️ **优惠券管理** - 发布特价券和平价券
- 📦 **订单管理** - 查看用户订单、核销已支付订单
- 统一侧边栏导航布局

**管理端（端口 5175）**
- 📊 **运营看板** - 查看今日订单、待审商户、待审店铺、待审内容统计
- 🏢 **商户审核** - 审核商户入驻申请
- 🏪 **店铺审核** - 审核店铺创建申请
- 📝 **内容审核** - 审核用户发布的帖子
- 📦 **订单管理** - 查询平台所有订单、导出CSV
- 📈 **数据统计** - 查看平台核心指标
- 统一侧边栏导航布局

## 🏗️ 技术架构

### 系统架构图

```
┌─────────────────────────────────────────────────────────────┐
│                         前端层                               │
│  ┌──────────────┬──────────────┬──────────────┐            │
│  │  用户端       │   商户端      │   管理端     │            │
│  │  (Vue3)      │   (Vue3)     │   (Vue3)     │            │
│  │  Port: 5173  │   Port: 5174 │   Port: 5175 │            │
│  └──────────────┴──────────────┴──────────────┘            │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                        网关层 (Gateway)                      │
│                   Spring Cloud Gateway                       │
│                     Port: 8081                               │
│          统一入口 / 路由转发 / JWT认证 / 限流                 │
└─────────────────────────────────────────────────────────────┘
                              │
          ┌───────────────────┼───────────────────┐
          ▼                   ▼                   ▼
┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐
│   认证服务       │  │   用户服务       │  │   商户服务       │
│  auth-service   │  │  user-service   │  │ merchant-service│
│    Port: 8091   │  │    Port: 8092   │  │    Port: 8093   │
└─────────────────┘  └─────────────────┘  └─────────────────┘
┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐
│   店铺服务       │  │   优惠券服务     │  │   订单服务       │
│  shop-service   │  │ coupon-service  │  │  order-service  │
│    Port: 8094   │  │    Port: 8095   │  │    Port: 8096   │
└─────────────────┘  └─────────────────┘  └─────────────────┘
┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐
│   帖子服务       │  │   推荐服务       │  │   AI服务        │
│  post-service   │  │ recommendation- │  │  ai-service     │
│    Port: 8097   │  │    service      │  │    Port: 8099   │
└─────────────────┘  │    Port: 8098   │  └─────────────────┘
                     └─────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                      基础设施层                              │
│  ┌────────────┬────────────┬────────────┬────────────────┐ │
│  │   Nacos    │   Redis    │   MySQL    │  腾讯云COS     │ │
│  │  注册中心   │   缓存     │   数据库   │   对象存储     │ │
│  │Port: 8848  │Port: 6379  │Port: 3306  │                │ │
│  └────────────┴────────────┴────────────┴────────────────┘ │
└─────────────────────────────────────────────────────────────┘
```

### 技术栈

**后端**
- **核心框架**: Spring Boot 3.2.5 + Spring Cloud Alibaba 2022.0.0
- **微服务治理**: Nacos（服务注册/发现/配置中心）
- **网关**: Spring Cloud Gateway
- **ORM**: MyBatis-Plus 3.5.5
- **缓存**: Redis
- **对象存储**: 腾讯云 COS
- **安全**: Spring Security + JWT
- **AI大模型**: 通义千问（DashScope API）
- **分布式锁**: Redisson

**前端**
- **框架**: Vue 3.4
- **构建工具**: Vite
- **路由**: Vue Router 4
- **状态管理**: 原生响应式（Vue 3 Composition API）
- **HTTP 客户端**: Axios
- **样式**: 自定义 CSS Variables
- **测试**: Vitest

## 🚀 环境要求

### 开发环境
- **JDK**: 17+
- **Maven**: 3.8+
- **Node.js**: 18+
- **MySQL**: 8.0+
- **Redis**: 6.0+

### 服务器环境（虚拟机 IP: 192.168.145.128）
| 服务 | 地址 | 用户名/密码 | 说明 |
|------|------|------------|------|
| Nacos | http://192.168.145.128:8848 | nacos/nacos | 注册中心与配置中心 |
| Redis | 192.168.145.128:6379 | 密码: 123456 | 缓存服务 |
| MySQL | 192.168.145.128:3306 | root/123456 | 数据库服务 |

### 数据库列表
- `dianping_user` - 用户服务数据库
- `dianping_merchant` - 商户服务数据库
- `dianping_shop` - 店铺服务数据库
- `dianping_coupon` - 优惠券服务数据库
- `dianping_order` - 订单服务数据库
- `dianping_post` - 帖子服务数据库
- `dianping_recommendation` - 推荐服务数据库
- `dianping_ai` - AI服务数据库（审核记录）

## 📦 项目结构

```
finalWork/
├── services/                    # 微服务模块
│   ├── common/                 # 公共模块（工具类、配置、接口）
│   ├── gateway/                # API网关
│   ├── auth-service/           # 认证服务
│   ├── user-service/           # 用户服务
│   ├── merchant-service/       # 商户服务
│   ├── shop-service/           # 店铺服务
│   ├── coupon-service/         # 优惠券服务
│   ├── order-service/          # 订单服务
│   ├── post-service/           # 帖子服务
│   └── recommendation-service/ # 推荐服务
│
├── frontend-user/              # 用户端前端
├── frontend-merchant/          # 商户端前端（侧边栏布局）
├── frontend-admin/             # 管理端前端（侧边栏布局）
│
├── nacos-config/               # Nacos配置文件
│   ├── auth-service.yml
│   ├── user-service.yml
│   ├── merchant-service.yml
│   ├── shop-service.yml
│   ├── coupon-service.yml
│   ├── order-service.yml
│   ├── post-service.yml
│   ├── recommendation-service.yml
│   └── dianping-gateway.yml
│
├── sql/                        # 数据库脚本
│   └── fix_in_store_consumption.sql  # 到店消费功能字段
│
├── .sisyphus/                  # 知识库
│   ├── notepads/               # 架构决策记录
│   └── plans/                  # 工作计划
│
└── README.md                   # 项目说明文档
```

## 🚦 快速开始

### 1. 克隆项目

```bash
git clone https://github.com/caroya1/finalwork.git
cd finalwork
```

### 2. 初始化数据库

```bash
# 在MySQL中执行（或使用MySQL Workbench等工具）
mysql -h 192.168.145.128 -u root -p

# 创建数据库（如不存在会自动创建）
# 各服务首次启动时会自动创建表结构（MyBatis-Plus自动建表）
```

### 3. 配置Nacos

```bash
# 进入nacos-config目录
cd nacos-config

# 运行导入脚本（Windows）
import-to-nacos.bat http://192.168.145.128:8848

# 或（Linux/Mac）
bash import-to-nacos.sh http://192.168.145.128:8848
```

**敏感信息配置**: 本项目使用环境变量管理敏感信息（API Key、密码等）：

1. **查看配置模板**（`nacos-config/secrets.template.yml`）了解需要配置哪些敏感信息

2. **创建本地 secrets 文件**（该文件已被 Git 忽略，不会被提交）：
   ```bash
   cd nacos-config
   cp secrets.template.yml secrets.yml
   # 编辑 secrets.yml 填入你的真实敏感信息
   ```

3. **配置方式**：
   
   **方式A：直接配置到 Nacos（推荐用于开发环境）**
   将 `secrets.yml` 中的配置复制到 Nacos 对应服务的配置文件中：
   - `ai-service.yml` - 配置 DashScope API Key
   - `post-service.yml` - 配置腾讯云 COS 密钥（secret-id, secret-key, bucket-name）
   
   **方式B：环境变量（推荐用于生产环境）**
   ```bash
   # Windows PowerShell
   $env:DASHSCOPE_API_KEY="sk-your-api-key"
   $env:TENCENT_SECRET_ID="your-secret-id"
   $env:TENCENT_SECRET_KEY="your-secret-key"
   
   # Linux/Mac
   export DASHSCOPE_API_KEY=sk-your-api-key
   export TENCENT_SECRET_ID=your-secret-id
   export TENCENT_SECRET_KEY=your-secret-key
   ```
   
   **配置优先级**: 环境变量 > Nacos 配置 > 配置文件默认值
   
4. **配置检查**: 确保以下配置项已正确设置：
   | 配置项 | 说明 | 所在文件 |
   |--------|------|----------|
   | `ai.dashscope.api-key` | 通义千问 API Key | ai-service.yml |
   | `app.oss.tencent.secret-id` | 腾讯云 Secret ID | post-service.yml |
   | `app.oss.tencent.secret-key` | 腾讯云 Secret Key | post-service.yml |
   | `app.oss.tencent.bucket-name` | COS 存储桶名称 | post-service.yml |

### 4. 编译项目

```bash
# 安装common模块到本地Maven仓库（必须先安装）
cd services/common
mvn clean install -DskipTests

# 编译所有服务
cd ../..
mvn clean install -DskipTests
```

### 5. 启动服务

**服务启动顺序（重要）**：

```bash
# 1. 启动网关（先启动，其他服务注册上来后会自动路由）
cd gateway
mvn spring-boot:run

# 2. 启动基础服务（无依赖或依赖少的服务）
cd ../services/auth-service
mvn spring-boot:run

cd ../user-service
mvn spring-boot:run

cd ../merchant-service
mvn spring-boot:run

cd ../shop-service
mvn spring-boot:run

# 3. 启动业务服务
cd ../coupon-service
mvn spring-boot:run

cd ../order-service
mvn spring-boot:run

cd ../post-service
mvn spring-boot:run

cd ../recommendation-service
mvn spring-boot:run

cd ../ai-service
mvn spring-boot:run
```

### 6. 启动前端

```bash
# 用户端
cd frontend-user
npm install
npm run dev

# 商户端（新终端）
cd frontend-merchant
npm install
npm run dev

# 管理端（新终端）
cd frontend-admin
npm install
npm run dev
```

### 7. 访问系统

- **用户端**: http://localhost:5173
- **商户端**: http://localhost:5174
- **管理端**: http://localhost:5175
- **API网关**: http://localhost:8081

## 🔧 服务说明

### 服务依赖关系

| 服务 | 依赖服务 | 说明 |
|------|---------|------|
| gateway | - | 无依赖，最先启动 |
| auth-service | - | 独立服务，用户认证 |
| user-service | - | 独立服务，用户信息管理 |
| merchant-service | - | 独立服务，商户管理 |
| shop-service | merchant-service | 依赖商户服务 |
| coupon-service | shop-service | 依赖店铺服务 |
| order-service | user-service, shop-service, coupon-service | 依赖用户、店铺、优惠券服务 |
| post-service | user-service, shop-service | 依赖用户、店铺服务 |
| recommendation-service | shop-service | 依赖店铺服务 |
| ai-service | shop-service | 依赖店铺服务，提供AI审核与推荐 |

### 服务端口映射

| 服务名 | 端口 | 服务ID | Nacos配置 |
|--------|------|--------|-----------|
| gateway | 8081 | - | dianping-gateway |
| auth-service | 8091 | auth-service | auth-service |
| user-service | 8092 | user-service | user-service |
| merchant-service | 8093 | merchant-service | merchant-service |
| shop-service | 8094 | shop-service | shop-service |
| coupon-service | 8095 | coupon-service | coupon-service |
| order-service | 8096 | order-service | order-service |
| post-service | 8097 | post-service | post-service |
| recommendation-service | 8098 | recommendation-service | recommendation-service |
| ai-service | 8099 | ai-service | ai-service |

## 🎯 功能详解

### 到店消费流程

用户到店消费的完整流程：

```
1. 用户在店铺页面输入消费金额
2. 系统自动选择最优优惠券（用户可手动取消选择）
3. 创建订单（优惠券立即核销）
4. 用户确认支付（扣减余额）
5. 订单完成
```

**技术要点**：
- 优惠券核销使用乐观锁防止超卖（`UPDATE ... WHERE status = 'paid'`）
- 支付时自动扣减用户余额，余额不足则支付失败
- 订单超时自动取消并返还优惠券

### 优惠券状态机

```
 paid → used（订单创建时核销）
 used → paid（订单取消/超时时返还）
 paid → refunded（用户主动退款）
```

### 金额处理规范

- **数据库**：统一使用"分"作为存储单位（避免浮点精度问题）
- **前端显示**：从分转换为元（除以100）
- **前端输入**：用户输入元，提交时转为分（乘以100）

### 前端布局

**商户端和管理端**：统一采用侧边栏导航布局

```
┌─────────────────────────────────────────┐
│  Logo          │                        │
├────────────────┤    页面内容区域         │
│  🏠 工作台      │                        │
│  🏪 门店管理    │                        │
│  🍽️ 菜品管理   │      RouterView        │
│  🎟️ 优惠券管理 │                        │
│  📦 订单管理    │                        │
├────────────────┤                        │
│  商户信息/退出  │                        │
└─────────────────────────────────────────┘
```

## 🤖 AI助手功能

### 功能简介

系统集成了**通义千问大模型**，提供智能对话式本地生活推荐服务：

**支持场景**
- 🍜 **美食推荐** - 根据用餐场景（聚餐、约会、商务等）推荐餐厅
- 🏨 **酒店住宿** - 根据位置、预算推荐舒适酒店
- 🎬 **电影娱乐** - 推荐附近影院及热门影片
- 🏞️ **景点游玩** - 推荐周边游、打卡景点
- ☕ **咖啡下午茶** - 推荐休闲场所

**使用方法**
1. 在用户端页面右下角点击「🤖 AI助手」按钮
2. 输入您的需求，例如：
   - "推荐适合约会的餐厅"
   - "找一家舒适的酒店"
   - "附近有什么好吃的"
   - "推荐个看电影的地方"
3. AI将智能分析您的需求并推荐最合适的地方

**技术实现**
- 后端：`ai-service` 服务通过 DashScope API 调用通义千问
- 前端：`SmartRecommendDialog.vue` 组件提供对话界面
- 接口：`POST /api/ai/chat` - 对话式推荐

## 📝 接口文档

### 网关统一入口

所有请求通过网关 `http://localhost:8081` 转发。

### 主要接口列表

**认证模块**
```
POST /api/auth/login           # 用户登录
POST /api/auth/register        # 用户注册
POST /api/auth/admin/login     # 管理员登录
POST /api/auth/logout          # 退出登录
```

**用户模块**
```
GET  /api/users/{id}           # 获取用户信息
PUT  /api/users/{id}           # 更新用户信息
GET  /api/users/{id}/followers # 获取粉丝列表
GET  /api/users/{id}/following # 获取关注列表
POST /api/users/{id}/follow    # 关注用户
GET  /api/users/profile        # 获取当前用户信息（含订单列表）
```

**商户模块**
```
POST /api/merchants/login      # 商户登录
POST /api/merchants/register   # 商户注册
GET  /api/merchants/{id}       # 获取商户信息
```

**店铺模块**
```
GET  /api/shops                # 店铺列表（可按城市、分类筛选）
GET  /api/shops/{id}           # 店铺详情
POST /api/shops                # 创建店铺（需商户权限）
GET  /api/shops/{id}/dishes    # 获取店铺菜品
GET  /api/shops/{id}/coupons   # 获取店铺优惠券
```

**优惠券模块**
```
GET  /api/coupons              # 优惠券列表
POST /api/coupons/{id}/seckill # 秒杀优惠券
GET  /api/coupons/my           # 我的优惠券
POST /api/coupons/best         # 获取最优优惠券（到店消费用）
```

**订单模块**
```
POST /api/orders               # 创建订单
GET  /api/orders/{id}          # 订单详情
GET  /api/orders               # 订单列表
POST /api/orders/{id}/pay      # 支付订单
POST /api/orders/{id}/cancel   # 取消订单
```

**帖子模块**
```
GET  /api/posts                # 帖子列表
GET  /api/posts/{id}           # 帖子详情
POST /api/posts                # 创建帖子
POST /api/posts/{id}/like      # 点赞帖子
POST /api/posts/{id}/comment   # 评论帖子
```

**推荐模块**
```
POST /api/recommendations      # 获取推荐列表
```

### 内部API（服务间调用）

**优惠券服务**
```
POST /internal/coupons/consume  # 核销优惠券
POST /internal/coupons/return   # 返还优惠券
```

**用户服务**
```
POST /internal/users/{userId}/deduct  # 扣减用户余额
```

**订单服务**
```
GET  /internal/orders/user/{userId}   # 查询用户订单列表
```

## ⚠️ 常见问题

### 1. 服务启动失败，提示找不到配置

**问题**: `Failed to configure a DataSource: 'url' attribute is not specified`

**解决**: 
- 检查Nacos是否正常运行
- 检查配置文件是否正确导入到Nacos
- 检查bootstrap.yml中的Nacos地址是否正确

### 2. 网关返回401 Unauthorized

**问题**: 请求被网关拦截

**解决**:
- 检查请求路径是否在白名单中（`/api/auth/login`, `/api/auth/register` 等）
- 检查Token是否有效
- 检查网关的 `JwtGatewayFilter` 配置

### 3. FeignClient调用失败

**问题**: `No instances available for xxx-service`

**解决**:
- 检查服务是否已注册到Nacos
- 检查Nacos服务列表中是否有该服务
- 检查服务名是否正确（区分大小写）

### 4. Redis连接失败

**问题**: `Unable to connect to Redis`

**解决**:
- 检查Redis服务是否启动
- 检查Redis密码是否正确（默认：123456）
- 检查防火墙是否允许连接
- 检查 order-service.yml 中的 Redis 配置

### 5. 优惠券核销失败

**问题**: `优惠券已被使用或不存在`

**解决**:
- 检查优惠券状态是否为 `paid`（已购买未使用）
- 检查优惠券是否属于当前用户
- 检查优惠券是否在有效期内

### 6. 订单支付失败

**问题**: `余额不足`

**解决**:
- 检查用户余额是否足够支付订单金额
- 检查订单优惠金额是否正确计算

### 7. 文件上传失败

**问题**: `OSS服务未配置，请联系管理员`

**解决**:
- 在Nacos中配置 `post-service.yml` 的腾讯云COS参数
- 或使用本地文件存储（需修改代码）

## 📚 开发规范

### 代码规范
- 使用阿里巴巴Java开发手册
- 统一使用UTF-8编码
- 代码注释使用中文

### Git提交规范
```
feat: 新功能
fix: 修复bug
docs: 文档更新
style: 代码格式调整
refactor: 重构
test: 测试相关
chore: 构建/工具相关
```

### 分支管理
- `master`: 主分支，稳定版本
- `develop`: 开发分支
- `feature/xxx`: 功能分支
- `bugfix/xxx`: 修复分支

### 数据库规范
- **命名**: `dianping_{service}`（每个服务独立数据库）
- **ORM**: MyBatis-Plus 3.5+
- **建表**: 自动建表（无需Flyway/Liquibase）

### 金额处理规范
- **存储**: 数据库统一使用"分"
- **显示**: 前端显示转换为"元"
- **输入**: 用户输入"元"，提交时转为"分"

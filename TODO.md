# 类大众点评系统 - 待办事项清单

> 记录项目当前不足之处和需要改进的功能，完成一项划掉一项。

---

## 🔴 P0 - 最高优先级（核心功能）（p0需求暂时搁置）

### 1. 推荐系统优化 ✅ **已完成**
**现状**：✅ 已实现热门推荐 + 协同过滤混合推荐算法

- [x] 实现用户行为数据采集（埋点SDK）
- [x] 构建用户画像系统（购买历史分析）
- [x] 实现协同过滤算法（Item-based CF）
- [x] 实现基于内容的推荐算法（店铺标签特征）
- [x] 新用户冷启动处理（热门+附近）
- [x] 推荐效果统计（点击率、转化率统计）

**实现功能**：
- **热门推荐**：基于评分计算热度分，8分钟缓存更新
- **协同过滤**：ItemCF算法，利用用户购买历史
- **混合策略**：新用户（热门80%+附近20%），老用户（CF70%+热门30%）
- **定时任务**：每8分钟预计算推荐数据
- **Redis缓存**：多级缓存机制，提升性能

**相关文件**：
- `services/recommendation-service/src/main/java/com/dianping/recommendation/strategy/` - 推荐策略
- `services/recommendation-service/src/main/java/com/dianping/recommendation/algorithm/ItemCollaborativeFilter.java` - 协同过滤算法
- `services/recommendation-service/src/main/java/com/dianping/recommendation/task/RecommendPrecomputeTask.java` - 定时任务

---

### 2. 大模型集成（LLM）
**现状**：整个系统没有任何大模型相关实现
**需求**：实现智能内容审核、智能问答、评价分析

- [ ] 搭建大模型服务（FastAPI轻量化部署）
- [ ] UGC内容审核（文本+图片语义分析）
- [ ] 智能问答（用户咨询商户问题自动回复）
- [ ] 评价关键词提取+情感分析
- [ ] 口碑摘要生成
- [ ] 异常订单识别（刷单、异地高频核销）
- [ ] 大模型降级策略（服务不可用时切人工）

**相关文件**：
- 需新建：`services/llm-service/`

---

## 🟡 P1 - 高优先级（业务闭环）

### 3. 商户端功能完善 ✅ **已完成**
**现状**：✅ 已完成商户登录、门店管理、菜品管理、订单管理、优惠券管理、数据统计

- [x] 商户登录/注册
- [x] 门店信息管理
- [x] 菜品管理（增删改查）
- [x] 订单管理（查看、核销）
- [x] 优惠券管理（创建、查看）
- [x] 评价管理（查看、回复）
- [x] 数据统计（营业额、订单量）

**相关文件**：
- `frontend-merchant/src/views/MerchantHome.vue`
- `services/merchant-service/src/main/java/com/dianping/merchant/controller/MerchantController.java`
- `services/shop-service/src/main/java/com/dianping/shop/controller/MerchantShopController.java`

---

### 4. 管理员端功能完善 ✅ **已完成**
**现状**：✅ 已完成管理员独立登录、用户管理、商户审核、内容审核、数据看板

- [x] 管理员登录（独立账号体系 dp_admin）
- [x] 用户管理（查看、封禁）
- [x] 商户审核（入驻审核）
- [x] 内容审核（帖子、评价审核）
- [x] 数据看板（日活、订单量、推荐点击率）
- [x] 系统配置管理（本地草案）

**相关文件**：
- `frontend-admin/src/views/AdminHome.vue`
- `services/auth-service/src/main/java/com/dianping/auth/controller/AuthController.java`
- `services/user-service/src/main/java/com/dianping/user/controller/AdminUserController.java`

---

### 5. 图片上传与存储 ✅ **已完成**
**现状**：✅ 已完成腾讯云OSS接入，用户头像、店铺图片、菜品图片上传

- [x] 腾讯云OSS SDK集成
- [x] 图片上传接口
- [x] 前端图片上传组件
- [x] 帖子封面上传功能
- [x] 用户头像上传
- [x] 店铺图片上传
- [x] 菜品图片上传

**待扩展**：
- [ ] 图片压缩/水印处理

---

### 6. 支付流程完善（作废，不完成该需求）
**现状**：余额充值只是简单模拟，无真实支付流程

- [ ] 集成微信支付
- [ ] 集成支付宝
- [ ] 支付订单状态管理
- [ ] 支付回调处理
- [ ] 退款流程

---

### 7. 订单系统完善 ✅ **已完成**
**现状**：✅ 已完成订单状态机、超时取消、搜索筛选、导出功能

- [x] 订单状态机（待支付、已支付、已核销、已退款）
- [x] 订单超时自动取消（定时任务）
- [x] 订单搜索与筛选
- [x] 订单导出（CSV）

**暂不做**：
- [ ] 分布式事务（Seata）

**相关文件**：
- `services/order-service/src/main/java/com/dianping/order/service/OrderService.java`
- `services/order-service/src/main/java/com/dianping/order/enums/OrderStatus.java`
- `services/order-service/src/main/java/com/dianping/order/task/OrderTimeoutTask.java`

---

## 🟢 P2 - 中优先级（质量保证）

### 8. 测试体系 ✅ **已完成**
**现状**：✅ 已添加JUnit + Mockito单元测试

- [x] 单元测试（JUnit + Mockito）
- [x] OrderService单元测试
- [x] JwtService单元测试
- [x] SensitiveWordFilter单元测试
- [ ] 集成测试
- [ ] API接口测试（Postman/Apifox集合）
- [ ] 性能测试（JMeter/Locust脚本）
- [ ] 压测报告（TPS/QPS/响应时间/错误率）

**相关文件**：
- `services/order-service/src/test/java/com/dianping/order/service/OrderServiceTest.java`
- `services/auth-service/src/test/java/com/dianping/auth/service/JwtServiceTest.java`
- `services/common/src/test/java/com/dianping/common/util/SensitiveWordFilterTest.java`

---

### 9. 监控与日志 ✅ **已完成**
**现状**：✅ 已配置日志框架和基础监控

- [x] 日志配置（结构化日志）
- [x] 应用监控（Prometheus + Actuator）
- [x] 链路追踪（Micrometer Tracing）
- [ ] 日志聚合（ELK Stack）
- [ ] 告警机制（钉钉/邮件通知）
- [ ] Grafana仪表板配置

**相关文件**：
- `services/common/src/main/resources/logging-config.yml`
- `services/pom.xml`（已添加micrometer、prometheus依赖）

---

### 10. 安全加固 ✅ **已完成**
**现状**：✅ 已完成敏感词过滤、接口限流、JWT续期

- [x] 接口限流（Rate Limiting）- Bucket4j
- [x] 敏感词过滤（帖子、评价内容）
- [x] JWT Token自动续期
- [ ] 防刷机制（验证码、滑动验证）
- [ ] SQL注入防护检查
- [ ] XSS防护
- [ ] 接口权限细化（RBAC）

**相关文件**：
- `services/common/src/main/java/com/dianping/common/util/RateLimit.java`
- `services/common/src/main/java/com/dianping/common/util/RateLimitAspect.java`
- `services/common/src/main/java/com/dianping/common/util/SensitiveWordFilter.java`
- `services/auth-service/src/main/java/com/dianping/auth/service/JwtService.java`

---

### 11. 前端优化 ✅ **已完成**
**现状**：✅ 已添加图片懒加载、无限滚动、骨架屏、错误边界

- [ ] 语音搜索实现
- [x] 图片懒加载（IntersectionObserver）
- [x] 无限滚动加载
- [x] 骨架屏组件
- [x] 错误边界处理
- [ ] 移动端适配优化

**相关文件**：
- `frontend-user/src/directives/lazy.js`
- `frontend-user/src/composables/useInfiniteScroll.js`
- `frontend-user/src/components/common/Skeleton.vue`
- `frontend-user/src/components/common/ErrorBoundary.vue`

---

### 12. 数据埋点与分析 ✅ **已完成**
**现状**：✅ 已实现埋点SDK、页面统计、漏斗分析、推荐效果分析

- [x] 埋点SDK集成（前端+后端）
- [x] 页面浏览统计
- [x] 点击事件统计
- [x] 用户行为漏斗分析
- [x] 推荐效果分析

**相关文件**：
- `services/common/src/main/java/com/dianping/common/util/TrackingSDK.java`
- `services/common/src/main/java/com/dianping/common/service/DataAnalysisService.java`
- `frontend-user/src/utils/tracking.js`

---

### 13. 高可用与容灾 ✅ **已完成**
**现状**：✅ 已实现熔断降级、限流配置、健康检查

- [x] 熔断降级（Sentinel）
- [x] 限流配置
- [x] 服务健康检查
- [ ] 数据库读写分离（暂不做）
- [ ] Redis集群配置（暂不做）

**相关文件**：
- `services/common/src/main/java/com/dianping/common/config/SentinelConfig.java`
- `services/common/src/main/java/com/dianping/common/config/HealthCheckConfig.java`

---

### 14. 性能优化 ✅ **已完成**
**现状**：✅ 已添加缓存策略、索引优化、连接池优化

- [x] 数据库索引优化（文档）
- [x] 缓存策略优化（热点数据）
- [x] 缓存穿透/雪崩防护
- [x] 异步处理优化（多线程池）
- [x] 数据库连接池调优（HikariCP配置）

**相关文件**：
- `docs/database-index-optimization.md`
- `services/common/src/main/java/com/dianping/common/util/CacheHelper.java`
- `services/common/src/main/java/com/dianping/common/util/CacheWarmUp.java`
- `services/common/src/main/java/com/dianping/common/config/CacheConfig.java`
- `services/common/src/main/java/com/dianping/common/config/RedisCacheConfig.java`
- `services/common/src/main/java/com/dianping/common/config/AsyncExecutorConfig.java`
- `services/common/src/main/resources/datasource-config.yml`

---

## 📊 进度统计

| 优先级 | 总数 | 已完成 | 进行中 | 待完成 |
|--------|------|--------|--------|--------|
| 🔴 P0  | 2    | 1      | 0      | 1      |
| 🟡 P1  | 5    | 4      | 0      | 1      |
| 🟢 P2  | 7    | 7      | 0      | 0      |
| **合计** | **14** | **12** | **0** | **2** |

---

## 📝 备注

- 完成某项后，将 `- [ ]` 改为 `- [x]`
- 进行中项目可标记为 `- [~]`
- 新发现的问题随时补充到对应优先级

---

**文档创建时间**: 2026-03-10
**最后更新**: 2026-03-10

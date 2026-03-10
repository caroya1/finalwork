# 类大众点评系统 - 待办事项清单

> 记录项目当前不足之处和需要改进的功能，完成一项划掉一项。

---

## 🔴 P0 - 最高优先级（核心功能）

### 1. 推荐系统优化
**现状**：当前仅使用 `Collections.shuffle()` 随机打乱店铺列表，没有真正的个性化算法
**需求**：基于用户行为数据、地理位置、场景需求生成精准推荐

- [ ] 实现用户行为数据采集（浏览历史、收藏、消费记录）
- [ ] 构建用户画像系统
- [ ] 实现协同过滤算法
- [ ] 实现基于内容的推荐算法
- [ ] 场景匹配算法（如"带老人聚餐"推荐清淡商户）
- [ ] 推荐效果统计（点击率、转化率）

**相关文件**：
- `services/recommendation-service/src/main/java/com/dianping/recommendation/service/RecommendationService.java`

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

### 6. 支付流程完善
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

### 8. 测试体系
**现状**：完全缺失

- [ ] 单元测试（JUnit + Mockito）
- [ ] 集成测试
- [ ] API接口测试（Postman/Apifox集合）
- [ ] 性能测试（JMeter/Locust脚本）
- [ ] 压测报告（TPS/QPS/响应时间/错误率）

---

### 9. 监控与日志
**现状**：完全缺失

- [ ] 日志聚合（ELK Stack）
- [ ] 应用监控（Prometheus + Grafana）
- [ ] 链路追踪（Sleuth + Zipkin）
- [ ] 告警机制（钉钉/邮件通知）
- [ ] 健康检查接口

---

### 10. 安全加固 🔄 **部分完成**
**现状**：✅ 已完成敏感词过滤

- [ ] 接口限流（Rate Limiting）
- [x] 敏感词过滤（帖子、评价内容）
- [ ] 防刷机制（验证码、滑动验证）
- [ ] SQL注入防护检查
- [ ] XSS防护
- [ ] JWT Token自动续期
- [ ] 接口权限细化（RBAC）

**相关文件**：
- `services/common/src/main/java/com/dianping/common/util/SensitiveWordFilter.java`

---

### 11. 前端优化
**现状**：部分功能缺失，体验待优化

- [ ] 语音搜索实现
- [ ] 图片懒加载
- [ ] 无限滚动加载
- [ ] 骨架屏
- [ ] 错误边界处理
- [ ] 移动端适配优化

---

### 12. 数据埋点与分析
**现状**：用户行为数据未采集

- [ ] 埋点SDK集成
- [ ] 页面浏览统计
- [ ] 点击事件统计
- [ ] 用户行为漏斗分析
- [ ] 推荐效果分析

---

### 13. 高可用与容灾
**现状**：缺少熔断降级机制

- [ ] 熔断降级（Sentinel）
- [ ] 限流配置
- [ ] 服务健康检查
- [ ] 数据库读写分离
- [ ] Redis集群配置

---

### 14. 性能优化
**现状**：部分实现可优化

- [ ] 数据库索引优化
- [ ] 缓存策略优化（热点数据）
- [ ] 缓存穿透/雪崩防护
- [ ] 异步处理优化
- [ ] 数据库连接池调优

---

## 📊 进度统计

| 优先级 | 总数 | 已完成 | 进行中 | 待完成 |
|--------|------|--------|--------|--------|
| 🔴 P0  | 2    | 0      | 0      | 2      |
| 🟡 P1  | 5    | 4      | 0      | 1      |
| 🟢 P2  | 7    | 0      | 1      | 6      |
| **合计** | **14** | **4** | **1** | **9** |

---

## 📝 备注

- 完成某项后，将 `- [ ]` 改为 `- [x]`
- 进行中项目可标记为 `- [~]`
- 新发现的问题随时补充到对应优先级

---

**文档创建时间**: 2026-03-10
**最后更新**: 2026-03-10

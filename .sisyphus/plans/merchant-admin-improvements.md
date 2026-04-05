# 商家端+管理端优化完整执行计划

**Plan**: merchant-admin-improvements  
**Scope**: frontend-merchant + frontend-admin  
**Generated**: 2025-04-04  
**Version**: 1.0

---

## TL;DR

> **目标**：补齐商家端和管理端的可用性短板，建立稳定、高效、可维护的前端体系
> 
> **交付物**：
> - 商家端：独立登录系统 + 完整业务闭环 + 可用性优化
> - 管理端：高效审核工作流 + 数据看板 + 运营工具
> 
> **执行策略**：5波次渐进交付，每波次可独立验收
> - Wave 1（基础可用性）→ Wave 2（商家业务）→ Wave 3（审核效率）→ Wave 4（数据运营）→ Wave 5（体验打磨）
> 
> **预估工作量**：25-30个任务，约5-7天开发周期

---

## Context

### 项目现状
- **商家端** (`frontend-merchant`): 有工作台、门店创建、优惠券、菜品、订单管理，但缺少登录页和统一错误处理
- **管理端** (`frontend-admin`): 有运营看板、审核功能，但操作流不完整，缺少登录和批量操作
- **技术栈**: Vue 3.4 + Vite + 纯CSS变量，无外部UI库

### 主要痛点
1. **无法独立使用**: 缺少登录界面，依赖用户端token
2. **可用性差**: 无加载态、无错误提示、表单无验证
3. **审核效率低**: 单条操作、硬编码原因、无批量功能
4. **数据不完整**: 看板缺少趋势、配置仅本地有效

---

## Work Objectives

### Core Objective
建立可独立运行的商家端和管理端，补齐核心可用性和业务效率短板。

### Concrete Deliverables
1. 商家端独立登录/登出系统 (`Login.vue`)
2. 管理端独立登录/登出系统 (`Login.vue`)
3. 统一错误处理与提示机制
4. 加载/空状态组件库
5. 订单状态文本映射工具
6. 批量审核操作功能
7. 动态拒绝原因输入
8. 实时数据刷新机制
9. 移动端响应式优化
10. 代码清理与文档

### Definition of Done
每波次任务完成后：
- [ ] 功能可正常运行
- [ ] 错误边界已测试
- [ ] 代码审查通过（无console.log，无硬编码）
- [ ] QA场景验证通过

### Must NOT Have (Guardrails)
- 不引入Element Plus/Ant Design等UI库
- 不改变Vue3 Composition API模式
- 不修改后端API（纯前端改动）
- 不改变认证机制（保持JWT）

---

## Verification Strategy

### Test Decision
- **Infrastructure**: 已存在（Vite + Vue3），无需新增
- **Automated tests**: 无（项目当前无测试框架）
- **Agent-Executed QA**: **强制要求** - 每任务必须通过Playwright场景验证

### QA Policy
- **Frontend**: Playwright技能 → 页面访问、元素可见、交互、截图
- **Acceptance**: 零人工介入，全自动化验证

---

## Execution Strategy

### Wave 1: 基础可用性（Foundation - 阻塞后续所有任务）

**目标**：让两个前端能独立运行，建立错误处理基础

```
Wave 1 (必须首先完成):
├── Task 1.1: 商家端登录页
├── Task 1.2: 管理端登录页
├── Task 1.3: 统一错误处理拦截器
├── Task 1.4: 路由完善 + 导航组件
└── Task 1.5: 401/403自动跳转
```

### Wave 2: 商家端业务体验

**目标**：补齐商家日常操作的可用性短板

```
Wave 2:
├── Task 2.1: 加载状态组件（全局）
├── Task 2.2: 空状态组件（全局）
├── Task 2.3: 表单验证 + 错误提示
├── Task 2.4: 图片上传预览
├── Task 2.5: 订单状态文本映射
├── Task 2.6: 店铺编辑功能
├── Task 2.7: 消息提示组件（Toast）
└── Task 2.8: Console.log清理
```

### Wave 3: 管理端审核效率

**目标**：提升审核人员工作效率

```
Wave 3:
├── Task 3.1: 批量选择组件
├── Task 3.2: 批量审核操作（通过/拒绝）
├── Task 3.3: 拒绝原因弹窗输入
├── Task 3.4: 审核详情侧滑/弹窗
├── Task 3.5: 快捷操作按钮（键盘支持）
├── Task 3.6: 高级筛选组件
└── Task 3.7: 审核统计优化
```

### Wave 4: 数据与运营

**目标**：运营数据可视化和系统配置

```
Wave 4:
├── Task 4.1: 实时数据自动刷新
├── Task 4.2: 趋势数据展示（简化图表）
├── Task 4.3: 操作日志列表
├── Task 4.4: 导出进度提示
├── Task 4.5: 系统配置持久化（后端接口）
└── Task 4.6: 数据看板移动端适配
```

### Wave 5: 体验打磨与验收

**目标**：最终优化、代码清理、整体验收

```
Wave 5:
├── Task 5.1: 移动端响应式全面优化
├── Task 5.2: 动画过渡统一
├── Task 5.3: 键盘快捷键支持
├── Task 5.4: 代码注释与文档
├── Task 5.5: 最终端到端测试
├── F1: Plan Compliance Audit
├── F2: Code Quality Review
└── F3: Manual QA & Approval
```

---

## TODOs

### Wave 1: 基础可用性（Foundation）

- [x] **1.1 商家端登录页**

  **What to do**:
  创建 `frontend-merchant/src/views/Login.vue`，实现商户独立登录
  - 用户名/密码输入表单
  - 调用 `/api/auth/login` 接口
  - 存储 token, refreshToken, merchantId, merchantName 到 localStorage
  - 登录成功后跳转到首页
  - 样式与现有商家端保持一致（使用CSS变量）

  **Must NOT do**:
  - 不引入UI库，使用纯CSS
  - 不改后端接口

  **Recommended Agent Profile**:
  - **Category**: `visual-engineering`
  - **Skills**: [`frontend-ui-ux`]
  - Reason: 需要创建新的页面组件，保持视觉一致性

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 1
  - **Blocks**: Task 1.4（路由需要Login组件）

  **References**:
  - `frontend-merchant/src/views/MerchantHome.vue` - 参考样式变量使用
  - `frontend-merchant/src/api/client.js` - token存储方式
  - `frontend-user/src/views/` - 参考用户端登录实现（如存在）

  **Acceptance Criteria**:
  - [ ] 登录页可访问 `/login`
  - [ ] 输入正确凭证后登录成功
  - [ ] localStorage包含dp_token, dp_refresh_token, dp_merchant_id
  - [ ] 登录后跳转到首页
  - [ ] 表单有基础验证（非空检查）

  **QA Scenarios**:
  ```
  Scenario: 商家成功登录
    Tool: Playwright
    Steps:
      1. Navigate to http://localhost:5174/login
      2. Fill username: "test_merchant"
      3. Fill password: "123456"
      4. Click login button
    Expected: URL changes to /, localStorage has dp_token
    Evidence: .sisyphus/evidence/task-1-1-login-success.png

  Scenario: 商家登录失败
    Tool: Playwright
    Steps:
      1. Navigate to http://localhost:5174/login
      2. Fill wrong password
      3. Click login
    Expected: Error message visible, stays on login page
    Evidence: .sisyphus/evidence/task-1-1-login-error.png
  ```

  **Commit**: YES
  - Message: `feat(merchant): add standalone login page`
  - Files: `frontend-merchant/src/views/Login.vue`

- [x] **1.2 管理端登录页**

  **What to do**:
  创建 `frontend-admin/src/views/Login.vue`，实现管理员独立登录
  - 类似商家端登录，角色为 `ROLE_ADMIN`
  - 存储 admin token 到 localStorage

  **Must NOT do**:
  - 不引入UI库
  - 与Task 1.1保持代码结构一致

  **Recommended Agent Profile**:
  - **Category**: `visual-engineering`
  - **Skills**: [`frontend-ui-ux`]

  **Parallelization**:
  - **Can Run In Parallel**: YES (with Task 1.1)
  - **Parallel Group**: Wave 1

  **References**:
  - Task 1.1实现代码
  - `frontend-admin/src/api/client.js`

  **Acceptance Criteria**:
  - [ ] 登录页可访问 `/login`
  - [ ] 管理员可成功登录
  - [ ] 登录后跳转到首页

  **QA Scenarios**:
  ```
  Scenario: 管理员登录
    Tool: Playwright
    Steps:
      1. Navigate to http://localhost:5175/login
      2. Fill admin credentials
      3. Click login
    Expected: Redirect to /, show admin dashboard
    Evidence: .sisyphus/evidence/task-1-2-admin-login.png
  ```

  **Commit**: YES
  - Message: `feat(admin): add standalone login page`

- [x] **1.3 统一错误处理拦截器**

  **What to do**:
  增强 `api/client.js` 的 response 拦截器
  - 统一处理 401/403/500 错误
  - 显示友好的错误提示（不使用alert）
  - 401时自动清除token并跳转到登录页
  - 添加请求loading状态管理

  **Must NOT do**:
  - 不使用浏览器原生alert/confirm
  - 不改变API调用方式（保持.then链）

  **Recommended Agent Profile**:
  - **Category**: `quick`
  - **Skills**: []

  **Parallelization**:
  - **Can Run In Parallel**: NO (需要等待1.1/1.2了解登录逻辑)
  - **Blocked By**: Task 1.1, Task 1.2

  **References**:
  - `frontend-merchant/src/api/client.js`
  - `frontend-admin/src/api/client.js`

  **Acceptance Criteria**:
  - [ ] 401错误自动跳转登录页
  - [ ] 403显示权限不足提示
  - [ ] 500显示服务器错误提示
  - [ ] 网络错误显示连接失败提示

  **QA Scenarios**:
  ```
  Scenario: Token过期自动跳转
    Tool: Playwright
    Preconditions: localStorage有过期token
    Steps:
      1. Navigate to http://localhost:5174/
      2. Wait for API request
    Expected: Redirected to /login, token cleared
    Evidence: .sisyphus/evidence/task-1-3-401-redirect.png
  ```

  **Commit**: YES
  - Message: `feat(shared): add unified error handling interceptor`
  - Files: `frontend-merchant/src/api/client.js`, `frontend-admin/src/api/client.js`

- [x] **1.4 路由完善 + 导航组件**

  **What to do**:
  - 更新 `router/index.js`，添加login路由和路由守卫
  - 创建基础布局组件（带导航）
  - 商家端：顶部导航（工作台、退出）
  - 管理端：侧边导航（看板、审核、记录）

  **Must NOT do**:
  - 不引入路由库（继续使用vue-router）
  - 导航样式保持简洁

  **Recommended Agent Profile**:
  - **Category**: `visual-engineering`
  - **Skills**: [`frontend-ui-ux`]

  **Parallelization**:
  - **Can Run In Parallel**: NO
  - **Blocked By**: Task 1.1, Task 1.2

  **References**:
  - `frontend-merchant/src/router/index.js`
  - `frontend-admin/src/router/index.js`
  - `frontend-merchant/src/App.vue`

  **Acceptance Criteria**:
  - [ ] 商家端有导航栏，可跳转到登录/首页
  - [ ] 管理端有侧边导航
  - [ ] 未登录访问首页自动跳转登录

  **QA Scenarios**:
  ```
  Scenario: 未登录访问首页被拦截
    Tool: Playwright
    Preconditions: Clear localStorage
    Steps:
      1. Navigate to http://localhost:5174/
    Expected: Redirected to /login
    Evidence: .sisyphus/evidence/task-1-4-guard.png
  ```

  **Commit**: YES
  - Message: `feat(merchant,admin): add navigation and route guards`

- [x] **1.5 401/403自动跳转**

  **What to do**:
  在路由守卫和API拦截器中实现
  - 路由守卫：检查localStorage token存在性
  - API拦截器：401时跳转登录页
  - 管理端同样逻辑

  **Must NOT do**:
  - 不改变后端认证逻辑

  **Recommended Agent Profile**:
  - **Category**: `quick`
  - **Skills**: []

  **Parallelization**:
  - **Can Run In Parallel**: NO
  - **Blocked By**: Task 1.3, Task 1.4

  **Acceptance Criteria**:
  - [ ] 无token时访问任何页面跳转登录
  - [ ] API返回401时跳转登录

  **QA Scenarios**: (同1.3/1.4)

  **Commit**: YES (可合并到1.4)

---

### Wave 2: 商家端业务体验

- [x] **2.1 加载状态组件（全局）**

  **What to do**:
  创建 `components/Loading.vue`，在数据请求时显示
  - 旋转动画
  - 半透明遮罩
  - 可配置文字

  **Recommended Agent Profile**:
  - **Category**: `visual-engineering`
  - **Skills**: [`frontend-ui-ux`]

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 2-1 (与2.2/2.3并行)
  - **Blocked By**: Wave 1

  **Acceptance Criteria**:
  - [ ] 加载时显示旋转动画
  - [ ] 不阻塞页面其他部分

  **QA Scenarios**:
  ```
  Scenario: 加载门店列表时显示loading
    Tool: Playwright
    Steps:
      1. Navigate to merchant home
      2. Throttle network to slow 3G
      3. Reload page
    Expected: Loading spinner visible during request
    Evidence: .sisyphus/evidence/task-2-1-loading.png
  ```

  **Commit**: YES

- [x] **2.2 空状态组件（全局）**

  **What to do**:
  创建 `components/EmptyState.vue`，列表为空时显示
  - 图标 + 文字提示
  - 可配置操作按钮

  **Recommended Agent Profile**:
  - **Category**: `visual-engineering`
  - **Skills**: [`frontend-ui-ux`]

  **Parallelization**:
  - **Can Run In Parallel**: YES (with 2.1)

  **Acceptance Criteria**:
  - [ ] 空列表时显示友好提示
  - [ ] 显示"暂无数据"图标和文字

  **QA Scenarios**:
  ```
  Scenario: 无门店时显示空状态
    Tool: Playwright
    Preconditions: 新注册商户无门店
    Steps:
      1. Login as new merchant
      2. View shop list
    Expected: Empty state with "暂无门店" message
    Evidence: .sisyphus/evidence/task-2-2-empty.png
  ```

  **Commit**: YES

- [x] **2.3 表单验证 + 错误提示**

  **What to do**:
  完善 `MerchantHome.vue` 中的表单
  - 门店创建：名称、分类、城市必填
  - 优惠券：售价必须小于优惠金额
  - 菜品：名称和价格必填
  - 错误提示显示在表单项下方

  **Must NOT do**:
  - 不引入验证库（保持轻量）

  **Recommended Agent Profile**:
  - **Category**: `quick`
  - **Skills**: []

  **Parallelization**:
  - **Can Run In Parallel**: YES (with 2.1, 2.2)

  **Acceptance Criteria**:
  - [ ] 必填项为空时提交显示错误
  - [ ] 价格逻辑错误时提示
  - [ ] 错误信息中文、明确

  **QA Scenarios**:
  ```
  Scenario: 创建门店缺必填项
    Tool: Playwright
    Steps:
      1. Click submit without filling name
    Expected: Error message "请填写门店名称"
    Evidence: .sisyphus/evidence/task-2-3-validation.png
  ```

  **Commit**: YES

- [x] **2.4 图片上传预览**

  **What to do**:
  修改 `MerchantHome.vue` 的图片上传
  - 选择图片后立即显示预览
  - 上传成功后显示已上传图片
  - 支持删除/重新选择

  **Recommended Agent Profile**:
  - **Category**: `visual-engineering`
  - **Skills**: [`frontend-ui-ux`]

  **Parallelization**:
  - **Can Run In Parallel**: NO (依赖2.3表单结构)
  - **Blocked By**: Task 2.3

  **Acceptance Criteria**:
  - [ ] 选择图片后显示预览
  - [ ] 上传成功显示最终图片

  **QA Scenarios**:
  ```
  Scenario: 店铺图片预览
    Tool: Playwright
    Steps:
      1. Click image upload
      2. Select image file
    Expected: Preview shown before upload
    Evidence: .sisyphus/evidence/task-2-4-preview.png
  ```

  **Commit**: YES

- [x] **2.5 订单状态文本映射**

  **What to do**:
  创建 `utils/status.js`，映射订单状态数字到中文
  ```javascript
  const ORDER_STATUS = {
    0: '待支付',
    1: '已支付',
    2: '已核销',
    3: '已退款',
    4: '已取消'
  }
  ```
  在订单列表中使用

  **Recommended Agent Profile**:
  - **Category**: `quick`
  - **Skills**: []

  **Parallelization**:
  - **Can Run In Parallel**: YES

  **Acceptance Criteria**:
  - [ ] 订单状态显示中文（已支付、已核销等）

  **QA Scenarios**:
  ```
  Scenario: 订单状态显示中文
    Tool: Playwright
    Steps:
      1. View order list
    Expected: Status shows "已支付" instead of "1"
    Evidence: .sisyphus/evidence/task-2-5-status.png
  ```

  **Commit**: YES

- [x] **2.6 店铺编辑功能**

  **What to do**:
  在 `MerchantHome.vue` 的店铺列表中添加"编辑"按钮
  - 点击后填充表单
  - 调用 `updateShop` API
  - 更新后刷新列表

  **Must NOT do**:
  - 不创建新页面，使用现有表单区域

  **Recommended Agent Profile**:
  - **Category**: `quick`
  - **Skills**: []

  **Parallelization**:
  - **Can Run In Parallel**: NO
  - **Blocked By**: Task 2.3

  **Acceptance Criteria**:
  - [ ] 可编辑现有门店信息
  - [ ] 编辑后数据更新

  **Commit**: YES

- [x] **2.7 消息提示组件（Toast）**

  **What to do**:
  创建 `components/Toast.vue` 全局消息提示
  - 成功（绿色）、错误（红色）、信息（蓝色）
  - 自动消失
  - 可同时显示多个

  **Recommended Agent Profile**:
  - **Category**: `visual-engineering`
  - **Skills**: [`frontend-ui-ux`]

  **Acceptance Criteria**:
  - [ ] 操作成功后显示Toast
  - [ ] 3秒后自动消失

  **QA Scenarios**:
  ```
  Scenario: 创建门店成功提示
    Tool: Playwright
    Steps:
      1. Create shop successfully
    Expected: Toast "门店创建成功" visible
    Evidence: .sisyphus/evidence/task-2-7-toast.png
  ```

  **Commit**: YES

- [x] **2.8 Console.log清理**

  **What to do**:
  移除所有 `console.log` 和 `debugger`
  - 保留必要的错误日志（console.error）

  **Recommended Agent Profile**:
  - **Category**: `quick`
  - **Skills**: []

  **Acceptance Criteria**:
  - [ ] 无console.log残留
  - [ ] 生产环境无调试输出

  **Commit**: YES
  - Message: `chore(merchant): remove console.log statements`

---

### Wave 3: 管理端审核效率

- [x] **3.1 批量选择组件**

  **What to do**:
  在 `AdminHome.vue` 和 `AuditRecords.vue` 的列表中添加复选框
  - 表头全选
  - 每行可选择
  - 显示已选数量

  **Recommended Agent Profile**:
  - **Category**: `visual-engineering`
  - **Skills**: [`frontend-ui-ux`]

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 3-1
  - **Blocked By**: Wave 1

  **Acceptance Criteria**:
  - [ ] 可勾选多条记录
  - [ ] 全选/取消全选功能
  - [ ] 显示已选数量

  **QA Scenarios**:
  ```
  Scenario: 批量选择审核记录
    Tool: Playwright
    Steps:
      1. Click checkbox on 3 rows
      2. Check "Selected: 3" displayed
    Expected: Count updates correctly
    Evidence: .sisyphus/evidence/task-3-1-batch-select.png
  ```

  **Commit**: YES

- [x] **3.2 批量审核操作**

  **What to do**:
  添加批量通过/拒绝按钮
  - 调用批量审核API（或循环单条）
  - 操作确认弹窗
  - 完成后刷新列表

  **Must NOT do**:
  - 如果不存在批量API，使用Promise.all并发调用

  **Recommended Agent Profile**:
  - **Category**: `quick`
  - **Skills**: []

  **Parallelization**:
  - **Can Run In Parallel**: NO
  - **Blocked By**: Task 3.1

  **Acceptance Criteria**:
  - [ ] 可选择多条后批量通过
  - [ ] 可选择多条后批量拒绝
  - [ ] 操作前确认弹窗

  **QA Scenarios**:
  ```
  Scenario: 批量通过商户
    Tool: Playwright
    Steps:
      1. Select 2 pending merchants
      2. Click "批量通过"
      3. Confirm in modal
    Expected: Both approved, list refreshed
    Evidence: .sisyphus/evidence/task-3-2-batch-approve.png
  ```

  **Commit**: YES

- [x] **3.3 拒绝原因弹窗输入**

  **What to do**:
  替换硬编码拒绝原因
  - 点击"拒绝"弹出输入框
  - 必填拒绝原因
  - 调用API时传递reason参数

  **Must NOT do**:
  - 不删除原有硬编码，替换为动态输入

  **Recommended Agent Profile**:
  - **Category**: `quick`
  - **Skills**: []

  **Parallelization**:
  - **Can Run In Parallel**: NO
  - **Blocked By**: Task 3.2

  **Acceptance Criteria**:
  - [ ] 拒绝时必须输入原因
  - [ ] 原因随拒绝请求发送

  **QA Scenarios**:
  ```
  Scenario: 拒绝店铺并输入原因
    Tool: Playwright
    Steps:
      1. Click reject on shop
      2. Enter reason "证件不清晰"
      3. Confirm
    Expected: Shop rejected with reason
    Evidence: .sisyphus/evidence/task-3-3-reject-reason.png
  ```

  **Commit**: YES

- [x] **3.4 审核详情侧滑/弹窗**

  **What to do**:
  点击记录时显示详情
  - 显示完整内容
  - 在详情中可进行审核操作
  - 侧边栏或模态框形式

  **Recommended Agent Profile**:
  - **Category**: `visual-engineering`
  - **Skills**: [`frontend-ui-ux`]

  **Acceptance Criteria**:
  - [ ] 点击记录查看详情
  - [ ] 详情中可操作

  **QA Scenarios**:
  ```
  Scenario: 查看帖子详情并审核
    Tool: Playwright
    Steps:
      1. Click on post row
      2. Side panel opens
      3. Click approve in panel
    Expected: Post approved, panel closes
    Evidence: .sisyphus/evidence/task-3-4-detail-panel.png
  ```

  **Commit**: YES

- [x] **3.5 快捷操作按钮（键盘支持）**

  **What to do**:
  添加快捷键支持
  - Y: 通过
  - N: 拒绝
  - J/K: 上下移动

  **Recommended Agent Profile**:
  - **Category**: `quick`
  - **Skills**: []

  **Acceptance Criteria**:
  - [ ] 键盘快捷键可用
  - [ ] 快捷键提示可见

  **Commit**: YES

- [x] **3.6 高级筛选组件**

  **What to do**:
  增强 `AuditRecords.vue` 的筛选功能
  - 时间范围选择
  - 审核人筛选（如适用）
  - 组合筛选

  **Recommended Agent Profile**:
  - **Category**: `visual-engineering`
  - **Skills**: [`frontend-ui-ux`]

  **Acceptance Criteria**:
  - [ ] 可按时间范围筛选
  - [ ] 可多条件组合

  **Commit**: YES

- [x] **3.7 审核统计优化**

  **What to do**:
  优化看板统计数据
  - 显示今日审核数
  - 显示平均审核时长
  - 显示通过率趋势

  **Acceptance Criteria**:
  - [ ] 统计卡片数据准确
  - [ ] 实时更新

  **Commit**: YES

---

### Wave 4: 数据与运营

- [x] **4.1 实时数据自动刷新**

  **What to do**:
  添加定时刷新机制
  - 每30秒刷新一次看板数据
  - 可手动刷新
  - 刷新按钮旋转动画

  **Acceptance Criteria**:
  - [ ] 数据每30秒自动刷新
  - [ ] 手动刷新按钮可用

  **Commit**: YES

- [ ] **4.2 趋势数据展示（简化图表）**

  **What to do**:
  使用CSS绘制简化趋势图
  - 不引入图表库
  - 纯CSS条形图/折线
  - 显示近7天数据

  **Acceptance Criteria**:
  - [ ] 趋势图表可见
  - [ ] 数据显示正确

  **Commit**: YES

- [ ] **4.3 操作日志列表**

  **What to do**:
  创建操作日志页面（如后端支持）
  - 显示管理员操作记录
  - 时间、操作人、操作内容

  **Acceptance Criteria**:
  - [ ] 可查看操作日志
  - [ ] 支持分页

  **Commit**: YES

- [x] **4.4 导出进度提示**

  **What to do**:
  优化订单导出功能
  - 显示导出进度
  - 大文件分段提示
  - 下载完成通知

  **Acceptance Criteria**:
  - [ ] 导出时显示loading
  - [ ] 完成后toast提示

  **Commit**: YES

- [ ] **4.5 系统配置持久化（后端接口）**

  **What to do**:
  对接后端配置接口
  - 保存配置到后端
  - 应用全局生效

  **Must NOT do**:
  - 如果后端无接口，保留本地存储并标记为"本地草稿"

  **Acceptance Criteria**:
  - [ ] 配置可保存
  - [ ] 配置生效

  **Commit**: YES

- [x] **4.6 数据看板移动端适配**

  **What to do**:
  优化看板在小屏幕显示
  - 统计卡片堆叠
  - 表格横向滚动
  - 图表自适应

  **Acceptance Criteria**:
  - [ ] 移动端显示正常
  - [ ] 布局不错乱

  **Commit**: YES

---

### Wave 5: 体验打磨与验收

- [x] **5.1 移动端响应式全面优化**

  **What to do**:
  全面测试移动端
  - iPhone SE / iPhone 14 / iPad尺寸
  - 触摸区域不小于44px
  - 字体不小于14px

  **Acceptance Criteria**:
  - [ ] 所有页面移动端可用
  - [ ] 无横向滚动（除表格外）

  **Commit**: YES

- [x] **5.2 动画过渡统一**

  **What to do**:
  统一动画时长和缓动函数
  - 创建CSS变量
  - 页面切换动画
  - 列表加载动画

  **Acceptance Criteria**:
  - [ ] 动画流畅
  - [ ] 风格一致

  **Commit**: YES

- [ ] **5.3 键盘快捷键支持**

  **What to do**:
  全局快捷键
  - /: 聚焦搜索
  - ESC: 关闭弹窗
  - ?: 显示快捷键帮助

  **Acceptance Criteria**:
  - [ ] 快捷键可用
  - [ ] 有帮助面板

  **Commit**: YES

- [x] **5.4 代码注释与文档**

  **What to do**:
  添加JSDoc注释
  - API函数注释
  - 工具函数注释
  - 复杂逻辑注释

  **Acceptance Criteria**:
  - [ ] 主要函数有注释
  - [ ] 复杂逻辑有说明

  **Commit**: YES

- [x] **5.5 最终端到端测试**

  **What to do**:
  完整流程测试
  - 商家完整业务流程
  - 管理员完整审核流程
  - 错误边界测试

  **Acceptance Criteria**:
  - [ ] 所有核心流程通过
  - [ ] 无阻塞性bug

  **Commit**: NO (测试不产生代码变更)

---

## Final Verification Wave

- [x] **F1. Plan Compliance Audit** — `oracle`
  对照本计划逐条检查：
  - 所有TODO任务是否完成
  - 文件结构是否符合规范
  - 是否引入禁止的依赖（UI库等）
  - 输出: 合规性报告

- [x] **F2. Code Quality Review** — `unspecified-high`
  代码质量检查：
  - 无console.log残留
  - 无debugger
  - 无硬编码中文（除提示外）
  - CSS变量使用一致
  - 输出: 质量报告

- [x] **F3. Manual QA & User Approval** — `unspecified-high` + `playwright`
  完整端到端测试：
  - 商家端全业务流程
  - 管理端全审核流程
  - 移动端响应式
  - 性能检查
  - 输出: 测试报告 + 用户确认

---

## Commit Strategy

- **Wave 1**: `feat(merchant,admin): foundation - login, routing, error handling`
- **Wave 2**: `feat(merchant): business UX - loading, validation, status mapping`
- **Wave 3**: `feat(admin): audit efficiency - batch operations, reject reasons`
- **Wave 4**: `feat(admin): data & ops - realtime dashboard, config persistence`
- **Wave 5**: `polish(merchant,admin): responsive, animations, docs`
- **Final**: `chore(merchant,admin): final cleanup and verification`

---

## Success Criteria

### Verification Commands

```bash
# 构建检查
cd frontend-merchant && npm run build
cd frontend-admin && npm run build

# 无构建错误，无警告
```

### Final Checklist

- [ ] 商家端可独立登录使用
- [ ] 管理端可独立登录使用
- [ ] 所有列表有加载和空状态
- [ ] 表单有验证和错误提示
- [ ] 订单状态显示中文
- [ ] 支持批量审核
- [ ] 拒绝原因可输入
- [ ] 数据看板有实时刷新
- [ ] 移动端可用
- [ ] 无console.log残留
- [ ] 代码有注释
- [ ] 用户验收通过



## 2025-04-04: AdminHome.vue 批量审核功能

### 实现内容
为管理端 AdminHome.vue 的三个审核区域添加了批量审核功能：

1. **商户审核** - 支持批量选择、批量通过、批量拒绝
2. **店铺审核** - 支持批量选择、批量通过、批量拒绝  
3. **内容审核** - 支持批量选择、批量通过、批量拒绝

### 添加的功能组件

#### 1. 批量选择复选框
- 每行列表项前添加复选框
- 使用 `v-model` 绑定到各自的选中ID数组
- 复选框样式使用 `accent-color` 匹配主题色

#### 2. 批量操作按钮栏
- 条件渲染：仅当选择项数 > 0 时显示
- 显示已选择数量
- 两个按钮：批量通过（cta样式）、批量拒绝（ghost-btn样式）
- 带动画效果 `slideDown`

#### 3. 拒绝原因弹窗
- 模态框设计（参考 AuditRecords.vue 的样式）
- 包含：标题、关闭按钮、原因输入textarea、取消/确认按钮
- 半透明遮罩 + 模糊背景效果
- 弹窗动画：`fadeIn` + `slideUp`

### 状态管理

```javascript
// 选中项ID数组
const selectedMerchantIds = ref([]);
const selectedShopIds = ref([]);
const selectedPostIds = ref([]);

// 弹窗状态
const showRejectModal = ref(false);
const rejectReason = ref("");
const rejectTargetType = ref(""); // 'merchant', 'shop', 'post'
```

### 批量操作方法

1. **batchApproveMerchants** - 遍历选中商户ID，依次调用 approveMerchant
2. **batchApproveShops** - 遍历选中店铺ID，依次调用 approveShop
3. **batchApprovePosts** - 遍历选中帖子ID，依次调用 approvePost
4. **confirmBatchReject** - 根据目标类型，遍历选中ID并调用对应的 reject 方法

### 样式规范

- 遵循现有 CSS 变量系统
- 批量操作栏背景使用 `bg-secondary`
- 复选框使用 `accent-color: var(--brand-primary)`
- 模态框样式与 AuditRecords.vue 保持一致
- 添加动画关键帧：`fadeIn`, `slideUp`, `slideDown`

### 兼容性

- 未改变原有单条审核逻辑
- 未引入外部UI库
- 未使用 alert() 
- 所有操作完成后清空选中列表并刷新数据

---

## 2026-04-04: Code Quality Review Results

### Summary
- Files Reviewed: 18
- Issues Found: 10
- LSP Errors: 0
- **Verdict: PASS with Minor Issues**

### Anti-patterns Found
1. **console.error**: 7 occurrences
   - Login.vue (merchant): 1
   - Login.vue (admin): 1
   - AuditRecords.vue: 4
   - audit.js: 1

2. **alert()**: 1 occurrence
   - AuditRecords.vue:398

3. **Hardcoded URLs**: 2 occurrences
   - frontend-admin/src/api/client.js:4
   - frontend-merchant/src/api/client.js:4

### Structure Review
- Naming Conventions: PASS
- CSS Variables: PASS
- Error Handling: PASS
- Component Architecture: PASS

### Positive Highlights
1. Consistent `<script setup>` Composition API usage
2. Excellent CSS Custom Properties adoption
3. Comprehensive error handling
4. Responsive design implemented
5. Proper localStorage key conventions (`dp_` prefix)

### Recommendations
1. Replace hardcoded API URLs with environment variables
2. Replace alert() with toast notifications
3. Remove console.error statements for production

### LSP Diagnostics
All files passed diagnostics with 0 errors and 0 warnings.

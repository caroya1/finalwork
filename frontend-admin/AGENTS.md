# FRONTEND-ADMIN KNOWLEDGE BASE

**Location**: `frontend-admin/`
**Parent**: Root
**Type**: Vue3 + Vite SPA
**Port**: 5175

---

## OVERVIEW

管理端前端 - 平台管理员后台系统。

---

## STRUCTURE

```
frontend-admin/
├── src/
│   ├── main.js               # 应用入口
│   ├── App.vue               # 根组件
│   ├── router/
│   │   └── index.js          # 路由配置
│   ├── api/                  # API模块
│   │   ├── auth.js
│   │   ├── user.js
│   │   ├── merchant.js
│   │   ├── shop.js
│   │   └── index.js
│   └── views/                # 页面组件
│       ├── Login.vue
│       ├── Dashboard.vue
│       ├── UserManage.vue
│       ├── MerchantAudit.vue
│       ├── ShopAudit.vue
│       └── Statistics.vue
├── index.html
├── package.json
└── vite.config.js            # fs.allow: [".."] 配置
```

---

## CONVENTIONS

与 frontend-user 相同：
- Composition API with `<script setup>`
- CSS Custom Properties
- Storage keys with `dp_` prefix
- `dp_admin_token` - 管理员JWT

---

## COMMANDS

```bash
npm install
npm run dev      # Port 5175
npm run build
npm run preview
```

---

## WHERE TO LOOK

| Task | Location | Notes |
|------|----------|-------|
| 用户管理 | `src/views/UserManage.vue` | 用户列表/封禁 |
| 商户审核 | `src/views/MerchantAudit.vue` | 入驻审核 |
| 店铺审核 | `src/views/ShopAudit.vue` | 店铺审核 |
| 数据统计 | `src/views/Statistics.vue` | 平台数据 |

---

## NOTES

- **最高权限**: `ROLE_ADMIN`
- **审核流程**: 商户入驻、店铺创建需管理员审核
- **Vite特殊配置**: `fs.allow: [".."]` 允许访问父目录

# FRONTEND-MERCHANT KNOWLEDGE BASE

**Location**: `frontend-merchant/`
**Parent**: Root
**Type**: Vue3 + Vite SPA
**Port**: 5174

---

## OVERVIEW

商户端前端 - 面向商户的店铺管理和订单处理平台。

---

## STRUCTURE

```
frontend-merchant/
├── src/
│   ├── main.js               # 应用入口
│   ├── App.vue               # 根组件
│   ├── router/
│   │   └── index.js          # 路由配置
│   ├── api/                  # API模块
│   │   ├── auth.js
│   │   ├── merchant.js
│   │   ├── shop.js
│   │   ├── dish.js
│   │   ├── coupon.js
│   │   ├── order.js
│   │   └── index.js          # axios实例
│   └── views/                # 页面组件
│       ├── Login.vue
│       ├── Dashboard.vue
│       ├── ShopManage.vue
│       ├── DishManage.vue
│       ├── CouponManage.vue
│       └── OrderManage.vue
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
- `dp_merchant_token` - 商户JWT

---

## COMMANDS

```bash
npm install
npm run dev      # Port 5174
npm run build
npm run preview
```

---

## WHERE TO LOOK

| Task | Location | Notes |
|------|----------|-------|
| 商户API | `src/api/merchant.js` | 商户相关接口 |
| 店铺管理 | `src/views/ShopManage.vue` | 店铺信息维护 |
| 订单处理 | `src/views/OrderManage.vue` | 接单/核销 |
| 优惠券管理 | `src/views/CouponManage.vue` | 发布优惠券 |

---

## NOTES

- **独立认证**: 商户登录与用户系统分离
- **角色**: `ROLE_MERCHANT`
- **Vite特殊配置**: `fs.allow: [".."]` 允许访问父目录

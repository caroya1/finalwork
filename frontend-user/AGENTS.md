# FRONTEND-USER KNOWLEDGE BASE

**Location**: `frontend-user/`
**Parent**: Root
**Type**: Vue3 + Vite SPA
**Port**: 5173

---

## OVERVIEW

用户端前端 - 面向普通消费者的本地生活服务平台。

---

## STRUCTURE

```
frontend-user/
├── src/
│   ├── main.js               # 应用入口
│   ├── App.vue               # 根组件
│   ├── router/
│   │   └── index.js          # 路由配置
│   ├── api/                  # API模块
│   │   ├── auth.js
│   │   ├── user.js
│   │   ├── shop.js
│   │   ├── coupon.js
│   │   ├── order.js
│   │   ├── post.js
│   │   ├── ai.js
│   │   └── index.js          # axios实例
│   ├── views/                # 页面组件
│   │   ├── Home.vue
│   │   ├── ShopDetail.vue
│   │   ├── Coupon.vue
│   │   ├── Post.vue
│   │   └── User.vue
│   ├── components/           # 可复用组件
│   │   └── SmartRecommendDialog.vue  # AI助手
│   └── style.css             # 全局样式
├── index.html
├── package.json
└── vite.config.js
```

---

## CONVENTIONS

### Vue Style
```javascript
// Composition API + script setup
<script setup>
import { ref, onMounted } from 'vue'

const shops = ref([])

onMounted(async () => {
  shops.value = await fetchShops()
})
</script>
```

### API Pattern
```javascript
// api/index.js - axios实例
const api = axios.create({
  baseURL: 'http://localhost:8081',
  timeout: 10000
})

// api/shop.js
export const getShops = (params) => api.get('/api/shops', { params })
```

### Storage Keys
- `dp_token` - JWT token
- `dp_user_id` - 用户ID
- `dp_user_role` - 用户角色

### CSS Variables
```css
:root {
  --brand-primary: #ff6633;
  --text-primary: #333;
  --space-4: 16px;
}
```

---

## COMMANDS

```bash
npm install
npm run dev      # Port 5173
npm run build    # 生产构建
npm run preview  # 预览生产构建
```

---

## WHERE TO LOOK

| Task | Location | Notes |
|------|----------|-------|
| 路由配置 | `src/router/index.js` | Vue Router 4 |
| API调用 | `src/api/*.js` | 按模块组织 |
| AI助手 | `src/components/SmartRecommendDialog.vue` | 智能推荐对话框 |
| 全局样式 | `src/style.css` | CSS Variables |
| Axios配置 | `src/api/index.js` | 拦截器、baseURL |

---

## NOTES

- **纯JavaScript**: 无TypeScript
- **无UI框架**: 纯自定义CSS，无Element Plus/Ant Design
- **API网关**: 所有请求通过 `localhost:8081`
- **AI功能**: 右下角悬浮按钮调用 `/api/ai/chat`

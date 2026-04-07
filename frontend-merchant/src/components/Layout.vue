<template>
  <div class="layout">
    <aside class="sidebar">
      <div class="sidebar-header">
        <h2>商户后台</h2>
        <p>{{ merchantName }}</p>
      </div>
      <nav class="sidebar-nav">
        <RouterLink to="/" class="nav-item" exact>
          <span class="nav-icon">📊</span>
          <span>工作台</span>
        </RouterLink>
        <RouterLink to="/shops" class="nav-item">
          <span class="nav-icon">🏪</span>
          <span>门店管理</span>
        </RouterLink>
        <RouterLink to="/dishes" class="nav-item">
          <span class="nav-icon">🍽️</span>
          <span>菜品管理</span>
        </RouterLink>
        <RouterLink to="/coupons" class="nav-item">
          <span class="nav-icon">🎫</span>
          <span>优惠券</span>
        </RouterLink>
        <RouterLink to="/orders" class="nav-item">
          <span class="nav-icon">📋</span>
          <span>订单管理</span>
        </RouterLink>
      </nav>
      <div class="sidebar-footer">
        <button class="logout-btn" @click="logout">退出登录</button>
      </div>
    </aside>
    <main class="main-content">
      <RouterView />
    </main>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import { useRouter, RouterView } from 'vue-router';

const router = useRouter();
const merchantName = ref(localStorage.getItem('dp_merchant_name') || '商户');

const logout = () => {
  localStorage.removeItem('dp_token');
  localStorage.removeItem('dp_merchant_id');
  localStorage.removeItem('dp_merchant_name');
  router.push('/login');
};
</script>

<style scoped>
.layout {
  display: flex;
  min-height: 100vh;
}

.sidebar {
  width: 240px;
  background: #1a1a2e;
  color: #fff;
  display: flex;
  flex-direction: column;
  position: fixed;
  height: 100vh;
}

.sidebar-header {
  padding: 24px;
  border-bottom: 1px solid rgba(255,255,255,0.1);
}

.sidebar-header h2 {
  margin: 0 0 8px 0;
  font-size: 1.2rem;
}

.sidebar-header p {
  margin: 0;
  font-size: 0.85rem;
  opacity: 0.7;
}

.sidebar-nav {
  flex: 1;
  padding: 16px 0;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 24px;
  color: rgba(255,255,255,0.8);
  text-decoration: none;
  transition: all 0.2s;
}

.nav-item:hover {
  background: rgba(255,255,255,0.05);
  color: #fff;
}

.nav-item.router-link-active {
  background: rgba(255,107,53,0.2);
  color: #ff6b35;
  border-right: 3px solid #ff6b35;
}

.nav-icon {
  font-size: 1.2rem;
}

.sidebar-footer {
  padding: 16px 24px;
  border-top: 1px solid rgba(255,255,255,0.1);
}

.logout-btn {
  width: 100%;
  padding: 10px;
  background: rgba(255,255,255,0.1);
  border: none;
  border-radius: 8px;
  color: #fff;
  cursor: pointer;
  transition: background 0.2s;
}

.logout-btn:hover {
  background: rgba(255,107,53,0.3);
}

.main-content {
  flex: 1;
  margin-left: 240px;
  padding: 24px;
  background: #f5f5f5;
  min-height: 100vh;
}
</style>

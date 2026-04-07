<template>
  <div class="layout">
    <aside class="sidebar">
      <div class="sidebar-header">
        <div class="brand-mark">管</div>
        <div class="brand-info">
          <h2>管理后台</h2>
          <p>{{ adminName }}</p>
        </div>
      </div>
      
      <nav class="sidebar-nav">
        <RouterLink to="/" class="nav-item" exact>
          <span class="nav-icon">📊</span>
          <span>运营看板</span>
        </RouterLink>
        <RouterLink to="/merchants" class="nav-item">
          <span class="nav-icon">🏪</span>
          <span>商户审核</span>
        </RouterLink>
        <RouterLink to="/shops" class="nav-item">
          <span class="nav-icon">🏢</span>
          <span>店铺审核</span>
        </RouterLink>
        <RouterLink to="/posts" class="nav-item">
          <span class="nav-icon">📝</span>
          <span>内容审核</span>
        </RouterLink>
        <RouterLink to="/orders" class="nav-item">
          <span class="nav-icon">📋</span>
          <span>订单管理</span>
        </RouterLink>
        <RouterLink to="/stats" class="nav-item">
          <span class="nav-icon">📈</span>
          <span>数据统计</span>
        </RouterLink>
      </nav>
      
      <div class="sidebar-footer">
        <button class="logout-btn" @click="logoutHandler">
          <span class="logout-icon">🚪</span>
          <span>退出登录</span>
        </button>
      </div>
    </aside>
    
    <main class="main-content">
      <RouterView />
    </main>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRouter, RouterLink, RouterView } from 'vue-router';
import { logout } from '../api/auth';

const router = useRouter();
const adminName = ref('管理员');

onMounted(() => {
  adminName.value = localStorage.getItem('dp_username') || '管理员';
});

const logoutHandler = async () => {
  const refreshToken = localStorage.getItem('dp_refresh_token');
  const accessToken = localStorage.getItem('dp_token');
  
  if (refreshToken || accessToken) {
    try {
      await logout(refreshToken || null, accessToken || null);
    } catch {
      // ignore
    }
  }
  
  // 清除所有本地存储
  localStorage.removeItem('dp_token');
  localStorage.removeItem('dp_user_id');
  localStorage.removeItem('dp_refresh_token');
  localStorage.removeItem('dp_username');
  localStorage.removeItem('dp_role');
  localStorage.removeItem('dp_city');
  localStorage.removeItem('dp_balance');
  
  // 跳转到登录页
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
  z-index: 100;
}

.sidebar-header {
  padding: 24px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
  display: flex;
  align-items: center;
  gap: 12px;
}

.brand-mark {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  background: linear-gradient(135deg, #ff6b35 0%, #ff8c42 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.1rem;
  font-weight: 600;
  color: #fff;
}

.brand-info h2 {
  margin: 0 0 2px 0;
  font-size: 1.1rem;
  font-weight: 600;
}

.brand-info p {
  margin: 0;
  font-size: 0.8rem;
  opacity: 0.6;
}

.sidebar-nav {
  flex: 1;
  padding: 16px 0;
  overflow-y: auto;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 24px;
  color: rgba(255, 255, 255, 0.75);
  text-decoration: none;
  transition: all 0.2s ease;
  margin: 0 12px;
  border-radius: 10px;
}

.nav-item:hover {
  background: rgba(255, 255, 255, 0.08);
  color: #fff;
}

.nav-item.router-link-active {
  background: rgba(255, 107, 53, 0.2);
  color: #ff6b35;
  border-left: 3px solid #ff6b35;
  margin-left: 9px;
}

.nav-icon {
  font-size: 1.25rem;
  width: 24px;
  text-align: center;
}

.sidebar-footer {
  padding: 16px 24px;
  border-top: 1px solid rgba(255, 255, 255, 0.1);
}

.logout-btn {
  width: 100%;
  padding: 12px;
  background: rgba(255, 255, 255, 0.08);
  border: none;
  border-radius: 10px;
  color: rgba(255, 255, 255, 0.8);
  cursor: pointer;
  transition: all 0.2s ease;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  font-size: 0.9rem;
}

.logout-btn:hover {
  background: rgba(255, 107, 53, 0.25);
  color: #ff6b35;
}

.logout-icon {
  font-size: 1rem;
}

.main-content {
  flex: 1;
  margin-left: 240px;
  padding: 24px;
  background: #f5f7fa;
  min-height: 100vh;
}

/* 响应式适配 */
@media (max-width: 768px) {
  .sidebar {
    width: 64px;
  }
  
  .brand-info,
  .nav-item span,
  .logout-btn span {
    display: none;
  }
  
  .sidebar-header {
    padding: 16px;
    justify-content: center;
  }
  
  .nav-item {
    justify-content: center;
    padding: 14px;
    margin: 0 8px;
  }
  
  .nav-item.router-link-active {
    margin-left: 5px;
  }
  
  .main-content {
    margin-left: 64px;
    padding: 16px;
  }
  
  .sidebar-footer {
    padding: 16px;
  }
}
</style>

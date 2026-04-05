<template>
  <aside class="side-nav">
    <div class="side-nav-header">
      <span class="brand-mark">DP</span>
      <span class="brand-text">管理后台</span>
    </div>
    
    <nav class="side-nav-menu">
      <RouterLink 
        to="/" 
        class="nav-item"
        :class="{ active: $route.path === '/' }"
      >
        <svg class="nav-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <rect x="3" y="3" width="7" height="7"></rect>
          <rect x="14" y="3" width="7" height="7"></rect>
          <rect x="14" y="14" width="7" height="7"></rect>
          <rect x="3" y="14" width="7" height="7"></rect>
        </svg>
        <span>看板</span>
      </RouterLink>
      
      <RouterLink 
        to="/audit-records" 
        class="nav-item"
        :class="{ active: $route.path === '/audit-records' }"
      >
        <svg class="nav-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"></path>
          <polyline points="14 2 14 8 20 8"></polyline>
          <line x1="16" y1="13" x2="8" y2="13"></line>
          <line x1="16" y1="17" x2="8" y2="17"></line>
          <polyline points="10 9 9 9 8 9"></polyline>
        </svg>
        <span>审核记录</span>
      </RouterLink>
    </nav>
    
    <div class="side-nav-footer">
      <div class="admin-info">
        <div class="admin-avatar">
          {{ adminName.charAt(0).toUpperCase() }}
        </div>
        <span class="admin-name">{{ adminName }}</span>
      </div>
      <button class="logout-btn" @click="handleLogout">
        <svg class="logout-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"></path>
          <polyline points="16 17 21 12 16 7"></polyline>
          <line x1="21" y1="12" x2="9" y2="12"></line>
        </svg>
        退出
      </button>
    </div>
  </aside>
</template>

<script setup>
import { ref, onMounted } from "vue";
import { useRouter, RouterLink } from "vue-router";
import { logout } from "../api/auth";

const router = useRouter();
const adminName = ref("");

onMounted(() => {
  adminName.value = localStorage.getItem("dp_username") || "管理员";
});

const handleLogout = async () => {
  const refreshToken = localStorage.getItem("dp_refresh_token");
  const accessToken = localStorage.getItem("dp_token");
  
  if (refreshToken || accessToken) {
    try {
      await logout(refreshToken || null, accessToken || null);
    } catch {
      // ignore
    }
  }
  
  // 清除所有本地存储
  localStorage.removeItem("dp_token");
  localStorage.removeItem("dp_user_id");
  localStorage.removeItem("dp_refresh_token");
  localStorage.removeItem("dp_username");
  localStorage.removeItem("dp_role");
  localStorage.removeItem("dp_city");
  localStorage.removeItem("dp_balance");
  
  // 跳转到登录页
  router.push("/login");
};
</script>

<style scoped>
.side-nav {
  position: fixed;
  top: 0;
  left: 0;
  bottom: 0;
  width: var(--sidebar-width);
  display: flex;
  flex-direction: column;
  background: var(--bg-sidebar);
  z-index: var(--z-sticky);
}

.side-nav-header {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  padding: var(--space-6) var(--space-5);
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.brand-mark {
  width: 36px;
  height: 36px;
  border-radius: var(--radius-lg);
  display: grid;
  place-items: center;
  background: var(--brand-gradient);
  color: var(--text-inverse);
  font-weight: var(--font-bold);
  font-size: var(--text-sm);
  letter-spacing: 0.5px;
  box-shadow: var(--shadow-brand);
}

.brand-text {
  font-size: var(--text-lg);
  font-weight: var(--font-semibold);
  color: var(--text-inverse);
  letter-spacing: -0.02em;
}

.side-nav-menu {
  flex: 1;
  padding: var(--space-4);
  display: flex;
  flex-direction: column;
  gap: var(--space-2);
}

.nav-item {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  padding: var(--space-3) var(--space-4);
  border-radius: var(--radius-lg);
  color: rgba(255, 255, 255, 0.7);
  font-size: var(--text-sm);
  font-weight: var(--font-medium);
  transition: all var(--transition-fast);
  text-decoration: none;
}

.nav-item:hover {
  background: rgba(255, 255, 255, 0.1);
  color: var(--text-inverse);
}

.nav-item.active {
  background: var(--brand-primary);
  color: var(--text-inverse);
  box-shadow: var(--shadow-brand);
}

.nav-icon {
  width: 18px;
  height: 18px;
  flex-shrink: 0;
}

.side-nav-footer {
  padding: var(--space-4);
  border-top: 1px solid rgba(255, 255, 255, 0.1);
  display: flex;
  flex-direction: column;
  gap: var(--space-4);
}

.admin-info {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  padding: var(--space-3);
  background: rgba(255, 255, 255, 0.05);
  border-radius: var(--radius-lg);
}

.admin-avatar {
  width: 32px;
  height: 32px;
  border-radius: var(--radius-full);
  background: var(--brand-gradient);
  display: grid;
  place-items: center;
  color: var(--text-inverse);
  font-size: var(--text-sm);
  font-weight: var(--font-semibold);
}

.admin-name {
  font-size: var(--text-sm);
  font-weight: var(--font-medium);
  color: var(--text-inverse);
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.logout-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: var(--space-2);
  padding: var(--space-3) var(--space-4);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: var(--radius-lg);
  background: transparent;
  color: rgba(255, 255, 255, 0.8);
  font-size: var(--text-sm);
  font-weight: var(--font-medium);
  transition: all var(--transition-fast);
  cursor: pointer;
  width: 100%;
}

.logout-btn:hover {
  background: var(--color-danger-light);
  border-color: var(--color-danger);
  color: var(--color-danger);
}

.logout-icon {
  width: 16px;
  height: 16px;
}

@media (max-width: 768px) {
  .side-nav {
    width: 64px;
  }
  
  .brand-text,
  .nav-item span,
  .admin-name {
    display: none;
  }
  
  .side-nav-header,
  .side-nav-menu,
  .side-nav-footer {
    padding: var(--space-3);
    align-items: center;
  }
  
  .nav-item {
    justify-content: center;
    padding: var(--space-3);
  }
  
  .admin-info {
    justify-content: center;
    padding: var(--space-2);
  }
  
  .logout-btn span {
    display: none;
  }
}
</style>

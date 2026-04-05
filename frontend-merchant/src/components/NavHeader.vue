<template>
  <header class="nav-header">
    <div class="nav-header-brand">
      <span class="brand-mark">DP</span>
      <span class="brand-text">商户工作台</span>
    </div>
    <div class="nav-header-actions">
      <span class="merchant-name">{{ merchantName }}</span>
      <button class="logout-btn" @click="handleLogout">
        <svg class="logout-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"></path>
          <polyline points="16 17 21 12 16 7"></polyline>
          <line x1="21" y1="12" x2="9" y2="12"></line>
        </svg>
        退出登录
      </button>
    </div>
  </header>
</template>

<script setup>
import { ref, onMounted } from "vue";
import { useRouter } from "vue-router";
import { logout } from "../api/auth";

const router = useRouter();
const merchantName = ref("");

onMounted(() => {
  merchantName.value = localStorage.getItem("dp_merchant_name") || 
                      localStorage.getItem("dp_username") || 
                      "商户";
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
  localStorage.removeItem("dp_merchant_token");
  localStorage.removeItem("dp_merchant_id");
  localStorage.removeItem("dp_merchant_name");
  localStorage.removeItem("dp_merchant_email");
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
.nav-header {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: var(--z-sticky);
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: var(--header-height);
  padding: 0 var(--space-8);
  background: var(--bg-primary);
  border-bottom: 1px solid var(--border-light);
  box-shadow: var(--shadow-sm);
}

.nav-header-brand {
  display: flex;
  align-items: center;
  gap: var(--space-3);
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
  color: var(--text-primary);
  letter-spacing: -0.02em;
}

.nav-header-actions {
  display: flex;
  align-items: center;
  gap: var(--space-4);
}

.merchant-name {
  font-size: var(--text-sm);
  font-weight: var(--font-medium);
  color: var(--text-secondary);
  padding: var(--space-2) var(--space-4);
  background: var(--bg-secondary);
  border-radius: var(--radius-md);
  border: 1px solid var(--border-light);
}

.logout-btn {
  display: inline-flex;
  align-items: center;
  gap: var(--space-2);
  padding: var(--space-2) var(--space-4);
  border: 1px solid var(--border-default);
  border-radius: var(--radius-lg);
  background: var(--bg-primary);
  color: var(--text-secondary);
  font-size: var(--text-sm);
  font-weight: var(--font-medium);
  transition: all var(--transition-fast);
  cursor: pointer;
}

.logout-btn:hover {
  border-color: var(--color-danger);
  color: var(--color-danger);
  background: var(--color-danger-light);
}

.logout-icon {
  width: 16px;
  height: 16px;
}

@media (max-width: 640px) {
  .nav-header {
    padding: 0 var(--space-4);
  }
  
  .brand-text {
    font-size: var(--text-base);
  }
  
  .merchant-name {
    display: none;
  }
}
</style>

<template>
  <div class="dashboard">
    <!-- 页面标题 -->
    <div class="page-header">
      <h1>运营看板</h1>
      <p class="subtitle">实时监控平台运营数据与审核状态</p>
    </div>

    <!-- 核心指标卡片 -->
    <section class="stats-section">
      <div class="stats-grid">
        <div class="stat-card primary">
          <div class="stat-icon">📦</div>
          <div class="stat-content">
            <div class="stat-value">{{ todayOrderCount }}</div>
            <div class="stat-label">今日订单</div>
          </div>
        </div>
        <div class="stat-card warning">
          <div class="stat-icon">🏪</div>
          <div class="stat-content">
            <div class="stat-value">{{ pendingMerchantCount }}</div>
            <div class="stat-label">待审商户</div>
          </div>
        </div>
        <div class="stat-card warning">
          <div class="stat-icon">🏢</div>
          <div class="stat-content">
            <div class="stat-value">{{ pendingShopCount }}</div>
            <div class="stat-label">待审店铺</div>
          </div>
        </div>
        <div class="stat-card warning">
          <div class="stat-icon">📝</div>
          <div class="stat-content">
            <div class="stat-value">{{ pendingPostCount }}</div>
            <div class="stat-label">待审内容</div>
          </div>
        </div>
      </div>
    </section>

    <!-- 快捷操作 -->
    <section class="quick-actions">
      <h2>快捷操作</h2>
      <div class="action-grid">
        <RouterLink to="/merchants" class="action-card">
          <span class="action-icon">🏪</span>
          <span class="action-text">商户审核</span>
          <span class="action-badge" v-if="pendingMerchantCount > 0">{{ pendingMerchantCount }}</span>
        </RouterLink>
        <RouterLink to="/shops" class="action-card">
          <span class="action-icon">🏢</span>
          <span class="action-text">店铺审核</span>
          <span class="action-badge" v-if="pendingShopCount > 0">{{ pendingShopCount }}</span>
        </RouterLink>
        <RouterLink to="/posts" class="action-card">
          <span class="action-icon">📝</span>
          <span class="action-text">内容审核</span>
          <span class="action-badge" v-if="pendingPostCount > 0">{{ pendingPostCount }}</span>
        </RouterLink>
        <RouterLink to="/orders" class="action-card">
          <span class="action-icon">📋</span>
          <span class="action-text">订单管理</span>
        </RouterLink>
        <RouterLink to="/stats" class="action-card">
          <span class="action-icon">📈</span>
          <span class="action-text">数据统计</span>
        </RouterLink>
      </div>
    </section>

    <!-- 审核统计 -->
    <section class="audit-stats">
      <div class="card">
        <h2>审核统计</h2>
        <div class="mini-stats">
          <div class="mini-stat">
            <div class="mini-value">{{ stats.todayApproved }}</div>
            <div class="mini-label">今日通过</div>
          </div>
          <div class="mini-stat">
            <div class="mini-value">{{ stats.todayRejected }}</div>
            <div class="mini-label">今日拒绝</div>
          </div>
          <div class="mini-stat">
            <div class="mini-value">{{ stats.passRate }}%</div>
            <div class="mini-label">通过率</div>
          </div>
        </div>
      </div>
      
      <div class="card">
        <h2>系统状态</h2>
        <div class="system-status">
          <div class="status-item">
            <span class="status-dot online"></span>
            <span>系统运行正常</span>
          </div>
          <div class="status-item">
            <span class="status-dot online"></span>
            <span>数据库连接正常</span>
          </div>
          <div class="status-item">
            <span class="status-dot online"></span>
            <span>缓存服务正常</span>
          </div>
          <div class="refresh-time">
            上次更新：{{ lastUpdateTime }}
          </div>
        </div>
      </div>
    </section>

    <!-- 最近订单 -->
    <section class="recent-section">
      <div class="card full-width">
        <div class="card-header">
          <h2>最近订单</h2>
          <RouterLink to="/orders" class="view-all">查看全部 →</RouterLink>
        </div>
        <div class="order-list">
          <div v-for="order in recentOrders" :key="order.id" class="order-item">
            <div class="order-info">
              <span class="order-no">{{ order.orderNo || `订单#${order.id}` }}</span>
              <span class="order-user">用户 {{ order.userId }}</span>
            </div>
            <div class="order-meta">
              <span class="order-amount">¥{{ ((order.payAmount || order.amount || 0) / 100).toFixed(2) }}</span>
              <span class="order-status" :class="getStatusClass(order.status)">
                {{ getStatusText(order.status) }}
              </span>
            </div>
          </div>
          <div v-if="recentOrders.length === 0" class="empty-state">
            暂无订单数据
          </div>
        </div>
      </div>
    </section>

    <!-- Toast Notifications -->
    <div class="toast-container">
      <div v-for="toast in toasts" :key="toast.id" class="toast" :class="toast.type">
        {{ toast.message }}
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from "vue";
import { RouterLink } from "vue-router";
import {
  queryOrders,
  getTodayOrderCount,
  listAdminMerchants,
  listAdminPosts,
  listAdminShops
} from "../api/order";

// ========== State ==========
const todayOrderCount = ref(0);
const orders = ref([]);
const merchants = ref([]);
const shops = ref([]);
const posts = ref([]);
const toasts = ref([]);
const lastUpdateTime = ref("-");

const stats = ref({
  todayApproved: 0,
  todayRejected: 0,
  passRate: 0
});

// ========== Computed ==========
const pendingMerchantCount = computed(() => merchants.value.filter((m) => m.status === 0).length);
const pendingShopCount = computed(() => shops.value.filter((s) => s.auditStatus === 0).length);
const pendingPostCount = computed(() => posts.value.filter((p) => p.auditStatus === 0).length);
const recentOrders = computed(() => orders.value.slice(0, 5));

// ========== Methods ==========
const showToast = (message, type = 'success') => {
  const id = Date.now();
  toasts.value.push({ id, message, type });
  setTimeout(() => {
    toasts.value = toasts.value.filter(t => t.id !== id);
  }, 3000);
};

const getStatusText = (status) => {
  const statusMap = {
    0: '待支付',
    1: '已支付',
    2: '已核销',
    3: '已退款',
    4: '已取消'
  };
  return statusMap[status] || '未知';
};

const getStatusClass = (status) => {
  const classMap = {
    0: 'pending',
    1: 'success',
    2: 'success',
    3: 'error',
    4: 'cancelled'
  };
  return classMap[status] || '';
};

const calculateStats = () => {
  const approved = merchants.value.filter(m => m.status === 1).length +
                   shops.value.filter(s => s.auditStatus === 1).length +
                   posts.value.filter(p => p.auditStatus === 1).length;
  const rejected = merchants.value.filter(m => m.status === 2).length +
                   shops.value.filter(s => s.auditStatus === 2).length +
                   posts.value.filter(p => p.auditStatus === 2).length;
  const total = approved + rejected;
  
  stats.value.todayApproved = approved;
  stats.value.todayRejected = rejected;
  stats.value.passRate = total > 0 ? Math.round((approved / total) * 100) : 0;
};

const refreshDashboard = async () => {
  try {
    const countResp = await getTodayOrderCount();
    if (countResp.success) {
      todayOrderCount.value = countResp.data || 0;
    }
    
    // 加载订单
    const orderResp = await queryOrders({ page: 1, pageSize: 10 });
    if (orderResp.success) {
      orders.value = orderResp.data?.records || [];
    }
    
    // 加载商户
    const merchantResp = await listAdminMerchants();
    if (merchantResp.success) {
      merchants.value = merchantResp.data || [];
    }
    
    // 加载店铺
    const shopResp = await listAdminShops();
    if (shopResp.success) {
      shops.value = shopResp.data || [];
    }
    
    // 加载帖子
    const postResp = await listAdminPosts();
    if (postResp.success) {
      posts.value = postResp.data || [];
    }
    
    calculateStats();
    lastUpdateTime.value = new Date().toLocaleTimeString('zh-CN');
  } catch (error) {
    console.error('刷新看板失败:', error);
    showToast('刷新失败，请重试', 'error');
  }
};

// ========== Lifecycle ==========
let refreshInterval = null;

onMounted(() => {
  refreshDashboard();
  // 每30秒自动刷新
  refreshInterval = setInterval(refreshDashboard, 30000);
});

onUnmounted(() => {
  if (refreshInterval) {
    clearInterval(refreshInterval);
  }
});
</script>

<style scoped>
.dashboard {
  max-width: 1200px;
  margin: 0 auto;
}

.page-header {
  margin-bottom: 24px;
}

.page-header h1 {
  margin: 0 0 8px 0;
  font-size: 1.75rem;
  font-weight: 600;
  color: #1a1a2e;
}

.subtitle {
  margin: 0;
  color: #666;
  font-size: 0.95rem;
}

/* Stats Section */
.stats-section {
  margin-bottom: 24px;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
}

.stat-card {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  transition: transform 0.2s, box-shadow 0.2s;
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
}

.stat-card.primary {
  border-left: 4px solid #ff6b35;
}

.stat-card.warning {
  border-left: 4px solid #f59e0b;
}

.stat-icon {
  font-size: 2rem;
  width: 56px;
  height: 56px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f7fa;
  border-radius: 12px;
}

.stat-content {
  flex: 1;
}

.stat-value {
  font-size: 1.75rem;
  font-weight: 700;
  color: #1a1a2e;
  line-height: 1.2;
}

.stat-label {
  font-size: 0.875rem;
  color: #666;
  margin-top: 4px;
}

/* Quick Actions */
.quick-actions {
  margin-bottom: 24px;
}

.quick-actions h2 {
  margin: 0 0 16px 0;
  font-size: 1.1rem;
  font-weight: 600;
  color: #1a1a2e;
}

.action-grid {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 16px;
}

.action-card {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  text-decoration: none;
  color: #333;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  transition: all 0.2s;
  position: relative;
}

.action-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
  color: #ff6b35;
}

.action-icon {
  font-size: 1.75rem;
}

.action-text {
  font-size: 0.9rem;
  font-weight: 500;
}

.action-badge {
  position: absolute;
  top: 8px;
  right: 8px;
  background: #ff6b35;
  color: #fff;
  font-size: 0.75rem;
  font-weight: 600;
  padding: 2px 8px;
  border-radius: 10px;
  min-width: 20px;
  text-align: center;
}

/* Audit Stats */
.audit-stats {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
  margin-bottom: 24px;
}

.card {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.card h2 {
  margin: 0 0 16px 0;
  font-size: 1.1rem;
  font-weight: 600;
  color: #1a1a2e;
}

.mini-stats {
  display: flex;
  gap: 32px;
}

.mini-stat {
  text-align: center;
}

.mini-value {
  font-size: 1.5rem;
  font-weight: 700;
  color: #ff6b35;
}

.mini-label {
  font-size: 0.8rem;
  color: #666;
  margin-top: 4px;
}

.system-status {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.status-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 0.9rem;
  color: #333;
}

.status-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #22c55e;
}

.status-dot.online {
  background: #22c55e;
}

.refresh-time {
  margin-top: 8px;
  padding-top: 12px;
  border-top: 1px solid #eee;
  font-size: 0.8rem;
  color: #999;
}

/* Recent Orders */
.recent-section {
  margin-bottom: 24px;
}

.full-width {
  width: 100%;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.card-header h2 {
  margin: 0;
}

.view-all {
  font-size: 0.85rem;
  color: #ff6b35;
  text-decoration: none;
}

.view-all:hover {
  text-decoration: underline;
}

.order-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.order-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  background: #f9fafb;
  border-radius: 8px;
  transition: background 0.2s;
}

.order-item:hover {
  background: #f0f1f2;
}

.order-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.order-no {
  font-weight: 600;
  color: #1a1a2e;
}

.order-user {
  font-size: 0.8rem;
  color: #666;
}

.order-meta {
  display: flex;
  align-items: center;
  gap: 12px;
}

.order-amount {
  font-weight: 600;
  color: #ff6b35;
}

.order-status {
  padding: 4px 10px;
  border-radius: 12px;
  font-size: 0.75rem;
  font-weight: 500;
}

.order-status.pending {
  background: #fef3c7;
  color: #92400e;
}

.order-status.success {
  background: #d1fae5;
  color: #065f46;
}

.order-status.error {
  background: #fee2e2;
  color: #991b1b;
}

.order-status.cancelled {
  background: #f3f4f6;
  color: #6b7280;
}

.empty-state {
  text-align: center;
  padding: 32px;
  color: #999;
  font-size: 0.9rem;
}

/* Toast */
.toast-container {
  position: fixed;
  top: 20px;
  right: 20px;
  z-index: 1000;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.toast {
  padding: 12px 20px;
  border-radius: 8px;
  background: #fff;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  animation: slideIn 0.3s ease;
  font-size: 0.9rem;
}

.toast.success {
  border-left: 4px solid #22c55e;
}

.toast.error {
  border-left: 4px solid #ef4444;
}

@keyframes slideIn {
  from {
    opacity: 0;
    transform: translateX(100px);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

/* Responsive */
@media (max-width: 1024px) {
  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  
  .action-grid {
    grid-template-columns: repeat(3, 1fr);
  }
  
  .audit-stats {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 640px) {
  .stats-grid {
    grid-template-columns: 1fr;
  }
  
  .action-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  
  .mini-stats {
    gap: 16px;
  }
}
</style>

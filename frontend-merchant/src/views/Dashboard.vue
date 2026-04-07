<template>
  <div class="dashboard">
    <h1 class="page-title">工作台</h1>
    
    <div class="hero-card">
      <h2>商户信息</h2>
      <p class="merchant-info">已登录商户：{{ merchantName }}（ID: {{ merchantId || "-" }}）</p>
      <div class="form-grid">
        <select v-model="activeShopId">
          <option value="">请选择门店</option>
          <option v-for="shop in shopList" :key="shop.id" :value="String(shop.id)">{{ shop.name }}</option>
        </select>
        <button class="cta" @click="loadShopAssets">刷新门店数据</button>
        <span>{{ dashboardMessage }}</span>
      </div>
      <div class="stats-grid" v-if="shopStats">
        <div class="stat-item">
          <div class="stat-label">有效订单</div>
          <div class="stat-value">{{ shopStats.orderCount || 0 }}</div>
        </div>
        <div class="stat-item">
          <div class="stat-label">营业额(元)</div>
          <div class="stat-value">{{ ((shopStats.revenue || 0) / 100).toFixed(2) }}</div>
        </div>
      </div>
    </div>

    <div class="quick-actions">
      <h2>快捷操作</h2>
      <div class="action-grid">
        <RouterLink to="/shops" class="action-card">
          <span class="action-icon">🏪</span>
          <span class="action-title">门店管理</span>
          <span class="action-desc">管理您的门店信息</span>
        </RouterLink>
        <RouterLink to="/dishes" class="action-card">
          <span class="action-icon">🍽️</span>
          <span class="action-title">菜品管理</span>
          <span class="action-desc">添加和管理菜品</span>
        </RouterLink>
        <RouterLink to="/coupons" class="action-card">
          <span class="action-icon">🎫</span>
          <span class="action-title">优惠券</span>
          <span class="action-desc">发布优惠券活动</span>
        </RouterLink>
        <RouterLink to="/orders" class="action-card">
          <span class="action-icon">📋</span>
          <span class="action-title">订单管理</span>
          <span class="action-desc">查看和处理订单</span>
        </RouterLink>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from "vue";
import { listMyShops, getOrderStats } from "../api/shop";

const merchantId = ref(localStorage.getItem("dp_merchant_id") || "");
const merchantName = ref(localStorage.getItem("dp_merchant_name") || localStorage.getItem("dp_username") || "");

const dashboardMessage = ref("");
const shopList = ref([]);
const shopStats = ref(null);
const activeShopId = ref("");

const loadShops = async () => {
  try {
    const token = localStorage.getItem("dp_token");
    const merchantId = localStorage.getItem("dp_merchant_id");
    
    if (!token) {
      dashboardMessage.value = "错误：未找到登录凭证，请重新登录";
      return;
    }
    if (!merchantId) {
      dashboardMessage.value = "错误：未找到商户ID，请重新登录";
      return;
    }
    
    const response = await listMyShops();
    
    if (response.success) {
      shopList.value = response.data || [];
      if (!activeShopId.value && shopList.value.length > 0) {
        activeShopId.value = String(shopList.value[0].id);
      }
    } else {
      dashboardMessage.value = "获取门店列表失败: " + (response.message || "未知错误");
    }
  } catch (error) {
    const errorMsg = error.userMessage || error.message || "网络错误";
    dashboardMessage.value = "获取门店列表失败: " + errorMsg;
  }
};

const loadStats = async () => {
  if (!activeShopId.value) return;
  const response = await getOrderStats(Number(activeShopId.value));
  if (response.success) {
    shopStats.value = response.data;
  }
};

const loadShopAssets = async () => {
  dashboardMessage.value = "";
  if (!activeShopId.value) {
    dashboardMessage.value = "请先选择门店";
    return;
  }
  await loadStats();
  dashboardMessage.value = "已刷新";
};

watch(activeShopId, async () => {
  if (!activeShopId.value) return;
  await loadShopAssets();
});

onMounted(async () => {
  await loadShops();
  if (activeShopId.value) {
    await loadShopAssets();
  }
});
</script>

<style scoped>
.dashboard {
  max-width: 1200px;
}

.page-title {
  font-size: 1.8rem;
  font-weight: 600;
  margin-bottom: 24px;
  color: #1a1a2e;
}

.hero-card {
  background: #fff;
  border-radius: 12px;
  padding: 24px;
  margin-bottom: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.hero-card h2 {
  font-size: 1.2rem;
  margin-bottom: 16px;
  color: #1a1a2e;
}

.merchant-info {
  color: #666;
  margin-bottom: 16px;
}

.form-grid {
  display: flex;
  gap: 12px;
  align-items: center;
  flex-wrap: wrap;
  margin-bottom: 16px;
}

.form-grid select {
  padding: 10px 16px;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  font-size: 14px;
  min-width: 200px;
}

.cta {
  padding: 10px 20px;
  background: #ff6b35;
  color: #fff;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-weight: 500;
  transition: background 0.2s;
}

.cta:hover {
  background: #e55a2b;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
  margin-top: 20px;
}

.stat-item {
  background: linear-gradient(135deg, #f8f9fa 0%, #fff 100%);
  border: 1px solid #e9ecef;
  border-radius: 12px;
  padding: 20px;
  position: relative;
  overflow: hidden;
}

.stat-item::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 4px;
  height: 100%;
  background: linear-gradient(180deg, #ff6b35, #feca57);
  border-radius: 12px 0 0 12px;
}

.stat-label {
  font-size: 12px;
  font-weight: 500;
  color: #999;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  margin-bottom: 8px;
}

.stat-value {
  font-size: 32px;
  font-weight: 700;
  color: #ff6b35;
}

.quick-actions {
  background: #fff;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.quick-actions h2 {
  font-size: 1.2rem;
  margin-bottom: 20px;
  color: #1a1a2e;
}

.action-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 16px;
}

.action-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 24px;
  background: #f8f9fa;
  border-radius: 12px;
  text-decoration: none;
  transition: all 0.2s;
  border: 1px solid transparent;
}

.action-card:hover {
  background: #fff;
  border-color: #ff6b35;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(255, 107, 53, 0.15);
}

.action-icon {
  font-size: 2rem;
  margin-bottom: 12px;
}

.action-title {
  font-size: 16px;
  font-weight: 600;
  color: #1a1a2e;
  margin-bottom: 4px;
}

.action-desc {
  font-size: 13px;
  color: #999;
}
</style>

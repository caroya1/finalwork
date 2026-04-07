<template>
  <div class="order-manage">
    <h1 class="page-title">订单管理</h1>

    <div class="filter-card">
      <div class="filter-row">
        <div class="filter-item">
          <label>选择门店：</label>
          <select v-model="activeShopId" @change="onShopChange">
            <option value="">请选择门店</option>
            <option v-for="shop in shopList" :key="shop.id" :value="String(shop.id)">{{ shop.name }}</option>
          </select>
        </div>
        <div class="filter-item">
          <label>订单状态：</label>
          <select v-model="orderStatusFilter">
            <option value="">全部状态</option>
            <option value="0">待支付</option>
            <option value="1">已支付</option>
            <option value="2">已核销</option>
            <option value="3">已退款</option>
            <option value="4">已取消</option>
          </select>
        </div>
        <button class="cta" @click="loadOrders">查询订单</button>
      </div>
    </div>

    <div v-if="activeShopId" class="list-card">
      <h2>订单列表</h2>
      <div class="order-list">
        <div v-for="order in orderList" :key="order.id" class="order-item">
          <div class="order-info">
            <strong>{{ order.orderNo || `订单#${order.id}` }}</strong>
            <div class="order-tags">
              <span class="tag">用户 {{ order.userId }}</span>
              <span class="tag amount">金额 ¥{{ ((order.payAmount || order.amount || 0) / 100).toFixed(2) }}</span>
              <span class="tag" :class="'status-' + order.status">{{ getStatusText(order.status) }}</span>
            </div>
          </div>
          <button 
            class="verify-btn" 
            v-if="order.status === 1" 
            @click="doVerify(order.id)"
          >
            核销
          </button>
        </div>
        <div v-if="orderList.length === 0" class="empty-state">暂无订单</div>
      </div>
    </div>

    <div v-else class="empty-state-card">
      <p>请先选择一个门店</p>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from "vue";
import { listMyShops, listOrdersByShop, verifyOrder } from "../api/shop";

const shopList = ref([]);
const orderList = ref([]);
const activeShopId = ref("");
const orderStatusFilter = ref("");

const loadShops = async () => {
  try {
    const response = await listMyShops();
    if (response.success) {
      shopList.value = response.data || [];
    }
  } catch (error) {
    console.error("加载门店列表失败:", error);
  }
};

const loadOrders = async () => {
  if (!activeShopId.value) return;
  const status = orderStatusFilter.value === "" ? undefined : Number(orderStatusFilter.value);
  const response = await listOrdersByShop(Number(activeShopId.value), status);
  if (response.success) {
    orderList.value = response.data || [];
  }
};

const onShopChange = async () => {
  if (activeShopId.value) {
    await loadOrders();
  } else {
    orderList.value = [];
  }
};

const getStatusText = (status) => {
  const statusMap = {
    0: "待支付",
    1: "已支付",
    2: "已核销",
    3: "已退款",
    4: "已取消"
  };
  return statusMap[status] || "未知";
};

const doVerify = async (orderId) => {
  const response = await verifyOrder(orderId);
  if (response.success) {
    await loadOrders();
    alert("核销成功");
  } else {
    alert(response.message || "核销失败");
  }
};

onMounted(async () => {
  await loadShops();
});
</script>

<style scoped>
.order-manage {
  max-width: 1200px;
}

.page-title {
  font-size: 1.8rem;
  font-weight: 600;
  margin-bottom: 24px;
  color: #1a1a2e;
}

.filter-card {
  background: #fff;
  border-radius: 12px;
  padding: 20px 24px;
  margin-bottom: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.filter-row {
  display: flex;
  align-items: center;
  gap: 20px;
  flex-wrap: wrap;
}

.filter-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.filter-item label {
  font-weight: 500;
  color: #666;
}

.filter-item select {
  padding: 10px 16px;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  font-size: 14px;
  min-width: 150px;
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

.list-card {
  background: #fff;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.list-card h2 {
  font-size: 1.2rem;
  margin-bottom: 20px;
  color: #1a1a2e;
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
  padding: 16px;
  background: #f8f9fa;
  border-radius: 8px;
  transition: background 0.2s;
}

.order-item:hover {
  background: #f0f0f0;
}

.order-info {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.order-info strong {
  font-size: 16px;
  color: #1a1a2e;
}

.order-tags {
  display: flex;
  gap: 8px;
}

.tag {
  padding: 4px 10px;
  background: #e9ecef;
  border-radius: 4px;
  font-size: 12px;
  color: #666;
}

.tag.amount {
  background: #ff6b35;
  color: #fff;
  font-weight: 500;
}

.tag.status-0 {
  background: #fff3cd;
  color: #856404;
}

.tag.status-1 {
  background: #d4edda;
  color: #155724;
}

.tag.status-2 {
  background: #cce5ff;
  color: #004085;
}

.tag.status-3 {
  background: #f8d7da;
  color: #721c24;
}

.tag.status-4 {
  background: #e2e3e5;
  color: #383d41;
}

.verify-btn {
  padding: 8px 20px;
  background: #28a745;
  color: #fff;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-weight: 500;
  transition: background 0.2s;
}

.verify-btn:hover {
  background: #218838;
}

.empty-state {
  text-align: center;
  padding: 40px;
  color: #999;
}

.empty-state-card {
  background: #fff;
  border-radius: 12px;
  padding: 60px;
  text-align: center;
  color: #999;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}
</style>

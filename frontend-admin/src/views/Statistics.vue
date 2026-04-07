<template>
  <div class="statistics-page">
    <div class="page-header">
      <h1>数据统计</h1>
      <p class="subtitle">平台核心指标与趋势概览</p>
    </div>

    <section class="stats-grid">
      <div class="stat-card">
        <div class="label">总订单数</div>
        <div class="value">{{ totalOrders }}</div>
      </div>
      <div class="stat-card">
        <div class="label">总营收（元）</div>
        <div class="value">{{ totalRevenue }}</div>
      </div>
      <div class="stat-card">
        <div class="label">待审核商户</div>
        <div class="value">{{ pendingMerchants }}</div>
      </div>
      <div class="stat-card">
        <div class="label">待审核店铺</div>
        <div class="value">{{ pendingShops }}</div>
      </div>
    </section>

    <section class="card">
      <h2>说明</h2>
      <p>详细统计能力可在后续迭代中接入图表组件与时间维度筛选。</p>
    </section>
  </div>
</template>

<script setup>
import { ref, onMounted } from "vue";
import { listAdminMerchants, listAdminShops } from "../api/audit";

const totalOrders = ref(0);
const totalRevenue = ref("0.00");
const pendingMerchants = ref(0);
const pendingShops = ref(0);

const load = async () => {
  try {
    const [merchantRes, shopRes] = await Promise.all([listAdminMerchants(), listAdminShops()]);
    if (merchantRes?.success) {
      const merchants = merchantRes.data || [];
      pendingMerchants.value = merchants.filter(m => m.status === "pending" || m.auditStatus === "pending").length;
    }
    if (shopRes?.success) {
      const shops = shopRes.data || [];
      pendingShops.value = shops.filter(s => s.auditStatus === "pending").length;
    }
  } catch {
    // keep defaults
  }
};

onMounted(load);
</script>

<style scoped>
.statistics-page {
  display: grid;
  gap: 20px;
}
.page-header h1 {
  margin: 0;
}
.subtitle {
  margin: 6px 0 0;
  color: #64748b;
}
.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 14px;
}
.stat-card {
  background: #fff;
  border: 1px solid #e2e8f0;
  border-radius: 14px;
  padding: 14px;
}
.label {
  color: #64748b;
  font-size: 12px;
}
.value {
  margin-top: 8px;
  font-size: 24px;
  font-weight: 700;
}
.card {
  background: #fff;
  border: 1px solid #e2e8f0;
  border-radius: 14px;
  padding: 16px;
}
@media (max-width: 900px) {
  .stats-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}
</style>

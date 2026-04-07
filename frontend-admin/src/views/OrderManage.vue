<template>
  <div class="page">
    <div class="page-header">
      <h1>订单管理</h1>
      <p>按条件检索平台订单</p>
    </div>

    <div class="card toolbar">
      <input v-model.number="query.userId" type="number" placeholder="用户ID" />
      <input v-model.number="query.shopId" type="number" placeholder="店铺ID" />
      <input v-model="query.orderNo" placeholder="订单号" />
      <select v-model="query.status">
        <option value="">全部状态</option>
        <option value="0">待支付</option>
        <option value="1">已支付</option>
        <option value="2">已核销</option>
        <option value="3">已退款</option>
        <option value="4">已取消</option>
      </select>
      <button class="btn" @click="load">查询</button>
    </div>

    <div class="card list">
      <div class="row head">
        <span>订单号</span><span>用户</span><span>店铺</span><span>金额(元)</span><span>状态</span>
      </div>
      <div v-for="o in orders" :key="o.id" class="row">
        <span>{{ o.orderNo || `ORDER${o.id}` }}</span>
        <span>{{ o.userId }}</span>
        <span>{{ o.shopId }}</span>
        <span>{{ ((o.payAmount || o.amount || 0) / 100).toFixed(2) }}</span>
        <span>{{ statusText(o.status) }}</span>
      </div>
      <div v-if="orders.length===0" class="empty">暂无订单</div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from "vue";
import { queryOrders } from "../api/order";

const orders = ref([]);
const query = ref({ page: 1, pageSize: 20, userId: undefined, shopId: undefined, orderNo: "", status: "" });

const statusText = (s) => {
  if (s === 0 || s === "0") return "待支付";
  if (s === 1 || s === "1") return "已支付";
  if (s === 2 || s === "2") return "已核销";
  if (s === 3 || s === "3") return "已退款";
  if (s === 4 || s === "4") return "已取消";
  return "未知";
};

const load = async () => {
  const payload = {
    page: query.value.page,
    pageSize: query.value.pageSize,
    userId: query.value.userId || undefined,
    shopId: query.value.shopId || undefined,
    orderNo: query.value.orderNo || undefined,
    status: query.value.status === "" ? undefined : Number(query.value.status)
  };
  const res = await queryOrders(payload);
  if (res?.success) {
    const data = res.data || {};
    orders.value = data.records || data.list || [];
  }
};

onMounted(load);
</script>

<style scoped>
.page { display: grid; gap: 16px; }
.page-header h1 { margin: 0; }
.page-header p { margin: 6px 0 0; color: #64748b; }
.card { background: #fff; border: 1px solid #e2e8f0; border-radius: 12px; padding: 14px; }
.toolbar { display: grid; grid-template-columns: repeat(5, minmax(0, 1fr)) auto; gap: 8px; }
input, select { padding: 8px 10px; border: 1px solid #cbd5e1; border-radius: 8px; }
.btn { padding: 8px 12px; border: 1px solid #cbd5e1; border-radius: 8px; background: #fff; cursor: pointer; }
.list { display: grid; gap: 8px; }
.row { display: grid; grid-template-columns: 2fr 1fr 1fr 1fr 1fr; gap: 8px; padding: 10px; border: 1px solid #edf2f7; border-radius: 8px; }
.row.head { font-weight: 600; background: #f8fafc; }
.empty { color: #64748b; }
@media (max-width: 1000px) {
  .toolbar { grid-template-columns: 1fr 1fr; }
  .row { grid-template-columns: 1fr 1fr; }
}
</style>

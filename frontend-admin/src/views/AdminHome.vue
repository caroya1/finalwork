<template>
  <section class="hero">
    <div class="hero-card">
      <h2>订单总览</h2>
      <p>对接后端 `/api/orders`，需要先登录获取用户ID。</p>
      <div class="form-grid">
        <input v-model="userId" placeholder="用户ID" />
        <button class="cta" @click="loadOrders">查询订单</button>
        <span>{{ orderMessage }}</span>
      </div>
    </div>
    <div class="hero-card">
      <h2>订单列表</h2>
      <div class="list">
        <div v-for="order in orders" :key="order.id" class="list-item">
          <strong>订单 #{{ order.id }}</strong>
          <div>
            <span class="tag">shop {{ order.shopId }}</span>
            <span class="tag">amount {{ order.amount }}</span>
          </div>
        </div>
        <div v-if="orders.length === 0" class="list-item">暂无订单</div>
      </div>
    </div>
  </section>

  <section class="panel-grid">
    <div class="panel">
      <h2>运营提示</h2>
      <p>当前后台仅演示订单查询流程，其他审核与风控模块待接入。</p>
    </div>
  </section>
</template>

<script setup>
import { ref } from "vue";
import { listOrders } from "../api/order";

const userId = ref("");
const orders = ref([]);
const orderMessage = ref("");

const loadOrders = async () => {
  orderMessage.value = "";
  if (!userId.value) {
    orderMessage.value = "请输入用户ID";
    return;
  }
  const response = await listOrders(userId.value);
  if (response.success) {
    orders.value = response.data || [];
  } else {
    orderMessage.value = response.message || "查询失败";
  }
};
</script>

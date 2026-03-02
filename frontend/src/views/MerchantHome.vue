<template>
  <section class="hero">
    <div class="hero-card">
      <h2>商户创建</h2>
      <p>对接后端 `/api/merchants`，需要先完成登录。</p>
      <div class="form-grid">
        <input v-model="merchantForm.name" placeholder="商户名称" />
        <input v-model="merchantForm.category" placeholder="分类" />
        <input v-model="merchantForm.city" placeholder="城市（建议英文）" />
        <input v-model="merchantForm.status" placeholder="状态" />
        <button class="cta" @click="submitMerchant">创建商户</button>
        <span>{{ merchantMessage }}</span>
      </div>
    </div>
    <div class="hero-card">
      <h2>门店创建</h2>
      <p>对接后端 `/api/shops`，用于推荐与订单。</p>
      <div class="form-grid">
        <input v-model="shopForm.name" placeholder="门店名称" />
        <input v-model="shopForm.category" placeholder="分类" />
        <input v-model="shopForm.city" placeholder="城市（建议英文）" />
        <input v-model="shopForm.tags" placeholder="标签" />
        <button class="cta" @click="submitShop">创建门店</button>
        <span>{{ shopMessage }}</span>
      </div>
    </div>
  </section>

  <section class="panel-grid">
    <div class="panel">
      <h2>门店列表</h2>
      <div class="form-grid">
        <input v-model="shopCity" placeholder="城市（建议英文）" />
        <button class="cta" @click="loadShops">查询门店</button>
      </div>
      <div class="list">
        <div v-for="shop in shopList" :key="shop.id" class="list-item">
          <strong>{{ shop.name }}</strong>
          <div>
            <span class="tag">{{ shop.category || "综合" }}</span>
            <span class="tag">{{ shop.city }}</span>
          </div>
        </div>
        <div v-if="shopList.length === 0" class="list-item">暂无门店</div>
      </div>
    </div>
  </section>
</template>

<script setup>
import { ref } from "vue";
import { createMerchant } from "../api/merchant";
import { createShop, listShops } from "../api/shop";

const merchantForm = ref({
  name: "",
  category: "",
  city: "Shanghai",
  status: "active"
});
const merchantMessage = ref("");

const shopForm = ref({
  name: "",
  category: "",
  city: "Shanghai",
  tags: ""
});
const shopMessage = ref("");

const shopCity = ref("Shanghai");
const shopList = ref([]);

const submitMerchant = async () => {
  merchantMessage.value = "";
  const response = await createMerchant(merchantForm.value);
  merchantMessage.value = response.success ? "创建成功" : response.message || "创建失败";
};

const submitShop = async () => {
  shopMessage.value = "";
  const response = await createShop(shopForm.value);
  shopMessage.value = response.success ? "创建成功" : response.message || "创建失败";
};

const loadShops = async () => {
  const response = await listShops(shopCity.value);
  if (response.success) {
    shopList.value = response.data || [];
  }
};
</script>

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
    <div class="hero-card">
      <h2>优惠券上架</h2>
      <p>对接后端 `/api/coupons`，支持平价券和秒杀券。</p>
      <div class="form-grid">
        <input v-model.number="couponForm.shopId" placeholder="门店ID" />
        <select v-model="couponForm.type">
          <option value="normal">平价券</option>
          <option value="seckill">特价券</option>
        </select>
        <input v-model="couponForm.title" placeholder="标题" />
        <input v-model="couponForm.description" placeholder="描述" />
        <input v-model.number="couponForm.discountAmount" type="number" step="0.01" placeholder="优惠金额" />
        <input v-model.number="couponForm.price" type="number" step="0.01" placeholder="售卖价格" />
        <input v-model.number="couponForm.totalStock" type="number" placeholder="总库存（秒杀券）" />
        <input v-model="couponForm.startTime" placeholder="开始时间 2026-03-04T10:00:00" />
        <input v-model="couponForm.endTime" placeholder="结束时间 2026-03-04T12:00:00" />
        <button class="cta" @click="submitCoupon">上架优惠券</button>
        <span>{{ couponMessage }}</span>
      </div>
    </div>
  </section>

  <section class="panel-grid">
    <div class="panel">
      <h2>门店列表</h2>
      <div class="form-grid">
        <input v-model="shopCity" placeholder="城市" />
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
import { createShop, listShops, createCoupon } from "../api/shop";

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

const couponForm = ref({
  shopId: "",
  type: "normal",
  title: "",
  description: "",
  discountAmount: 10,
  price: 1,
  totalStock: 100,
  startTime: "",
  endTime: ""
});
const couponMessage = ref("");

const shopCity = ref("上海");
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

const submitCoupon = async () => {
  couponMessage.value = "";
  const payload = {
    shopId: Number(couponForm.value.shopId),
    type: couponForm.value.type,
    title: couponForm.value.title,
    description: couponForm.value.description,
    discountAmount: Number(couponForm.value.discountAmount),
    price: Number(couponForm.value.price),
    totalStock: couponForm.value.type === "seckill" ? Number(couponForm.value.totalStock) : null,
    remainingStock: couponForm.value.type === "seckill" ? Number(couponForm.value.totalStock) : null,
    startTime: couponForm.value.type === "seckill" ? couponForm.value.startTime : null,
    endTime: couponForm.value.type === "seckill" ? couponForm.value.endTime : null
  };
  if (!payload.shopId || !payload.title || !payload.type) {
    couponMessage.value = "请补全门店与优惠券信息";
    return;
  }
  if (payload.discountAmount <= 0 || payload.price < 0 || payload.price >= payload.discountAmount) {
    couponMessage.value = "优惠金额需大于0，且售价需小于优惠金额";
    return;
  }
  if (payload.type === "seckill" && (!payload.totalStock || !payload.startTime || !payload.endTime)) {
    couponMessage.value = "秒杀券需填写库存与时间";
    return;
  }
  const response = await createCoupon(payload);
  couponMessage.value = response.success ? "上架成功" : response.message || "上架失败";
};

const loadShops = async () => {
  const response = await listShops({ city: shopCity.value });
  if (response.success) {
    shopList.value = response.data || [];
  }
};
</script>

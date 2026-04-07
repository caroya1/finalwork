<template>
  <div class="coupon-manage">
    <h1 class="page-title">优惠券管理</h1>

    <div class="form-card">
      <h2>上架优惠券</h2>
      <div class="form-grid">
        <select v-model="couponForm.shopId">
          <option value="">选择门店</option>
          <option v-for="shop in shopList" :key="shop.id" :value="shop.id">{{ shop.name }}</option>
        </select>
        <select v-model="couponForm.type">
          <option value="normal">平价券</option>
          <option value="seckill">特价券</option>
        </select>
        <input v-model="couponForm.title" placeholder="标题" />
        <input v-model="couponForm.description" placeholder="描述" />
        <input v-model.number="couponForm.discountAmount" type="number" step="0.01" placeholder="优惠金额（元）" />
        <input v-model.number="couponForm.price" type="number" step="0.01" placeholder="售卖价格（元）" />
        <input v-if="couponForm.type === 'seckill'" v-model.number="couponForm.totalStock" type="number" placeholder="总库存" />
        <input v-if="couponForm.type === 'seckill'" v-model="couponForm.startTime" type="datetime-local" placeholder="开始时间" />
        <select v-if="couponForm.type === 'seckill'" v-model="couponForm.endTimePreset">
          <option value="">选择结束时间</option>
          <option value="1h">1小时后结束</option>
          <option value="2h">2小时后结束</option>
          <option value="4h">4小时后结束</option>
          <option value="8h">8小时后结束</option>
          <option value="24h">24小时后结束</option>
          <option value="48h">48小时后结束</option>
          <option value="168h">7天后结束</option>
        </select>
        <button class="cta" @click="submitCoupon">上架优惠券</button>
        <span>{{ couponMessage }}</span>
      </div>
    </div>

    <div class="list-card">
      <div class="list-header">
        <h2>优惠券列表</h2>
        <select v-model="selectedShopId" @change="loadCoupons">
          <option value="">全部门店</option>
          <option v-for="shop in shopList" :key="shop.id" :value="String(shop.id)">{{ shop.name }}</option>
        </select>
      </div>
      <div class="coupon-list">
        <div v-for="coupon in couponList" :key="coupon.id" class="coupon-item">
          <div class="coupon-info">
            <strong>{{ coupon.title }}</strong>
            <div class="coupon-tags">
              <span class="tag" :class="'type-' + coupon.type">{{ coupon.type === 'seckill' ? '秒杀' : '普通' }}</span>
              <span class="tag discount">优惠 ¥{{ (coupon.discountAmount / 100).toFixed(2) }}</span>
              <span class="tag price">售价 ¥{{ (coupon.price / 100).toFixed(2) }}</span>
            </div>
          </div>
        </div>
        <div v-if="couponList.length === 0" class="empty-state">暂无优惠券</div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from "vue";
import { listMyShops, createCoupon, listCouponsByShop } from "../api/shop";

const shopList = ref([]);
const couponList = ref([]);
const selectedShopId = ref("");

const couponForm = ref({
  shopId: "",
  type: "normal",
  title: "",
  description: "",
  discountAmount: 10,
  price: 1,
  totalStock: 100,
  startTime: "",
  endTimePreset: ""
});
const couponMessage = ref("");

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

const loadCoupons = async () => {
  if (!selectedShopId.value) {
    couponList.value = [];
    return;
  }
  const response = await listCouponsByShop(Number(selectedShopId.value));
  if (response.success) {
    couponList.value = response.data || [];
  }
};

const formatLocalDateTime = (value) => {
  if (!value) return "";
  return value.length === 16 ? `${value}:00` : value;
};

const resolveCouponEndTime = (startTime, preset) => {
  if (!startTime || !preset) return "";
  const start = new Date(startTime);
  if (Number.isNaN(start.getTime())) return "";
  const hours = Number(preset.replace("h", ""));
  if (!hours || hours <= 0) return "";
  const end = new Date(start.getTime() + hours * 60 * 60 * 1000);
  const yyyy = end.getFullYear();
  const mm = String(end.getMonth() + 1).padStart(2, "0");
  const dd = String(end.getDate()).padStart(2, "0");
  const hh = String(end.getHours()).padStart(2, "0");
  const mi = String(end.getMinutes()).padStart(2, "0");
  const ss = String(end.getSeconds()).padStart(2, "0");
  return `${yyyy}-${mm}-${dd}T${hh}:${mi}:${ss}`;
};

const submitCoupon = async () => {
  couponMessage.value = "";
  const resolvedEndTime = resolveCouponEndTime(couponForm.value.startTime, couponForm.value.endTimePreset);
  const payload = {
    shopId: Number(couponForm.value.shopId),
    type: couponForm.value.type,
    title: couponForm.value.title,
    description: couponForm.value.description,
    discountAmount: Number(couponForm.value.discountAmount) * 100,
    price: Number(couponForm.value.price) * 100,
    totalStock: couponForm.value.type === "seckill" ? Number(couponForm.value.totalStock) : null,
    remainingStock: couponForm.value.type === "seckill" ? Number(couponForm.value.totalStock) : null,
    startTime: couponForm.value.type === "seckill" ? formatLocalDateTime(couponForm.value.startTime) : null,
    endTime: couponForm.value.type === "seckill" ? resolvedEndTime : null
  };
  if (!payload.shopId || !payload.title) {
    couponMessage.value = "请补全门店和优惠券标题";
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
  if (payload.type === "seckill" && payload.endTime <= payload.startTime) {
    couponMessage.value = "结束时间必须晚于开始时间";
    return;
  }
  const response = await createCoupon(payload);
  couponMessage.value = response.success ? "优惠券上架成功" : response.message || "上架失败";
  if (response.success) {
    if (selectedShopId.value === String(payload.shopId)) {
      await loadCoupons();
    } else {
      selectedShopId.value = String(payload.shopId);
      await loadCoupons();
    }
  }
};

onMounted(async () => {
  await loadShops();
});
</script>

<style scoped>
.coupon-manage {
  max-width: 1200px;
}

.page-title {
  font-size: 1.8rem;
  font-weight: 600;
  margin-bottom: 24px;
  color: #1a1a2e;
}

.form-card {
  background: #fff;
  border-radius: 12px;
  padding: 24px;
  margin-bottom: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.form-card h2 {
  font-size: 1.2rem;
  margin-bottom: 20px;
  color: #1a1a2e;
}

.form-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 16px;
}

.form-grid input,
.form-grid select {
  padding: 12px 16px;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  font-size: 14px;
}

.form-grid input:focus,
.form-grid select:focus {
  outline: none;
  border-color: #ff6b35;
}

.cta {
  padding: 12px 24px;
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

.list-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.list-header h2 {
  font-size: 1.2rem;
  color: #1a1a2e;
}

.list-header select {
  padding: 8px 16px;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  font-size: 14px;
}

.coupon-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.coupon-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  background: #f8f9fa;
  border-radius: 8px;
  transition: background 0.2s;
}

.coupon-item:hover {
  background: #f0f0f0;
}

.coupon-info {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.coupon-info strong {
  font-size: 16px;
  color: #1a1a2e;
}

.coupon-tags {
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

.tag.type-normal {
  background: #d4edda;
  color: #155724;
}

.tag.type-seckill {
  background: #fff3cd;
  color: #856404;
}

.tag.discount {
  background: #ff6b35;
  color: #fff;
  font-weight: 500;
}

.tag.price {
  background: #17a2b8;
  color: #fff;
}

.empty-state {
  text-align: center;
  padding: 40px;
  color: #999;
}
</style>

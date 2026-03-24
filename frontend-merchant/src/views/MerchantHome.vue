<template>
  <section class="hero">
    <div class="hero-card">
      <h2>商户工作台</h2>
      <p>已登录商户：{{ merchantName }}（ID: {{ merchantId || "-" }}）</p>
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
          <div class="stat-label">营业额(分)</div>
          <div class="stat-value">{{ shopStats.revenue || 0 }}</div>
        </div>
      </div>
    </div>

    <div class="hero-card">
      <h2>创建门店</h2>
      <div class="form-grid">
        <input v-model="shopForm.name" placeholder="门店名称" />
        <input v-model="shopForm.category" placeholder="分类" />
        <input v-model="shopForm.city" placeholder="城市" />
        <input v-model="shopForm.tags" placeholder="标签(逗号分隔)" />
        <input v-model="shopForm.address" placeholder="地址" />
        <input v-model="shopForm.businessHours" placeholder="营业时间，例如 10:00-22:00" />
        <input v-model="shopForm.contactPhone" placeholder="联系电话" />
        <input type="file" accept="image/*" @change="onShopImageChange" />
        <button class="cta" @click="submitShop">创建门店</button>
        <span>{{ shopMessage }}</span>
      </div>
    </div>

    <div class="hero-card">
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
        <input v-model.number="couponForm.discountAmount" type="number" step="0.01" placeholder="优惠金额" />
        <input v-model.number="couponForm.price" type="number" step="0.01" placeholder="售卖价格" />
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
  </section>

  <section class="panel-grid">
    <div class="panel">
      <h2>我的门店</h2>
      <div class="list">
        <div v-for="shop in shopList" :key="shop.id" class="list-item">
          <strong>{{ shop.name }}</strong>
          <div>
            <span class="tag">{{ shop.category || "综合" }}</span>
            <span class="tag">{{ shop.city || "-" }}</span>
            <span class="tag">审核 {{ shop.auditStatus }}</span>
          </div>
        </div>
        <div v-if="shopList.length === 0" class="list-item">暂无门店</div>
      </div>
    </div>

    <div class="panel" v-if="activeShopId">
      <h2>菜品管理</h2>
      <div class="form-grid">
        <input v-model="dishForm.name" placeholder="菜品名称" />
        <input v-model.number="dishForm.price" type="number" step="0.01" placeholder="价格" />
        <input v-model="dishForm.description" placeholder="描述" />
        <input type="file" accept="image/*" @change="onDishImageChange" />
        <button class="cta" @click="submitDish">新增菜品</button>
        <span>{{ dishMessage }}</span>
      </div>
      <div class="list" style="margin-top: 12px;">
        <div v-for="dish in dishList" :key="dish.id" class="list-item">
          <strong>{{ dish.name }}</strong>
          <div>
            <span class="tag">¥{{ dish.price }}</span>
            <span class="tag">状态 {{ dish.status }}</span>
            <button class="ghost-btn" @click="removeDish(dish.id)">删除</button>
          </div>
        </div>
        <div v-if="dishList.length === 0" class="list-item">暂无菜品</div>
      </div>
    </div>

    <div class="panel" v-if="activeShopId">
      <h2>订单管理</h2>
      <div class="form-grid">
        <select v-model="orderStatusFilter">
          <option value="">全部状态</option>
          <option value="0">待支付</option>
          <option value="1">已支付</option>
          <option value="2">已核销</option>
          <option value="3">已退款</option>
          <option value="4">已取消</option>
        </select>
        <button class="cta" @click="loadOrders">查询订单</button>
      </div>
      <div class="list" style="margin-top: 12px;">
        <div v-for="order in orderList" :key="order.id" class="list-item">
          <strong>{{ order.orderNo || `订单#${order.id}` }}</strong>
          <div>
            <span class="tag">用户 {{ order.userId }}</span>
            <span class="tag">金额 {{ order.payAmount || order.amount }}</span>
            <span class="tag">状态 {{ order.status }}</span>
            <button class="ghost-btn" v-if="order.status === 1" @click="doVerify(order.id)">核销</button>
          </div>
        </div>
        <div v-if="orderList.length === 0" class="list-item">暂无订单</div>
      </div>
    </div>

    <div class="panel" v-if="activeShopId">
      <h2>优惠券列表</h2>
      <div class="list">
        <div v-for="coupon in couponList" :key="coupon.id" class="list-item">
          <strong>{{ coupon.title }}</strong>
          <div>
            <span class="tag">{{ coupon.type }}</span>
            <span class="tag">优惠 ¥{{ coupon.discountAmount }}</span>
            <span class="tag">售价 ¥{{ coupon.price }}</span>
          </div>
        </div>
        <div v-if="couponList.length === 0" class="list-item">暂无优惠券</div>
      </div>
    </div>
  </section>
</template>

<script setup>
import { ref, onMounted, watch } from "vue";
import {
  createShop,
  listMyShops,
  createCoupon,
  listCouponsByShop,
  listDishes,
  createDish,
  deleteDish,
  listOrdersByShop,
  verifyOrder,
  getOrderStats
} from "../api/shop";
import { uploadImage } from "../api/oss";

const merchantId = ref(localStorage.getItem("dp_merchant_id") || "");
const merchantName = ref(localStorage.getItem("dp_merchant_name") || localStorage.getItem("dp_username") || "");

const shopForm = ref({
  name: "",
  category: "",
  city: "上海",
  tags: "",
  address: "",
  businessHours: "",
  contactPhone: "",
  imageUrl: ""
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
  endTimePreset: ""
});
const couponMessage = ref("");

const dishForm = ref({
  name: "",
  price: 0,
  description: "",
  imageUrl: ""
});
const dishMessage = ref("");

const dashboardMessage = ref("");
const shopList = ref([]);
const dishList = ref([]);
const orderList = ref([]);
const couponList = ref([]);
const shopStats = ref(null);
const activeShopId = ref("");
const orderStatusFilter = ref("");

const loadShops = async () => {
  const response = await listMyShops();
  if (response.success) {
    shopList.value = response.data || [];
    if (!activeShopId.value && shopList.value.length > 0) {
      activeShopId.value = String(shopList.value[0].id);
    }
  }
};

const submitShop = async () => {
  shopMessage.value = "";
  if (!shopForm.value.name || !shopForm.value.category || !shopForm.value.city) {
    shopMessage.value = "请填写门店名称、分类和城市";
    return;
  }
  const response = await createShop(shopForm.value);
  shopMessage.value = response.success ? "门店创建成功" : response.message || "门店创建失败";
  if (response.success) {
    shopForm.value = {
      name: "",
      category: "",
      city: "上海",
      tags: "",
      address: "",
      businessHours: "",
      contactPhone: "",
      imageUrl: ""
    };
    await loadShops();
  }
};

const submitCoupon = async () => {
  couponMessage.value = "";
  const resolvedEndTime = resolveCouponEndTime(couponForm.value.startTime, couponForm.value.endTimePreset);
  const payload = {
    shopId: Number(couponForm.value.shopId),
    type: couponForm.value.type,
    title: couponForm.value.title,
    description: couponForm.value.description,
    discountAmount: Number(couponForm.value.discountAmount),
    price: Number(couponForm.value.price),
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
  if (response.success && activeShopId.value) {
    await loadCoupons();
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

const submitDish = async () => {
  dishMessage.value = "";
  if (!activeShopId.value) {
    dishMessage.value = "请先选择门店";
    return;
  }
  if (!dishForm.value.name || !dishForm.value.price) {
    dishMessage.value = "请填写菜品名称和价格";
    return;
  }
  const response = await createDish(Number(activeShopId.value), dishForm.value);
  dishMessage.value = response.success ? "菜品新增成功" : response.message || "新增失败";
  if (response.success) {
    dishForm.value = { name: "", price: 0, description: "", imageUrl: "" };
    await loadDishes();
  }
};

const loadDishes = async () => {
  if (!activeShopId.value) return;
  const response = await listDishes(Number(activeShopId.value));
  if (response.success) {
    dishList.value = response.data || [];
  }
};

const removeDish = async (dishId) => {
  if (!activeShopId.value) return;
  const response = await deleteDish(Number(activeShopId.value), dishId);
  if (response.success) {
    await loadDishes();
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

const doVerify = async (orderId) => {
  const response = await verifyOrder(orderId);
  dashboardMessage.value = response.success ? "核销成功" : response.message || "核销失败";
  await loadOrders();
  await loadStats();
};

const loadCoupons = async () => {
  if (!activeShopId.value) return;
  const response = await listCouponsByShop(Number(activeShopId.value));
  if (response.success) {
    couponList.value = response.data || [];
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
  await Promise.all([loadDishes(), loadOrders(), loadCoupons(), loadStats()]);
  dashboardMessage.value = "已刷新";
};

const onShopImageChange = async (event) => {
  const file = event.target.files?.[0];
  if (!file) return;
  const response = await uploadImage(file, "shops");
  if (response.success) {
    shopForm.value.imageUrl = response.data?.url || "";
    shopMessage.value = "店铺图片上传成功";
  } else {
    shopMessage.value = response.message || "店铺图片上传失败";
  }
};

const onDishImageChange = async (event) => {
  const file = event.target.files?.[0];
  if (!file) return;
  const response = await uploadImage(file, "dishes");
  if (response.success) {
    dishForm.value.imageUrl = response.data?.url || "";
    dishMessage.value = "菜品图片上传成功";
  } else {
    dishMessage.value = response.message || "菜品图片上传失败";
  }
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
/* ========== Stats Grid ========== */
.stats-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: var(--space-4);
  margin-top: var(--space-5);
}

.stat-item {
  background: linear-gradient(135deg, var(--bg-secondary) 0%, var(--bg-primary) 100%);
  border: 1px solid var(--border-light);
  border-radius: var(--radius-lg);
  padding: var(--space-4);
  transition: all var(--transition-fast);
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
  background: var(--brand-gradient);
  border-radius: var(--radius-lg) 0 0 var(--radius-lg);
}

.stat-item:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-md);
  border-color: var(--brand-primary);
}

.stat-label {
  font-size: var(--text-xs);
  font-weight: var(--font-medium);
  color: var(--text-tertiary);
  text-transform: uppercase;
  letter-spacing: 0.05em;
  margin-bottom: var(--space-1);
}

.stat-value {
  font-size: var(--text-2xl);
  font-weight: var(--font-bold);
  color: var(--brand-primary);
}

/* ========== Hero Card Enhancements ========== */
.hero-card {
  position: relative;
}

.hero-card h2 {
  display: flex;
  align-items: center;
  gap: var(--space-2);
}

.hero-card h2::before {
  content: '';
  width: 4px;
  height: 20px;
  background: var(--brand-gradient);
  border-radius: var(--radius-full);
}

/* ========== Form Enhancements ========== */
.form-grid input[type="number"] {
  appearance: textfield;
}

.form-grid input[type="number"]::-webkit-outer-spin-button,
.form-grid input[type="number"]::-webkit-inner-spin-button {
  appearance: none;
  margin: 0;
}

.form-grid input[type="file"] {
  padding: var(--space-2);
  cursor: pointer;
}

.form-grid input[type="file"]::file-selector-button {
  padding: var(--space-2) var(--space-4);
  border: none;
  border-radius: var(--radius-md);
  background: var(--brand-primary-light);
  color: var(--brand-primary);
  font-size: var(--text-sm);
  font-weight: var(--font-medium);
  cursor: pointer;
  transition: all var(--transition-fast);
}

.form-grid input[type="file"]::file-selector-button:hover {
  background: var(--brand-primary);
  color: var(--text-inverse);
}

.form-grid span {
  font-size: var(--text-sm);
  color: var(--text-tertiary);
  padding: var(--space-2) 0;
}

.form-grid .cta {
  align-self: flex-start;
}

/* ========== List Enhancements ========== */
.list-item {
  display: flex;
  flex-direction: column;
  gap: var(--space-2);
}

.list-item strong {
  font-size: var(--text-base);
  color: var(--text-primary);
  font-weight: var(--font-semibold);
}

.list-item .tag {
  font-size: var(--text-xs);
}

.list-item button {
  font-size: var(--text-xs);
  padding: var(--space-1) var(--space-3);
}

/* ========== Panel Section Headers ========== */
.panel h2 {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  padding-bottom: var(--space-3);
  border-bottom: 1px solid var(--border-light);
  margin-bottom: var(--space-4);
}

.panel h2::before {
  content: '';
  width: 4px;
  height: 18px;
  background: var(--brand-gradient);
  border-radius: var(--radius-full);
}

/* ========== Responsive ========== */
@media (max-width: 640px) {
  .stats-grid {
    grid-template-columns: 1fr;
  }
  
  .stat-value {
    font-size: var(--text-xl);
  }
}
</style>

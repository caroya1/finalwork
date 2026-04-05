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
      <h2>{{ editingShopId ? '编辑门店' : '创建门店' }}</h2>
      <div class="form-grid">
        <input v-model="shopForm.name" placeholder="门店名称" />
        <input v-model="shopForm.category" placeholder="分类" />
        <input v-model="shopForm.city" placeholder="城市" />
        <input v-model="shopForm.tags" placeholder="标签(逗号分隔)" />
        <input v-model="shopForm.address" placeholder="地址" />
        <input v-model="shopForm.businessHours" placeholder="营业时间，例如 10:00-22:00" />
        <input v-model="shopForm.contactPhone" placeholder="联系电话" />
        <input type="file" accept="image/*" @change="onShopImageChange" />
        <div v-if="shopForm.imagePreview" class="image-preview">
          <img :src="shopForm.imagePreview" alt="店铺图片预览" />
          <button type="button" class="remove-btn" @click="shopForm.imagePreview = ''; shopForm.imageUrl = ''">×</button>
        </div>
        <button class="cta" @click="submitShop">{{ editingShopId ? '更新门店' : '创建门店' }}</button>
        <button v-if="editingShopId" class="ghost-btn" @click="resetShopForm">取消编辑</button>
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
            <button class="ghost-btn" @click="editShop(shop)">编辑</button>
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
        <div v-if="dishForm.imagePreview" class="image-preview">
          <img :src="dishForm.imagePreview" alt="菜品图片预览" />
          <button type="button" class="remove-btn" @click="dishForm.imagePreview = ''; dishForm.imageUrl = ''">×</button>
        </div>
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
  updateShop,
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
  imageUrl: "",
  imagePreview: ""
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
  imageUrl: "",
  imagePreview: ""
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
const editingShopId = ref(null);

/**
 * Load merchant's shops from the backend.
 * Auto-selects the first shop if none is selected.
 * @async
 * @returns {Promise<void>}
 */
const loadShops = async () => {
  try {
    // 检查localStorage中的认证信息
    const token = localStorage.getItem("dp_token");
    const merchantId = localStorage.getItem("dp_merchant_id");
    
    if (!token) {
      dashboardMessage.value = "错误：未找到登录凭证，请重新登录";
      console.error("[loadShops] 未找到dp_token");
      return;
    }
    if (!merchantId) {
      dashboardMessage.value = "错误：未找到商户ID，请重新登录";
      console.error("[loadShops] 未找到dp_merchant_id");
      return;
    }
    
    console.log("[loadShops] 正在加载门店列表...", { token: token.substring(0, 20) + "...", merchantId });
    
    const response = await listMyShops();
    console.log("[loadShops] API响应:", response);
    
    if (response.success) {
      shopList.value = response.data || [];
      console.log("[loadShops] 加载成功，门店数量:", shopList.value.length);
      if (!activeShopId.value && shopList.value.length > 0) {
        activeShopId.value = String(shopList.value[0].id);
      }
    } else {
      dashboardMessage.value = "获取门店列表失败: " + (response.message || "未知错误");
      console.error("[loadShops] API返回错误:", response.message);
    }
  } catch (error) {
    const errorMsg = error.userMessage || error.message || "网络错误";
    dashboardMessage.value = "获取门店列表失败: " + errorMsg;
    console.error("[loadShops] 异常:", error);
    
    // 如果是401/403，错误拦截器会自动跳转登录页
    if (error.response?.status === 401 || error.response?.status === 403) {
      dashboardMessage.value = "登录已过期，正在跳转到登录页...";
    }
  }
};

const resetShopForm = () => {
  shopForm.value = {
    name: "",
    category: "",
    city: "上海",
    tags: "",
    address: "",
    businessHours: "",
    contactPhone: "",
    imageUrl: "",
    imagePreview: ""
  };
  editingShopId.value = null;
};

const editShop = (shop) => {
  editingShopId.value = shop.id;
  shopForm.value = {
    name: shop.name,
    category: shop.category,
    city: shop.city,
    tags: shop.tags || "",
    address: shop.address || "",
    businessHours: shop.businessHours || "",
    contactPhone: shop.contactPhone || "",
    imageUrl: shop.imageUrl || "",
    imagePreview: shop.imageUrl || ""
  };
};

const submitShop = async () => {
  shopMessage.value = "";
  if (!shopForm.value.name || !shopForm.value.category || !shopForm.value.city) {
    shopMessage.value = "请填写门店名称、分类和城市";
    return;
  }
  
  // 检查敏感词（简单的本地检查）
  const sensitiveWords = ['测试', 'test', 'TEST'];
  const nameLower = shopForm.value.name.toLowerCase();
  const hasSensitiveWord = sensitiveWords.some(word => nameLower.includes(word.toLowerCase()));
  if (hasSensitiveWord) {
    shopMessage.value = "提示：店铺名称包含'测试'等敏感词可能导致审核被拒绝，建议使用真实店铺名称";
    // 继续提交，但给出警告
  }
  
  let response;
  if (editingShopId.value) {
    response = await updateShop(editingShopId.value, shopForm.value);
    shopMessage.value = response.success ? "门店更新成功" : response.message || "门店更新失败";
  } else {
    response = await createShop(shopForm.value);
    if (response.success) {
      shopMessage.value = "门店创建成功";
    } else {
      // 检查是否是审核拒绝的错误
      if (response.message && response.message.includes("Content violates")) {
        shopMessage.value = "创建失败：内容违反社区规范（可能包含敏感词'测试'等）。请使用真实店铺名称重试。";
      } else {
        shopMessage.value = response.message || "门店创建失败";
      }
    }
  }
  
  if (response.success) {
    resetShopForm();
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
    dishForm.value = { name: "", price: 0, description: "", imageUrl: "", imagePreview: "" };
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
  
  // 立即显示本地预览
  const reader = new FileReader();
  reader.onload = (e) => {
    shopForm.value.imagePreview = e.target.result;
  };
  reader.readAsDataURL(file);
  
  // 上传到服务器
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
  
  // 立即显示本地预览
  const reader = new FileReader();
  reader.onload = (e) => {
    dishForm.value.imagePreview = e.target.result;
  };
  reader.readAsDataURL(file);
  
  // 上传到服务器
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

/* ========== Image Preview ========== */
.image-preview {
  position: relative;
  display: inline-block;
  width: 100px;
  height: 100px;
  border-radius: var(--radius-md);
  overflow: hidden;
  border: 2px solid var(--border-light);
  margin-top: var(--space-2);
}

.image-preview img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.image-preview .remove-btn {
  position: absolute;
  top: 4px;
  right: 4px;
  width: 20px;
  height: 20px;
  border-radius: 50%;
  background: rgba(0, 0, 0, 0.6);
  color: #fff;
  border: none;
  font-size: 14px;
  line-height: 1;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
}

.image-preview .remove-btn:hover {
  background: rgba(220, 53, 69, 0.9);
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

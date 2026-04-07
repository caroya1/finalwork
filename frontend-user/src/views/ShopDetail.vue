<template>
  <div class="shop-detail" v-if="shop">
    <div class="detail-left">
      <div class="shop-banner">
        <div class="banner-bg"></div>
        <div class="banner-overlay">
          <h1>{{ shop.name }}</h1>
          <div class="banner-tags">
            <span class="tag">{{ shop.category || '综合' }}</span>
            <span class="tag">{{ shop.city }}</span>
            <span class="tag rating-tag">⭐ {{ shop.rating ? shop.rating.toFixed(2) : '0.00' }} 分</span>
          </div>
          <p class="banner-address">📍 {{ shop.address || '地址完善中' }}</p>
        </div>
      </div>

      <!-- 我的评分 -->
      <div class="panel rate-panel">
        <h3>我的评分</h3>
        <div class="shop-rate">
          <select class="rate-select" v-model="myRating" @change="submitRating">
            <option value="">选择评分</option>
            <option v-for="s in ratingOptions" :key="s" :value="s">{{ s }} 分</option>
          </select>
          <span v-if="rateMsg" class="rate-msg">{{ rateMsg }}</span>
        </div>
      </div>

      <!-- 菜谱 / 菜品 -->
      <div class="panel">
        <div class="panel-head">
          <h3>🍽 菜谱（{{ dishes.length }}）</h3>
          <button class="cta small-cta" @click="showDishForm = !showDishForm">
            {{ showDishForm ? '收起' : '+ 添加菜品' }}
          </button>
        </div>

        <div v-if="showDishForm" class="dish-form">
          <input v-model="dishForm.name" placeholder="菜品名称（必填）" />
          <input v-model.number="dishForm.price" type="number" step="0.01" placeholder="价格（元）" />
          <input v-model="dishForm.description" placeholder="描述（选填）" />
          <button class="cta" @click="submitDish" :disabled="!dishForm.name">提交</button>
          <span v-if="dishMsg" class="rate-msg">{{ dishMsg }}</span>
        </div>

        <div class="dish-grid" v-if="dishes.length > 0">
          <div class="dish-card" v-for="dish in dishes" :key="dish.id">
            <div class="dish-icon">🍜</div>
            <div class="dish-info">
              <div class="dish-name">{{ dish.name }}</div>
              <div class="dish-desc" v-if="dish.description">{{ dish.description }}</div>
              <div class="dish-bottom">
                <span class="dish-price" v-if="dish.price != null">¥{{ dish.price.toFixed ? dish.price.toFixed(2) : dish.price }}</span>
                <span class="dish-user" v-if="dish.userId">用户 {{ dish.userId }} 推荐</span>
              </div>
            </div>
          </div>
        </div>
        <div v-else class="empty-state">暂无菜品，快来添加第一道菜吧！</div>
      </div>
    </div>

    <div class="detail-right">
      <!-- 店铺信息 -->
      <div class="panel">
        <h3>店铺信息</h3>
        <div class="info-row"><span class="info-label">分类</span><span>{{ shop.category || '-' }}</span></div>
        <div class="info-row"><span class="info-label">标签</span>
          <span class="tag" v-for="t in tagList" :key="t">{{ t }}</span>
        </div>
        <div class="info-row"><span class="info-label">城市</span><span>{{ shop.city || '-' }}</span></div>
        <div class="info-row"><span class="info-label">地址</span><span>{{ shop.address || '-' }}</span></div>
        <div class="info-row"><span class="info-label">评分</span><span>{{ shop.rating ? shop.rating.toFixed(2) : '0.00' }} 分</span></div>
      </div>

      <!-- 优惠券列表 -->
      <div class="panel">
        <h3>🎟 优惠券</h3>
        <div v-if="coupons.length === 0" class="empty-state">暂无优惠券</div>
        <div v-for="coupon in coupons" :key="coupon.id" class="coupon-card">
          <div class="coupon-main">
            <div class="coupon-title">{{ coupon.title }}</div>
            <div class="coupon-desc">{{ coupon.description || '限店铺使用' }}</div>
            <div class="coupon-meta">
              <span class="tag">{{ coupon.type === 'seckill' ? '特价券' : '平价券' }}</span>
              <span class="tag">优惠 ¥{{ (coupon.discountAmount / 100).toFixed(2) }}</span>
              <span class="tag">售价 ¥{{ ((coupon.price || 0) / 100).toFixed(2) }}</span>
              <span v-if="coupon.type === 'seckill'" class="tag">剩余 {{ coupon.remainingStock || 0 }}</span>
            </div>
          </div>
          <div class="coupon-action">
            <button class="cta small-cta" :disabled="isCouponOwned(coupon.id)" @click="openCouponConfirm(coupon)">
              {{ isCouponOwned(coupon.id) ? '已购买' : (coupon.type === 'seckill' ? '秒杀' : '购买') }}
            </button>
            <div v-if="couponHints[coupon.id]" :class="['rec-message', couponHints[coupon.id].type]">
              {{ couponHints[coupon.id].text }}
            </div>
          </div>
        </div>
      </div>

      <div v-if="couponConfirmOpen" class="auth-overlay" @click.self="closeCouponConfirm">
        <div class="auth-card coupon-confirm">
          <div class="confirm-header">
            <h3>确认购买</h3>
            <button class="auth-close" @click="closeCouponConfirm">×</button>
          </div>
          <p class="confirm-title">{{ selectedCoupon?.title }}</p>
          <p class="confirm-desc">{{ selectedCoupon?.description || '限店铺使用' }}</p>
          <div class="confirm-meta">
            <span class="tag">售价 ¥{{ ((selectedCoupon?.price || 0) / 100).toFixed(2) }}</span>
            <span class="tag">优惠 ¥{{ (selectedCoupon?.discountAmount / 100).toFixed(2) }}</span>
            <span class="tag">余额 ¥{{ (userBalance / 100).toFixed(2) }}</span>
          </div>
          <div class="confirm-actions">
            <button class="ghost-btn" @click="closeCouponConfirm">取消</button>
          <button class="cta" @click="confirmBuyCoupon">确认购买</button>
          </div>
          <div v-if="couponMessage" :class="['rec-message', couponMessageType]">{{ couponMessage }}</div>
        </div>
      </div>

      <div v-if="balanceModalOpen" class="auth-overlay" @click.self="balanceModalOpen = false">
        <div class="auth-card balance-modal">
          <h3>余额不足，请先充值</h3>
          <RouterLink class="balance-link" to="/profile" @click="balanceModalOpen = false">点此充值余额</RouterLink>
        </div>
      </div>

      <!-- 到店消费 -->
      <div class="panel consume-panel" v-if="isLoggedIn">
        <h3>🍽 到店消费</h3>
        <div class="consume-form">
          <div class="amount-input-wrapper">
            <span class="currency">¥</span>
            <input
              v-model.number="consumeAmount"
              type="number"
              min="0"
              step="0.01"
              placeholder="输入消费金额"
              class="amount-input"
            />
          </div>

          <div v-if="bestCoupon && consumeAmount > 0 && !skipCoupon" class="coupon-preview">
            <div class="selected-coupon">
              <div class="coupon-info">
                <span class="coupon-badge">已选优惠</span>
                <span class="coupon-name">{{ bestCoupon.title }}</span>
              </div>
              <span class="discount-amount">-¥{{ bestCoupon.discountAmount }}</span>
            </div>
          </div>

          <div v-else-if="consumeAmount > 0 && !skipCoupon" class="no-coupon">
            <span class="no-coupon-text">暂无可用优惠券</span>
          </div>

          <div v-if="consumeAmount > 0" class="opt-out-wrapper">
            <label class="opt-out">
              <input type="checkbox" v-model="skipCoupon" />
              <span>不使用优惠券</span>
            </label>
          </div>

          <div v-if="consumeAmount > 0" class="pay-preview">
            <div class="preview-row">
              <span class="preview-label">原价</span>
              <span class="preview-value original">¥{{ consumeAmount.toFixed(2) }}</span>
            </div>
            <div v-if="!skipCoupon && bestCoupon" class="preview-row discount">
              <span class="preview-label">优惠</span>
              <span class="preview-value discount-val">-¥{{ (bestCoupon.discountAmount / 100).toFixed(2) }}</span>
            </div>
            <div class="preview-row total">
              <span class="preview-label">应付</span>
              <span class="preview-value payable">¥{{ payableAmount.toFixed(2) }}</span>
            </div>
          </div>

          <button
            class="cta consume-cta"
            @click="submitOrder"
            :disabled="consumeAmount <= 0 || submitting"
          >
            {{ submitting ? '提交中...' : '立即下单' }}
          </button>

          <div v-if="orderMsg" :class="['order-message', orderMsgType]">
            {{ orderMsg }}
          </div>
        </div>
      </div>

      <!-- 支付确认弹窗 -->
      <div v-if="showPayModal" class="auth-overlay" @click.self="closePayModal">
        <div class="auth-card pay-modal">
          <div class="confirm-header">
            <h3>确认支付</h3>
            <button class="auth-close" @click="closePayModal">×</button>
          </div>
          <div class="pay-info" v-if="currentOrder">
            <div class="pay-row">
              <span>订单号</span>
              <span>{{ currentOrder.orderNo }}</span>
            </div>
            <div class="pay-row">
              <span>消费金额</span>
              <span>¥{{ (currentOrder.amount / 100)?.toFixed(2) }}</span>
            </div>
            <div class="pay-row discount" v-if="currentOrder.discountAmount">
              <span>优惠金额</span>
              <span>-¥{{ (currentOrder.discountAmount / 100)?.toFixed(2) }}</span>
            </div>
            <div class="pay-row total">
              <span>实付金额</span>
              <span class="pay-amount">¥{{ (currentOrder.payAmount / 100)?.toFixed(2) }}</span>
            </div>
          </div>
          <div class="pay-actions">
            <button class="ghost-btn" @click="closePayModal">取消</button>
            <button class="cta" @click="confirmPay" :disabled="paying">
              {{ paying ? '支付中...' : '确认支付' }}
            </button>
          </div>
        </div>
      </div>

      <!-- 相关帖子 -->
      <div class="panel">
        <h3>📝 相关笔记（{{ posts.length }}）</h3>
        <div v-if="posts.length === 0" class="empty-state">暂无相关笔记</div>
        <RouterLink
          class="related-post"
          v-for="p in posts"
          :key="p.id"
          :to="`/posts/${p.id}`"
        >
          <div class="related-post-title">{{ p.title }}</div>
          <div class="related-post-meta">
            <span>{{ p.city }}</span>
            <span>{{ p.likes || 0 }} 赞</span>
          </div>
        </RouterLink>
      </div>
    </div>
  </div>

  <div v-else class="empty-state" style="margin-top:60px;">
    <button class="ghost-btn" @click="$router.push('/')">← 返回首页</button>
    <p>店铺不存在或加载中...</p>
  </div>
</template>

<script setup>
import { ref, onMounted, computed, watch } from "vue";
import { useRoute, RouterLink } from "vue-router";
import { getShopDetail, rateShop, addDish, listCoupons, purchaseCoupon } from "../api/shop";
import { getProfile } from "../api/user";
import { createOrder, payOrder } from "../api/order";

const route = useRoute();
const shop = ref(null);
const dishes = ref([]);
const posts = ref([]);
const coupons = ref([]);
const myRating = ref("");
const rateMsg = ref("");
const showDishForm = ref(false);
const dishForm = ref({ name: "", price: null, description: "" });
const dishMsg = ref("");
const ratingOptions = [5, 4.5, 4, 3.5, 3, 2.5, 2, 1.5, 1];
const couponMessage = ref("");
const couponMessageType = ref("");
const ownedCouponIds = ref(new Set());
const couponHints = ref({});
const balanceModalOpen = ref(false);
const couponConfirmOpen = ref(false);
const selectedCoupon = ref(null);
const userBalance = ref(0);

// 到店消费相关状态
const consumeAmount = ref(0);
const skipCoupon = ref(false);
const bestCoupon = ref(null);
const submitting = ref(false);
const orderMsg = ref("");
const orderMsgType = ref("");
const isLoggedIn = computed(() => !!localStorage.getItem("dp_token"));

// 支付弹窗相关状态
const showPayModal = ref(false);
const currentOrder = ref(null);
const paying = ref(false);

const payableAmount = computed(() => {
  if (skipCoupon.value || !bestCoupon.value) {
    return consumeAmount.value || 0;
  }
  return Math.max(0, consumeAmount.value - bestCoupon.value.discountAmount);
});

// 打开支付弹窗
const openPayModal = (order) => {
  currentOrder.value = order;
  showPayModal.value = true;
};

// 关闭支付弹窗
const closePayModal = () => {
  showPayModal.value = false;
  currentOrder.value = null;
};

// 确认支付
const confirmPay = async () => {
  if (!currentOrder.value) return;
  paying.value = true;
  try {
    const res = await payOrder(currentOrder.value.id, currentOrder.value.payAmount);
    if (res.success) {
      orderMsg.value = "支付成功！";
      orderMsgType.value = "success";
      closePayModal();
      // 重置表单
      consumeAmount.value = 0;
      skipCoupon.value = false;
      bestCoupon.value = null;
    } else {
      orderMsg.value = res.message || "支付失败";
      orderMsgType.value = "error";
    }
  } catch (e) {
    orderMsg.value = e?.response?.data?.message || "支付失败，请重试";
    orderMsgType.value = "error";
  } finally {
    paying.value = false;
  }
};

const selectBestCoupon = () => {
  if (!consumeAmount.value || consumeAmount.value <= 0) {
    bestCoupon.value = null;
    return;
  }
  // 获取用户拥有的且适用于当前店铺的优惠券
  const availableCoupons = coupons.value.filter((c) => {
    return (
      ownedCouponIds.value.has(c.id) &&
      c.shopId === shop.value?.id &&
      c.discountAmount <= consumeAmount.value
    );
  });
  // 按优惠金额降序排列，选择最优的
  if (availableCoupons.length > 0) {
    availableCoupons.sort((a, b) => b.discountAmount - a.discountAmount);
    bestCoupon.value = availableCoupons[0];
  } else {
    bestCoupon.value = null;
  }
};

const submitOrder = async () => {
  orderMsg.value = "";
  orderMsgType.value = "";
  if (consumeAmount.value <= 0) {
    orderMsg.value = "请输入消费金额";
    orderMsgType.value = "error";
    return;
  }
  submitting.value = true;
  try {
    const res = await createOrder({
      shopId: shop.value?.id,
      amount: consumeAmount.value * 100,  // 元转分
      couponId: skipCoupon.value || !bestCoupon.value ? null : bestCoupon.value.id
    });
    if (res.success) {
      // 打开支付弹窗
      openPayModal(res.data);
    } else {
      orderMsg.value = res.message || "下单失败";
      orderMsgType.value = "error";
    }
  } catch (e) {
    orderMsg.value = e?.response?.data?.message || "下单失败，请重试";
    orderMsgType.value = "error";
  } finally {
    submitting.value = false;
  }
};

// 监听消费金额变化，自动选择最优优惠券
watch(consumeAmount, () => {
  selectBestCoupon();
});

const tagList = computed(() => {
  if (!shop.value || !shop.value.tags) return [];
  return shop.value.tags.split(",").map(t => t.trim()).filter(Boolean);
});

const load = async () => {
  const res = await getShopDetail(route.params.id);
  if (res.success) {
    shop.value = res.data.shop;
    dishes.value = res.data.dishes || [];
    posts.value = res.data.posts || [];
  }
  const couponRes = await listCoupons(route.params.id);
  if (couponRes.success) {
    coupons.value = couponRes.data || [];
  }
  await loadOwnedCoupons();
};

const loadOwnedCoupons = async () => {
  const userId = localStorage.getItem("dp_user_id");
  if (!userId) {
    ownedCouponIds.value = new Set();
    return;
  }
  const response = await getProfile(userId);
  if (response.success && response.data && Array.isArray(response.data.coupons)) {
    const paid = response.data.coupons.filter((c) => c && c.status !== "refunded");
    ownedCouponIds.value = new Set(paid.map((c) => c.couponId));
  }
};

const isCouponOwned = (couponId) => ownedCouponIds.value.has(couponId);

const submitRating = async () => {
  rateMsg.value = "";
  if (!myRating.value) return;
  const token = localStorage.getItem("dp_token");
  const refreshToken = localStorage.getItem("dp_refresh_token");
  if (!token || !refreshToken) {
    rateMsg.value = "请先登录";
    myRating.value = "";
    return;
  }
  const res = await rateShop(shop.value.id, Number(myRating.value));
  if (res.success) {
    rateMsg.value = "评分成功";
    await load();
  } else {
    rateMsg.value = res.message || "评分失败";
  }
};

const submitDish = async () => {
  dishMsg.value = "";
  const token = localStorage.getItem("dp_token");
  const refreshToken = localStorage.getItem("dp_refresh_token");
  if (!token || !refreshToken) {
    dishMsg.value = "请先登录";
    return;
  }
  if (!dishForm.value.name.trim()) {
    dishMsg.value = "请输入菜品名称";
    return;
  }
  const res = await addDish(shop.value.id, {
    name: dishForm.value.name.trim(),
    price: dishForm.value.price || null,
    description: dishForm.value.description.trim() || null
  });
  if (res.success) {
    dishMsg.value = "添加成功";
    dishForm.value = { name: "", price: null, description: "" };
    showDishForm.value = false;
    await load();
  } else {
    dishMsg.value = res.message || "添加失败";
  }
};


const openCouponConfirm = (coupon) => {
  couponMessage.value = "";
  couponMessageType.value = "";
  const userId = localStorage.getItem("dp_user_id");
  if (!userId) {
    couponHints.value[coupon.id] = { text: "请先登录", type: "error" };
    return;
  }
  if (isCouponOwned(coupon.id)) {
    couponHints.value[coupon.id] = { text: "已购买", type: "success" };
    return;
  }
  selectedCoupon.value = coupon;
  couponConfirmOpen.value = true;
  loadUserBalance();
};

const closeCouponConfirm = () => {
  couponConfirmOpen.value = false;
  selectedCoupon.value = null;
};

const confirmBuyCoupon = async () => {
  couponMessage.value = "";
  couponMessageType.value = "";
  const userId = localStorage.getItem("dp_user_id");
  if (!userId) {
    couponMessage.value = "请先登录";
    couponMessageType.value = "error";
    return;
  }
  if (!selectedCoupon.value) {
    couponMessage.value = "请选择优惠券";
    couponMessageType.value = "error";
    return;
  }
  const res = await purchaseCoupon(selectedCoupon.value.id, Number(userId));
  if (res.success) {
    ownedCouponIds.value.add(selectedCoupon.value.id);
    couponHints.value[selectedCoupon.value.id] = { text: "已购买", type: "success" };
    couponMessage.value = "已购买";
    couponMessageType.value = "success";
    couponConfirmOpen.value = false;
    await loadUserBalance();
    await load();
    return;
  }
  const message = res.message || "购买失败";
  if (message.includes("insufficient balance") || message.includes("余额不足")) {
    balanceModalOpen.value = true;
    return;
  }
  couponHints.value[selectedCoupon.value.id] = { text: message, type: "error" };
  couponMessage.value = message;
  couponMessageType.value = "error";
};

const loadUserBalance = async () => {
  const userId = localStorage.getItem("dp_user_id");
  if (!userId) return;
  const response = await getProfile(userId);
  if (response.success && response.data) {
    if (typeof response.data.balance === "number") {
      userBalance.value = response.data.balance;
    }
  }
};

onMounted(async () => {
  await load();
  await loadUserBalance();
});
</script>

<style scoped>
.shop-detail {
  display: grid;
  grid-template-columns: 1.3fr 0.7fr;
  gap: 28px;
  align-items: start;
  max-width: 1200px;
  margin: 0 auto;
}
.shop-banner {
  position: relative;
  border-radius: 24px;
  overflow: hidden;
  margin-bottom: 20px;
}
.banner-bg {
  height: 220px;
  background: linear-gradient(135deg, #ffc3a0, #ffb07c, #ffd6b8);
}
.banner-overlay {
  position: absolute;
  bottom: 0;
  left: 0; right: 0;
  padding: 24px 28px;
  background: linear-gradient(transparent, rgba(0,0,0,0.55));
  color: #fff;
}
.banner-overlay h1 {
  margin: 0 0 8px;
  font-size: 1.6rem;
}
.banner-tags {
  display: flex;
  gap: 8px;
  margin-bottom: 6px;
}
.banner-tags .tag {
  background: rgba(255,255,255,0.2);
  color: #fff;
}
.rating-tag {
  background: var(--accent) !important;
}
.banner-address {
  margin: 0;
  font-size: 0.85rem;
  opacity: 0.9;
}
.panel {
  background: var(--card);
  border-radius: 20px;
  padding: 20px 22px;
  box-shadow: var(--shadow);
  margin-bottom: 20px;
}
.coupon-card {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
  padding: 12px 14px;
  border: 1px solid var(--line);
  border-radius: 14px;
  margin-bottom: 12px;
  background: #fff;
}
.coupon-action {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 6px;
}
.coupon-title {
  font-weight: 600;
  margin-bottom: 4px;
}
.coupon-desc {
  font-size: 0.85rem;
  color: var(--muted);
  margin-bottom: 6px;
}
.coupon-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}
.balance-modal {
  text-align: center;
}
.balance-modal h3 {
  margin: 0 0 8px;
}
.coupon-confirm {
  max-width: 460px;
}
.confirm-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}
.confirm-title {
  font-weight: 600;
  margin: 0 0 6px;
}
.confirm-desc {
  font-size: 0.85rem;
  color: var(--muted);
  margin: 0 0 12px;
}
.confirm-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 16px;
}
.confirm-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}
.balance-link {
  display: inline-block;
  font-size: 0.85rem;
  color: var(--accent);
}
.panel h3 {
  margin: 0 0 14px;
  font-size: 1.1rem;
}
.panel-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 14px;
}
.panel-head h3 {
  margin: 0;
}
.small-cta {
  padding: 6px 14px;
  font-size: 0.8rem;
}
.rate-panel {
  display: flex;
  align-items: center;
  gap: 16px;
}
.rate-panel h3 {
  margin: 0;
  white-space: nowrap;
}
.shop-rate {
  display: flex;
  align-items: center;
  gap: 10px;
}
.rate-select {
  padding: 8px 14px;
  border-radius: 12px;
  border: 1px solid var(--line);
  background: #fff;
  font-size: 0.85rem;
}
.rate-msg {
  font-size: 0.8rem;
  color: var(--accent);
}
/* 菜品表单 */
.dish-form {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-bottom: 16px;
  padding: 14px;
  border: 1px dashed var(--line);
  border-radius: 14px;
  background: #fafafa;
}
.dish-form input {
  flex: 1;
  min-width: 140px;
  padding: 8px 12px;
  border: 1px solid var(--line);
  border-radius: 10px;
  font-size: 0.85rem;
}
.dish-form input:focus {
  outline: none;
  border-color: var(--accent);
}
/* 菜品列表 */
.dish-grid {
  display: grid;
  gap: 12px;
}
.dish-card {
  display: flex;
  align-items: flex-start;
  gap: 14px;
  padding: 14px;
  border: 1px solid var(--line);
  border-radius: 16px;
  transition: box-shadow 0.2s;
}
.dish-card:hover {
  box-shadow: 0 4px 16px rgba(0,0,0,0.06);
}
.dish-icon {
  width: 48px;
  height: 48px;
  border-radius: 14px;
  background: linear-gradient(135deg, #ffe3d1, #fff3e8);
  display: grid;
  place-items: center;
  font-size: 1.4rem;
  flex-shrink: 0;
}
.dish-name {
  font-weight: 600;
  margin-bottom: 4px;
}
.dish-desc {
  font-size: 0.8rem;
  color: var(--muted);
  margin-bottom: 4px;
}
.dish-bottom {
  display: flex;
  gap: 12px;
  align-items: center;
}
.dish-price {
  color: var(--accent);
  font-weight: 600;
  font-size: 0.9rem;
}
.dish-user {
  font-size: 0.75rem;
  color: var(--muted);
}
/* 店铺信息 */
.info-row {
  display: flex;
  gap: 10px;
  padding: 8px 0;
  border-bottom: 1px solid var(--line);
  font-size: 0.88rem;
  flex-wrap: wrap;
}
.info-row:last-child {
  border-bottom: none;
}
.info-label {
  color: var(--muted);
  min-width: 48px;
  flex-shrink: 0;
}
/* 相关帖子 */
.related-post {
  display: block;
  padding: 12px 14px;
  border: 1px solid var(--line);
  border-radius: 14px;
  margin-bottom: 10px;
  transition: border-color 0.2s;
  text-decoration: none;
  color: inherit;
}
.related-post:hover {
  border-color: var(--accent);
}
.related-post-title {
  font-weight: 600;
  margin-bottom: 4px;
  font-size: 0.9rem;
}
.related-post-meta {
  display: flex;
  justify-content: space-between;
  font-size: 0.78rem;
  color: var(--muted);
}
@media (max-width: 900px) {
  .shop-detail {
    grid-template-columns: 1fr;
  }
}

/* 到店消费面板 */
.consume-panel {
  background: linear-gradient(135deg, var(--card) 0%, #fff9f5 100%);
}

.consume-form {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.amount-input-wrapper {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 16px;
  background: #fff;
  border: 2px solid var(--line);
  border-radius: 14px;
  transition: border-color 0.2s;
}

.amount-input-wrapper:focus-within {
  border-color: var(--accent);
}

.amount-input-wrapper .currency {
  font-size: 1.2rem;
  font-weight: 600;
  color: var(--accent);
}

.amount-input {
  flex: 1;
  border: none;
  outline: none;
  font-size: 1.1rem;
  font-weight: 500;
  background: transparent;
}

.amount-input::placeholder {
  color: var(--muted);
}

.coupon-preview {
  padding: 12px 14px;
  background: linear-gradient(135deg, #e8f5e9 0%, #f1f8e9 100%);
  border-radius: 12px;
  border: 1px solid #c8e6c9;
}

.selected-coupon {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 10px;
}

.coupon-info {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.coupon-badge {
  display: inline-block;
  padding: 4px 10px;
  background: var(--accent);
  color: #fff;
  font-size: 0.75rem;
  font-weight: 600;
  border-radius: 20px;
}

.coupon-name {
  font-weight: 500;
  color: var(--text);
  font-size: 0.9rem;
}

.discount-amount {
  font-size: 1.1rem;
  font-weight: 700;
  color: #4caf50;
}

.no-coupon {
  padding: 12px 14px;
  background: var(--muted-bg, #f5f5f5);
  border-radius: 12px;
  text-align: center;
}

.no-coupon-text {
  color: var(--muted);
  font-size: 0.85rem;
}

.opt-out-wrapper {
  padding: 4px 2px;
}

.opt-out {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  font-size: 0.85rem;
  color: var(--muted);
  user-select: none;
}

.opt-out input[type="checkbox"] {
  width: 18px;
  height: 18px;
  cursor: pointer;
  accent-color: var(--accent);
}

.pay-preview {
  padding: 16px;
  background: #fff;
  border-radius: 14px;
  border: 1px solid var(--line);
}

.preview-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 6px 0;
}

.preview-row.discount {
  color: #4caf50;
}

.preview-row.total {
  margin-top: 8px;
  padding-top: 12px;
  border-top: 1px dashed var(--line);
}

.preview-label {
  font-size: 0.85rem;
  color: var(--muted);
}

.preview-value {
  font-weight: 500;
}

.preview-value.original {
  text-decoration: line-through;
  color: var(--muted);
}

.preview-value.discount-val {
  color: #4caf50;
  font-weight: 600;
}

.preview-value.payable {
  font-size: 1.3rem;
  font-weight: 700;
  color: var(--accent);
}

.consume-cta {
  width: 100%;
  padding: 14px 24px;
  font-size: 1rem;
  font-weight: 600;
}

.consume-cta:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.order-message {
  padding: 10px 14px;
  border-radius: 10px;
  font-size: 0.85rem;
  text-align: center;
}

.order-message.success {
  background: #e8f5e9;
  color: #2e7d32;
}

.order-message.error {
  background: #ffebee;
  color: #c62828;
}

/* 支付弹窗样式 */
.pay-modal {
  max-width: 400px;
}

.pay-info {
  margin: 20px 0;
  padding: 16px;
  background: #f8f9fa;
  border-radius: 12px;
}

.pay-row {
  display: flex;
  justify-content: space-between;
  padding: 8px 0;
  border-bottom: 1px dashed #e0e0e0;
}

.pay-row:last-child {
  border-bottom: none;
}

.pay-row.discount {
  color: #4caf50;
}

.pay-row.total {
  margin-top: 8px;
  padding-top: 12px;
  border-top: 2px solid #e0e0e0;
  font-weight: 600;
}

.pay-amount {
  font-size: 1.4rem;
  color: var(--accent);
}

.pay-actions {
  display: flex;
  gap: 12px;
  justify-content: flex-end;
  margin-top: 20px;
}
</style>

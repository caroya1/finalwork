<template>
  <section class="profile-page">
    <div v-if="loginMessage" class="auth-message error">{{ loginMessage }}</div>
    <div class="profile-hero" v-if="profile">
      <div class="profile-info">
        <div class="profile-avatar">{{ profile.username?.slice(0, 1) || "U" }}</div>
        <div>
          <h2>{{ profile.username }}</h2>
          <div class="profile-meta">
            <span class="tag">{{ profile.city || '-' }}</span>
            <span class="tag">{{ profile.role || 'user' }}</span>
          </div>
        </div>
      </div>
      <div class="profile-balance">
        <div class="balance-label">余额</div>
        <div class="balance-value">¥ {{ balanceDisplay }}</div>
        <div class="form-grid">
          <input v-model.number="rechargeAmount" type="number" min="1" placeholder="充值金额" />
          <button class="cta" @click="doRecharge">充值</button>
          <span v-if="rechargeMessage">{{ rechargeMessage }}</span>
        </div>
      </div>
    </div>

    <section class="profile-section">
      <div class="section-header">
        <h3>我的帖子</h3>
      </div>
      <div v-if="posts.length === 0" class="empty-state">暂无发布的帖子</div>
      <RouterLink v-for="post in posts" :key="post.id" class="list-item list-item-link" :to="`/posts/${post.id}`">
        <strong>{{ post.title }}</strong>
        <div class="feed-meta">
          <span>{{ post.city || '-' }}</span>
          <span>{{ post.likes || 0 }} 赞</span>
        </div>
      </RouterLink>
    </section>

    <section class="profile-section">
      <div class="section-header">
        <h3>我的优惠券</h3>
      </div>
      <div v-if="coupons.length === 0" class="empty-state">暂无优惠券</div>
      <div v-for="coupon in coupons" :key="coupon.purchaseId" class="coupon-card">
        <div class="coupon-main">
          <div class="coupon-title">{{ coupon.title }}</div>
          <div class="coupon-desc">{{ coupon.description || '限店铺使用' }}</div>
          <div class="coupon-meta">
            <span class="tag">{{ coupon.type === 'seckill' ? '特价券' : '平价券' }}</span>
            <span class="tag">优惠 ¥{{ coupon.discountAmount }}</span>
            <span class="tag">购入 ¥{{ coupon.price || 0 }}</span>
            <span class="tag">{{ coupon.status }}</span>
            <span v-if="coupon.refundReason" class="tag">退款原因：{{ coupon.refundReason }}</span>
          </div>
        </div>
        <div class="coupon-actions">
          <RouterLink class="ghost-btn" :to="`/shops/${coupon.shopId}`">去店铺</RouterLink>
          <button class="ghost-btn" :disabled="coupon.status === 'refunded'" @click="openRefund(coupon)">
            {{ coupon.status === 'refunded' ? '已退款' : '退款' }}
          </button>
        </div>
      </div>
    </section>

    <div v-if="refundOpen" class="auth-overlay" @click.self="closeRefund">
      <div class="auth-card refund-card">
        <div class="confirm-header">
          <h3>申请退款</h3>
          <button class="auth-close" @click="closeRefund">×</button>
        </div>
        <p class="confirm-title">{{ selectedPurchase?.title }}</p>
        <div class="confirm-meta">
          <span class="tag">购入 ¥{{ selectedPurchase?.price || 0 }}</span>
        </div>
        <select v-model="refundReason" class="form-select">
          <option value="" disabled>请选择退款原因</option>
          <option v-for="reason in refundReasons" :key="reason" :value="reason">{{ reason }}</option>
        </select>
        <div class="confirm-actions">
          <button class="ghost-btn" @click="closeRefund">取消</button>
          <button class="cta" @click="submitRefund" :disabled="!refundReason">确认退款</button>
        </div>
        <div v-if="refundMessage" :class="['rec-message', refundMessageType]">{{ refundMessage }}</div>
      </div>
    </div>
  </section>
</template>

<script setup>
import { ref, onMounted, computed } from "vue";
import { useRouter } from "vue-router";
import { RouterLink } from "vue-router";
import { rechargeBalance } from "../api/user";
import { refundCoupon } from "../api/shop";
import client from "../api/client";

const profile = ref(null);
const posts = ref([]);
const coupons = ref([]);
const rechargeAmount = ref(50);
const rechargeMessage = ref("");
const loginMessage = ref("");
const router = useRouter();
const refundOpen = ref(false);
const refundReason = ref("");
const refundMessage = ref("");
const refundMessageType = ref("");
const selectedPurchase = ref(null);
const refundReasons = [
  "买错了",
  "不想要了",
  "活动不符合预期",
  "价格不合适",
  "其他"
];

const balanceDisplay = computed(() => {
  if (!profile.value || profile.value.balance == null) return "0.00";
  const value = Number(profile.value.balance);
  return Number.isNaN(value) ? profile.value.balance : value.toFixed(2);
});

const requireLogin = () => {
  const token = localStorage.getItem("dp_token");
  const refreshToken = localStorage.getItem("dp_refresh_token");
  if (!token || !refreshToken) {
    loginMessage.value = "请先登录";
    router.push("/");
    window.dispatchEvent(new CustomEvent("dp-auth-required"));
    return false;
  }
  return true;
};

const loadProfile = async () => {
  if (!requireLogin()) return;
  const userId = localStorage.getItem("dp_user_id");
  if (!userId) return;
  const response = await client.get(`/api/users/${userId}/profile`);
  if (response.data && response.data.success) {
    profile.value = response.data.data;
    posts.value = response.data.data.posts || [];
    coupons.value = response.data.data.coupons || [];
    if (profile.value.balance != null) {
      localStorage.setItem("dp_balance", String(profile.value.balance));
    }
  }
};

const doRecharge = async () => {
  rechargeMessage.value = "";
  if (!requireLogin()) return;
  const userId = localStorage.getItem("dp_user_id");
  if (!userId) {
    rechargeMessage.value = "请先登录";
    return;
  }
  if (!rechargeAmount.value || rechargeAmount.value <= 0) {
    rechargeMessage.value = "充值金额需大于 0";
    return;
  }
  const res = await rechargeBalance(userId, Number(rechargeAmount.value));
  if (res.success) {
    rechargeMessage.value = "充值成功";
    await loadProfile();
  } else {
    rechargeMessage.value = res.message || "充值失败";
  }
};

const openRefund = (coupon) => {
  refundMessage.value = "";
  refundMessageType.value = "";
  if (!requireLogin()) return;
  selectedPurchase.value = coupon;
  refundReason.value = "";
  refundOpen.value = true;
};

const closeRefund = () => {
  refundOpen.value = false;
  selectedPurchase.value = null;
  refundReason.value = "";
};

const submitRefund = async () => {
  refundMessage.value = "";
  refundMessageType.value = "";
  if (!requireLogin()) return;
  if (!selectedPurchase.value) {
    refundMessage.value = "请选择需要退款的优惠券";
    refundMessageType.value = "error";
    return;
  }
  if (!refundReason.value) {
    refundMessage.value = "请选择退款原因";
    refundMessageType.value = "error";
    return;
  }
  const userId = localStorage.getItem("dp_user_id");
  const res = await refundCoupon(selectedPurchase.value.purchaseId, {
    userId: Number(userId),
    reason: refundReason.value
  });
  if (res.success) {
    refundMessage.value = "已退款";
    refundMessageType.value = "success";
    await loadProfile();
    return;
  }
  refundMessage.value = res.message || "退款失败";
  refundMessageType.value = "error";
};

onMounted(loadProfile);
</script>

<style scoped>
.profile-page {
  display: grid;
  gap: 28px;
  max-width: 1200px;
  margin: 0 auto;
}
.profile-hero {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 24px;
  align-items: center;
}
.profile-info {
  display: flex;
  gap: 16px;
  align-items: center;
  background: var(--card);
  padding: 20px 24px;
  border-radius: 20px;
  box-shadow: var(--shadow);
}
.profile-avatar {
  width: 64px;
  height: 64px;
  border-radius: 20px;
  background: var(--accent);
  color: #fff;
  display: grid;
  place-items: center;
  font-size: 1.4rem;
  font-weight: 600;
}
.profile-meta {
  margin-top: 8px;
  display: flex;
  gap: 8px;
}
.profile-balance {
  background: var(--card);
  padding: 20px 24px;
  border-radius: 20px;
  box-shadow: var(--shadow);
}
.balance-label {
  font-size: 0.85rem;
  color: var(--muted);
}
.balance-value {
  font-size: 1.6rem;
  font-weight: 600;
  margin: 6px 0 12px;
}
.profile-section {
  background: var(--card);
  padding: 20px 24px;
  border-radius: 20px;
  box-shadow: var(--shadow);
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
.coupon-actions {
  display: flex;
  gap: 10px;
  align-items: center;
}

.refund-card {
  max-width: 460px;
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
.list-item-link {
  display: block;
  text-decoration: none;
  color: inherit;
  transition: border-color 0.2s;
  cursor: pointer;
}
.list-item-link:hover {
  border-color: var(--accent);
}
@media (max-width: 900px) {
  .profile-hero {
    grid-template-columns: 1fr;
  }
}
</style>

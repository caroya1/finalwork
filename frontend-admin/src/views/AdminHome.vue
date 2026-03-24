<template>
  <section class="hero">
    <div class="hero-card">
      <h2>运营看板</h2>
      <p>系统今日订单与审核概览</p>
      <div class="stats-grid">
        <div class="stat-item">
          <div class="stat-label">今日订单</div>
          <div class="stat-value">{{ todayOrderCount }}</div>
        </div>
        <div class="stat-item">
          <div class="stat-label">待审商户</div>
          <div class="stat-value">{{ pendingMerchantCount }}</div>
        </div>
        <div class="stat-item">
          <div class="stat-label">待审店铺</div>
          <div class="stat-value">{{ pendingShopCount }}</div>
        </div>
        <div class="stat-item">
          <div class="stat-label">待审内容</div>
          <div class="stat-value">{{ pendingPostCount }}</div>
        </div>
      </div>
      <div class="form-grid" style="margin-top: 12px;">
        <button class="cta" @click="refreshDashboard">刷新看板</button>
        <span>{{ dashboardMessage }}</span>
      </div>
    </div>

    <div class="hero-card">
      <h2>订单检索</h2>
      <div class="form-grid">
        <input v-model.number="orderQuery.userId" placeholder="用户ID" />
        <input v-model.number="orderQuery.shopId" placeholder="店铺ID" />
        <input v-model="orderQuery.orderNo" placeholder="订单号" />
        <select v-model="orderQuery.status">
          <option value="">全部状态</option>
          <option value="0">待支付</option>
          <option value="1">已支付</option>
          <option value="2">已核销</option>
          <option value="3">已退款</option>
          <option value="4">已取消</option>
        </select>
        <button class="cta" @click="searchOrders">查询订单</button>
        <button class="ghost-btn" @click="downloadOrders">导出CSV</button>
        <span>{{ orderMessage }}</span>
      </div>
    </div>
  </section>

  <section class="panel-grid">
    <div class="panel">
      <h2>订单列表</h2>
      <div class="list">
        <div v-for="order in orders" :key="order.id" class="list-item">
          <strong>{{ order.orderNo || `订单#${order.id}` }}</strong>
          <div>
            <span class="tag">用户 {{ order.userId }}</span>
            <span class="tag">店铺 {{ order.shopId }}</span>
            <span class="tag">金额 {{ order.payAmount || order.amount }}</span>
            <span class="tag">状态 {{ order.status }}</span>
          </div>
        </div>
        <div v-if="orders.length === 0" class="list-item">暂无订单</div>
      </div>
    </div>

    <div class="panel">
      <h2>用户管理</h2>
      <div class="form-grid">
        <select v-model="userStatusFilter">
          <option value="">全部用户</option>
          <option value="1">正常</option>
          <option value="0">禁用</option>
        </select>
        <button class="cta" @click="loadUsers">查询用户</button>
      </div>
      <div class="list" style="margin-top: 12px;">
        <div v-for="user in users" :key="user.id" class="list-item">
          <strong>{{ user.username }}</strong>
          <div>
            <span class="tag">{{ user.email || '-' }}</span>
            <span class="tag">状态 {{ user.status }}</span>
            <button class="ghost-btn" @click="changeUserStatus(user)">{{ user.status === 1 ? '禁用' : '启用' }}</button>
          </div>
        </div>
        <div v-if="users.length === 0" class="list-item">暂无用户</div>
      </div>
    </div>

    <div class="panel">
      <h2>商户审核</h2>
      <div class="list">
        <div v-for="merchant in merchants" :key="merchant.id" class="list-item">
          <strong>{{ merchant.name }}</strong>
          <div>
            <span class="tag">{{ merchant.email || '-' }}</span>
            <span class="tag">状态 {{ merchant.status }}</span>
            <button class="ghost-btn" v-if="merchant.status === 0" @click="approveMerchantAction(merchant.id)">通过</button>
            <button class="ghost-btn" v-if="merchant.status === 0" @click="rejectMerchantAction(merchant.id)">拒绝</button>
          </div>
        </div>
        <div v-if="merchants.length === 0" class="list-item">暂无商户</div>
      </div>
    </div>

    <div class="panel">
      <h2>店铺审核</h2>
      <div class="list">
        <div v-for="shop in shops" :key="shop.id" class="list-item">
          <strong>{{ shop.name }}</strong>
          <div>
            <span class="tag">商户 {{ shop.merchantId }}</span>
            <span class="tag">审核 {{ shop.auditStatus }}</span>
            <button class="ghost-btn" v-if="shop.auditStatus === 0" @click="approveShopAction(shop.id)">通过</button>
            <button class="ghost-btn" v-if="shop.auditStatus === 0" @click="rejectShopAction(shop.id)">拒绝</button>
          </div>
        </div>
        <div v-if="shops.length === 0" class="list-item">暂无店铺</div>
      </div>
    </div>

    <div class="panel">
      <h2>内容审核</h2>
      <div class="list">
        <div v-for="post in posts" :key="post.id" class="list-item">
          <strong>{{ post.title }}</strong>
          <div>
            <span class="tag">用户 {{ post.userId }}</span>
            <span class="tag">审核 {{ post.auditStatus }}</span>
            <button class="ghost-btn" v-if="post.auditStatus !== 1" @click="approvePostAction(post.id)">通过</button>
            <button class="ghost-btn" v-if="post.auditStatus !== 2" @click="rejectPostAction(post.id)">驳回</button>
          </div>
        </div>
        <div v-if="posts.length === 0" class="list-item">暂无帖子</div>
      </div>
    </div>

    <div class="panel">
      <h2>系统配置</h2>
      <div class="form-grid">
        <input v-model.number="localConfig.orderExpireMinutes" type="number" min="1" placeholder="订单超时分钟" />
        <select v-model="localConfig.autoCancel">
          <option :value="true">自动取消开启</option>
          <option :value="false">自动取消关闭</option>
        </select>
        <input v-model.number="localConfig.uploadMaxSize" type="number" min="1" placeholder="上传大小限制(字节)" />
        <button class="cta" @click="saveConfig">保存配置(本地草案)</button>
        <span>{{ configMessage }}</span>
      </div>
    </div>
  </section>
</template>

<script setup>
import { ref, computed, onMounted } from "vue";
import {
  queryOrders,
  getTodayOrderCount,
  exportOrders,
  listAdminMerchants,
  approveMerchant,
  rejectMerchant,
  listAdminPosts,
  approvePost,
  rejectPost,
  listAdminShops,
  approveShop,
  rejectShop
} from "../api/order";
import { listUsers, updateUserStatus } from "../api/user";

const dashboardMessage = ref("");
const orderMessage = ref("");
const configMessage = ref("");

const todayOrderCount = ref(0);
const orders = ref([]);
const users = ref([]);
const merchants = ref([]);
const shops = ref([]);
const posts = ref([]);

const orderQuery = ref({
  userId: null,
  shopId: null,
  orderNo: "",
  status: "",
  page: 1,
  pageSize: 50
});

const userStatusFilter = ref("");

const localConfig = ref({
  orderExpireMinutes: 30,
  autoCancel: true,
  uploadMaxSize: 10485760
});

const pendingMerchantCount = computed(() => merchants.value.filter((m) => m.status === 0).length);
const pendingShopCount = computed(() => shops.value.filter((s) => s.auditStatus === 0).length);
const pendingPostCount = computed(() => posts.value.filter((p) => p.auditStatus === 0).length);

const searchOrders = async () => {
  orderMessage.value = "";
  const payload = {
    ...orderQuery.value,
    status: orderQuery.value.status === "" ? null : Number(orderQuery.value.status)
  };
  const response = await queryOrders(payload);
  if (response.success) {
    orders.value = response.data?.records || [];
  } else {
    orderMessage.value = response.message || "查询失败";
  }
};

const downloadOrders = async () => {
  const params = {
    userId: orderQuery.value.userId || undefined,
    shopId: orderQuery.value.shopId || undefined,
    status: orderQuery.value.status === "" ? undefined : Number(orderQuery.value.status)
  };
  const response = await exportOrders(params);
  const blob = response.data;
  const url = window.URL.createObjectURL(blob);
  const link = document.createElement("a");
  link.href = url;
  link.download = `orders-${Date.now()}.csv`;
  document.body.appendChild(link);
  link.click();
  document.body.removeChild(link);
  window.URL.revokeObjectURL(url);
};

const loadUsers = async () => {
  const status = userStatusFilter.value === "" ? undefined : Number(userStatusFilter.value);
  const response = await listUsers(status);
  if (response.success) {
    users.value = response.data || [];
  }
};

const changeUserStatus = async (user) => {
  const targetStatus = user.status === 1 ? 0 : 1;
  const response = await updateUserStatus(user.id, targetStatus);
  if (response.success) {
    await loadUsers();
  }
};

const loadMerchants = async () => {
  const response = await listAdminMerchants();
  if (response.success) {
    merchants.value = response.data || [];
  }
};

const approveMerchantAction = async (merchantId) => {
  const response = await approveMerchant(merchantId);
  if (response.success) {
    await loadMerchants();
  }
};

const rejectMerchantAction = async (merchantId) => {
  const response = await rejectMerchant(merchantId);
  if (response.success) {
    await loadMerchants();
  }
};

const loadShops = async () => {
  const response = await listAdminShops();
  if (response.success) {
    shops.value = response.data || [];
  }
};

const approveShopAction = async (shopId) => {
  const response = await approveShop(shopId);
  if (response.success) {
    await loadShops();
  }
};

const rejectShopAction = async (shopId) => {
  const response = await rejectShop(shopId, "不符合规范");
  if (response.success) {
    await loadShops();
  }
};

const loadPosts = async () => {
  const response = await listAdminPosts();
  if (response.success) {
    posts.value = response.data || [];
  }
};

const approvePostAction = async (postId) => {
  const response = await approvePost(postId);
  if (response.success) {
    await loadPosts();
  }
};

const rejectPostAction = async (postId) => {
  const response = await rejectPost(postId, "内容不符合社区规范");
  if (response.success) {
    await loadPosts();
  }
};

const refreshDashboard = async () => {
  dashboardMessage.value = "";
  const countResp = await getTodayOrderCount();
  if (countResp.success) {
    todayOrderCount.value = countResp.data || 0;
  }
  await Promise.all([searchOrders(), loadUsers(), loadMerchants(), loadShops(), loadPosts()]);
  dashboardMessage.value = "已刷新";
};

const saveConfig = () => {
  localStorage.setItem("dp_admin_config_draft", JSON.stringify(localConfig.value));
  configMessage.value = "已保存到本地草案（待后端配置中心接口）";
};

onMounted(async () => {
  const cache = localStorage.getItem("dp_admin_config_draft");
  if (cache) {
    try {
      localConfig.value = { ...localConfig.value, ...JSON.parse(cache) };
    } catch {
      // ignore
    }
  }
  await refreshDashboard();
});
</script>

<style scoped>
/* ========== Stats Grid ========== */
.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
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
  font-size: var(--text-3xl);
  font-weight: var(--font-bold);
  color: var(--brand-primary);
}

.stat-value.empty {
  color: var(--text-tertiary);
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
@media (max-width: 1024px) {
  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 640px) {
  .stats-grid {
    grid-template-columns: 1fr;
  }
  
  .stat-value {
    font-size: var(--text-2xl);
  }
}
</style>

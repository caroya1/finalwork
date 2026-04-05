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

  <!-- Filter Bar -->
  <section class="filter-section">
    <div class="filter-bar">
      <div class="filter-group">
        <label>时间范围</label>
        <select v-model="filterDateRange">
          <option value="all">全部</option>
          <option value="today">今天</option>
          <option value="week">本周</option>
          <option value="month">本月</option>
        </select>
      </div>
      <div class="filter-group">
        <label>审核状态</label>
        <select v-model="filterStatus">
          <option value="pending">待审核</option>
          <option value="approved">已通过</option>
          <option value="rejected">已拒绝</option>
          <option value="all">全部</option>
        </select>
      </div>
      <div class="filter-actions">
        <button class="ghost-btn" @click="applyFilters">筛选</button>
        <button class="ghost-btn" @click="resetFilters">重置</button>
      </div>
    </div>
  </section>

  <!-- Enhanced Stats -->
  <section class="hero">
    <div class="hero-card">
      <h2>审核统计</h2>
      <div class="stats-grid">
        <div class="stat-card">
          <div class="stat-value">{{ stats.todayApproved }}</div>
          <div class="stat-label">今日通过</div>
        </div>
        <div class="stat-card">
          <div class="stat-value">{{ stats.todayRejected }}</div>
          <div class="stat-label">今日拒绝</div>
        </div>
        <div class="stat-card">
          <div class="stat-value">{{ stats.avgReviewTime }}</div>
          <div class="stat-label">平均审核时长</div>
        </div>
        <div class="stat-card">
          <div class="stat-value">{{ stats.passRate }}%</div>
          <div class="stat-label">通过率</div>
        </div>
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
      <div v-if="selectedMerchantIds.length > 0" class="batch-actions">
        <span class="batch-count">已选择 {{ selectedMerchantIds.length }} 项</span>
        <button class="cta" @click="batchApproveMerchants">批量通过</button>
        <button class="ghost-btn" @click="openRejectModal('merchant')">批量拒绝</button>
      </div>
      <div class="list">
        <div v-for="merchant in merchants" :key="merchant.id" class="list-item" @click="viewDetail(merchant, 'merchant')">
          <div class="list-item-header">
            <input
              type="checkbox"
              :value="merchant.id"
              v-model="selectedMerchantIds"
              class="batch-checkbox"
              @click.stop
            />
            <strong>{{ merchant.name }}</strong>
          </div>
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
      <div v-if="selectedShopIds.length > 0" class="batch-actions">
        <span class="batch-count">已选择 {{ selectedShopIds.length }} 项</span>
        <button class="cta" @click="batchApproveShops">批量通过</button>
        <button class="ghost-btn" @click="openRejectModal('shop')">批量拒绝</button>
      </div>
      <div class="list">
        <div v-for="shop in shops" :key="shop.id" class="list-item" @click="viewDetail(shop, 'shop')">
          <div class="list-item-header">
            <input
              type="checkbox"
              :value="shop.id"
              v-model="selectedShopIds"
              class="batch-checkbox"
              @click.stop
            />
            <strong>{{ shop.name }}</strong>
          </div>
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
      <div v-if="selectedPostIds.length > 0" class="batch-actions">
        <span class="batch-count">已选择 {{ selectedPostIds.length }} 项</span>
        <button class="cta" @click="batchApprovePosts">批量通过</button>
        <button class="ghost-btn" @click="openRejectModal('post')">批量拒绝</button>
      </div>
      <div class="list">
        <div v-for="post in posts" :key="post.id" class="list-item" @click="viewDetail(post, 'post')">
          <div class="list-item-header">
            <input
              type="checkbox"
              :value="post.id"
              v-model="selectedPostIds"
              class="batch-checkbox"
              @click.stop
            />
            <strong>{{ post.title }}</strong>
          </div>
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

  <!-- Batch Reject Modal -->
  <div v-if="showRejectModal" class="modal-overlay" @click.self="closeRejectModal">
    <div class="modal-card">
      <div class="modal-header">
        <h3>拒绝原因</h3>
        <button class="modal-close" @click="closeRejectModal">&times;</button>
      </div>
      <div class="modal-body">
        <div class="form-group">
          <label>请输入拒绝原因</label>
          <textarea
            v-model="rejectReason"
            placeholder="请输入拒绝原因..."
            rows="3"
          ></textarea>
        </div>
      </div>
      <div class="modal-footer">
        <button class="ghost-btn" @click="closeRejectModal">取消</button>
        <button class="cta danger" @click="confirmBatchReject">确认拒绝</button>
      </div>
    </div>
  </div>

  <!-- Detail Modal -->
  <div v-if="showDetailModal" class="modal-overlay" @click.self="showDetailModal = false">
    <div class="modal-card detail-modal">
      <div class="modal-header">
        <h3>{{ detailModalTitle }}</h3>
        <button class="modal-close" @click="showDetailModal = false">&times;</button>
      </div>
      <div class="modal-body">
        <div v-for="(value, key) in detailData" :key="key" class="detail-row">
          <span class="detail-label">{{ key }}</span>
          <span class="detail-value">{{ value }}</span>
        </div>
      </div>
      <div class="modal-actions">
        <button class="ghost-btn" @click="showDetailModal = false">关闭</button>
        <button class="ghost-btn" @click="rejectFromDetail" v-if="detailType === 'merchant' || detailType === 'shop' || detailType === 'post'">拒绝</button>
        <button class="cta" @click="approveFromDetail" v-if="detailType === 'merchant' && detailId">通过</button>
        <button class="cta" @click="approveShopFromDetail" v-if="detailType === 'shop' && detailId">通过</button>
        <button class="cta" @click="approvePostFromDetail" v-if="detailType === 'post' && detailId">通过</button>
      </div>
    </div>
  </div>

  <!-- Export Progress -->
  <div v-if="exporting" class="export-progress">
    <div class="progress-spinner"></div>
    <span>正在导出数据...</span>
  </div>

  <!-- Toast Notifications -->
  <div class="toast-container">
    <div v-for="toast in toasts" :key="toast.id" class="toast" :class="toast.type">
      {{ toast.message }}
    </div>
  </div>

  <!-- Operation Log Panel -->
  <div v-if="showOperationLog" class="operation-log-panel">
    <div class="log-header">
      <h3>操作日志</h3>
      <button class="close-btn" @click="showOperationLog = false">&times;</button>
    </div>
    <div class="log-content">
      <div v-if="recentLogs.length === 0" class="log-empty">暂无操作记录</div>
      <div v-for="log in recentLogs" :key="log.id" class="log-item">
        <span class="log-time">{{ log.timestamp }}</span>
        <span class="log-action">{{ log.action }}</span>
        <span class="log-status" :class="log.status">{{ log.status === 'success' ? '成功' : '失败' }}</span>
      </div>
    </div>
  </div>

  <!-- Operation Log Toggle Button -->
  <button class="log-toggle-btn" @click="showOperationLog = true" title="查看操作日志">
    📋
  </button>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from "vue";
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

// ========== Auto-Refresh Dashboard ==========
const dashboardRefreshInterval = ref(null);

const startAutoRefresh = () => {
  dashboardRefreshInterval.value = setInterval(() => {
    refreshDashboard();
  }, 30000);
};

const stopAutoRefresh = () => {
  if (dashboardRefreshInterval.value) {
    clearInterval(dashboardRefreshInterval.value);
    dashboardRefreshInterval.value = null;
  }
};

const handleVisibilityChange = () => {
  if (document.hidden) {
    stopAutoRefresh();
  } else {
    startAutoRefresh();
    refreshDashboard();
  }
};

// ========== Export Progress ==========
const exporting = ref(false);
const exportProgress = ref(0);

// ========== Toast Notification ==========
const toasts = ref([]);

const showToast = (message, type = 'success') => {
  const id = Date.now();
  toasts.value.push({ id, message, type });
  setTimeout(() => {
    toasts.value = toasts.value.filter(t => t.id !== id);
  }, 3000);
};

// ========== Operation Log ==========
const recentLogs = ref([]);
const showOperationLog = ref(false);

const addLog = (action, status) => {
  recentLogs.value.unshift({
    id: Date.now(),
    timestamp: new Date().toLocaleString('zh-CN', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit',
      second: '2-digit'
    }).replace('/', '-').replace('/', '-'),
    action,
    status
  });
  if (recentLogs.value.length > 10) {
    recentLogs.value = recentLogs.value.slice(0, 10);
  }
};

const formatTime = (timestamp) => {
  return timestamp;
};

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

// Batch selection state
const selectedMerchantIds = ref([]);
const selectedShopIds = ref([]);
const selectedPostIds = ref([]);

// Batch reject modal state
const showRejectModal = ref(false);
const rejectReason = ref("");
const rejectTargetType = ref(""); // 'merchant', 'shop', 'post'

// Detail modal state
const showDetailModal = ref(false);
const detailData = ref({});
const detailType = ref(''); // 'merchant', 'shop', 'post'
const detailId = ref(null);

const detailModalTitle = computed(() => {
  const titles = {
    'merchant': '商户详情',
    'shop': '店铺详情',
    'post': '帖子详情'
  };
  return titles[detailType.value] || '详情';
});

const localConfig = ref({
  orderExpireMinutes: 30,
  autoCancel: true,
  uploadMaxSize: 10485760
});

const pendingMerchantCount = computed(() => merchants.value.filter((m) => m.status === 0).length);
const pendingShopCount = computed(() => shops.value.filter((s) => s.auditStatus === 0).length);
const pendingPostCount = computed(() => posts.value.filter((p) => p.auditStatus === 0).length);

// Filter state
const filterDateRange = ref('all');
const filterStatus = ref('pending');

// Stats data
const stats = ref({
  todayApproved: 0,
  todayRejected: 0,
  avgReviewTime: '-',
  passRate: 0
});

// Filter functions
const applyFilters = () => {
  calculateStats();
};

const resetFilters = () => {
  filterDateRange.value = 'all';
  filterStatus.value = 'pending';
  calculateStats();
};

const calculateStats = () => {
  const today = new Date().toDateString();
  const now = new Date();
  
  let filteredMerchants = merchants.value;
  let filteredShops = shops.value;
  let filteredPosts = posts.value;
  
  // Apply date range filter
  if (filterDateRange.value === 'today') {
    filteredMerchants = filteredMerchants.filter(m => m.createTime && new Date(m.createTime).toDateString() === today);
    filteredShops = filteredShops.filter(s => s.createTime && new Date(s.createTime).toDateString() === today);
    filteredPosts = filteredPosts.filter(p => p.createTime && new Date(p.createTime).toDateString() === today);
  } else if (filterDateRange.value === 'week') {
    const weekAgo = new Date(now.getTime() - 7 * 24 * 60 * 60 * 1000);
    filteredMerchants = filteredMerchants.filter(m => m.createTime && new Date(m.createTime) >= weekAgo);
    filteredShops = filteredShops.filter(s => s.createTime && new Date(s.createTime) >= weekAgo);
    filteredPosts = filteredPosts.filter(p => p.createTime && new Date(p.createTime) >= weekAgo);
  } else if (filterDateRange.value === 'month') {
    const monthAgo = new Date(now.getTime() - 30 * 24 * 60 * 60 * 1000);
    filteredMerchants = filteredMerchants.filter(m => m.createTime && new Date(m.createTime) >= monthAgo);
    filteredShops = filteredShops.filter(s => s.createTime && new Date(s.createTime) >= monthAgo);
    filteredPosts = filteredPosts.filter(p => p.createTime && new Date(p.createTime) >= monthAgo);
  }
  
  // Apply status filter
  if (filterStatus.value !== 'all') {
    if (filterStatus.value === 'pending') {
      filteredMerchants = filteredMerchants.filter(m => m.status === 0);
      filteredShops = filteredShops.filter(s => s.auditStatus === 0);
      filteredPosts = filteredPosts.filter(p => p.auditStatus === 0);
    } else if (filterStatus.value === 'approved') {
      filteredMerchants = filteredMerchants.filter(m => m.status === 1);
      filteredShops = filteredShops.filter(s => s.auditStatus === 1);
      filteredPosts = filteredPosts.filter(p => p.auditStatus === 1);
    } else if (filterStatus.value === 'rejected') {
      filteredMerchants = filteredMerchants.filter(m => m.status === 2);
      filteredShops = filteredShops.filter(s => s.auditStatus === 2);
      filteredPosts = filteredPosts.filter(p => p.auditStatus === 2);
    }
  }
  
  // Calculate stats
  const allItems = [...filteredMerchants, ...filteredShops, ...filteredPosts];
  const approved = allItems.filter(i => 
    (i.status === 1) || (i.auditStatus === 1)
  ).length;
  const rejected = allItems.filter(i => 
    (i.status === 2) || (i.auditStatus === 2)
  ).length;
  const total = approved + rejected;
  
  stats.value.todayApproved = approved;
  stats.value.todayRejected = rejected;
  stats.value.passRate = total > 0 ? Math.round((approved / total) * 100) : 0;
  stats.value.avgReviewTime = '-';
};

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
  exporting.value = true;
  exportProgress.value = 0;
  
  try {
    const params = {
      userId: orderQuery.value.userId || undefined,
      shopId: orderQuery.value.shopId || undefined,
      status: orderQuery.value.status === "" ? undefined : Number(orderQuery.value.status)
    };
    const response = await exportOrders(params);
    exportProgress.value = 100;
    
    // Simulate delay for progress display
    await new Promise(resolve => setTimeout(150));
    exporting.value = false;
    exportProgress.value = 0;
    
    const blob = response.data;
    const url = window.URL.createObjectURL(blob);
    const link = document.createElement("a");
    link.href = url;
    link.download = `orders-${Date.now()}.csv`;
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    window.URL.revokeObjectURL(url);
    
    addLog('导出订单数据', 'success');
    showToast('导出成功');
  } catch (error) {
    console.error('Export failed:', error);
    exporting.value = false;
    exportProgress.value = 0;
    addLog('导出订单失败', 'error');
    showToast('导出失败', 'error');
  }
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

/**
 * Load pending merchants from the backend and update the merchants list.
 * @async
 * @returns {Promise<void>}
 */
const loadMerchants = async () => {
  const response = await listAdminMerchants();
  if (response.success) {
    merchants.value = response.data || [];
  }
};

// View detail handler
const viewDetail = (item, type) => {
  detailType.value = type;
  detailId.value = item.id;
  
  if (type === 'merchant') {
    detailData.value = {
      'ID': item.id,
      '名称': item.name || '-',
      '邮箱': item.email || '-',
      '电话': item.phone || '-',
      '状态': item.status === 0 ? '待审核' : item.status === 1 ? '已通过' : '已拒绝',
      '创建时间': item.createdAt || '-',
      '备注': item.remark || '-'
    };
  } else if (type === 'shop') {
    detailData.value = {
      'ID': item.id,
      '名称': item.name || '-',
      '商户ID': item.merchantId || '-',
      '城市': item.city || '-',
      '地址': item.address || '-',
      '审核状态': item.auditStatus === 0 ? '待审核' : item.auditStatus === 1 ? '已通过' : '已拒绝',
      '创建时间': item.createdAt || '-'
    };
  } else if (type === 'post') {
    detailData.value = {
      'ID': item.id,
      '标题': item.title || '-',
      '用户ID': item.userId || '-',
      '店铺ID': item.shopId || '-',
      '内容': item.content?.substring(0, 100) + (item.content?.length > 100 ? '...' : '') || '-',
      '审核状态': item.auditStatus === 0 ? '待审核' : item.auditStatus === 1 ? '已通过' : '已拒绝',
      '创建时间': item.createdAt || '-'
    };
  }
  
  showDetailModal.value = true;
};

// Detail modal action handlers
const approveFromDetail = async () => {
  if (detailType.value === 'merchant' && detailId.value) {
    await approveMerchantAction(detailId.value);
  } else if (detailType.value === 'shop' && detailId.value) {
    await approveShopAction(detailId.value);
  } else if (detailType.value === 'post' && detailId.value) {
    await approvePostAction(detailId.value);
  }
  showDetailModal.value = false;
};

const rejectFromDetail = () => {
  if (detailType.value === 'merchant') {
    selectedMerchantIds.value = [detailId.value];
  } else if (detailType.value === 'shop') {
    selectedShopIds.value = [detailId.value];
  } else if (detailType.value === 'post') {
    selectedPostIds.value = [detailId.value];
  }
  showDetailModal.value = false;
  openRejectModal(detailType.value);
};

const approveShopFromDetail = approveFromDetail;
const approvePostFromDetail = approveFromDetail;

const approveMerchantAction = async (merchantId) => {
  const response = await approveMerchant(merchantId);
  if (response.success) {
    await loadMerchants();
    addLog(`通过商户 ${merchantId}`, 'success');
  }
};

const rejectMerchantAction = async (merchantId) => {
  const response = await rejectMerchant(merchantId);
  if (response.success) {
    await loadMerchants();
    addLog(`拒绝商户 ${merchantId}`, 'success');
  }
};

/**
 * Load pending shops from the backend and update the shops list.
 * @async
 * @returns {Promise<void>}
 */
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
    addLog(`通过店铺 ${shopId}`, 'success');
  }
};

const rejectShopAction = async (shopId) => {
  const response = await rejectShop(shopId, "不符合规范");
  if (response.success) {
    await loadShops();
    addLog(`拒绝店铺 ${shopId}`, 'success');
  }
};

/**
 * Load pending posts from the backend and update the posts list.
 * @async
 * @returns {Promise<void>}
 */
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
    addLog(`通过帖子 ${postId}`, 'success');
  }
};

const rejectPostAction = async (postId) => {
  const response = await rejectPost(postId, "内容不符合社区规范");
  if (response.success) {
    await loadPosts();
    addLog(`拒绝帖子 ${postId}`, 'success');
  }
};

// Batch reject modal handlers
const openRejectModal = (type) => {
  rejectTargetType.value = type;
  rejectReason.value = "";
  showRejectModal.value = true;
};

const closeRejectModal = () => {
  showRejectModal.value = false;
  rejectReason.value = "";
  rejectTargetType.value = "";
};

// Batch approve functions
const batchApproveMerchants = async () => {
  for (const id of selectedMerchantIds.value) {
    await approveMerchant(id);
  }
  selectedMerchantIds.value = [];
  await loadMerchants();
};

const batchApproveShops = async () => {
  for (const id of selectedShopIds.value) {
    await approveShop(id);
  }
  selectedShopIds.value = [];
  await loadShops();
};

const batchApprovePosts = async () => {
  for (const id of selectedPostIds.value) {
    await approvePost(id);
  }
  selectedPostIds.value = [];
  await loadPosts();
};

// Batch reject functions
const confirmBatchReject = async () => {
  if (!rejectReason.value.trim()) {
    return;
  }

  if (rejectTargetType.value === "merchant") {
    for (const id of selectedMerchantIds.value) {
      await rejectMerchant(id, rejectReason.value);
    }
    selectedMerchantIds.value = [];
    await loadMerchants();
  } else if (rejectTargetType.value === "shop") {
    for (const id of selectedShopIds.value) {
      await rejectShop(id, rejectReason.value);
    }
    selectedShopIds.value = [];
    await loadShops();
  } else if (rejectTargetType.value === "post") {
    for (const id of selectedPostIds.value) {
      await rejectPost(id, rejectReason.value);
    }
    selectedPostIds.value = [];
    await loadPosts();
  }

  closeRejectModal();
};

const refreshDashboard = async () => {
  dashboardMessage.value = "";
  const countResp = await getTodayOrderCount();
  if (countResp.success) {
    todayOrderCount.value = countResp.data || 0;
  }
  await Promise.all([searchOrders(), loadUsers(), loadMerchants(), loadShops(), loadPosts()]);
  calculateStats();
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
  calculateStats();
  window.addEventListener('keydown', handleKeyboard);
  
  // Start auto-refresh
  startAutoRefresh();
  document.addEventListener('visibilitychange', handleVisibilityChange);
});

onUnmounted(() => {
  window.removeEventListener('keydown', handleKeyboard);
  stopAutoRefresh();
  document.removeEventListener('visibilitychange', handleVisibilityChange);
});

// Keyboard shortcuts: Y=approve first pending, N=reject first pending, ESC=close modal
const handleKeyboard = (e) => {
  // Y = 通过第一个待审核项
  if (e.key === 'y' || e.key === 'Y') {
    if (pendingMerchants.value.length > 0) {
      approveMerchantAction(pendingMerchants.value[0].id);
    } else if (pendingShops.value.length > 0) {
      approveShopAction(pendingShops.value[0].id);
    } else if (pendingPosts.value.length > 0) {
      approvePostAction(pendingPosts.value[0].id);
    }
  }
  // N = 拒绝第一个待审核项（打开拒绝弹窗）
  if (e.key === 'n' || e.key === 'N') {
    if (pendingMerchants.value.length > 0) {
      selectedMerchantIds.value = [pendingMerchants.value[0].id];
      openRejectModal('merchant');
    } else if (pendingShops.value.length > 0) {
      selectedShopIds.value = [pendingShops.value[0].id];
      openRejectModal('shop');
    } else if (pendingPosts.value.length > 0) {
      selectedPostIds.value = [pendingPosts.value[0].id];
      openRejectModal('post');
    }
  }
  // ESC = 关闭弹窗
  if (e.key === 'Escape') {
    showDetailModal.value = false;
    showRejectModal.value = false;
  }
};

const pendingMerchants = computed(() => merchants.value.filter((m) => m.status === 0));
const pendingShops = computed(() => shops.value.filter((s) => s.auditStatus === 0));
const pendingPosts = computed(() => posts.value.filter((p) => p.auditStatus === 0));
</script>

<style scoped>
/* ========== Filter Section ========== */
.filter-section {
  margin-bottom: var(--space-4);
}

.filter-bar {
  display: flex;
  gap: var(--space-4);
  padding: var(--space-4);
  background: var(--bg-secondary);
  border-radius: var(--radius-lg);
  flex-wrap: wrap;
  align-items: flex-end;
}

.filter-group {
  display: flex;
  flex-direction: column;
  gap: var(--space-1);
}

.filter-group label {
  font-size: var(--text-xs);
  color: var(--text-tertiary);
  font-weight: var(--font-medium);
}

.filter-group select {
  padding: var(--space-2) var(--space-3);
  border: 1px solid var(--border-default);
  border-radius: var(--radius-md);
  background: var(--bg-primary);
  color: var(--text-primary);
  font-size: var(--text-sm);
  min-width: 120px;
  cursor: pointer;
  transition: all var(--transition-fast);
}

.filter-group select:hover {
  border-color: var(--brand-primary);
}

.filter-group select:focus {
  outline: none;
  border-color: var(--brand-primary);
  box-shadow: 0 0 0 2px var(--brand-primary-light);
}

.filter-actions {
  display: flex;
  gap: var(--space-2);
}

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

/* ========== Stat Card (Enhanced Stats) ========== */
.stat-card {
  background: var(--bg-primary);
  padding: var(--space-4);
  border-radius: var(--radius-lg);
  text-align: center;
  border: 1px solid var(--border-light);
  transition: all var(--transition-fast);
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-md);
  border-color: var(--brand-primary);
}

.stat-card .stat-value {
  font-size: var(--text-2xl);
  font-weight: var(--font-bold);
  color: var(--brand-primary);
}

.stat-card .stat-label {
  font-size: var(--text-sm);
  color: var(--text-tertiary);
  margin-top: var(--space-1);
  text-transform: none;
  letter-spacing: normal;
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

/* ========== Batch Actions ========== */
.batch-actions {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  padding: var(--space-3) var(--space-4);
  margin-bottom: var(--space-4);
  background: var(--bg-secondary);
  border-radius: var(--radius-lg);
  border: 1px solid var(--border-light);
  animation: slideDown 200ms ease;
}

.batch-count {
  font-size: var(--text-sm);
  font-weight: var(--font-medium);
  color: var(--text-secondary);
  margin-right: auto;
}

.batch-actions .cta,
.batch-actions .ghost-btn {
  font-size: var(--text-xs);
  padding: var(--space-2) var(--space-3);
}

/* ========== List Item Header with Checkbox ========== */
.list-item-header {
  display: flex;
  align-items: center;
  gap: var(--space-3);
}

.batch-checkbox {
  width: 16px;
  height: 16px;
  cursor: pointer;
  accent-color: var(--brand-primary);
}

/* ========== Modal Styles ========== */
.modal-overlay {
  position: fixed;
  inset: 0;
  background: var(--bg-overlay);
  backdrop-filter: blur(4px);
  display: grid;
  place-items: center;
  z-index: var(--z-modal);
  animation: fadeIn 200ms ease;
}

.modal-card {
  background: var(--bg-primary);
  border-radius: var(--radius-xl);
  width: 420px;
  max-width: 90vw;
  box-shadow: var(--shadow-xl);
  animation: slideUp 300ms ease;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: var(--space-5) var(--space-6);
  border-bottom: 1px solid var(--border-light);
}

.modal-header h3 {
  font-size: var(--text-lg);
  font-weight: var(--font-semibold);
  color: var(--text-primary);
  margin: 0;
}

.modal-close {
  width: 32px;
  height: 32px;
  border: none;
  border-radius: var(--radius-md);
  background: var(--bg-secondary);
  color: var(--text-tertiary);
  font-size: var(--text-xl);
  cursor: pointer;
  transition: all var(--transition-fast);
}

.modal-close:hover {
  background: var(--bg-tertiary);
  color: var(--text-primary);
}

.modal-body {
  padding: var(--space-5) var(--space-6);
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: var(--space-2);
}

.form-group label {
  font-size: var(--text-sm);
  font-weight: var(--font-medium);
  color: var(--text-secondary);
}

.form-group textarea {
  width: 100%;
  padding: var(--space-3);
  border: 1px solid var(--border-default);
  border-radius: var(--radius-lg);
  font-size: var(--text-sm);
  color: var(--text-primary);
  background: var(--bg-primary);
  resize: vertical;
  transition: all var(--transition-fast);
}

.form-group textarea:hover {
  border-color: var(--neutral-400);
}

.form-group textarea:focus {
  outline: none;
  border-color: var(--brand-primary);
  box-shadow: 0 0 0 3px var(--brand-primary-light);
}

.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: var(--space-3);
  padding: var(--space-4) var(--space-6);
  border-top: 1px solid var(--border-light);
}

.cta.danger {
  background: linear-gradient(135deg, #EF4444 0%, #DC2626 100%);
}

.cta.danger:hover {
  box-shadow: 0 4px 14px -3px rgba(239, 68, 68, 0.4);
}

/* ========== Detail Modal ========== */
.detail-modal {
  max-width: 600px;
  max-height: 80vh;
  overflow-y: auto;
}

.modal-actions {
  display: flex;
  justify-content: flex-end;
  gap: var(--space-3);
  padding: var(--space-4) var(--space-6);
  border-top: 1px solid var(--border-light);
}

.detail-row {
  display: flex;
  padding: var(--space-3) 0;
  border-bottom: 1px solid var(--border-light);
}

.detail-label {
  flex: 0 0 120px;
  font-weight: var(--font-medium);
  color: var(--text-secondary);
}

.detail-value {
  flex: 1;
  color: var(--text-primary);
  word-break: break-all;
}

/* ========== Animations ========== */
@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

@keyframes slideUp {
  from {
    opacity: 0;
    transform: translateY(20px) scale(0.96);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

@keyframes slideDown {
  from {
    opacity: 0;
    transform: translateY(-10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
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
  
  .filter-bar {
    flex-direction: column;
    align-items: stretch;
  }
  
  .filter-group {
    width: 100%;
  }
  
  .filter-group select {
    width: 100%;
  }
  
  .batch-actions {
    flex-wrap: wrap;
  }
  
  .panel-grid {
    grid-template-columns: 1fr;
  }
  
  .hero {
    padding: var(--space-4);
  }
  
  .form-grid {
    grid-template-columns: 1fr;
  }
  
  .hero-card {
    padding: var(--space-4);
  }
  
  .detail-modal {
    width: 95vw;
    max-height: 90vh;
  }
}

/* ========== Export Progress ========== */
.export-progress {
  position: fixed;
  bottom: 24px;
  right: 24px;
  display: flex;
  align-items: center;
  gap: var(--space-3);
  padding: var(--space-3) var(--space-4);
  background: var(--bg-primary);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-lg);
  border: 1px solid var(--border-light);
  z-index: var(--z-toast);
  animation: slideUp 300ms ease;
}

.progress-spinner {
  width: 20px;
  height: 20px;
  border: 2px solid var(--border-light);
  border-top-color: var(--brand-primary);
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

/* ========== Toast Notifications ========== */
.toast-container {
  position: fixed;
  top: 20px;
  right: 20px;
  z-index: var(--z-toast);
  display: flex;
  flex-direction: column;
  gap: var(--space-2);
}

.toast {
  padding: var(--space-3) var(--space-4);
  border-radius: var(--radius-lg);
  background: var(--bg-primary);
  border: 1px solid var(--border-light);
  box-shadow: var(--shadow-lg);
  animation: slideInRight 300ms ease;
  font-size: var(--text-sm);
}

.toast.success {
  border-color: var(--success);
  background: linear-gradient(135deg, var(--success-light) 0%, var(--bg-primary) 100%);
}

.toast.error {
  border-color: var(--error);
  background: linear-gradient(135deg, #FEE2E2 0%, var(--bg-primary) 100%);
}

@keyframes slideInRight {
  from {
    opacity: 0;
    transform: translateX(100px);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

/* ========== Operation Log Panel ========== */
.operation-log-panel {
  position: fixed;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 480px;
  max-width: 90vw;
  max-height: 60vh;
  background: var(--bg-primary);
  border-radius: var(--radius-xl);
  box-shadow: var(--shadow-xl);
  z-index: var(--z-modal);
  animation: slideUp 300ms ease;
  display: flex;
  flex-direction: column;
}

.log-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: var(--space-4) var(--space-5);
  border-bottom: 1px solid var(--border-light);
}

.log-header h3 {
  margin: 0;
  font-size: var(--text-lg);
  font-weight: var(--font-semibold);
}

.close-btn {
  width: 28px;
  height: 28px;
  border: none;
  border-radius: var(--radius-md);
  background: var(--bg-secondary);
  color: var(--text-tertiary);
  font-size: var(--text-lg);
  cursor: pointer;
  transition: all var(--transition-fast);
}

.close-btn:hover {
  background: var(--bg-tertiary);
  color: var(--text-primary);
}

.log-content {
  flex: 1;
  overflow-y: auto;
  padding: var(--space-4);
}

.log-empty {
  text-align: center;
  color: var(--text-tertiary);
  padding: var(--space-8);
  font-size: var(--text-sm);
}

.log-item {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  padding: var(--space-3);
  border-bottom: 1px solid var(--border-light);
  font-size: var(--text-sm);
}

.log-item:last-child {
  border-bottom: none;
}

.log-time {
  color: var(--text-tertiary);
  font-size: var(--text-xs);
  flex-shrink: 0;
}

.log-action {
  flex: 1;
  color: var(--text-primary);
}

.log-status {
  padding: 2px 8px;
  border-radius: var(--radius-full);
  font-size: var(--text-xs);
  font-weight: var(--font-medium);
}

.log-status.success {
  background: #DCFCE7;
  color: #166534;
}

.log-status.error {
  background: #FEE2E2;
  color: #991B1B;
}

/* ========== Log Toggle Button ========== */
.log-toggle-btn {
  position: fixed;
  bottom: 24px;
  left: 24px;
  width: 48px;
  height: 48px;
  border: none;
  border-radius: var(--radius-full);
  background: var(--brand-primary);
  color: white;
  font-size: var(--text-xl);
  cursor: pointer;
  box-shadow: var(--shadow-lg);
  transition: all var(--transition-fast);
  z-index: var(--z-toast);
}

.log-toggle-btn:hover {
  transform: scale(1.1);
  box-shadow: var(--shadow-xl);
}

@media (max-width: 640px) {
  .export-progress {
    bottom: 16px;
    right: 16px;
    left: 16px;
    justify-content: center;
  }
  
  .toast-container {
    top: 16px;
    right: 16px;
    left: 16px;
  }
  
  .operation-log-panel {
    width: 95vw;
    max-height: 70vh;
  }
  
  .log-toggle-btn {
    bottom: 16px;
    left: 16px;
    width: 40px;
    height: 40px;
    font-size: var(--text-lg);
  }
}
</style>

<template>
  <div class="shop-audit">
    <!-- 页面标题 -->
    <div class="page-header">
      <h1>店铺审核</h1>
      <p class="subtitle">审核店铺信息，确保内容合规</p>
    </div>

    <!-- 统计卡片 -->
    <section class="stats-section">
      <div class="stats-grid">
        <div class="stat-card">
          <div class="stat-value">{{ pendingCount }}</div>
          <div class="stat-label">待审核</div>
        </div>
        <div class="stat-card">
          <div class="stat-value">{{ approvedCount }}</div>
          <div class="stat-label">已通过</div>
        </div>
        <div class="stat-card">
          <div class="stat-value">{{ rejectedCount }}</div>
          <div class="stat-label">已拒绝</div>
        </div>
        <div class="stat-card">
          <div class="stat-value">{{ shops.length }}</div>
          <div class="stat-label">总店铺数</div>
        </div>
      </div>
    </section>

    <!-- 筛选栏 -->
    <section class="filter-section">
      <div class="filter-bar">
        <div class="filter-group">
          <label>审核状态</label>
          <select v-model="filterStatus">
            <option value="all">全部</option>
            <option value="0">待审核</option>
            <option value="1">已通过</option>
            <option value="2">已拒绝</option>
          </select>
        </div>
        <div class="filter-group">
          <label>所在城市</label>
          <select v-model="filterCity">
            <option value="all">全部城市</option>
            <option v-for="city in cities" :key="city" :value="city">{{ city }}</option>
          </select>
        </div>
        <div class="filter-actions">
          <button class="btn btn-primary" @click="loadShops">刷新列表</button>
        </div>
      </div>
    </section>

    <!-- 批量操作 -->
    <div v-if="selectedIds.length > 0" class="batch-actions">
      <span class="batch-count">已选择 {{ selectedIds.length }} 项</span>
      <button class="btn btn-success" @click="batchApprove">批量通过</button>
      <button class="btn btn-danger" @click="openRejectModal">批量拒绝</button>
      <button class="btn btn-ghost" @click="selectedIds = []">取消选择</button>
    </div>

    <!-- 店铺列表 -->
    <section class="list-section">
      <div class="card">
        <div class="card-header">
          <h2>店铺列表</h2>
        </div>
        <div class="shop-list">
          <div 
            v-for="shop in filteredShops" 
            :key="shop.id" 
            class="shop-item"
            @click="viewDetail(shop)"
          >
            <div class="item-checkbox" @click.stop>
              <input
                type="checkbox"
                :value="shop.id"
                v-model="selectedIds"
              />
            </div>
            <div class="item-icon">🏪</div>
            <div class="item-content">
              <div class="item-main">
                <strong>{{ shop.name }}</strong>
                <span class="status-badge" :class="getStatusClass(shop.auditStatus)">
                  {{ getStatusText(shop.auditStatus) }}
                </span>
              </div>
              <div class="item-meta">
                <span>👤 商户ID: {{ shop.merchantId }}</span>
                <span>📍 {{ shop.city || '-' }}</span>
                <span>🏷️ {{ shop.category || '-' }}</span>
                <span>🕐 {{ formatTime(shop.createdAt) }}</span>
              </div>
              <div class="item-address" v-if="shop.address">
                📍 {{ shop.address }}
              </div>
            </div>
            <div class="item-actions" @click.stop>
              <button 
                v-if="shop.auditStatus === 0" 
                class="btn btn-sm btn-success"
                @click="approveShop(shop.id)"
              >
                通过
              </button>
              <button 
                v-if="shop.auditStatus === 0" 
                class="btn btn-sm btn-danger"
                @click="openRejectModal(shop.id)"
              >
                拒绝
              </button>
              <button class="btn btn-sm btn-ghost" @click="viewDetail(shop)">
                详情
              </button>
            </div>
          </div>
          <div v-if="filteredShops.length === 0" class="empty-state">
            暂无店铺数据
          </div>
        </div>
      </div>
    </section>

    <!-- 拒绝弹窗 -->
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
          <button class="btn btn-ghost" @click="closeRejectModal">取消</button>
          <button class="btn btn-danger" @click="confirmReject">确认拒绝</button>
        </div>
      </div>
    </div>

    <!-- 详情弹窗 -->
    <div v-if="showDetailModal" class="modal-overlay" @click.self="closeDetailModal">
      <div class="modal-card detail-modal">
        <div class="modal-header">
          <h3>店铺详情</h3>
          <button class="modal-close" @click="closeDetailModal">&times;</button>
        </div>
        <div class="modal-body">
          <div class="detail-row">
            <span class="detail-label">店铺ID</span>
            <span class="detail-value">{{ detailData.id }}</span>
          </div>
          <div class="detail-row">
            <span class="detail-label">店铺名称</span>
            <span class="detail-value">{{ detailData.name }}</span>
          </div>
          <div class="detail-row">
            <span class="detail-label">所属商户</span>
            <span class="detail-value">商户ID: {{ detailData.merchantId }}</span>
          </div>
          <div class="detail-row">
            <span class="detail-label">城市</span>
            <span class="detail-value">{{ detailData.city || '-' }}</span>
          </div>
          <div class="detail-row">
            <span class="detail-label">地址</span>
            <span class="detail-value">{{ detailData.address || '-' }}</span>
          </div>
          <div class="detail-row">
            <span class="detail-label">分类</span>
            <span class="detail-value">{{ detailData.category || '-' }}</span>
          </div>
          <div class="detail-row">
            <span class="detail-label">审核状态</span>
            <span class="detail-value">
              <span class="status-badge" :class="getStatusClass(detailData.auditStatus)">
                {{ getStatusText(detailData.auditStatus) }}
              </span>
            </span>
          </div>
          <div class="detail-row">
            <span class="detail-label">创建时间</span>
            <span class="detail-value">{{ detailData.createdAt }}</span>
          </div>
        </div>
        <div class="modal-footer">
          <button class="btn btn-ghost" @click="closeDetailModal">关闭</button>
          <template v-if="detailData.auditStatus === 0">
            <button class="btn btn-danger" @click="rejectFromDetail">拒绝</button>
            <button class="btn btn-success" @click="approveFromDetail">通过</button>
          </template>
        </div>
      </div>
    </div>

    <!-- Toast -->
    <div class="toast-container">
      <div v-for="toast in toasts" :key="toast.id" class="toast" :class="toast.type">
        {{ toast.message }}
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from "vue";
import {
  listAdminShops,
  approveShop,
  rejectShop
} from "../api/order";

// ========== State ==========
const shops = ref([]);
const selectedIds = ref([]);
const filterStatus = ref("all");
const filterCity = ref("all");
const toasts = ref([]);

// Modal state
const showRejectModal = ref(false);
const showDetailModal = ref(false);
const rejectReason = ref("");
const rejectTargetId = ref(null);
const detailData = ref({});

// ========== Computed ==========
const pendingCount = computed(() => shops.value.filter(s => s.auditStatus === 0).length);
const approvedCount = computed(() => shops.value.filter(s => s.auditStatus === 1).length);
const rejectedCount = computed(() => shops.value.filter(s => s.auditStatus === 2).length);

const cities = computed(() => {
  const citySet = new Set(shops.value.map(s => s.city).filter(Boolean));
  return Array.from(citySet).sort();
});

const filteredShops = computed(() => {
  let result = shops.value;
  
  if (filterStatus.value !== "all") {
    result = result.filter(s => s.auditStatus === Number(filterStatus.value));
  }
  
  if (filterCity.value !== "all") {
    result = result.filter(s => s.city === filterCity.value);
  }
  
  return result;
});

// ========== Methods ==========
const showToast = (message, type = 'success') => {
  const id = Date.now();
  toasts.value.push({ id, message, type });
  setTimeout(() => {
    toasts.value = toasts.value.filter(t => t.id !== id);
  }, 3000);
};

const getStatusText = (status) => {
  const map = { 0: '待审核', 1: '已通过', 2: '已拒绝' };
  return map[status] || '未知';
};

const getStatusClass = (status) => {
  const map = { 0: 'pending', 1: 'success', 2: 'danger' };
  return map[status] || '';
};

const formatTime = (time) => {
  if (!time) return '-';
  return new Date(time).toLocaleString('zh-CN');
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
    showToast('店铺已通过');
    await loadShops();
  } else {
    showToast(response.message || '操作失败', 'error');
  }
};

const openRejectModal = (shopId = null) => {
  rejectTargetId.value = shopId;
  rejectReason.value = "";
  showRejectModal.value = true;
};

const closeRejectModal = () => {
  showRejectModal.value = false;
  rejectReason.value = "";
  rejectTargetId.value = null;
};

const confirmReject = async () => {
  if (!rejectReason.value.trim()) {
    showToast('请输入拒绝原因', 'error');
    return;
  }

  if (rejectTargetId.value) {
    // 单个拒绝
    const response = await rejectShop(rejectTargetId.value, rejectReason.value);
    if (response.success) {
      showToast('店铺已拒绝');
      await loadShops();
    }
  } else if (selectedIds.value.length > 0) {
    // 批量拒绝
    for (const id of selectedIds.value) {
      await rejectShop(id, rejectReason.value);
    }
    showToast(`已拒绝 ${selectedIds.value.length} 个店铺`);
    selectedIds.value = [];
    await loadShops();
  }

  closeRejectModal();
};

const batchApprove = async () => {
  for (const id of selectedIds.value) {
    await approveShop(id);
  }
  showToast(`已通过 ${selectedIds.value.length} 个店铺`);
  selectedIds.value = [];
  await loadShops();
};

const viewDetail = (shop) => {
  detailData.value = { ...shop };
  showDetailModal.value = true;
};

const closeDetailModal = () => {
  showDetailModal.value = false;
  detailData.value = {};
};

const approveFromDetail = async () => {
  await approveShopAction(detailData.value.id);
  closeDetailModal();
};

const rejectFromDetail = () => {
  const id = detailData.value.id;
  closeDetailModal();
  openRejectModal(id);
};

// ========== Lifecycle ==========
onMounted(() => {
  loadShops();
});
</script>

<style scoped>
.shop-audit {
  max-width: 1200px;
  margin: 0 auto;
}

.page-header {
  margin-bottom: 24px;
}

.page-header h1 {
  margin: 0 0 8px 0;
  font-size: 1.75rem;
  font-weight: 600;
  color: #1a1a2e;
}

.subtitle {
  margin: 0;
  color: #666;
  font-size: 0.95rem;
}

/* Stats */
.stats-section {
  margin-bottom: 24px;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}

.stat-card {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  text-align: center;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.stat-value {
  font-size: 2rem;
  font-weight: 700;
  color: #1a1a2e;
}

.stat-label {
  font-size: 0.875rem;
  color: #666;
  margin-top: 4px;
}

/* Filter */
.filter-section {
  margin-bottom: 16px;
}

.filter-bar {
  display: flex;
  gap: 16px;
  align-items: flex-end;
  background: #fff;
  padding: 16px 20px;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  flex-wrap: wrap;
}

.filter-group {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.filter-group label {
  font-size: 0.8rem;
  color: #666;
  font-weight: 500;
}

.filter-group select {
  padding: 8px 12px;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  background: #fff;
  font-size: 0.9rem;
  min-width: 120px;
}

/* Batch Actions */
.batch-actions {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  margin-bottom: 16px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.batch-count {
  font-size: 0.9rem;
  color: #666;
  margin-right: auto;
}

/* Buttons */
.btn {
  padding: 8px 16px;
  border-radius: 8px;
  border: none;
  font-size: 0.875rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-sm {
  padding: 6px 12px;
  font-size: 0.8rem;
}

.btn-primary {
  background: #ff6b35;
  color: #fff;
}

.btn-primary:hover {
  background: #e55a2b;
}

.btn-success {
  background: #22c55e;
  color: #fff;
}

.btn-success:hover {
  background: #16a34a;
}

.btn-danger {
  background: #ef4444;
  color: #fff;
}

.btn-danger:hover {
  background: #dc2626;
}

.btn-ghost {
  background: #f3f4f6;
  color: #374151;
}

.btn-ghost:hover {
  background: #e5e7eb;
}

/* List Section */
.list-section {
  margin-bottom: 24px;
}

.card {
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  overflow: hidden;
}

.card-header {
  padding: 16px 20px;
  border-bottom: 1px solid #f0f1f2;
}

.card-header h2 {
  margin: 0;
  font-size: 1.1rem;
  font-weight: 600;
  color: #1a1a2e;
}

.shop-list {
  padding: 8px;
}

.shop-item {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 16px;
  border-radius: 10px;
  cursor: pointer;
  transition: background 0.2s;
}

.shop-item:hover {
  background: #f9fafb;
}

.item-checkbox {
  flex-shrink: 0;
  padding-top: 4px;
}

.item-checkbox input {
  width: 18px;
  height: 18px;
  cursor: pointer;
  accent-color: #ff6b35;
}

.item-icon {
  font-size: 1.5rem;
  flex-shrink: 0;
}

.item-content {
  flex: 1;
  min-width: 0;
}

.item-main {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 6px;
  flex-wrap: wrap;
}

.item-main strong {
  font-size: 1rem;
  color: #1a1a2e;
}

.item-meta {
  display: flex;
  gap: 16px;
  font-size: 0.8rem;
  color: #666;
  flex-wrap: wrap;
}

.item-address {
  font-size: 0.8rem;
  color: #888;
  margin-top: 4px;
}

.item-actions {
  display: flex;
  gap: 8px;
  flex-shrink: 0;
  padding-top: 4px;
}

/* Status Badge */
.status-badge {
  padding: 4px 10px;
  border-radius: 12px;
  font-size: 0.75rem;
  font-weight: 500;
}

.status-badge.pending {
  background: #fef3c7;
  color: #92400e;
}

.status-badge.success {
  background: #d1fae5;
  color: #065f46;
}

.status-badge.danger {
  background: #fee2e2;
  color: #991b1b;
}

/* Empty State */
.empty-state {
  text-align: center;
  padding: 48px;
  color: #999;
  font-size: 0.9rem;
}

/* Modal */
.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal-card {
  background: #fff;
  border-radius: 16px;
  width: 480px;
  max-width: 90vw;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
}

.detail-modal {
  width: 540px;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px;
  border-bottom: 1px solid #f0f1f2;
}

.modal-header h3 {
  margin: 0;
  font-size: 1.1rem;
  font-weight: 600;
}

.modal-close {
  width: 32px;
  height: 32px;
  border: none;
  border-radius: 8px;
  background: #f3f4f6;
  color: #666;
  font-size: 1.5rem;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
}

.modal-close:hover {
  background: #e5e7eb;
}

.modal-body {
  padding: 20px 24px;
  max-height: 60vh;
  overflow-y: auto;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.form-group label {
  font-size: 0.875rem;
  font-weight: 500;
  color: #374151;
}

.form-group textarea {
  width: 100%;
  padding: 12px;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  font-size: 0.9rem;
  resize: vertical;
  min-height: 80px;
}

.form-group textarea:focus {
  outline: none;
  border-color: #ff6b35;
  box-shadow: 0 0 0 3px rgba(255, 107, 53, 0.1);
}

.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding: 16px 24px;
  border-top: 1px solid #f0f1f2;
}

/* Detail Rows */
.detail-row {
  display: flex;
  padding: 12px 0;
  border-bottom: 1px solid #f0f1f2;
}

.detail-row:last-child {
  border-bottom: none;
}

.detail-label {
  flex: 0 0 100px;
  font-weight: 500;
  color: #666;
  font-size: 0.9rem;
}

.detail-value {
  flex: 1;
  color: #1a1a2e;
  font-size: 0.9rem;
}

/* Toast */
.toast-container {
  position: fixed;
  top: 20px;
  right: 20px;
  z-index: 1100;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.toast {
  padding: 12px 20px;
  border-radius: 8px;
  background: #fff;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  animation: slideIn 0.3s ease;
  font-size: 0.9rem;
}

.toast.success {
  border-left: 4px solid #22c55e;
}

.toast.error {
  border-left: 4px solid #ef4444;
}

@keyframes slideIn {
  from {
    opacity: 0;
    transform: translateX(100px);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

/* Responsive */
@media (max-width: 1024px) {
  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 640px) {
  .stats-grid {
    grid-template-columns: 1fr;
  }
  
  .filter-bar {
    flex-direction: column;
    align-items: stretch;
  }
  
  .shop-item {
    flex-direction: column;
    align-items: flex-start;
  }
  
  .item-actions {
    width: 100%;
    justify-content: flex-end;
  }
  
  .item-meta {
    gap: 8px;
  }
}
</style>

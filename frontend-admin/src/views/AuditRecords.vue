<template>
  <div class="audit-records">
    <!-- Header Section -->
    <section class="page-header">
      <div class="header-content">
        <h1>审核记录</h1>
        <p>查看和管理平台内容审核记录</p>
      </div>
      <div class="header-stats">
        <div class="stat-card">
          <span class="stat-number">{{ stats.pending }}</span>
          <span class="stat-label">待审核</span>
        </div>
        <div class="stat-card approved">
          <span class="stat-number">{{ stats.approved }}</span>
          <span class="stat-label">已通过</span>
        </div>
        <div class="stat-card rejected">
          <span class="stat-number">{{ stats.rejected }}</span>
          <span class="stat-label">已拒绝</span>
        </div>
      </div>
    </section>

    <!-- Filters Section -->
    <section class="filters-section">
      <div class="filters-grid">
        <div class="filter-group">
          <label>类型</label>
          <select v-model="filters.type" class="filter-select">
            <option value="">全部类型</option>
            <option value="post">帖子</option>
            <option value="comment">评论</option>
            <option value="shop">店铺</option>
            <option value="merchant">商户</option>
          </select>
        </div>
        <div class="filter-group">
          <label>审核状态</label>
          <select v-model="filters.status" class="filter-select">
            <option value="">全部状态</option>
            <option value="0">待审核</option>
            <option value="1">已通过</option>
            <option value="2">已拒绝</option>
          </select>
        </div>
        <div class="filter-actions">
          <button class="cta" @click="loadAuditRecords">
            <span class="btn-icon">🔍</span>
            查询
          </button>
          <button class="ghost-btn" @click="resetFilters">
            <span class="btn-icon">↺</span>
            重置
          </button>
        </div>
      </div>
    </section>

    <!-- Table Section -->
    <section class="table-section">
      <div class="table-container">
        <table class="audit-table">
          <thead>
            <tr>
              <th class="col-type">类型</th>
              <th class="col-content">内容</th>
              <th class="col-status">状态</th>
              <th class="col-reason">审核备注</th>
              <th class="col-time">审核时间</th>
              <th class="col-actions">操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="record in auditRecords" :key="record.id" class="table-row">
              <td class="col-type">
                <span class="type-badge" :class="record.type">
                  {{ getTypeLabel(record.type) }}
                </span>
              </td>
              <td class="col-content">
                <div class="content-preview">
                  <span class="content-id">#{{ record.entityId }}</span>
                  <span class="content-text" :title="record.content">
                    {{ truncateContent(record.content) }}
                  </span>
                </div>
              </td>
              <td class="col-status">
                <span class="status-badge" :class="getStatusClass(record.status)">
                  {{ getStatusLabel(record.status) }}
                </span>
              </td>
              <td class="col-reason">
                <span class="reason-text" :title="record.reason">
                  {{ record.reason || '-' }}
                </span>
              </td>
              <td class="col-time">
                <span class="time-text">{{ formatTime(record.auditTime) }}</span>
              </td>
              <td class="col-actions">
                <button 
                  v-if="record.status === 0" 
                  class="action-btn approve"
                  @click="approveRecord(record)"
                  title="通过"
                >
                  ✓
                </button>
                <button 
                  v-if="record.status === 0" 
                  class="action-btn reject"
                  @click="rejectRecord(record)"
                  title="拒绝"
                >
                  ✕
                </button>
                <span v-else class="action-done">已处理</span>
              </td>
            </tr>
            <tr v-if="auditRecords.length === 0" class="empty-row">
              <td colspan="6" class="empty-cell">
                <div class="empty-state">
                  <span class="empty-icon">📋</span>
                  <p>暂无审核记录</p>
                  <span class="empty-hint">尝试调整筛选条件</span>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- Pagination -->
      <div v-if="totalPages > 1" class="pagination">
        <button 
          class="page-btn" 
          :disabled="currentPage === 1"
          @click="goToPage(currentPage - 1)"
        >
          ← 上一页
        </button>
        <div class="page-numbers">
          <button 
            v-for="page in visiblePages" 
            :key="page"
            class="page-number"
            :class="{ active: page === currentPage }"
            @click="goToPage(page)"
          >
            {{ page }}
          </button>
        </div>
        <button 
          class="page-btn" 
          :disabled="currentPage === totalPages"
          @click="goToPage(currentPage + 1)"
        >
          下一页 →
        </button>
        <span class="page-info">共 {{ total }} 条</span>
      </div>
    </section>

    <!-- Reject Modal -->
    <div v-if="showRejectModal" class="modal-overlay" @click.self="closeRejectModal">
      <div class="modal-card">
        <div class="modal-header">
          <h3>拒绝审核</h3>
          <button class="modal-close" @click="closeRejectModal">&times;</button>
        </div>
        <div class="modal-body">
          <div class="form-group">
            <label>拒绝原因</label>
            <textarea 
              v-model="rejectReason" 
              placeholder="请输入拒绝原因..."
              rows="3"
            ></textarea>
          </div>
        </div>
        <div class="modal-footer">
          <button class="ghost-btn" @click="closeRejectModal">取消</button>
          <button class="cta danger" @click="confirmReject">确认拒绝</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { 
  getAuditRecords, 
  approvePost, 
  rejectPost, 
  approveShop, 
  rejectShop,
  listAdminPosts,
  listAdminShops,
  listAdminMerchants
} from '../api/audit';

// State
const auditRecords = ref([]);
const currentPage = ref(1);
const pageSize = ref(10);
const total = ref(0);
const loading = ref(false);

const filters = ref({
  type: '',
  status: ''
});

const stats = ref({
  pending: 0,
  approved: 0,
  rejected: 0
});

// Reject modal
const showRejectModal = ref(false);
const rejectReason = ref('');
const currentRecord = ref(null);

// Computed
const totalPages = computed(() => Math.ceil(total.value / pageSize.value));

const visiblePages = computed(() => {
  const pages = [];
  const maxVisible = 5;
  let start = Math.max(1, currentPage.value - Math.floor(maxVisible / 2));
  let end = Math.min(totalPages.value, start + maxVisible - 1);
  
  if (end - start + 1 < maxVisible) {
    start = Math.max(1, end - maxVisible + 1);
  }
  
  for (let i = start; i <= end; i++) {
    pages.push(i);
  }
  return pages;
});

// Methods
const getTypeLabel = (type) => {
  const labels = {
    post: '帖子',
    comment: '评论',
    shop: '店铺',
    merchant: '商户'
  };
  return labels[type] || type;
};

const getStatusLabel = (status) => {
  const labels = {
    0: '待审核',
    1: '已通过',
    2: '已拒绝'
  };
  return labels[status] || '未知';
};

const getStatusClass = (status) => {
  const classes = {
    0: 'pending',
    1: 'approved',
    2: 'rejected'
  };
  return classes[status] || '';
};

const truncateContent = (content) => {
  if (!content) return '-';
  return content.length > 30 ? content.substring(0, 30) + '...' : content;
};

const formatTime = (time) => {
  if (!time) return '-';
  const date = new Date(time);
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  });
};

const loadAuditRecords = async () => {
  loading.value = true;
  try {
    const response = await getAuditRecords({
      type: filters.value.type || undefined,
      status: filters.value.status === '' ? undefined : Number(filters.value.status),
      page: currentPage.value,
      pageSize: pageSize.value
    });
    
    if (response.success) {
      auditRecords.value = response.data?.records || [];
      total.value = response.data?.total || 0;
    }
  } catch (error) {
    console.error('Failed to load audit records:', error);
  } finally {
    loading.value = false;
  }
};

const loadStats = async () => {
  try {
    // Load data from all sources to calculate stats
    const [postsRes, shopsRes, merchantsRes] = await Promise.all([
      listAdminPosts(),
      listAdminShops(),
      listAdminMerchants()
    ]);

    const posts = postsRes.data || [];
    const shops = shopsRes.data || [];
    const merchants = merchantsRes.data || [];

    // Calculate stats
    stats.value.pending = [
      ...posts.filter(p => p.auditStatus === 0),
      ...shops.filter(s => s.auditStatus === 0),
      ...merchants.filter(m => m.aiAuditStatus === 0 || m.status === 0)
    ].length;

    stats.value.approved = [
      ...posts.filter(p => p.auditStatus === 1),
      ...shops.filter(s => s.auditStatus === 1),
      ...merchants.filter(m => m.aiAuditStatus === 1 || m.status === 1)
    ].length;

    stats.value.rejected = [
      ...posts.filter(p => p.auditStatus === 2),
      ...shops.filter(s => s.auditStatus === 2),
      ...merchants.filter(m => m.aiAuditStatus === 2)
    ].length;
  } catch (error) {
    console.error('Failed to load stats:', error);
  }
};

const resetFilters = () => {
  filters.value = {
    type: '',
    status: ''
  };
  currentPage.value = 1;
  loadAuditRecords();
};

const goToPage = (page) => {
  if (page >= 1 && page <= totalPages.value) {
    currentPage.value = page;
    loadAuditRecords();
  }
};

const approveRecord = async (record) => {
  try {
    let response;
    if (record.type === 'post') {
      response = await approvePost(record.entityId);
    } else if (record.type === 'shop') {
      response = await approveShop(record.entityId);
    }
    
    if (response?.success) {
      await loadAuditRecords();
      await loadStats();
    }
  } catch (error) {
    console.error('Failed to approve:', error);
  }
};

const rejectRecord = (record) => {
  currentRecord.value = record;
  rejectReason.value = '';
  showRejectModal.value = true;
};

const closeRejectModal = () => {
  showRejectModal.value = false;
  currentRecord.value = null;
  rejectReason.value = '';
};

const confirmReject = async () => {
  if (!rejectReason.value.trim()) {
    alert('请输入拒绝原因');
    return;
  }
  
  try {
    let response;
    if (currentRecord.value.type === 'post') {
      response = await rejectPost(currentRecord.value.entityId, rejectReason.value);
    } else if (currentRecord.value.type === 'shop') {
      response = await rejectShop(currentRecord.value.entityId, rejectReason.value);
    }
    
    if (response?.success) {
      closeRejectModal();
      await loadAuditRecords();
      await loadStats();
    }
  } catch (error) {
    console.error('Failed to reject:', error);
  }
};

onMounted(() => {
  loadAuditRecords();
  loadStats();
});
</script>

<style scoped>
/* ========== Page Layout ========== */
.audit-records {
  display: flex;
  flex-direction: column;
  gap: var(--space-6);
}

/* ========== Page Header ========== */
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: var(--space-6);
  padding: var(--space-6);
  background: var(--bg-primary);
  border-radius: var(--radius-xl);
  box-shadow: var(--shadow-md);
  border: 1px solid var(--border-light);
}

.header-content h1 {
  font-size: var(--text-2xl);
  font-weight: var(--font-bold);
  color: var(--text-primary);
  margin: 0 0 var(--space-2) 0;
}

.header-content p {
  font-size: var(--text-sm);
  color: var(--text-tertiary);
  margin: 0;
}

.header-stats {
  display: flex;
  gap: var(--space-4);
}

.stat-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: var(--space-4) var(--space-6);
  background: var(--bg-secondary);
  border-radius: var(--radius-lg);
  border: 1px solid var(--border-light);
  min-width: 80px;
}

.stat-card.approved {
  background: var(--color-success-light);
  border-color: var(--color-success);
}

.stat-card.rejected {
  background: var(--color-danger-light);
  border-color: var(--color-danger);
}

.stat-number {
  font-size: var(--text-2xl);
  font-weight: var(--font-bold);
  color: var(--brand-primary);
}

.stat-card.approved .stat-number {
  color: var(--color-success);
}

.stat-card.rejected .stat-number {
  color: var(--color-danger);
}

.stat-label {
  font-size: var(--text-xs);
  color: var(--text-tertiary);
  margin-top: var(--space-1);
}

/* ========== Filters Section ========== */
.filters-section {
  padding: var(--space-5);
  background: var(--bg-primary);
  border-radius: var(--radius-xl);
  box-shadow: var(--shadow-sm);
  border: 1px solid var(--border-light);
}

.filters-grid {
  display: flex;
  align-items: flex-end;
  gap: var(--space-4);
  flex-wrap: wrap;
}

.filter-group {
  display: flex;
  flex-direction: column;
  gap: var(--space-2);
  min-width: 160px;
}

.filter-group label {
  font-size: var(--text-sm);
  font-weight: var(--font-medium);
  color: var(--text-secondary);
}

.filter-select {
  padding: var(--space-2) var(--space-3);
  border: 1px solid var(--border-default);
  border-radius: var(--radius-md);
  font-size: var(--text-sm);
  color: var(--text-primary);
  background: var(--bg-primary);
  cursor: pointer;
  transition: all var(--transition-fast);
}

.filter-select:hover {
  border-color: var(--brand-primary);
}

.filter-select:focus {
  outline: none;
  border-color: var(--brand-primary);
  box-shadow: 0 0 0 3px var(--brand-primary-light);
}

.filter-actions {
  display: flex;
  gap: var(--space-3);
  margin-left: auto;
}

.btn-icon {
  font-size: var(--text-sm);
}

/* ========== Table Section ========== */
.table-section {
  background: var(--bg-primary);
  border-radius: var(--radius-xl);
  box-shadow: var(--shadow-md);
  border: 1px solid var(--border-light);
  overflow: hidden;
}

.table-container {
  overflow-x: auto;
}

.audit-table {
  width: 100%;
  border-collapse: collapse;
  font-size: var(--text-sm);
}

.audit-table th {
  padding: var(--space-4) var(--space-4);
  text-align: left;
  font-weight: var(--font-semibold);
  color: var(--text-secondary);
  background: var(--bg-secondary);
  border-bottom: 1px solid var(--border-light);
  white-space: nowrap;
}

.audit-table td {
  padding: var(--space-4);
  border-bottom: 1px solid var(--border-light);
  color: var(--text-primary);
}

.table-row {
  transition: background-color var(--transition-fast);
}

.table-row:hover {
  background: var(--bg-secondary);
}

.table-row:last-child td {
  border-bottom: none;
}

/* Column widths */
.col-type {
  width: 80px;
}

.col-content {
  min-width: 200px;
}

.col-status {
  width: 100px;
}

.col-reason {
  min-width: 150px;
}

.col-time {
  width: 140px;
}

.col-actions {
  width: 100px;
  text-align: center;
}

/* Type Badge */
.type-badge {
  display: inline-flex;
  align-items: center;
  padding: var(--space-1) var(--space-3);
  border-radius: var(--radius-full);
  font-size: var(--text-xs);
  font-weight: var(--font-medium);
  background: var(--bg-secondary);
  color: var(--text-secondary);
}

.type-badge.post {
  background: #E0F2FE;
  color: #0369A1;
}

.type-badge.comment {
  background: #F3E8FF;
  color: #7C3AED;
}

.type-badge.shop {
  background: #D1FAE5;
  color: #059669;
}

.type-badge.merchant {
  background: #FEF3C7;
  color: #D97706;
}

/* Content Preview */
.content-preview {
  display: flex;
  flex-direction: column;
  gap: var(--space-1);
}

.content-id {
  font-size: var(--text-xs);
  color: var(--text-tertiary);
  font-weight: var(--font-medium);
}

.content-text {
  color: var(--text-primary);
  word-break: break-all;
}

/* Status Badge */
.status-badge {
  display: inline-flex;
  align-items: center;
  padding: var(--space-1) var(--space-3);
  border-radius: var(--radius-full);
  font-size: var(--text-xs);
  font-weight: var(--font-medium);
}

.status-badge.pending {
  background: var(--neutral-100);
  color: var(--neutral-600);
}

.status-badge.approved {
  background: var(--color-success-light);
  color: var(--color-success);
}

.status-badge.rejected {
  background: var(--color-danger-light);
  color: var(--color-danger);
}

/* Reason Text */
.reason-text {
  color: var(--text-secondary);
  font-size: var(--text-sm);
}

/* Time Text */
.time-text {
  color: var(--text-tertiary);
  font-size: var(--text-sm);
  white-space: nowrap;
}

/* Action Buttons */
.col-actions {
  display: flex;
  justify-content: center;
  gap: var(--space-2);
}

.action-btn {
  width: 28px;
  height: 28px;
  border: none;
  border-radius: var(--radius-md);
  font-size: var(--text-sm);
  cursor: pointer;
  transition: all var(--transition-fast);
  display: inline-flex;
  align-items: center;
  justify-content: center;
}

.action-btn.approve {
  background: var(--color-success-light);
  color: var(--color-success);
}

.action-btn.approve:hover {
  background: var(--color-success);
  color: white;
}

.action-btn.reject {
  background: var(--color-danger-light);
  color: var(--color-danger);
}

.action-btn.reject:hover {
  background: var(--color-danger);
  color: white;
}

.action-done {
  font-size: var(--text-xs);
  color: var(--text-tertiary);
}

/* Empty State */
.empty-row:hover {
  background: transparent;
}

.empty-cell {
  padding: var(--space-12) var(--space-6);
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--space-3);
}

.empty-icon {
  font-size: var(--text-3xl);
  opacity: 0.5;
}

.empty-state p {
  font-size: var(--text-base);
  font-weight: var(--font-medium);
  color: var(--text-secondary);
  margin: 0;
}

.empty-hint {
  font-size: var(--text-sm);
  color: var(--text-tertiary);
}

/* ========== Pagination ========== */
.pagination {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: var(--space-3);
  padding: var(--space-5);
  border-top: 1px solid var(--border-light);
}

.page-btn {
  padding: var(--space-2) var(--space-4);
  border: 1px solid var(--border-default);
  border-radius: var(--radius-md);
  background: var(--bg-primary);
  color: var(--text-secondary);
  font-size: var(--text-sm);
  cursor: pointer;
  transition: all var(--transition-fast);
}

.page-btn:hover:not(:disabled) {
  border-color: var(--brand-primary);
  color: var(--brand-primary);
}

.page-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.page-numbers {
  display: flex;
  gap: var(--space-1);
}

.page-number {
  width: 36px;
  height: 36px;
  border: 1px solid transparent;
  border-radius: var(--radius-md);
  background: transparent;
  color: var(--text-secondary);
  font-size: var(--text-sm);
  cursor: pointer;
  transition: all var(--transition-fast);
}

.page-number:hover {
  background: var(--bg-secondary);
}

.page-number.active {
  background: var(--brand-primary);
  color: white;
  border-color: var(--brand-primary);
}

.page-info {
  font-size: var(--text-sm);
  color: var(--text-tertiary);
  margin-left: var(--space-4);
}

/* ========== Modal ========== */
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

/* ========== Responsive ========== */
@media (max-width: 1024px) {
  .page-header {
    flex-direction: column;
  }
  
  .header-stats {
    width: 100%;
    justify-content: flex-start;
  }
}

@media (max-width: 768px) {
  .filters-grid {
    flex-direction: column;
    align-items: stretch;
  }
  
  .filter-actions {
    margin-left: 0;
    justify-content: flex-end;
  }
  
  .audit-table {
    font-size: var(--text-xs);
  }
  
  .audit-table th,
  .audit-table td {
    padding: var(--space-3) var(--space-2);
  }
  
  .col-reason,
  .col-time {
    display: none;
  }
  
  .pagination {
    flex-wrap: wrap;
  }
}
</style>
<template>
  <div class="page">
    <div class="page-header">
      <h1>内容审核</h1>
      <p>查看并审核用户帖子内容</p>
    </div>

    <div class="toolbar card">
      <select v-model="statusFilter">
        <option value="">全部状态</option>
        <option value="0">待审核</option>
        <option value="1">已通过</option>
        <option value="2">已拒绝</option>
      </select>
      <button class="btn" @click="loadPosts">刷新</button>
    </div>

    <div class="card list">
      <div v-if="posts.length === 0" class="empty">暂无内容</div>
      <div v-for="p in posts" :key="p.id" class="item">
        <div class="title">{{ p.title || `帖子#${p.id}` }}</div>
        <div class="meta">
          <span>用户 {{ p.userId }}</span>
          <span>店铺 {{ p.shopId || '-' }}</span>
          <span :class="['status', `s-${p.auditStatus}`]">{{ statusText(p.auditStatus) }}</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from "vue";
import { listAdminPosts } from "../api/audit";

const posts = ref([]);
const statusFilter = ref("");

const statusText = (s) => {
  if (s === 0 || s === "0") return "待审核";
  if (s === 1 || s === "1") return "已通过";
  if (s === 2 || s === "2") return "已拒绝";
  return "未知";
};

const loadPosts = async () => {
  const res = await listAdminPosts(statusFilter.value === "" ? undefined : Number(statusFilter.value));
  if (res?.success) posts.value = res.data || [];
};

onMounted(loadPosts);
</script>

<style scoped>
.page { display: grid; gap: 16px; }
.page-header h1 { margin: 0; }
.page-header p { margin: 6px 0 0; color: #64748b; }
.card { background: #fff; border: 1px solid #e2e8f0; border-radius: 12px; padding: 14px; }
.toolbar { display: flex; gap: 10px; align-items: center; }
select { padding: 8px 10px; border: 1px solid #cbd5e1; border-radius: 8px; }
.btn { padding: 8px 12px; border: 1px solid #cbd5e1; border-radius: 8px; background: #fff; cursor: pointer; }
.list { display: grid; gap: 10px; }
.item { border: 1px solid #edf2f7; border-radius: 10px; padding: 12px; }
.title { font-weight: 600; margin-bottom: 6px; }
.meta { display: flex; gap: 10px; color: #64748b; font-size: 13px; }
.status { padding: 2px 8px; border-radius: 999px; }
.s-0 { background: #fff7ed; color: #c2410c; }
.s-1 { background: #eff6ff; color: #1d4ed8; }
.s-2 { background: #fef2f2; color: #b91c1c; }
.empty { color: #64748b; }
</style>

<!--
  骨架屏组件
  
  使用示例:
  <Skeleton :loading="isLoading" type="card">
    <ActualContent />
  </Skeleton>
  
  或直接使用骨架屏:
  <Skeleton type="list" :count="5" />
-->

<template>
  <div v-if="loading" class="skeleton-wrapper">
    <!-- 卡片类型 -->
    <template v-if="type === 'card'">
      <div class="skeleton-card" v-for="i in count" :key="i">
        <div class="skeleton-image skeleton-bg shimmer"></div>
        <div class="skeleton-content">
          <div class="skeleton-title skeleton-bg shimmer"></div>
          <div class="skeleton-text skeleton-bg shimmer"></div>
          <div class="skeleton-text short skeleton-bg shimmer"></div>
        </div>
      </div>
    </template>
    
    <!-- 列表类型 -->
    <template v-else-if="type === 'list'">
      <div class="skeleton-list-item" v-for="i in count" :key="i">
        <div class="skeleton-avatar skeleton-bg shimmer"></div>
        <div class="skeleton-list-content">
          <div class="skeleton-title skeleton-bg shimmer"></div>
          <div class="skeleton-text skeleton-bg shimmer"></div>
        </div>
      </div>
    </template>
    
    <!-- 详情类型 -->
    <template v-else-if="type === 'detail'">
      <div class="skeleton-detail">
        <div class="skeleton-detail-image skeleton-bg shimmer"></div>
        <div class="skeleton-detail-content">
          <div class="skeleton-title large skeleton-bg shimmer"></div>
          <div class="skeleton-text skeleton-bg shimmer" v-for="i in 4" :key="i"></div>
        </div>
      </div>
    </template>
    
    <!-- 文本类型 -->
    <template v-else-if="type === 'text'">
      <div class="skeleton-text-block">
        <div class="skeleton-text skeleton-bg shimmer" v-for="i in count" :key="i"></div>
      </div>
    </template>
  </div>
  
  <slot v-else></slot>
</template>

<script setup>
defineProps({
  loading: {
    type: Boolean,
    default: true
  },
  type: {
    type: String,
    default: 'card',
    validator: (value) => ['card', 'list', 'detail', 'text'].includes(value)
  },
  count: {
    type: Number,
    default: 1
  }
});
</script>

<style scoped>
.skeleton-wrapper {
  width: 100%;
}

.skeleton-bg {
  background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
  background-size: 200% 100%;
  border-radius: 4px;
}

.shimmer {
  animation: shimmer 1.5s infinite;
}

@keyframes shimmer {
  0% {
    background-position: 200% 0;
  }
  100% {
    background-position: -200% 0;
  }
}

/* 卡片类型 */
.skeleton-card {
  background: #fff;
  border-radius: 8px;
  overflow: hidden;
  margin-bottom: 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.skeleton-image {
  width: 100%;
  height: 180px;
}

.skeleton-content {
  padding: 16px;
}

.skeleton-title {
  height: 20px;
  margin-bottom: 12px;
  width: 60%;
}

.skeleton-title.large {
  height: 28px;
  width: 80%;
}

.skeleton-text {
  height: 14px;
  margin-bottom: 8px;
  width: 100%;
}

.skeleton-text.short {
  width: 40%;
}

/* 列表类型 */
.skeleton-list-item {
  display: flex;
  align-items: center;
  padding: 12px 0;
  border-bottom: 1px solid #f0f0f0;
}

.skeleton-avatar {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  margin-right: 12px;
  flex-shrink: 0;
}

.skeleton-list-content {
  flex: 1;
}

/* 详情类型 */
.skeleton-detail {
  padding: 16px;
}

.skeleton-detail-image {
  width: 100%;
  height: 240px;
  border-radius: 8px;
  margin-bottom: 16px;
}

.skeleton-detail-content {
  padding: 0;
}

/* 文本类型 */
.skeleton-text-block {
  padding: 16px;
}

.skeleton-text-block .skeleton-text {
  margin-bottom: 12px;
}
</style>

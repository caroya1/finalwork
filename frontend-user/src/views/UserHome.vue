<template>
  <section class="recommend-section">
    <div class="recommend-header">
      <h2 :class="['recommend-title', currentStrategy === 'ai' ? 'ai-active' : '']">
        <span v-if="currentStrategy === 'ai'" class="ai-title-icon">✨</span>
        <span v-else>🔥</span>
        {{ currentStrategy === 'ai' ? 'AI智能推荐' : '热门推荐' }}
        <span v-if="currentStrategy === 'ai'" class="ai-badge">AI</span>
      </h2>
      <p class="recommend-subtitle" v-if="recommendations.length > 0">
        {{ currentStrategy === 'ai' ? 'AI为你智能匹配' : '为你精选' }} {{ recommendations.length }} 家好店
      </p>
    </div>
    
    <div class="recommend-cards">
      <RouterLink 
        v-for="shop in recommendations" 
        :key="shop.id" 
        class="shop-card" 
        :to="`/shops/${shop.id}`"
      >
        <div class="shop-image">
          <img :src="shop.images ? shop.images.split(',')[0] : '/default-shop.jpg'" :alt="shop.name" />
          <span class="category-tag">{{ shop.category || "美食" }}</span>
        </div>
        <div class="shop-info">
          <div class="shop-name-row">
            <h3 class="shop-name">{{ shop.name }}</h3>
            <span class="rating-badge">{{ shop.rating ? shop.rating.toFixed(1) : "0.0" }}</span>
          </div>
          <div class="shop-tags">
            <span v-for="(tag, index) in getShopTags(shop)" :key="index" class="shop-tag">{{ tag }}</span>
          </div>
          <div class="shop-address">
            <span class="address-icon">📍</span>
            <span class="address-text">{{ shop.address || shop.city }}</span>
          </div>
        </div>
      </RouterLink>
      
      <div v-if="recommendations.length === 0 && !recLoading" class="empty-recommend">
        <span class="empty-icon">🏪</span>
        <span>暂无推荐数据</span>
      </div>
      <div v-if="recLoading" class="empty-recommend loading">
        <span class="loading-spinner"></span>
        <span>加载中...</span>
      </div>
    </div>
    <div v-if="recMessage" class="rec-message">{{ recMessage }}</div>
  </section>

  <section class="category-section">
    <div class="category-grid">
      <button
        class="category-card"
        v-for="item in categories"
        :key="item.label"
        @click="goCategory(item)"
      >
        <div class="category-icon">{{ item.icon }}</div>
        <div class="category-content">
          <div class="category-label">{{ item.label }}</div>
          <div class="category-desc">{{ item.desc }}</div>
        </div>
      </button>
    </div>
  </section>

  <section class="feed-section" v-if="feeds.length > 0">
    <div class="section-header">
      <h2>探索发现</h2>
      <p>发现身边的精彩生活</p>
    </div>

    <div class="feed-grid">
      <RouterLink
        class="feed-card"
        v-for="item in feeds"
        :key="item.id"
        :to="`/posts/${item.id}`"
      >
        <div class="feed-image">
          <img v-if="item.coverUrl" :src="item.coverUrl" :alt="item.title" />
          <div v-else class="feed-image-placeholder"></div>
        </div>
        <div class="feed-body">
          <h3 class="feed-title">{{ item.title }}</h3>
          <p class="feed-content">{{ item.content }}</p>
          <div class="feed-meta">
            <span class="meta-location">
              <span class="meta-icon">📍</span>
              {{ item.city || "-" }}
            </span>
            <span class="meta-likes">
              <span class="meta-icon">❤️</span>
              {{ item.likes || 0 }}
            </span>
          </div>
        </div>
      </RouterLink>
    </div>
  </section>
  
  <div v-if="feeds.length === 0 && !recLoading" class="empty-state">
    <span class="empty-icon-large">📝</span>
    <p>暂无帖子，稍后再试</p>
  </div>

  <!-- Floating AI Recommend Button -->
  <button 
    class="fab-smart-recommend" 
    @click="openSmartDialog"
    aria-label="智能推荐"
    title="智能推荐"
  >
    <div class="fab-icon-wrapper">
      <svg class="fab-icon" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
        <path d="M12 2L2 7L12 12L22 7L12 2Z" fill="currentColor" opacity="0.4"/>
        <path d="M2 17L12 22L22 17" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
        <path d="M2 12L12 17L22 12" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
      </svg>
    </div>
    <span class="fab-tooltip">智能推荐</span>
    <span class="fab-pulse-ring"></span>
  </button>

  <!-- Smart Recommend Dialog -->
  <SmartRecommendDialog v-model="showSmartDialog" />
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount } from "vue";
import { RouterLink, useRouter } from "vue-router";
import { fetchRecommendations } from "../api/recommendation";
import { listPosts } from "../api/post";
import SmartRecommendDialog from "../components/SmartRecommendDialog.vue";

const router = useRouter();

const recommendations = ref([]);
const recMessage = ref("");
const recLoading = ref(false);

// Smart Recommend Dialog state
const showSmartDialog = ref(false);

const openSmartDialog = () => {
  showSmartDialog.value = true;
};

const closeSmartDialog = () => {
  showSmartDialog.value = false;
};

const categories = ref([
  { icon: "🍜", label: "美食", desc: "今日爆款", query: "美食" },
  { icon: "🏞", label: "景点/周边游", desc: "轻松出发", query: "休闲,景点,周边游" },
  { icon: "🏨", label: "酒店/民宿", desc: "安心住", query: "酒店,民宿" },
  { icon: "🎮", label: "休闲/玩乐", desc: "好去处", query: "休闲" },
  { icon: "🎬", label: "猫眼电影", desc: "热映中", query: "电影" }
]);

const feeds = ref([]);

const currentStrategy = ref("hot");

const loadRecommendations = async (scene, strategy = "hot") => {
  recMessage.value = "";
  recLoading.value = true;
  const userId = localStorage.getItem("dp_user_id");
  const city = localStorage.getItem("dp_city") || "上海";

  /* 更新当前策略 */
  currentStrategy.value = strategy;

  const payload = {
    userId: userId ? Number(userId) : 0,
    city: city,
    scene: scene || null,
    strategy: strategy
  };

  try {
    const response = await fetchRecommendations(payload);
    if (response.success) {
      recommendations.value = response.data || [];
      if (recommendations.value.length === 0) {
        recMessage.value = "暂无匹配的推荐结果";
      }
    } else {
      recMessage.value = response.message || "获取推荐失败";
    }
  } catch (error) {
    console.error("获取推荐失败:", error);
    recMessage.value = "获取推荐失败，请稍后重试";
  } finally {
    recLoading.value = false;
  }
};

const loadPosts = async (keyword) => {
  const city = localStorage.getItem("dp_city") || "上海";
  const response = await listPosts({ city, keyword });
  if (response.success) {
    feeds.value = (response.data || []).map((item) => ({
      ...item,
      content: item.content ? item.content.slice(0, 60) + (item.content.length > 60 ? "..." : "") : ""
    }));
  } else {
    feeds.value = [];
  }
};

const goCreatePost = () => {
  if (!isLoggedIn.value) {
    window.dispatchEvent(new CustomEvent("dp-auth-required"));
    return;
  }
  router.push("/posts/new");
};

const goCategory = (item) => {
  const city = localStorage.getItem("dp_city") || "上海";
  router.push({
    path: "/category",
    query: { category: item.query, label: item.label, city }
  });
};

const getShopTags = (shop) => {
  const tags = [];
  if (shop.tags) {
    const tagList = shop.tags.split(/[,，]/).filter(tag => tag.trim());
    tags.push(...tagList.slice(0, 3));
  }
  if (tags.length < 2 && shop.category) {
    const categoryMap = {
      "美食": ["好吃", "人气旺"],
      "火锅": ["正宗", "麻辣"],
      "酒店": ["舒适", "性价比高"],
      "景点": ["好玩", "值得去"]
    };
    const defaultTags = categoryMap[shop.category] || ["推荐", "热门"];
    tags.push(...defaultTags.slice(0, 2 - tags.length));
  }
  return tags.slice(0, 3);
};

const handleSearch = (event) => {
  const detail = event.detail || {};
  const keyword = typeof detail === "string" ? detail : detail.keyword;
  const mode = typeof detail === "string" ? "search" : detail.mode || "search";
  const strategy = typeof detail === "string" ? "hot" : detail.strategy || "hot";
  if (mode === "recommend") {
    loadRecommendations(keyword || "", strategy);
    return;
  }
  if (keyword) {
    router.push({
      path: "/search",
      query: { keyword, city: localStorage.getItem("dp_city") || "上海" }
    });
    return;
  }
  loadPosts("");
};

onMounted(() => {
  loadPosts("");
  loadRecommendations("");
  window.addEventListener("dp-search", handleSearch);
});

onBeforeUnmount(() => {
  window.removeEventListener("dp-search", handleSearch);
});
</script>

<style scoped>
/* 推荐区域 */
.recommend-section {
  padding: var(--space-6);
  background: linear-gradient(135deg, #FFF5F0 0%, #FFEBE3 100%);
  border-radius: var(--radius-2xl);
  margin-bottom: var(--space-6);
  border: 1px solid rgba(255, 107, 53, 0.1);
}

.recommend-header {
  margin-bottom: var(--space-5);
}

.recommend-title {
  font-size: var(--text-xl);
  font-weight: var(--font-bold);
  color: var(--text-primary);
  margin: 0 0 var(--space-1) 0;
  letter-spacing: -0.02em;
}

.recommend-subtitle {
  font-size: var(--text-sm);
  color: var(--text-secondary);
  margin: 0;
}

.recommend-cards {
  display: flex;
  gap: var(--space-4);
  overflow-x: auto;
  padding-bottom: var(--space-2);
  scrollbar-width: thin;
  scrollbar-color: var(--brand-primary-light) transparent;
  scroll-snap-type: x mandatory;
}

.recommend-cards::-webkit-scrollbar {
  height: 6px;
}

.recommend-cards::-webkit-scrollbar-track {
  background: transparent;
}

.recommend-cards::-webkit-scrollbar-thumb {
  background-color: rgba(255, 107, 53, 0.2);
  border-radius: 3px;
}

.recommend-cards::-webkit-scrollbar-thumb:hover {
  background-color: rgba(255, 107, 53, 0.4);
}

/* 店铺卡片 */
.shop-card {
  flex: 0 0 300px;
  background: var(--bg-primary);
  border-radius: var(--radius-2xl);
  overflow: hidden;
  box-shadow: var(--shadow-md);
  text-decoration: none;
  color: inherit;
  transition: all var(--transition-base);
  scroll-snap-align: start;
  border: 1px solid var(--border-light);
}

.shop-card:hover {
  transform: translateY(-6px);
  box-shadow: var(--shadow-xl);
}

.shop-card:focus-visible {
  outline: 2px solid var(--brand-primary);
  outline-offset: 2px;
}

.shop-card:active {
  transform: translateY(-3px);
}

.shop-image {
  position: relative;
  width: 100%;
  height: 170px;
  overflow: hidden;
  background: linear-gradient(135deg, #FFE5D0 0%, #FFCDB2 100%);
}

.shop-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform var(--transition-slow);
}

.shop-card:hover .shop-image img {
  transform: scale(1.05);
}

.category-tag {
  position: absolute;
  top: var(--space-3);
  left: var(--space-3);
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(8px);
  padding: var(--space-1) var(--space-3);
  border-radius: var(--radius-full);
  font-size: var(--text-xs);
  font-weight: var(--font-semibold);
  color: var(--brand-primary);
  box-shadow: var(--shadow-sm);
}

.shop-info {
  padding: var(--space-4);
}

.shop-name-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: var(--space-2);
  gap: var(--space-2);
}

.shop-name {
  font-size: var(--text-base);
  font-weight: var(--font-semibold);
  color: var(--text-primary);
  margin: 0;
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.rating-badge {
  background: var(--brand-gradient);
  color: var(--text-inverse);
  padding: var(--space-1) var(--space-2);
  border-radius: var(--radius-md);
  font-size: var(--text-sm);
  font-weight: var(--font-bold);
  flex-shrink: 0;
  min-width: 40px;
  text-align: center;
}

.shop-tags {
  display: flex;
  flex-wrap: wrap;
  gap: var(--space-2);
  margin-bottom: var(--space-3);
}

.shop-tag {
  background: var(--bg-secondary);
  color: var(--text-secondary);
  padding: var(--space-1) var(--space-2);
  border-radius: var(--radius-md);
  font-size: var(--text-xs);
  font-weight: var(--font-medium);
}

.shop-address {
  display: flex;
  align-items: center;
  color: var(--text-tertiary);
  font-size: var(--text-sm);
}

.address-icon {
  margin-right: var(--space-1);
  font-size: var(--text-xs);
}

.address-text {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* 空状态 */
.empty-recommend {
  width: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: var(--space-10);
  color: var(--text-tertiary);
  font-size: var(--text-sm);
  gap: var(--space-2);
}

.empty-icon {
  font-size: var(--text-4xl);
  opacity: 0.5;
}

.loading {
  color: var(--brand-primary);
}

.loading-spinner {
  width: 24px;
  height: 24px;
  border: 2px solid var(--brand-primary-light);
  border-top-color: var(--brand-primary);
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.rec-message {
  margin-top: var(--space-4);
  padding: var(--space-3) var(--space-4);
  background: var(--color-warning-light);
  border-radius: var(--radius-lg);
  color: #92400E;
  font-size: var(--text-sm);
  text-align: center;
}

/* 分类区域 */
.category-section {
  margin-bottom: var(--space-8);
}

.category-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
  gap: var(--space-4);
}

.category-card {
  display: flex;
  align-items: center;
  gap: var(--space-4);
  padding: var(--space-4) var(--space-5);
  background: var(--bg-primary);
  border: 1px solid var(--border-light);
  border-radius: var(--radius-2xl);
  text-align: left;
  box-shadow: var(--shadow-sm);
  transition: all var(--transition-base);
  cursor: pointer;
}

.category-card:hover {
  transform: translateY(-4px);
  box-shadow: var(--shadow-lg);
  border-color: var(--brand-primary);
}

.category-card:focus-visible {
  outline: 2px solid var(--brand-primary);
  outline-offset: 2px;
}

.category-card:active {
  transform: translateY(-2px);
}

.category-icon {
  width: 52px;
  height: 52px;
  border-radius: var(--radius-xl);
  display: grid;
  place-items: center;
  background: linear-gradient(135deg, var(--brand-primary-light) 0%, rgba(255, 143, 92, 0.15) 100%);
  font-size: var(--text-2xl);
  flex-shrink: 0;
}

.category-content {
  flex: 1;
  min-width: 0;
}

.category-label {
  font-weight: var(--font-semibold);
  color: var(--text-primary);
  font-size: var(--text-base);
  margin-bottom: var(--space-1);
}

.category-desc {
  font-size: var(--text-xs);
  color: var(--text-tertiary);
}

/* 帖子区域 */
.feed-section {
  margin-bottom: var(--space-8);
}

.section-header {
  margin-bottom: var(--space-5);
}

.section-header h2 {
  margin: 0 0 var(--space-1);
  font-size: var(--text-2xl);
  font-weight: var(--font-semibold);
  color: var(--text-primary);
  letter-spacing: -0.02em;
}

.section-header p {
  margin: 0;
  font-size: var(--text-sm);
  color: var(--text-tertiary);
}

.feed-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: var(--space-5);
}

.feed-card {
  background: var(--bg-primary);
  border-radius: var(--radius-2xl);
  overflow: hidden;
  box-shadow: var(--shadow-sm);
  border: 1px solid var(--border-light);
  transition: all var(--transition-base);
  text-decoration: none;
  color: inherit;
}

.feed-card:hover {
  transform: translateY(-6px);
  box-shadow: var(--shadow-xl);
}

.feed-card:focus-visible {
  outline: 2px solid var(--brand-primary);
  outline-offset: 2px;
}

.feed-image {
  position: relative;
  width: 100%;
  height: 180px;
  overflow: hidden;
  background: linear-gradient(135deg, #FFE5D0 0%, #FFCDB2 100%);
}

.feed-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform var(--transition-slow);
}

.feed-card:hover .feed-image img {
  transform: scale(1.05);
}

.feed-image-placeholder {
  width: 100%;
  height: 100%;
  background: linear-gradient(135deg, #FFE5D0 0%, #FFCDB2 100%);
}

.feed-body {
  padding: var(--space-4);
}

.feed-title {
  margin: 0 0 var(--space-2);
  font-size: var(--text-base);
  font-weight: var(--font-semibold);
  color: var(--text-primary);
  line-height: var(--leading-snug);
}

.feed-content {
  margin: 0 0 var(--space-3);
  font-size: var(--text-sm);
  color: var(--text-secondary);
  line-height: var(--leading-relaxed);
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.feed-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: var(--text-xs);
  color: var(--text-tertiary);
}

.meta-location,
.meta-likes {
  display: flex;
  align-items: center;
  gap: var(--space-1);
}

.meta-icon {
  font-size: var(--text-xs);
}

/* 空状态 */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: var(--space-12);
  color: var(--text-tertiary);
  text-align: center;
}

.empty-icon-large {
  font-size: 4rem;
  margin-bottom: var(--space-4);
  opacity: 0.4;
}

.empty-state p {
  margin: 0;
  font-size: var(--text-base);
}

/* 响应式 */
@media (max-width: 768px) {
  .recommend-section {
    padding: var(--space-4);
    margin-bottom: var(--space-4);
  }
  
  .shop-card {
    flex: 0 0 260px;
  }
  
  .category-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  
  .feed-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 480px) {
  .category-grid {
    grid-template-columns: 1fr;
  }

  .category-card {
    padding: var(--space-3) var(--space-4);
  }

  .category-icon {
    width: 44px;
    height: 44px;
    font-size: var(--text-xl);
  }
}

/* Floating Action Button - Smart Recommend */
.fab-smart-recommend {
  position: fixed;
  bottom: 80px;
  right: 20px;
  width: 60px;
  height: 60px;
  border-radius: 50%;
  border: none;
  background: linear-gradient(135deg, var(--brand-primary) 0%, var(--brand-secondary) 100%);
  color: var(--text-inverse);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 8px 32px rgba(255, 107, 53, 0.4);
  transition: all var(--transition-base);
  z-index: var(--z-fixed);
  overflow: visible;
}

.fab-smart-recommend:hover {
  transform: translateY(-4px) scale(1.05);
  box-shadow: 0 12px 40px rgba(255, 107, 53, 0.5);
}

.fab-smart-recommend:active {
  transform: translateY(-2px) scale(0.98);
}

.fab-icon-wrapper {
  display: flex;
  align-items: center;
  justify-content: center;
  animation: fab-bounce 2s ease-in-out infinite;
}

.fab-icon {
  width: 28px;
  height: 28px;
}

/* Pulse Ring Animation */
.fab-pulse-ring {
  position: absolute;
  inset: -4px;
  border-radius: 50%;
  border: 2px solid var(--brand-primary);
  opacity: 0;
  animation: fab-pulse 2s ease-out infinite;
  pointer-events: none;
}

.fab-smart-recommend:hover .fab-pulse-ring {
  animation-play-state: paused;
}

@keyframes fab-pulse {
  0% {
    transform: scale(1);
    opacity: 0.6;
  }
  100% {
    transform: scale(1.3);
    opacity: 0;
  }
}

@keyframes fab-bounce {
  0%, 100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-2px);
  }
}

/* Tooltip */
.fab-tooltip {
  position: absolute;
  right: calc(100% + 12px);
  top: 50%;
  transform: translateY(-50%) scale(0.9);
  background: var(--neutral-800);
  color: var(--text-inverse);
  padding: var(--space-2) var(--space-3);
  border-radius: var(--radius-lg);
  font-size: var(--text-sm);
  font-weight: var(--font-medium);
  white-space: nowrap;
  opacity: 0;
  visibility: hidden;
  transition: all var(--transition-fast);
  box-shadow: var(--shadow-lg);
  pointer-events: none;
}

.fab-tooltip::after {
  content: '';
  position: absolute;
  left: 100%;
  top: 50%;
  transform: translateY(-50%);
  border: 6px solid transparent;
  border-left-color: var(--neutral-800);
}

.fab-smart-recommend:hover .fab-tooltip {
  opacity: 1;
  visibility: visible;
  transform: translateY(-50%) scale(1);
}

/* Responsive adjustments for FAB */
@media (max-width: 768px) {
  .fab-smart-recommend {
    bottom: 70px;
    right: 16px;
    width: 56px;
    height: 56px;
  }

  .fab-icon {
    width: 24px;
    height: 24px;
  }

  .fab-tooltip {
    display: none;
  }
}

@media (max-width: 480px) {
  .fab-smart-recommend {
    bottom: 60px;
    right: 12px;
    width: 52px;
    height: 52px;
  }

  .fab-icon {
    width: 22px;
    height: 22px;
  }
}

/* AI推荐标题样式 */
.recommend-title.ai-active {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  background: linear-gradient(135deg, var(--brand-primary) 0%, var(--brand-secondary) 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.ai-title-icon {
  font-size: var(--text-lg);
  animation: sparkle 2s ease-in-out infinite;
}

@keyframes sparkle {
  0%, 100% { opacity: 1; transform: scale(1); }
  50% { opacity: 0.7; transform: scale(1.1); }
}

.ai-badge {
  display: inline-flex;
  align-items: center;
  padding: var(--space-1) var(--space-2);
  background: linear-gradient(135deg, var(--brand-primary) 0%, var(--brand-secondary) 100%);
  color: var(--text-inverse);
  font-size: var(--text-xs);
  font-weight: var(--font-bold);
  border-radius: var(--radius-md);
  margin-left: var(--space-2);
  -webkit-text-fill-color: var(--text-inverse);
}
</style>

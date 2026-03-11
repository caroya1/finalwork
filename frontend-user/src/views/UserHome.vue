<template>
  <section class="recommend-section">
    <div class="recommend-header">
      <h2 class="recommend-title">🔥 热门推荐</h2>
      <p class="recommend-subtitle" v-if="recommendations.length > 0">为你精选 {{ recommendations.length }} 家好店</p>
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
            <span class="rating-badge">{{ shop.rating ? shop.rating.toFixed(2) : "0.00" }}分</span>
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
        暂无推荐数据
      </div>
      <div v-if="recLoading" class="empty-recommend">
        加载中...
      </div>
    </div>
    <div v-if="recMessage" class="rec-message">{{ recMessage }}</div>
  </section>

  <section class="category-grid">
    <button
      class="category-card"
      v-for="item in categories"
      :key="item.label"
      @click="goCategory(item)"
    >
      <div class="category-icon">{{ item.icon }}</div>
      <div>
        <div>{{ item.label }}</div>
        <div class="feed-meta">{{ item.desc }}</div>
      </div>
    </button>
  </section>

  <section class="section-header" v-if="feeds.length > 0">
    <h2>探索发现</h2>
    <p>发现身边的精彩生活</p>
  </section>

  <section class="feed-grid">
    <RouterLink
      class="feed-card"
      v-for="item in feeds"
      :key="item.id"
      :to="`/posts/${item.id}`"
    >
      <div class="feed-image"></div>
      <div class="feed-body">
        <h3 class="feed-title">{{ item.title }}</h3>
        <p class="feed-content">{{ item.content }}</p>
        <div class="feed-meta">
          <span>{{ item.city || "-" }}</span>
          <span>{{ item.likes || 0 }} 赞</span>
        </div>
      </div>
    </RouterLink>
  </section>
  <div v-if="feeds.length === 0" class="empty-state">
    暂无帖子，稍后再试
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount } from "vue";
import { RouterLink, useRouter } from "vue-router";
import { fetchRecommendations } from "../api/recommendation";
import { listPosts } from "../api/post";

const router = useRouter();

const recommendations = ref([]);
const recMessage = ref("");
const recLoading = ref(false);

const categories = ref([
  { icon: "🍜", label: "美食", desc: "今日爆款", query: "美食" },
  { icon: "🏞", label: "景点/周边游", desc: "轻松出发", query: "休闲,景点,周边游" },
  { icon: "🏨", label: "酒店/民宿", desc: "安心住", query: "酒店,民宿" },
  { icon: "🎮", label: "休闲/玩乐", desc: "好去处", query: "休闲" },
  { icon: "🎬", label: "猫眼电影", desc: "热映中", query: "电影" }
]);

const feeds = ref([]);

const loadRecommendations = async (scene) => {
  recMessage.value = "";
  recLoading.value = true;
  const userId = localStorage.getItem("dp_user_id");
  const city = localStorage.getItem("dp_city") || "上海";
  
  // 构建请求参数，未登录用户使用默认ID 0
  const payload = {
    userId: userId ? Number(userId) : 0,
    city: city,
    scene: scene || null
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

// 获取店铺标签列表
const getShopTags = (shop) => {
  const tags = [];
  if (shop.tags) {
    // 解析逗号分隔的标签
    const tagList = shop.tags.split(/[,，]/).filter(tag => tag.trim());
    tags.push(...tagList.slice(0, 3)); // 最多显示3个标签
  }
  // 如果标签不足，添加一些默认标签
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
  if (mode === "recommend") {
    loadRecommendations(keyword || "");
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
  loadRecommendations(""); // 首页自动加载热门推荐
  window.addEventListener("dp-search", handleSearch);
});

onBeforeUnmount(() => {
  window.removeEventListener("dp-search", handleSearch);
});
</script>

<style scoped>
/* 推荐区域样式 */
.recommend-section {
  padding: 20px 16px;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  border-radius: 16px;
  margin: 16px;
}

.recommend-header {
  margin-bottom: 16px;
}

.recommend-title {
  font-size: 20px;
  font-weight: 700;
  color: #333;
  margin: 0 0 4px 0;
}

.recommend-subtitle {
  font-size: 14px;
  color: #666;
  margin: 0;
}

/* 卡片容器 - 横向滚动 */
.recommend-cards {
  display: flex;
  gap: 16px;
  overflow-x: auto;
  padding-bottom: 8px;
  scrollbar-width: thin;
  scrollbar-color: #ccc transparent;
}

.recommend-cards::-webkit-scrollbar {
  height: 6px;
}

.recommend-cards::-webkit-scrollbar-track {
  background: transparent;
}

.recommend-cards::-webkit-scrollbar-thumb {
  background-color: #ccc;
  border-radius: 3px;
}

/* 单个店铺卡片 */
.shop-card {
  flex: 0 0 280px;
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  text-decoration: none;
  color: inherit;
  transition: transform 0.2s, box-shadow 0.2s;
}

.shop-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.12);
}

/* 店铺图片区域 */
.shop-image {
  position: relative;
  width: 100%;
  height: 160px;
  overflow: hidden;
  background: linear-gradient(135deg, #ffecd2 0%, #fcb69f 100%);
}

.shop-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.category-tag {
  position: absolute;
  top: 12px;
  left: 12px;
  background: rgba(255, 255, 255, 0.95);
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 12px;
  color: #ff6b6b;
  font-weight: 600;
}

/* 店铺信息区域 */
.shop-info {
  padding: 16px;
}

.shop-name-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 8px;
}

.shop-name {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  margin: 0;
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.rating-badge {
  background: linear-gradient(135deg, #ff6b6b 0%, #ee5a6f 100%);
  color: #fff;
  padding: 4px 10px;
  border-radius: 20px;
  font-size: 13px;
  font-weight: 600;
  margin-left: 8px;
  flex-shrink: 0;
}

/* 店铺标签 */
.shop-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 12px;
}

.shop-tag {
  background: #f5f5f5;
  color: #666;
  padding: 4px 10px;
  border-radius: 4px;
  font-size: 12px;
}

/* 店铺地址 */
.shop-address {
  display: flex;
  align-items: center;
  color: #999;
  font-size: 13px;
}

.address-icon {
  margin-right: 4px;
}

.address-text {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* 空状态 */
.empty-recommend {
  width: 100%;
  text-align: center;
  padding: 40px;
  color: #999;
}

.rec-message {
  margin-top: 12px;
  padding: 12px;
  background: #fff3cd;
  border-radius: 8px;
  color: #856404;
  font-size: 14px;
}

/* 旧样式兼容 */
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
</style>

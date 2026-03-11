<template>
  <section class="hero">
    <div class="hero-card">
      <h2>🔥 热门推荐</h2>
      <div class="list">
        <RouterLink v-for="shop in recommendations" :key="shop.id" class="list-item list-item-link" :to="`/shops/${shop.id}`">
          <strong>{{ shop.name }}</strong>
          <div>
            <span class="tag">{{ shop.category || "综合" }}</span>
            <span class="tag">{{ shop.city }}</span>
            <span v-if="shop.rating" class="tag">{{ shop.rating }}分</span>
          </div>
        </RouterLink>
        <div v-if="recommendations.length === 0 && !recLoading" class="list-item">
          暂无推荐数据
        </div>
        <div v-if="recLoading" class="list-item">
          加载中...
        </div>
      </div>
      <div v-if="recMessage" class="rec-message">{{ recMessage }}</div>
    </div>
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

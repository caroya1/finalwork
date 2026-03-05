<template>
  <section class="hero">
    <div class="hero-card">
      <h2>智能推荐</h2>
      <p>基于您的偏好与场景，为您推荐最合适的商户。</p>
      <div class="form-grid">
        <input v-model="form.scene" placeholder="输入场景，例如：带老人聚餐、朋友约火锅" />
        <button class="cta" @click="loadRecommendations">获取推荐</button>
      </div>
      <div v-if="recMessage" class="rec-message">{{ recMessage }}</div>
    </div>
    <div class="hero-card">
      <h2>发布帖子</h2>
      <p>分享你的真实体验，记录城市里的美好瞬间。</p>
      <button class="cta" @click="goCreatePost">去发布</button>
      <div v-if="!isLoggedIn" class="hint">登录后可发布帖子</div>
    </div>
    <div class="hero-card">
      <h2>推荐结果</h2>
      <div class="list">
        <RouterLink v-for="shop in recommendations" :key="shop.id" class="list-item list-item-link" :to="`/shops/${shop.id}`">
          <strong>{{ shop.name }}</strong>
          <div>
            <span class="tag">{{ shop.category || "综合" }}</span>
            <span class="tag">{{ shop.city }}</span>
            <span v-if="shop.rating" class="tag">{{ shop.rating }}分</span>
          </div>
        </RouterLink>
        <div v-if="recommendations.length === 0" class="list-item">
          暂无推荐，请输入场景后获取。
        </div>
      </div>
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

const form = ref({
  scene: ""
});

const recommendations = ref([]);
const recMessage = ref("");
const isLoggedIn = ref(false);

const categories = ref([
  { icon: "🍜", label: "美食", desc: "今日爆款", query: "美食" },
  { icon: "🏞", label: "景点/周边游", desc: "轻松出发", query: "休闲,景点,周边游" },
  { icon: "🏨", label: "酒店/民宿", desc: "安心住", query: "酒店,民宿" },
  { icon: "🎮", label: "休闲/玩乐", desc: "好去处", query: "休闲" },
  { icon: "🎬", label: "猫眼电影", desc: "热映中", query: "电影" }
]);

const feeds = ref([]);

const loadRecommendations = async () => {
  recMessage.value = "";
  const userId = localStorage.getItem("dp_user_id");
  if (!userId) {
    recMessage.value = "请先登录后获取个性化推荐";
    return;
  }
  const city = localStorage.getItem("dp_city") || "上海";
  const payload = {
    userId: Number(userId),
    city: city,
    scene: form.value.scene || null
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
  } catch {
    recMessage.value = "获取推荐失败，请稍后重试";
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
  loadPosts(event.detail || "");
};

onMounted(() => {
  const token = localStorage.getItem("dp_token");
  const refreshToken = localStorage.getItem("dp_refresh_token");
  isLoggedIn.value = !!token && !!refreshToken;
  loadPosts("");
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

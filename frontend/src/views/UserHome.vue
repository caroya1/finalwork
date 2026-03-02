<template>
  <section class="hero">
    <div class="hero-card">
      <h2>账号与推荐</h2>
      <p>登录后可刷新推荐列表，搜索已集成在顶部搜索栏。</p>
      <div class="form-grid">
        <input v-model="form.userId" placeholder="用户ID（登录后自动填充）" />
        <input v-model="form.city" placeholder="城市（建议英文）" />
        <input v-model="form.scene" placeholder="场景，例如：带老人聚餐" />
        <button class="cta" @click="loadRecommendations">刷新推荐</button>
      </div>
    </div>
    <div class="hero-card">
      <h2>推荐结果</h2>
      <div class="list">
        <div v-for="shop in recommendations" :key="shop.id" class="list-item">
          <strong>{{ shop.name }}</strong>
          <div>
            <span class="tag">{{ shop.category || "综合" }}</span>
            <span class="tag">{{ shop.city }}</span>
          </div>
        </div>
        <div v-if="recommendations.length === 0" class="list-item">
          暂无推荐，填写信息后获取。
        </div>
      </div>
    </div>
  </section>

  <section class="category-grid">
    <div class="category-card" v-for="item in categories" :key="item.label">
      <div class="category-icon">{{ item.icon }}</div>
      <div>
        <div>{{ item.label }}</div>
        <div class="feed-meta">{{ item.desc }}</div>
      </div>
    </div>
  </section>

  <section class="panel-grid">
    <div class="panel">
      <h2>身份鉴权</h2>
      <div class="form-grid">
        <input v-model="auth.username" placeholder="用户名" />
        <input v-model="auth.password" type="password" placeholder="密码" />
        <button class="cta" @click="doLogin">登录</button>
        <span>{{ authMessage }}</span>
      </div>
    </div>
    <div class="panel">
      <h2>快速注册</h2>
      <div class="form-grid">
        <input v-model="registerForm.username" placeholder="用户名" />
        <input v-model="registerForm.email" placeholder="邮箱" />
        <input v-model="registerForm.phone" placeholder="手机号" />
        <input v-model="registerForm.password" type="password" placeholder="密码" />
        <button class="cta" @click="doRegister">注册</button>
        <span>{{ registerMessage }}</span>
      </div>
    </div>
    <div class="panel">
      <h2>推荐引擎</h2>
      <p>基于用户行为与场景数据的个性化推荐。</p>
    </div>
    <div class="panel">
      <h2>商户详情</h2>
      <p>展示评分、口碑摘要与相关推荐。</p>
    </div>
    <div class="panel">
      <h2>社区互动</h2>
      <p>关注、话题讨论、榜单投票沉淀用户行为。</p>
    </div>
  </section>

  <section class="feed-grid">
    <div class="feed-card" v-for="item in feeds" :key="item.id">
      <div class="feed-image"></div>
      <div class="feed-body">
        <h3 class="feed-title">{{ item.title }}</h3>
        <div class="feed-meta">
          <span>{{ item.city || "-" }}</span>
          <span>likes {{ item.likes || 0 }}</span>
        </div>
      </div>
    </div>
  </section>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount } from "vue";
import { fetchRecommendations } from "../api/recommendation";
import { listPosts } from "../api/post";
import { login } from "../api/auth";
import { register } from "../api/user";

const form = ref({
  userId: "",
  city: "Shanghai",
  scene: ""
});

const recommendations = ref([]);
const categories = ref([
  { icon: "FOOD", label: "美食", desc: "今日爆款" },
  { icon: "TRIP", label: "景点/周边游", desc: "轻松出发" },
  { icon: "HOTEL", label: "酒店/民宿", desc: "安心住" },
  { icon: "FUN", label: "休闲/玩乐", desc: "好去处" },
  { icon: "MOVIE", label: "猫眼电影", desc: "热映中" }
]);
const feeds = ref([]);
const auth = ref({
  username: "",
  password: ""
});
const registerForm = ref({
  username: "",
  email: "",
  phone: "",
  password: ""
});
const authMessage = ref("");
const registerMessage = ref("");

const doLogin = async () => {
  authMessage.value = "";
  try {
    const response = await login(auth.value);
    if (response.success) {
      localStorage.setItem("dp_token", response.data.token);
      form.value.userId = String(response.data.userId || "");
      authMessage.value = "登录成功";
    } else {
      authMessage.value = response.message || "登录失败";
    }
  } catch (error) {
    authMessage.value = "登录失败";
  }
};

const doRegister = async () => {
  registerMessage.value = "";
  try {
    const response = await register(registerForm.value);
    if (response.success) {
      form.value.userId = String(response.data.id || "");
      registerMessage.value = "注册成功";
    } else {
      registerMessage.value = response.message || "注册失败";
    }
  } catch (error) {
    registerMessage.value = "注册失败";
  }
};

const loadRecommendations = async () => {
  if (!form.value.userId) {
    authMessage.value = "请先登录";
    return;
  }
  const payload = {
    userId: Number(form.value.userId),
    city: form.value.city,
    scene: form.value.scene || null
  };
  const response = await fetchRecommendations(payload);
  if (response.success) {
    recommendations.value = response.data || [];
  }
};

const loadPosts = async (keyword) => {
  const response = await listPosts({ city: form.value.city, keyword });
  if (response.success) {
    feeds.value = response.data || [];
  }
};

const handleSearch = (event) => {
  loadPosts(event.detail || "");
};

onMounted(() => {
  loadPosts("");
  window.addEventListener("dp-search", handleSearch);
});

onBeforeUnmount(() => {
  window.removeEventListener("dp-search", handleSearch);
});
</script>

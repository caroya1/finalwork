<template>
  <div class="app-shell">
    <header class="app-header">
      <RouterLink class="brand brand-link" to="/">
        <span class="brand-mark">DP</span>
        <div>
          <h1>类大众点评</h1>
          <p>发现 · 评价 · 到店</p>
        </div>
      </RouterLink>
      <div class="header-search">
        <div class="location" @click="openCityPicker" title="点击切换城市">
          <span>{{ currentCity }}</span>
          <span class="location-arrow">▼</span>
        </div>
        <input
          class="search-input"
          v-model="keyword"
          :placeholder="searchMode === 'recommend' ? '输入场景，例如：朋友聚餐、亲子游' : '输入商户名、地点或菜品'"
          @keyup.enter="emitSearch"
        />
        <div class="search-mode">
          <button
            :class="['mode-btn', searchMode === 'search' ? 'active' : '']"
            @click="setSearchMode('search')"
          >搜索</button>
          <button
            :class="['mode-btn', searchMode === 'recommend' ? 'active' : '']"
            @click="setSearchMode('recommend')"
          >智能推荐</button>
        </div>
        <button class="search-btn" @click="emitSearch">
          {{ searchMode === 'recommend' ? '推荐' : '搜索' }}
        </button>
      </div>
      <nav class="nav">
        <RouterLink to="/">发现</RouterLink>
      </nav>
      <div class="header-actions" v-if="!isLoggedIn">
        <button class="ghost-btn" @click="openAuth('login')">登录</button>
        <button class="cta" @click="openAuth('register')">注册</button>
      </div>
      <div class="header-actions" v-else>
        <button class="ghost-btn" @click="goCreatePost">发布帖子</button>
        <RouterLink class="user-info user-link" to="/profile">{{ currentUsername }}</RouterLink>
        <button class="ghost-btn" @click="doLogout">退出</button>
      </div>
    </header>
    <main class="app-main">
      <RouterView />
    </main>

    <!-- 登录/注册弹窗 -->
    <div v-if="authOpen" class="auth-overlay" @click.self="closeAuth">
      <div class="auth-card">
        <div class="auth-header">
          <h2>{{ authMode === 'login' ? '登录' : '注册' }}</h2>
          <button class="auth-close" @click="closeAuth">&times;</button>
        </div>
        <div class="auth-tabs">
          <button
            :class="['auth-tab', authMode === 'login' ? 'active' : '']"
            @click="switchMode('login')"
          >登录</button>
          <button
            :class="['auth-tab', authMode === 'register' ? 'active' : '']"
            @click="switchMode('register')"
          >注册</button>
        </div>
        <div class="form-grid">
          <input v-model="authForm.username" placeholder="用户名" />
          <input v-model="authForm.password" type="password" placeholder="密码" />
          <template v-if="authMode === 'register'">
            <input v-model="authForm.email" placeholder="邮箱" />
            <input v-model="authForm.phone" placeholder="手机号" />
            <select v-model="authForm.city" class="form-select">
              <option value="" disabled>选择默认城市</option>
              <option v-for="c in cityOptions" :key="c" :value="c">{{ c }}</option>
            </select>
          </template>
          <button class="cta auth-submit" @click="submitAuth">
            {{ authMode === 'login' ? '登录' : '注册并登录' }}
          </button>
          <div v-if="authMessage" class="auth-message" :class="authMessageType">
            {{ authMessage }}
          </div>
        </div>
      </div>
    </div>

    <!-- 城市选择弹窗 -->
    <div v-if="cityPickerOpen" class="auth-overlay" @click.self="closeCityPicker">
      <div class="auth-card city-picker-card">
        <div class="auth-header">
          <h2>选择城市</h2>
          <button class="auth-close" @click="closeCityPicker">&times;</button>
        </div>
        <div class="city-grid">
          <button
            v-for="c in cityOptions"
            :key="c"
            :class="['city-item', currentCity === c ? 'active' : '']"
            @click="selectCity(c)"
          >{{ c }}</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from "vue";
import { onBeforeUnmount } from "vue";
import { useRouter } from "vue-router";
import { RouterLink, RouterView } from "vue-router";
import { login, logout } from "./api/auth";
import { register, updateCity } from "./api/user";

const router = useRouter();
const keyword = ref("");
const searchMode = ref("search");

/* ---------- 城市选项 ---------- */
const cityOptions = [
  "上海", "北京", "广州", "深圳", "杭州",
  "南京", "成都", "重庆", "武汉", "西安",
  "长沙", "天津", "苏州", "厦门", "青岛"
];

/* ---------- 登录状态 ---------- */
const isLoggedIn = ref(false);
const currentUsername = ref("");
const currentCity = ref("上海");

const checkLoginState = () => {
  const token = localStorage.getItem("dp_token");
  const refreshToken = localStorage.getItem("dp_refresh_token");
  const username = localStorage.getItem("dp_username");
  const city = localStorage.getItem("dp_city");
  isLoggedIn.value = !!token && !!refreshToken;
  currentUsername.value = username || "";
  currentCity.value = city || "上海";
};

onMounted(() => {
  checkLoginState();
  window.addEventListener("dp-auth-required", openLoginModal);
});

onBeforeUnmount(() => {
  window.removeEventListener("dp-auth-required", openLoginModal);
});

/* ---------- 搜索 ---------- */
const emitSearch = () => {
  window.dispatchEvent(new CustomEvent("dp-search", {
    detail: { keyword: keyword.value, mode: searchMode.value }
  }));
};

const setSearchMode = (mode) => {
  searchMode.value = mode;
  emitSearch();
};

/* ---------- 登录/注册弹窗 ---------- */
const authOpen = ref(false);
const authMode = ref("login");
const authForm = ref({
  username: "",
  password: "",
  email: "",
  phone: "",
  city: "上海"
});
const authMessage = ref("");
const authMessageType = ref("");

const openAuth = (mode) => {
  authMode.value = mode;
  authMessage.value = "";
  authMessageType.value = "";
  authForm.value = {
    username: "",
    password: "",
    email: "",
    phone: "",
    city: "上海"
  };
  authOpen.value = true;
};

const switchMode = (mode) => {
  authMode.value = mode;
  authMessage.value = "";
  authMessageType.value = "";
};

const closeAuth = () => {
  authOpen.value = false;
};

const openLoginModal = () => {
  openAuth("login");
};

const goCreatePost = () => {
  router.push("/posts/new");
};

const submitAuth = async () => {
  authMessage.value = "";
  authMessageType.value = "";

  if (!authForm.value.username || !authForm.value.password) {
    authMessage.value = "请填写用户名和密码";
    authMessageType.value = "error";
    return;
  }

  try {
    /* 注册流程 */
    if (authMode.value === "register") {
      const regResp = await register({
        username: authForm.value.username,
        password: authForm.value.password,
        email: authForm.value.email,
        phone: authForm.value.phone,
        city: authForm.value.city || "上海",
        role: "user"
      });
      if (!regResp.success) {
        authMessage.value = regResp.message || "注册失败";
        authMessageType.value = "error";
        return;
      }
    }

    /* 登录流程（注册成功后自动登录） */
    const loginResp = await login({
      username: authForm.value.username,
      password: authForm.value.password
    });

    if (!loginResp.success) {
      authMessage.value = loginResp.message || "登录失败";
      authMessageType.value = "error";
      return;
    }

    /* 保存登录信息 */
    localStorage.setItem("dp_token", loginResp.data.token);
    if (loginResp.data.refreshToken) {
      localStorage.setItem("dp_refresh_token", loginResp.data.refreshToken);
    }
    localStorage.setItem("dp_user_id", String(loginResp.data.userId));
    localStorage.setItem("dp_username", authForm.value.username);
    localStorage.setItem("dp_role", "user");
    if (loginResp.data.balance != null) {
      localStorage.setItem("dp_balance", String(loginResp.data.balance));
    }

    /* 城市逻辑：登录时后端返回用户在数据库中的城市 */
    const userCity = loginResp.data.city || "上海";
    localStorage.setItem("dp_city", userCity);
    currentCity.value = userCity;

    /* 如果是注册且选了城市，同步到后端 */
    if (authMode.value === "register" && authForm.value.city) {
      await updateCity(loginResp.data.userId, authForm.value.city);
      localStorage.setItem("dp_city", authForm.value.city);
      currentCity.value = authForm.value.city;
    }

    /* 更新状态 */
    isLoggedIn.value = true;
    currentUsername.value = authForm.value.username;
    authOpen.value = false;

    /* 按角色导航 */
    router.push("/");
  } catch (err) {
    authMessage.value = "操作失败，请检查网络后重试";
    authMessageType.value = "error";
  }
};

/* ---------- 退出登录 ---------- */
const doLogout = async () => {
  const refreshToken = localStorage.getItem("dp_refresh_token");
  const accessToken = localStorage.getItem("dp_token");
  if (refreshToken || accessToken) {
    try {
      await logout(refreshToken || null, accessToken || null);
    } catch {
      // ignore logout errors
    }
  }
  localStorage.removeItem("dp_token");
  localStorage.removeItem("dp_user_id");
  localStorage.removeItem("dp_refresh_token");
  localStorage.removeItem("dp_username");
  localStorage.removeItem("dp_role");
  localStorage.removeItem("dp_city");
  localStorage.removeItem("dp_balance");
  isLoggedIn.value = false;
  currentUsername.value = "";
  currentCity.value = "上海";
  router.push("/");
};

/* ---------- 城市选择 ---------- */
const cityPickerOpen = ref(false);

const openCityPicker = () => {
  cityPickerOpen.value = true;
};

const closeCityPicker = () => {
  cityPickerOpen.value = false;
};

const selectCity = async (city) => {
  currentCity.value = city;
  localStorage.setItem("dp_city", city);
  cityPickerOpen.value = false;

  /* 已登录用户同步城市到后端 */
  if (isLoggedIn.value) {
    const userId = localStorage.getItem("dp_user_id");
    if (userId) {
      try {
        await updateCity(Number(userId), city);
      } catch {
        // 静默失败，本地已更新
      }
    }
  }

  /* 触发重新搜索 */
  emitSearch();
};
</script>

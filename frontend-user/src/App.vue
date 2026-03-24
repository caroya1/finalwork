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
          <input v-model="authForm.username" placeholder="用户名 / 手机 / 邮箱" />
          <input v-model="authForm.password" :type="showPassword ? 'text' : 'password'" placeholder="密码" />
          <label class="password-toggle">
            <input v-model="showPassword" type="checkbox" />
            显示密码
          </label>
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
  const current = router.currentRoute.value;
  if (searchMode.value === "search") {
    if (current.path !== "/search") {
      router.push({
        path: "/search",
        query: {
          keyword: keyword.value || "",
          city: currentCity.value
        }
      });
      return;
    }
    router.replace({
      path: "/search",
      query: {
        keyword: keyword.value || "",
        city: currentCity.value
      }
    });
    return;
  }
  window.dispatchEvent(new CustomEvent("dp-search", {
    detail: { keyword: keyword.value, mode: searchMode.value }
  }));
};

const refreshCurrentRouteByCity = (city) => {
  const current = router.currentRoute.value;
  if (current.path === "/search") {
    router.replace({
      path: "/search",
      query: {
        ...current.query,
        city
      }
    });
    return;
  }
  if (current.path === "/category") {
    router.replace({
      path: "/category",
      query: {
        ...current.query,
        city
      }
    });
    return;
  }
  if (current.path === "/") {
    window.dispatchEvent(new CustomEvent("dp-search", {
      detail: { keyword: "", mode: "search" }
    }));
  }
};

const setSearchMode = (mode) => {
  searchMode.value = mode;
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
const showPassword = ref(false);

const openAuth = (mode) => {
  authMode.value = mode;
  authMessage.value = "";
  authMessageType.value = "";
  showPassword.value = false;
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
  showPassword.value = false;
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
    if (loginResp.data.username) {
      localStorage.setItem("dp_username", loginResp.data.username);
    } else {
      localStorage.setItem("dp_username", authForm.value.username);
    }
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

    /* 通知主页刷新数据 */
    window.dispatchEvent(new CustomEvent("dp-auth-change", { detail: { type: "login" } }));
    
    /* 跳转到主页并刷新 */
    if (router.currentRoute.value.path === "/") {
      window.location.reload();
    } else {
      router.push("/");
    }
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
  
  /* 通知主页刷新数据 */
  window.dispatchEvent(new CustomEvent("dp-auth-change", { detail: { type: "logout" } }));
  
  /* 跳转到主页并刷新 */
  if (router.currentRoute.value.path === "/") {
    window.location.reload();
  } else {
    router.push("/");
  }
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

  if (isLoggedIn.value) {
    const userId = localStorage.getItem("dp_user_id");
    if (userId) {
      try {
        await updateCity(Number(userId), city);
      } catch {
      }
    }
  }

  refreshCurrentRouteByCity(city);
};
</script>

<style scoped>
.app-shell {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: var(--bg-secondary);
}

.app-header {
  position: sticky;
  top: 0;
  z-index: var(--z-sticky);
  display: grid;
  grid-template-columns: auto 1fr auto auto;
  align-items: center;
  gap: var(--space-6);
  padding: var(--space-4) var(--space-8);
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(12px);
  border-bottom: 1px solid var(--border-light);
}

.brand {
  display: flex;
  align-items: center;
  gap: var(--space-3);
}

.brand-link {
  display: inline-flex;
  align-items: center;
  gap: var(--space-3);
  transition: opacity var(--transition-fast);
}

.brand-link:hover {
  opacity: 0.85;
}

.brand-mark {
  width: 44px;
  height: 44px;
  border-radius: var(--radius-xl);
  display: grid;
  place-items: center;
  background: var(--brand-gradient);
  color: var(--text-inverse);
  font-weight: var(--font-bold);
  font-size: var(--text-lg);
  letter-spacing: 0.5px;
  box-shadow: var(--shadow-brand);
  transition: transform var(--transition-base);
}

.brand-link:hover .brand-mark {
  transform: scale(1.05);
}

.brand h1 {
  font-size: var(--text-lg);
  font-weight: var(--font-semibold);
  color: var(--text-primary);
  letter-spacing: -0.02em;
  margin: 0;
}

.brand p {
  margin: var(--space-1) 0 0;
  font-size: var(--text-xs);
  color: var(--text-tertiary);
  letter-spacing: 0.05em;
}

.header-search {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  max-width: 560px;
  width: 100%;
  background: var(--bg-primary);
  border: 2px solid var(--border-light);
  border-radius: var(--radius-full);
  padding: var(--space-1) var(--space-2) var(--space-1) var(--space-4);
  transition: all var(--transition-base);
}

.header-search:focus-within {
  border-color: var(--brand-primary);
  box-shadow: 0 0 0 4px var(--brand-primary-light);
}

.location {
  display: flex;
  align-items: center;
  gap: var(--space-1);
  font-size: var(--text-sm);
  font-weight: var(--font-medium);
  color: var(--brand-primary);
  cursor: pointer;
  padding: var(--space-1) var(--space-2);
  border-radius: var(--radius-md);
  transition: all var(--transition-fast);
  white-space: nowrap;
  user-select: none;
}

.location:hover {
  background: var(--brand-primary-light);
}

.location-arrow {
  font-size: 0.5rem;
  opacity: 0.6;
  transition: transform var(--transition-fast);
}

.location:hover .location-arrow {
  transform: translateY(2px);
}

.search-input {
  flex: 1;
  min-width: 0;
  border: none;
  outline: none;
  font-size: var(--text-sm);
  color: var(--text-primary);
  background: transparent;
}

.search-input::placeholder {
  color: var(--text-placeholder);
}

.search-mode {
  display: flex;
  gap: var(--space-1);
  padding: var(--space-1);
  background: var(--bg-secondary);
  border-radius: var(--radius-full);
}

.mode-btn {
  padding: var(--space-1) var(--space-3);
  border: none;
  background: transparent;
  color: var(--text-tertiary);
  font-size: var(--text-xs);
  font-weight: var(--font-medium);
  border-radius: var(--radius-full);
  transition: all var(--transition-fast);
}

.mode-btn:hover {
  color: var(--brand-primary);
}

.mode-btn:focus-visible {
  outline: 2px solid var(--brand-primary);
  outline-offset: 2px;
}

.mode-btn:active {
  transform: scale(0.97);
}

.mode-btn.active {
  background: var(--bg-primary);
  color: var(--brand-primary);
  box-shadow: var(--shadow-sm);
}

.search-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: var(--space-2);
  padding: var(--space-2) var(--space-5);
  border: none;
  border-radius: var(--radius-full);
  background: var(--brand-gradient);
  color: var(--text-inverse);
  font-size: var(--text-sm);
  font-weight: var(--font-medium);
  transition: all var(--transition-base);
}

.search-btn:hover {
  transform: translateY(-1px);
  box-shadow: var(--shadow-brand);
}

.search-btn:focus-visible {
  outline: 2px solid var(--brand-primary);
  outline-offset: 2px;
}

.search-btn:active {
  transform: translateY(0) scale(0.98);
}

.nav {
  display: flex;
  align-items: center;
  gap: var(--space-2);
}

.nav a {
  position: relative;
  padding: var(--space-2) var(--space-4);
  font-size: var(--text-sm);
  font-weight: var(--font-medium);
  color: var(--text-secondary);
  border-radius: var(--radius-full);
  transition: all var(--transition-fast);
}

.nav a:hover {
  color: var(--brand-primary);
  background: var(--brand-primary-lighter);
}

.nav a.router-link-active {
  color: var(--brand-primary);
  background: var(--brand-primary-light);
}

.header-actions {
  display: flex;
  align-items: center;
  gap: var(--space-3);
}

.user-info {
  font-size: var(--text-sm);
  font-weight: var(--font-medium);
  color: var(--text-primary);
}

.user-link {
  padding: var(--space-2) var(--space-3);
  border-radius: var(--radius-full);
  background: var(--brand-primary-light);
  transition: all var(--transition-fast);
}

.user-link:hover {
  background: var(--brand-primary-light);
  color: var(--brand-primary);
}

.app-main {
  flex: 1;
  padding: var(--space-8);
  max-width: var(--max-w-2xl);
  margin: 0 auto;
  width: 100%;
}

.auth-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.5);
  backdrop-filter: blur(4px);
  display: grid;
  place-items: center;
  z-index: var(--z-modal-backdrop);
  animation: fadeIn var(--duration-200) var(--ease-out);
}

@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

.auth-card {
  background: var(--bg-primary);
  border-radius: var(--radius-3xl);
  padding: var(--space-8);
  width: 440px;
  max-width: 92vw;
  max-height: 90vh;
  overflow-y: auto;
  box-shadow: var(--shadow-2xl);
  animation: slideUp var(--duration-300) var(--ease-out);
}

@keyframes slideUp {
  from { 
    opacity: 0; 
    transform: translateY(24px) scale(0.96); 
  }
  to { 
    opacity: 1; 
    transform: translateY(0) scale(1); 
  }
}

.auth-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--space-5);
}

.auth-header h2 {
  font-size: var(--text-2xl);
  font-weight: var(--font-semibold);
  color: var(--text-primary);
  letter-spacing: -0.02em;
  margin: 0;
}

.auth-close {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  border: none;
  background: var(--bg-secondary);
  font-size: var(--text-xl);
  color: var(--text-tertiary);
  display: grid;
  place-items: center;
  transition: all var(--transition-fast);
}

.auth-close:hover {
  background: var(--bg-tertiary);
  color: var(--text-primary);
}

.auth-tabs {
  display: flex;
  gap: 0;
  margin-bottom: var(--space-5);
  background: var(--bg-secondary);
  border-radius: var(--radius-xl);
  padding: var(--space-1);
}

.auth-tab {
  flex: 1;
  padding: var(--space-3);
  border: none;
  background: transparent;
  border-radius: var(--radius-lg);
  font-size: var(--text-sm);
  font-weight: var(--font-medium);
  color: var(--text-tertiary);
  transition: all var(--transition-fast);
}

.auth-tab:hover {
  color: var(--text-primary);
}

.auth-tab.active {
  background: var(--bg-primary);
  color: var(--brand-primary);
  box-shadow: var(--shadow-sm);
}

.form-grid {
  display: flex;
  flex-direction: column;
  gap: var(--space-3);
}

.form-grid input,
.form-grid select {
  width: 100%;
  padding: var(--space-3) var(--space-4);
  border: 1.5px solid var(--border-default);
  border-radius: var(--radius-xl);
  font-size: var(--text-sm);
  color: var(--text-primary);
  background: var(--bg-primary);
  transition: all var(--transition-fast);
}

.form-grid input:hover,
.form-grid select:hover {
  border-color: var(--neutral-400);
}

.form-grid input:focus,
.form-grid select:focus {
  outline: none;
  border-color: var(--brand-primary);
  box-shadow: 0 0 0 4px var(--brand-primary-light);
}

.form-grid input::placeholder {
  color: var(--text-placeholder);
}

.form-select {
  width: 100%;
  padding: var(--space-3) var(--space-4);
  border: 1.5px solid var(--border-default);
  border-radius: var(--radius-xl);
  font-size: var(--text-sm);
  color: var(--text-primary);
  background: var(--bg-primary);
  cursor: pointer;
  transition: all var(--transition-fast);
}

.form-select:focus {
  outline: none;
  border-color: var(--brand-primary);
  box-shadow: 0 0 0 4px var(--brand-primary-light);
}

.password-toggle {
  display: inline-flex;
  align-items: center;
  gap: var(--space-2);
  font-size: var(--text-sm);
  color: var(--text-secondary);
}

.password-toggle input {
  width: 18px;
  height: 18px;
  accent-color: var(--brand-primary);
}

.auth-submit {
  width: 100%;
  margin-top: var(--space-2);
  padding: var(--space-3);
  font-size: var(--text-base);
}

.auth-message {
  text-align: center;
  padding: var(--space-3);
  border-radius: var(--radius-lg);
  font-size: var(--text-sm);
}

.auth-message.error {
  color: var(--color-danger);
  background: var(--color-danger-light);
}

.auth-message.success {
  color: var(--color-success);
  background: var(--color-success-light);
}

.city-picker-card {
  width: 520px;
}

.city-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: var(--space-3);
}

.city-item {
  padding: var(--space-3) var(--space-4);
  border: 1.5px solid var(--border-light);
  border-radius: var(--radius-xl);
  background: var(--bg-primary);
  font-size: var(--text-sm);
  font-weight: var(--font-medium);
  color: var(--text-secondary);
  text-align: center;
  transition: all var(--transition-fast);
}

.city-item:hover {
  border-color: var(--brand-primary);
  color: var(--brand-primary);
  background: var(--brand-primary-lighter);
}

.city-item:focus-visible {
  outline: 2px solid var(--brand-primary);
  outline-offset: 2px;
}

.city-item:active {
  transform: scale(0.98);
}

.city-item.active {
  border-color: var(--brand-primary);
  background: var(--brand-primary-light);
  color: var(--brand-primary);
  font-weight: var(--font-semibold);
}

@media (max-width: 768px) {
  .app-header {
    grid-template-columns: 1fr;
    gap: var(--space-4);
    padding: var(--space-4);
  }
  
  .header-search {
    order: 2;
    max-width: none;
  }
  
  .nav {
    order: 3;
    justify-content: center;
  }
  
  .header-actions {
    order: 4;
    justify-content: center;
  }
  
  .app-main {
    padding: var(--space-6) var(--space-4);
  }
  
  .city-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  
  .auth-card {
    padding: var(--space-6);
    border-radius: var(--radius-2xl);
  }
}

@media (max-width: 480px) {
  .brand h1 {
    font-size: var(--text-base);
  }
  
  .brand p {
    display: none;
  }
  
  .search-mode {
    display: none;
  }
  
  .city-grid {
    grid-template-columns: 1fr;
  }
}
</style>

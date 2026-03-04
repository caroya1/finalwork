<template>
  <div class="app-shell">
    <header class="app-header">
      <div class="brand">
        <span class="brand-mark">DP</span>
        <div>
          <h1>运营后台</h1>
          <p>订单 · 运营 · 风控</p>
        </div>
      </div>
      <nav class="nav">
        <RouterLink to="/">订单中心</RouterLink>
      </nav>
      <div class="header-actions" v-if="!isLoggedIn">
        <button class="ghost-btn" @click="openAuth('login')">登录</button>
        <button class="cta" @click="openAuth('register')">注册</button>
      </div>
      <div class="header-actions" v-else>
        <span class="user-info">{{ currentUsername }}</span>
        <button class="ghost-btn" @click="doLogout">退出</button>
      </div>
    </header>
    <main class="app-main">
      <RouterView />
    </main>

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
  </div>
</template>

<script setup>
import { ref, onMounted } from "vue";
import { useRouter, RouterLink, RouterView } from "vue-router";
import { login, logout } from "./api/auth";
import { register, updateCity } from "./api/user";

const router = useRouter();
const cityOptions = [
  "上海", "北京", "广州", "深圳", "杭州",
  "南京", "成都", "重庆", "武汉", "西安",
  "长沙", "天津", "苏州", "厦门", "青岛"
];

const isLoggedIn = ref(false);
const currentUsername = ref("");

const checkLoginState = () => {
  const token = localStorage.getItem("dp_token");
  const refreshToken = localStorage.getItem("dp_refresh_token");
  const username = localStorage.getItem("dp_username");
  isLoggedIn.value = !!token && !!refreshToken;
  currentUsername.value = username || "";
};

onMounted(checkLoginState);

const authOpen = ref(false);
const authMode = ref("login");
const authForm = ref({
  username: "",
  password: "",
  email: "",
  phone: "",
  city: "上海",
  role: "admin"
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
    city: "上海",
    role: "admin"
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

const submitAuth = async () => {
  authMessage.value = "";
  authMessageType.value = "";

  if (!authForm.value.username || !authForm.value.password) {
    authMessage.value = "请填写用户名和密码";
    authMessageType.value = "error";
    return;
  }

  try {
    if (authMode.value === "register") {
      const regResp = await register({
        username: authForm.value.username,
        password: authForm.value.password,
        email: authForm.value.email,
        phone: authForm.value.phone,
        city: authForm.value.city || "上海",
        role: authForm.value.role
      });
      if (!regResp.success) {
        authMessage.value = regResp.message || "注册失败";
        authMessageType.value = "error";
        return;
      }
    }

    const loginResp = await login({
      username: authForm.value.username,
      password: authForm.value.password
    });

    if (!loginResp.success) {
      authMessage.value = loginResp.message || "登录失败";
      authMessageType.value = "error";
      return;
    }

    localStorage.setItem("dp_token", loginResp.data.token);
    if (loginResp.data.refreshToken) {
      localStorage.setItem("dp_refresh_token", loginResp.data.refreshToken);
    }
    localStorage.setItem("dp_user_id", String(loginResp.data.userId));
    localStorage.setItem("dp_username", authForm.value.username);
    localStorage.setItem("dp_role", "admin");

    const userCity = loginResp.data.city || "上海";
    localStorage.setItem("dp_city", userCity);

    if (authMode.value === "register" && authForm.value.city) {
      await updateCity(loginResp.data.userId, authForm.value.city);
      localStorage.setItem("dp_city", authForm.value.city);
    }

    isLoggedIn.value = true;
    currentUsername.value = authForm.value.username;
    authOpen.value = false;
    router.push("/");
  } catch {
    authMessage.value = "操作失败，请检查网络后重试";
    authMessageType.value = "error";
  }
};

const doLogout = async () => {
  const refreshToken = localStorage.getItem("dp_refresh_token");
  const accessToken = localStorage.getItem("dp_token");
  if (refreshToken || accessToken) {
    try {
      await logout(refreshToken || null, accessToken || null);
    } catch {
      // ignore
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
  router.push("/");
};
</script>

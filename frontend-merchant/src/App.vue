<template>
  <div class="app-shell">
    <header class="app-header">
      <div class="brand">
        <span class="brand-mark">DP</span>
        <div>
          <h1>商户中心</h1>
          <p>门店 · 优惠券 · 运营</p>
        </div>
      </div>
      <nav class="nav">
        <RouterLink to="/">门店管理</RouterLink>
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
          <template v-if="authMode === 'register'">
            <input v-model="authForm.name" placeholder="商户名称" />
            <input v-model="authForm.contactName" placeholder="联系人" />
            <input v-model="authForm.contactPhone" placeholder="联系电话" />
            <input v-model="authForm.category" placeholder="商户分类" />
          </template>
          <input v-model="authForm.email" placeholder="邮箱" />
          <input v-model="authForm.password" type="password" placeholder="密码" />
          <template v-if="authMode === 'register'">
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
import { logout } from "./api/auth";
import { merchantLogin, merchantRegister } from "./api/merchant";

const router = useRouter();
const cityOptions = [
  "上海", "北京", "广州", "深圳", "杭州",
  "南京", "成都", "重庆", "武汉", "西安",
  "长沙", "天津", "苏州", "厦门", "青岛"
];

const isLoggedIn = ref(false);
const currentUsername = ref("");

const checkLoginState = () => {
  const token = localStorage.getItem("dp_token") || localStorage.getItem("dp_merchant_token");
  const merchantId = localStorage.getItem("dp_merchant_id");
  isLoggedIn.value = !!(token && merchantId);
  currentUsername.value = localStorage.getItem("dp_username") || localStorage.getItem("dp_merchant_name") || "";
};

onMounted(checkLoginState);

const authOpen = ref(false);
const authMode = ref("login");
const authForm = ref({
  name: "",
  contactName: "",
  contactPhone: "",
  email: "",
  password: "",
  city: "上海",
  category: ""
});
const authMessage = ref("");
const authMessageType = ref("");

const openAuth = (mode) => {
  authMode.value = mode;
  authMessage.value = "";
  authMessageType.value = "";
  authForm.value = {
    name: "",
    contactName: "",
    contactPhone: "",
    email: "",
    password: "",
    city: "上海",
    category: ""
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

  if (!authForm.value.email || !authForm.value.password) {
    authMessage.value = "请填写邮箱和密码";
    authMessageType.value = "error";
    return;
  }

  try {
    if (authMode.value === "register") {
      const regResp = await merchantRegister({
        name: authForm.value.name,
        contactName: authForm.value.contactName,
        contactPhone: authForm.value.contactPhone,
        email: authForm.value.email,
        password: authForm.value.password,
        city: authForm.value.city || "上海",
        category: authForm.value.category
      });
      if (!regResp.success) {
        authMessage.value = regResp.message || "注册失败";
        authMessageType.value = "error";
        return;
      }
    }

    const loginResp = await merchantLogin({
      email: authForm.value.email,
      password: authForm.value.password
    });

    if (!loginResp.success) {
      authMessage.value = loginResp.message || "登录失败";
      authMessageType.value = "error";
      return;
    }

    localStorage.setItem("dp_token", loginResp.data.token);
    localStorage.setItem("dp_merchant_token", loginResp.data.token);
    localStorage.setItem("dp_merchant_id", String(loginResp.data.merchantId));
    localStorage.setItem("dp_merchant_name", loginResp.data.name || "");
    localStorage.setItem("dp_merchant_email", loginResp.data.email || authForm.value.email);
    if (loginResp.data.refreshToken) {
      localStorage.setItem("dp_refresh_token", loginResp.data.refreshToken);
    }
    localStorage.setItem("dp_user_id", String(loginResp.data.merchantId));
    localStorage.setItem("dp_username", loginResp.data.name || authForm.value.email);
    localStorage.setItem("dp_role", "merchant");
    localStorage.setItem("dp_city", authForm.value.city || "上海");

    isLoggedIn.value = true;
    currentUsername.value = loginResp.data.name || authForm.value.email;
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
  localStorage.removeItem("dp_merchant_token");
  localStorage.removeItem("dp_merchant_id");
  localStorage.removeItem("dp_merchant_name");
  localStorage.removeItem("dp_merchant_email");
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

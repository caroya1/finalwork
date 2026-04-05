<template>
  <div class="app-shell">
    <SideNav v-if="isLoggedIn" />
    <main class="app-main" :class="{ 'with-sidebar': isLoggedIn }">
      <RouterView />
    </main>

    <div v-if="authOpen && !isLoggedIn" class="auth-overlay" @click.self="closeAuth">
      <div class="auth-card">
        <div class="auth-header">
          <h2>管理员登录</h2>
          <button class="auth-close" @click="closeAuth">&times;</button>
        </div>
        <div class="form-grid">
          <input v-model="authForm.username" placeholder="管理员用户名" />
          <input v-model="authForm.password" type="password" placeholder="密码" />
          <button class="cta auth-submit" @click="submitAuth">登录</button>
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
import { useRouter, RouterView } from "vue-router";
import SideNav from "./components/SideNav.vue";
import { adminLogin } from "./api/auth";

const router = useRouter();

const isLoggedIn = ref(false);

const checkLoginState = () => {
  const token = localStorage.getItem("dp_token");
  const refreshToken = localStorage.getItem("dp_refresh_token");
  isLoggedIn.value = !!token && !!refreshToken;
};

onMounted(checkLoginState);

const authOpen = ref(false);
const authForm = ref({ username: "", password: "" });
const authMessage = ref("");
const authMessageType = ref("");

const openAuth = () => {
  authMessage.value = "";
  authMessageType.value = "";
  authForm.value = { username: "", password: "" };
  authOpen.value = true;
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
    const loginResp = await adminLogin({
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
    localStorage.setItem("dp_username", loginResp.data.username || authForm.value.username);
    localStorage.setItem("dp_role", "admin");
    localStorage.setItem("dp_city", loginResp.data.city || "");
    isLoggedIn.value = true;
    authOpen.value = false;
    router.push("/");
  } catch {
    authMessage.value = "操作失败，请检查网络后重试";
    authMessageType.value = "error";
  }
};
</script>

<style>
.app-main.with-sidebar {
  margin-left: var(--sidebar-width);
  max-width: none;
}

@media (max-width: 768px) {
  .app-main.with-sidebar {
    margin-left: 64px;
  }
}
</style>

<template>
  <div class="login-page">
    <div class="login-container">
      <div class="login-card">
        <div class="login-header">
          <div class="brand-mark">DP</div>
          <h1>管理后台登录</h1>
          <p>运营后台管理系统</p>
        </div>

        <form class="login-form" @submit.prevent="handleLogin">
          <div class="form-group">
            <label for="username">管理员账号</label>
            <input
              id="username"
              v-model="form.username"
              type="text"
              placeholder="管理员账号"
              :disabled="loading"
            />
          </div>

          <div class="form-group">
            <label for="password">密码</label>
            <input
              id="password"
              v-model="form.password"
              type="password"
              placeholder="请输入密码"
              :disabled="loading"
            />
          </div>

          <div v-if="errorMessage" class="error-message">
            {{ errorMessage }}
          </div>

          <button
            type="submit"
            class="cta login-submit"
            :disabled="loading || !isFormValid"
          >
            <span v-if="loading">登录中...</span>
            <span v-else>登录</span>
          </button>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from "vue";
import { useRouter } from "vue-router";
import { adminLogin } from "../api/auth";

const router = useRouter();

const form = ref({
  username: "",
  password: ""
});

const loading = ref(false);
const errorMessage = ref("");

const isFormValid = computed(() => {
  return form.value.username.trim() && form.value.password.trim();
});

/**
 * Handle admin login with form validation.
 * Stores JWT tokens and admin info to localStorage on success.
 * @async
 * @returns {Promise<void>}
 */
const handleLogin = async () => {
  // 表单验证
  if (!form.value.username.trim()) {
    errorMessage.value = "请输入管理员账号";
    return;
  }
  if (!form.value.password.trim()) {
    errorMessage.value = "请输入密码";
    return;
  }

  errorMessage.value = "";
  loading.value = true;

  try {
    const response = await adminLogin({
      username: form.value.username.trim(),
      password: form.value.password
    });

    if (!response.success) {
      errorMessage.value = response.message || "登录失败";
      return;
    }

    // 存储登录信息到 localStorage
    const data = response.data;
    localStorage.setItem("dp_token", data.accessToken || data.token);
    if (data.refreshToken) {
      localStorage.setItem("dp_refresh_token", data.refreshToken);
    }
    localStorage.setItem("dp_admin_id", String(data.userId || data.id));
    localStorage.setItem("dp_username", data.username || form.value.username);
    if (data.role) {
      localStorage.setItem("dp_role", data.role);
    }

    // 登录成功，跳转到首页
    router.push("/");
  } catch (err) {
    errorMessage.value = "网络错误，请检查网络连接后重试";
    console.error("Login error:", err);
  } finally {
    loading.value = false;
  }
};
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, var(--bg-secondary) 0%, var(--neutral-100) 100%);
  padding: var(--space-4);
}

.login-container {
  width: 100%;
  max-width: 420px;
}

.login-card {
  background: var(--bg-primary);
  border-radius: var(--radius-2xl);
  padding: var(--space-10) var(--space-8);
  box-shadow: var(--shadow-xl);
  border: 1px solid var(--border-light);
}

.login-header {
  text-align: center;
  margin-bottom: var(--space-8);
}

.login-header .brand-mark {
  width: 64px;
  height: 64px;
  border-radius: var(--radius-xl);
  display: grid;
  place-items: center;
  background: var(--brand-gradient);
  color: var(--text-inverse);
  font-weight: var(--font-bold);
  font-size: var(--text-xl);
  letter-spacing: 0.5px;
  box-shadow: var(--shadow-brand);
  margin: 0 auto var(--space-5);
}

.login-header h1 {
  margin: 0 0 var(--space-2);
  font-size: var(--text-2xl);
  font-weight: var(--font-bold);
  color: var(--text-primary);
  letter-spacing: -0.02em;
}

.login-header p {
  margin: 0;
  font-size: var(--text-sm);
  color: var(--text-tertiary);
}

.login-form {
  display: flex;
  flex-direction: column;
  gap: var(--space-5);
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: var(--space-2);
}

.form-group label {
  font-size: var(--text-sm);
  font-weight: var(--font-medium);
  color: var(--text-secondary);
}

.form-group input {
  width: 100%;
  padding: var(--space-3) var(--space-4);
  border: 1px solid var(--border-default);
  border-radius: var(--radius-lg);
  font-size: var(--text-base);
  color: var(--text-primary);
  background: var(--bg-primary);
  transition: all var(--transition-fast);
}

.form-group input:hover {
  border-color: var(--neutral-400);
}

.form-group input:focus {
  outline: none;
  border-color: var(--brand-primary);
  box-shadow: 0 0 0 3px var(--brand-primary-light);
}

.form-group input::placeholder {
  color: var(--text-placeholder);
}

.form-group input:disabled {
  background: var(--bg-secondary);
  cursor: not-allowed;
  opacity: 0.7;
}

.error-message {
  padding: var(--space-3) var(--space-4);
  border-radius: var(--radius-lg);
  background: var(--color-danger-light);
  color: var(--color-danger);
  font-size: var(--text-sm);
  text-align: center;
  animation: shake 300ms ease;
}

@keyframes shake {
  0%, 100% { transform: translateX(0); }
  25% { transform: translateX(-4px); }
  75% { transform: translateX(4px); }
}

.login-submit {
  width: 100%;
  padding: var(--space-4);
  font-size: var(--text-base);
  font-weight: var(--font-semibold);
  margin-top: var(--space-2);
}

.login-submit:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  transform: none;
}

@media (max-width: 480px) {
  .login-card {
    padding: var(--space-8) var(--space-6);
  }

  .login-header .brand-mark {
    width: 56px;
    height: 56px;
    font-size: var(--text-lg);
  }

  .login-header h1 {
    font-size: var(--text-xl);
  }
}
</style>

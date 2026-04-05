<template>
  <div class="login-page">
    <div class="login-container">
      <div class="login-header">
        <div class="brand-mark">DP</div>
        <h1>商户登录</h1>
        <p>登录您的商户账号，管理门店与优惠券</p>
      </div>

      <div class="login-card">
        <div class="form-grid">
          <div class="form-group">
            <label for="username">用户名</label>
            <input
              id="username"
              v-model="form.username"
              type="text"
              placeholder="商户名/手机号"
              @keyup.enter="handleLogin"
            />
          </div>

          <div class="form-group">
            <label for="password">密码</label>
            <input
              id="password"
              v-model="form.password"
              type="password"
              placeholder="请输入密码"
              @keyup.enter="handleLogin"
            />
          </div>

          <div v-if="errorMessage" class="error-message">
            {{ errorMessage }}
          </div>

          <button
            class="cta login-btn"
            :disabled="isLoading"
            @click="handleLogin"
          >
            {{ isLoading ? '登录中...' : '登录' }}
          </button>
        </div>

        <div class="login-footer">
          <p>还没有商户账号？</p>
          <button class="ghost-btn" @click="goToRegister">立即注册</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from "vue";
import { useRouter } from "vue-router";
import { merchantLogin } from "../api/merchant";

const router = useRouter();

const form = ref({
  username: "",
  password: ""
});

const errorMessage = ref("");
const isLoading = ref(false);

const validateForm = () => {
  errorMessage.value = "";
  
  if (!form.value.username.trim()) {
    errorMessage.value = "请输入用户名";
    return false;
  }
  
  if (!form.value.password) {
    errorMessage.value = "请输入密码";
    return false;
  }
  
  return true;
};

/**
 * Handle merchant login with form validation and error handling.
 * Stores JWT tokens and merchant info to localStorage on success.
 * @async
 * @returns {Promise<void>}
 */
const handleLogin = async () => {
  if (!validateForm()) {
    return;
  }

  isLoading.value = true;
  errorMessage.value = "";

  try {
    const response = await merchantLogin({
      email: form.value.username,
      password: form.value.password
    });

    if (!response.success) {
      errorMessage.value = response.message || "登录失败，请检查用户名和密码";
      return;
    }

    // Store tokens and user info in localStorage
    const data = response.data;
    localStorage.setItem("dp_token", data.token || "");
    localStorage.setItem("dp_refresh_token", data.refreshToken || "");
    localStorage.setItem("dp_merchant_id", String(data.merchantId || ""));
    localStorage.setItem("dp_merchant_name", data.name || data.username || "");
    localStorage.setItem("dp_username", data.name || data.username || form.value.username);
    localStorage.setItem("dp_merchant_token", data.token || "");
    localStorage.setItem("dp_merchant_email", data.email || form.value.username);
    localStorage.setItem("dp_user_id", String(data.merchantId || ""));
    localStorage.setItem("dp_role", "merchant");

    // Redirect to home page
    router.push("/");
  } catch (error) {
    console.error("Login error:", error);
    if (error.response) {
      const responseData = error.response.data;
      errorMessage.value = responseData?.message || "登录失败，请稍后重试";
    } else if (error.request) {
      errorMessage.value = "网络连接失败，请检查网络设置";
    } else {
      errorMessage.value = "登录失败，请稍后重试";
    }
  } finally {
    isLoading.value = false;
  }
};

const goToRegister = () => {
  // Navigate to home where the auth modal can be opened
  // or the user can register from there
  router.push("/");
};
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--bg-secondary);
  padding: var(--space-6);
}

.login-container {
  width: 100%;
  max-width: 420px;
}

.login-header {
  text-align: center;
  margin-bottom: var(--space-8);
}

.brand-mark {
  width: 64px;
  height: 64px;
  border-radius: var(--radius-xl);
  display: grid;
  place-items: center;
  background: var(--brand-gradient);
  color: var(--text-inverse);
  font-weight: var(--font-bold);
  font-size: var(--text-2xl);
  letter-spacing: 0.5px;
  box-shadow: var(--shadow-brand);
  margin: 0 auto var(--space-6);
}

.login-header h1 {
  font-size: var(--text-2xl);
  font-weight: var(--font-bold);
  color: var(--text-primary);
  margin: 0 0 var(--space-2);
}

.login-header p {
  font-size: var(--text-sm);
  color: var(--text-tertiary);
  margin: 0;
}

.login-card {
  background: var(--bg-primary);
  border-radius: var(--radius-2xl);
  padding: var(--space-8);
  box-shadow: var(--shadow-xl);
  border: 1px solid var(--border-light);
}

.form-grid {
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

.error-message {
  padding: var(--space-3) var(--space-4);
  border-radius: var(--radius-lg);
  background: var(--color-danger-light);
  color: var(--color-danger);
  font-size: var(--text-sm);
  text-align: center;
}

.login-btn {
  width: 100%;
  padding: var(--space-4);
  font-size: var(--text-base);
  font-weight: var(--font-semibold);
  margin-top: var(--space-2);
}

.login-footer {
  margin-top: var(--space-6);
  padding-top: var(--space-6);
  border-top: 1px solid var(--border-light);
  text-align: center;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--space-3);
}

.login-footer p {
  font-size: var(--text-sm);
  color: var(--text-tertiary);
  margin: 0;
}

/* ========== Responsive ========== */
@media (max-width: 480px) {
  .login-page {
    padding: var(--space-4);
  }

  .login-card {
    padding: var(--space-6);
  }

  .login-header h1 {
    font-size: var(--text-xl);
  }

  .brand-mark {
    width: 56px;
    height: 56px;
    font-size: var(--text-xl);
  }
}
</style>

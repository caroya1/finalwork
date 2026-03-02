<template>
  <section class="hero">
    <div class="hero-card">
      <h2>智能发现与搜索</h2>
      <p>支持场景推荐、位置筛选与新品探索，推荐结果缓存加速。</p>
      <div class="form-grid">
        <input v-model="form.userId" placeholder="用户ID" />
        <input v-model="form.city" placeholder="城市" />
        <input v-model="form.scene" placeholder="场景，例如：带老人聚餐" />
        <button class="cta" @click="loadRecommendations">获取推荐</button>
      </div>
    </div>
    <div class="hero-card">
      <h2>为你推荐</h2>
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
      <h2>智能搜索</h2>
      <p>文字、语音、拍照多模式入口（大模型部分暂未接入）。</p>
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
</template>

<script setup>
import { ref } from "vue";
import { fetchRecommendations } from "../api/recommendation";
import { login } from "../api/auth";
import { register } from "../api/user";

const form = ref({
  userId: "1",
  city: "上海",
  scene: ""
});

const recommendations = ref([]);
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
      registerMessage.value = "注册成功";
    } else {
      registerMessage.value = response.message || "注册失败";
    }
  } catch (error) {
    registerMessage.value = "注册失败";
  }
};

const loadRecommendations = async () => {
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
</script>

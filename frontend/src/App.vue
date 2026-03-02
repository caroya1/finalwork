<template>
  <div class="app-shell">
    <header class="app-header">
      <div class="brand">
        <span class="brand-mark">DP</span>
        <div>
          <h1>类大众点评</h1>
          <p>发现 · 评价 · 到店</p>
        </div>
      </div>
      <div class="header-search">
        <div class="location">上海</div>
        <input
          class="search-input"
          v-model="keyword"
          placeholder="输入商户名、地点或菜品"
        />
        <button class="search-btn" @click="emitSearch">搜索</button>
      </div>
      <nav class="nav">
        <RouterLink to="/">发现</RouterLink>
        <RouterLink to="/merchant">商户服务</RouterLink>
        <RouterLink to="/admin">运营后台</RouterLink>
      </nav>
      <div class="header-actions">
        <button class="ghost-btn">登录/注册</button>
        <button class="cta">去APP看更多内容</button>
      </div>
    </header>
    <main class="app-main">
      <RouterView />
    </main>
  </div>
</template>

<script setup>
import { ref } from "vue";
import { RouterLink, RouterView } from "vue-router";

const keyword = ref("");

const emitSearch = () => {
  window.dispatchEvent(new CustomEvent("dp-search", { detail: keyword.value }));
};
</script>

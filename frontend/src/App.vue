<template>
  <div class="app-shell">
    <header class="app-header">
      <div class="brand">
        <span class="brand-mark">DP</span>
        <div>
          <h1>类大众点评智能平台</h1>
          <p>推荐优先 · 高并发保障 · 多端协同</p>
        </div>
      </div>
      <nav class="nav">
        <RouterLink to="/">用户端</RouterLink>
        <RouterLink to="/merchant">商户端</RouterLink>
        <RouterLink to="/admin">管理员端</RouterLink>
      </nav>
    </header>
    <main class="app-main">
      <RouterView />
    </main>
  </div>
</template>

<script setup>
import { RouterLink, RouterView } from "vue-router";
</script>

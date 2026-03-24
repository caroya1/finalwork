<template>
  <div class="category-page">
    <div class="page-header">
      <button class="ghost-btn back-btn" @click="$router.push('/')">← 返回首页</button>
      <h2>{{ categoryLabel }} · {{ city }}</h2>
      <p>为你找到 {{ shops.length }} 家店铺</p>
    </div>

    <section class="shop-grid" v-if="shops.length > 0">
      <RouterLink
        class="shop-card shop-card-link"
        v-for="shop in shops"
        :key="shop.id"
        :to="`/shops/${shop.id}`"
      >
        <div class="shop-card-banner">
          <span class="shop-card-category">{{ shop.category || '综合' }}</span>
        </div>
        <div class="shop-card-body">
          <div class="shop-header">
            <h3>{{ shop.name }}</h3>
            <span class="tag rating-tag">{{ shop.rating ? shop.rating.toFixed(2) : '0.00' }} 分</span>
          </div>
          <div class="shop-meta">
            <span class="tag" v-for="t in (shop.tags || '').split(',')" :key="t">{{ t }}</span>
          </div>
          <p class="shop-address">📍 {{ shop.address || '地址完善中' }}</p>
        </div>
      </RouterLink>
    </section>

    <div v-else class="empty-state">暂无该分类店铺</div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from "vue";
import { useRoute, RouterLink } from "vue-router";
import { listShops } from "../api/shop";

const route = useRoute();
const shops = ref([]);
const city = ref("");
const categoryLabel = ref("");

const load = async () => {
  city.value = route.query.city || localStorage.getItem("dp_city") || "上海";
  categoryLabel.value = route.query.label || route.query.category || "";
  const category = route.query.category || "";
  const response = await listShops({ city: city.value, category });
  if (response.success) {
    shops.value = response.data || [];
  } else {
    shops.value = [];
  }
};

onMounted(load);
watch(() => route.query, load);
</script>

<style scoped>
.category-page {
  max-width: 1100px;
  margin: 0 auto;
}
.page-header {
  margin-bottom: 28px;
}
.page-header h2 {
  margin: 12px 0 4px;
  font-size: 1.5rem;
}
.page-header p {
  margin: 0;
  color: var(--muted);
  font-size: 0.9rem;
}
.back-btn {
  font-size: 0.85rem;
  padding: 6px 14px;
}
.shop-card-link {
  text-decoration: none;
  color: inherit;
  cursor: pointer;
  transition: transform 0.2s, box-shadow 0.2s;
}
.shop-card-link:hover {
  transform: translateY(-4px);
  box-shadow: 0 14px 40px rgba(31,31,31,0.13);
}
.shop-card-banner {
  height: 90px;
  background: linear-gradient(135deg, #ffd3bf, #ffe9d6);
  border-radius: 18px 18px 0 0;
  display: flex;
  align-items: flex-end;
  padding: 10px 14px;
}
.shop-card-category {
  background: rgba(255,255,255,0.85);
  padding: 3px 10px;
  border-radius: 999px;
  font-size: 0.75rem;
  color: var(--accent);
  font-weight: 600;
}
.shop-card-body {
  padding: 14px 16px 16px;
}
.rating-tag {
  background: var(--accent);
  color: #fff;
}
</style>

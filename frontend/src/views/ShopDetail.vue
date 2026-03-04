<template>
  <div class="shop-detail" v-if="shop">
    <div class="detail-left">
      <div class="shop-banner">
        <div class="banner-bg"></div>
        <div class="banner-overlay">
          <h1>{{ shop.name }}</h1>
          <div class="banner-tags">
            <span class="tag">{{ shop.category || '综合' }}</span>
            <span class="tag">{{ shop.city }}</span>
            <span class="tag rating-tag">⭐ {{ shop.rating ? shop.rating.toFixed(2) : '0.00' }} 分</span>
          </div>
          <p class="banner-address">📍 {{ shop.address || '地址完善中' }}</p>
        </div>
      </div>

      <!-- 我的评分 -->
      <div class="panel rate-panel">
        <h3>我的评分</h3>
        <div class="shop-rate">
          <select class="rate-select" v-model="myRating" @change="submitRating">
            <option value="">选择评分</option>
            <option v-for="s in ratingOptions" :key="s" :value="s">{{ s }} 分</option>
          </select>
          <span v-if="rateMsg" class="rate-msg">{{ rateMsg }}</span>
        </div>
      </div>

      <!-- 菜谱 / 菜品 -->
      <div class="panel">
        <div class="panel-head">
          <h3>🍽 菜谱（{{ dishes.length }}）</h3>
          <button class="cta small-cta" @click="showDishForm = !showDishForm">
            {{ showDishForm ? '收起' : '+ 添加菜品' }}
          </button>
        </div>

        <div v-if="showDishForm" class="dish-form">
          <input v-model="dishForm.name" placeholder="菜品名称（必填）" />
          <input v-model.number="dishForm.price" type="number" step="0.01" placeholder="价格（元）" />
          <input v-model="dishForm.description" placeholder="描述（选填）" />
          <button class="cta" @click="submitDish" :disabled="!dishForm.name">提交</button>
          <span v-if="dishMsg" class="rate-msg">{{ dishMsg }}</span>
        </div>

        <div class="dish-grid" v-if="dishes.length > 0">
          <div class="dish-card" v-for="dish in dishes" :key="dish.id">
            <div class="dish-icon">🍜</div>
            <div class="dish-info">
              <div class="dish-name">{{ dish.name }}</div>
              <div class="dish-desc" v-if="dish.description">{{ dish.description }}</div>
              <div class="dish-bottom">
                <span class="dish-price" v-if="dish.price != null">¥{{ dish.price.toFixed ? dish.price.toFixed(2) : dish.price }}</span>
                <span class="dish-user" v-if="dish.userId">用户 {{ dish.userId }} 推荐</span>
              </div>
            </div>
          </div>
        </div>
        <div v-else class="empty-state">暂无菜品，快来添加第一道菜吧！</div>
      </div>
    </div>

    <div class="detail-right">
      <!-- 店铺信息 -->
      <div class="panel">
        <h3>店铺信息</h3>
        <div class="info-row"><span class="info-label">分类</span><span>{{ shop.category || '-' }}</span></div>
        <div class="info-row"><span class="info-label">标签</span>
          <span class="tag" v-for="t in tagList" :key="t">{{ t }}</span>
        </div>
        <div class="info-row"><span class="info-label">城市</span><span>{{ shop.city || '-' }}</span></div>
        <div class="info-row"><span class="info-label">地址</span><span>{{ shop.address || '-' }}</span></div>
        <div class="info-row"><span class="info-label">评分</span><span>{{ shop.rating ? shop.rating.toFixed(2) : '0.00' }} 分</span></div>
      </div>

      <!-- 相关帖子 -->
      <div class="panel">
        <h3>📝 相关笔记（{{ posts.length }}）</h3>
        <div v-if="posts.length === 0" class="empty-state">暂无相关笔记</div>
        <RouterLink
          class="related-post"
          v-for="p in posts"
          :key="p.id"
          :to="`/posts/${p.id}`"
        >
          <div class="related-post-title">{{ p.title }}</div>
          <div class="related-post-meta">
            <span>{{ p.city }}</span>
            <span>{{ p.likes || 0 }} 赞</span>
          </div>
        </RouterLink>
      </div>
    </div>
  </div>

  <div v-else class="empty-state" style="margin-top:60px;">
    <button class="ghost-btn" @click="$router.push('/')">← 返回首页</button>
    <p>店铺不存在或加载中...</p>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from "vue";
import { useRoute, RouterLink } from "vue-router";
import { getShopDetail, rateShop, addDish } from "../api/shop";

const route = useRoute();
const shop = ref(null);
const dishes = ref([]);
const posts = ref([]);
const myRating = ref("");
const rateMsg = ref("");
const showDishForm = ref(false);
const dishForm = ref({ name: "", price: null, description: "" });
const dishMsg = ref("");
const ratingOptions = [5, 4.5, 4, 3.5, 3, 2.5, 2, 1.5, 1];

const tagList = computed(() => {
  if (!shop.value || !shop.value.tags) return [];
  return shop.value.tags.split(",").map(t => t.trim()).filter(Boolean);
});

const load = async () => {
  const res = await getShopDetail(route.params.id);
  if (res.success) {
    shop.value = res.data.shop;
    dishes.value = res.data.dishes || [];
    posts.value = res.data.posts || [];
  }
};

const submitRating = async () => {
  rateMsg.value = "";
  if (!myRating.value) return;
  const token = localStorage.getItem("dp_token");
  const refreshToken = localStorage.getItem("dp_refresh_token");
  if (!token || !refreshToken) {
    rateMsg.value = "请先登录";
    myRating.value = "";
    return;
  }
  const res = await rateShop(shop.value.id, Number(myRating.value));
  if (res.success) {
    rateMsg.value = "评分成功";
    await load();
  } else {
    rateMsg.value = res.message || "评分失败";
  }
};

const submitDish = async () => {
  dishMsg.value = "";
  const token = localStorage.getItem("dp_token");
  const refreshToken = localStorage.getItem("dp_refresh_token");
  if (!token || !refreshToken) {
    dishMsg.value = "请先登录";
    return;
  }
  if (!dishForm.value.name.trim()) {
    dishMsg.value = "请输入菜品名称";
    return;
  }
  const res = await addDish(shop.value.id, {
    name: dishForm.value.name.trim(),
    price: dishForm.value.price || null,
    description: dishForm.value.description.trim() || null
  });
  if (res.success) {
    dishMsg.value = "添加成功";
    dishForm.value = { name: "", price: null, description: "" };
    showDishForm.value = false;
    await load();
  } else {
    dishMsg.value = res.message || "添加失败";
  }
};

onMounted(load);
</script>

<style scoped>
.shop-detail {
  display: grid;
  grid-template-columns: 1.3fr 0.7fr;
  gap: 28px;
  align-items: start;
  max-width: 1200px;
  margin: 0 auto;
}
.shop-banner {
  position: relative;
  border-radius: 24px;
  overflow: hidden;
  margin-bottom: 20px;
}
.banner-bg {
  height: 220px;
  background: linear-gradient(135deg, #ffc3a0, #ffb07c, #ffd6b8);
}
.banner-overlay {
  position: absolute;
  bottom: 0;
  left: 0; right: 0;
  padding: 24px 28px;
  background: linear-gradient(transparent, rgba(0,0,0,0.55));
  color: #fff;
}
.banner-overlay h1 {
  margin: 0 0 8px;
  font-size: 1.6rem;
}
.banner-tags {
  display: flex;
  gap: 8px;
  margin-bottom: 6px;
}
.banner-tags .tag {
  background: rgba(255,255,255,0.2);
  color: #fff;
}
.rating-tag {
  background: var(--accent) !important;
}
.banner-address {
  margin: 0;
  font-size: 0.85rem;
  opacity: 0.9;
}
.panel {
  background: var(--card);
  border-radius: 20px;
  padding: 20px 22px;
  box-shadow: var(--shadow);
  margin-bottom: 20px;
}
.panel h3 {
  margin: 0 0 14px;
  font-size: 1.1rem;
}
.panel-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 14px;
}
.panel-head h3 {
  margin: 0;
}
.small-cta {
  padding: 6px 14px;
  font-size: 0.8rem;
}
.rate-panel {
  display: flex;
  align-items: center;
  gap: 16px;
}
.rate-panel h3 {
  margin: 0;
  white-space: nowrap;
}
.shop-rate {
  display: flex;
  align-items: center;
  gap: 10px;
}
.rate-select {
  padding: 8px 14px;
  border-radius: 12px;
  border: 1px solid var(--line);
  background: #fff;
  font-size: 0.85rem;
}
.rate-msg {
  font-size: 0.8rem;
  color: var(--accent);
}
/* 菜品表单 */
.dish-form {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-bottom: 16px;
  padding: 14px;
  border: 1px dashed var(--line);
  border-radius: 14px;
  background: #fafafa;
}
.dish-form input {
  flex: 1;
  min-width: 140px;
  padding: 8px 12px;
  border: 1px solid var(--line);
  border-radius: 10px;
  font-size: 0.85rem;
}
.dish-form input:focus {
  outline: none;
  border-color: var(--accent);
}
/* 菜品列表 */
.dish-grid {
  display: grid;
  gap: 12px;
}
.dish-card {
  display: flex;
  align-items: flex-start;
  gap: 14px;
  padding: 14px;
  border: 1px solid var(--line);
  border-radius: 16px;
  transition: box-shadow 0.2s;
}
.dish-card:hover {
  box-shadow: 0 4px 16px rgba(0,0,0,0.06);
}
.dish-icon {
  width: 48px;
  height: 48px;
  border-radius: 14px;
  background: linear-gradient(135deg, #ffe3d1, #fff3e8);
  display: grid;
  place-items: center;
  font-size: 1.4rem;
  flex-shrink: 0;
}
.dish-name {
  font-weight: 600;
  margin-bottom: 4px;
}
.dish-desc {
  font-size: 0.8rem;
  color: var(--muted);
  margin-bottom: 4px;
}
.dish-bottom {
  display: flex;
  gap: 12px;
  align-items: center;
}
.dish-price {
  color: var(--accent);
  font-weight: 600;
  font-size: 0.9rem;
}
.dish-user {
  font-size: 0.75rem;
  color: var(--muted);
}
/* 店铺信息 */
.info-row {
  display: flex;
  gap: 10px;
  padding: 8px 0;
  border-bottom: 1px solid var(--line);
  font-size: 0.88rem;
  flex-wrap: wrap;
}
.info-row:last-child {
  border-bottom: none;
}
.info-label {
  color: var(--muted);
  min-width: 48px;
  flex-shrink: 0;
}
/* 相关帖子 */
.related-post {
  display: block;
  padding: 12px 14px;
  border: 1px solid var(--line);
  border-radius: 14px;
  margin-bottom: 10px;
  transition: border-color 0.2s;
  text-decoration: none;
  color: inherit;
}
.related-post:hover {
  border-color: var(--accent);
}
.related-post-title {
  font-weight: 600;
  margin-bottom: 4px;
  font-size: 0.9rem;
}
.related-post-meta {
  display: flex;
  justify-content: space-between;
  font-size: 0.78rem;
  color: var(--muted);
}
@media (max-width: 900px) {
  .shop-detail {
    grid-template-columns: 1fr;
  }
}
</style>

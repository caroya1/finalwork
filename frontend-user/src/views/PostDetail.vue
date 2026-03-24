<template>
  <section class="post-detail" v-if="post">
    <div class="post-media">
      <div class="post-cover">
        <img v-if="post?.coverUrl" :src="post.coverUrl" alt="post" />
        <div v-else class="post-cover-fallback">
          <span class="fallback-icon">📷</span>
        </div>
      </div>
      <div class="post-actions-bar">
        <button class="icon-btn like-btn" @click="likePost" :disabled="!isLoggedIn" :class="{ liked: liked }">
          <span class="action-icon">{{ liked ? '❤️' : '🤍' }}</span>
          <span class="action-count">{{ likeCount }}</span>
        </button>
        <button class="icon-btn" disabled>
          <span class="action-icon">💬</span>
          <span class="action-count">{{ comments.length }}</span>
        </button>
        <button class="icon-btn" disabled>
          <span class="action-icon">🔖</span>
        </button>
      </div>
    </div>
    
    <aside class="post-aside">
      <div class="post-header">
        <div class="post-author">
          <div class="author-avatar">{{ (authorUsername || 'DP').charAt(0).toUpperCase() }}</div>
          <div class="author-info">
            <div class="author-name">{{ authorUsername || '用户' + post.userId }}</div>
            <div class="author-meta">
              <span class="meta-location">📍 {{ post.city }}</span>
            </div>
          </div>
        </div>
        <div class="author-actions">
          <button
            class="ghost-btn"
            :class="followed ? 'followed-btn' : ''"
            :disabled="!isLoggedIn || isSelf || followLoading"
            @click="toggleFollow"
          >
            <span v-if="followLoading" class="loading-dot"></span>
            {{ followLoading ? '' : (followed ? '已关注' : '+ 关注') }}
          </button>
          <button
            v-if="isSelf"
            class="ghost-btn delete-btn"
            :disabled="deleteLoading"
            @click="deletePostConfirm"
          >
            {{ deleteLoading ? '删除中...' : '删除' }}
          </button>
        </div>
      </div>

      <h2 class="post-title">{{ post.title }}</h2>
      <p class="post-content">{{ post.content }}</p>
      
      <div class="post-tags" v-if="tagList.length > 0">
        <span class="tag" v-for="tag in tagList" :key="tag"># {{ tag }}</span>
      </div>

      <RouterLink v-if="shop && shopLinkId" class="shop-link" :to="`/shops/${shopLinkId}`">
        <div class="shop-thumb">
          <span class="shop-icon">🏪</span>
        </div>
        <div class="shop-info">
          <div class="shop-name">{{ shop.name }}</div>
          <div class="shop-meta">
            <span class="tag">{{ shop.category || "综合" }}</span>
            <span class="tag rating">⭐ {{ shop.rating ? shop.rating.toFixed(1) : "0.0" }}</span>
          </div>
          <div class="shop-address">{{ shop.address || shop.city || "地址完善中" }}</div>
        </div>
        <span class="link-arrow">→</span>
      </RouterLink>

      <section class="post-comments">
        <div class="comments-header">
          <h3>评论</h3>
          <span class="comments-count">{{ comments.length }}</span>
        </div>
        
        <div v-if="actionMessage" class="action-message">{{ actionMessage }}</div>
        
        <div v-if="comments.length === 0" class="empty-comments">
          <span class="empty-icon">💬</span>
          <p>暂无评论，快来抢沙发吧~</p>
        </div>
        
        <div v-for="comment in comments" :key="comment.id" class="comment-item">
          <div class="comment-avatar">{{ (comment.username || '用').charAt(0).toUpperCase() }}</div>
          <div class="comment-body">
            <div class="comment-user">{{ comment.username || '用户' + comment.userId }}</div>
            <div class="comment-content">{{ comment.content }}</div>
            <div class="comment-time">{{ formatTime(comment.createdAt) }}</div>
          </div>
        </div>

        <div class="comment-form">
          <input
            v-model="commentInput"
            class="comment-input"
            placeholder="说点什么吧..."
            :disabled="!isLoggedIn"
          />
          <button class="cta" @click="submitComment" :disabled="!isLoggedIn || !commentInput.trim()">
            发布
          </button>
        </div>
        <div v-if="!isLoggedIn" class="hint">登录后可评论</div>
      </section>
    </aside>

    <div v-else class="hero-card">
      <p>帖子不存在或已被删除</p>
    </div>
  </section>
  
  <div v-else class="loading-state">
    <span class="loading-spinner"></span>
    <p>加载中...</p>
  </div>
</template>

<script setup>
import { computed, ref, onMounted, watch } from "vue";
import { useRoute, RouterLink, useRouter } from "vue-router";
import { getPostDetail, likePost as likePostApi, unlikePost as unlikePostApi, addComment, deletePost } from "../api/post";
import { followUser, unfollowUser } from "../api/user";
import { getShopDetail } from "../api/shop";

const route = useRoute();
const router = useRouter();
const post = ref(null);
const authorUsername = ref("");
const likeCount = ref(0);
const liked = ref(false);
const followed = ref(false);
const followLoading = ref(false);
const deleteLoading = ref(false);
const comments = ref([]);
const shop = ref(null);
const commentInput = ref("");
const actionMessage = ref("");
const isLoggedIn = ref(false);
const tagList = ref([]);
const isSelf = ref(false);
const shopLinkId = computed(() => {
  if (shop.value && shop.value.id != null) {
    return shop.value.id;
  }
  if (post.value && post.value.shopId != null) {
    return post.value.shopId;
  }
  return null;
});

const enrichShopDetail = async (shopId, baseShop) => {
  if (shopId == null) {
    return baseShop;
  }
  try {
    const response = await getShopDetail(shopId);
    if (response.success && response.data && response.data.shop) {
      return { ...baseShop, ...response.data.shop };
    }
  } catch (error) {
    return baseShop;
  }
  return baseShop;
};

const loadPost = async () => {
  const response = await getPostDetail(route.params.id);
  if (response.success) {
    const detail = response.data || {};
    post.value = detail.post;
    authorUsername.value = detail.authorUsername || "";
    likeCount.value = detail.likeCount;
    liked.value = detail.liked;
    followed.value = !!detail.followed;
    comments.value = detail.comments || [];
    shop.value = detail.shop || null;
    if (post.value && post.value.shopId != null) {
      const needsFallback = !shop.value || shop.value.id == null;
      const needsEnrich = shop.value && (!shop.value.name || !shop.value.city || !shop.value.address);
      if (needsFallback) {
        shop.value = await enrichShopDetail(post.value.shopId, { id: post.value.shopId });
      } else if (needsEnrich) {
        shop.value = await enrichShopDetail(post.value.shopId, shop.value);
      }
    }
    tagList.value = post.value.tags ? post.value.tags.split(",") : [];
    const userId = localStorage.getItem("dp_user_id");
    isSelf.value = !!userId && post.value && String(userId) === String(post.value.userId);
  } else {
    actionMessage.value = response.message || "加载失败";
  }
};

const likePost = async () => {
  if (!isLoggedIn.value) {
    return;
  }
  if (liked.value) {
    const response = await unlikePostApi(route.params.id);
    if (response.success) {
      liked.value = false;
      likeCount.value = Math.max(0, likeCount.value - 1);
      actionMessage.value = "已取消点赞";
    } else {
      actionMessage.value = response.message || "取消点赞失败";
    }
    return;
  }
  const response = await likePostApi(route.params.id);
  if (response.success) {
    liked.value = true;
    likeCount.value += 1;
    actionMessage.value = "点赞成功";
  } else {
    actionMessage.value = response.message || "点赞失败";
  }
};

const submitComment = async () => {
  const content = commentInput.value.trim();
  if (!isLoggedIn.value || !content) {
    return;
  }
  const response = await addComment(route.params.id, content);
  if (response.success) {
    commentInput.value = "";
    await loadPost();
    actionMessage.value = "评论已发布";
  } else {
    actionMessage.value = response.message || "评论失败";
  }
};

const toggleFollow = async () => {
  if (!isLoggedIn.value || !post.value || isSelf.value) {
    return;
  }
  followLoading.value = true;
  if (followed.value) {
    const response = await unfollowUser(post.value.userId);
    if (response.success) {
      followed.value = false;
      actionMessage.value = "已取消关注";
    } else {
      actionMessage.value = response.message || "取消关注失败";
    }
  } else {
    const response = await followUser(post.value.userId);
    if (response.success) {
      followed.value = true;
      actionMessage.value = "关注成功";
    } else {
      actionMessage.value = response.message || "关注失败";
    }
  }
  followLoading.value = false;
};

const deletePostConfirm = async () => {
  if (!post.value) return;
  if (!window.confirm("确认删除该帖子？")) {
    return;
  }
  deleteLoading.value = true;
  const response = await deletePost(post.value.id);
  if (response.success) {
    actionMessage.value = "帖子已删除";
    setTimeout(() => {
      router.push("/profile");
    }, 300);
  } else {
    actionMessage.value = response.message || "删除失败";
  }
  deleteLoading.value = false;
};

watch(commentInput, () => {
  actionMessage.value = "";
});

const formatTime = (value) => {
  if (!value) return "";
  return value.replace("T", " ");
};

onMounted(() => {
  const token = localStorage.getItem("dp_token");
  const refreshToken = localStorage.getItem("dp_refresh_token");
  isLoggedIn.value = !!token && !!refreshToken;
  loadPost();
});
</script>

<style scoped>
.post-detail {
  display: grid;
  grid-template-columns: minmax(380px, 1.3fr) minmax(360px, 0.7fr);
  gap: var(--space-8);
  align-items: start;
}

/* 帖子媒体区 */
.post-media {
  background: var(--bg-primary);
  border-radius: var(--radius-3xl);
  padding: var(--space-5);
  box-shadow: var(--shadow-lg);
  border: 1px solid var(--border-light);
  position: sticky;
  top: calc(var(--header-height) + var(--space-6));
}

.post-cover {
  position: relative;
  border-radius: var(--radius-2xl);
  overflow: hidden;
  background: var(--bg-secondary);
}

.post-cover img {
  width: 100%;
  display: block;
  border-radius: var(--radius-2xl);
}

.post-cover-fallback {
  height: 420px;
  background: linear-gradient(135deg, #FFE5D0 0%, #FFCDB2 50%, #FFB899 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: var(--radius-2xl);
}

.fallback-icon {
  font-size: 4rem;
  opacity: 0.4;
}

/* 操作栏 */
.post-actions-bar {
  display: flex;
  gap: var(--space-3);
  margin-top: var(--space-4);
}

.icon-btn {
  display: inline-flex;
  align-items: center;
  gap: var(--space-2);
  padding: var(--space-2) var(--space-4);
  border: 1px solid var(--border-default);
  background: var(--bg-primary);
  border-radius: var(--radius-full);
  font-size: var(--text-sm);
  color: var(--text-secondary);
  transition: all var(--transition-fast);
  cursor: pointer;
}

.icon-btn:hover:not(:disabled) {
  border-color: var(--brand-primary);
  color: var(--brand-primary);
  background: var(--brand-primary-lighter);
}

.icon-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.icon-btn.liked {
  border-color: var(--color-danger);
  color: var(--color-danger);
  background: var(--color-danger-light);
}

.action-icon {
  font-size: var(--text-lg);
  transition: transform var(--transition-bounce);
}

.icon-btn:active .action-icon {
  transform: scale(1.3);
}

.action-count {
  font-weight: var(--font-semibold);
}

/* 侧边栏 */
.post-aside {
  background: var(--bg-primary);
  border-radius: var(--radius-3xl);
  padding: var(--space-6);
  box-shadow: var(--shadow-lg);
  border: 1px solid var(--border-light);
}

/* 作者信息 */
.post-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--space-3);
  margin-bottom: var(--space-5);
  padding-bottom: var(--space-4);
  border-bottom: 1px solid var(--border-light);
}

.post-author {
  display: flex;
  align-items: center;
  gap: var(--space-3);
}

.author-avatar {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  background: var(--brand-gradient);
  color: var(--text-inverse);
  display: grid;
  place-items: center;
  font-weight: var(--font-bold);
  font-size: var(--text-lg);
  box-shadow: var(--shadow-brand);
}

.author-info {
  min-width: 0;
}

.author-name {
  font-weight: var(--font-semibold);
  font-size: var(--text-base);
  color: var(--text-primary);
}

.author-meta {
  margin-top: var(--space-1);
  font-size: var(--text-xs);
  color: var(--text-tertiary);
}

.author-actions {
  display: flex;
  gap: var(--space-2);
}

.followed-btn {
  border-color: var(--brand-primary);
  color: var(--brand-primary);
  background: var(--brand-primary-lighter);
}

.delete-btn {
  border-color: var(--color-danger);
  color: var(--color-danger);
}

.delete-btn:hover {
  background: var(--color-danger-light);
}

.loading-dot {
  width: 6px;
  height: 6px;
  background: var(--brand-primary);
  border-radius: 50%;
  animation: pulse 1s ease-in-out infinite;
}

@keyframes pulse {
  0%, 100% { opacity: 1; transform: scale(1); }
  50% { opacity: 0.5; transform: scale(0.8); }
}

/* 帖子内容 */
.post-title {
  margin: 0 0 var(--space-3);
  font-size: var(--text-2xl);
  font-weight: var(--font-bold);
  color: var(--text-primary);
  line-height: var(--leading-tight);
  letter-spacing: -0.02em;
}

.post-content {
  margin: 0 0 var(--space-5);
  font-size: var(--text-base);
  line-height: var(--leading-relaxed);
  color: var(--text-secondary);
}

.post-tags {
  display: flex;
  flex-wrap: wrap;
  gap: var(--space-2);
  margin-bottom: var(--space-5);
}

.tag {
  display: inline-flex;
  align-items: center;
  padding: var(--space-1) var(--space-3);
  border-radius: var(--radius-full);
  background: var(--bg-secondary);
  font-size: var(--text-xs);
  font-weight: var(--font-medium);
  color: var(--text-secondary);
  transition: all var(--transition-fast);
}

.tag:hover {
  background: var(--brand-primary-light);
  color: var(--brand-primary);
}

.tag.rating {
  background: var(--color-warning-light);
  color: #B45309;
}

/* 店铺链接 */
.shop-link {
  display: grid;
  grid-template-columns: 56px 1fr auto;
  gap: var(--space-3);
  align-items: center;
  padding: var(--space-3);
  border: 1px solid var(--border-light);
  border-radius: var(--radius-xl);
  margin-bottom: var(--space-6);
  transition: all var(--transition-base);
  text-decoration: none;
  color: inherit;
}

.shop-link:hover {
  border-color: var(--brand-primary);
  box-shadow: var(--shadow-brand);
  transform: translateY(-2px);
}

.shop-thumb {
  width: 56px;
  height: 56px;
  border-radius: var(--radius-lg);
  background: linear-gradient(135deg, #FFE5D0 0%, #FFCDB2 100%);
  display: grid;
  place-items: center;
}

.shop-icon {
  font-size: var(--text-xl);
}

.shop-info {
  min-width: 0;
}

.shop-info .shop-name {
  font-weight: var(--font-semibold);
  color: var(--text-primary);
  margin-bottom: var(--space-1);
}

.shop-info .shop-meta {
  display: flex;
  gap: var(--space-2);
  margin-bottom: var(--space-1);
}

.shop-info .shop-address {
  font-size: var(--text-xs);
  color: var(--text-tertiary);
}

.link-arrow {
  color: var(--text-tertiary);
  font-size: var(--text-lg);
  transition: transform var(--transition-fast);
}

.shop-link:hover .link-arrow {
  transform: translateX(4px);
  color: var(--brand-primary);
}

/* 评论区 */
.post-comments {
  border-top: 1px solid var(--border-light);
  padding-top: var(--space-5);
}

.comments-header {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  margin-bottom: var(--space-4);
}

.comments-header h3 {
  margin: 0;
  font-size: var(--text-base);
  font-weight: var(--font-semibold);
  color: var(--text-primary);
}

.comments-count {
  background: var(--brand-primary-light);
  color: var(--brand-primary);
  padding: var(--space-1) var(--space-2);
  border-radius: var(--radius-full);
  font-size: var(--text-xs);
  font-weight: var(--font-semibold);
}

.action-message {
  font-size: var(--text-sm);
  font-weight: var(--font-medium);
  color: var(--brand-primary);
  margin-bottom: var(--space-3);
  padding: var(--space-3);
  background: var(--brand-primary-lighter);
  border-radius: var(--radius-lg);
  text-align: center;
}

.empty-comments {
  text-align: center;
  padding: var(--space-8);
  color: var(--text-tertiary);
}

.empty-icon {
  font-size: var(--text-4xl);
  display: block;
  margin-bottom: var(--space-2);
  opacity: 0.5;
}

.empty-comments p {
  margin: 0;
  font-size: var(--text-sm);
}

.comment-item {
  display: grid;
  grid-template-columns: 36px 1fr;
  gap: var(--space-3);
  padding: var(--space-3) 0;
  border-bottom: 1px solid var(--border-light);
}

.comment-item:last-of-type {
  border-bottom: none;
}

.comment-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: linear-gradient(135deg, #E5E7EB 0%, #D1D5DB 100%);
  display: grid;
  place-items: center;
  font-weight: var(--font-semibold);
  font-size: var(--text-sm);
  color: var(--text-secondary);
}

.comment-body {
  min-width: 0;
}

.comment-user {
  font-size: var(--text-sm);
  font-weight: var(--font-medium);
  color: var(--text-secondary);
  margin-bottom: var(--space-1);
}

.comment-content {
  font-size: var(--text-sm);
  color: var(--text-primary);
  line-height: var(--leading-relaxed);
  margin-bottom: var(--space-1);
  word-break: break-word;
}

.comment-time {
  font-size: var(--text-xs);
  color: var(--text-placeholder);
}

.comment-form {
  display: flex;
  gap: var(--space-2);
  margin-top: var(--space-4);
  padding-top: var(--space-4);
  border-top: 1px solid var(--border-light);
}

.comment-input {
  flex: 1;
  padding: var(--space-3) var(--space-4);
  border: 1px solid var(--border-default);
  border-radius: var(--radius-full);
  font-size: var(--text-sm);
  transition: all var(--transition-fast);
}

.comment-input:hover {
  border-color: var(--neutral-400);
}

.comment-input:focus {
  outline: none;
  border-color: var(--brand-primary);
  box-shadow: 0 0 0 4px var(--brand-primary-light);
}

.comment-input:disabled {
  background: var(--bg-secondary);
  cursor: not-allowed;
}

.hint {
  font-size: var(--text-xs);
  color: var(--text-tertiary);
  margin-top: var(--space-2);
  text-align: center;
}

/* 加载状态 */
.loading-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: var(--space-16);
  color: var(--text-tertiary);
}

.loading-spinner {
  width: 32px;
  height: 32px;
  border: 3px solid var(--border-light);
  border-top-color: var(--brand-primary);
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
  margin-bottom: var(--space-3);
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.loading-state p {
  margin: 0;
  font-size: var(--text-sm);
}

/* 响应式 */
@media (max-width: 1024px) {
  .post-detail {
    grid-template-columns: 1fr;
  }
  
  .post-media {
    position: static;
  }
}

@media (max-width: 768px) {
  .post-detail {
    gap: var(--space-4);
  }
  
  .post-media,
  .post-aside {
    padding: var(--space-4);
    border-radius: var(--radius-2xl);
  }
  
  .post-title {
    font-size: var(--text-xl);
  }
}
</style>

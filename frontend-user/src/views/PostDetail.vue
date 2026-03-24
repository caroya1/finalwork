<template>
  <section class="post-detail">
    <div class="post-media">
      <div class="post-cover">
        <img v-if="post?.coverUrl" :src="post.coverUrl" alt="post" />
        <div v-else class="post-cover-fallback"></div>
        <div class="post-page">1/9</div>
      </div>
      <div class="post-actions-bar">
        <button class="icon-btn" @click="likePost" :disabled="!isLoggedIn">
          <span :class="['heart', liked ? 'filled' : '']">{{ liked ? '❤' : '♡' }}</span>
          <span>{{ likeCount }}</span>
        </button>
        <button class="icon-btn" disabled>
          <span>💬</span>
          <span>{{ comments.length }}</span>
        </button>
        <button class="icon-btn" disabled>
          <span>🔖</span>
        </button>
      </div>
    </div>
    <aside class="post-aside" v-if="post">
      <div class="post-header">
      <div class="post-author">
        <div class="author-avatar">DP</div>
        <div>
          <div class="author-name">{{ authorUsername || '用户' + post.userId }}</div>
          <div class="author-meta">{{ post.city }}</div>
        </div>
      </div>
        <div class="author-actions">
          <button
            class="ghost-btn"
            :class="followed ? 'followed-btn' : ''"
            :disabled="!isLoggedIn || isSelf || followLoading"
            @click="toggleFollow"
          >
            {{ followLoading ? '处理中...' : (followed ? '已关注' : '关注') }}
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
      <div class="post-tags">
        <span class="tag" v-for="tag in tagList" :key="tag"># {{ tag }}</span>
      </div>

      <RouterLink v-if="shop && shopLinkId" class="shop-link" :to="`/shops/${shopLinkId}`">
        <div class="shop-thumb"></div>
        <div class="shop-info">
          <div class="shop-name">{{ shop.name }}</div>
          <div class="shop-meta">
            <span class="tag">{{ shop.category || "综合" }}</span>
            <span class="tag">{{ shop.rating ? shop.rating.toFixed(2) : "0.00" }}分</span>
            <span class="tag">{{ shop.city }}</span>
          </div>
          <div class="shop-address">{{ shop.address || "地址完善中" }}</div>
        </div>
      </RouterLink>

      <section class="post-comments">
        <h3>评论 ({{ comments.length }})</h3>
        <div v-if="actionMessage" class="action-message">{{ actionMessage }}</div>
        <div v-if="comments.length === 0" class="empty-state">暂无评论</div>
        <div v-for="comment in comments" :key="comment.id" class="comment-item">
          <div class="comment-avatar">🙂</div>
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
          <button class="cta" @click="submitComment" :disabled="!isLoggedIn || !commentInput">
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

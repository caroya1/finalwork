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
            <div class="author-name">用户 {{ post.userId }}</div>
            <div class="author-meta">{{ post.city }}</div>
          </div>
        </div>
        <button
          class="ghost-btn"
          :class="followed ? 'followed-btn' : ''"
          :disabled="!isLoggedIn || isSelf || followLoading"
          @click="toggleFollow"
        >
          {{ followLoading ? '处理中...' : (followed ? '已关注' : '关注') }}
        </button>
      </div>

      <h2 class="post-title">{{ post.title }}</h2>
      <p class="post-content">{{ post.content }}</p>
      <div class="post-tags">
        <span class="tag" v-for="tag in tagList" :key="tag"># {{ tag }}</span>
      </div>

      <RouterLink v-if="shop" class="shop-link" :to="`/shops/${shop.id}`">
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
            <div class="comment-user">用户 {{ comment.userId }}</div>
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
import { ref, onMounted, watch } from "vue";
import { useRoute, RouterLink } from "vue-router";
import { getPostDetail, likePost as likePostApi, unlikePost as unlikePostApi, addComment } from "../api/post";
import { followUser, unfollowUser } from "../api/user";

const route = useRoute();
const post = ref(null);
const likeCount = ref(0);
const liked = ref(false);
const followed = ref(false);
const followLoading = ref(false);
const comments = ref([]);
const shop = ref(null);
const commentInput = ref("");
const actionMessage = ref("");
const isLoggedIn = ref(false);
const tagList = ref([]);
const isSelf = ref(false);

const loadPost = async () => {
  const response = await getPostDetail(route.params.id);
  if (response.success) {
    post.value = response.data.post;
    likeCount.value = response.data.likeCount;
    liked.value = response.data.liked;
    followed.value = !!response.data.followed;
    comments.value = response.data.comments || [];
    shop.value = response.data.shop || null;
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

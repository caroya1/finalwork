<template>
  <section class="post-create">
    <div class="post-create-card">
      <div class="post-create-header">
        <div>
          <h2>发布新帖子</h2>
          <p>分享你的真实体验，记录城市里的美好瞬间。</p>
        </div>
        <RouterLink class="ghost-btn" to="/">返回首页</RouterLink>
      </div>

      <div class="form-grid">
        <input v-model="postForm.title" placeholder="标题" />
        <input v-model="postForm.shopId" placeholder="关联商铺 ID（可选）" />
        <input v-model="postForm.tags" placeholder="标签，逗号分隔（可选）" />
        <input v-model="postForm.coverUrl" placeholder="封面图片 URL（可选）" />
        <textarea v-model="postForm.content" class="post-textarea" placeholder="说说你的体验..." />
        <button class="cta" @click="submitPost">发布帖子</button>
        <span v-if="postMessage">{{ postMessage }}</span>
      </div>
    </div>
  </section>
</template>

<script setup>
import { ref, onMounted } from "vue";
import { useRouter, RouterLink } from "vue-router";
import { createPost } from "../api/post";

const router = useRouter();
const postMessage = ref("");
const postForm = ref({
  title: "",
  shopId: "",
  tags: "",
  coverUrl: "",
  content: ""
});

const submitPost = async () => {
  postMessage.value = "";
  if (!postForm.value.title.trim() || !postForm.value.content.trim()) {
    postMessage.value = "请填写标题和内容";
    return;
  }
  if (postForm.value.shopId && Number.isNaN(Number(postForm.value.shopId))) {
    postMessage.value = "商铺 ID 需为数字";
    return;
  }
  const payload = {
    title: postForm.value.title.trim(),
    content: postForm.value.content.trim(),
    shopId: postForm.value.shopId ? Number(postForm.value.shopId) : null,
    tags: postForm.value.tags || null,
    coverUrl: postForm.value.coverUrl || null,
    city: localStorage.getItem("dp_city") || "上海"
  };
  const response = await createPost(payload);
  if (response.success) {
    postMessage.value = "发布成功";
    router.push("/profile");
  } else {
    postMessage.value = response.message || "发布失败";
  }
};

onMounted(() => {
  const token = localStorage.getItem("dp_token");
  const refreshToken = localStorage.getItem("dp_refresh_token");
  if (!token || !refreshToken) {
    router.push("/");
    window.dispatchEvent(new CustomEvent("dp-auth-required"));
  }
});
</script>

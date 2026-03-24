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

        <!-- 封面图片上传 -->
        <div class="form-group">
          <label class="form-label">封面图片</label>
          <ImageUpload
            v-model="postForm.coverUrl"
            dir="posts"
            placeholder="点击或拖拽上传封面图片"
            @success="onImageUploadSuccess"
            @error="onImageUploadError"
          />
        </div>

        <textarea v-model="postForm.content" class="post-textarea" placeholder="说说你的体验..." />
        <button class="cta" @click="submitPost">发布帖子</button>
        <span v-if="postMessage" :class="['post-message', postMessageType]">{{ postMessage }}</span>
      </div>
    </div>
  </section>
</template>

<script setup>
import { ref, onMounted } from "vue";
import { useRouter, RouterLink } from "vue-router";
import { createPost } from "../api/post";
import ImageUpload from "../components/ImageUpload.vue";

const router = useRouter();
const postMessage = ref("");
const postMessageType = ref("");
const postForm = ref({
  title: "",
  shopId: "",
  tags: "",
  coverUrl: "",
  content: ""
});

const onImageUploadSuccess = (data) => {
  postMessage.value = `图片上传成功: ${data.filename}`;
  postMessageType.value = "success";
  setTimeout(() => {
    postMessage.value = "";
  }, 3000);
};

const onImageUploadError = (error) => {
  postMessage.value = error;
  postMessageType.value = "error";
};

const submitPost = async () => {
  postMessage.value = "";
  postMessageType.value = "";

  if (!postForm.value.title.trim() || !postForm.value.content.trim()) {
    postMessage.value = "请填写标题和内容";
    postMessageType.value = "error";
    return;
  }
  if (postForm.value.shopId && Number.isNaN(Number(postForm.value.shopId))) {
    postMessage.value = "商铺 ID 需为数字";
    postMessageType.value = "error";
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
    postMessageType.value = "success";
    router.push("/profile");
  } else {
    postMessage.value = response.message || "发布失败";
    postMessageType.value = "error";
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

<style scoped>
.form-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.form-label {
  font-size: 14px;
  color: #333;
  font-weight: 500;
}

.post-message {
  font-size: 14px;
  padding: 8px 12px;
  border-radius: 4px;
}

.post-message.success {
  color: #52c41a;
  background: #f6ffed;
  border: 1px solid #b7eb8f;
}

.post-message.error {
  color: #ff4d4f;
  background: #fff2f0;
  border: 1px solid #ffccc7;
}
</style>

<template>
  <div class="image-upload">
    <!-- 图片预览区域 -->
    <div v-if="modelValue" class="image-preview">
      <img :src="modelValue" alt="预览" />
      <button type="button" class="remove-btn" @click="removeImage" title="删除图片">
        ×
      </button>
    </div>

    <!-- 上传按钮 -->
    <div v-else class="upload-area" @click="triggerUpload" @drop.prevent="handleDrop" @dragover.prevent>
      <input
        ref="fileInput"
        type="file"
        accept="image/jpeg,image/png,image/gif,image/webp"
        style="display: none"
        @change="handleFileChange"
      />
      <div class="upload-placeholder">
        <span class="upload-icon">📷</span>
        <span class="upload-text">{{ placeholder }}</span>
        <span class="upload-hint">支持 JPG、PNG、GIF、WebP，最大 {{ maxSize }}MB</span>
      </div>
    </div>

    <!-- 上传进度 -->
    <div v-if="uploading" class="upload-progress">
      <div class="progress-bar">
        <div class="progress-fill" :style="{ width: progress + '%' }"></div>
      </div>
      <span class="progress-text">上传中...</span>
    </div>

    <!-- 错误提示 -->
    <div v-if="error" class="upload-error">{{ error }}</div>
  </div>
</template>

<script setup>
import { ref } from "vue";
import { uploadImage } from "../api/oss";

const props = defineProps({
  modelValue: {
    type: String,
    default: ""
  },
  dir: {
    type: String,
    default: "common"
  },
  placeholder: {
    type: String,
    default: "点击或拖拽上传图片"
  },
  maxSize: {
    type: Number,
    default: 10 // MB
  }
});

const emit = defineEmits(["update:modelValue", "success", "error"]);

const fileInput = ref(null);
const uploading = ref(false);
const progress = ref(0);
const error = ref("");

const triggerUpload = () => {
  fileInput.value?.click();
};

const handleFileChange = (event) => {
  const file = event.target.files?.[0];
  if (file) {
    uploadFile(file);
  }
  // 清空input，允许重复选择同一文件
  event.target.value = "";
};

const handleDrop = (event) => {
  const file = event.dataTransfer.files?.[0];
  if (file && file.type.startsWith("image/")) {
    uploadFile(file);
  }
};

const validateFile = (file) => {
  // 检查文件类型
  const allowedTypes = ["image/jpeg", "image/png", "image/gif", "image/webp"];
  if (!allowedTypes.includes(file.type)) {
    return "不支持的文件类型，请上传 JPG、PNG、GIF 或 WebP 格式的图片";
  }

  // 检查文件大小
  const maxBytes = props.maxSize * 1024 * 1024;
  if (file.size > maxBytes) {
    return `文件大小超过限制，最大支持 ${props.maxSize}MB`;
  }

  return null;
};

const uploadFile = async (file) => {
  // 验证文件
  const validationError = validateFile(file);
  if (validationError) {
    error.value = validationError;
    emit("error", validationError);
    return;
  }

  error.value = "";
  uploading.value = true;
  progress.value = 0;

  // 模拟进度
  const progressTimer = setInterval(() => {
    if (progress.value < 90) {
      progress.value += Math.random() * 10;
    }
  }, 200);

  try {
    const result = await uploadImage(file, props.dir);
    clearInterval(progressTimer);

    if (result.success) {
      progress.value = 100;
      emit("update:modelValue", result.data.url);
      emit("success", result.data);
    } else {
      error.value = result.message || "上传失败";
      emit("error", error.value);
    }
  } catch (err) {
    clearInterval(progressTimer);
    error.value = err.response?.data?.message || "上传失败，请稍后重试";
    emit("error", error.value);
  } finally {
    setTimeout(() => {
      uploading.value = false;
    }, 500);
  }
};

const removeImage = () => {
  emit("update:modelValue", "");
  error.value = "";
};
</script>

<style scoped>
.image-upload {
  width: 100%;
}

.upload-area {
  border: 2px dashed #d9d9d9;
  border-radius: 8px;
  padding: 32px;
  text-align: center;
  cursor: pointer;
  transition: border-color 0.3s;
  background: #fafafa;
}

.upload-area:hover {
  border-color: #ff6633;
  background: #fff5f0;
}

.upload-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
}

.upload-icon {
  font-size: 32px;
}

.upload-text {
  font-size: 14px;
  color: #666;
}

.upload-hint {
  font-size: 12px;
  color: #999;
}

.image-preview {
  position: relative;
  border-radius: 8px;
  overflow: hidden;
  border: 1px solid #e8e8e8;
}

.image-preview img {
  width: 100%;
  height: 200px;
  object-fit: cover;
  display: block;
}

.remove-btn {
  position: absolute;
  top: 8px;
  right: 8px;
  width: 24px;
  height: 24px;
  border-radius: 50%;
  background: rgba(0, 0, 0, 0.5);
  color: white;
  border: none;
  cursor: pointer;
  font-size: 16px;
  line-height: 1;
  display: flex;
  align-items: center;
  justify-content: center;
}

.remove-btn:hover {
  background: rgba(0, 0, 0, 0.7);
}

.upload-progress {
  margin-top: 12px;
}

.progress-bar {
  height: 4px;
  background: #f0f0f0;
  border-radius: 2px;
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  background: #ff6633;
  transition: width 0.3s;
}

.progress-text {
  font-size: 12px;
  color: #666;
  margin-top: 4px;
  display: block;
}

.upload-error {
  margin-top: 8px;
  color: #ff4d4f;
  font-size: 12px;
}
</style>

<!--
  错误边界组件
  
  使用示例:
  <ErrorBoundary>
    <MyComponent />
  </ErrorBoundary>
  
  或指定fallback内容:
  <ErrorBoundary>
    <template #fallback="{ error, reset }">
      <div class="custom-error">
        <p>出错了: {{ error.message }}</p>
        <button @click="reset">重试</button>
      </div>
    </template>
    <MyComponent />
  </ErrorBoundary>
-->

<template>
  <slot v-if="!hasError"></slot>
  
  <slot v-else name="fallback" :error="error" :reset="reset">
    <div class="error-boundary">
      <div class="error-icon">⚠️</div>
      <h3 class="error-title">页面出错了</h3>
      <p class="error-message">{{ displayError }}</p>
      <div class="error-actions">
        <button class="btn-retry" @click="reset">
          重新加载
        </button>
        <button class="btn-back" @click="goBack">
          返回上页
        </button>
      </div>
    </div>
  </slot>
</template>

<script setup>
import { ref, computed, onErrorCaptured } from 'vue';
import { useRouter } from 'vue-router';

const props = defineProps({
  // 是否停止错误传播
  stopPropagation: {
    type: Boolean,
    default: true
  },
  // 是否在控制台输出错误
  logError: {
    type: Boolean,
    default: true
  }
});

const emit = defineEmits(['error']);

const router = useRouter();
const hasError = ref(false);
const error = ref(null);

const displayError = computed(() => {
  if (!error.value) return '发生未知错误';
  
  // 生产环境隐藏详细错误信息
  if (import.meta.env.PROD) {
    return '系统繁忙，请稍后重试';
  }
  
  return error.value.message || '发生未知错误';
});

onErrorCaptured((err, instance, info) => {
  hasError.value = true;
  error.value = err;
  
  if (props.logError) {
    console.error('ErrorBoundary捕获到错误:', {
      error: err,
      component: instance,
      info: info
    });
  }
  
  emit('error', {
    error: err,
    component: instance,
    info: info
  });
  
  return !props.stopPropagation;
});

const reset = () => {
  hasError.value = false;
  error.value = null;
};

const goBack = () => {
  router.back();
};
</script>

<style scoped>
.error-boundary {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 48px 24px;
  text-align: center;
  min-height: 300px;
}

.error-icon {
  font-size: 64px;
  margin-bottom: 16px;
}

.error-title {
  font-size: 20px;
  color: #333;
  margin-bottom: 12px;
  font-weight: 600;
}

.error-message {
  font-size: 14px;
  color: #666;
  margin-bottom: 24px;
  max-width: 400px;
  word-break: break-word;
}

.error-actions {
  display: flex;
  gap: 12px;
}

.btn-retry,
.btn-back {
  padding: 10px 24px;
  border-radius: 20px;
  border: none;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.btn-retry {
  background: linear-gradient(135deg, #ff6b6b, #ff8e8e);
  color: white;
}

.btn-retry:hover {
  background: linear-gradient(135deg, #ff5252, #ff7979);
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(255, 107, 107, 0.3);
}

.btn-back {
  background: #f5f5f5;
  color: #666;
}

.btn-back:hover {
  background: #e8e8e8;
}
</style>

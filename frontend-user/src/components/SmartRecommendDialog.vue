<template>
  <!-- Modal Overlay -->
  <div v-if="visible" class="smart-dialog-overlay" @click.self="closeDialog">
    <div class="smart-dialog">
      <!-- Header -->
      <div class="smart-dialog-header">
        <div class="header-title">
          <span class="ai-icon">🤖</span>
          <h2>AI生活助手</h2>
        </div>
        <button class="close-btn" @click="closeDialog" aria-label="关闭">
          <span>×</span>
        </button>
      </div>

      <!-- Chat Area -->
      <div class="smart-dialog-body" ref="chatBodyRef">
        <!-- Welcome Message -->
        <div v-if="messages.length === 0" class="welcome-section">
          <div class="welcome-icon">🌟</div>
          <h3>我是您的本地生活顾问</h3>
          <p>告诉我您的需求，我为您推荐美食、酒店、电影、景点等</p>
          <div class="scene-categories">
            <div 
              v-for="scene in sceneCategories" 
              :key="scene.type"
              class="scene-card"
              @click="usePrompt(scene.prompt)"
            >
              <span class="scene-icon">{{ scene.icon }}</span>
              <span class="scene-label">{{ scene.label }}</span>
            </div>
          </div>
        </div>

        <!-- Chat Messages -->
        <div v-else class="chat-messages">
          <div 
            v-for="(message, index) in messages" 
            :key="index"
            :class="['message', message.type]"
            :style="{ animationDelay: `${index * 100}ms` }"
          >
            <!-- User Message -->
            <template v-if="message.type === 'user'">
              <div class="message-avatar user-avatar">
                <span>👤</span>
              </div>
              <div class="message-content user-content">
                <p>{{ message.content }}</p>
              </div>
            </template>

            <!-- AI Thinking Message -->
            <template v-else-if="message.type === 'ai-thinking'">
              <div class="message-avatar ai-avatar">
                <span>🤖</span>
              </div>
              <div class="message-content ai-content">
                <div class="thinking-box">
                  <div class="thinking-header">
                    <span class="sparkle">✨</span>
                    <span>AI正在分析您的需求</span>
                  </div>
                  <div class="thinking-content">
                    <p class="scene-analysis" v-if="message.sceneAnalysis">
                      <strong>场景识别：</strong>{{ message.sceneAnalysis }}
                    </p>
                    <p class="preference-analysis" v-if="message.preferenceAnalysis">
                      <strong>偏好分析：</strong>{{ message.preferenceAnalysis }}
                    </p>
                  </div>
                </div>
              </div>
            </template>

            <!-- AI Suggestion Message -->
            <template v-else-if="message.type === 'ai-suggestion'">
              <div class="message-avatar ai-avatar">
                <span>💡</span>
              </div>
              <div class="message-content ai-content">
                <div class="suggestion-box">
                  <div class="suggestion-header">
                    <span>🎯</span>
                    <span>为您推荐以下餐厅</span>
                  </div>
                  <p class="suggestion-intro">{{ message.content }}</p>
                </div>
              </div>
            </template>

            <!-- Shop Cards -->
            <template v-else-if="message.type === 'shops'">
              <div class="message-avatar ai-avatar">
                <span>🏪</span>
              </div>
              <div class="message-content shops-content">
                <div class="shop-cards-container">
                  <div
                    v-for="(shop, idx) in message.shops.slice(0, 3)"
                    :key="shop.id"
                    class="shop-card-enhanced"
                    @click="navigateToShop(shop.id)"
                  >
                    <!-- Card Header with Rank -->
                    <div class="card-header">
                      <span class="rank-badge" :class="`rank-${idx + 1}`">{{ idx + 1 }}</span>
                      <div class="shop-image-wrapper">
                        <img 
                          :src="shop.images ? shop.images.split(',')[0] : '/default-shop.jpg'" 
                          :alt="shop.name"
                          class="shop-image"
                        />
                        <span class="category-tag">{{ shop.category || '美食' }}</span>
                      </div>
                    </div>

                    <!-- Card Body -->
                    <div class="card-body">
                      <h4 class="shop-name">{{ shop.name }}</h4>
                      <div class="shop-meta">
                        <span class="rating">
                          <span class="star">⭐</span>
                          {{ shop.rating ? shop.rating.toFixed(1) : '0.0' }}
                        </span>
                        <span class="price" v-if="shop.avgPrice">¥{{ shop.avgPrice }}/人</span>
                      </div>
                      <p class="shop-address">
                        <span class="location-icon">📍</span>
                        {{ shop.address || shop.city }}
                      </p>
                      <div class="shop-tags" v-if="shop.tags">
                        <span 
                          v-for="tag in formatTags(shop.tags).slice(0, 2)" 
                          :key="tag"
                          class="tag"
                        >
                          {{ tag }}
                        </span>
                      </div>
                      <!-- Shop Highlights -->
                      <div class="shop-highlights" v-if="shop.highlights">
                        <div class="highlight-item" v-for="(highlight, hIdx) in shop.highlights.slice(0, 2)" :key="hIdx">
                          <span class="highlight-icon">✓</span>
                          <span>{{ highlight }}</span>
                        </div>
                      </div>
                    </div>

                    <!-- Card Footer -->
                    <div class="card-footer">
                      <span class="recommend-reason" v-if="shop.recommendReason">
                        💡 {{ shop.recommendReason }}
                      </span>
                    </div>
                  </div>
                </div>
              </div>
            </template>

            <!-- Comparison Message -->
            <template v-else-if="message.type === 'comparison'">
              <div class="message-avatar ai-avatar">
                <span>📊</span>
              </div>
              <div class="message-content ai-content">
                <div class="comparison-box">
                  <div class="comparison-header">
                    <span>🔍</span>
                    <span>店铺对比分析</span>
                  </div>
                  <div class="comparison-content">
                    <div class="comparison-section">
                      <h5>🎯 适合人群</h5>
                      <p>{{ message.targetAudience }}</p>
                    </div>
                    <div class="comparison-section">
                      <h5>📍 位置特点</h5>
                      <p>{{ message.locationComparison }}</p>
                    </div>
                    <div class="comparison-section">
                      <h5>💰 价格差异</h5>
                      <p>{{ message.priceComparison }}</p>
                    </div>
                    <div class="comparison-section" v-if="message.atmosphereComparison">
                      <h5>🎭 氛围风格</h5>
                      <p>{{ message.atmosphereComparison }}</p>
                    </div>
                    <div class="comparison-section" v-if="message.foodComparison">
                      <h5>🍜 菜品特色</h5>
                      <p>{{ message.foodComparison }}</p>
                    </div>
                    <div class="final-advice">
                      <h5>💡 最终建议</h5>
                      <p>{{ message.finalAdvice }}</p>
                    </div>
                  </div>
                </div>
              </div>
            </template>

            <!-- AI Text Message -->
            <template v-else-if="message.type === 'ai'">
              <div class="message-avatar ai-avatar">
                <span>🤖</span>
              </div>
              <div class="message-content ai-content">
                <p>{{ message.content }}</p>
              </div>
            </template>
          </div>

          <!-- Loading State -->
          <div v-if="isLoading" class="message ai loading-message">
            <div class="message-avatar ai-avatar">
              <span>🤖</span>
            </div>
            <div class="message-content ai-content">
              <div class="typing-indicator">
                <span></span>
                <span></span>
                <span></span>
              </div>
              <p class="loading-text">AI 正在为您分析需求并挑选最佳餐厅...</p>
            </div>
          </div>
        </div>
      </div>

      <!-- Input Area -->
      <div class="smart-dialog-footer">
        <div class="input-wrapper">
          <input
            v-model="userInput"
            type="text"
            placeholder="描述您的需求，例如：附近有什么好吃的、推荐个电影、找家舒适的酒店..."
            @keyup.enter="sendQuery"
            :disabled="isLoading"
            ref="inputRef"
          />
          <button 
            class="send-btn"
            :class="{ 'can-send': userInput.trim() && !isLoading }"
            @click="sendQuery"
            :disabled="!userInput.trim() || isLoading"
          >
            <span v-if="!isLoading">发送</span>
            <span v-else class="loading-dots">
              <span></span>
              <span></span>
              <span></span>
            </span>
          </button>
        </div>
        <p class="input-hint">
          <span class="hint-icon">💡</span>
          AI会为您推荐美食、酒店、电影、景点等本地生活服务
        </p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, nextTick, watch } from 'vue';
import { useRouter } from 'vue-router';
import client from '../api/client';

const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  }
});

const emit = defineEmits(['update:visible', 'close']);
const router = useRouter();

// Reactive state
const userInput = ref('');
const messages = ref([]);
const isLoading = ref(false);
const chatBodyRef = ref(null);
const inputRef = ref(null);

    // Scene categories - 支持更多生活场景
const sceneCategories = [
  { type: 'gathering', icon: '👥', label: '朋友聚餐', prompt: '推荐适合朋友聚餐的地方，氛围轻松，可以聊天' },
  { type: 'date', icon: '💕', label: '情侣约会', prompt: '推荐适合情侣约会的地方，环境浪漫' },
  { type: 'movie', icon: '🎬', label: '看电影', prompt: '推荐好看的餐厅或适合约会后看电影的地方' },
  { type: 'hotel', icon: '🏨', label: '住宿酒店', prompt: '推荐舒适便捷的酒店或民宿' },
  { type: 'travel', icon: '🏞️', label: '周边游玩', prompt: '推荐周边适合游玩打卡的景点或休闲场所' },
  { type: 'budget', icon: '💰', label: '高性价比', prompt: '推荐性价比高的吃喝玩乐场所' }
];

// Methods
const closeDialog = () => {
  emit('update:visible', false);
  emit('close');
};

const navigateToShop = (shopId) => {
  closeDialog();
  router.push(`/shops/${shopId}`);
};

const usePrompt = (prompt) => {
  userInput.value = prompt;
  inputRef.value?.focus();
  sendQuery();
};

const formatTags = (tags) => {
  if (!tags) return [];
  return tags.split(/[,，]/).filter(tag => tag.trim()).slice(0, 3);
};

const scrollToBottom = async () => {
  await nextTick();
  if (chatBodyRef.value) {
    chatBodyRef.value.scrollTop = chatBodyRef.value.scrollHeight;
  }
};

// Generate AI thinking content
const generateThinkingContent = (query) => {
  const sceneKeywords = {
    '聚餐': '朋友/同事聚餐场景',
    '约会': '情侣约会场景',
    '商务': '商务宴请场景',
    '家庭': '家庭聚餐场景',
    '生日': '生日庆祝场景',
    '约会': '浪漫约会场景',
    '聊天': '轻松聊天场景'
  };
  
  const priceKeywords = {
    '便宜': '预算敏感型',
    '贵': '高端品质型',
    '性价比': '性价比优先型',
    '人均': '预算控制型',
    '实惠': '经济实惠型'
  };
  
  let sceneAnalysis = '综合用餐场景';
  let preferenceAnalysis = '注重整体用餐体验';
  
  for (const [keyword, scene] of Object.entries(sceneKeywords)) {
    if (query.includes(keyword)) {
      sceneAnalysis = scene;
      break;
    }
  }
  
  for (const [keyword, preference] of Object.entries(priceKeywords)) {
    if (query.includes(keyword)) {
      preferenceAnalysis = preference;
      break;
    }
  }
  
  return { sceneAnalysis, preferenceAnalysis };
};

// Generate comparison content
const generateComparison = (shops, query) => {
  const shopCount = Math.min(shops.length, 3);
  
  if (shopCount === 0) return null;
  
  // Target audience analysis
  const targetAudiences = {
    '聚餐': '适合3-8人的小型聚餐，氛围轻松不拘束',
    '约会': '适合情侣二人世界，环境私密浪漫',
    '商务': '适合正式商务场合，环境优雅得体',
    '家庭': '适合各年龄段家庭成员，菜品丰富',
    '生日': '适合生日聚会，有特色或包间'
  };
  
  let targetAudience = '适合一般用餐需求';
  for (const [keyword, desc] of Object.entries(targetAudiences)) {
    if (query.includes(keyword)) {
      targetAudience = desc;
      break;
    }
  }
  
  // Location comparison
  const locations = shops.slice(0, 3).map(s => s.address || s.city).filter(Boolean);
  const locationComparison = locations.length > 1 
    ? `第1家位于${locations[0]}，第2家位于${locations[1]}${locations[2] ? '，第3家位于' + locations[2] : ''}。建议根据您的出发地选择交通最便利的。`
    : '各店铺位置都比较便利';
  
  // Price comparison
  const prices = shops.slice(0, 3).map(s => s.avgPrice || 100).filter(Boolean);
  let priceComparison = '';
  if (prices.length >= 2) {
    const minPrice = Math.min(...prices);
    const maxPrice = Math.max(...prices);
    if (maxPrice - minPrice > 50) {
      priceComparison = `第1家性价比最高（约¥${minPrice}/人），第3家定位稍高端（约¥${maxPrice}/人），第2家居中。根据您的预算灵活选择。`;
    } else {
      priceComparison = '三家店铺价格相近，都在合理区间内，可根据口味偏好选择。';
    }
  } else {
    priceComparison = '价格适中，符合该类型餐厅的正常水平。';
  }
  
  // Atmosphere comparison
  const ratings = shops.slice(0, 3).map(s => s.rating || 4.0);
  const maxRatingIndex = ratings.indexOf(Math.max(...ratings));
  const atmosphereComparison = `第${maxRatingIndex + 1}家评分最高（${ratings[maxRatingIndex].toFixed(1)}分），环境和服务最受好评。`;
  
  // Food comparison
  const categories = [...new Set(shops.slice(0, 3).map(s => s.category).filter(Boolean))];
  const foodComparison = categories.length > 1
    ? `菜系风格各异：${categories.join('、')}，可以满足不同口味需求。`
    : '专注' + (categories[0] || '特色') + '菜系，口味正宗。';
  
  // Final advice
  const finalAdvice = `综合推荐：如果您${query.includes('预算') || query.includes('便宜') ? '注重性价比' : '追求用餐体验'}，建议首选第1家；如果想要${query.includes('商务') ? '更正式的环境' : '更好的氛围'}，第2家是不错的选择；${shopCount > 2 ? '第3家适合追求品质的用餐者。' : ''}`;
  
  return {
    targetAudience,
    locationComparison,
    priceComparison,
    atmosphereComparison,
    foodComparison,
    finalAdvice
  };
};

const sendQuery = async () => {
  const query = userInput.value.trim();
  if (!query || isLoading.value) return;

  messages.value.push({
    type: 'user',
    content: query
  });

  userInput.value = '';
  isLoading.value = true;

  await scrollToBottom();

  try {
    const userId = localStorage.getItem('dp_user_id');
    const city = localStorage.getItem('dp_city') || '上海';

    const response = await client.post('/api/ai/chat', {
      userId: userId ? Number(userId) : null,
      city,
      query: query
    });

    if (response.data && response.data.success) {
      const result = response.data.data;
      
      // 显示AI分析过程
      messages.value.push({
        type: 'ai-thinking',
        sceneAnalysis: result.scene || '综合场景',
        preferenceAnalysis: result.reasoning || '根据您的需求分析'
      });
      
      await scrollToBottom();

      if (result.recommendations && result.recommendations.length > 0) {
        messages.value.push({
          type: 'ai-suggestion',
          content: result.reply || `根据您的需求，我为您精选了${Math.min(result.recommendations.length, 3)}家最合适的地方。`
        });
        
        await scrollToBottom();
        
        const shops = result.recommendations.map(r => ({
          ...r.shop,
          recommendReason: r.reason
        }));
        
        messages.value.push({
          type: 'shops',
          shops: shops
        });
        
        await scrollToBottom();
      } else {
        messages.value.push({
          type: 'ai',
          content: result.reply || '抱歉，没有找到完全符合您需求的地方。建议您尝试调整需求描述。'
        });
      }
    } else {
      messages.value.push({
        type: 'ai',
        content: '获取推荐失败，请稍后重试。'
      });
    }
  } catch (error) {
    console.error('AI chat error:', error);
    messages.value.push({
      type: 'ai',
      content: '网络连接异常，请检查网络后重试。'
    });
  } finally {
    isLoading.value = false;
    await scrollToBottom();
    inputRef.value?.focus();
  }
};

// Focus input when dialog opens
watch(() => props.visible, (newVal) => {
  if (newVal) {
    nextTick(() => {
      inputRef.value?.focus();
    });
  }
});
</script>

<style scoped>
/* Modal Overlay */
.smart-dialog-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.6);
  backdrop-filter: blur(8px);
  display: grid;
  place-items: center;
  z-index: 1000;
  animation: fadeIn 0.2s ease-out;
  padding: 20px;
}

@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

/* Dialog Container */
.smart-dialog {
  background: #fff;
  border-radius: 20px;
  width: 100%;
  max-width: 800px;
  max-height: 85vh;
  display: flex;
  flex-direction: column;
  box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.25), 0 0 0 1px rgba(255, 107, 53, 0.1);
  animation: slideUp 0.3s ease-out;
  overflow: hidden;
}

@keyframes slideUp {
  from {
    opacity: 0;
    transform: translateY(24px) scale(0.96);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

/* Header */
.smart-dialog-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 24px;
  border-bottom: 1px solid #e5e7eb;
  background: linear-gradient(135deg, rgba(255, 107, 53, 0.08) 0%, rgba(255, 143, 92, 0.05) 100%);
}

.header-title {
  display: flex;
  align-items: center;
  gap: 12px;
}

.ai-icon {
  width: 44px;
  height: 44px;
  border-radius: 12px;
  background: linear-gradient(135deg, #FF6B35 0%, #FF8F5C 100%);
  display: grid;
  place-items: center;
  font-size: 24px;
  box-shadow: 0 4px 12px rgba(255, 107, 53, 0.3);
}

.header-title h2 {
  font-size: 20px;
  font-weight: 600;
  color: #111827;
  margin: 0;
}

.close-btn {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  border: none;
  background: #f3f4f6;
  color: #6b7280;
  font-size: 24px;
  display: grid;
  place-items: center;
  cursor: pointer;
  transition: all 0.2s;
  line-height: 1;
}

.close-btn:hover {
  background: #e5e7eb;
  color: #111827;
  transform: rotate(90deg);
}

/* Body */
.smart-dialog-body {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
  scroll-behavior: smooth;
}

/* Welcome Section */
.welcome-section {
  text-align: center;
  padding: 48px 24px;
  animation: fadeIn 0.3s ease-out;
}

.welcome-icon {
  font-size: 64px;
  margin-bottom: 20px;
}

.welcome-section h3 {
  font-size: 24px;
  font-weight: 600;
  color: #111827;
  margin: 0 0 12px;
}

.welcome-section p {
  font-size: 16px;
  color: #6b7280;
  margin: 0 0 32px;
}

.scene-categories {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
  max-width: 600px;
  margin: 0 auto;
}

.scene-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 20px 16px;
  border: 2px solid #e5e7eb;
  border-radius: 16px;
  background: #fff;
  cursor: pointer;
  transition: all 0.3s;
}

.scene-card:hover {
  border-color: #FF6B35;
  background: rgba(255, 107, 53, 0.05);
  transform: translateY(-4px);
  box-shadow: 0 12px 24px rgba(0, 0, 0, 0.1);
}

.scene-icon {
  font-size: 32px;
}

.scene-label {
  font-size: 14px;
  font-weight: 500;
  color: #374151;
}

/* Chat Messages */
.chat-messages {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.message {
  display: flex;
  gap: 12px;
  animation: messageSlideIn 0.3s ease-out forwards;
  opacity: 0;
  transform: translateY(10px);
}

@keyframes messageSlideIn {
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.message-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  display: grid;
  place-items: center;
  font-size: 20px;
  flex-shrink: 0;
}

.user-avatar {
  background: #f3f4f6;
  order: 1;
}

.ai-avatar {
  background: linear-gradient(135deg, #FF6B35 0%, #FF8F5C 100%);
  box-shadow: 0 4px 12px rgba(255, 107, 53, 0.3);
}

.message-content {
  max-width: calc(100% - 60px);
  border-radius: 16px;
  padding: 16px 20px;
}

.user-content {
  background: #FF6B35;
  color: #fff;
  margin-left: auto;
  border-bottom-right-radius: 4px;
}

.user-content p {
  margin: 0;
  font-size: 15px;
  line-height: 1.5;
}

.ai-content {
  background: #f9fafb;
  border: 1px solid #e5e7eb;
  border-bottom-left-radius: 4px;
}

.ai-content p {
  margin: 0;
  font-size: 15px;
  color: #374151;
  line-height: 1.6;
}

/* Thinking Box */
.thinking-box {
  background: linear-gradient(135deg, rgba(255, 107, 53, 0.08) 0%, rgba(255, 143, 92, 0.05) 100%);
  border-radius: 12px;
  padding: 16px;
  border-left: 4px solid #FF6B35;
}

.thinking-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;
  font-size: 14px;
  font-weight: 600;
  color: #FF6B35;
}

.sparkle {
  animation: twinkle 1.5s ease-in-out infinite;
}

@keyframes twinkle {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.5; }
}

.thinking-content p {
  margin: 8px 0;
  color: #4b5563;
  font-size: 14px;
}

.thinking-content strong {
  color: #111827;
}

/* Suggestion Box */
.suggestion-box {
  background: linear-gradient(135deg, rgba(16, 185, 129, 0.08) 0%, rgba(5, 150, 105, 0.05) 100%);
  border-radius: 12px;
  padding: 16px;
  border-left: 4px solid #10B981;
}

.suggestion-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;
  font-size: 14px;
  font-weight: 600;
  color: #059669;
}

.suggestion-intro {
  color: #374151;
  font-size: 15px;
  line-height: 1.6;
}

/* Shop Cards Container */
.shops-content {
  background: transparent;
  border: none;
  padding: 0;
  max-width: 100%;
}

.shop-cards-container {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

/* Enhanced Shop Card */
.shop-card-enhanced {
  display: grid;
  grid-template-columns: 140px 1fr;
  gap: 16px;
  padding: 16px;
  background: #fff;
  border: 2px solid #e5e7eb;
  border-radius: 16px;
  cursor: pointer;
  transition: all 0.3s;
  position: relative;
}

.shop-card-enhanced:hover {
  border-color: #FF6B35;
  box-shadow: 0 8px 24px rgba(255, 107, 53, 0.15);
  transform: translateY(-2px);
}

.card-header {
  position: relative;
}

.rank-badge {
  position: absolute;
  top: -8px;
  left: -8px;
  width: 32px;
  height: 32px;
  border-radius: 50%;
  display: grid;
  place-items: center;
  font-size: 14px;
  font-weight: 700;
  color: #fff;
  z-index: 2;
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.2);
}

.rank-1 {
  background: linear-gradient(135deg, #FFD700 0%, #FFA500 100%);
}

.rank-2 {
  background: linear-gradient(135deg, #C0C0C0 0%, #A0A0A0 100%);
}

.rank-3 {
  background: linear-gradient(135deg, #CD7F32 0%, #B87333 100%);
}

.shop-image-wrapper {
  position: relative;
  width: 140px;
  height: 140px;
  border-radius: 12px;
  overflow: hidden;
  background: linear-gradient(135deg, #FFE5D0 0%, #FFCDB2 100%);
}

.shop-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s;
}

.shop-card-enhanced:hover .shop-image {
  transform: scale(1.05);
}

.category-tag {
  position: absolute;
  top: 8px;
  right: 8px;
  background: rgba(255, 255, 255, 0.95);
  padding: 4px 10px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 600;
  color: #FF6B35;
  backdrop-filter: blur(4px);
}

.card-body {
  display: flex;
  flex-direction: column;
  gap: 8px;
  min-width: 0;
}

.shop-name {
  font-size: 18px;
  font-weight: 600;
  color: #111827;
  margin: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.shop-meta {
  display: flex;
  align-items: center;
  gap: 16px;
}

.rating {
  display: flex;
  align-items: center;
  gap: 4px;
  background: linear-gradient(135deg, #FF6B35 0%, #FF8F5C 100%);
  color: #fff;
  padding: 4px 10px;
  border-radius: 20px;
  font-size: 14px;
  font-weight: 600;
}

.star {
  font-size: 12px;
}

.price {
  font-size: 14px;
  color: #059669;
  font-weight: 500;
}

.shop-address {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
  color: #6b7280;
  margin: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.shop-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.tag {
  background: #f3f4f6;
  color: #6b7280;
  padding: 4px 10px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 500;
}

.shop-highlights {
  display: flex;
  flex-direction: column;
  gap: 6px;
  margin-top: 4px;
}

.highlight-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  color: #059669;
}

.highlight-icon {
  width: 18px;
  height: 18px;
  border-radius: 50%;
  background: rgba(5, 150, 105, 0.1);
  display: grid;
  place-items: center;
  font-size: 10px;
  font-weight: 700;
}

.card-footer {
  grid-column: 1 / -1;
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px dashed #e5e7eb;
}

.recommend-reason {
  font-size: 13px;
  color: #FF6B35;
  font-style: italic;
}

/* Comparison Box */
.comparison-box {
  background: linear-gradient(135deg, rgba(59, 130, 246, 0.08) 0%, rgba(99, 102, 241, 0.05) 100%);
  border-radius: 12px;
  padding: 20px;
  border-left: 4px solid #3B82F6;
}

.comparison-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 16px;
  font-size: 16px;
  font-weight: 600;
  color: #2563EB;
}

.comparison-content {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.comparison-section {
  padding: 12px 16px;
  background: rgba(255, 255, 255, 0.6);
  border-radius: 8px;
}

.comparison-section h5 {
  font-size: 14px;
  font-weight: 600;
  color: #111827;
  margin: 0 0 8px;
  display: flex;
  align-items: center;
  gap: 6px;
}

.comparison-section p {
  margin: 0;
  font-size: 14px;
  color: #4b5563;
  line-height: 1.6;
}

.final-advice {
  padding: 16px;
  background: linear-gradient(135deg, rgba(16, 185, 129, 0.1) 0%, rgba(5, 150, 105, 0.05) 100%);
  border-radius: 8px;
  border: 1px solid rgba(16, 185, 129, 0.2);
}

.final-advice h5 {
  font-size: 14px;
  font-weight: 600;
  color: #059669;
  margin: 0 0 8px;
}

.final-advice p {
  margin: 0;
  font-size: 14px;
  color: #374151;
  line-height: 1.6;
}

/* Loading State */
.loading-message .ai-content {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.typing-indicator {
  display: flex;
  gap: 6px;
  padding: 8px 0;
}

.typing-indicator span {
  width: 10px;
  height: 10px;
  background: #FF6B35;
  border-radius: 50%;
  animation: typing 1.4s ease-in-out infinite;
}

.typing-indicator span:nth-child(1) { animation-delay: 0s; }
.typing-indicator span:nth-child(2) { animation-delay: 0.2s; }
.typing-indicator span:nth-child(3) { animation-delay: 0.4s; }

@keyframes typing {
  0%, 60%, 100% { transform: translateY(0); }
  30% { transform: translateY(-10px); }
}

.loading-text {
  font-size: 14px;
  color: #6b7280;
}

/* Footer */
.smart-dialog-footer {
  padding: 20px 24px;
  border-top: 1px solid #e5e7eb;
  background: #f9fafb;
}

.input-wrapper {
  display: flex;
  gap: 12px;
  background: #fff;
  border: 2px solid #e5e7eb;
  border-radius: 50px;
  padding: 4px 4px 4px 20px;
  transition: all 0.2s;
}

.input-wrapper:focus-within {
  border-color: #FF6B35;
  box-shadow: 0 0 0 4px rgba(255, 107, 53, 0.1);
}

.input-wrapper input {
  flex: 1;
  border: none;
  outline: none;
  font-size: 15px;
  color: #111827;
  background: transparent;
  min-width: 0;
}

.input-wrapper input::placeholder {
  color: #9ca3af;
}

.input-wrapper input:disabled {
  cursor: not-allowed;
}

.send-btn {
  padding: 10px 24px;
  border: none;
  border-radius: 50px;
  background: #e5e7eb;
  color: #9ca3af;
  font-size: 15px;
  font-weight: 600;
  cursor: not-allowed;
  transition: all 0.2s;
  display: flex;
  align-items: center;
  gap: 4px;
}

.send-btn.can-send {
  background: linear-gradient(135deg, #FF6B35 0%, #FF8F5C 100%);
  color: #fff;
  cursor: pointer;
  box-shadow: 0 4px 12px rgba(255, 107, 53, 0.3);
}

.send-btn.can-send:hover {
  transform: translateY(-1px);
  box-shadow: 0 6px 16px rgba(255, 107, 53, 0.4);
}

.send-btn.can-send:active {
  transform: translateY(0) scale(0.98);
}

.loading-dots {
  display: flex;
  gap: 4px;
}

.loading-dots span {
  width: 6px;
  height: 6px;
  background: currentColor;
  border-radius: 50%;
  animation: dotPulse 1.4s ease-in-out infinite;
}

.loading-dots span:nth-child(2) { animation-delay: 0.2s; }
.loading-dots span:nth-child(3) { animation-delay: 0.4s; }

@keyframes dotPulse {
  0%, 100% { opacity: 0.4; }
  50% { opacity: 1; }
}

.input-hint {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  margin: 12px 0 0;
  font-size: 13px;
  color: #6b7280;
}

.hint-icon {
  font-size: 14px;
}

/* Scrollbar Styling */
.smart-dialog-body::-webkit-scrollbar {
  width: 6px;
}

.smart-dialog-body::-webkit-scrollbar-track {
  background: transparent;
}

.smart-dialog-body::-webkit-scrollbar-thumb {
  background-color: rgba(255, 107, 53, 0.2);
  border-radius: 3px;
}

.smart-dialog-body::-webkit-scrollbar-thumb:hover {
  background-color: rgba(255, 107, 53, 0.4);
}

/* Responsive Design */
@media (max-width: 640px) {
  .smart-dialog-overlay {
    padding: 0;
    align-items: flex-end;
  }

  .smart-dialog {
    max-height: 90vh;
    border-radius: 20px 20px 0 0;
    animation: slideUpMobile 0.3s ease-out;
  }

  @keyframes slideUpMobile {
    from {
      opacity: 0;
      transform: translateY(100%);
    }
    to {
      opacity: 1;
      transform: translateY(0);
    }
  }

  .scene-categories {
    grid-template-columns: repeat(2, 1fr);
  }

  .shop-card-enhanced {
    grid-template-columns: 100px 1fr;
  }

  .shop-image-wrapper {
    width: 100px;
    height: 100px;
  }

  .shop-name {
    font-size: 16px;
  }

  .comparison-content {
    gap: 12px;
  }
}

@media (max-width: 480px) {
  .message-content {
    max-width: calc(100% - 56px);
  }

  .shop-card-enhanced {
    grid-template-columns: 1fr;
  }

  .shop-image-wrapper {
    width: 100%;
    height: 160px;
  }

  .card-header {
    display: contents;
  }

  .rank-badge {
    position: absolute;
    top: 8px;
    left: 8px;
  }
}
</style>

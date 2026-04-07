<template>
  <div class="shop-manage">
    <h1 class="page-title">门店管理</h1>

    <div class="form-card">
      <h2>{{ editingShopId ? '编辑门店' : '创建门店' }}</h2>
      <div class="form-grid">
        <input v-model="shopForm.name" placeholder="门店名称" />
        <input v-model="shopForm.category" placeholder="分类" />
        <input v-model="shopForm.city" placeholder="城市" />
        <input v-model="shopForm.tags" placeholder="标签(逗号分隔)" />
        <input v-model="shopForm.address" placeholder="地址" />
        <input v-model="shopForm.businessHours" placeholder="营业时间，例如 10:00-22:00" />
        <input v-model="shopForm.contactPhone" placeholder="联系电话" />
        <input type="file" accept="image/*" @change="onShopImageChange" />
        <div v-if="shopForm.imagePreview" class="image-preview">
          <img :src="shopForm.imagePreview" alt="店铺图片预览" />
          <button type="button" class="remove-btn" @click="shopForm.imagePreview = ''; shopForm.imageUrl = ''">×</button>
        </div>
        <div class="form-actions">
          <button class="cta" @click="submitShop">{{ editingShopId ? '更新门店' : '创建门店' }}</button>
          <button v-if="editingShopId" class="ghost-btn" @click="resetShopForm">取消编辑</button>
        </div>
        <span>{{ shopMessage }}</span>
      </div>
    </div>

    <div class="list-card">
      <h2>我的门店</h2>
      <div class="shop-list">
        <div v-for="shop in shopList" :key="shop.id" class="shop-item">
          <div class="shop-info">
            <strong>{{ shop.name }}</strong>
            <div class="shop-tags">
              <span class="tag">{{ shop.category || "综合" }}</span>
              <span class="tag">{{ shop.city || "-" }}</span>
              <span class="tag" :class="'status-' + shop.auditStatus">审核 {{ shop.auditStatus }}</span>
            </div>
          </div>
          <button class="ghost-btn" @click="editShop(shop)">编辑</button>
        </div>
        <div v-if="shopList.length === 0" class="empty-state">暂无门店</div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from "vue";
import { createShop, updateShop, listMyShops } from "../api/shop";
import { uploadImage } from "../api/oss";

const shopForm = ref({
  name: "",
  category: "",
  city: "上海",
  tags: "",
  address: "",
  businessHours: "",
  contactPhone: "",
  imageUrl: "",
  imagePreview: ""
});
const shopMessage = ref("");
const shopList = ref([]);
const editingShopId = ref(null);

const loadShops = async () => {
  try {
    const token = localStorage.getItem("dp_token");
    const merchantId = localStorage.getItem("dp_merchant_id");
    
    if (!token || !merchantId) {
      shopMessage.value = "错误：未找到登录凭证，请重新登录";
      return;
    }
    
    const response = await listMyShops();
    
    if (response.success) {
      shopList.value = response.data || [];
    } else {
      shopMessage.value = "获取门店列表失败: " + (response.message || "未知错误");
    }
  } catch (error) {
    const errorMsg = error.userMessage || error.message || "网络错误";
    shopMessage.value = "获取门店列表失败: " + errorMsg;
  }
};

const resetShopForm = () => {
  shopForm.value = {
    name: "",
    category: "",
    city: "上海",
    tags: "",
    address: "",
    businessHours: "",
    contactPhone: "",
    imageUrl: "",
    imagePreview: ""
  };
  editingShopId.value = null;
};

const editShop = (shop) => {
  editingShopId.value = shop.id;
  shopForm.value = {
    name: shop.name,
    category: shop.category,
    city: shop.city,
    tags: shop.tags || "",
    address: shop.address || "",
    businessHours: shop.businessHours || "",
    contactPhone: shop.contactPhone || "",
    imageUrl: shop.imageUrl || "",
    imagePreview: shop.imageUrl || ""
  };
};

const submitShop = async () => {
  shopMessage.value = "";
  if (!shopForm.value.name || !shopForm.value.category || !shopForm.value.city) {
    shopMessage.value = "请填写门店名称、分类和城市";
    return;
  }
  
  const sensitiveWords = ['测试', 'test', 'TEST'];
  const nameLower = shopForm.value.name.toLowerCase();
  const hasSensitiveWord = sensitiveWords.some(word => nameLower.includes(word.toLowerCase()));
  if (hasSensitiveWord) {
    shopMessage.value = "提示：店铺名称包含'测试'等敏感词可能导致审核被拒绝，建议使用真实店铺名称";
  }
  
  let response;
  if (editingShopId.value) {
    response = await updateShop(editingShopId.value, shopForm.value);
    shopMessage.value = response.success ? "门店更新成功" : response.message || "门店更新失败";
  } else {
    response = await createShop(shopForm.value);
    if (response.success) {
      shopMessage.value = "门店创建成功";
    } else {
      if (response.message && response.message.includes("Content violates")) {
        shopMessage.value = "创建失败：内容违反社区规范（可能包含敏感词'测试'等）。请使用真实店铺名称重试。";
      } else {
        shopMessage.value = response.message || "门店创建失败";
      }
    }
  }
  
  if (response.success) {
    resetShopForm();
    await loadShops();
  }
};

const onShopImageChange = async (event) => {
  const file = event.target.files?.[0];
  if (!file) return;
  
  const reader = new FileReader();
  reader.onload = (e) => {
    shopForm.value.imagePreview = e.target.result;
  };
  reader.readAsDataURL(file);
  
  const response = await uploadImage(file, "shops");
  if (response.success) {
    shopForm.value.imageUrl = response.data?.url || "";
    shopMessage.value = "店铺图片上传成功";
  } else {
    shopMessage.value = response.message || "店铺图片上传失败";
  }
};

onMounted(async () => {
  await loadShops();
});
</script>

<style scoped>
.shop-manage {
  max-width: 1200px;
}

.page-title {
  font-size: 1.8rem;
  font-weight: 600;
  margin-bottom: 24px;
  color: #1a1a2e;
}

.form-card {
  background: #fff;
  border-radius: 12px;
  padding: 24px;
  margin-bottom: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.form-card h2 {
  font-size: 1.2rem;
  margin-bottom: 20px;
  color: #1a1a2e;
}

.form-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 16px;
}

.form-grid input {
  padding: 12px 16px;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  font-size: 14px;
}

.form-grid input:focus {
  outline: none;
  border-color: #ff6b35;
}

.form-actions {
  display: flex;
  gap: 12px;
  grid-column: 1 / -1;
}

.cta {
  padding: 12px 24px;
  background: #ff6b35;
  color: #fff;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-weight: 500;
  transition: background 0.2s;
}

.cta:hover {
  background: #e55a2b;
}

.ghost-btn {
  padding: 12px 24px;
  background: transparent;
  color: #666;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  cursor: pointer;
  font-weight: 500;
  transition: all 0.2s;
}

.ghost-btn:hover {
  border-color: #ff6b35;
  color: #ff6b35;
}

.image-preview {
  position: relative;
  display: inline-block;
  width: 100px;
  height: 100px;
  border-radius: 8px;
  overflow: hidden;
  border: 2px solid #e0e0e0;
}

.image-preview img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.remove-btn {
  position: absolute;
  top: 4px;
  right: 4px;
  width: 24px;
  height: 24px;
  border-radius: 50%;
  background: rgba(0, 0, 0, 0.6);
  color: #fff;
  border: none;
  font-size: 16px;
  line-height: 1;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
}

.remove-btn:hover {
  background: rgba(220, 53, 69, 0.9);
}

.list-card {
  background: #fff;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.list-card h2 {
  font-size: 1.2rem;
  margin-bottom: 20px;
  color: #1a1a2e;
}

.shop-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.shop-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  background: #f8f9fa;
  border-radius: 8px;
  transition: background 0.2s;
}

.shop-item:hover {
  background: #f0f0f0;
}

.shop-info {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.shop-info strong {
  font-size: 16px;
  color: #1a1a2e;
}

.shop-tags {
  display: flex;
  gap: 8px;
}

.tag {
  padding: 4px 10px;
  background: #e9ecef;
  border-radius: 4px;
  font-size: 12px;
  color: #666;
}

.tag.status-PENDING {
  background: #fff3cd;
  color: #856404;
}

.tag.status-APPROVED {
  background: #d4edda;
  color: #155724;
}

.tag.status-REJECTED {
  background: #f8d7da;
  color: #721c24;
}

.empty-state {
  text-align: center;
  padding: 40px;
  color: #999;
}
</style>

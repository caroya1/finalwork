<template>
  <div class="dish-manage">
    <h1 class="page-title">菜品管理</h1>

    <div class="shop-selector">
      <label>选择门店：</label>
      <select v-model="activeShopId" @change="onShopChange">
        <option value="">请选择门店</option>
        <option v-for="shop in shopList" :key="shop.id" :value="String(shop.id)">{{ shop.name }}</option>
      </select>
    </div>

    <div v-if="activeShopId" class="content-area">
      <div class="form-card">
        <h2>新增菜品</h2>
        <div class="form-grid">
          <input v-model="dishForm.name" placeholder="菜品名称" />
          <input v-model.number="dishForm.price" type="number" step="0.01" placeholder="价格（元）" />
          <input v-model="dishForm.description" placeholder="描述" />
          <input type="file" accept="image/*" @change="onDishImageChange" />
          <div v-if="dishForm.imagePreview" class="image-preview">
            <img :src="dishForm.imagePreview" alt="菜品图片预览" />
            <button type="button" class="remove-btn" @click="dishForm.imagePreview = ''; dishForm.imageUrl = ''">×</button>
          </div>
          <button class="cta" @click="submitDish">新增菜品</button>
          <span>{{ dishMessage }}</span>
        </div>
      </div>

      <div class="list-card">
        <h2>菜品列表</h2>
        <div class="dish-list">
          <div v-for="dish in dishList" :key="dish.id" class="dish-item">
            <div class="dish-info">
              <strong>{{ dish.name }}</strong>
              <div class="dish-tags">
                <span class="tag price">¥{{ (dish.price / 100).toFixed(2) }}</span>
                <span class="tag" :class="'status-' + dish.status">{{ dish.status }}</span>
              </div>
            </div>
            <button class="ghost-btn" @click="removeDish(dish.id)">删除</button>
          </div>
          <div v-if="dishList.length === 0" class="empty-state">暂无菜品</div>
        </div>
      </div>
    </div>

    <div v-else class="empty-state-card">
      <p>请先选择一个门店</p>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from "vue";
import { listMyShops, listDishes, createDish, deleteDish } from "../api/shop";
import { uploadImage } from "../api/oss";

const shopList = ref([]);
const dishList = ref([]);
const activeShopId = ref("");

const dishForm = ref({
  name: "",
  price: 0,
  description: "",
  imageUrl: "",
  imagePreview: ""
});
const dishMessage = ref("");

const loadShops = async () => {
  try {
    const response = await listMyShops();
    if (response.success) {
      shopList.value = response.data || [];
    }
  } catch (error) {
    console.error("加载门店列表失败:", error);
  }
};

const loadDishes = async () => {
  if (!activeShopId.value) return;
  const response = await listDishes(Number(activeShopId.value));
  if (response.success) {
    dishList.value = response.data || [];
  }
};

const onShopChange = async () => {
  if (activeShopId.value) {
    await loadDishes();
  } else {
    dishList.value = [];
  }
};

const submitDish = async () => {
  dishMessage.value = "";
  if (!activeShopId.value) {
    dishMessage.value = "请先选择门店";
    return;
  }
  if (!dishForm.value.name || !dishForm.value.price) {
    dishMessage.value = "请填写菜品名称和价格";
    return;
  }
  const response = await createDish(Number(activeShopId.value), {
    ...dishForm.value,
    price: dishForm.value.price * 100
  });
  dishMessage.value = response.success ? "菜品新增成功" : response.message || "新增失败";
  if (response.success) {
    dishForm.value = { name: "", price: 0, description: "", imageUrl: "", imagePreview: "" };
    await loadDishes();
  }
};

const removeDish = async (dishId) => {
  if (!activeShopId.value) return;
  const response = await deleteDish(Number(activeShopId.value), dishId);
  if (response.success) {
    await loadDishes();
  }
};

const onDishImageChange = async (event) => {
  const file = event.target.files?.[0];
  if (!file) return;
  
  const reader = new FileReader();
  reader.onload = (e) => {
    dishForm.value.imagePreview = e.target.result;
  };
  reader.readAsDataURL(file);
  
  const response = await uploadImage(file, "dishes");
  if (response.success) {
    dishForm.value.imageUrl = response.data?.url || "";
    dishMessage.value = "菜品图片上传成功";
  } else {
    dishMessage.value = response.message || "菜品图片上传失败";
  }
};

onMounted(async () => {
  await loadShops();
});
</script>

<style scoped>
.dish-manage {
  max-width: 1200px;
}

.page-title {
  font-size: 1.8rem;
  font-weight: 600;
  margin-bottom: 24px;
  color: #1a1a2e;
}

.shop-selector {
  background: #fff;
  border-radius: 12px;
  padding: 20px 24px;
  margin-bottom: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  display: flex;
  align-items: center;
  gap: 12px;
}

.shop-selector label {
  font-weight: 500;
  color: #666;
}

.shop-selector select {
  padding: 10px 16px;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  font-size: 14px;
  min-width: 200px;
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

.dish-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.dish-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  background: #f8f9fa;
  border-radius: 8px;
  transition: background 0.2s;
}

.dish-item:hover {
  background: #f0f0f0;
}

.dish-info {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.dish-info strong {
  font-size: 16px;
  color: #1a1a2e;
}

.dish-tags {
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

.tag.price {
  background: #ff6b35;
  color: #fff;
  font-weight: 500;
}

.tag.status-AVAILABLE {
  background: #d4edda;
  color: #155724;
}

.tag.status-SOLD_OUT {
  background: #f8d7da;
  color: #721c24;
}

.ghost-btn {
  padding: 8px 16px;
  background: transparent;
  color: #dc3545;
  border: 1px solid #dc3545;
  border-radius: 8px;
  cursor: pointer;
  font-weight: 500;
  transition: all 0.2s;
}

.ghost-btn:hover {
  background: #dc3545;
  color: #fff;
}

.empty-state {
  text-align: center;
  padding: 40px;
  color: #999;
}

.empty-state-card {
  background: #fff;
  border-radius: 12px;
  padding: 60px;
  text-align: center;
  color: #999;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}
</style>

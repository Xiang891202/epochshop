<template>
  <div class="product-detail-container">
    <router-link to="/products" class="back-link">← 返回商品列表</router-link>
    <div v-if="loading">載入中...</div>
    <div v-else-if="error">發生錯誤：{{ error }}</div>
    <div v-else-if="product" class="product-detail">
      <div class="product-image">
        <img v-if="product.imageUrl" :src="product.imageUrl" :alt="product.name" />
        <div v-else class="no-image">暫無圖片</div>
      </div>
      <div class="product-info">
        <h1>{{ product.name }}</h1>
        <p class="product-price">${{ product.price.toLocaleString() }}</p>
        <p class="product-description">{{ product.description }}</p>
        <p class="product-stock">庫存：{{ product.stock }}</p>

        <div class="product-actions" v-if="!isAdmin">
          <div class="quantity-selector">
            <label>數量：</label>
            <input type="number" v-model="quantity" min="1" :max="product.stock" />
          </div>
          <button @click="addToCart" :disabled="adding" class="btn btn-primary btn-lg">
            {{ adding ? '加入中...' : '加入購物車' }}
          </button>
          <button @click="contactSeller" class="btn btn-outline btn-lg">📩 聯絡賣家</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed, inject } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import axios from '../utils/axios';

const toast = inject('toast') as (text: string, type?: string) => void;

interface Product {
  id: number;
  name: string;
  description: string;
  price: number;
  stock: number;
  imageUrl?: string;
  seller: { id: number };
}

const route = useRoute();
const router = useRouter();
const product = ref<Product | null>(null);
const loading = ref(true);
const error = ref<string | null>(null);
const quantity = ref(1);
const adding = ref(false);

const isAdmin = computed(() => localStorage.getItem('role') === 'ROLE_ADMIN');

onMounted(async () => {
  try {
    const id = route.params.id;
    const res = await axios.get(`/products/${id}`);
    product.value = res.data;
  } catch (err: any) {
    error.value = err.response?.data || err.message;
  } finally {
    loading.value = false;
  }
});

const addToCart = async () => {
  if (adding.value || !product.value) return;
  adding.value = true;
  try {
    const tokenRes = await axios.get('/cart/idempotent-token');
    const idempotentKey = tokenRes.data.token;
    await axios.post('/cart/items', {
      productId: product.value.id,
      quantity: quantity.value,
      idempotentKey,
    });
    toast('加入購物車成功！', 'success');
  } catch (err: any) {
    toast('加入購物車失敗：' + (err.response?.data || err.message), 'error');
  } finally {
    adding.value = false;
  }
};

const contactSeller = () => {
  if (!product.value) return;
  router.push({ path: '/messages', query: { userId: product.value.seller.id.toString() } });
};
</script>

<style scoped>
.product-detail-container { max-width: 1000px; margin: 0 auto; padding: 20px; }
.back-link { display: inline-block; margin-bottom: 20px; color: var(--info-color); text-decoration: none; }
.product-detail { display: flex; gap: 40px; margin-top: 20px; }
.product-image { flex: 1; max-width: 500px; }
.product-image img { width: 100%; border-radius: 12px; object-fit: cover; }
.no-image { width: 100%; height: 400px; background: #f0f0f0; display: flex; align-items: center; justify-content: center; border-radius: 12px; color: #999; }
.product-info { flex: 1; }
.product-info h1 { font-size: 2rem; margin-bottom: 10px; }
.product-price { font-size: 1.8rem; color: var(--danger-color); font-weight: bold; margin-bottom: 15px; }
.product-description { font-size: 1.1rem; color: #666; margin-bottom: 20px; line-height: 1.6; }
.product-stock { font-size: 1rem; color: #999; margin-bottom: 25px; }
.quantity-selector { margin-bottom: 20px; display: flex; align-items: center; gap: 10px; }
.quantity-selector input { width: 80px; padding: 8px; border: 1px solid var(--border-color); border-radius: 6px; }
.btn-lg { padding: 12px 24px; font-size: 1.1rem; margin-right: 10px; }
.btn-outline { background: transparent; border: 2px solid var(--primary-color); color: var(--primary-color); }
@media (max-width: 768px) {
  .product-detail { flex-direction: column; }
  .product-image { max-width: 100%; }
}
</style>
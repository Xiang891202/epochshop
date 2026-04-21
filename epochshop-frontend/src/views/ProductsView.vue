<template>
  <div class="products-container">
    <h1>🛒 瞬購秒殺商城 - 商品列表</h1>
    <div class="header-actions">
      <router-link to="/cart" class="cart-link">🛒 購物車</router-link>
      <router-link to="/orders" class="orders-link">📋 我的訂單</router-link>
      <button @click="logout" class="logout-btn">登出</button>
    </div>
    <div v-if="loading">載入中...</div>
    <div v-else-if="error">發生錯誤：{{ error }}</div>
    <ul v-else>
      <li v-for="product in products" :key="product.id">
        <strong>{{ product.name }}</strong> - ${{ product.price }}
        <p>{{ product.description }}</p>
        <small>庫存：{{ product.stock }}</small>
        <div class="product-actions">
          <input type="number" v-model="quantities[product.id]" min="1" :max="product.stock" />
          <button @click="addToCart(product.id)" :disabled="adding[product.id]">
            {{ adding[product.id] ? '加入中...' : '加入購物車' }}
          </button>
        </div>
      </li>
    </ul>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import axios from '../utils/axios';
import { useRouter } from 'vue-router';

interface Product {
  id: number;
  name: string;
  description: string;
  price: number;
  stock: number;
}

const router = useRouter();
const products = ref<Product[]>([]);
const quantities = ref<Record<number, number>>({});
const loading = ref(true);
const error = ref<string | null>(null);
// 在 <script setup> 中加入
const adding = ref<Record<number, boolean>>({});

onMounted(async () => {
  try {
    const res = await axios.get('/products');
    products.value = res.data;
    products.value.forEach(p => quantities.value[p.id] = 1);
  } catch (err: any) {
    error.value = err.message;
  } finally {
    loading.value = false;
  }
});

const addToCart = async (productId: number) => {
  if (adding.value[productId]) return; // 防止重複點擊

  const quantity = quantities.value[productId];
  adding.value[productId] = true;

  try {
    // 1. 取得冪等 Token
    const tokenRes = await axios.get('/cart/idempotent-token');
    const idempotentKey = tokenRes.data.token;

    // 2. 送出請求
    await axios.post('/cart/items', {
      productId,
      quantity,
      idempotentKey
    });
    alert('加入購物車成功！');
  } catch (err: any) {
    alert('加入購物車失敗：' + (err.response?.data || err.message));
  } finally {
    adding.value[productId] = false;
  }
};

const logout = () => {
  localStorage.removeItem('token');
  router.push('/login');
};
</script>

<style scoped>
.products-container {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
}
.header-actions {
  display: flex;
  justify-content: space-between;
  margin-bottom: 20px;
}
.cart-link {
  text-decoration: none;
  font-size: 1.2rem;
  padding: 5px 10px;
  background: #f0f0f0;
  border-radius: 5px;
}
.logout-btn {
  padding: 5px 10px;
}
.product-actions {
  margin-top: 10px;
}
.product-actions input {
  width: 60px;
  margin-right: 10px;
}
li {
  margin-bottom: 20px;
  border-bottom: 1px solid #ddd;
  padding-bottom: 10px;
  list-style: none;
}
</style>
<template>
  <div class="products-container">
    <h1>🛒 瞬購秒殺商城 - 商品列表</h1>
    <button @click="logout" class="logout-btn">登出</button>
    <div v-if="loading">載入中...</div>
    <div v-else-if="error">發生錯誤：{{ error }}</div>
    <ul v-else>
      <li v-for="product in products" :key="product.id">
        <strong>{{ product.name }}</strong> - ${{ product.price }}
        <p>{{ product.description }}</p>
        <small>庫存：{{ product.stock }}</small>
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
const loading = ref(true);
const error = ref<string | null>(null);

onMounted(async () => {
  try {
    const res = await axios.get('/products');
    products.value = res.data;
  } catch (err: any) {
    error.value = err.message;
  } finally {
    loading.value = false;
  }
});

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
.logout-btn {
  float: right;
  margin-bottom: 20px;
}
li {
  margin-bottom: 20px;
  border-bottom: 1px solid #ddd;
  padding-bottom: 10px;
  list-style: none;
}
</style>
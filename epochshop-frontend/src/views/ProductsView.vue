<template>
  <div class="products-container">
    <h1>🛒 瞬購秒殺商城 - 商品列表</h1>
    <div class="header-actions">
      <router-link to="/cart" class="cart-link">🛒 購物車</router-link>
      <router-link to="/orders" class="orders-link">📋 我的訂單</router-link>
      <router-link v-if="isAdmin" to="/admin" class="admin-link">⚙️ 管理</router-link>
      <button @click="logout" class="logout-btn">登出</button>
    </div>

    <!-- 搜尋框 -->
    <div class="search-bar">
      <input v-model="searchKeyword" placeholder="搜尋商品..." @input="debouncedSearch" />
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

    <!-- 分頁元件 -->
    <div class="pagination" v-if="totalPages > 1">
      <button @click="changePage(currentPage - 1)" :disabled="currentPage === 0">上一頁</button>
      <span>第 {{ currentPage + 1 }} / {{ totalPages }} 頁</span>
      <button @click="changePage(currentPage + 1)" :disabled="currentPage >= totalPages - 1">下一頁</button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch, computed } from 'vue';
import axios from '../utils/axios';
import { useRouter } from 'vue-router';
import { debounce } from 'lodash-es';

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
const adding = ref<Record<number, boolean>>({});
const loading = ref(true);
const error = ref<string | null>(null);

const searchKeyword = ref('');
const currentPage = ref(0);
const pageSize = 5;
const totalPages = ref(0);

const isAdmin = computed(() => localStorage.getItem('role') === 'ROLE_ADMIN');

const fetchProducts = async () => {
  loading.value = true;
  try {
    const res = await axios.get('/products', {
      params: {
        keyword: searchKeyword.value || undefined,
        page: currentPage.value,
        size: pageSize,
        sort: 'id,desc'
      }
    });
    products.value = res.data.content;
    totalPages.value = res.data.totalPages;
    products.value.forEach(p => {
      if (!quantities.value[p.id]) quantities.value[p.id] = 1;
      if (!adding.value[p.id]) adding.value[p.id] = false;
    });
  } catch (err: any) {
    error.value = err.message;
  } finally {
    loading.value = false;
  }
};

const debouncedSearch = debounce(() => {
  currentPage.value = 0;
  fetchProducts();
}, 500);

const changePage = (page: number) => {
  currentPage.value = page;
  fetchProducts();
};

const addToCart = async (productId: number) => {
  if (adding.value[productId]) return;
  const quantity = quantities.value[productId];
  adding.value[productId] = true;
  try {
    const tokenRes = await axios.get('/cart/idempotent-token');
    const idempotentKey = tokenRes.data.token;
    await axios.post('/cart/items', { productId, quantity, idempotentKey });
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

onMounted(fetchProducts);
watch(currentPage, fetchProducts);
</script>

<style scoped>
.products-container { max-width: 800px; margin: 0 auto; padding: 20px; }
.header-actions { display: flex; justify-content: flex-end; gap: 15px; margin-bottom: 20px; }
.search-bar { margin-bottom: 20px; }
.search-bar input { width: 100%; padding: 8px; font-size: 1rem; }
li { margin-bottom: 20px; border-bottom: 1px solid #ddd; padding-bottom: 10px; list-style: none; }
.product-actions { margin-top: 10px; }
.product-actions input { width: 60px; margin-right: 10px; }
.pagination { display: flex; justify-content: center; gap: 20px; margin-top: 30px; }
.pagination button { padding: 5px 15px; }
</style>
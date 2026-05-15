<template>
  <div class="marketplace">
    <!-- 顶部导航 -->
    <header class="top-bar">
      <div class="logo">🛒 EpochShop</div>
      <div class="search-bar">
        <input v-model="searchKeyword" placeholder="搜尋商品..." @input="debouncedSearch" />
      </div>
      <button class="menu-toggle" @click="mobileMenuOpen = !mobileMenuOpen">☰</button>
      <nav class="nav-actions" :class="{ 'open': mobileMenuOpen }">
        <!-- 原有导航链接 -->
        <template v-if="!isAdmin">
          <router-link to="/cart" class="nav-link" @click="mobileMenuOpen = false">🛒 購物車</router-link>
          <router-link to="/orders" class="nav-link" @click="mobileMenuOpen = false">📋 訂單</router-link>
        </template>
        <template v-else>
          <router-link to="/admin" class="nav-link" @click="mobileMenuOpen = false">⚙️ 管理</router-link>
          <router-link to="/admin/sales" class="nav-link" @click="mobileMenuOpen = false">📊 銷售</router-link>
        </template>
        <router-link to="/messages" class="nav-link" @click="mobileMenuOpen = false">
          💬 訊息
          <span v-if="unreadCount > 0" class="unread-badge">{{ unreadCount > 99 ? '99+' : unreadCount }}</span>
        </router-link>
          <button @click="logout" class="btn btn-outline">登出</button>
        </nav>
      </header>

    <!-- 商品網格 -->
    <div class="product-grid" v-if="!loading && !error">
      <div v-for="product in products" :key="product.id" class="product-card card">
        <router-link :to="'/products/' + product.id" class="card-img-link">
          <img v-if="product.imageUrl" :src="product.imageUrl" :alt="product.name" />
          <div v-else class="no-image">暫無圖片</div>
        </router-link>
        <div class="card-body">
          <router-link :to="'/products/' + product.id" class="product-name">
            {{ product.name }}
          </router-link>
          <div class="product-price">${{ product.price?.toLocaleString() }}</div>
          <div class="product-meta">
            <span class="sales-count">已售 1.2k</span>
            <span class="location">高雄市</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 加载与错误状态 -->
    <div v-if="loading" class="loading">載入中...</div>
    <div v-if="error" class="error">發生錯誤：{{ error }}</div>

    <!-- 分頁 -->
    <div class="pagination" v-if="totalPages > 1">
      <button @click="changePage(currentPage - 1)" :disabled="currentPage === 0" class="btn">上一頁</button>
      <span>第 {{ currentPage + 1 }} / {{ totalPages }} 頁</span>
      <button @click="changePage(currentPage + 1)" :disabled="currentPage >= totalPages - 1" class="btn">下一頁</button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch, computed, onBeforeMount, onBeforeUnmount } from 'vue';
import axios from '../utils/axios';
import { useRouter } from 'vue-router';
import { debounce } from 'lodash-es';

interface Product {
  id: number;
  name: string;
  description: string;
  price: number;
  stock: number;
  imageUrl?: string;
  seller: { id: number; username?: string; displayName?: string };
}

const router = useRouter();
const products = ref<Product[]>([]);
const loading = ref(true);
const error = ref<string | null>(null);

const searchKeyword = ref('');
const currentPage = ref(0);
const pageSize = 12;
const totalPages = ref(0);

const mobileMenuOpen = ref(false);
const unreadCount = ref(0);

const isAdmin = computed(() => localStorage.getItem('role') === 'ROLE_ADMIN');

const fetchProducts = async () => {
  loading.value = true;
  error.value = null;
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
  } catch (err: any) {
    error.value = err.message;
  } finally {
    loading.value = false;
  }
};

const debouncedSearch = debounce(() => {
  currentPage.value = 0;
  fetchProducts();
}, 300);

const changePage = (page: number) => {
  currentPage.value = page;
  fetchProducts();
};

const logout = () => {
  localStorage.removeItem('token');
  localStorage.removeItem('role');
  router.push('/login');
};

const fetchUnreadCount = async () => {
  try {
    const res = await axios.get('/messages/unread-count');
    unreadCount.value = res.data.count;
  } catch (err) {
    console.error('获取未读消息数失败', err);
  }
};

let interval: number;
// 在 ProductsView.vue 的 <script setup> 中
onMounted(() => {
  fetchProducts();
  fetchUnreadCount();
  interval = window.setInterval(fetchUnreadCount, 30000);
});

onBeforeUnmount(() => {
  clearInterval(interval);
});

watch(currentPage, fetchProducts, { immediate: true });
</script>

<style scoped>
.marketplace { max-width: 1200px; margin: 0 auto; padding: 0 16px; }

/* 顶部导航 */
.top-bar {
  display: flex;
  align-items: center;
  gap: 20px;
  padding: 12px 0;
  background: var(--bg-white, #fff);
  position: sticky;
  top: 0;
  z-index: 100;
  box-shadow: 0 2px 8px rgba(0,0,0,0.08);
}
.logo { font-size: 1.5rem; font-weight: 700; color: var(--primary-color, #ee4d2d); }
.search-bar { flex: 1; max-width: 500px; }
.search-bar input {
  width: 100%;
  padding: 10px 16px;
  border: 1px solid #ddd;
  border-radius: 24px;
  font-size: 0.95rem;
}
.nav-actions { display: flex; gap: 8px; align-items: center; }
.nav-link { text-decoration: none; padding: 8px 16px; border-radius: 8px; color: #333; font-size: 0.95rem; transition: background 0.2s; }
.nav-link:hover { background: #f0f0f0; }
.btn-outline { background: white; border: 2px solid var(--primary-color, #ee4d2d); color: var(--primary-color, #ee4d2d); padding: 8px 16px; border-radius: 8px; cursor: pointer; font-weight: 500; transition: all 0.2s; }
.btn-outline:hover { background: #fff0ed; }

/* 商品網格 */
.product-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 16px;
  margin: 20px 0;
}

/* 商品卡片 */
.product-card {
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.08);
  overflow: hidden;
  transition: transform 0.2s, box-shadow 0.2s;
}
.product-card:hover { transform: translateY(-2px); box-shadow: 0 4px 16px rgba(0,0,0,0.16); }
.card-img-link { display: block; height: 200px; overflow: hidden; }
.card-img-link img { width: 100%; height: 100%; object-fit: cover; transition: transform 0.2s; }
.product-card:hover .card-img-link img { transform: scale(1.05); }
.card-body { padding: 12px; }
.product-name {
  color: var(--text-color, #222);
  text-decoration: none;
  font-size: 0.9rem;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  line-clamp: 2;  /* 添加标准属性，消除警告 */
  overflow: hidden;
  margin-bottom: 8px;
}
.product-price {
  font-size: 1.2rem;
  font-weight: 700;
  color: var(--primary-color, #ee4d2d);
  margin-bottom: 6px;
}
.product-meta {
  display: flex;
  justify-content: space-between;
  font-size: 0.8rem;
  color: #999;
}
.no-image {
  width: 100%;
  height: 200px;
  background: #f0f0f0;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #999;
}
.loading, .error { text-align: center; padding: 40px; color: #999; }
.pagination { display: flex; justify-content: center; gap: 16px; margin: 30px 0; }
.btn { padding: 10px 20px; border: none; border-radius: 8px; cursor: pointer; font-size: 0.95rem; font-weight: 500; background: #f0f0f0; color: #333; transition: all 0.2s; }
.btn:disabled { opacity: 0.5; cursor: not-allowed; }
.btn:hover:not(:disabled) { background: #e0e0e0; }

.menu-toggle {
  display: none;
  background: none;
  border: none;
  font-size: 1.8rem;
  cursor: pointer;
  padding: 0 12px;
}

@media (max-width: 768px) {
  .menu-toggle {
    display: block;
  }
  .nav-actions {
    position: fixed;
    top: 60px;
    left: -100%;
    width: 70%;
    height: calc(100vh - 60px);
    background: white;
    flex-direction: column;
    align-items: flex-start;
    padding: 20px;
    gap: 16px;
    box-shadow: 2px 0 8px rgba(0,0,0,0.1);
    transition: left 0.3s ease;
    z-index: 1000;
  }
  .nav-actions.open {
    left: 0;
  }
  .nav-link, .btn-outline {
    width: 100%;
    text-align: left;
  }
}

.product-grid {
  display: grid;
  gap: 12px;
  grid-template-columns: repeat(2, 1fr);  /* 手机默认2列 */
}
@media (min-width: 560px) {
  .product-grid {
    grid-template-columns: repeat(3, 1fr);
  }
}
@media (min-width: 768px) {
  .product-grid {
    grid-template-columns: repeat(4, 1fr);
  }
}
@media (min-width: 1024px) {
  .product-grid {
    grid-template-columns: repeat(5, 1fr);
  }
}

.nav-link {
  position: relative;
}
.unread-badge {
  position: absolute;
  top: -8px;
  right: -12px;
  background: #f44336;
  color: white;
  border-radius: 50%;
  min-width: 18px;
  height: 18px;
  font-size: 0.65rem;
  font-weight: bold;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 0 4px;
  box-shadow: 0 1px 2px rgba(0,0,0,0.2);
}
</style>
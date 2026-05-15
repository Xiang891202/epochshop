<template>
  <div class="cart-container">
    <h1>🛒 我的購物車</h1>
    <router-link to="/products" class="back-link">← 繼續購物</router-link>
    <div v-if="loading">載入中...</div>
    <div v-else-if="error">發生錯誤：{{ error }}</div>
    <div v-else>
      <div v-if="cart.items.length === 0">
        <p>購物車是空的，快去選購商品吧！</p>
      </div>

      <!-- 桌面版：表格 -->
      <div class="cart-desktop">
        <table v-if="cart.items.length > 0">
          <thead>
            <tr>
              <th>商品名稱</th>
              <th>單價</th>
              <th>數量</th>
              <th>小計</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="item in cart.items" :key="item.itemId">
              <td>{{ item.productName }}</td>
              <td>${{ item.unitPrice }}</td>
              <td>
                <input type="number" v-model="item.quantity" min="1" @change="updateItem(item)" />
               </td>
              <td>${{ item.subtotal }}</td>
              <td>
                <button @click="removeItem(item.itemId)">刪除</button>
               </td>
            </tr>
          </tbody>
          <tfoot>
            <tr>
              <td colspan="3" align="right"><strong>總計</strong></td>
              <td><strong>${{ cart.totalAmount }}</strong></td>
              <td></td>
            </tr>
          </tfoot>
        </table>
      </div>

      <!-- 行動版：卡片列表 -->
      <div class="cart-mobile">
        <div v-for="item in cart.items" :key="item.itemId" class="cart-card">
          <div class="cart-card-header">{{ item.productName }}</div>
          <div class="cart-card-body">
            <div class="cart-card-row">
              <span class="label">單價</span>
              <span class="value">${{ item.unitPrice }}</span>
            </div>
            <div class="cart-card-row">
              <span class="label">數量</span>
              <input type="number" v-model="item.quantity" min="1" @change="updateItem(item)" />
            </div>
            <div class="cart-card-row">
              <span class="label">小計</span>
              <span class="value">${{ item.subtotal }}</span>
            </div>
            <button class="btn-remove" @click="removeItem(item.itemId)">刪除</button>
          </div>
        </div>
        <div v-if="cart.items.length > 0" class="mobile-total">
          <div class="total-label">總計</div>
          <div class="total-amount">${{ cart.totalAmount }}</div>
        </div>
      </div>

      <div class="checkout-section" v-if="cart.items.length > 0">
        <button class="checkout-btn" @click="checkout">
          {{ checkingOut ? '處理中...' : '結帳' }}
        </button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, inject } from 'vue';
import axios from '../utils/axios';
import router from '@/router';

const toast = inject('toast') as (text: string, type?: string) => void;

interface CartItem {
  itemId: number;
  productId: number;
  productName: string;
  unitPrice: number;
  quantity: number;
  subtotal: number;
}

interface Cart {
  cartId: number;
  items: CartItem[];
  totalAmount: number;
}

const cart = ref<Cart>({ cartId: 0, items: [], totalAmount: 0 });
const loading = ref(true);
const error = ref<string | null>(null);
const checkingOut = ref(false);

const fetchCart = async () => {
  try {
    const res = await axios.get('/cart');
    cart.value = res.data;
  } catch (err: any) {
    error.value = err.message;
  } finally {
    loading.value = false;
  }
};

const updateItem = async (item: CartItem) => {
  try {
    const res = await axios.put(`/cart/items/${item.itemId}?quantity=${item.quantity}`);
    cart.value = res.data;
  } catch (err: any) {
    toast('更新失敗：' + err.message, 'error');
    fetchCart(); // 復原
  }
};

const removeItem = async (itemId: number) => {
  if (!confirm('確定要移除這個商品嗎？')) return;
  try {
    const res = await axios.delete(`/cart/items/${itemId}`);
    cart.value = res.data;
  } catch (err: any) {
    toast('刪除失敗：' + err.message, 'error');
  }
};

const checkout = async () => {
  if (checkingOut.value) return;
  checkingOut.value = true;
  try {
    const tokenRes = await axios.get('/orders/idempotent-token');
    const idempotentKey = tokenRes.data.token;
    const res = await axios.post('/orders', { idempotentKey });
    toast('訂單建立成功！訂單編號：' + res.data.orderId, 'success');
    router.push('/orders');
  } catch (err: any) {
    const message = err.response?.data || err.message || '結帳失敗';
    toast('結帳失敗：' + message, 'error');
  } finally {
    checkingOut.value = false;
  }
};

onMounted(fetchCart);
</script>

<style scoped>
.cart-container {
  max-width: 1000px;
  margin: 0 auto;
  padding: 20px;
}

/* 桌面版表格样式 */
.cart-desktop {
  display: block;
}
.cart-mobile {
  display: none;
}
table {
  width: 100%;
  border-collapse: collapse;
}
th, td {
  border: 1px solid #ddd;
  padding: 10px;
  text-align: left;
}
th {
  background-color: #f2f2f2;
}
tfoot td {
  font-weight: bold;
}
input[type="number"] {
  width: 70px;
  padding: 6px;
}

/* 行動版卡片样式 */
@media (max-width: 768px) {
  .cart-desktop {
    display: none;
  }
  .cart-mobile {
    display: block;
  }

  .cart-card {
    background: white;
    border-radius: 12px;
    box-shadow: 0 2px 8px rgba(0,0,0,0.06);
    margin-bottom: 16px;
    overflow: hidden;
    transition: box-shadow 0.2s;
  }
  .cart-card:hover {
    box-shadow: 0 4px 12px rgba(0,0,0,0.1);
  }
  .cart-card-header {
    background: #f9f9f9;
    padding: 12px 16px;
    font-weight: 600;
    font-size: 1rem;
    border-bottom: 1px solid #eee;
  }
  .cart-card-body {
    padding: 12px 16px;
  }
  .cart-card-row {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 12px;
  }
  .cart-card-row .label {
    color: #666;
    font-size: 0.9rem;
  }
  .cart-card-row .value {
    font-weight: 500;
  }
  .cart-card-row input {
    width: 70px;
    padding: 8px;
    border: 1px solid #ddd;
    border-radius: 6px;
    text-align: center;
  }
  .btn-remove {
    background: #f44336;
    color: white;
    border: none;
    border-radius: 6px;
    padding: 8px 12px;
    width: 100%;
    margin-top: 8px;
    font-size: 0.9rem;
    cursor: pointer;
    transition: background 0.2s;
  }
  .btn-remove:hover {
    background: #d32f2f;
  }
  .mobile-total {
    background: #f9f9f9;
    padding: 16px;
    border-radius: 12px;
    display: flex;
    justify-content: space-between;
    font-size: 1.1rem;
    font-weight: bold;
    margin-top: 8px;
    margin-bottom: 20px;
  }
  .total-amount {
    color: #f44336;
  }
}

/* 结账区域 */
.checkout-section {
  margin-top: 20px;
  text-align: right;
}
.checkout-btn {
  padding: 10px 30px;
  font-size: 1.2rem;
  background-color: #4CAF50;
  color: white;
  border: none;
  border-radius: 5px;
  cursor: pointer;
  transition: background 0.2s;
}
.checkout-btn:hover {
  background-color: #45a049;
}

/* 小屏幕时结账按钮居中 */
@media (max-width: 768px) {
  .checkout-section {
    text-align: center;
  }
  .checkout-btn {
    width: 100%;
    padding: 14px;
    font-size: 1rem;
  }
}
</style>
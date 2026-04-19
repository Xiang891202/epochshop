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
      <table v-else>
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
      <div class="checkout-section" v-if="cart.items.length > 0">
        <button class="checkout-btn" @click="checkout">結帳</button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import axios from '../utils/axios';
import router from '@/router';

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
    alert('更新失敗：' + err.message);
    fetchCart(); // 復原
  }
};

const removeItem = async (itemId: number) => {
  if (!confirm('確定要移除這個商品嗎？')) return;
  try {
    const res = await axios.delete(`/cart/items/${itemId}`);
    cart.value = res.data;
  } catch (err: any) {
    alert('刪除失敗：' + err.message);
  }
};

const checkout = async () => {
  try {
    const res = await axios.post('/orders');
    alert('訂單建立成功！訂單編號：' + res.data.orderId);
    router.push('/orders');
  } catch (err: any) {
    // 優先取用後端回傳的訊息，若無則顯示一般錯誤
    const message = err.response?.data || err.message || '結帳失敗';
    alert('結帳失敗：' + message);
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
.back-link {
  display: inline-block;
  margin-bottom: 20px;
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
  width: 60px;
}

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
}
</style>
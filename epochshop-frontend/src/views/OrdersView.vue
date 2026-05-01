<template>
  <div class="orders-container">
    <h1>📦 我的訂單</h1>
    <router-link to="/products" class="back-link">← 繼續購物</router-link>
    <div v-if="loading">載入中...</div>
    <div v-else-if="error">發生錯誤：{{ error }}</div>
    <div v-else>
      <div v-if="orders.length === 0">
        <p>尚無訂單記錄</p>
      </div>
      <div v-for="order in orders" :key="order.orderId" class="order-card">
        <div class="order-header">
          <span>訂單編號：{{ order.orderId }}</span>
          <span>狀態：{{ order.status }}</span>
          <span>總金額：${{ order.totalAmount }}</span>
          <span>{{ new Date(order.createdAt).toLocaleString() }}</span>
        </div>
        <ul>
          <li v-for="item in order.items" :key="item.productId">
            {{ item.productName }} x {{ item.quantity }} = ${{ item.subtotal }}
          </li>
        </ul>
        <button v-if="order.status === 'PENDING'" @click="payOrder(order.orderId)" class="pay-btn">
          模擬付款
        </button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, inject } from 'vue';
import axios from '../utils/axios';

const toast = inject('toast') as (text: string, type?: string) => void;

interface OrderItem {
  productId: number;
  productName: string;
  quantity: number;
  unitPrice: number;
  subtotal: number;
}

interface Order {
  orderId: number;
  createdAt: string;
  status: string;
  totalAmount: number;
  items: OrderItem[];
}

const orders = ref<Order[]>([]);
const loading = ref(true);
const error = ref<string | null>(null);

const fetchOrders = async () => {
  try {
    const res = await axios.get('/orders');
    orders.value = res.data;
  } catch (err: any) {
    error.value = err.message;
  } finally {
    loading.value = false;
  }
};

const payOrder = async (orderId: number) => {
  try {
    await axios.put(`/orders/${orderId}/pay`);
    toast('付款成功！');
    fetchOrders();  // 刷新
  } catch (err: any) {
    toast('付款失敗：' + err.message, 'error');
  }
};

onMounted(fetchOrders);
</script>

<style scoped>
.orders-container {
  max-width: 900px;
  margin: 0 auto;
  padding: 20px;
}
.order-card {
  border: 1px solid #ddd;
  border-radius: 8px;
  padding: 15px;
  margin-bottom: 20px;
}
.order-header {
  display: flex;
  justify-content: space-between;
  background: #f5f5f5;
  padding: 10px;
  margin-bottom: 10px;
}
.pay-btn {
  margin-top: 10px;
  padding: 5px 15px;
  background-color: #2196F3;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

@media (max-width: 768px) {
  .order-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 5px;
  }
  .orders-container {
    padding: 10px;
  }
}
</style>
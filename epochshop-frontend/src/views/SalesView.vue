<template>
  <div class="sales-container">
    <h1>💰 銷售額儀表板</h1>

    <div v-if="loading">載入中...</div>
    <div v-else-if="error" class="error">發生錯誤：{{ error }}</div>
    <div v-else>
      <!-- 數據卡片 -->
      <div class="stats-cards">
        <div class="card">
          <div class="card-title">總銷售額</div>
          <div class="card-value">${{ formatNumber(data.totalSales) }}</div>
        </div>
        <div class="card">
          <div class="card-title">已付款訂單</div>
          <div class="card-value">{{ data.paidOrders }} 筆</div>
        </div>
      </div>

      <!-- 商品銷售排行 -->
      <div class="product-ranking">
        <h3>🏆 商品銷售排行</h3>
        <table v-if="data.productSales.length > 0">
          <thead>
            <tr>
              <th>排名</th>
              <th>商品名稱</th>
              <th>銷售數量</th>
              <th>銷售金額</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="(item, index) in data.productSales" :key="index">
              <td>
                <span class="rank-badge" :class="'rank-' + (index + 1)">
                  {{ index + 1 }}
                </span>
              </td>
              <td>{{ item[0] }}</td>
              <td>{{ item[1] }}</td>
              <td>${{ formatNumber(item[2]) }}</td>
            </tr>
          </tbody>
        </table>
        <p v-else class="no-data">尚無銷售資料</p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import axios from '../utils/axios';

interface SalesData {
  totalSales: number;
  paidOrders: number;
  productSales: any[][];
}

const data = ref<SalesData>({ totalSales: 0, paidOrders: 0, productSales: [] });
const loading = ref(true);
const error = ref<string | null>(null);

const formatNumber = (num: number) => {
  if (num === null || num === undefined) return '0';
  return num.toLocaleString('en-US', { minimumFractionDigits: 0, maximumFractionDigits: 0 });
};

onMounted(async () => {
  try {
    const res = await axios.get('/admin/sales');
    data.value = res.data;
  } catch (err: any) {
    error.value = err.response?.data || err.message;
  } finally {
    loading.value = false;
  }
});
</script>

<style scoped>
.sales-container {
  max-width: 1000px;
  margin: 0 auto;
  padding: 20px;
}

.stats-cards {
  display: flex;
  gap: 20px;
  margin-bottom: 30px;
}

.card {
  flex: 1;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border-radius: 12px;
  padding: 25px;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
  text-align: center;
}

.card-title {
  font-size: 1rem;
  opacity: 0.9;
  margin-bottom: 10px;
}

.card-value {
  font-size: 2rem;
  font-weight: bold;
}

.product-ranking {
  background: white;
  border-radius: 12px;
  padding: 25px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
}

table {
  width: 100%;
  border-collapse: collapse;
  margin-top: 15px;
}

th, td {
  padding: 12px 15px;
  text-align: left;
  border-bottom: 1px solid #eee;
}

th {
  background: #f8f9fa;
  font-weight: 600;
  color: #333;
}

.rank-badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 30px;
  height: 30px;
  border-radius: 50%;
  font-weight: bold;
  font-size: 0.9rem;
  color: white;
}

.rank-1 { background: #FFD700; color: #333; }
.rank-2 { background: #C0C0C0; color: #333; }
.rank-3 { background: #CD7F32; color: white; }
.rank-badge:not(.rank-1):not(.rank-2):not(.rank-3) { background: #95a5a6; }

.no-data {
  text-align: center;
  color: #999;
  padding: 20px;
}
</style>
<template>
  <div class="admin-container">
    <h1>📋 商品管理（管理員）</h1>
    <div class="admin-actions">
      <button @click="showForm = !showForm" class="btn-primary">
        {{ showForm ? '取消' : '新增商品' }}
      </button>
    </div>

    <!-- 商品表單（响应式） -->
    <div v-if="showForm" class="product-form">
      <h3>{{ editingProduct ? '編輯商品' : '新增商品' }}</h3>
      <form @submit.prevent="handleSubmit">
        <div class="form-group">
          <label>商品名稱</label>
          <input v-model="form.name" required />
        </div>
        <div class="form-group">
          <label>商品描述</label>
          <textarea v-model="form.description" rows="3"></textarea>
        </div>
        <div class="form-row">
          <div class="form-group half">
            <label>價格</label>
            <input v-model.number="form.price" type="number" min="0" step="0.01" required />
          </div>
          <div class="form-group half">
            <label>庫存數量</label>
            <input v-model.number="form.stock" type="number" min="0" required />
          </div>
        </div>

        <!-- 圖片上傳區塊 -->
        <div class="form-group">
          <label>商品圖片</label>
          <div class="image-upload">
            <input type="file" accept="image/*" @change="handleFileUpload" ref="fileInput" />
            <div v-if="imagePreview" class="preview-container">
              <img :src="imagePreview" class="image-preview" />
              <button type="button" @click="removeImage" class="btn-remove">移除</button>
            </div>
            <p v-if="uploading">上傳中...</p>
          </div>
        </div>

        <div class="form-buttons">
          <button type="submit" class="btn-primary">
            {{ editingProduct ? '更新' : '新增' }}
          </button>
          <button type="button" @click="resetForm" class="btn-secondary">取消</button>
        </div>
      </form>
    </div>

    <!-- 商品列表：桌面版表格 -->
    <div v-if="!showForm" class="product-desktop">
      <table v-if="products.length > 0">
        <thead>
          <tr>
            <th>商品</th>
            <th>價格</th>
            <th>庫存</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="product in products" :key="product.id">
            <td>
              <div class="admin-product-cell">
                <img v-if="product.imageUrl" :src="product.imageUrl" class="admin-thumb" :alt="product.name" />
                <div v-else class="admin-no-image">無圖</div>
                <span>{{ product.name }}</span>
              </div>
            </td>
            <td>${{ product.price }}</td>
            <td>{{ product.stock }}</td>
            <td>
              <button @click="editProduct(product)" class="btn-edit">編輯</button>
              <button v-if="product.active" @click="deactivateProduct(product.id)" class="btn-delete">下架</button>
              <button v-else @click="reactivateProduct(product.id)" class="btn-restore">恢復上架</button>
            </td>
          </tr>
        </tbody>
      </table>
      <p v-else>尚無商品資料</p>
    </div>

    <!-- 商品列表：移动版卡片 -->
    <div v-if="!showForm" class="product-mobile">
      <div v-if="products.length === 0">
        <p>尚無商品資料</p>
      </div>
      <div v-for="product in products" :key="product.id" class="product-card">
        <div class="product-card-header">
          <div class="product-card-image">
            <img v-if="product.imageUrl" :src="product.imageUrl" :alt="product.name" />
            <div v-else class="no-img">無圖</div>
          </div>
          <div class="product-card-info">
            <div class="product-name">{{ product.name }}</div>
            <div class="product-price">${{ product.price }}</div>
            <div class="product-stock">庫存：{{ product.stock }}</div>
          </div>
        </div>
        <div class="product-card-actions">
          <button @click="editProduct(product)" class="btn-edit">編輯</button>
          <button v-if="product.active" @click="deactivateProduct(product.id)" class="btn-delete">下架</button>
          <button v-else @click="reactivateProduct(product.id)" class="btn-restore">恢復上架</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, inject } from 'vue';
import axios from '../utils/axios';

const toast = inject('toast') as (text: string, type?: string) => void;

interface Product {
  id: number;
  name: string;
  description: string;
  price: number;
  stock: number;
  active: boolean;
  imageUrl?: string;
}

const products = ref<Product[]>([]);
const showForm = ref(false);
const editingProduct = ref<Product | null>(null);
const fileInput = ref<HTMLInputElement | null>(null);
const imagePreview = ref<string>('');
const uploading = ref(false);

const form = ref({
  name: '',
  description: '',
  price: 0,
  stock: 0,
  imageUrl: '',
});

const fetchProducts = async () => {
  try {
    const res = await axios.get('/products/admin', { params: { size: 100 } });
    products.value = res.data.content;
  } catch (err: any) {
    toast('取得商品失敗' + err.message, 'error');
  }
};

const resetForm = () => {
  form.value = { name: '', description: '', price: 0, stock: 0, imageUrl: '' };
  editingProduct.value = null;
  showForm.value = false;
  imagePreview.value = '';
  if (fileInput.value) fileInput.value.value = '';
};

const editProduct = (product: Product) => {
  editingProduct.value = product;
  form.value = { 
    name: product.name,
    description: product.description,
    price: product.price,
    stock: product.stock,
    imageUrl: product.imageUrl || '',
  };
  imagePreview.value = product.imageUrl || '';
  showForm.value = true;
};

const handleFileUpload = async (event: Event) => {
  const input = event.target as HTMLInputElement;
  if (!input.files || input.files.length === 0) return;
  const file = input.files[0];
  if (!file) return;

  const formData = new FormData();
  formData.append('file', file);

  uploading.value = true;
  try {
    const res = await axios.post('/upload/image', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    });
    form.value.imageUrl = res.data.imageUrl;
    imagePreview.value = res.data.imageUrl;
  } catch (err: any) {
    toast('圖片上傳失敗：' + (err.response?.data?.error || err.message), 'error');
  } finally {
    uploading.value = false;
  }
};

const removeImage = () => {
  form.value.imageUrl = '';
  imagePreview.value = '';
  if (fileInput.value) fileInput.value.value = '';
};

const handleSubmit = async () => {
  try {
    if (editingProduct.value) {
      await axios.put(`/products/${editingProduct.value.id}`, form.value);
    } else {
      await axios.post('/products', form.value);
    }
    toast('操作成功', 'success');
    resetForm();
    fetchProducts();
  } catch (err: any) {
    toast('操作失敗：' + (err.response?.data || err.message), 'error');
  }
};

const deactivateProduct = async (id: number) => {
  if (!confirm('確定要下架此商品嗎？')) return;
  try {
    await axios.put(`/products/${id}/deactivate`);
    toast('下架成功', 'success');
    fetchProducts();
  } catch (err: any) {
    toast('操作失敗：' + (err.response?.data || err.message), 'error');
  }
};

const reactivateProduct = async (id: number) => {
  try {
    await axios.put(`/products/${id}/reactivate`);
    toast('恢復成功', 'success');
    fetchProducts();
  } catch (err: any) {
    toast('操作失敗：' + (err.response?.data || err.message), 'error');
  }
};

onMounted(fetchProducts);
</script>

<style scoped>
.admin-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.admin-actions {
  margin-bottom: 20px;
  text-align: right;
}

/* 表單樣式 */
.product-form {
  background: #f9f9f9;
  padding: 20px;
  border-radius: 12px;
  margin-bottom: 30px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.05);
}
.form-group {
  margin-bottom: 16px;
}
.form-group label {
  display: block;
  margin-bottom: 6px;
  font-weight: 500;
  color: #333;
}
.form-group input,
.form-group textarea {
  width: 100%;
  padding: 10px 12px;
  border: 1px solid #ddd;
  border-radius: 8px;
  font-size: 0.95rem;
  transition: border-color 0.2s;
}
.form-group input:focus,
.form-group textarea:focus {
  border-color: #4CAF50;
  outline: none;
  box-shadow: 0 0 0 3px rgba(76,175,80,0.1);
}
.form-row {
  display: flex;
  gap: 16px;
}
.form-group.half {
  flex: 1;
}
.form-buttons {
  display: flex;
  gap: 12px;
  margin-top: 20px;
  justify-content: flex-end;
}
.image-upload {
  margin-top: 5px;
}
.preview-container {
  margin-top: 12px;
  display: flex;
  align-items: center;
  gap: 12px;
}
.image-preview {
  width: 80px;
  height: 80px;
  object-fit: cover;
  border-radius: 8px;
  border: 1px solid #ddd;
}
.btn-remove {
  background: #f44336;
  color: white;
  border: none;
  padding: 6px 12px;
  border-radius: 20px;
  cursor: pointer;
}

/* 按鈕共用 */
.btn-primary, .btn-secondary, .btn-edit, .btn-delete, .btn-restore {
  border: none;
  padding: 8px 16px;
  border-radius: 6px;
  cursor: pointer;
  font-size: 0.9rem;
  transition: all 0.2s;
}
.btn-primary { background: #4CAF50; color: white; }
.btn-primary:hover { background: #45a049; }
.btn-secondary { background: #ccc; color: #333; }
.btn-secondary:hover { background: #bbb; }
.btn-edit { background: #2196F3; color: white; margin-right: 5px; }
.btn-edit:hover { background: #0b7dda; }
.btn-delete { background: #f44336; color: white; }
.btn-delete:hover { background: #d32f2f; }
.btn-restore { background: #FF9800; color: white; }
.btn-restore:hover { background: #fb8c00; }

/* 桌面版表格 */
.product-desktop {
  display: block;
}
table {
  width: 100%;
  border-collapse: collapse;
  background: white;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 1px 3px rgba(0,0,0,0.05);
}
th, td {
  border-bottom: 1px solid #eee;
  padding: 12px 15px;
  text-align: left;
}
th {
  background: #f5f5f5;
  font-weight: 600;
}
.admin-product-cell {
  display: flex;
  align-items: center;
  gap: 12px;
}
.admin-thumb {
  width: 48px;
  height: 48px;
  object-fit: cover;
  border-radius: 6px;
}
.admin-no-image {
  width: 48px;
  height: 48px;
  background: #eee;
  border-radius: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 0.7rem;
  color: #999;
}

/* 行動版卡片 */
.product-mobile {
  display: none;
}
.product-card {
  background: white;
  border-radius: 12px;
  margin-bottom: 16px;
  padding: 14px;
  box-shadow: 0 2px 6px rgba(0,0,0,0.05);
  transition: box-shadow 0.2s;
}
.product-card-header {
  display: flex;
  gap: 14px;
  margin-bottom: 12px;
}
.product-card-image {
  flex-shrink: 0;
  width: 70px;
  height: 70px;
}
.product-card-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: 8px;
}
.product-card-image .no-img {
  width: 100%;
  height: 100%;
  background: #f0f0f0;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 0.7rem;
  color: #aaa;
}
.product-card-info {
  flex: 1;
}
.product-name {
  font-weight: 600;
  margin-bottom: 4px;
  font-size: 1rem;
}
.product-price {
  color: #f44336;
  font-weight: 500;
}
.product-stock {
  font-size: 0.8rem;
  color: #666;
  margin-top: 4px;
}
.product-card-actions {
  display: flex;
  gap: 8px;
  justify-content: flex-end;
  border-top: 1px solid #eee;
  padding-top: 12px;
}
.product-card-actions button {
  flex: 1;
  text-align: center;
}

/* 響應式切換 */
@media (max-width: 768px) {
  .admin-container {
    padding: 12px;
  }
  .admin-actions {
    text-align: center;
    margin-bottom: 16px;
  }
  .product-desktop {
    display: none;
  }
  .product-mobile {
    display: block;
  }
  .form-row {
    flex-direction: column;
    gap: 0;
  }
  .form-buttons {
    flex-direction: column;
  }
  .form-buttons button {
    width: 100%;
  }
}
</style>
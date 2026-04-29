<template>
  <div class="admin-container">
    <h1>📋 商品管理（管理員）</h1>
    <div class="admin-actions">
      <button @click="showForm = !showForm" class="btn-primary">
        {{ showForm ? '取消' : '新增商品' }}
      </button>
    </div>

    <!-- 商品表單 -->
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
        <div class="form-group">
          <label>價格</label>
          <input v-model.number="form.price" type="number" min="0" step="0.01" required />
        </div>
        <div class="form-group">
          <label>庫存數量</label>
          <input v-model.number="form.stock" type="number" min="0" required />
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

        <button type="submit" class="btn-primary">
          {{ editingProduct ? '更新' : '新增' }}
        </button>
        <button type="button" @click="resetForm" class="btn-secondary">取消</button>
      </form>
    </div>

    <!-- 商品列表 -->
    <div v-if="!showForm" class="product-form">
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
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import axios from '../utils/axios';

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
  } catch (err) {
    console.error('取得商品失敗', err);
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
  if (!file) return; // ✅ 明確檢查 file 不是 undefined

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
    alert('圖片上傳失敗：' + (err.response?.data?.error || err.message));
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
    alert('操作成功');
    resetForm();
    fetchProducts();
  } catch (err: any) {
    alert('操作失敗：' + (err.response?.data || err.message));
  }
};

const deactivateProduct = async (id: number) => {
  if (!confirm('確定要下架此商品嗎？')) return;
  try {
    await axios.put(`/products/${id}/deactivate`);
    alert('下架成功');
    fetchProducts();
  } catch (err: any) {
    alert('操作失敗：' + (err.response?.data || err.message));
  }
};

const reactivateProduct = async (id: number) => {
  try {
    await axios.put(`/products/${id}/reactivate`);
    alert('恢復成功');
    fetchProducts();
  } catch (err: any) {
    alert('操作失敗：' + (err.response?.data || err.message));
  }
};

onMounted(fetchProducts);
</script>

<style scoped>
.admin-container { max-width: 1000px; margin: 0 auto; padding: 20px; }
.admin-actions { margin-bottom: 20px; }
.product-form { background: #f9f9f9; padding: 20px; border-radius: 8px; margin-bottom: 30px; }
.form-group { margin-bottom: 15px; }
.form-group label { display: block; margin-bottom: 5px; }
.form-group input, .form-group textarea { width: 100%; padding: 8px; box-sizing: border-box; }
table { width: 100%; border-collapse: collapse; }
th, td { border: 1px solid #ddd; padding: 10px; text-align: left; }
th { background: #f2f2f2; }
.btn-primary { background: #4CAF50; color: white; padding: 8px 16px; border: none; border-radius: 4px; cursor: pointer; margin-right: 10px; }
.btn-secondary { background: #ccc; padding: 8px 16px; border: none; border-radius: 4px; cursor: pointer; }
.btn-edit { background: #2196F3; color: white; border: none; padding: 5px 10px; margin-right: 5px; border-radius: 4px; cursor: pointer; }
.btn-delete { background: #f44336; color: white; border: none; padding: 5px 10px; border-radius: 4px; cursor: pointer; }
.btn-restore { background: #FF9800; color: white; border: none; padding: 5px 10px; border-radius: 4px; cursor: pointer; }
.btn-remove { background: #f44336; color: white; border: none; padding: 4px 8px; border-radius: 4px; cursor: pointer; }

.admin-product-cell { display: flex; align-items: center; gap: 10px; }
.admin-thumb { width: 40px; height: 40px; object-fit: cover; border-radius: 4px; }
.admin-no-image { width: 40px; height: 40px; background: #eee; border-radius: 4px; display: flex; align-items: center; justify-content: center; font-size: 0.7rem; color: #999; }

.image-upload { margin-top: 5px; }
.preview-container { margin-top: 10px; display: flex; align-items: center; gap: 10px; }
.image-preview { width: 100px; height: 100px; object-fit: cover; border-radius: 4px; border: 1px solid #ddd; }
</style>
<template>
  <div class="login-page">
    <div class="login-card">
      <div class="brand">
        <h1>🛒 EpochShop</h1>
        <p>瞬購秒殺商城</p>
      </div>

      <div class="tab-buttons">
        <button :class="{ active: isLogin }" @click="switchMode(true)">登入</button>
        <button :class="{ active: !isLogin }" @click="switchMode(false)">註冊</button>
      </div>

      <form @submit.prevent="handleSubmit">
        <div class="input-group">
          <label>帳號</label>
          <input
            v-model="username"
            type="text"
            placeholder="請輸入帳號"
            required
            autocomplete="username"
          />
        </div>

        <div class="input-group">
          <label>密碼</label>
          <input
            v-model="password"
            type="password"
            placeholder="請輸入密碼"
            required
            autocomplete="current-password"
          />
        </div>

        <!-- 註冊時額外欄位 -->
        <div v-if="!isLogin" class="input-group">
          <label>顯示名稱</label>
          <input
            v-model="displayName"
            type="text"
            placeholder="將顯示在您的個人資料"
          />
        </div>

        <button type="submit" class="submit-btn">
          {{ isLogin ? '登入' : '立即註冊' }}
        </button>
      </form>

      <!-- 測試帳號提示 -->
      <div class="demo-accounts">
        <p>🔐 測試帳號</p>
        <div class="demo-row">
          <span>買家：</span>
          <code>testuser / 123456</code>
          <button @click="fillDemo('testuser', '123456')" class="fill-btn">填寫</button>
        </div>
        <div class="demo-row">
          <span>管理員：</span>
          <code>admin / admin123</code>
          <button @click="fillDemo('admin', '123456')" class="fill-btn">填寫</button>
        </div>
      </div>

      <p v-if="error" class="error-message">{{ error }}</p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import axios from '../utils/axios';
import { useRouter } from 'vue-router';

const router = useRouter();
const username = ref('');
const password = ref('');
const displayName = ref('');
const isLogin = ref(true);
const error = ref('');

const switchMode = (mode: boolean) => {
  isLogin.value = mode;
  error.value = '';
};

const fillDemo = (user: string, pass: string) => {
  username.value = user;
  password.value = pass;
};

const handleSubmit = async () => {
  const endpoint = isLogin.value ? '/auth/login' : '/auth/register';
  try {
    const payload: any = {
      username: username.value,
      password: password.value,
    };
    if (!isLogin.value) {
      payload.displayName = displayName.value || username.value;
    }

    const res = await axios.post(endpoint, payload);
    localStorage.setItem('token', res.data.token);

    // 取得角色
    const meRes = await axios.get('/auth/me');
    localStorage.setItem('role', meRes.data.role);

    // 根據角色跳轉
    if (meRes.data.role === 'ROLE_ADMIN') {
      router.push('/admin');
    } else {
      router.push('/products');
    }
  } catch (err: any) {
    error.value = err.response?.data || '操作失敗，請檢查帳號或密碼';
  }
};
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #f5f7fa 0%, #e9eef5 100%);
  padding: 20px;
}

.login-card {
  width: 100%;
  max-width: 480px;
  background: white;
  border-radius: 28px;
  box-shadow: 0 25px 45px -12px rgba(0, 0, 0, 0.2);
  padding: 32px 28px 40px;
  transition: transform 0.2s;
}

.brand {
  text-align: center;
  margin-bottom: 28px;
}

.brand h1 {
  font-size: 2rem;
  color: #ee4d2d;
  margin: 0 0 4px;
  font-weight: 700;
  letter-spacing: -0.5px;
}

.brand p {
  color: #7f8c8d;
  font-size: 0.9rem;
  margin: 0;
}

.tab-buttons {
  display: flex;
  gap: 16px;
  margin-bottom: 28px;
  border-bottom: 1px solid #e0e0e0;
}

.tab-buttons button {
  flex: 1;
  background: none;
  border: none;
  padding: 12px 0;
  font-size: 1.1rem;
  font-weight: 600;
  color: #95a5a6;
  cursor: pointer;
  transition: all 0.2s;
  position: relative;
}

.tab-buttons button.active {
  color: #ee4d2d;
}

.tab-buttons button.active::after {
  content: '';
  position: absolute;
  bottom: -1px;
  left: 0;
  width: 100%;
  height: 3px;
  background: #ee4d2d;
  border-radius: 3px 3px 0 0;
}

.input-group {
  margin-bottom: 20px;
}

.input-group label {
  display: block;
  font-size: 0.9rem;
  font-weight: 500;
  color: #2c3e50;
  margin-bottom: 8px;
}

.input-group input {
  width: 100%;
  padding: 14px 16px;
  font-size: 1rem;
  border: 1px solid #ddd;
  border-radius: 16px;
  background: #fafafa;
  transition: all 0.2s;
  outline: none;
  box-sizing: border-box;
}

.input-group input:focus {
  border-color: #ee4d2d;
  background: white;
  box-shadow: 0 0 0 3px rgba(238, 77, 45, 0.1);
}

.submit-btn {
  width: 100%;
  background: #ee4d2d;
  color: white;
  border: none;
  padding: 14px;
  font-size: 1.05rem;
  font-weight: 600;
  border-radius: 40px;
  cursor: pointer;
  margin-top: 12px;
  transition: background 0.2s, transform 0.1s;
}

.submit-btn:hover {
  background: #d43f21;
}

.submit-btn:active {
  transform: scale(0.98);
}

.demo-accounts {
  margin-top: 30px;
  background: #f8f9fa;
  border-radius: 20px;
  padding: 16px;
  font-size: 0.85rem;
}

.demo-accounts p {
  margin: 0 0 10px 0;
  font-weight: 600;
  color: #2c3e50;
}

.demo-row {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
  font-size: 0.85rem;
}

.demo-row span {
  min-width: 55px;
  color: #555;
}

.demo-row code {
  background: #e9ecef;
  padding: 4px 8px;
  border-radius: 12px;
  font-family: monospace;
  color: #d6336c;
}

.fill-btn {
  background: none;
  border: 1px solid #ee4d2d;
  color: #ee4d2d;
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 0.75rem;
  cursor: pointer;
  transition: all 0.2s;
}

.fill-btn:hover {
  background: #ee4d2d;
  color: white;
}

.error-message {
  color: #f44336;
  font-size: 0.85rem;
  text-align: center;
  margin-top: 16px;
  background: #ffe6e5;
  padding: 8px;
  border-radius: 12px;
}

/* 移動端微調 */
@media (max-width: 520px) {
  .login-card {
    padding: 24px 20px 32px;
  }
  .brand h1 {
    font-size: 1.8rem;
  }
  .input-group input {
    padding: 12px 14px;
  }
}
</style>
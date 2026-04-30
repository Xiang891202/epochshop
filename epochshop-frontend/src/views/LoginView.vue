<template>
  <div class="login-container">
    <h2>{{ isLogin ? '登入' : '註冊' }}</h2>
    <form @submit.prevent="handleSubmit">
      <div>
        <label>帳號</label>
        <input v-model="username" type="text" required />
      </div>
      <div>
        <label>密碼</label>
        <input v-model="password" type="password" required />
      </div>
      <!-- 註冊時才顯示名稱輸入 -->
      <div v-if="!isLogin">
        <label>名稱</label>
        <input v-model="displayName" type="text" placeholder="您的顯示名稱" />
      </div>
      <button type="submit">{{ isLogin ? '登入' : '註冊' }}</button>
      <button type="button" @click="isLogin = !isLogin">
        切換到{{ isLogin ? '註冊' : '登入' }}
      </button>
    </form>
    <p v-if="error" class="error">{{ error }}</p>
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

const handleSubmit = async () => {
  const endpoint = isLogin.value ? '/auth/login' : '/auth/register';
  try {
    const payload: any = {
      username: username.value,
      password: password.value,
    };
    // 注册时传递 displayName
    if (!isLogin.value) {
      payload.displayName = displayName.value || username.value;
    }

    const res = await axios.post(endpoint, payload);
    localStorage.setItem('token', res.data.token);

    // 取得角色
    const meRes = await axios.get('/auth/me');
    localStorage.setItem('role', meRes.data.role);

    // 根据角色跳转
    if (meRes.data.role === 'ROLE_ADMIN') {
      router.push('/admin');
    } else {
      router.push('/products');
    }
  } catch (err: any) {
    error.value = err.response?.data || '操作失敗';
  }
};
</script>

<style scoped>
.login-container {
  max-width: 400px;
  margin: 50px auto;
  padding: 20px;
  border: 1px solid #ddd;
  border-radius: 8px;
}
label {
  display: inline-block;
  width: 60px;
}
input {
  margin: 10px 0;
  padding: 8px;
  width: 200px;
}
button {
  margin-right: 10px;
  padding: 8px 16px;
}
.error {
  color: red;
}
</style>
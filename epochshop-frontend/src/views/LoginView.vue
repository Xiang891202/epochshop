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
const isLogin = ref(true);
const error = ref('');

const handleSubmit = async () => {
  const endpoint = isLogin.value ? '/auth/login' : '/auth/register';
  try {
    const res = await axios.post(endpoint, {
      username: username.value,
      password: password.value,
    });
    localStorage.setItem('token', res.data.token);
    router.push('/products');
  } catch (err: any) {
    error.value = err.response?.data || '操作失敗，請稍後再試';
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
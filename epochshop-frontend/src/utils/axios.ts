import axios from 'axios';

const instance = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api',
  headers: {
    'Content-Type': 'application/json',
  },
});

// 請求攔截器：自動帶上 Token
instance.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// 回應攔截器：處理 401/403 自動跳轉登入
instance.interceptors.response.use(
  (response) => response,
  (error) => {
      if (error.response?.status === 401 || error.response?.status === 403) {
      localStorage.removeItem('token');
      localStorage.removeItem('role');   // ← 可能你之前没有删除 role，导致 role 残留
      window.location.href = '/login';
  }
    return Promise.reject(error);
  }
);

export default instance;
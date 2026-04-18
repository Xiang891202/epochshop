import { createRouter, createWebHistory } from 'vue-router';
import LoginView from '../views/LoginView.vue';
import ProductsView from '../views/ProductsView.vue';
import CartView from '../views/CartView.vue';

const routes = [
  {
    path: '/',
    redirect: '/products',
  },
  {
    path: '/login',
    name: 'Login',
    component: LoginView,
  },
  {
    path: '/products',
    name: 'Products',
    component: ProductsView,
    meta: { requiresAuth: true },
  },
  {
    path: '/cart',                               // ✅ 新增購物車路由
    name: 'Cart',
    component: CartView,
    meta: { requiresAuth: true },
  },
];

const router = createRouter({
  history: createWebHistory(),
  routes,
});

// 路由守衛：未登入時導向登入頁
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token');
  if (to.meta.requiresAuth && !token) {
    next('/login');
  } else if (to.path === '/login' && token) {
    next('/products'); // 已登入者訪問登入頁直接導向商品頁
  } else {
    next();
  }
});

export default router;
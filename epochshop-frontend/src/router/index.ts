import { createRouter, createWebHistory } from 'vue-router';
import LoginView from '../views/LoginView.vue';
import ProductsView from '../views/ProductsView.vue';
import CartView from '../views/CartView.vue';
import OrdersView from '../views/OrdersView.vue';
import AdminView from '../views/AdminView.vue';
import SalesView from '../views/SalesView.vue';
import AdminLayout from '../views/layout/AdminLayout.vue';
import ChatView from '@/views/ChatView.vue';
// import { p } from 'vue-router/dist/router-CWoNjPRp.mjs';

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
  {
    path: '/orders',                              // ✅ 新增訂單歷史路由
    name: 'Orders',
    component: OrdersView,
    meta: { requiresAuth: true },
  },
  {
    path: '/admin',
    component: AdminLayout,
    meta: { requiresAuth: true, requiresAdmin: true },
    children: [
      { path: '', component: AdminView },
      { path: 'sales', component: SalesView },
    ],
  },
  {
    path: '/admin/sales',
    name: 'Sales',
    component: SalesView,
    meta: { requiresAuth: true, requiresAdmin: true },
  },
  {
    path: '/messages',
    name: 'Chat',
    component: ChatView,
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
  const role = localStorage.getItem('role');

  // 需要管理員權限，但沒有 token 或 role 不符 → 踢回商品頁
  if (to.meta.requiresAdmin && (!token || role !== 'ROLE_ADMIN')) {
    next('/products');
    return;
  }

  // 需要登入但沒有 token → 回登入頁
  if (to.meta.requiresAuth && !token) {
    next('/login');
    return;
  }

  // 已登入卻想去登入頁 → 根據角色導向
  if (to.path === '/login' && token) {
    const role = localStorage.getItem('role');
    if (role === 'ROLE_ADMIN') {
      next('/admin');
    } else {
      next('/products');
    }
    return;
  }

  next();
});
export default router;
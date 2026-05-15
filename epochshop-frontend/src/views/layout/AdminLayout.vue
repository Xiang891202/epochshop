<template>
  <div class="admin-layout">
    <!-- 桌面版侧边栏 -->
    <aside class="admin-sidebar" :class="{ 'mobile-open': mobileMenuOpen }">
      <div class="sidebar-header">
        <h3>管理後台</h3>
        <button class="mobile-close" @click="mobileMenuOpen = false">✕</button>
      </div>
      <nav class="sidebar-nav">
        <router-link to="/admin" class="sidebar-link" @click="mobileMenuOpen = false">商品管理</router-link>
        <router-link to="/admin/sales" class="sidebar-link" @click="mobileMenuOpen = false">銷售額</router-link>
        <router-link to="/admin/messages" class="sidebar-link" @click="mobileMenuOpen = false">
          💬 訊息
          <span v-if="unreadCount > 0" class="unread-badge">{{ unreadCount }}</span>
        </router-link>
        <router-link to="/products" class="sidebar-link" @click="mobileMenuOpen = false">← 回商城</router-link>
      </nav>
    </aside>

    <!-- 移动端顶部栏 -->
    <div class="mobile-header">
      <button class="menu-toggle" @click="mobileMenuOpen = !mobileMenuOpen">☰</button>
      <span class="mobile-title">管理後台</span>
      <div class="placeholder"></div>
    </div>

    <!-- 主内容区 -->
    <main class="admin-content">
      <router-view />
    </main>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onBeforeUnmount, watch } from 'vue';
import { useRoute } from 'vue-router';
import axios from '../../utils/axios';

const mobileMenuOpen = ref(false);
const unreadCount = ref(0);
const route = useRoute();

// 获取未读消息数量
const fetchUnreadCount = async () => {
  try {
    const res = await axios.get('/messages/unread-count');
    unreadCount.value = res.data.count;
  } catch (err) {
    console.error('获取未读消息数失败', err);
  }
};

// 监听路由变化，当进入消息页面时标记已读（可选：不清除红点，但用户进入列表后自动标记）
watch(() => route.path, (newPath) => {
  if (newPath === '/admin/messages') {
    // 进入消息中心后刷新计数（页面内已读标记后会自动更新，无需额外动作）
    fetchUnreadCount();
  }
});

// 定时刷新未读数（每30秒）
let interval: number;
onMounted(() => {
  fetchUnreadCount();
  interval = window.setInterval(fetchUnreadCount, 30000);
  window.addEventListener('resize', handleResize);
});
onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize);
  clearInterval(interval);
});

const handleResize = () => {
  if (window.innerWidth > 768) {
    mobileMenuOpen.value = false;
  }
};
</script>

<style scoped>
/* 原有样式保留，增加未读角标 */
.unread-badge {
  background: #f44336;
  color: white;
  border-radius: 12px;
  padding: 2px 8px;
  font-size: 0.7rem;
  font-weight: bold;
  margin-left: auto;
  display: inline-block;
}

/* 侧边栏链接需支持 flex 布局 */
.sidebar-link {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

/* 其余样式保持不变（参考之前 RWD 修改） */
.admin-layout { display: flex; min-height: 100vh; }
.admin-sidebar {
  width: 240px;
  background: #2c3e50;
  color: white;
  padding: 20px 0;
  display: flex;
  flex-direction: column;
  transition: transform 0.3s ease;
  z-index: 1000;
}
.sidebar-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 20px 20px;
  border-bottom: 1px solid #34495e;
  margin-bottom: 20px;
}
.mobile-close { display: none; background: none; border: none; color: white; font-size: 1.5rem; cursor: pointer; }
.sidebar-nav { display: flex; flex-direction: column; gap: 8px; padding: 0 16px; }
.sidebar-link {
  color: white;
  text-decoration: none;
  padding: 10px 16px;
  border-radius: 8px;
  transition: background 0.2s;
}
.sidebar-link:hover, .sidebar-link.router-link-active { background: #34495e; }
.mobile-header { display: none; position: sticky; top: 0; background: #2c3e50; color: white; padding: 12px 16px; align-items: center; justify-content: space-between; z-index: 999; }
.menu-toggle { background: none; border: none; font-size: 1.8rem; color: white; cursor: pointer; }
.mobile-title { font-size: 1.2rem; font-weight: 600; }
.placeholder { width: 32px; }
.admin-content { flex: 1; padding: 24px; background: #f4f6f9; }

@media (max-width: 768px) {
  .admin-layout { flex-direction: column; }
  .admin-sidebar {
    position: fixed;
    top: 0;
    left: 0;
    width: 260px;
    height: 100%;
    transform: translateX(-100%);
    box-shadow: 2px 0 12px rgba(0,0,0,0.3);
    overflow-y: auto;
  }
  .admin-sidebar.mobile-open { transform: translateX(0); }
  .mobile-close { display: block; }
  .mobile-header { display: flex; }
  .admin-content { padding: 16px; }
}
</style>
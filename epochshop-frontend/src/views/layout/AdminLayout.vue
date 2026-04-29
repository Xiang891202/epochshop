<template>
  <div class="admin-layout">
    <div class="admin-sidebar">
      <h3>管理後台</h3>
      <router-link to="/admin" class="sidebar-link">商品管理</router-link>
      <router-link to="/admin/sales" class="sidebar-link">銷售額</router-link>
      <router-link to="/products" class="sidebar-link">← 回商城</router-link>
    </div>
    <div class="admin-content">
      <router-view />
    </div>
  </div>
</template>

<script setup lang="ts">
</script>

<style scoped>
.admin-layout {
  display: flex;
  min-height: calc(100vh - 60px);
}
.admin-sidebar {
  width: 200px;
  background: #2c3e50;
  color: white;
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 10px;
}
.admin-sidebar h3 {
  margin-bottom: 20px;
  text-align: center;
}
.sidebar-link {
  color: white;
  text-decoration: none;
  padding: 10px;
  border-radius: 6px;
  transition: background 0.3s;
}
.sidebar-link:hover, .sidebar-link.router-link-active {
  background: #34495e;
}
.admin-content {
  flex: 1;
  padding: 20px;
}
</style>
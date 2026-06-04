<template>
  <div class="admin-layout">
    <aside class="sidebar">
      <div class="sidebar-header">
        <router-link to="/home" class="sidebar-logo">
          <el-icon :size="22"><Reading /></el-icon>
          <span>BookStore 管理</span>
        </router-link>
      </div>
      <el-menu
        :default-active="route.path"
        router
        background-color="#1a1a2e"
        text-color="#c0c0d0"
        active-text-color="#fff"
      >
        <el-menu-item index="/admin/dashboard">
          <el-icon><DataAnalysis /></el-icon>
          <span>仪表盘</span>
        </el-menu-item>
        <el-menu-item index="/admin/books">
          <el-icon><Notebook /></el-icon>
          <span>图书管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/categories">
          <el-icon><Files /></el-icon>
          <span>分类管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/orders">
          <el-icon><List /></el-icon>
          <span>订单管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/users">
          <el-icon><UserFilled /></el-icon>
          <span>用户管理</span>
        </el-menu-item>
        <el-menu-item index="/home">
          <el-icon><Back /></el-icon>
          <span>返回前台</span>
        </el-menu-item>
      </el-menu>
    </aside>
    <main class="admin-main">
      <header class="admin-header">
        <h2>{{ route.meta.title }}</h2>
        <div class="admin-header-actions">
          <span class="admin-badge">管理员：{{ userStore.user?.nickname || userStore.user?.username }}</span>
          <el-button text @click="handleLogout">退出</el-button>
        </div>
      </header>
      <div class="admin-content">
        <router-view />
      </div>
    </main>
  </div>
</template>

<script setup>
import { useUserStore } from '../../stores/user'
import { useRouter, useRoute } from 'vue-router'

const userStore = useUserStore()
const router = useRouter()
const route = useRoute()

function handleLogout() {
  userStore.clearAuth()
  router.push('/login')
}
</script>

<style scoped>
.admin-layout {
  display: flex;
  min-height: 100vh;
}
.sidebar {
  width: 220px;
  background: #1a1a2e;
  flex-shrink: 0;
}
.sidebar-header {
  padding: 20px 16px;
  border-bottom: 1px solid rgba(255,255,255,0.08);
}
.sidebar-logo {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #fff;
  font-size: 1rem;
  font-weight: 600;
}
.sidebar-logo:hover {
  color: #4f46e5;
}
.el-menu {
  border-right: none;
}
.admin-main {
  flex: 1;
  background: #f5f6fa;
  display: flex;
  flex-direction: column;
}
.admin-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: #fff;
  padding: 16px 24px;
  border-bottom: 1px solid #ebeef5;
}
.admin-header h2 {
  font-size: 1.1rem;
  color: #303133;
}
.admin-header-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}
.admin-badge {
  font-size: 0.85rem;
  color: #909399;
}
.admin-content {
  padding: 24px;
  flex: 1;
  overflow-y: auto;
}
</style>

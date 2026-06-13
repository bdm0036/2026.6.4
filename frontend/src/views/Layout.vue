<template>
  <div class="layout">
    <header class="header">
      <div class="header-inner container">
        <router-link to="/home" class="logo">
          <el-icon :size="24"><Reading /></el-icon>
          <span>BookStore</span>
        </router-link>
        <nav class="nav">
          <router-link to="/home">首页</router-link>
          <router-link to="/books">图书</router-link>
          <router-link to="/favorites" v-if="userStore.isLoggedIn()">收藏</router-link>
          <router-link to="/coupons" v-if="userStore.isLoggedIn()">优惠券</router-link>
        </nav>
        <div class="header-actions">
          <router-link to="/cart" class="cart-btn">
            <el-badge :value="cartStore.totalCount" :hidden="cartStore.totalCount === 0" :max="99">
              <el-icon :size="20"><ShoppingCart /></el-icon>
            </el-badge>
          </router-link>
          <template v-if="userStore.isLoggedIn()">
            <router-link v-if="userStore.isAdmin()" to="/admin/dashboard" class="admin-link">
              <el-icon :size="16"><Setting /></el-icon>
              管理后台
            </router-link>
            <el-dropdown>
              <span class="user-dropdown">
                <el-avatar :size="32" :src="userStore.user?.avatar" />
                <span class="username">{{ userStore.user?.nickname || userStore.user?.username }}</span>
                <el-icon><ArrowDown /></el-icon>
              </span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item @click="$router.push('/orders')">我的订单</el-dropdown-item>
                  <el-dropdown-item @click="$router.push('/favorites')">我的收藏</el-dropdown-item>
                  <el-dropdown-item @click="$router.push('/profile')">个人中心</el-dropdown-item>
                  <el-dropdown-item v-if="userStore.isAdmin()" divided @click="$router.push('/admin/dashboard')">管理后台</el-dropdown-item>
                  <el-dropdown-item divided @click="handleLogout">退出登录</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
          <template v-else>
            <router-link to="/login" class="btn-text">登录</router-link>
            <router-link to="/register" class="btn-primary-sm">注册</router-link>
          </template>
        </div>
      </div>
    </header>
    <main class="main">
      <router-view />
    </main>
    <footer class="footer">
      <div class="footer-inner container">
        <p>© 2024 BookStore 网上书店 - 微服务架构毕业设计项目</p>
      </div>
    </footer>
  </div>
</template>

<script setup>
import { useUserStore } from '../stores/user'
import { useCartStore } from '../stores/cart'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'

const userStore = useUserStore()
const cartStore = useCartStore()
const router = useRouter()

async function handleLogout() {
  await userStore.logout()
  cartStore.clearCart()
  ElMessage.success('已退出登录')
  router.push('/home')
}
</script>

<style scoped>
.layout {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}
.header {
  position: sticky;
  top: 0;
  z-index: 100;
  background: rgba(255, 255, 255, 0.92);
  backdrop-filter: blur(12px);
  border-bottom: 1px solid rgba(0, 0, 0, 0.06);
  height: 64px;
}
.header-inner {
  display: flex;
  align-items: center;
  height: 100%;
  gap: 32px;
}
.logo {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 1.2rem;
  font-weight: 700;
  color: #1a1a2e;
}
.nav {
  display: flex;
  gap: 24px;
  flex: 1;
}
.nav a {
  font-size: 0.9rem;
  color: #5a5a7a;
  transition: color 0.2s;
}
.nav a:hover, .nav a.router-link-active {
  color: #4f46e5;
}
.header-actions {
  display: flex;
  align-items: center;
  gap: 16px;
}
.cart-btn {
  color: #5a5a7a;
  padding: 6px;
  border-radius: 8px;
  transition: background 0.2s;
}
.cart-btn:hover {
  background: #f0f0f8;
}
.user-dropdown {
  display: flex;
  align-items: center;
  gap: 6px;
  cursor: pointer;
}
.username {
  font-size: 0.9rem;
  color: #1a1a2e;
}
.admin-link {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 0.85rem;
  color: #fff;
  background: #e67e22;
  padding: 6px 14px;
  border-radius: 8px;
  transition: background 0.2s;
}
.admin-link:hover {
  background: #d35400;
}
.btn-text {
  font-size: 0.9rem;
  color: #5a5a7a;
}
.btn-primary-sm {
  font-size: 0.85rem;
  color: #fff;
  background: #4f46e5;
  padding: 6px 16px;
  border-radius: 8px;
  transition: background 0.2s;
}
.btn-primary-sm:hover {
  background: #4338ca;
}
.main {
  flex: 1;
}
.footer {
  background: #fff;
  border-top: 1px solid rgba(0, 0, 0, 0.06);
  padding: 24px 0;
  margin-top: 60px;
}
.footer-inner {
  text-align: center;
  color: #8c8ca1;
  font-size: 0.85rem;
}
</style>

import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/',
    component: () => import('../views/Layout.vue'),
    redirect: '/home',
    children: [
      { path: 'home', name: 'Home', component: () => import('../views/Home.vue'), meta: { title: '首页' } },
      { path: 'books', name: 'Books', component: () => import('../views/Books.vue'), meta: { title: '图书列表' } },
      { path: 'book/:id', name: 'BookDetail', component: () => import('../views/BookDetail.vue'), meta: { title: '图书详情' } },
      { path: 'cart', name: 'Cart', component: () => import('../views/Cart.vue'), meta: { title: '购物车' } },
      { path: 'orders', name: 'Orders', component: () => import('../views/Orders.vue'), meta: { title: '我的订单' } },
      { path: 'favorites', name: 'Favorites', component: () => import('../views/Favorites.vue'), meta: { title: '我的收藏' } },
      { path: 'profile', name: 'Profile', component: () => import('../views/Profile.vue'), meta: { title: '个人中心' } },
    ],
  },
  // 管理员后台
  {
    path: '/admin',
    component: () => import('../views/admin/AdminLayout.vue'),
    meta: { requiresAdmin: true },
    redirect: '/admin/dashboard',
    children: [
      { path: 'dashboard', name: 'Dashboard', component: () => import('../views/admin/Dashboard.vue'), meta: { title: '仪表盘' } },
      { path: 'books', name: 'BooksManager', component: () => import('../views/admin/BooksManager.vue'), meta: { title: '图书管理' } },
      { path: 'categories', name: 'CategoriesManager', component: () => import('../views/admin/CategoriesManager.vue'), meta: { title: '分类管理' } },
      { path: 'orders', name: 'OrdersManager', component: () => import('../views/admin/OrdersManager.vue'), meta: { title: '订单管理' } },
      { path: 'users', name: 'UsersManager', component: () => import('../views/admin/UsersManager.vue'), meta: { title: '用户管理' } },
    ],
  },
  { path: '/login', name: 'Login', component: () => import('../views/Login.vue'), meta: { title: '登录' } },
  { path: '/register', name: 'Register', component: () => import('../views/Register.vue'), meta: { title: '注册' } },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior: () => ({ top: 0 }),
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  const user = JSON.parse(localStorage.getItem('user') || 'null')
  const publicPages = ['/login', '/register', '/home', '/books', '/']
  const isPublicBookDetail = to.path.startsWith('/book/')
  const isPublic = publicPages.includes(to.path) || isPublicBookDetail

  if (!token && !isPublic) {
    next('/login')
  } else if (token && to.meta.requiresAdmin && user?.role !== 'ADMIN') {
    next('/home')
  } else if (token && (to.path === '/login' || to.path === '/register')) {
    next('/home')
  } else {
    next()
  }
})

export default router

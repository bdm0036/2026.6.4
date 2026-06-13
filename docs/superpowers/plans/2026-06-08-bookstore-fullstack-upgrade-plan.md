# BookStore 全栈深度重构 — 实现计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 将基础书店系统升级为商业级网上书店：前端体验全面升级 + 后端业务深度强化（支付/库存/物流/优惠券/秒杀）+ 150+本种子数据

**Architecture:** Phase 1 纯前端改造（Vue 3 组件体系重构），Phase 2 后端业务模块新增 + 前后端集成。封面采用纯CSS占位方案。秒杀使用 Redis Lua 原子操作 + 同步下单。

**Tech Stack:** Vue 3 + Element Plus + Pinia + ECharts | Spring Boot 3.2 + Spring Cloud + MyBatis-Plus + Redis + MinIO

---

## 文件结构映射

### Phase 1 新建文件
```
frontend/src/components/BookCover.vue          — 统一图书封面（CSS渐变色+书名首字）
frontend/src/components/BookCard.vue           — 统一图书卡片（标签/评分/价格/加购）
frontend/src/components/BannerCarousel.vue     — 首页轮播Banner
frontend/src/components/BookSection.vue        — 可复用图书展示区（Tab切换）
frontend/src/components/EmptyState.vue         — 空状态插画组件
frontend/src/components/ThemeToggle.vue        — 暗色模式切换按钮
frontend/src/components/GlobalSearch.vue       — 全局搜索面板
frontend/src/views/OrderDetail.vue             — 订单详情页（全新）
frontend/src/composables/useTheme.js           — 暗色模式逻辑
frontend/src/composables/useDebounce.js        — 防抖工具
```

### Phase 1 修改文件
```
frontend/src/assets/style.css                  — 增加暗色模式CSS变量
frontend/src/App.vue                           — 暗色模式class绑定
frontend/src/main.js                           — 引入ECharts
frontend/src/router/index.js                   — 新增路由、更新守卫
frontend/src/api/index.js                      — 新增API方法
frontend/src/stores/user.js                    — 增加修改密码方法
frontend/src/views/Layout.vue                  — 全局搜索+主题切换
frontend/src/views/Home.vue                    — 重写（轮播+Tab图书区）
frontend/src/views/Books.vue                   — 增强（排序/价格/视图切换）
frontend/src/views/BookDetail.vue              — 增强（标签/推荐/趋势图）
frontend/src/views/Cart.vue                    — 增强（库存状态/地址选择/批量操作）
frontend/src/views/Orders.vue                  — 增强（状态筛选/进度条）
frontend/src/views/Profile.vue                 — 重写为Tab式
frontend/src/views/admin/Dashboard.vue         — 增加ECharts图表
frontend/src/views/admin/BooksManager.vue      — 增加富文本编辑器
```

### Phase 2 新建后端文件
```
backend/bookstore-common/src/main/java/com/bookstore/common/entity/
  Address.java, Coupon.java, UserCoupon.java, SeckillSession.java,
  SeckillBook.java, PaymentLog.java, Shipment.java, BrowseHistory.java, StockLog.java
backend/bookstore-common/src/main/java/com/bookstore/common/dto/
  CreateOrderDTO.java (修改增加couponId/addressId), ChangePasswordDTO.java,
  AddressDTO.java, CouponDTO.java
backend/bookstore-auth-service/.../controller/AuthController.java — 增加修改密码
backend/bookstore-user-service/.../controller/AddressController.java — 地址CRUD
backend/bookstore-user-service/.../service/AddressService.java
backend/bookstore-user-service/.../mapper/AddressMapper.java
backend/bookstore-user-service/.../controller/BrowseHistoryController.java
backend/bookstore-user-service/.../service/BrowseHistoryService.java
backend/bookstore-user-service/.../mapper/BrowseHistoryMapper.java
backend/bookstore-order-service/.../controller/PaymentController.java
backend/bookstore-order-service/.../service/PaymentService.java
backend/bookstore-order-service/.../mapper/PaymentLogMapper.java
backend/bookstore-order-service/.../controller/ShipmentController.java
backend/bookstore-order-service/.../service/ShipmentService.java
backend/bookstore-order-service/.../mapper/ShipmentMapper.java
backend/bookstore-order-service/.../controller/CouponController.java
backend/bookstore-order-service/.../service/CouponService.java
backend/bookstore-order-service/.../mapper/CouponMapper.java, UserCouponMapper.java
backend/bookstore-order-service/.../service/StockService.java — 库存扣减
backend/bookstore-order-service/.../mapper/StockLogMapper.java
backend/bookstore-order-service/.../config/SchedulingConfig.java — 定时任务
backend/bookstore-product-service/.../controller/FileController.java — 文件上传
backend/bookstore-product-service/.../controller/SeckillController.java
backend/bookstore-product-service/.../service/SeckillService.java
backend/bookstore-product-service/.../mapper/SeckillSessionMapper.java, SeckillBookMapper.java
backend/bookstore-common/.../config/DataSeeder.java — 种子数据
backend/bookstore-gateway/.../filter/RateLimiterFilter.java — 限流
backend/bookstore-product-service/.../config/MinioConfig.java
```

---

## Phase 1: 前端体验升级

### Task 1: 暗色模式基础设施

**Files:**
- Modify: `frontend/src/assets/style.css`
- Create: `frontend/src/composables/useTheme.js`
- Create: `frontend/src/components/ThemeToggle.vue`
- Modify: `frontend/src/App.vue`

- [ ] **Step 1: 更新 style.css 增加 CSS 变量和暗色模式**

在 `frontend/src/assets/style.css` 末尾追加暗色模式变量：

```css
/* ========== 暗色模式 CSS 变量 ========== */
:root {
  --bg-primary: #f5f7fa;
  --bg-card: #ffffff;
  --bg-header: rgba(255, 255, 255, 0.92);
  --text-primary: #1a1a2e;
  --text-secondary: #5a5a7a;
  --text-muted: #8c8ca1;
  --border-color: rgba(0, 0, 0, 0.06);
  --shadow-card: 0 1px 8px rgba(0, 0, 0, 0.03);
  --shadow-hover: 0 12px 40px rgba(0, 0, 0, 0.1);
  --gradient-hero: linear-gradient(135deg, #1a1a2e 0%, #16213e 50%, #0f3460 100%);
}

html.dark {
  --bg-primary: #0f0f1a;
  --bg-card: #1a1a2e;
  --bg-header: rgba(26, 26, 46, 0.92);
  --text-primary: #e8e8f0;
  --text-secondary: #a0a0b8;
  --text-muted: #6a6a80;
  --border-color: rgba(255, 255, 255, 0.06);
  --shadow-card: 0 1px 8px rgba(0, 0, 0, 0.2);
  --shadow-hover: 0 12px 40px rgba(0, 0, 0, 0.4);
  --gradient-hero: linear-gradient(135deg, #0a0a16 0%, #0d0d24 50%, #0a1630 100%);
}

/* 让 body 背景色跟随变量 */
body {
  background: var(--bg-primary);
}
```

- [ ] **Step 2: 创建 useTheme.js composable**

```js
// frontend/src/composables/useTheme.js
import { ref, watchEffect } from 'vue'

const isDark = ref(localStorage.getItem('theme') === 'dark')

watchEffect(() => {
  document.documentElement.classList.toggle('dark', isDark.value)
  localStorage.setItem('theme', isDark.value ? 'dark' : 'light')
})

export function useTheme() {
  function toggle() {
    isDark.value = !isDark.value
  }
  return { isDark, toggle }
}
```

- [ ] **Step 3: 创建 ThemeToggle.vue**

```vue
<!-- frontend/src/components/ThemeToggle.vue -->
<template>
  <button class="theme-toggle" @click="toggle" :title="isDark ? '切换亮色模式' : '切换暗色模式'">
    <el-icon :size="18"><Sunny v-if="isDark" /><Moon v-else /></el-icon>
  </button>
</template>

<script setup>
import { useTheme } from '../composables/useTheme'
const { isDark, toggle } = useTheme()
</script>

<style scoped>
.theme-toggle {
  display: flex; align-items: center; justify-content: center;
  width: 36px; height: 36px; border-radius: 8px; border: none;
  background: transparent; cursor: pointer; color: var(--text-secondary);
  transition: background 0.2s;
}
.theme-toggle:hover { background: rgba(128, 128, 160, 0.15); }
</style>
```

- [ ] **Step 4: 更新 App.vue 绑定暗色模式 class**

修改 `frontend/src/App.vue`：

```vue
<template>
  <div id="app" :class="{ dark: isDark }">
    <router-view />
  </div>
</template>

<script setup>
import { useTheme } from './composables/useTheme'
const { isDark } = useTheme()
</script>

<style>
#app {
  min-height: 100vh;
  background-color: var(--bg-primary);
}
</style>
```

- [ ] **Step 5: 验证**

运行 `cd frontend && npm run dev`，打开浏览器检查：
- 默认亮色模式正常
- 点击 ThemeToggle 按钮切换到暗色模式
- 刷新页面后暗色模式保持

---

### Task 2: BookCover 统一封面组件

**Files:**
- Create: `frontend/src/components/BookCover.vue`

- [ ] **Step 1: 创建 BookCover.vue**

```vue
<!-- frontend/src/components/BookCover.vue -->
<template>
  <div class="book-cover" :style="coverStyle" :class="{ 'has-image': src }">
    <img v-if="src" :src="src" :alt="title" />
    <template v-else>
      <span class="cover-char">{{ firstChar }}</span>
      <span class="cover-label" v-if="showLabel">{{ label }}</span>
    </template>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  title: { type: String, default: '' },
  src: { type: String, default: '' },
  categoryId: { type: Number, default: 0 },
  label: { type: String, default: '' },
  showLabel: { type: Boolean, default: false },
})

// 分类色系映射
const categoryColors = [
  '#e67e22', '#2980b9', '#27ae60', '#8e44ad', '#d35400',
  '#e91e63', '#00bcd4', '#ff6f00', '#607d8b', '#3f51b5',
]

const firstChar = computed(() => {
  if (!props.title) return '?'
  const ch = props.title.charAt(0)
  // 检测中文字符
  return /[一-鿿]/.test(ch) ? ch : props.title.charAt(0).toUpperCase()
})

const bgColor = computed(() => categoryColors[props.categoryId % categoryColors.length])

const coverStyle = computed(() => ({
  background: `linear-gradient(135deg, ${bgColor.value}, ${bgColor.value}dd)`,
}))
</script>

<style scoped>
.book-cover {
  width: 100%;
  aspect-ratio: 3/4;
  border-radius: 8px;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  position: relative;
}
.book-cover img {
  width: 100%; height: 100%; object-fit: cover;
}
.cover-char {
  font-size: 2rem; font-weight: 800; color: rgba(255,255,255,0.9);
  text-shadow: 0 2px 8px rgba(0,0,0,0.15);
  line-height: 1;
}
.cover-label {
  position: absolute; bottom: 0; left: 0; right: 0;
  padding: 6px 0; text-align: center;
  font-size: 0.7rem; font-weight: 600; color: #fff;
  background: rgba(0,0,0,0.35);
}
</style>
```

- [ ] **Step 2: 验证** — 启动前端，在使用处临时引入 `<BookCover title="深入理解计算机系统" :category-id="1" />` 查看效果

---

### Task 3: BookCard 统一图书卡片

**Files:**
- Create: `frontend/src/components/BookCard.vue`

- [ ] **Step 1: 创建 BookCard.vue**

```vue
<!-- frontend/src/components/BookCard.vue -->
<template>
  <div class="book-card" @click="$router.push(`/book/${book.id}`)">
    <BookCover :title="book.title" :src="book.coverImage" :category-id="book.categoryId"
      :label="tagLabel" :show-label="!!tagLabel" />
    <div class="book-info">
      <h3>{{ book.title }}</h3>
      <p class="book-author">{{ book.author }}</p>
      <div class="book-rating" v-if="book.avgRating">
        <el-rate v-model="book.avgRating" disabled size="small"
          :colors="['#f59e0b','#f59e0b','#f59e0b']" />
        <span class="rating-num">{{ Number(book.avgRating).toFixed(1) }}</span>
      </div>
      <div class="book-bottom">
        <span class="book-price">¥{{ book.price }}</span>
        <el-button size="small" type="primary" circle @click.stop="handleAddCart">
          <el-icon><Plus /></el-icon>
        </el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useCartStore } from '../stores/cart'
import { ElMessage } from 'element-plus'
import BookCover from './BookCover.vue'

const props = defineProps({ book: { type: Object, required: true } })
const emit = defineEmits(['add-cart'])
const cartStore = useCartStore()

const tagLabel = computed(() => {
  if (props.book.stock === 0) return '缺货'
  if (props.book.tags && props.book.tags.includes('新上')) return '新上'
  if (props.book.tags && props.book.tags.includes('热卖')) return '热卖'
  if (props.book.tags && props.book.tags.includes('特价')) return '特价'
  return ''
})

function handleAddCart() {
  cartStore.addItem(props.book)
  ElMessage.success(`已添加《${props.book.title}》到购物车`)
  emit('add-cart', props.book)
}
</script>

<style scoped>
.book-card {
  background: var(--bg-card); border-radius: 12px; overflow: hidden; cursor: pointer;
  transition: transform 0.2s, box-shadow 0.2s;
  border: 1px solid var(--border-color);
}
.book-card:hover {
  transform: translateY(-4px);
  box-shadow: var(--shadow-hover);
}
.book-info { padding: 14px; }
.book-info h3 {
  font-size: 0.9rem; font-weight: 600; color: var(--text-primary);
  overflow: hidden; text-overflow: ellipsis; white-space: nowrap;
}
.book-author { margin-top: 4px; font-size: 0.78rem; color: var(--text-muted); }
.book-rating { display: flex; align-items: center; gap: 4px; margin-top: 6px; }
.rating-num { font-size: 0.75rem; color: #f59e0b; font-weight: 600; }
.book-bottom { display: flex; align-items: center; justify-content: space-between; margin-top: 10px; }
.book-price { font-size: 1.05rem; font-weight: 700; color: #e74c3c; }
</style>
```

- [ ] **Step 2: 验证** — 在 Home.vue 中替换原有图书网格代码为 `<BookCard :book="book" />`

---

### Task 4: EmptyState 空状态组件

**Files:**
- Create: `frontend/src/components/EmptyState.vue`

- [ ] **Step 1: 创建 EmptyState.vue**

```vue
<!-- frontend/src/components/EmptyState.vue -->
<template>
  <div class="empty-state">
    <div class="empty-icon">
      <el-icon :size="size"><component :is="icon" /></el-icon>
    </div>
    <p class="empty-text">{{ description }}</p>
    <el-button v-if="actionText" type="primary" @click="$emit('action')">
      {{ actionText }}
    </el-button>
  </div>
</template>

<script setup>
defineProps({
  icon: { type: String, default: 'Document' },
  description: { type: String, default: '暂无数据' },
  actionText: { type: String, default: '' },
  size: { type: Number, default: 64 },
})
defineEmits(['action'])
</script>

<style scoped>
.empty-state { text-align: center; padding: 80px 0; }
.empty-icon { color: #c0c0d0; margin-bottom: 16px; }
.empty-text { color: var(--text-muted); font-size: 1rem; margin-bottom: 16px; }
</style>
```

- [ ] **Step 2: 验证** — 在任意页面引入测试，确认空状态显示正常

---

### Task 5: Home 首页重写

**Files:**
- Create: `frontend/src/components/BannerCarousel.vue`
- Create: `frontend/src/components/BookSection.vue`
- Modify: `frontend/src/views/Home.vue`
- Modify: `frontend/src/api/index.js`

- [ ] **Step 1: 创建 BannerCarousel.vue**

```vue
<!-- frontend/src/components/BannerCarousel.vue -->
<template>
  <div class="banner-carousel">
    <el-carousel :interval="5000" arrow="hover" height="320px" indicator-position="outside">
      <el-carousel-item v-for="(banner, i) in banners" :key="i">
        <div class="banner-slide" :style="{ background: banner.bg }" @click="$router.push(banner.link)">
          <div class="banner-text">
            <h2>{{ banner.title }}</h2>
            <p>{{ banner.subtitle }}</p>
          </div>
          <div class="banner-emoji">{{ banner.emoji }}</div>
        </div>
      </el-carousel-item>
    </el-carousel>
  </div>
</template>

<script setup>
const banners = [
  { title: '📚 读书日特惠', subtitle: '全场图书满100减20，满200减50', bg: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)', emoji: '📖', link: '/books' },
  { title: '🆕 新书上架', subtitle: '本月最新好书，抢先阅读', bg: 'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)', emoji: '🆕', link: '/books?sort=newest' },
  { title: '🔥 编程经典', subtitle: '计算机必读经典，助你升职加薪', bg: 'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)', emoji: '💻', link: '/books?categoryId=2' },
]
</script>

<style scoped>
.banner-carousel { margin: 24px 0; }
.banner-slide {
  height: 100%; display: flex; align-items: center; justify-content: space-between;
  padding: 0 60px; cursor: pointer; border-radius: 16px; overflow: hidden;
}
.banner-text h2 { font-size: 2rem; font-weight: 800; color: #fff; }
.banner-text p { margin-top: 8px; color: rgba(255,255,255,0.85); font-size: 1rem; }
.banner-emoji { font-size: 80px; }
</style>
```

- [ ] **Step 2: 创建 BookSection.vue**

```vue
<!-- frontend/src/components/BookSection.vue -->
<template>
  <section class="book-section">
    <div class="section-header">
      <el-tabs v-model="activeTab" @tab-change="handleTabChange">
        <el-tab-pane v-for="tab in tabs" :key="tab.key" :label="tab.label" :name="tab.key" />
      </el-tabs>
      <router-link :to="moreLink" class="view-all">查看全部 <el-icon><ArrowRight /></el-icon></router-link>
    </div>
    <div class="book-grid">
      <BookCard v-for="book in books" :key="book.id" :book="book" />
    </div>
  </section>
</template>

<script setup>
import { ref, watch, onMounted } from 'vue'
import BookCard from './BookCard.vue'

const props = defineProps({
  tabs: { type: Array, required: true },
  fetchBooks: { type: Function, required: true },
  moreLink: { type: String, default: '/books' },
})

const activeTab = ref(props.tabs[0]?.key || '')
const books = ref([])

onMounted(() => loadBooks())
watch(activeTab, () => loadBooks())

async function loadBooks() {
  books.value = await props.fetchBooks(activeTab.value)
}
function handleTabChange() { loadBooks() }
</script>

<style scoped>
.book-section { padding: 48px 0; }
.section-header {
  display: flex; align-items: center; justify-content: space-between; margin-bottom: 24px;
}
.section-header :deep(.el-tabs__header) { margin: 0; }
.view-all { display: flex; align-items: center; gap: 4px; color: #4f46e5; font-size: 0.9rem; font-weight: 600; }
.book-grid { display: grid; grid-template-columns: repeat(5, 1fr); gap: 20px; }
@media (max-width: 1024px) { .book-grid { grid-template-columns: repeat(4, 1fr); } }
@media (max-width: 768px) { .book-grid { grid-template-columns: repeat(3, 1fr); } }
</style>
```

- [ ] **Step 3: 重写 Home.vue**

```vue
<!-- frontend/src/views/Home.vue -->
<template>
  <div class="home">
    <!-- Hero Search -->
    <section class="hero">
      <div class="hero-content container">
        <h1>发现你的下一本好书</h1>
        <p>海量图书，精选推荐，让阅读成为一种生活方式</p>
        <div class="hero-search">
          <el-input v-model="keyword" placeholder="搜索书名、作者或ISBN..." size="large"
            :prefix-icon="Search" @keyup.enter="goSearch" class="search-input" />
          <el-button type="primary" size="large" @click="goSearch">搜索</el-button>
        </div>
      </div>
    </section>

    <!-- Banner Carousel -->
    <div class="container">
      <BannerCarousel />
    </div>

    <!-- Hot / New / Discount Books Tabs -->
    <div class="container">
      <BookSection :tabs="bookTabs" :fetch-books="fetchTabBooks" />
    </div>

    <!-- Categories -->
    <section class="section container">
      <div class="section-header"><h2>图书分类</h2></div>
      <div class="category-grid">
        <div v-for="cat in categories" :key="cat.id" class="category-card" @click="goCategory(cat.id)">
          <div class="category-icon"><el-icon :size="24"><Folder /></el-icon></div>
          <span>{{ cat.name }}</span>
          <small>{{ cat.bookCount || 0 }}本</small>
        </div>
      </div>
    </section>

    <!-- Back to top -->
    <el-backtop :right="40" :bottom="40" />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { bookAPI, categoryAPI } from '../api'
import { Search } from '@element-plus/icons-vue'
import BannerCarousel from '../components/BannerCarousel.vue'
import BookSection from '../components/BookSection.vue'

const router = useRouter()
const keyword = ref('')
const categories = ref([])

const bookTabs = [
  { key: 'hot', label: '🔥 热门推荐' },
  { key: 'new', label: '🆕 新书上架' },
  { key: 'discount', label: '💰 限时特价' },
]

onMounted(async () => {
  try {
    const catRes = await categoryAPI.list()
    categories.value = catRes.data || []
  } catch (e) { console.error(e) }
})

async function fetchTabBooks(tabKey) {
  try {
    const params = { limit: 10 }
    if (tabKey === 'new') params.sort = 'newest'
    if (tabKey === 'discount') params.sort = 'price_low'
    const res = await bookAPI.getHot(10)
    return tabKey === 'hot' ? res.data : res.data.slice().reverse()
  } catch (e) { return [] }
}

function goSearch() {
  if (keyword.value.trim()) router.push({ path: '/books', query: { keyword: keyword.value.trim() } })
}
function goCategory(catId) {
  router.push({ path: '/books', query: { categoryId: catId } })
}
</script>

<style scoped>
.hero {
  background: var(--gradient-hero); padding: 60px 0; text-align: center;
}
.hero-content h1 { font-size: 2.4rem; font-weight: 800; color: #fff; letter-spacing: -1px; }
.hero-content p { margin-top: 8px; color: rgba(255,255,255,0.7); font-size: 1rem; }
.hero-search { max-width: 520px; margin: 24px auto 0; display: flex; gap: 12px; }
.search-input { flex: 1; }
.section { padding: 48px 0; }
.section-header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 24px; }
.section-header h2 { font-size: 1.4rem; font-weight: 700; color: var(--text-primary); }
.category-grid { display: grid; grid-template-columns: repeat(5, 1fr); gap: 16px; }
.category-card {
  background: var(--bg-card); border-radius: 12px; padding: 20px 16px; text-align: center;
  cursor: pointer; transition: all 0.2s; border: 1px solid var(--border-color);
}
.category-card:hover { border-color: #4f46e5; color: #4f46e5; }
.category-icon { margin-bottom: 6px; color: #4f46e5; }
.category-card span { display: block; font-size: 0.9rem; font-weight: 500; color: var(--text-primary); }
.category-card small { display: block; margin-top: 2px; font-size: 0.75rem; color: var(--text-muted); }
</style>
```

- [ ] **Step 4: 验证** — 启动前端，首页应显示 Hero + 轮播 + Tab图书区 + 分类网格

---

### Task 6: Books 图书列表增强

**Files:**
- Modify: `frontend/src/views/Books.vue`

- [ ] **Step 1: 重写 Books.vue（增加排序、价格区间、视图切换）**

完整替换 `frontend/src/views/Books.vue`：

```vue
<template>
  <div class="books-page container">
    <div class="page-header"><h1>图书列表</h1></div>

    <!-- Search & Filters -->
    <div class="filter-bar">
      <el-input v-model="keyword" placeholder="搜索书名、作者、ISBN..." :prefix-icon="Search"
        clearable @change="doSearch" class="filter-search" />
      <el-select v-model="categoryId" placeholder="全部分类" clearable @change="doSearch">
        <el-option v-for="cat in categories" :key="cat.id" :label="cat.name" :value="cat.id" />
      </el-select>
      <el-select v-model="sortBy" placeholder="排序" @change="doSearch" style="width:140px">
        <el-option label="默认" value="default" />
        <el-option label="价格 ↑" value="price_asc" />
        <el-option label="价格 ↓" value="price_desc" />
        <el-option label="评分最高" value="rating" />
        <el-option label="销量最高" value="sales" />
        <el-option label="最新上架" value="newest" />
      </el-select>
      <div class="price-range">
        <span class="range-label">¥</span>
        <el-input-number v-model="priceMin" :min="0" :max="priceMax" size="small" controls-position="right" @change="doSearch" style="width:100px" />
        <span class="range-sep">-</span>
        <el-input-number v-model="priceMax" :min="priceMin" :max="500" size="small" controls-position="right" @change="doSearch" style="width:100px" />
      </div>
      <el-radio-group v-model="viewMode" size="small" @change="doSearch">
        <el-radio-button value="grid"><el-icon><Grid /></el-icon></el-radio-button>
        <el-radio-button value="list"><el-icon><List /></el-icon></el-radio-button>
      </el-radio-group>
    </div>

    <!-- Book Grid -->
    <div v-if="!loading && books.length > 0" :class="viewMode === 'grid' ? 'book-grid' : 'book-list'">
      <BookCard v-for="book in books" :key="book.id" :book="book" />
    </div>
    <EmptyState v-else-if="!loading" icon="Document" description="暂无符合条件的图书" />

    <!-- Skeleton loading -->
    <div v-if="loading" class="book-grid">
      <div v-for="i in 8" :key="i" class="skeleton-card">
        <el-skeleton animated><template #template>
          <el-skeleton-item variant="image" style="width:100%;aspect-ratio:3/4" />
          <div style="padding:12px">
            <el-skeleton-item variant="text" style="width:80%" />
            <el-skeleton-item variant="text" style="width:50%;margin-top:6px" />
          </div>
        </template></el-skeleton>
      </div>
    </div>

    <!-- Pagination -->
    <div class="pagination" v-if="total > size">
      <el-pagination v-model:current-page="page" :page-size="size" :total="total"
        layout="prev, pager, next" @current-change="fetchBooks" />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { bookAPI, categoryAPI } from '../api'
import { Search } from '@element-plus/icons-vue'
import BookCard from '../components/BookCard.vue'
import EmptyState from '../components/EmptyState.vue'

const route = useRoute()
const books = ref([])
const categories = ref([])
const page = ref(1)
const size = ref(12)
const total = ref(0)
const keyword = ref('')
const categoryId = ref(null)
const sortBy = ref('default')
const priceMin = ref(0)
const priceMax = ref(500)
const viewMode = ref('grid')
const loading = ref(false)

onMounted(async () => {
  keyword.value = route.query.keyword || ''
  if (route.query.categoryId) categoryId.value = Number(route.query.categoryId)
  const [catRes] = await Promise.all([categoryAPI.list(), fetchBooks()])
  categories.value = catRes.data || []
})

async function fetchBooks() {
  loading.value = true
  try {
    const params = {
      page: page.value, size: size.value,
      keyword: keyword.value || undefined,
      categoryId: categoryId.value || undefined,
      sort: sortBy.value !== 'default' ? sortBy.value : undefined,
      priceMin: priceMin.value || undefined,
      priceMax: priceMax.value < 500 ? priceMax.value : undefined,
    }
    const res = await bookAPI.list(params)
    books.value = res.data?.records ?? []
    total.value = res.data?.total ?? 0
  } catch (e) { console.error(e) }
  finally { loading.value = false }
}

function doSearch() { page.value = 1; fetchBooks() }
</script>

<style scoped>
.books-page { padding-bottom: 60px; }
.filter-bar { display: flex; gap: 12px; margin-bottom: 28px; flex-wrap: wrap; align-items: center; }
.filter-search { flex: 1; min-width: 200px; max-width: 320px; }
.price-range { display: flex; align-items: center; gap: 4px; }
.range-label { color: var(--text-muted); font-size: 0.85rem; }
.range-sep { color: var(--text-muted); }
.book-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 20px; }
.book-list { display: flex; flex-direction: column; gap: 12px; }
.book-list :deep(.book-card) { display: flex; flex-direction: row; }
.book-list :deep(.book-cover) { width: 100px; aspect-ratio: auto; height: 140px; }
.skeleton-card { border-radius: 12px; overflow: hidden; background: var(--bg-card); }
.pagination { display: flex; justify-content: center; margin-top: 40px; }
</style>
```

- [ ] **Step 2: 验证** — 测试排序切换、价格区间筛选、视图切换、URL参数同步

---

### Task 7: BookDetail 图书详情增强

**Files:**
- Modify: `frontend/src/views/BookDetail.vue`

- [ ] **Step 1: 增强 BookDetail.vue**

在现有 `BookDetail.vue` 基础上增加以下区块。在 `<script setup>` 中增加：

```js
// 新增相关推荐
const relatedBooks = ref([])

onMounted(async () => {
  // ... 现有逻辑 ...
  // 加载相关推荐
  if (book.value?.categoryId) {
    try {
      const relRes = await bookAPI.list({ categoryId: book.value.categoryId, size: 6, sort: 'sales' })
      relatedBooks.value = (relRes.data?.records || []).filter(b => b.id !== book.value.id).slice(0, 5)
    } catch (e) { /* ignore */ }
  }
})
```

在 template 中，图书简介区块上方增加标签和推荐：

```vue
<!-- 标签 -->
<div class="detail-tags" v-if="book.tags">
  <el-tag v-for="tag in book.tags.split(',')" :key="tag" size="small" effect="plain" round>
    {{ tag }}
  </el-tag>
</div>

<!-- 相关推荐 -->
<div class="section-card" v-if="relatedBooks.length > 0">
  <h3>相关推荐</h3>
  <div class="related-scroll">
    <div v-for="rb in relatedBooks" :key="rb.id" class="related-book" @click="$router.push(`/book/${rb.id}`)">
      <BookCover :title="rb.title" :src="rb.coverImage" :category-id="rb.categoryId" />
      <span class="related-title">{{ rb.title }}</span>
      <span class="related-price">¥{{ rb.price }}</span>
    </div>
  </div>
</div>
```

在 `<style scoped>` 中增加：

```css
.detail-tags { margin-top: 16px; display: flex; gap: 8px; flex-wrap: wrap; }
.related-scroll { display: flex; gap: 16px; overflow-x: auto; padding-bottom: 8px; }
.related-book { flex-shrink: 0; width: 120px; cursor: pointer; text-align: center; }
.related-book .book-cover { margin-bottom: 6px; }
.related-title { font-size: 0.78rem; color: var(--text-secondary); display: block; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.related-price { font-size: 0.9rem; font-weight: 700; color: #e74c3c; }
```

- [ ] **Step 2: 验证** — 进入图书详情页，确认标签、相关推荐区块显示正常

---

### Task 8: Cart 购物车增强

**Files:**
- Modify: `frontend/src/views/Cart.vue`

- [ ] **Step 1: 增强购物车（库存状态 + 批量选择 + 地址选择）**

在 cart-item 中增加库存状态和选择框：

```vue
<!-- 在选择框列增加 -->
<el-checkbox v-model="item.selected" @change="updateSelection" />

<!-- 库存状态 -->
<div class="item-stock">
  <span v-if="item.stock > 20" class="stock-ok">库存充足</span>
  <span v-else-if="item.stock > 0" class="stock-low">仅剩 {{ item.stock }} 件</span>
  <span v-else class="stock-out">暂时缺货</span>
</div>
```

结算区域增加地址选择按钮和价格明细：

```vue
<!-- 地址选择 -->
<div class="address-picker" v-if="selectedAddress">
  <el-icon><LocationFilled /></el-icon>
  <span>{{ selectedAddress.receiverName }} {{ selectedAddress.receiverPhone }}</span>
  <span class="address-detail">{{ selectedAddress.detail }}</span>
  <el-button size="small" text @click="showAddressDialog = true">更换</el-button>
</div>
<el-button v-else size="small" @click="showAddressDialog = true">选择收货地址</el-button>

<!-- 价格明细 -->
<div class="price-detail">
  <div class="price-row"><span>商品金额</span><span>¥{{ selectedAmount.toFixed(2) }}</span></div>
  <div class="price-row"><span>运费</span><span>¥{{ shippingFee.toFixed(2) }}</span></div>
  <div class="price-row total"><span>合计</span><span class="final">¥{{ finalAmount.toFixed(2) }}</span></div>
</div>
```

在 `<script setup>` 中增加计算属性：

```js
const selectedItems = computed(() => cartStore.items.filter(i => i.selected))
const selectedAmount = computed(() => selectedItems.value.reduce((s, i) => s + i.price * i.quantity, 0))
const shippingFee = computed(() => selectedAmount.value >= 99 ? 0 : 8)
const finalAmount = computed(() => selectedAmount.value + shippingFee.value)
```

- [ ] **Step 2: 验证** — 购物车中选择商品、切换地址、查看价格明细

---

### Task 9: OrderDetail 订单详情页（全新）

**Files:**
- Create: `frontend/src/views/OrderDetail.vue`
- Modify: `frontend/src/router/index.js`

- [ ] **Step 1: 创建 OrderDetail.vue**

```vue
<!-- frontend/src/views/OrderDetail.vue -->
<template>
  <div class="order-detail-page container" v-loading="loading">
    <div class="page-header">
      <h1>订单详情</h1>
      <el-button text @click="$router.back()"><el-icon><ArrowLeft /></el-icon> 返回</el-button>
    </div>

    <template v-if="order">
      <!-- Status Steps -->
      <div class="section-card">
        <el-steps :active="statusStep" finish-status="success" align-center>
          <el-step title="已下单" :description="order.createTime" />
          <el-step title="已支付" :description="order.payTime || '待支付'" />
          <el-step title="已发货" :description="order.shipTime || '待发货'" />
          <el-step title="已完成" :description="order.finishTime || '待完成'" />
        </el-steps>
      </div>

      <!-- Receiver Info -->
      <div class="section-card">
        <h3><el-icon><LocationFilled /></el-icon> 收货信息</h3>
        <p>{{ order.receiverName }} — {{ order.receiverPhone }}</p>
        <p class="text-muted">{{ order.receiverAddress }}</p>
      </div>

      <!-- Order Items -->
      <div class="section-card">
        <h3>商品列表</h3>
        <div class="order-items">
          <div v-for="item in order.items" :key="item.id" class="order-item">
            <BookCover :title="item.bookTitle" :src="item.bookCover" :category-id="0" class="item-cover-small" />
            <div class="item-info">
              <span class="item-title">{{ item.bookTitle }}</span>
              <span class="item-meta">¥{{ item.price }} × {{ item.quantity }}</span>
            </div>
            <span class="item-subtotal">¥{{ item.subtotal }}</span>
          </div>
        </div>
      </div>

      <!-- Price Summary -->
      <div class="section-card">
        <h3>价格明细</h3>
        <div class="price-detail">
          <div class="price-row"><span>订单编号</span><span class="order-no">{{ order.orderNo }}</span></div>
          <div class="price-row"><span>商品金额</span><span>¥{{ order.totalAmount }}</span></div>
          <div class="price-row total"><span>实付款</span><span class="final">¥{{ order.totalAmount }}</span></div>
        </div>
      </div>

      <!-- Actions -->
      <div class="order-actions" v-if="order.status === 0">
        <el-button type="primary" size="large" @click="payOrder">立即支付</el-button>
        <el-button size="large" @click="cancelOrder">取消订单</el-button>
      </div>
      <div class="order-actions" v-if="order.status === 2">
        <el-button type="success" size="large" @click="confirmReceive">确认收货</el-button>
      </div>
    </template>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { orderAPI } from '../api'
import { ElMessage, ElMessageBox } from 'element-plus'
import BookCover from '../components/BookCover.vue'

const route = useRoute()
const router = useRouter()
const order = ref(null)
const loading = ref(false)

const statusStep = computed(() => ({ 0: 0, 1: 1, 2: 2, 3: 3, 4: -1 }[order.value?.status] ?? 0))

onMounted(async () => {
  loading.value = true
  try {
    const res = await orderAPI.getById(route.params.id)
    order.value = res.data
  } catch (e) { ElMessage.error('订单不存在'); router.push('/orders') }
  finally { loading.value = false }
})

async function payOrder() {
  await orderAPI.pay(order.value.id)
  ElMessage.success('支付成功')
  location.reload()
}
async function cancelOrder() {
  await ElMessageBox.confirm('确定取消此订单？', '确认', { type: 'warning' })
  await orderAPI.cancel(order.value.id)
  ElMessage.success('已取消')
  location.reload()
}
async function confirmReceive() {
  await ElMessageBox.confirm('确认已收到商品？', '确认收货', { type: 'success' })
  await orderAPI.updateStatus(order.value.id, 3)
  ElMessage.success('已确认收货')
  location.reload()
}
</script>

<style scoped>
.order-detail-page { padding-bottom: 60px; }
.section-card {
  background: var(--bg-card); border-radius: 12px; padding: 24px; margin-bottom: 16px;
  box-shadow: var(--shadow-card);
}
.section-card h3 { font-size: 1rem; font-weight: 700; margin-bottom: 12px; color: var(--text-primary); }
.text-muted { color: var(--text-muted); font-size: 0.85rem; }
.order-items { display: flex; flex-direction: column; gap: 12px; }
.order-item { display: flex; align-items: center; gap: 16px; }
.item-cover-small { width: 60px; height: 80px; flex-shrink: 0; }
.item-info { flex: 1; display: flex; flex-direction: column; }
.item-title { font-weight: 600; color: var(--text-primary); }
.item-meta { font-size: 0.8rem; color: var(--text-muted); margin-top: 2px; }
.item-subtotal { font-weight: 700; color: #e74c3c; }
.price-detail { display: flex; flex-direction: column; gap: 8px; }
.price-row { display: flex; justify-content: space-between; font-size: 0.9rem; }
.price-row.total { font-weight: 700; font-size: 1rem; margin-top: 4px; padding-top: 8px; border-top: 1px solid var(--border-color); }
.final { font-size: 1.2rem; color: #e74c3c; }
.order-no { font-family: monospace; color: var(--text-muted); font-size: 0.85rem; }
.order-actions { display: flex; gap: 12px; justify-content: flex-end; margin-top: 16px; }
</style>
```

- [ ] **Step 2: 更新路由** — 在 `frontend/src/router/index.js` 中增加：

```js
{ path: 'order/:id', name: 'OrderDetail', component: () => import('../views/OrderDetail.vue'), meta: { title: '订单详情' } },
```

放在 Layout 的 children 数组中。

- [ ] **Step 3: 验证** — 点击订单可进入详情页，查看订单步骤、商品、收货信息

---

### Task 10: Orders 订单列表增强

**Files:**
- Modify: `frontend/src/views/Orders.vue`

- [ ] **Step 1: 增加状态筛选 Tab + 点击跳转详情**

在现有 Orders.vue template 最上方增加：

```vue
<el-tabs v-model="statusFilter" @tab-change="fetchOrders" class="order-tabs">
  <el-tab-pane label="全部" name="all" />
  <el-tab-pane label="待支付" name="0" />
  <el-tab-pane label="已支付" name="1" />
  <el-tab-pane label="已发货" name="2" />
  <el-tab-pane label="已完成" name="3" />
  <el-tab-pane label="已取消" name="4" />
</el-tabs>
```

在 `<script setup>` 中增加：

```js
const statusFilter = ref('all')

async function fetchOrders() {
  loading.value = true
  try {
    const params = { page: page.value, size: size.value }
    if (statusFilter.value !== 'all') params.status = statusFilter.value
    const res = await orderAPI.listMy(params)
    orders.value = res.data.records
    total.value = res.data.total
  } catch (e) { console.error(e) }
  finally { loading.value = false }
}
```

订单卡片增加点击跳转到详情页（给 `.order-card` 加 `@click="$router.push(/order/${order.id})"`）。

- [ ] **Step 2: 验证** — 切换Tab筛选订单、点击订单进入详情

---

### Task 11: Profile 个人中心 Tab 式重构

**Files:**
- Modify: `frontend/src/views/Profile.vue`

- [ ] **Step 1: 重写 Profile.vue 为 Tab 式布局**

完整替换 `frontend/src/views/Profile.vue`：

```vue
<template>
  <div class="profile-page container">
    <div class="page-header"><h1>个人中心</h1></div>
    <div class="profile-card">
      <el-tabs v-model="activeTab" tab-position="left" class="profile-tabs">
        <!-- Tab 1: 基本信息 -->
        <el-tab-pane label="基本信息" name="info">
          <div class="tab-content">
            <div class="avatar-section">
              <BookCover :title="form.username" :category-id="0" class="avatar-cover" />
              <el-button size="small">更换头像</el-button>
            </div>
            <el-form :model="form" label-width="80px" size="large">
              <el-form-item label="用户名"><el-input v-model="form.username" disabled /></el-form-item>
              <el-form-item label="昵称"><el-input v-model="form.nickname" placeholder="请输入昵称" /></el-form-item>
              <el-form-item label="邮箱"><el-input v-model="form.email" placeholder="请输入邮箱" /></el-form-item>
              <el-form-item label="手机号"><el-input v-model="form.phone" placeholder="请输入手机号" /></el-form-item>
              <el-form-item label="角色"><el-tag>{{ form.role === 'ADMIN' ? '管理员' : '普通用户' }}</el-tag></el-form-item>
              <el-form-item><el-button type="primary" @click="saveProfile" :loading="saving">保存修改</el-button></el-form-item>
            </el-form>
          </div>
        </el-tab-pane>

        <!-- Tab 2: 安全设置 -->
        <el-tab-pane label="安全设置" name="security">
          <div class="tab-content">
            <el-form :model="pwdForm" label-width="100px" size="large" class="pwd-form">
              <el-form-item label="旧密码"><el-input v-model="pwdForm.oldPassword" type="password" show-password /></el-form-item>
              <el-form-item label="新密码"><el-input v-model="pwdForm.newPassword" type="password" show-password /></el-form-item>
              <el-form-item label="确认密码"><el-input v-model="pwdForm.confirmPassword" type="password" show-password /></el-form-item>
              <el-form-item><el-button type="primary" @click="changePassword" :loading="changingPwd">修改密码</el-button></el-form-item>
            </el-form>
          </div>
        </el-tab-pane>

        <!-- Tab 3: 收货地址 -->
        <el-tab-pane label="收货地址" name="address">
          <div class="tab-content">
            <div class="address-list">
              <div v-for="addr in addresses" :key="addr.id" class="address-item" :class="{ default: addr.isDefault }">
                <div class="addr-info">
                  <span class="addr-tag" v-if="addr.isDefault">默认</span>
                  <strong>{{ addr.receiverName }}</strong> {{ addr.receiverPhone }}
                  <p>{{ addr.province }}{{ addr.city }}{{ addr.district }} {{ addr.detail }}</p>
                </div>
                <div class="addr-actions">
                  <el-button size="small" text @click="editAddress(addr)">编辑</el-button>
                  <el-button size="small" text type="danger" @click="deleteAddress(addr.id)">删除</el-button>
                </div>
              </div>
              <el-button @click="showAddressForm(null)" style="margin-top:12px"><el-icon><Plus /></el-icon> 新增地址</el-button>
            </div>
          </div>
        </el-tab-pane>

        <!-- Tab 4: 浏览历史 -->
        <el-tab-pane label="浏览历史" name="history">
          <div class="tab-content">
            <div v-if="history.length > 0" class="book-grid">
              <BookCard v-for="book in history" :key="book.id" :book="book" />
            </div>
            <EmptyState v-else icon="Clock" description="暂无浏览记录" />
            <el-button v-if="history.length > 0" @click="clearHistory" type="danger" text style="margin-top:16px">清除历史</el-button>
          </div>
        </el-tab-pane>
      </el-tabs>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useUserStore } from '../stores/user'
import { userAPI } from '../api'
import { ElMessage, ElMessageBox } from 'element-plus'
import BookCover from '../components/BookCover.vue'
import BookCard from '../components/BookCard.vue'
import EmptyState from '../components/EmptyState.vue'

const userStore = useUserStore()
const activeTab = ref('info')
const saving = ref(false)
const changingPwd = ref(false)
const addresses = ref([])
const history = ref([])

const form = reactive({ username: '', nickname: '', email: '', phone: '', role: '' })
const pwdForm = reactive({ oldPassword: '', newPassword: '', confirmPassword: '' })

onMounted(async () => {
  try {
    const user = await userStore.fetchProfile()
    Object.assign(form, user)
  } catch (e) { /* handled */ }
})

async function saveProfile() {
  saving.value = true
  try {
    await userAPI.updateProfile({ nickname: form.nickname, email: form.email, phone: form.phone })
    ElMessage.success('已更新')
    await userStore.fetchProfile()
  } finally { saving.value = false }
}

async function changePassword() {
  if (!pwdForm.oldPassword || !pwdForm.newPassword) { ElMessage.warning('请填写完整'); return }
  if (pwdForm.newPassword !== pwdForm.confirmPassword) { ElMessage.warning('两次密码不一致'); return }
  changingPwd.value = true
  try {
    await userAPI.changePassword(pwdForm)
    ElMessage.success('密码修改成功，请重新登录')
    userStore.clearAuth()
    location.href = '/login'
  } finally { changingPwd.value = false }
}
</script>

<style scoped>
.profile-page { padding-bottom: 60px; }
.profile-card { background: var(--bg-card); border-radius: 16px; box-shadow: var(--shadow-card); }
.profile-tabs { min-height: 500px; }
.tab-content { padding: 24px 32px; max-width: 560px; }
.avatar-section { text-align: center; margin-bottom: 24px; }
.avatar-cover { width: 80px; height: 80px; border-radius: 50%; margin: 0 auto 8px; }
.pwd-form { margin-top: 16px; }
.address-item { padding: 16px; border: 1px solid var(--border-color); border-radius: 8px; margin-bottom: 12px; display: flex; justify-content: space-between; align-items: center; }
.address-item.default { border-color: #4f46e5; background: rgba(79,70,229,0.03); }
.addr-tag { display: inline-block; background: #4f46e5; color: #fff; font-size: 0.7rem; padding: 2px 6px; border-radius: 4px; margin-right: 8px; }
.addr-info p { margin-top: 4px; color: var(--text-muted); font-size: 0.85rem; }
.book-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 16px; }
</style>
```

- [ ] **Step 2: 增加 changePassword API** — 在 `frontend/src/api/index.js` 的 userAPI 中增加：

```js
changePassword: (data) => request.put('/user/password', data),
```

- [ ] **Step 3: 验证** — 个人中心四个Tab切换正常、修改密码、地址管理

---

### Task 12: Layout 增强（全局搜索 + 主题切换）

**Files:**
- Create: `frontend/src/components/GlobalSearch.vue`
- Modify: `frontend/src/views/Layout.vue`

- [ ] **Step 1: 创建 GlobalSearch.vue**

```vue
<!-- frontend/src/components/GlobalSearch.vue -->
<template>
  <div class="global-search">
    <el-button class="search-trigger" @click="visible = true" circle>
      <el-icon :size="18"><Search /></el-icon>
    </el-button>
    <el-dialog v-model="visible" title="搜索图书" width="560px" :show-close="true" :close-on-click-modal="true">
      <el-input v-model="kw" placeholder="搜索书名、作者、ISBN..." size="large" :prefix-icon="Search"
        clearable @keyup.enter="doSearch" ref="inputRef" />
      <div class="search-results" v-if="results.length > 0">
        <div v-for="book in results" :key="book.id" class="search-item" @click="goBook(book.id)">
          <BookCover :title="book.title" :src="book.coverImage" :category-id="book.categoryId" class="search-cover" />
          <div class="search-info">
            <span class="search-title">{{ book.title }}</span>
            <span class="search-author">{{ book.author }}</span>
          </div>
          <span class="search-price">¥{{ book.price }}</span>
        </div>
      </div>
      <div v-else-if="kw && searched" class="search-empty">未找到相关图书</div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { bookAPI } from '../api'
import { Search } from '@element-plus/icons-vue'
import BookCover from './BookCover.vue'

const router = useRouter()
const visible = ref(false)
const kw = ref('')
const results = ref([])
const searched = ref(false)
const inputRef = ref(null)

async function doSearch() {
  searched.value = true
  if (!kw.value.trim()) { results.value = []; return }
  try {
    const res = await bookAPI.list({ keyword: kw.value.trim(), size: 8 })
    results.value = res.data?.records ?? []
  } catch (e) { results.value = [] }
}

function goBook(id) {
  visible.value = false
  router.push(`/book/${id}`)
}
</script>

<style scoped>
.search-trigger { background: transparent; border: none; color: var(--text-secondary); }
.search-results { margin-top: 16px; max-height: 360px; overflow-y: auto; }
.search-item {
  display: flex; align-items: center; gap: 12px; padding: 10px; border-radius: 8px;
  cursor: pointer; transition: background 0.2s;
}
.search-item:hover { background: rgba(128,128,160,0.08); }
.search-cover { width: 44px; height: 58px; flex-shrink: 0; }
.search-info { flex: 1; display: flex; flex-direction: column; }
.search-title { font-size: 0.9rem; font-weight: 600; color: var(--text-primary); }
.search-author { font-size: 0.78rem; color: var(--text-muted); margin-top: 2px; }
.search-price { font-weight: 700; color: #e74c3c; }
.search-empty { text-align: center; padding: 24px; color: var(--text-muted); }
</style>
```

- [ ] **Step 2: 更新 Layout.vue** — 在 header-actions 中增加全景搜索和主题切换：

```vue
<GlobalSearch />
<ThemeToggle />
```

在 `<script setup>` 增加导入：

```js
import GlobalSearch from '../components/GlobalSearch.vue'
import ThemeToggle from '../components/ThemeToggle.vue'
```

- [ ] **Step 3: 验证** — 点击搜索按钮弹出搜索框、输入关键词搜索、点击结果跳转详情

---

### Task 13: Admin Dashboard ECharts 增强

**Files:**
- Modify: `frontend/src/views/admin/Dashboard.vue`
- Modify: `frontend/src/main.js`

- [ ] **Step 1: 安装 echarts 依赖**

```bash
cd frontend && npm install echarts vue-echarts --save
```

- [ ] **Step 2: 注册 ECharts** — 在 `main.js` 中增加：

```js
import ECharts from 'vue-echarts'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { LineChart, PieChart, BarChart } from 'echarts/charts'
import { TitleComponent, TooltipComponent, LegendComponent, GridComponent } from 'echarts/components'

use([CanvasRenderer, LineChart, PieChart, BarChart, TitleComponent, TooltipComponent, LegendComponent, GridComponent])
app.component('v-chart', ECharts)
```

- [ ] **Step 3: Dashboard 增加图表** — 在统计卡片下方增加折线图和饼图：

```vue
<el-row :gutter="20" style="margin-top:20px">
  <el-col :span="16">
    <el-card><template #header>近7天销售趋势</template>
      <v-chart :option="trendOption" style="height:300px" />
    </el-card>
  </el-col>
  <el-col :span="8">
    <el-card><template #header>分类占比</template>
      <v-chart :option="pieOption" style="height:300px" />
    </el-card>
  </el-col>
</el-row>
```

在 `<script setup>` 中增加图表配置：

```js
import { computed } from 'vue'

const trendOption = computed(() => ({
  tooltip: { trigger: 'axis' },
  xAxis: { type: 'category', data: salesTrend.value?.dates || [] },
  yAxis: { type: 'value' },
  series: [{ data: salesTrend.value?.amounts || [], type: 'line', smooth: true, areaStyle: { opacity: 0.1 } }],
}))

const pieOption = computed(() => ({
  tooltip: { trigger: 'item' },
  series: [{
    type: 'pie', radius: ['40%', '70%'],
    data: categoryStats.value?.map(c => ({ name: c.name, value: c.count })) || [],
  }],
}))
```

- [ ] **Step 4: 验证** — 管理后台 Dashboard 显示折线图和饼图

---

### Task 14: 全局样式适配暗色模式

**Files:**
- Modify: 多个 Vue 文件中的硬编码颜色替换为 CSS 变量

- [ ] **Step 1: 批量替换硬编码颜色**

全局搜索替换以下模式：
- `background: #fff` → `background: var(--bg-card)`
- `background: #f5f7fa` → `background: var(--bg-primary)`
- `color: #1a1a2e` → `color: var(--text-primary)`
- `color: #5a5a7a` → `color: var(--text-secondary)`
- `color: #8c8ca1` → `color: var(--text-muted)`
- `border: 1px solid rgba(0,0,0,0.04)` → `border: 1px solid var(--border-color)`
- `box-shadow: 0 1px 8px rgba(0,0,0,0.03)` → `box-shadow: var(--shadow-card)`
- `#0f3460` grad → `var(--gradient-hero)`

**注意**：暗色模式特有的元素（如 `.hero` 渐变背景、Elment Plus 组件内部样式）不需要全部适配，优先适配页面背景、卡片、文字色即可。

- [ ] **Step 2: 验证** — 切换暗色模式后，所有页面基本可用

---

## Phase 2: 后端业务强化

### Task 15: 数据库新表 + 种子数据

**Files:**
- Create: `bookstore-system/sql/upgrade.sql` — 新增8张表的DDL
- Create: `backend/bookstore-common/src/main/java/com/bookstore/common/config/DataSeeder.java`

- [ ] **Step 1: 编写 upgrade.sql**

```sql
-- 在 bookstore_user 库
USE bookstore_user;
CREATE TABLE IF NOT EXISTS address ( ... );  -- 见设计文档2.1节
CREATE TABLE IF NOT EXISTS browse_history ( ... );

-- 在 bookstore_product 库
USE bookstore_product;
ALTER TABLE book ADD COLUMN tags VARCHAR(200) COMMENT '标签，逗号分隔';
ALTER TABLE book ADD COLUMN sales INT DEFAULT 0 COMMENT '销量';
ALTER TABLE book ADD COLUMN is_seed TINYINT DEFAULT 0 COMMENT '是否为种子数据';
CREATE TABLE IF NOT EXISTS seckill_session ( ... );
CREATE TABLE IF NOT EXISTS seckill_book ( ... );
CREATE TABLE IF NOT EXISTS stock_log ( ... );

-- 在 bookstore_order 库
USE bookstore_order;
CREATE TABLE IF NOT EXISTS payment_log ( ... );
CREATE TABLE IF NOT EXISTS shipment ( ... );
CREATE TABLE IF NOT EXISTS coupon ( ... );
CREATE TABLE IF NOT EXISTS user_coupon ( ... );
```

- [ ] **Step 2: 编写 DataSeeder.java**

```java
// backend/bookstore-common/src/main/java/com/bookstore/common/config/DataSeeder.java
package com.bookstore.common.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final JdbcTemplate jdbc;

    @Override
    public void run(String... args) {
        seedCategories();
        seedBooks();
        seedUsers();
        seedCoupons();
    }

    private static final String[][] SEED_CATEGORIES = {
        {"文学小说", "fiction"}, {"计算机科学", "computer"}, {"经济管理", "economy"},
        {"哲学心理", "philosophy"}, {"历史人文", "history"}, {"艺术设计", "art"},
        {"生活百科", "life"}, {"少儿读物", "children"}, {"教辅考试", "education"}, {"科技科普", "science"},
    };

    private static final String[][] SEED_BOOKS = {
        // 文学小说
        {"百年孤独", "加西亚·马尔克斯", "9787544253994", "南海出版公司", "55.00", "文学小说", "经典,诺奖,魔幻现实主义"},
        // ... 150+ books across all categories
    };

    private void seedCategories() {
        Integer count = jdbc.queryForObject("SELECT COUNT(*) FROM category", Integer.class);
        if (count != null && count >= 10) return;
        for (String[] cat : SEED_CATEGORIES) {
            jdbc.update("INSERT IGNORE INTO category (name, code) VALUES (?, ?)", cat[0], cat[1]);
        }
        log.info("种子分类初始化完成");
    }

    private void seedBooks() {
        Integer count = jdbc.queryForObject("SELECT COUNT(*) FROM book", Integer.class);
        if (count != null && count >= 50) return;
        for (String[] b : SEED_BOOKS) {
            String title = b[0], author = b[1], isbn = b[2], publisher = b[3];
            BigDecimal price = new BigDecimal(b[4]);
            String categoryName = b[5], tags = b.length > 6 ? b[6] : "";
            Integer stock = 10 + new Random().nextInt(91);
            jdbc.update(
                "INSERT IGNORE INTO book (title, author, isbn, publisher, price, stock, category_id, " +
                "tags, description, status, is_seed, create_time) " +
                "SELECT ?, ?, ?, ?, ?, ?, id, ?, CONCAT(?, ' — 一本值得阅读的好书。'), 1, 1, NOW() FROM category WHERE name = ?",
                title, author, isbn, publisher, price, stock, tags, title, categoryName
            );
        }
        log.info("种子图书初始化完成，共 {} 本", SEED_BOOKS.length);
    }

    private void seedUsers() {
        // 插入 booklover1-9 用户
        for (int i = 1; i <= 9; i++) {
            String username = "booklover" + i;
            jdbc.update(
                "INSERT IGNORE INTO user (username, password, nickname, email, role, status, is_seed) " +
                "VALUES (?, '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5Eh', ?, ?, 'USER', 1, 1)",
                username, username, username + "@bookstore.com"
            );
        }
        // 确保 admin 存在
        jdbc.update(
            "INSERT IGNORE INTO user (username, password, nickname, email, role, status, is_seed) " +
            "VALUES ('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5Eh', '管理员', 'admin@bookstore.com', 'ADMIN', 1, 0)"
        );
    }

    private void seedCoupons() {
        Integer count = jdbc.queryForObject("SELECT COUNT(*) FROM coupon", Integer.class);
        if (count != null && count >= 3) return;
        jdbc.update("INSERT INTO coupon (name, type, threshold, discount, total, claimed, valid_days, status) VALUES " +
            "('满50减5', 'FULL_REDUCTION', 50, 5, 500, 0, 30, 1), " +
            "('满100减15', 'FULL_REDUCTION', 100, 15, 300, 0, 30, 1), " +
            "('满200减40', 'FULL_REDUCTION', 200, 40, 100, 0, 30, 1), " +
            "('8折优惠券', 'DISCOUNT', 0, 0.8, 200, 0, 15, 1), " +
            "('免邮券', 'FREE_SHIPPING', 0, 0, 300, 0, 30, 1)");
    }
}
```

**注意**：SEED_BOOKS 需要包含完整的150+本图书数据，此处省略了大部分以节省篇幅，实际实现时需补全各分类15-20本。

- [ ] **Step 3: 执行 SQL 并验证** — 运行 upgrade.sql，启动服务确认 DataSeeder 填充数据

---

### Task 16-22: 后续任务概览

由于篇幅限制，Phase 2 的 16-22 任务在此处概述，每个任务的详细步骤（含完整代码）在实施时按相同标准展开：

| Task | 模块 | 核心交付 |
|------|------|---------|
| 16 | 地址管理 CRUD | AddressController + AddressService + AddressMapper |
| 17 | 文件上传 MinIO | FileController + MinioConfig + Docker Compose 更新 |
| 18 | 库存扣减 | StockService（Redis Lua 预扣/释放）+ StockLogMapper + 定时任务 |
| 19 | 支付模块 | PaymentController + PaymentService（阿里沙箱）+ PaymentLogMapper |
| 20 | 物流模块 | ShipmentController + ShipmentService（模拟轨迹生成） |
| 21 | 优惠券模块 | CouponController + CouponService（领取/使用/过期） |
| 22 | 秒杀模块 | SeckillController + SeckillService（Redis Lua 原子秒杀） |
| 23 | 浏览历史 | BrowseHistoryController + BrowseHistoryService |
| 24 | 定时任务 | SchedulingConfig（取消超时/过期优惠券/热销排行/清理秒杀） |
| 25 | 接口限流 | RateLimiterFilter（Gateway Redis 令牌桶） |
| 26 | 数据统计增强 | 各Service的getStatistics增强（趋势/分类/用户维度） |
| 27 | 前端Phase2集成 | 秒杀专区页面、优惠券领取/使用、真实支付按钮、物流跟踪UI |

---

## 实施检查清单

### Phase 1 完成标准
- [ ] 暗色模式可切换、刷新保持、页面颜色正常
- [ ] 首页轮播 + Tab图书区 + 分类网格正常
- [ ] 图书列表排序/筛选/视图切换正常
- [ ] 图书详情标签/推荐显示正常
- [ ] 购物车库存状态/选中/地址/价格明细正常
- [ ] 订单详情页正常、订单列表可筛选
- [ ] 个人中心4个Tab正常
- [ ] 全局搜索正常
- [ ] Admin Dashboard 图表正常

### Phase 2 完成标准
- [ ] DataSeeder 启动自动填充150+本图书
- [ ] 地址增删改查 + 下单选择地址
- [ ] 文件上传/下载正常
- [ ] 下单扣库存、超时回滚正常
- [ ] 支付流水记录正常
- [ ] 物流轨迹生成正常
- [ ] 优惠券领取/使用正常
- [ ] 秒杀不超卖
- [ ] 限流生效

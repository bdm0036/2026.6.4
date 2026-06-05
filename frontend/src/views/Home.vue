<template>
  <div class="home">
    <!-- Hero -->
    <section class="hero">
      <div class="hero-content container">
        <h1>发现你的下一本好书</h1>
        <p>海量图书，精选推荐，让阅读成为一种生活方式</p>
        <div class="hero-search">
          <el-input
            v-model="keyword"
            placeholder="搜索书名、作者或ISBN..."
            size="large"
            :prefix-icon="Search"
            @keyup.enter="goSearch"
            class="search-input"
          />
          <el-button type="primary" size="large" @click="goSearch">搜索</el-button>
        </div>
      </div>
    </section>

    <!-- Hot Books -->
    <section class="section container">
      <div class="section-header">
        <h2>热门推荐</h2>
        <router-link to="/books" class="view-all">查看全部 <el-icon><ArrowRight /></el-icon></router-link>
      </div>
      <div class="book-grid">
        <div v-for="book in hotBooks" :key="book.id" class="book-card" @click="$router.push(`/book/${book.id}`)">
          <div class="book-cover">
            <img v-if="book.coverImage" :src="book.coverImage" :alt="book.title" />
            <div v-else class="cover-placeholder">
              <el-icon :size="40"><Reading /></el-icon>
            </div>
          </div>
          <div class="book-info">
            <h3>{{ book.title }}</h3>
            <p class="book-author">{{ book.author }}</p>
            <div class="book-rating" v-if="book.avgRating">
              <el-rate v-model="book.avgRating" disabled size="small" :colors="['#f59e0b','#f59e0b','#f59e0b']" />
              <span class="rating-num">{{ book.avgRating }}</span>
            </div>
            <div class="book-bottom">
              <span class="book-price">¥{{ book.price }}</span>
              <el-button size="small" type="primary" circle @click.stop="addToCart(book)">
                <el-icon><Plus /></el-icon>
              </el-button>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- Categories -->
    <section class="section container">
      <div class="section-header">
        <h2>图书分类</h2>
      </div>
      <div class="category-grid">
        <div v-for="cat in categories" :key="cat.id" class="category-card" @click="goCategory(cat.id)">
          <div class="category-icon">
            <el-icon :size="28"><Folder /></el-icon>
          </div>
          <span>{{ cat.name }}</span>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useCartStore } from '../stores/cart'
import { bookAPI, categoryAPI } from '../api'
import { Search } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const router = useRouter()
const cartStore = useCartStore()
const keyword = ref('')
const hotBooks = ref([])
const categories = ref([])

onMounted(async () => {
  try {
    const [bookRes, catRes] = await Promise.all([
      bookAPI.getHot(8),
      categoryAPI.list(),
    ])
    hotBooks.value = bookRes.data
    categories.value = catRes.data
  } catch (e) {
    console.error(e)
  }
})

function goSearch() {
  if (keyword.value.trim()) {
    router.push({ path: '/books', query: { keyword: keyword.value.trim() } })
  }
}

function goCategory(catId) {
  router.push({ path: '/books', query: { categoryId: catId } })
}

function addToCart(book) {
  cartStore.addItem(book)
  ElMessage.success(`已添加《${book.title}》到购物车`)
}
</script>

<style scoped>
.hero {
  background: linear-gradient(135deg, #1a1a2e 0%, #16213e 50%, #0f3460 100%);
  padding: 80px 0;
  text-align: center;
}
.hero-content h1 {
  font-size: 2.8rem;
  font-weight: 800;
  color: #fff;
  letter-spacing: -1px;
}
.hero-content p {
  margin-top: 12px;
  color: rgba(255, 255, 255, 0.7);
  font-size: 1.1rem;
}
.hero-search {
  max-width: 560px;
  margin: 32px auto 0;
  display: flex;
  gap: 12px;
}
.hero-search .search-input {
  flex: 1;
}
.section {
  padding: 60px 0;
}
.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 28px;
}
.section-header h2 {
  font-size: 1.5rem;
  font-weight: 700;
}
.view-all {
  display: flex;
  align-items: center;
  gap: 4px;
  color: #4f46e5;
  font-size: 0.9rem;
  font-weight: 600;
}
.book-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 24px;
}
.book-card {
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
  cursor: pointer;
  transition: transform 0.2s, box-shadow 0.2s;
  border: 1px solid rgba(0, 0, 0, 0.04);
}
.book-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.1);
}
.book-cover {
  aspect-ratio: 3/4;
  background: #f0f0f8;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}
.book-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.cover-placeholder {
  color: #c0c0d0;
}
.book-info {
  padding: 16px;
}
.book-info h3 {
  font-size: 0.95rem;
  font-weight: 600;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.book-author {
  margin-top: 4px;
  font-size: 0.8rem;
  color: #8c8ca1;
}
.book-bottom {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 12px;
}
.book-price {
  font-size: 1.1rem;
  font-weight: 700;
  color: #e74c3c;
}
.category-grid {
  display: grid;
  grid-template-columns: repeat(6, 1fr);
  gap: 16px;
}
.category-card {
  background: #fff;
  border-radius: 12px;
  padding: 24px 16px;
  text-align: center;
  cursor: pointer;
  transition: all 0.2s;
  border: 1px solid rgba(0, 0, 0, 0.04);
}
.category-card:hover {
  border-color: #4f46e5;
  color: #4f46e5;
}
.category-icon {
  margin-bottom: 8px;
  color: #4f46e5;
}
.category-card span {
  font-size: 0.9rem;
  font-weight: 500;
}
.book-rating { display: flex; align-items: center; gap: 4px; margin-top: 6px; }
.rating-num { font-size: 0.8rem; color: #f59e0b; font-weight: 600; }
</style>

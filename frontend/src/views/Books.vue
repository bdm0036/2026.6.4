<template>
  <div class="books-page container">
    <div class="page-header">
      <h1>图书列表</h1>
      <p>探索我们的图书收藏</p>
    </div>

    <!-- Search & Filter -->
    <div class="filter-bar">
      <el-input v-model="keyword" placeholder="搜索书名、作者..." :prefix-icon="Search" clearable @change="doSearch" class="filter-search" />
      <el-select v-model="categoryId" placeholder="全部分类" clearable @change="doSearch" class="filter-select">
        <el-option v-for="cat in categories" :key="cat.id" :label="cat.name" :value="cat.id" />
      </el-select>
    </div>

    <!-- Book List -->
    <div class="book-grid">
      <div v-for="book in books" :key="book.id" class="book-card" @click="$router.push(`/book/${book.id}`)">
        <div class="book-cover">
          <img v-if="book.coverImage" :src="book.coverImage" :alt="book.title" />
          <div v-else class="cover-placeholder"><el-icon :size="40"><Reading /></el-icon></div>
        </div>
        <div class="book-info">
          <h3>{{ book.title }}</h3>
          <p class="book-author">{{ book.author }}</p>
          <p class="book-category" v-if="book.categoryName">{{ book.categoryName }}</p>
          <div class="book-bottom">
            <span class="book-price">¥{{ book.price }}</span>
            <span class="book-stock">库存: {{ book.stock }}</span>
          </div>
        </div>
      </div>
    </div>

    <div v-if="books.length === 0 && !loading" class="empty">
      <el-icon :size="60"><Document /></el-icon>
      <p>暂无图书</p>
    </div>

    <!-- Pagination -->
    <div class="pagination" v-if="total > size">
      <el-pagination
        v-model:current-page="page"
        :page-size="size"
        :total="total"
        layout="prev, pager, next"
        @current-change="fetchBooks"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { bookAPI, categoryAPI } from '../api'
import { Search } from '@element-plus/icons-vue'

const route = useRoute()
const books = ref([])
const categories = ref([])
const page = ref(1)
const size = ref(12)
const total = ref(0)
const keyword = ref('')
const categoryId = ref(null)
const loading = ref(false)

onMounted(async () => {
  // 从URL获取搜索参数
  keyword.value = route.query.keyword || ''
  categoryId.value = route.query.categoryId ? Number(route.query.categoryId) : null

  const [catRes] = await Promise.all([
    categoryAPI.list(),
    fetchBooks(),
  ])
  categories.value = catRes.data
})

async function fetchBooks() {
  loading.value = true
  try {
    const res = await bookAPI.list({
      page: page.value,
      size: size.value,
      keyword: keyword.value || undefined,
      categoryId: categoryId.value || undefined,
    })
    books.value = res.data.records
    total.value = res.data.total
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

function doSearch() {
  page.value = 1
  fetchBooks()
}
</script>

<style scoped>
.books-page {
  padding-bottom: 60px;
}
.filter-bar {
  display: flex;
  gap: 16px;
  margin-bottom: 32px;
}
.filter-search {
  flex: 1;
  max-width: 400px;
}
.filter-select {
  width: 180px;
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
.book-cover img { width: 100%; height: 100%; object-fit: cover; }
.cover-placeholder { color: #c0c0d0; }
.book-info { padding: 16px; }
.book-info h3 {
  font-size: 0.95rem; font-weight: 600;
  overflow: hidden; text-overflow: ellipsis; white-space: nowrap;
}
.book-author { margin-top: 4px; font-size: 0.8rem; color: #8c8ca1; }
.book-category { margin-top: 2px; font-size: 0.78rem; color: #4f46e5; }
.book-bottom { display: flex; align-items: center; justify-content: space-between; margin-top: 12px; }
.book-price { font-size: 1.1rem; font-weight: 700; color: #e74c3c; }
.book-stock { font-size: 0.78rem; color: #8c8ca1; }
.empty { text-align: center; padding: 80px 0; color: #c0c0d0; }
.empty p { margin-top: 12px; font-size: 1rem; }
.pagination { display: flex; justify-content: center; margin-top: 40px; }
</style>

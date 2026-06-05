<template>
  <div class="favorites-page container">
    <div class="page-header">
      <h1>我的收藏</h1>
      <p>{{ favorites.length }} 本图书</p>
    </div>

    <div v-if="favorites.length === 0 && !loading" class="empty">
      <el-icon :size="64"><Star /></el-icon>
      <p>还没有收藏图书</p>
      <el-button type="primary" @click="$router.push('/books')">去逛逛</el-button>
    </div>

    <div class="book-grid" v-else>
      <div v-for="book in favorites" :key="book.id" class="book-card" @click="$router.push(`/book/${book.id}`)">
        <div class="book-cover">
          <img v-if="book.coverImage" :src="book.coverImage" :alt="book.title" />
          <div v-else class="cover-placeholder"><el-icon :size="40"><Reading /></el-icon></div>
        </div>
        <div class="book-info">
          <h3>{{ book.title }}</h3>
          <p class="book-author">{{ book.author }}</p>
          <p class="book-category" v-if="book.categoryName">{{ book.categoryName }}</p>
          <div class="book-rating" v-if="book.avgRating">
            <el-rate v-model="book.avgRating" disabled size="small" :colors="['#f59e0b','#f59e0b','#f59e0b']" />
            <span class="rating-num">{{ book.avgRating }}</span>
          </div>
          <div class="book-bottom">
            <span class="book-price">¥{{ book.price }}</span>
            <el-button size="small" type="danger" text @click.stop="removeFavorite(book.id)">
              <el-icon><Delete /></el-icon> 取消收藏
            </el-button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { favoriteAPI } from '../api'
import { ElMessage } from 'element-plus'

const router = useRouter()
const favorites = ref([])
const loading = ref(false)

onMounted(() => fetchFavorites())

async function fetchFavorites() {
  loading.value = true
  try {
    const res = await favoriteAPI.list({ page: 1, size: 50 })
    favorites.value = res.data || []
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

async function removeFavorite(bookId) {
  try {
    await favoriteAPI.toggle(bookId)
    ElMessage.success('已取消收藏')
    fetchFavorites()
  } catch (e) { /* handled */ }
}
</script>

<style scoped>
.favorites-page { padding-bottom: 60px; }
.empty { text-align: center; padding: 80px 0; color: #c0c0d0; }
.empty p { margin: 12px 0 16px; font-size: 1.1rem; }
.book-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 24px;
}
.book-card {
  background: #fff; border-radius: 12px; overflow: hidden; cursor: pointer;
  transition: transform 0.2s, box-shadow 0.2s;
  border: 1px solid rgba(0, 0, 0, 0.04);
}
.book-card:hover { transform: translateY(-4px); box-shadow: 0 12px 40px rgba(0, 0, 0, 0.1); }
.book-cover {
  aspect-ratio: 3/4; background: #f0f0f8;
  display: flex; align-items: center; justify-content: center; overflow: hidden;
}
.book-cover img { width: 100%; height: 100%; object-fit: cover; }
.cover-placeholder { color: #c0c0d0; }
.book-info { padding: 16px; }
.book-info h3 { font-size: 0.95rem; font-weight: 600; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.book-author { margin-top: 4px; font-size: 0.8rem; color: #8c8ca1; }
.book-category { margin-top: 2px; font-size: 0.78rem; color: #4f46e5; }
.book-rating { display: flex; align-items: center; gap: 4px; margin-top: 6px; }
.rating-num { font-size: 0.8rem; color: #f59e0b; font-weight: 600; }
.book-bottom { display: flex; align-items: center; justify-content: space-between; margin-top: 12px; }
.book-price { font-size: 1.1rem; font-weight: 700; color: #e74c3c; }
</style>

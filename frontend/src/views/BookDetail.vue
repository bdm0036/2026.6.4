<template>
  <div class="book-detail container" v-if="book">
    <div class="detail-card">
      <div class="detail-cover">
        <img v-if="book.coverImage" :src="book.coverImage" :alt="book.title" />
        <div v-else class="cover-placeholder"><el-icon :size="64"><Reading /></el-icon></div>
      </div>
      <div class="detail-info">
        <p class="detail-category" v-if="book.categoryName">{{ book.categoryName }}</p>
        <h1>{{ book.title }}</h1>
        <p class="detail-author">作者：{{ book.author }}</p>
        <p class="detail-meta">出版社：{{ book.publisher }} | ISBN：{{ book.isbn || '暂无' }}</p>

        <!-- Rating Stars -->
        <div class="detail-rating" v-if="book.avgRating">
          <el-rate v-model="book.avgRating" disabled :colors="['#f59e0b','#f59e0b','#f59e0b']" />
          <span class="rating-text">{{ book.avgRating }} 分 ({{ book.ratingCount }}人评价)</span>
        </div>

        <div class="detail-price">¥{{ book.price }}</div>
        <p class="detail-stock" :class="{ low: book.stock < 10 }">
          库存：{{ book.stock > 0 ? book.stock + ' 本' : '暂时缺货' }}
        </p>
        <div class="detail-actions">
          <el-input-number v-model="quantity" :min="1" :max="book.stock" size="large" />
          <el-button type="primary" size="large" @click="addToCart" :disabled="book.stock === 0">
            <el-icon><ShoppingCart /></el-icon> 加入购物车
          </el-button>
          <el-button size="large" @click="buyNow" :disabled="book.stock === 0">
            立即购买
          </el-button>
          <el-button size="large" @click="toggleFavorite" :type="book.favorited ? 'warning' : 'default'">
            <el-icon><StarFilled v-if="book.favorited" /><Star v-else /></el-icon>
            {{ book.favorited ? '已收藏' : '收藏' }} ({{ book.favoriteCount || 0 }})
          </el-button>
        </div>
      </div>
    </div>

    <!-- My Rating -->
    <div class="section-card" v-if="userStore.isLoggedIn()">
      <h3>我的评分</h3>
      <div class="my-rating">
        <el-rate v-model="myRating" :colors="['#f59e0b','#f59e0b','#f59e0b']" @change="submitRating" show-text :texts="['很差','较差','一般','推荐','力荐']" />
        <span class="my-rating-hint" v-if="!myRating">点击星星评分</span>
      </div>
    </div>

    <!-- Reviews -->
    <div class="section-card">
      <h3>读者评论 ({{ reviewTotal }})</h3>
      <!-- Add Review -->
      <div class="add-review" v-if="userStore.isLoggedIn()">
        <el-input v-model="newReview" type="textarea" :rows="3" placeholder="写下你的评论..." maxlength="500" show-word-limit />
        <el-button type="primary" @click="submitReview" :disabled="!newReview.trim()" style="margin-top:8px">发表评论</el-button>
      </div>
      <div v-else class="add-review-hint">
        <router-link to="/login">登录</router-link> 后即可发表评论
      </div>
      <!-- Review List -->
      <div class="review-list" v-if="reviews.length > 0">
        <div v-for="r in reviews" :key="r.id" class="review-item">
          <div class="review-header">
            <span class="review-user">{{ r.username }}</span>
            <span class="review-time">{{ r.createTime }}</span>
            <el-button v-if="userStore.profile?.id === r.userId" size="small" text type="danger" @click="deleteReview(r.id)">删除</el-button>
          </div>
          <p class="review-content">{{ r.content }}</p>
        </div>
        <el-pagination
          v-if="reviewTotal > reviewSize"
          v-model:current-page="reviewPage"
          :page-size="reviewSize"
          :total="reviewTotal"
          layout="prev, pager, next"
          @current-change="fetchReviews"
          small
          style="margin-top:16px;justify-content:center"
        />
      </div>
      <div v-else class="no-reviews">暂无评论，来写第一条吧</div>
    </div>

    <!-- Description -->
    <div class="detail-description">
      <h2>图书简介</h2>
      <p>{{ book.description || '暂无简介' }}</p>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useCartStore } from '../stores/cart'
import { useUserStore } from '../stores/user'
import { bookAPI, ratingAPI, reviewAPI, favoriteAPI, historyAPI } from '../api'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()
const cartStore = useCartStore()
const userStore = useUserStore()
const book = ref(null)
const quantity = ref(1)
const myRating = ref(0)
const reviews = ref([])
const reviewPage = ref(1)
const reviewSize = ref(10)
const reviewTotal = ref(0)
const newReview = ref('')

onMounted(async () => {
  try {
    const id = route.params.id
    const [bookRes, ratingRes, favRes] = await Promise.all([
      bookAPI.getById(id),
      ratingAPI.getRating(id),
      favoriteAPI.check(id),
    ])
    book.value = bookRes.data
    if (book.value) {
      historyAPI.record(id).catch(() => {})  // 记录浏览历史（静默）
      book.value.avgRating = ratingRes.data.avgRating || 0
      book.value.ratingCount = ratingRes.data.ratingCount || 0
      book.value.favorited = favRes.data.favorited
      book.value.favoriteCount = favRes.data.favoriteCount
      if (ratingRes.data.userRating) myRating.value = ratingRes.data.userRating
    }
    fetchReviews()
  } catch (e) {
    ElMessage.error('图书不存在')
    router.push('/books')
  }
})

async function fetchReviews() {
  try {
    const res = await reviewAPI.list(route.params.id, { page: reviewPage.value, size: reviewSize.value })
    reviews.value = res.data.records || []
    reviewTotal.value = res.data.total || 0
  } catch (e) { console.error(e) }
}

async function submitRating(score) {
  try {
    await ratingAPI.rate(route.params.id, score)
    ElMessage.success('评分成功')
    // Refresh rating
    const res = await ratingAPI.getRating(route.params.id)
    if (book.value) {
      book.value.avgRating = res.data.avgRating || 0
      book.value.ratingCount = res.data.ratingCount || 0
    }
  } catch (e) { /* handled */ }
}

async function submitReview() {
  if (!newReview.value.trim()) return
  try {
    await reviewAPI.add(route.params.id, newReview.value.trim())
    ElMessage.success('评论发表成功')
    newReview.value = ''
    reviewPage.value = 1
    fetchReviews()
  } catch (e) { /* handled */ }
}

async function deleteReview(id) {
  try {
    await reviewAPI.delete(route.params.id, id)
    ElMessage.success('评论已删除')
    fetchReviews()
  } catch (e) { /* handled */ }
}

async function toggleFavorite() {
  if (!userStore.isLoggedIn()) {
    ElMessage.warning('请先登录')
    return
  }
  try {
    const res = await favoriteAPI.toggle(route.params.id)
    if (book.value) {
      book.value.favorited = res.data.favorited
      book.value.favoriteCount = res.data.favoriteCount
    }
    ElMessage.success(book.value.favorited ? '已收藏' : '已取消收藏')
  } catch (e) { /* handled */ }
}

function addToCart() {
  cartStore.addItem({ ...book.value, quantity: quantity.value })
  ElMessage.success(`已添加《${book.value.title}》到购物车`)
  quantity.value = 1
}

function buyNow() {
  cartStore.addItem({ ...book.value, quantity: quantity.value })
  router.push('/cart')
}
</script>

<style scoped>
.book-detail { padding: 40px 0; }
.detail-card {
  display: flex; gap: 48px;
  background: #fff; border-radius: 16px; padding: 40px;
  box-shadow: 0 2px 20px rgba(0, 0, 0, 0.04);
}
.detail-cover {
  width: 240px; flex-shrink: 0;
  aspect-ratio: 3/4; background: #f0f0f8;
  border-radius: 8px; overflow: hidden;
  display: flex; align-items: center; justify-content: center;
}
.detail-cover img { width: 100%; height: 100%; object-fit: cover; }
.cover-placeholder { color: #c0c0d0; }
.detail-info { flex: 1; }
.detail-category { color: #4f46e5; font-size: 0.85rem; font-weight: 600; margin-bottom: 8px; }
.detail-info h1 { font-size: 1.8rem; font-weight: 700; color: #1a1a2e; }
.detail-author { margin-top: 8px; color: #5a5a7a; }
.detail-meta { margin-top: 4px; color: #8c8ca1; font-size: 0.85rem; }
.detail-rating { display: flex; align-items: center; gap: 8px; margin-top: 16px; }
.rating-text { font-size: 0.85rem; color: #8c8ca1; }
.detail-price { margin-top: 20px; font-size: 2rem; font-weight: 800; color: #e74c3c; }
.detail-stock { margin-top: 8px; color: #8c8ca1; }
.detail-stock.low { color: #e74c3c; }
.detail-actions { margin-top: 28px; display: flex; gap: 12px; align-items: center; flex-wrap: wrap; }
.detail-description { margin-top: 32px; background: #fff; border-radius: 16px; padding: 32px; box-shadow: 0 2px 20px rgba(0, 0, 0, 0.04); }
.detail-description h2 { font-size: 1.2rem; font-weight: 700; margin-bottom: 16px; }
.detail-description p { line-height: 1.8; color: #5a5a7a; }

.section-card {
  margin-top: 24px; background: #fff; border-radius: 16px; padding: 28px 32px;
  box-shadow: 0 2px 20px rgba(0, 0, 0, 0.04);
}
.section-card h3 { font-size: 1.1rem; font-weight: 700; margin-bottom: 16px; }
.my-rating { display: flex; align-items: center; gap: 12px; }
.my-rating-hint { font-size: 0.85rem; color: #c0c0d0; }
.add-review { margin-bottom: 20px; }
.add-review-hint { font-size: 0.9rem; color: #8c8ca1; padding: 16px 0; }
.add-review-hint a { color: #4f46e5; }
.review-item { padding: 16px 0; border-bottom: 1px solid #f0f0f0; }
.review-item:last-child { border-bottom: none; }
.review-header { display: flex; align-items: center; gap: 12px; margin-bottom: 8px; }
.review-user { font-weight: 600; color: #1a1a2e; font-size: 0.9rem; }
.review-time { color: #b0b5bd; font-size: 0.8rem; }
.review-content { color: #5a5a7a; line-height: 1.6; font-size: 0.9rem; }
.no-reviews { text-align: center; color: #c0c0d0; padding: 24px 0; }
</style>

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
        </div>
      </div>
    </div>

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
import { bookAPI } from '../api'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()
const cartStore = useCartStore()
const book = ref(null)
const quantity = ref(1)

onMounted(async () => {
  try {
    const res = await bookAPI.getById(route.params.id)
    book.value = res.data
  } catch (e) {
    ElMessage.error('图书不存在')
    router.push('/books')
  }
})

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
  width: 320px; flex-shrink: 0;
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
.detail-price { margin-top: 20px; font-size: 2rem; font-weight: 800; color: #e74c3c; }
.detail-stock { margin-top: 8px; color: #8c8ca1; }
.detail-stock.low { color: #e74c3c; }
.detail-actions { margin-top: 28px; display: flex; gap: 12px; align-items: center; }
.detail-description { margin-top: 32px; background: #fff; border-radius: 16px; padding: 32px; box-shadow: 0 2px 20px rgba(0, 0, 0, 0.04); }
.detail-description h2 { font-size: 1.2rem; font-weight: 700; margin-bottom: 16px; }
.detail-description p { line-height: 1.8; color: #5a5a7a; }
</style>

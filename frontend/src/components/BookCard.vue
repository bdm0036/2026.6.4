<template>
  <div class="book-card" @click="$router.push(`/book/${book.id}`)">
    <BookCover :title="book.title" :src="book.coverImage" :category-id="book.categoryId"
      :label="tagLabel" :show-label="!!tagLabel" />
    <div class="book-info">
      <h3 :title="book.title">{{ book.title }}</h3>
      <p class="book-author">{{ book.author }}</p>
      <div class="book-category" v-if="book.categoryName">{{ book.categoryName }}</div>
      <div class="book-rating" v-if="book.avgRating">
        <el-rate v-model="book.avgRating" disabled size="small"
          :colors="['#f59e0b','#f59e0b','#f59e0b']" />
        <span class="rating-num">{{ Number(book.avgRating).toFixed(1) }}</span>
      </div>
      <div class="book-bottom">
        <span class="book-price">¥{{ book.price }}</span>
        <el-button size="small" type="primary" circle @click.stop="handleAddCart" :disabled="book.stock === 0">
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
  if (props.book.tags) {
    if (props.book.tags.includes('新上')) return '新上'
    if (props.book.tags.includes('热卖')) return '热卖'
    if (props.book.tags.includes('特价')) return '特价'
    if (props.book.tags.includes('秒杀')) return '秒杀'
  }
  if (props.book.stock > 0 && props.book.stock < 10) return '紧张'
  return ''
})

function handleAddCart() {
  if (props.book.stock === 0) {
    ElMessage.warning('该图书暂时缺货')
    return
  }
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
.book-category { margin-top: 2px; font-size: 0.75rem; color: #4f46e5; }
.book-rating { display: flex; align-items: center; gap: 4px; margin-top: 6px; }
.rating-num { font-size: 0.75rem; color: #f59e0b; font-weight: 600; }
.book-bottom { display: flex; align-items: center; justify-content: space-between; margin-top: 10px; }
.book-price { font-size: 1.05rem; font-weight: 700; color: #e74c3c; }
</style>

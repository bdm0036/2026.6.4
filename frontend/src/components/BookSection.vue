<template>
  <section class="book-section">
    <div class="section-header">
      <el-tabs v-model="activeTab" @tab-change="handleTabChange" class="section-tabs">
        <el-tab-pane v-for="tab in tabs" :key="tab.key" :label="tab.label" :name="tab.key" />
      </el-tabs>
      <router-link :to="moreLink" class="view-all">查看全部 <el-icon><ArrowRight /></el-icon></router-link>
    </div>
    <div v-if="loading" class="book-grid">
      <div v-for="i in 5" :key="i" class="skeleton-card">
        <el-skeleton animated>
          <template #template>
            <el-skeleton-item variant="image" style="width:100%;aspect-ratio:3/4" />
            <div style="padding:12px">
              <el-skeleton-item variant="text" style="width:80%" />
              <el-skeleton-item variant="text" style="width:50%;margin-top:6px" />
            </div>
          </template>
        </el-skeleton>
      </div>
    </div>
    <div v-else class="book-grid">
      <BookCard v-for="book in books" :key="book.id" :book="book" />
    </div>
  </section>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import BookCard from './BookCard.vue'

const props = defineProps({
  tabs: { type: Array, required: true },
  fetchBooks: { type: Function, required: true },
  moreLink: { type: String, default: '/books' },
})

const activeTab = ref(props.tabs[0]?.key || '')
const books = ref([])
const loading = ref(false)

onMounted(() => loadBooks())

async function loadBooks() {
  loading.value = true
  try {
    books.value = await props.fetchBooks(activeTab.value)
  } catch (e) {
    books.value = []
  } finally {
    loading.value = false
  }
}

function handleTabChange() { loadBooks() }
</script>

<style scoped>
.book-section { padding: 48px 0; }
.section-header {
  display: flex; align-items: center; justify-content: space-between;
  margin-bottom: 24px;
}
.section-tabs :deep(.el-tabs__header) { margin: 0; }
.view-all { display: flex; align-items: center; gap: 4px; color: #4f46e5; font-size: 0.9rem; font-weight: 600; white-space: nowrap; }
.book-grid { display: grid; grid-template-columns: repeat(5, 1fr); gap: 20px; }
.skeleton-card { border-radius: 12px; overflow: hidden; background: var(--bg-card); }
@media (max-width: 1024px) { .book-grid { grid-template-columns: repeat(4, 1fr); } }
@media (max-width: 768px) { .book-grid { grid-template-columns: repeat(3, 1fr); } }
</style>

<template>
  <div class="global-search">
    <button class="search-trigger" @click="open" title="搜索图书">
      <el-icon :size="18"><Search /></el-icon>
    </button>
    <el-dialog v-model="visible" title="搜索图书" width="560px" :close-on-click-modal="true"
      @opened="onOpened">
      <el-input v-model="kw" ref="inputRef" placeholder="搜索书名、作者、ISBN..." size="large"
        :prefix-icon="Search" clearable @keyup.enter="doSearch" />
      <div class="search-results" v-if="results.length > 0">
        <div v-for="book in results" :key="book.id" class="search-item" @click="goBook(book.id)">
          <BookCover :title="book.title" :src="book.coverImage" :category-id="book.categoryId" class="search-cover" />
          <div class="search-info">
            <span class="search-title">{{ book.title }}</span>
            <span class="search-author">{{ book.author }}</span>
            <span class="search-cat" v-if="book.categoryName">{{ book.categoryName }}</span>
          </div>
          <span class="search-price">¥{{ book.price }}</span>
        </div>
      </div>
      <div v-else-if="kw && searched" class="search-empty">
        <el-icon :size="40"><Document /></el-icon>
        <p>未找到相关图书</p>
      </div>
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

function open() { visible.value = true }
function onOpened() { nextTick(() => inputRef.value?.focus()) }

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
  kw.value = ''
  results.value = []
  searched.value = false
  router.push(`/book/${id}`)
}
</script>

<style scoped>
.search-trigger {
  display: flex; align-items: center; justify-content: center;
  width: 36px; height: 36px; border-radius: 8px; border: none;
  background: transparent; cursor: pointer; color: var(--text-secondary);
  transition: background 0.2s;
}
.search-trigger:hover { background: rgba(128, 128, 160, 0.15); }
.search-results { margin-top: 16px; max-height: 360px; overflow-y: auto; }
.search-item {
  display: flex; align-items: center; gap: 12px; padding: 10px; border-radius: 8px;
  cursor: pointer; transition: background 0.2s;
}
.search-item:hover { background: rgba(128,128,160,0.08); }
.search-cover { width: 44px; height: 58px; flex-shrink: 0; border-radius: 4px; }
.search-info { flex: 1; display: flex; flex-direction: column; min-width: 0; }
.search-title { font-size: 0.9rem; font-weight: 600; color: var(--text-primary); overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.search-author { font-size: 0.78rem; color: var(--text-muted); margin-top: 2px; }
.search-cat { font-size: 0.72rem; color: #4f46e5; margin-top: 1px; }
.search-price { font-weight: 700; color: #e74c3c; font-size: 0.95rem; }
.search-empty { text-align: center; padding: 32px; color: var(--text-muted); }
.search-empty p { margin-top: 8px; }
</style>

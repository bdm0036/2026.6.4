<template>
  <div>
    <div class="toolbar">
      <el-input v-model="keyword" placeholder="搜索书名/作者" style="width:220px" clearable @input="fetchData" />
      <el-select v-model="categoryId" placeholder="分类筛选" style="width:160px" clearable @change="fetchData">
        <el-option v-for="c in categories" :key="c.id" :label="c.name" :value="c.id" />
      </el-select>
      <el-button type="primary" @click="showDialog(null)"><el-icon><Plus /></el-icon>新增图书</el-button>
    </div>
    <el-table :data="books" v-loading="loading" stripe>
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="title" label="书名" min-width="180" />
      <el-table-column prop="author" label="作者" width="120" />
      <el-table-column prop="categoryName" label="分类" width="100" />
      <el-table-column prop="price" label="价格" width="100">
        <template #default="scope">¥{{ scope.row.price }}</template>
      </el-table-column>
      <el-table-column prop="stock" label="库存" width="80" />
      <el-table-column prop="status" label="状态" width="90">
        <template #default="scope">
          <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'" size="small">
            {{ scope.row.status === 1 ? '上架' : '下架' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="180" fixed="right">
        <template #default="scope">
          <el-button size="small" text @click="showDialog(scope.row)">编辑</el-button>
          <el-button size="small" text :type="scope.row.status === 1 ? 'danger' : 'success'"
            @click="toggleStatus(scope.row)">
            {{ scope.row.status === 1 ? '下架' : '上架' }}
          </el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination
      v-model:current-page="page" :page-size="size" :total="total" layout="total, prev, pager, next"
      @current-change="fetchData" style="margin-top:16px;justify-content:center" />

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="editing ? '编辑图书' : '新增图书'" width="550px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="书名"><el-input v-model="form.title" /></el-form-item>
        <el-form-item label="作者"><el-input v-model="form.author" /></el-form-item>
        <el-form-item label="ISBN"><el-input v-model="form.isbn" /></el-form-item>
        <el-form-item label="出版社"><el-input v-model="form.publisher" /></el-form-item>
        <el-form-item label="价格"><el-input-number v-model="form.price" :min="0" :precision="2" /></el-form-item>
        <el-form-item label="库存"><el-input-number v-model="form.stock" :min="0" /></el-form-item>
        <el-form-item label="分类">
          <el-select v-model="form.categoryId" placeholder="选择分类">
            <el-option v-for="c in categories" :key="c.id" :label="c.name" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="简介"><el-input v-model="form.description" type="textarea" :rows="3" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveBook">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { bookAPI, categoryAPI } from '../../api'
import { ElMessage, ElMessageBox } from 'element-plus'

const books = ref([])
const categories = ref([])
const loading = ref(false)
const page = ref(1)
const size = ref(12)
const total = ref(0)
const keyword = ref('')
const categoryId = ref(null)
const dialogVisible = ref(false)
const editing = ref(null)
const form = ref({})

onMounted(() => { fetchData(); fetchCategories() })

async function fetchData() {
  loading.value = true
  try {
    const res = await bookAPI.list({ page: page.value, size: size.value, keyword: keyword.value, categoryId: categoryId.value })
    books.value = res.data?.records ?? []
    total.value = res.data?.total ?? 0
  } finally { loading.value = false }
}
async function fetchCategories() {
  const res = await categoryAPI.list()
  categories.value = res.data ?? []
}
function showDialog(row) {
  editing.value = row
  form.value = row ? { ...row } : { title: '', author: '', isbn: '', publisher: '', price: 0, stock: 0, categoryId: null, description: '' }
  dialogVisible.value = true
}
async function saveBook() {
  try {
    if (editing.value) {
      await bookAPI.update(editing.value.id, form.value)
      ElMessage.success('更新成功')
    } else {
      await bookAPI.add(form.value)
      ElMessage.success('添加成功')
    }
    dialogVisible.value = false
    fetchData()
  } catch (e) { /* error handled by interceptor */ }
}
async function toggleStatus(row) {
  try {
    await ElMessageBox.confirm(`确定${row.status === 1 ? '下架' : '上架'}《${row.title}》？`, '提示', { type: 'warning' })
    await bookAPI.update(row.id, { ...row, status: row.status === 1 ? 0 : 1 })
    ElMessage.success('操作成功')
    fetchData()
  } catch (e) { /* cancel or error */ }
}
</script>

<style scoped>
.toolbar {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
  flex-wrap: wrap;
}
</style>

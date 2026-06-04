<template>
  <div>
    <div class="toolbar">
      <el-button type="primary" @click="showDialog(null)"><el-icon><Plus /></el-icon>新增分类</el-button>
    </div>
    <el-table :data="categories" v-loading="loading" stripe>
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="name" label="分类名称" width="180" />
      <el-table-column prop="description" label="描述" min-width="300" />
      <el-table-column prop="sortOrder" label="排序" width="80" />
      <el-table-column prop="status" label="状态" width="80">
        <template #default="scope">
          <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'" size="small">
            {{ scope.row.status === 1 ? '启用' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="160">
        <template #default="scope">
          <el-button size="small" text @click="showDialog(scope.row)">编辑</el-button>
          <el-button size="small" text type="danger" @click="deleteCategory(scope.row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="editing ? '编辑分类' : '新增分类'" width="450px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="名称"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="描述"><el-input v-model="form.description" /></el-form-item>
        <el-form-item label="排序"><el-input-number v-model="form.sortOrder" :min="0" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveCategory">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { categoryAPI } from '../../api'
import { ElMessage, ElMessageBox } from 'element-plus'

const categories = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const editing = ref(null)
const form = ref({})

onMounted(() => fetchData())

async function fetchData() {
  loading.value = true
  try {
    const res = await categoryAPI.list()
    categories.value = res.data ?? []
  } finally { loading.value = false }
}
function showDialog(row) {
  editing.value = row
  form.value = row ? { ...row } : { name: '', description: '', sortOrder: 0 }
  dialogVisible.value = true
}
async function saveCategory() {
  try {
    if (editing.value) {
      await categoryAPI.update(editing.value.id, form.value)
      ElMessage.success('更新成功')
    } else {
      await categoryAPI.add(form.value)
      ElMessage.success('添加成功')
    }
    dialogVisible.value = false
    fetchData()
  } catch (e) { }
}
async function deleteCategory(row) {
  try {
    await ElMessageBox.confirm(`确定删除分类「${row.name}」？`, '提示', { type: 'warning' })
    await categoryAPI.delete(row.id)
    ElMessage.success('删除成功')
    fetchData()
  } catch (e) { }
}
</script>

<style scoped>
.toolbar { display: flex; gap: 12px; margin-bottom: 16px; }
</style>

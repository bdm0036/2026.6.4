<template>
  <div>
    <el-table :data="users" v-loading="loading" stripe>
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="username" label="用户名" width="140" />
      <el-table-column prop="nickname" label="昵称" width="140" />
      <el-table-column prop="email" label="邮箱" min-width="180" />
      <el-table-column prop="phone" label="手机号" width="140" />
      <el-table-column prop="role" label="角色" width="90">
        <template #default="scope">
          <el-tag :type="scope.row.role === 'ADMIN' ? 'danger' : 'primary'" size="small">
            {{ scope.row.role === 'ADMIN' ? '管理员' : '用户' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="90">
        <template #default="scope">
          <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'" size="small">
            {{ scope.row.status === 1 ? '正常' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="注册时间" width="170" />
      <el-table-column label="操作" width="120" fixed="right">
        <template #default="scope">
          <el-button v-if="scope.row.id !== 1" size="small" text
            :type="scope.row.status === 1 ? 'danger' : 'success'"
            @click="toggleStatus(scope.row)">
            {{ scope.row.status === 1 ? '禁用' : '启用' }}
          </el-button>
          <span v-else style="color:#c0c4cc;font-size:0.85rem">超级管理员</span>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { userAPI } from '../../api'
import { ElMessage, ElMessageBox } from 'element-plus'

const users = ref([])
const loading = ref(false)

onMounted(() => fetchData())

async function fetchData() {
  loading.value = true
  try {
    const res = await userAPI.list()
    users.value = res.data ?? []
  } finally { loading.value = false }
}
async function toggleStatus(row) {
  const newStatus = row.status === 1 ? 0 : 1
  try {
    await ElMessageBox.confirm(`确定${newStatus === 0 ? '禁用' : '启用'}用户「${row.username}」？`, '提示', { type: 'warning' })
    await userAPI.updateStatus(row.id, newStatus)
    ElMessage.success('操作成功')
    fetchData()
  } catch (e) { }
}
</script>

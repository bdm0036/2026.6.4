<template>
  <div>
    <el-table :data="orders" v-loading="loading" stripe>
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="orderNo" label="订单号" width="200" />
      <el-table-column prop="userId" label="用户ID" width="80" />
      <el-table-column prop="totalAmount" label="金额" width="100">
        <template #default="scope">¥{{ scope.row.totalAmount }}</template>
      </el-table-column>
      <el-table-column prop="receiverName" label="收货人" width="100" />
      <el-table-column prop="receiverPhone" label="电话" width="130" />
      <el-table-column prop="receiverAddress" label="地址" min-width="160" />
      <el-table-column prop="status" label="状态" width="100">
        <template #default="scope">
          <el-tag :type="statusTagType(scope.row.status)" size="small">
            {{ statusText(scope.row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="下单时间" width="170" />
      <el-table-column label="操作" width="220" fixed="right">
        <template #default="scope">
          <el-dropdown @command="(cmd) => handleCommand(cmd, scope.row)">
            <el-button size="small">操作 <el-icon><ArrowDown /></el-icon></el-button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item :command="1" :disabled="scope.row.status !== 0">设为已支付</el-dropdown-item>
                <el-dropdown-item :command="2" :disabled="scope.row.status !== 1">设为已发货</el-dropdown-item>
                <el-dropdown-item :command="3" :disabled="scope.row.status !== 2">设为已完成</el-dropdown-item>
                <el-dropdown-item :command="4" divided :disabled="scope.row.status !== 0" style="color:#F56C6C">取消订单</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination
      v-model:current-page="page" :page-size="size" :total="total" layout="total, prev, pager, next"
      @current-change="fetchData" style="margin-top:16px;justify-content:center" />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { orderAPI } from '../../api'
import { ElMessage } from 'element-plus'

const orders = ref([])
const loading = ref(false)
const page = ref(1)
const size = ref(12)
const total = ref(0)

onMounted(() => fetchData())

async function fetchData() {
  loading.value = true
  try {
    const res = await orderAPI.listAll({ page: page.value, size: size.value })
    orders.value = res.data?.records ?? []
    total.value = res.data?.total ?? 0
  } finally { loading.value = false }
}
async function handleCommand(cmd, row) {
  try {
    if (cmd === 4) {
      await orderAPI.cancel(row.id)
      ElMessage.success('订单已取消')
    } else {
      await orderAPI.updateStatus(row.id, cmd)
      ElMessage.success('状态更新成功')
    }
    fetchData()
  } catch (e) { }
}
function statusText(status) {
  const map = { 0: '待支付', 1: '已支付', 2: '已发货', 3: '已完成', 4: '已取消' }
  return map[status] ?? '未知'
}
function statusTagType(status) {
  const map = { 0: 'warning', 1: 'primary', 2: 'info', 3: 'success', 4: 'danger' }
  return map[status] ?? ''
}
</script>

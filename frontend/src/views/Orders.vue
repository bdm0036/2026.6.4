<template>
  <div class="orders-page container">
    <div class="page-header">
      <h1>我的订单</h1>
    </div>
    <div v-if="orders.length === 0 && !loading" class="empty">
      <el-icon :size="64"><Document /></el-icon>
      <p>暂无订单</p>
    </div>
    <div v-else class="order-list">
      <div v-for="order in orders" :key="order.id" class="order-card">
        <div class="order-header">
          <span class="order-no">订单号：{{ order.orderNo }}</span>
          <el-tag :type="statusType(order.status)">{{ statusText(order.status) }}</el-tag>
        </div>
        <div class="order-body">
          <div class="order-meta">
            <span>收货人：{{ order.receiverName }}</span>
            <span>电话：{{ order.receiverPhone }}</span>
          </div>
          <div class="order-footer">
            <span class="order-time">{{ order.createTime }}</span>
            <span class="order-amount">¥{{ order.totalAmount }}</span>
          </div>
        </div>
        <div class="order-actions" v-if="order.status === 0">
          <el-button size="small" type="primary" @click="payOrder(order.id)">立即支付</el-button>
          <el-button size="small" @click="cancelOrder(order.id)">取消订单</el-button>
        </div>
      </div>
    </div>
    <div class="pagination" v-if="total > size">
      <el-pagination v-model:current-page="page" :page-size="size" :total="total" layout="prev, pager, next" @current-change="fetchOrders" />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { orderAPI } from '../api'
import { ElMessage, ElMessageBox } from 'element-plus'

const orders = ref([])
const page = ref(1)
const size = ref(10)
const total = ref(0)
const loading = ref(false)

onMounted(() => fetchOrders())

async function fetchOrders() {
  loading.value = true
  try {
    const res = await orderAPI.listMy({ page: page.value, size: size.value })
    orders.value = res.data.records
    total.value = res.data.total
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

function statusType(status) {
  return { 0: 'warning', 1: 'success', 2: 'primary', 3: 'info', 4: 'danger' }[status] || 'info'
}
function statusText(status) {
  return { 0: '待支付', 1: '已支付', 2: '已发货', 3: '已完成', 4: '已取消' }[status] || '未知'
}

async function payOrder(id) {
  try {
    await orderAPI.pay(id)
    ElMessage.success('支付成功')
    fetchOrders()
  } catch (e) {}
}

async function cancelOrder(id) {
  try {
    await ElMessageBox.confirm('确定要取消此订单吗？', '确认取消', { type: 'warning' })
    await orderAPI.cancel(id)
    ElMessage.success('订单已取消')
    fetchOrders()
  } catch (e) {}
}
</script>

<style scoped>
.orders-page { padding-bottom: 60px; }
.empty { text-align: center; padding: 80px 0; color: #c0c0d0; }
.empty p { margin-top: 12px; }
.order-list { display: flex; flex-direction: column; gap: 16px; }
.order-card { background: #fff; border-radius: 12px; padding: 20px 24px; box-shadow: 0 1px 8px rgba(0, 0, 0, 0.03); }
.order-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px; }
.order-no { font-size: 0.9rem; color: #5a5a7a; font-weight: 500; }
.order-meta { display: flex; gap: 24px; font-size: 0.85rem; color: #8c8ca1; }
.order-footer { display: flex; justify-content: space-between; align-items: center; margin-top: 12px; }
.order-time { font-size: 0.8rem; color: #b0b5bd; }
.order-amount { font-size: 1.2rem; font-weight: 700; color: #e74c3c; }
.order-actions { margin-top: 12px; display: flex; gap: 8px; justify-content: flex-end; }
.pagination { display: flex; justify-content: center; margin-top: 32px; }
</style>

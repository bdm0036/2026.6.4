<template>
  <div class="order-detail-page container" v-loading="loading">
    <div class="page-header">
      <h1>订单详情</h1>
      <el-button text @click="$router.push('/orders')">← 返回</el-button>
    </div>

    <template v-if="order">
      <!-- Status Steps -->
      <div class="section-card">
        <el-steps :active="statusStep" finish-status="success" align-center>
          <el-step title="已下单" :description="order.createTime" />
          <el-step title="已支付" :description="order.payTime || '待支付'" />
          <el-step title="已发货" :description="order.shipTime || '待发货'" />
          <el-step title="已完成" :description="order.finishTime || '待完成'" />
        </el-steps>
        <div class="status-tag" v-if="order.status === 4">
          <el-tag type="danger">已取消</el-tag>
        </div>
      </div>

      <!-- Receiver Info -->
      <div class="section-card">
        <h3>收货信息</h3>
        <p><strong>{{ order.receiverName }}</strong> {{ order.receiverPhone }}</p>
        <p class="text-muted">{{ order.receiverAddress }}</p>
        <p class="text-muted" v-if="order.remark">备注：{{ order.remark }}</p>
      </div>

      <!-- Order Items -->
      <div class="section-card">
        <h3>商品列表</h3>
        <div class="order-items">
          <div v-for="item in order.items" :key="item.id" class="order-item"
            @click="$router.push(`/book/${item.bookId}`)">
            <BookCover :title="item.bookTitle" :category-id="0" class="item-cover-small" />
            <div class="item-info">
              <span class="item-title">{{ item.bookTitle }}</span>
              <span class="item-meta">¥{{ item.price }} × {{ item.quantity }}</span>
            </div>
            <span class="item-subtotal">¥{{ item.subtotal }}</span>
          </div>
        </div>
      </div>

      <!-- Price Summary -->
      <div class="section-card">
        <h3>价格明细</h3>
        <div class="price-detail">
          <div class="price-row"><span>订单编号</span><span class="order-no-text">{{ order.orderNo }}</span></div>
          <div class="price-row"><span>商品金额</span><span>¥{{ order.totalAmount }}</span></div>
          <div class="price-row total"><span>实付款</span><span class="final">¥{{ order.totalAmount }}</span></div>
        </div>
      </div>

      <!-- Payment Info -->
      <div class="section-card" v-if="payment">
        <h3>支付信息</h3>
        <div class="info-row"><span>支付方式</span><span>{{ payment.channel }}</span></div>
        <div class="info-row"><span>交易号</span><span class="order-no-text">{{ payment.tradeNo }}</span></div>
        <div class="info-row"><span>支付时间</span><span>{{ payment.payTime }}</span></div>
      </div>

      <!-- Shipment Info -->
      <div class="section-card" v-if="shipment">
        <h3>物流信息</h3>
        <div class="info-row"><span>快递公司</span><span>{{ shipment.company }}</span></div>
        <div class="info-row"><span>运单号</span><span class="order-no-text">{{ shipment.trackingNo }}</span></div>
        <div class="info-row"><span>发货时间</span><span>{{ shipment.shipTime }}</span></div>
        <div class="info-row" v-if="shipment.signTime"><span>签收时间</span><span>{{ shipment.signTime }}</span></div>
      </div>

      <!-- Actions -->
      <div class="order-actions-bar">
        <el-button v-if="order.status === 0" type="primary" size="large" @click="payOrder">立即支付</el-button>
        <el-button v-if="order.status === 0" size="large" @click="cancelOrder">取消订单</el-button>
        <el-button v-if="order.status === 1 && userStore.isAdmin()" type="warning" size="large" @click="shipOrder">模拟发货</el-button>
        <el-button v-if="order.status === 2" type="success" size="large" @click="confirmReceive">确认收货</el-button>
        <el-button v-if="order.status === 3" size="large" @click="$router.push(`/book/${order.items[0]?.bookId}`)">再次购买</el-button>
      </div>
    </template>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { orderAPI, paymentAPI, shipmentAPI } from '../api'
import { useUserStore } from '../stores/user'
import { ElMessage, ElMessageBox } from 'element-plus'
import BookCover from '../components/BookCover.vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const order = ref(null)
const loading = ref(false)
const payment = ref(null)
const shipment = ref(null)

const statusStep = computed(() => {
  const s = order.value?.status
  return s === 0 ? 0 : s === 1 ? 1 : s === 2 ? 2 : s === 3 ? 3 : s === 4 ? -1 : 0
})

onMounted(async () => {
  loading.value = true
  try {
    const res = await orderAPI.getById(route.params.id)
    order.value = res.data
  } catch (e) {
    ElMessage.error('订单不存在')
    router.push('/orders')
  } finally { loading.value = false }
  loadPaymentAndShipment()
})

async function loadPaymentAndShipment() {
  try {
    const [payRes, shipRes] = await Promise.all([
      paymentAPI.getByOrder(route.params.id).catch(() => ({ data: null })),
      shipmentAPI.getByOrder(route.params.id).catch(() => ({ data: null })),
    ])
    payment.value = payRes.data
    shipment.value = shipRes.data
  } catch (e) { /* silent */ }
}

async function payOrder() {
  try {
    await paymentAPI.pay(order.value.id)
    ElMessage.success('支付成功')
    location.reload()
  } catch (e) { /* handled */ }
}

async function shipOrder() {
  try {
    await ElMessageBox.confirm('确认模拟发货？', '确认', { type: 'info' })
    await shipmentAPI.ship(order.value.id)
    ElMessage.success('发货成功')
    location.reload()
  } catch (e) { /* cancel */ }
}
async function cancelOrder() {
  try {
    await ElMessageBox.confirm('确定取消此订单？', '确认取消', { type: 'warning' })
    await orderAPI.cancel(order.value.id)
    ElMessage.success('订单已取消')
    location.reload()
  } catch (e) { /* cancel or error */ }
}
async function confirmReceive() {
  try {
    await ElMessageBox.confirm('确认已收到商品？', '确认收货', { type: 'success' })
    await orderAPI.updateStatus(order.value.id, 3)
    ElMessage.success('已确认收货')
    location.reload()
  } catch (e) { /* cancel or error */ }
}
</script>

<style scoped>
.order-detail-page { padding-bottom: 60px; }

.section-card {
  background: var(--bg-card);
  border: 1px solid var(--border-light);
  border-radius: 6px;
  padding: 24px;
  margin-bottom: 16px;
}

.section-card h3 {
  font-size: 1rem;
  font-weight: 700;
  margin-bottom: 14px;
  color: var(--text-primary);
}

.status-tag {
  text-align: center;
  margin-top: 12px;
}

.text-muted {
  color: var(--text-muted);
  font-size: 0.85rem;
}

.order-items {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.order-item {
  display: flex;
  align-items: center;
  gap: 16px;
  cursor: pointer;
  padding: 8px;
  border-radius: 4px;
  transition: background 0.15s;
}

.order-item:hover {
  background: var(--accent-soft);
}

.item-cover-small {
  width: 56px;
  height: 74px;
  flex-shrink: 0;
}

.item-info {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.item-title {
  font-weight: 600;
  color: var(--text-primary);
  font-size: 0.9rem;
}

.item-meta {
  font-size: 0.8rem;
  color: var(--text-muted);
  margin-top: 2px;
}

.item-subtotal {
  font-weight: 700;
  color: var(--text-primary);
  font-size: 0.95rem;
  font-feature-settings: 'tnum';
  font-variant-numeric: tabular-nums;
}

.price-detail {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.price-row {
  display: flex;
  justify-content: space-between;
  font-size: 0.88rem;
  color: var(--text-secondary);
}

.price-row.total {
  font-weight: 700;
  font-size: 0.95rem;
  padding-top: 8px;
  border-top: 1px solid var(--border-light);
  color: var(--text-primary);
}

.final {
  font-size: 1.2rem;
  color: var(--text-primary);
  font-feature-settings: 'tnum';
  font-variant-numeric: tabular-nums;
}

.order-no-text {
  color: var(--text-muted);
  font-size: 0.82rem;
  font-feature-settings: 'tnum';
}

.info-row {
  display: flex;
  justify-content: space-between;
  font-size: 0.88rem;
  color: var(--text-secondary);
  margin-bottom: 6px;
}

.order-actions-bar {
  display: flex;
  gap: 12px;
  justify-content: flex-end;
  margin-top: 16px;
}
</style>

<template>
  <div class="cart-page container">
    <div class="page-header">
      <h1>购物车</h1>
      <p>{{ cartStore.totalCount }} 件商品</p>
    </div>

    <div v-if="cartStore.items.length === 0" class="empty-cart">
      <el-icon :size="64"><ShoppingCart /></el-icon>
      <p>购物车是空的</p>
      <el-button type="primary" @click="$router.push('/books')">去逛逛</el-button>
    </div>

    <div v-else class="cart-content">
      <div class="cart-items">
        <div v-for="item in cartStore.items" :key="item.bookId" class="cart-item">
          <div class="item-cover" @click="$router.push(`/book/${item.bookId}`)">
            <img v-if="item.coverImage" :src="item.coverImage" :alt="item.title" />
            <div v-else class="cover-placeholder"><el-icon :size="32"><Reading /></el-icon></div>
          </div>
          <div class="item-info" @click="$router.push(`/book/${item.bookId}`)">
            <h3>{{ item.title }}</h3>
            <p class="item-price">¥{{ item.price }}</p>
          </div>
          <div class="item-quantity">
            <el-input-number v-model="item.quantity" :min="1" :max="99" @change="cartStore.updateQuantity(item.bookId, item.quantity)" />
          </div>
          <div class="item-subtotal">
            ¥{{ (item.price * item.quantity).toFixed(2) }}
          </div>
          <div class="item-action">
            <el-button type="danger" circle :icon="Delete" size="small" @click="cartStore.removeItem(item.bookId)" />
          </div>
        </div>
      </div>

      <div class="cart-summary">
        <div class="summary-row">
          <span>商品金额</span>
          <span class="summary-amount">¥{{ cartStore.totalAmount.toFixed(2) }}</span>
        </div>
        <el-button type="primary" size="large" class="checkout-btn" @click="showOrderDialog = true" :disabled="!userStore.isLoggedIn()">
          {{ userStore.isLoggedIn() ? '去结算' : '请先登录' }}
        </el-button>
        <p class="summary-tip" v-if="!userStore.isLoggedIn()">
          <router-link to="/login">登录</router-link> 后即可下单
        </p>
      </div>
    </div>

    <!-- Order Dialog -->
    <el-dialog v-model="showOrderDialog" title="确认订单" width="520px" :close-on-click-modal="false">
      <el-form :model="orderForm" label-position="top" size="large">
        <el-form-item label="收货人" required>
          <el-input v-model="orderForm.receiverName" placeholder="请输入收货人姓名" />
        </el-form-item>
        <el-form-item label="联系电话" required>
          <el-input v-model="orderForm.receiverPhone" placeholder="请输入联系电话" />
        </el-form-item>
        <el-form-item label="收货地址" required>
          <el-input v-model="orderForm.receiverAddress" placeholder="请输入详细收货地址" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="orderForm.remark" type="textarea" placeholder="订单备注（选填）" />
        </el-form-item>
      </el-form>
      <div class="order-items-summary">
        <div v-for="item in cartStore.items" :key="item.bookId" class="order-item-line">
          <span>{{ item.title }} × {{ item.quantity }}</span>
          <span>¥{{ (item.price * item.quantity).toFixed(2) }}</span>
        </div>
        <div class="order-total-line">
          <span>合计</span>
          <span class="total-amount">¥{{ cartStore.totalAmount.toFixed(2) }}</span>
        </div>
      </div>
      <template #footer>
        <el-button @click="showOrderDialog = false">取消</el-button>
        <el-button type="primary" @click="submitOrder" :loading="submitting">确认下单</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useCartStore } from '../stores/cart'
import { useUserStore } from '../stores/user'
import { orderAPI } from '../api'
import { ElMessage } from 'element-plus'
import { Delete } from '@element-plus/icons-vue'

const router = useRouter()
const cartStore = useCartStore()
const userStore = useUserStore()
const showOrderDialog = ref(false)
const submitting = ref(false)

const orderForm = reactive({
  receiverName: '',
  receiverPhone: '',
  receiverAddress: '',
  remark: '',
})

async function submitOrder() {
  if (!orderForm.receiverName || !orderForm.receiverPhone || !orderForm.receiverAddress) {
    ElMessage.warning('请填写完整的收货信息')
    return
  }
  submitting.value = true
  try {
    const items = cartStore.items.map(i => ({ bookId: i.bookId, quantity: i.quantity }))
    await orderAPI.create({ ...orderForm, items })
    ElMessage.success('下单成功！')
    cartStore.clearCart()
    showOrderDialog.value = false
    router.push('/orders')
  } catch (e) {
    // handled by interceptor
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.cart-page { padding-bottom: 60px; }
.empty-cart { text-align: center; padding: 80px 0; color: #c0c0d0; }
.empty-cart p { margin: 16px 0; font-size: 1.1rem; }
.cart-content { display: flex; gap: 32px; align-items: flex-start; }
.cart-items { flex: 1; display: flex; flex-direction: column; gap: 16px; }
.cart-item {
  display: flex; align-items: center; gap: 16px;
  background: #fff; border-radius: 12px; padding: 20px;
  box-shadow: 0 1px 8px rgba(0, 0, 0, 0.03);
}
.item-cover {
  width: 80px; aspect-ratio: 3/4; background: #f0f0f8;
  border-radius: 8px; overflow: hidden; cursor: pointer;
  display: flex; align-items: center; justify-content: center;
}
.item-cover img { width: 100%; height: 100%; object-fit: cover; }
.cover-placeholder { color: #c0c0d0; }
.item-info { flex: 1; cursor: pointer; }
.item-info h3 { font-size: 0.95rem; font-weight: 600; }
.item-price { margin-top: 4px; font-size: 0.9rem; color: #e74c3c; font-weight: 600; }
.item-subtotal { font-size: 1rem; font-weight: 700; color: #e74c3c; min-width: 80px; text-align: right; }
.cart-summary {
  width: 300px; flex-shrink: 0;
  background: #fff; border-radius: 12px; padding: 24px;
  box-shadow: 0 1px 8px rgba(0, 0, 0, 0.03);
  position: sticky; top: 88px;
}
.summary-row { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.summary-amount { font-size: 1.3rem; font-weight: 700; color: #e74c3c; }
.checkout-btn { width: 100%; height: 48px; font-size: 1rem; font-weight: 600; }
.summary-tip { margin-top: 12px; text-align: center; font-size: 0.85rem; color: #8c8ca1; }
.summary-tip a { color: #4f46e5; }
.order-items-summary { margin-top: 16px; border-top: 1px solid #eee; padding-top: 16px; }
.order-item-line { display: flex; justify-content: space-between; margin-bottom: 8px; font-size: 0.9rem; color: #5a5a7a; }
.order-total-line { display: flex; justify-content: space-between; margin-top: 8px; padding-top: 8px; border-top: 1px solid #eee; font-weight: 700; }
.total-amount { font-size: 1.2rem; color: #e74c3c; }
</style>

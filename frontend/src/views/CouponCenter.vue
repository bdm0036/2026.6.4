<template>
  <div class="coupon-page container">
    <div class="page-header">
      <h1>优惠券中心</h1>
      <p>领取优惠券，下单更优惠</p>
    </div>

    <!-- 可领取的优惠券 -->
    <section class="section">
      <h2>可领取</h2>
      <div class="coupon-grid" v-if="availableCoupons.length > 0">
        <div v-for="c in availableCoupons" :key="c.id" class="coupon-card" :class="{ claimed: claimedIds.has(c.id) }">
          <div class="coupon-left">
            <span class="coupon-amount" v-if="c.type === 'DISCOUNT'">{{ (c.discount * 10).toFixed(0) }}折</span>
            <span class="coupon-amount" v-else>减{{ c.discount }}</span>
            <span class="coupon-condition">满{{ c.threshold }}可用</span>
          </div>
          <div class="coupon-right">
            <strong>{{ c.name }}</strong>
            <span class="coupon-meta">有效期 {{ c.validDays }} 天 · 剩余 {{ c.total - c.claimed }} 张</span>
            <el-button size="small" type="primary" @click="claimCoupon(c.id)" :loading="claimingId === c.id"
              :disabled="claimedIds.has(c.id) || c.claimed >= c.total">
              {{ claimedIds.has(c.id) ? '已领取' : c.claimed >= c.total ? '已抢光' : '立即领取' }}
            </el-button>
          </div>
        </div>
      </div>
      <div v-else class="empty-hint">暂无可用优惠券</div>
    </section>

    <!-- 我的优惠券 -->
    <section class="section">
      <h2>我的优惠券</h2>
      <div class="coupon-grid" v-if="myCoupons.length > 0">
        <div v-for="c in myCoupons" :key="c.id" class="coupon-card mine" :class="{ used: c.status === 'USED' }">
          <div class="coupon-left">
            <span class="coupon-amount" v-if="c.type === 'DISCOUNT'">{{ (c.discount * 10).toFixed(0) }}折</span>
            <span class="coupon-amount" v-else>减{{ c.discount }}</span>
            <span class="coupon-condition">满{{ c.threshold }}可用</span>
          </div>
          <div class="coupon-right">
            <strong>{{ c.name }}</strong>
            <span class="coupon-meta">领取时间：{{ c.claimTime }}</span>
            <el-tag v-if="c.status === 'UNUSED'" type="success" size="small">未使用</el-tag>
            <el-tag v-else type="info" size="small">已使用</el-tag>
          </div>
        </div>
      </div>
      <div v-else class="empty-hint">还没有优惠券，去上面领取吧</div>
    </section>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { couponAPI } from '../api'
import { ElMessage } from 'element-plus'

const availableCoupons = ref([])
const myCoupons = ref([])
const claimedIds = ref(new Set())
const claimingId = ref(null)

onMounted(async () => {
  await Promise.all([loadAvailable(), loadMy()])
})

async function loadAvailable() {
  try {
    const res = await couponAPI.list()
    availableCoupons.value = res.data || []
  } catch (e) { availableCoupons.value = [] }
}

async function loadMy() {
  try {
    const res = await couponAPI.myList()
    myCoupons.value = res.data || []
    claimedIds.value = new Set(myCoupons.value.map(c => c.couponId))
  } catch (e) { myCoupons.value = [] }
}

async function claimCoupon(couponId) {
  claimingId.value = couponId
  try {
    await couponAPI.claim(couponId)
    ElMessage.success('领取成功')
    claimedIds.value.add(couponId)
    loadMy()
  } catch (e) { /* handled */ }
  finally { claimingId.value = null }
}
</script>

<style scoped>
.coupon-page { padding-bottom: 60px; }
.section { margin-bottom: 40px; }
.section h2 { font-size: 1.2rem; font-weight: 700; margin-bottom: 16px; color: var(--text-primary); }
.coupon-grid { display: grid; grid-template-columns: repeat(2, 1fr); gap: 16px; }
.coupon-card {
  display: flex; border-radius: 10px; overflow: hidden;
  border: 1px solid var(--border-color); background: #fff;
  transition: box-shadow 0.2s;
}
.coupon-card:hover { box-shadow: var(--shadow-hover); }
.coupon-card.claimed { opacity: 0.6; }
.coupon-card.mine { border-left: 4px solid #4f46e5; }
.coupon-card.used { opacity: 0.5; }
.coupon-left {
  width: 120px; background: linear-gradient(135deg, #4f46e5, #6366f1);
  display: flex; flex-direction: column; align-items: center; justify-content: center;
  color: #fff; padding: 16px; flex-shrink: 0;
}
.coupon-amount { font-size: 1.5rem; font-weight: 800; }
.coupon-condition { font-size: 0.75rem; margin-top: 4px; opacity: 0.85; }
.coupon-right { flex: 1; padding: 16px; display: flex; flex-direction: column; justify-content: center; gap: 6px; }
.coupon-right strong { font-size: 0.95rem; color: var(--text-primary); }
.coupon-meta { font-size: 0.78rem; color: var(--text-muted); }
.empty-hint { text-align: center; padding: 40px 0; color: var(--text-muted); }
</style>

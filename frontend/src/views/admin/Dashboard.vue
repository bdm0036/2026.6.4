<template>
  <div>
    <el-row :gutter="20">
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon" style="background:#e8f4fd"><el-icon :size="28" color="#409EFF"><Notebook /></el-icon></div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.totalBooks ?? '-' }}</div>
            <div class="stat-label">图书总数</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon" style="background:#fef0f0"><el-icon :size="28" color="#F56C6C"><List /></el-icon></div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.totalOrders ?? '-' }}</div>
            <div class="stat-label">订单总数</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon" style="background:#e8f8e8"><el-icon :size="28" color="#67C23A"><UserFilled /></el-icon></div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.totalUsers ?? '-' }}</div>
            <div class="stat-label">用户总数</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon" style="background:#fdf6ec"><el-icon :size="28" color="#E6A23C"><Money /></el-icon></div>
          <div class="stat-info">
            <div class="stat-value">¥{{ stats.totalRevenue ?? '-' }}</div>
            <div class="stat-label">销售额</div>
          </div>
        </el-card>
      </el-col>
    </el-row>
    <el-row :gutter="20" style="margin-top:20px">
      <el-col :span="12">
        <el-card>
          <template #header><span>最近订单</span></template>
          <el-table :data="recentOrders" size="small" v-loading="loading">
            <el-table-column prop="orderNo" label="订单号" width="180" />
            <el-table-column prop="userId" label="用户ID" width="80" />
            <el-table-column prop="totalAmount" label="金额">
              <template #default="scope">¥{{ scope.row.totalAmount }}</template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="100">
              <template #default="scope">
                <el-tag :type="statusTagType(scope.row.status)" size="small">
                  {{ statusText(scope.row.status) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="createTime" label="时间" width="160" />
          </el-table>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card>
          <template #header><span>系统信息</span></template>
          <el-descriptions :column="1" border size="small">
            <el-descriptions-item label="后端框架">Spring Boot 3.2 + Spring Cloud 2023.0</el-descriptions-item>
            <el-descriptions-item label="服务注册">Consul 1.15</el-descriptions-item>
            <el-descriptions-item label="数据库">MySQL 8.0（分库：user / product / order）</el-descriptions-item>
            <el-descriptions-item label="缓存">Redis 7</el-descriptions-item>
            <el-descriptions-item label="认证">JWT (jjwt 0.12.3)</el-descriptions-item>
            <el-descriptions-item label="前端">Vue 3 + Element Plus</el-descriptions-item>
          </el-descriptions>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { bookAPI, orderAPI, userAPI } from '../../api'

const stats = ref({})
const recentOrders = ref([])
const loading = ref(false)

onMounted(async () => {
  loading.value = true
  try {
    const [bookStats, orderStats, userList] = await Promise.all([
      bookAPI.getStatistics(),
      orderAPI.getStatistics(),
      userAPI.list({ page: 1, size: 100 }),
    ])
    stats.value = {
      totalBooks: bookStats.data?.totalBooks ?? 0,
      totalCategories: bookStats.data?.totalCategories ?? 0,
      totalOrders: orderStats.data?.totalOrders ?? 0,
      totalRevenue: orderStats.data?.totalRevenue ?? 0,
      totalUsers: userList.data?.length ?? 0,
    }
    // 最近订单
    const orderData = await orderAPI.listAll({ page: 1, size: 8 })
    recentOrders.value = orderData.data?.records ?? []
  } catch (e) {
    console.error('获取统计数据失败', e)
  }
  loading.value = false
})

function statusText(status) {
  const map = { 0: '待支付', 1: '已支付', 2: '已发货', 3: '已完成', 4: '已取消' }
  return map[status] ?? '未知'
}
function statusTagType(status) {
  const map = { 0: 'warning', 1: 'primary', 2: 'info', 3: 'success', 4: 'danger' }
  return map[status] ?? ''
}
</script>

<style scoped>
.stat-card {
  display: flex;
}
.stat-card :deep(.el-card__body) {
  display: flex;
  align-items: center;
  gap: 16px;
  width: 100%;
}
.stat-icon {
  width: 56px;
  height: 56px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.stat-value {
  font-size: 1.5rem;
  font-weight: 700;
  color: #303133;
}
.stat-label {
  font-size: 0.85rem;
  color: #909399;
  margin-top: 2px;
}
</style>

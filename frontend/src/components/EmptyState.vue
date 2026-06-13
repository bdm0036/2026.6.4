<template>
  <div class="empty-state">
    <div class="empty-icon">
      <el-icon :size="size"><component :is="icon" /></el-icon>
    </div>
    <p class="empty-text">{{ description }}</p>
    <el-button v-if="actionText" type="primary" @click="$emit('action')">
      {{ actionText }}
    </el-button>
  </div>
</template>

<script setup>
defineProps({
  icon: { type: String, default: 'Document' },
  description: { type: String, default: '暂无数据' },
  actionText: { type: String, default: '' },
  size: { type: Number, default: 64 },
})
defineEmits(['action'])
</script>

<style scoped>
.empty-state { text-align: center; padding: 80px 0; }
.empty-icon { color: #c0c4d0; margin-bottom: 16px; }
.empty-text { color: var(--text-muted); font-size: 1rem; margin-bottom: 16px; }
</style>

<template>
  <button class="theme-toggle" @click="toggle" :title="isDark ? '切换亮色模式' : '切换暗色模式'">
    <el-icon :size="18"><Sunny v-if="isDark" /><Moon v-else /></el-icon>
  </button>
</template>

<script setup>
import { useTheme } from '../composables/useTheme'
const { isDark, toggle } = useTheme()
</script>

<style scoped>
.theme-toggle {
  display: flex; align-items: center; justify-content: center;
  width: 36px; height: 36px; border-radius: 8px; border: none;
  background: transparent; cursor: pointer; color: var(--text-secondary);
  transition: background 0.2s;
}
.theme-toggle:hover { background: rgba(128, 128, 160, 0.15); }
</style>

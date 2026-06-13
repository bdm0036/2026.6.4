<template>
  <div class="book-cover" :style="coverStyle" :class="{ 'has-image': src }">
    <img v-if="src" :src="src" :alt="title" />
    <template v-else>
      <span class="cover-label" v-if="showLabel">{{ label }}</span>
    </template>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  title: { type: String, default: '' },
  src: { type: String, default: '' },
  categoryId: { type: Number, default: 0 },
  label: { type: String, default: '' },
  showLabel: { type: Boolean, default: false },
})

const categoryColors = [
  '#e67e22', '#2980b9', '#27ae60', '#8e44ad', '#d35400',
  '#e91e63', '#00bcd4', '#ff6f00', '#607d8b', '#3f51b5',
]

const bgColor = computed(() => categoryColors[props.categoryId % categoryColors.length])

const coverStyle = computed(() => ({
  background: `linear-gradient(135deg, ${bgColor.value}, ${bgColor.value}dd)`,
}))
</script>

<style scoped>
.book-cover {
  width: 100%;
  aspect-ratio: 3/4;
  border-radius: 8px;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  position: relative;
}
.book-cover img {
  width: 100%; height: 100%; object-fit: cover;
}
.cover-label {
  position: absolute; bottom: 0; left: 0; right: 0;
  padding: 6px 0; text-align: center;
  font-size: 0.7rem; font-weight: 600; color: #fff;
  background: rgba(0,0,0,0.35);
}
</style>

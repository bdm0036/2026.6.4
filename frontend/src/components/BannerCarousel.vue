<template>
  <div class="banner-carousel">
    <el-carousel :interval="5000" arrow="hover" height="320px" indicator-position="none">
      <el-carousel-item v-for="(banner, i) in banners" :key="i">
        <div class="banner-slide" :style="{ background: banner.bg }" @click="$router.push(banner.link)">
          <div class="banner-text">
            <h2>{{ banner.title }}</h2>
            <p>{{ banner.subtitle }}</p>
          </div>
          <div class="banner-emoji">{{ banner.emoji }}</div>
        </div>
      </el-carousel-item>
    </el-carousel>
  </div>
</template>

<script setup>
const banners = [
  {
    title: '读书日特惠',
    subtitle: '全场图书满100减20，满200减50',
    bg: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
    emoji: '📖',
    link: '/books',
  },
  {
    title: '新书上架',
    subtitle: '本月最新好书，抢先阅读',
    bg: 'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)',
    emoji: '🆕',
    link: '/books?sort=newest',
  },
  {
    title: '编程经典',
    subtitle: '计算机必读经典，助你升职加薪',
    bg: 'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)',
    emoji: '💻',
    link: '/books?categoryId=2',
  },
]
</script>

<style scoped>
.banner-carousel { margin: 24px 0; }
.banner-slide {
  height: 100%; display: flex; align-items: center; justify-content: space-between;
  padding: 0 60px; cursor: pointer; border-radius: 16px; overflow: hidden;
}
.banner-text h2 { font-size: 2rem; font-weight: 800; color: #fff; }
.banner-text p { margin-top: 8px; color: rgba(255,255,255,0.85); font-size: 1rem; }
.banner-emoji { font-size: 80px; }
</style>

<template>
  <div class="profile-page container">
    <div class="page-header">
      <h1>个人中心</h1>
    </div>
    <div class="profile-card">
      <div class="profile-avatar">
        <el-avatar :size="80" :src="form.avatar" />
        <el-button size="small" class="avatar-btn">更换头像</el-button>
      </div>
      <el-form :model="form" label-width="80px" size="large" class="profile-form">
        <el-form-item label="用户名">
          <el-input v-model="form.username" disabled />
        </el-form-item>
        <el-form-item label="昵称">
          <el-input v-model="form.nickname" placeholder="请输入昵称" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="form.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="form.phone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="角色">
          <el-tag>{{ form.role === 'ADMIN' ? '管理员' : '普通用户' }}</el-tag>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="saveProfile" :loading="saving">保存修改</el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useUserStore } from '../stores/user'
import { userAPI } from '../api'
import { ElMessage } from 'element-plus'

const userStore = useUserStore()
const saving = ref(false)

const form = reactive({
  username: '',
  nickname: '',
  email: '',
  phone: '',
  avatar: '',
  role: '',
})

onMounted(async () => {
  try {
    const user = await userStore.fetchProfile()
    Object.assign(form, user)
  } catch (e) {
    console.error(e)
  }
})

async function saveProfile() {
  saving.value = true
  try {
    await userAPI.updateProfile({
      nickname: form.nickname,
      email: form.email,
      phone: form.phone,
      avatar: form.avatar,
    })
    ElMessage.success('个人信息已更新')
    await userStore.fetchProfile()
  } catch (e) {
    // handled
  } finally {
    saving.value = false
  }
}
</script>

<style scoped>
.profile-page { padding-bottom: 60px; }
.profile-card {
  max-width: 560px; margin: 0 auto;
  background: #fff; border-radius: 16px; padding: 40px;
  box-shadow: 0 2px 20px rgba(0, 0, 0, 0.04);
}
.profile-avatar { text-align: center; margin-bottom: 32px; }
.avatar-btn { margin-top: 12px; display: block; margin-left: auto; margin-right: auto; }
.profile-form { max-width: 400px; margin: 0 auto; }
</style>

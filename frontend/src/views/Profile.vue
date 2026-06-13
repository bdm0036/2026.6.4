<template>
  <div class="profile-page container">
    <div class="page-header"><h1>个人中心</h1></div>
    <div class="profile-card">
      <el-tabs v-model="activeTab" tab-position="left" class="profile-tabs">
        <!-- Tab 1: 基本信息 -->
        <el-tab-pane label="基本信息" name="info">
          <div class="tab-content">
            <el-form :model="form" label-width="80px" size="large" class="info-form">
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
        </el-tab-pane>

        <!-- Tab 2: 安全设置 -->
        <el-tab-pane label="安全设置" name="security">
          <div class="tab-content">
            <el-form :model="pwdForm" label-width="100px" size="large" class="pwd-form">
              <el-form-item label="旧密码">
                <el-input v-model="pwdForm.oldPassword" type="password" show-password />
              </el-form-item>
              <el-form-item label="新密码">
                <el-input v-model="pwdForm.newPassword" type="password" show-password placeholder="至少6位" />
              </el-form-item>
              <el-form-item label="确认密码">
                <el-input v-model="pwdForm.confirmPassword" type="password" show-password />
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="changePassword" :loading="changingPwd">修改密码</el-button>
              </el-form-item>
            </el-form>
          </div>
        </el-tab-pane>

        <!-- Tab 3: 收货地址 -->
        <el-tab-pane label="收货地址" name="address">
          <div class="tab-content">
            <div class="address-list" v-if="addresses.length > 0">
              <div v-for="addr in addresses" :key="addr.id" class="address-item" :class="{ default: addr.isDefault }">
                <div class="addr-info">
                  <span class="addr-tag" v-if="addr.isDefault">默认</span>
                  <strong>{{ addr.receiverName }}</strong> {{ addr.receiverPhone }}
                  <p>{{ addr.province }}{{ addr.city }}{{ addr.district }} {{ addr.detail }}</p>
                </div>
                <div class="addr-actions">
                  <el-button size="small" text @click="editAddress(addr)">编辑</el-button>
                  <el-button size="small" text type="danger" @click="deleteAddress(addr.id)">删除</el-button>
                </div>
              </div>
            </div>
            <el-button @click="showAddressForm()" style="margin-top:8px">
              <el-icon><Plus /></el-icon> 新增地址
            </el-button>

            <!-- Address Dialog -->
            <el-dialog v-model="addressDialog" :title="editingAddr?.id ? '编辑地址' : '新增地址'" width="500px">
              <el-form :model="addrForm" label-width="80px" size="large">
                <el-form-item label="收货人" required>
                  <el-input v-model="addrForm.receiverName" placeholder="请输入收货人姓名" />
                </el-form-item>
                <el-form-item label="联系电话" required>
                  <el-input v-model="addrForm.receiverPhone" placeholder="请输入联系电话" />
                </el-form-item>
                <el-form-item label="所在地区">
                  <el-input v-model="addrForm.province" placeholder="省" style="width:30%" />
                  <el-input v-model="addrForm.city" placeholder="市" style="width:30%;margin-left:8px" />
                  <el-input v-model="addrForm.district" placeholder="区" style="width:30%;margin-left:8px" />
                </el-form-item>
                <el-form-item label="详细地址" required>
                  <el-input v-model="addrForm.detail" placeholder="街道、门牌号等" />
                </el-form-item>
                <el-form-item label="默认地址">
                  <el-switch v-model="addrForm.isDefault" :active-value="1" :inactive-value="0" />
                </el-form-item>
              </el-form>
              <template #footer>
                <el-button @click="addressDialog = false">取消</el-button>
                <el-button type="primary" @click="saveAddress" :loading="savingAddr">保存</el-button>
              </template>
            </el-dialog>
          </div>
        </el-tab-pane>

        <!-- Tab 4: 浏览历史 -->
        <el-tab-pane label="浏览历史" name="history">
          <div class="tab-content">
            <div v-if="history.length > 0">
              <div style="display:flex;justify-content:flex-end;margin-bottom:12px">
                <el-button size="small" text type="danger" @click="clearHistory">清除全部历史</el-button>
              </div>
              <div class="book-grid">
                <BookCard v-for="book in history" :key="book.id" :book="book" />
              </div>
            </div>
            <div v-else class="empty-hint">暂无浏览记录，去逛逛吧</div>
          </div>
        </el-tab-pane>
      </el-tabs>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useUserStore } from '../stores/user'
import { userAPI, addressAPI, historyAPI } from '../api'
import { ElMessage, ElMessageBox } from 'element-plus'
import BookCard from '../components/BookCard.vue'

const userStore = useUserStore()
const activeTab = ref('info')
const saving = ref(false)
const changingPwd = ref(false)
const savingAddr = ref(false)
const addresses = ref([])
const history = ref([])
const addressDialog = ref(false)
const editingAddr = ref(null)

const form = reactive({ username: '', nickname: '', email: '', phone: '', role: '' })
const pwdForm = reactive({ oldPassword: '', newPassword: '', confirmPassword: '' })
const addrForm = reactive({ receiverName: '', receiverPhone: '', province: '', city: '', district: '', detail: '', isDefault: 0 })

onMounted(async () => {
  try {
    const user = await userStore.fetchProfile()
    Object.assign(form, user)
  } catch (e) { /* handled */ }
  loadAddresses()
  loadHistory()
})

async function saveProfile() {
  saving.value = true
  try {
    await userAPI.updateProfile({ nickname: form.nickname, email: form.email, phone: form.phone })
    ElMessage.success('个人信息已更新')
    await userStore.fetchProfile()
  } catch (e) { /* handled */ }
  finally { saving.value = false }
}

async function changePassword() {
  if (!pwdForm.oldPassword || !pwdForm.newPassword || !pwdForm.confirmPassword) {
    ElMessage.warning('请填写完整信息'); return
  }
  if (pwdForm.newPassword.length < 6) {
    ElMessage.warning('新密码至少6位'); return
  }
  if (pwdForm.newPassword !== pwdForm.confirmPassword) {
    ElMessage.warning('两次密码输入不一致'); return
  }
  changingPwd.value = true
  try {
    await userAPI.changePassword(pwdForm)
    ElMessage.success('密码修改成功，请重新登录')
    userStore.clearAuth()
    location.href = '/login'
  } catch (e) { /* handled */ }
  finally { changingPwd.value = false }
}

// ============ Address CRUD ============
async function loadAddresses() {
  try {
    const res = await addressAPI.list()
    addresses.value = res.data || []
  } catch (e) { addresses.value = [] }
}

function showAddressForm(addr) {
  editingAddr.value = addr || null
  if (addr) {
    Object.assign(addrForm, addr)
  } else {
    Object.assign(addrForm, { receiverName: '', receiverPhone: '', province: '', city: '', district: '', detail: '', isDefault: 0 })
  }
  addressDialog.value = true
}

function editAddress(addr) { showAddressForm(addr) }

async function saveAddress() {
  if (!addrForm.receiverName || !addrForm.receiverPhone || !addrForm.detail) {
    ElMessage.warning('请填写完整的收货信息'); return
  }
  savingAddr.value = true
  try {
    if (editingAddr.value?.id) {
      await addressAPI.update(editingAddr.value.id, addrForm)
      ElMessage.success('地址已更新')
    } else {
      await addressAPI.add(addrForm)
      ElMessage.success('地址已添加')
    }
    addressDialog.value = false
    loadAddresses()
  } catch (e) { /* handled */ }
  finally { savingAddr.value = false }
}

async function deleteAddress(id) {
  try {
    await ElMessageBox.confirm('确定删除此地址？', '确认', { type: 'warning' })
    await addressAPI.delete(id)
    ElMessage.success('已删除')
    loadAddresses()
  } catch (e) { /* cancel or error */ }
}

// ============ Browse History ============
async function loadHistory() {
  try {
    const res = await historyAPI.list({ page: 1, size: 50 })
    history.value = (res.data || []).map(item => ({
      id: item.id,
      title: item.title,
      author: item.author,
      price: item.price,
      coverImage: item.coverImage,
      categoryId: item.categoryId,
      categoryName: item.categoryName,
      stock: item.stock > 0 ? item.stock : 0,
      tags: item.tags,
    }))
  } catch (e) { history.value = [] }
}

async function clearHistory() {
  try {
    await ElMessageBox.confirm('确定清除全部浏览历史？', '确认', { type: 'warning' })
    await historyAPI.clear()
    history.value = []
    ElMessage.success('已清除')
  } catch (e) { /* cancel */ }
}
</script>

<style scoped>
.profile-page { padding-bottom: 60px; }
.profile-card { background: var(--bg-card); border-radius: 12px; box-shadow: var(--shadow-card); overflow: hidden; }
.profile-tabs { min-height: 500px; }
.tab-content { padding: 24px 32px; max-width: 600px; }
.info-form { max-width: 400px; }
.pwd-form { max-width: 380px; }
.address-item {
  padding: 16px; border: 1px solid var(--border-color); border-radius: 8px;
  margin-bottom: 12px; display: flex; justify-content: space-between; align-items: center;
}
.address-item.default { border-color: #4f46e5; background: rgba(79,70,229,0.03); }
.addr-tag { display: inline-block; background: #4f46e5; color: #fff; font-size: 0.7rem; padding: 2px 6px; border-radius: 4px; margin-right: 8px; }
.addr-info p { margin-top: 4px; color: var(--text-muted); font-size: 0.85rem; }
.addr-actions { display: flex; gap: 4px; flex-shrink: 0; }
.empty-hint { text-align: center; padding: 60px 0; color: var(--text-muted); font-size: 0.95rem; }
.book-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 16px; }
</style>

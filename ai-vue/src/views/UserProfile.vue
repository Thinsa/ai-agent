<template>
  <main class="profile-page">
    <header class="topbar">
      <button class="back-button" type="button" @click="router.push('/')">返回首页</button>
      <button class="logout-button" type="button" @click="handleLogout">退出登录</button>
    </header>

    <section class="profile-layout">
      <aside class="profile-summary">
        <div class="avatar-large" :style="avatarStyle">
          <img v-if="user?.avatarUrl" :src="user.avatarUrl" :alt="`${user?.displayName || '用户'}头像`" />
          <span v-else>{{ user?.avatar.text }}</span>
        </div>
        <label class="avatar-upload">
          <span>{{ uploadingAvatar ? '上传中...' : '更换头像' }}</span>
          <input type="file" accept="image/png,image/jpeg,image/webp,image/gif" :disabled="uploadingAvatar" @change="handleAvatarChange" />
        </label>
        <h1>{{ user?.displayName }}</h1>
        <p class="role">{{ user?.role }}</p>
        <p class="bio">{{ user?.bio }}</p>
        <div class="summary-grid">
          <div>
            <span>用户名</span>
            <strong>{{ user?.username }}</strong>
          </div>
          <div>
            <span>注册日期</span>
            <strong>{{ user?.createdAt }}</strong>
          </div>
        </div>
      </aside>

      <section class="edit-panel">
        <div class="panel-heading">
          <p>Profile</p>
          <h2>用户信息</h2>
          <span>资料会保存到后端账号系统，并在刷新后恢复。</span>
        </div>

        <form class="profile-form" @submit.prevent="handleSubmit">
          <label class="form-field" for="displayName">
            <span>昵称</span>
            <input id="displayName" v-model.trim="form.displayName" type="text" placeholder="请输入昵称" required />
          </label>

          <label class="form-field" for="username">
            <span>用户名</span>
            <input id="username" v-model.trim="form.username" type="text" placeholder="请输入用户名" required />
          </label>

          <label class="form-field" for="email">
            <span>邮箱</span>
            <input id="email" v-model.trim="form.email" type="email" placeholder="请输入邮箱" />
          </label>

          <label class="form-field" for="role">
            <span>用户身份</span>
            <input id="role" v-model.trim="form.role" type="text" placeholder="例如：AI 应用体验官" />
          </label>

          <label class="form-field form-field-wide" for="bio">
            <span>个人简介</span>
            <textarea id="bio" v-model.trim="form.bio" rows="5" placeholder="介绍一下你自己"></textarea>
          </label>

          <div class="form-actions">
            <button class="primary-button" type="submit" :disabled="saving">{{ saving ? '保存中...' : '保存修改' }}</button>
            <button class="ghost-button" type="button" @click="resetForm">重置</button>
          </div>

          <p v-if="successMessage" class="success-message">{{ successMessage }}</p>
          <p v-if="errorMessage" class="error-message">{{ errorMessage }}</p>
        </form>
      </section>
    </section>
  </main>
</template>

<script setup>
import { computed, reactive, ref, watchEffect } from 'vue'
import { useRouter } from 'vue-router'
import { useHead } from '@vueuse/head'
import { currentUser, logout, updateUser, uploadAvatar } from '../stores/userStore'

useHead({
  title: '用户信息 - AI超级智能体应用平台',
  meta: [
    {
      name: 'description',
      content: 'AI超级智能体应用平台用户信息页面'
    }
  ]
})

const router = useRouter()
const user = currentUser
const successMessage = ref('')
const errorMessage = ref('')
const saving = ref(false)
const uploadingAvatar = ref(false)
const form = reactive({
  displayName: '',
  username: '',
  email: '',
  role: '',
  bio: ''
})

const maxAvatarSizeBytes = 5 * 1024 * 1024

const syncForm = () => {
  if (!user.value) return
  form.displayName = user.value.displayName
  form.username = user.value.username
  form.email = user.value.email
  form.role = user.value.role
  form.bio = user.value.bio
}

watchEffect(syncForm)

const avatarStyle = computed(() => {
  if (user.value?.avatarUrl) {
    return {}
  }
  return { background: user.value?.avatar.color }
})

const getErrorMessage = (error) => {
  return error?.response?.data?.message || error?.response?.data || '保存失败，请确认后端服务已启动'
}

const handleAvatarChange = async (event) => {
  const file = event.target.files?.[0]
  event.target.value = ''
  if (!file) return
  if (file.size > maxAvatarSizeBytes) {
    errorMessage.value = '头像文件不能超过 5MB。'
    return
  }
  uploadingAvatar.value = true
  successMessage.value = ''
  errorMessage.value = ''
  try {
    await uploadAvatar(file)
    successMessage.value = '头像已更新。'
    window.setTimeout(() => {
      successMessage.value = ''
    }, 2200)
  } catch (error) {
    errorMessage.value = getErrorMessage(error)
  } finally {
    uploadingAvatar.value = false
  }
}

const handleSubmit = async () => {
  saving.value = true
  successMessage.value = ''
  errorMessage.value = ''
  try {
    await updateUser({
      displayName: form.displayName,
      email: form.email,
      role: form.role,
      bio: form.bio
    })
    successMessage.value = '用户信息已保存。'
    window.setTimeout(() => {
      successMessage.value = ''
    }, 2200)
  } catch (error) {
    errorMessage.value = getErrorMessage(error)
  } finally {
    saving.value = false
  }
}

const resetForm = () => {
  syncForm()
  successMessage.value = ''
  errorMessage.value = ''
}

const handleLogout = async () => {
  await logout()
  router.push('/login')
}
</script>

<style scoped>
.profile-page {
  min-height: 100vh;
  padding: 24px;
  color: #edf7ff;
  background:
    linear-gradient(135deg, rgba(4, 9, 24, 0.97), rgba(15, 22, 45, 0.96)),
    radial-gradient(circle at 18% 18%, rgba(0, 178, 255, 0.22), transparent 28%),
    radial-gradient(circle at 82% 80%, rgba(124, 58, 237, 0.2), transparent 30%);
}

.topbar {
  width: min(1120px, 100%);
  margin: 0 auto 22px;
  display: flex;
  justify-content: space-between;
  gap: 12px;
}

.back-button,
.logout-button,
.primary-button,
.ghost-button {
  min-height: 44px;
  border: 0;
  padding: 0 18px;
  font-weight: 700;
  cursor: pointer;
  transition: transform 0.2s, border-color 0.2s, box-shadow 0.2s;
}

.back-button,
.ghost-button {
  color: #bcefff;
  border: 1px solid rgba(104, 232, 255, 0.28);
  background: rgba(255, 255, 255, 0.04);
}

.logout-button {
  color: #ffd8e4;
  border: 1px solid rgba(255, 112, 151, 0.32);
  background: rgba(255, 77, 141, 0.08);
}

.profile-layout {
  width: min(1120px, 100%);
  margin: 0 auto;
  display: grid;
  grid-template-columns: 360px 1fr;
  gap: 24px;
}

.profile-summary,
.edit-panel {
  border: 1px solid rgba(255, 255, 255, 0.14);
  background: rgba(10, 17, 36, 0.78);
  box-shadow: 0 24px 80px rgba(0, 0, 0, 0.32);
  backdrop-filter: blur(16px);
}

.profile-summary {
  padding: 34px;
  align-self: start;
}

.avatar-large {
  width: 104px;
  height: 104px;
  display: grid;
  place-items: center;
  border-radius: 28px;
  color: white;
  font-size: 2rem;
  font-weight: 900;
  box-shadow: 0 18px 44px rgba(0, 178, 255, 0.24);
  overflow: hidden;
}

.avatar-large img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.avatar-upload {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-height: 38px;
  margin-top: 14px;
  padding: 0 14px;
  color: #bcefff;
  border: 1px solid rgba(104, 232, 255, 0.28);
  background: rgba(255, 255, 255, 0.04);
  font-weight: 700;
  cursor: pointer;
}

.avatar-upload input {
  display: none;
}

.profile-summary h1 {
  margin: 24px 0 6px;
  font-size: 2rem;
  letter-spacing: 0;
}

.role {
  margin: 0;
  color: #68e8ff;
  font-weight: 700;
}

.bio {
  margin: 20px 0;
  color: rgba(237, 247, 255, 0.72);
  line-height: 1.8;
}

.summary-grid {
  display: grid;
  gap: 12px;
}

.summary-grid div {
  padding: 14px;
  background: rgba(255, 255, 255, 0.06);
  border: 1px solid rgba(255, 255, 255, 0.08);
}

.summary-grid span {
  display: block;
  color: rgba(237, 247, 255, 0.58);
  font-size: 0.86rem;
  margin-bottom: 4px;
}

.summary-grid strong {
  color: #fff;
  word-break: break-word;
}

.edit-panel {
  padding: 34px;
}

.panel-heading {
  margin-bottom: 24px;
}

.panel-heading p {
  margin: 0 0 6px;
  color: #68e8ff;
  text-transform: uppercase;
  font-size: 0.86rem;
}

.panel-heading h2 {
  margin: 0;
  font-size: 2rem;
  letter-spacing: 0;
}

.panel-heading span {
  display: block;
  margin-top: 10px;
  color: rgba(237, 247, 255, 0.64);
  line-height: 1.7;
}

.profile-form {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 18px;
}

.form-field {
  display: grid;
  gap: 8px;
  color: rgba(237, 247, 255, 0.82);
  text-align: left;
}

.form-field-wide,
.form-actions,
.success-message,
.error-message {
  grid-column: 1 / -1;
}

.form-field input,
.form-field textarea {
  width: 100%;
  border: 1px solid rgba(255, 255, 255, 0.16);
  background: rgba(255, 255, 255, 0.08);
  color: #fff;
  padding: 12px 14px;
  font-size: 1rem;
  outline: none;
  resize: vertical;
  transition: border-color 0.2s, box-shadow 0.2s;
}

.form-field input {
  min-height: 48px;
}

.form-field input:focus,
.form-field textarea:focus {
  border-color: #68e8ff;
  box-shadow: 0 0 0 3px rgba(104, 232, 255, 0.16);
}

.form-actions {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
  margin-top: 6px;
}

.primary-button {
  color: #06111f;
  background: linear-gradient(90deg, #00f0ff, #7cdbff);
  box-shadow: 0 12px 34px rgba(0, 178, 255, 0.26);
}

.success-message {
  margin: 0;
  color: #7dffce;
}

.error-message {
  margin: 0;
  color: #ff9ab7;
}

.back-button:hover,
.logout-button:hover,
.primary-button:hover,
.ghost-button:hover {
  transform: translateY(-2px);
}

.back-button:focus-visible,
.logout-button:focus-visible,
.primary-button:focus-visible,
.ghost-button:focus-visible {
  outline: 3px solid rgba(104, 232, 255, 0.36);
  outline-offset: 2px;
}

@media (max-width: 900px) {
  .profile-layout {
    grid-template-columns: 1fr;
  }

  .profile-form {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 520px) {
  .profile-page {
    padding: 16px;
  }

  .topbar {
    flex-direction: column;
  }

  .profile-summary,
  .edit-panel {
    padding: 24px;
  }
}
</style>

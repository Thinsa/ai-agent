<template>
  <main class="login-page">
    <section class="login-shell">
      <div class="brand-panel">
        <div class="brand-mark">AI</div>
        <p class="eyebrow">Yu AI Agent</p>
        <h1>登录智能体应用平台</h1>
        <p class="brand-copy">进入后可体验 AI 恋爱大师、超级智能体，并管理你的用户资料。</p>
        <div class="signal-list" aria-label="登录页功能说明">
          <span>账号登录</span>
          <span>资料管理</span>
          <span>JWT 鉴权</span>
        </div>
      </div>

      <form class="login-card" @submit.prevent="handleSubmit">
        <div class="form-heading">
          <h2>{{ isRegisterMode ? '创建账号' : '欢迎回来' }}</h2>
          <p>{{ isRegisterMode ? '先注册账号，再进入智能体应用平台。' : '使用已注册账号登录后端服务。' }}</p>
        </div>

        <label class="form-field" for="username">
          <span>用户名</span>
          <input id="username" v-model.trim="form.username" type="text" autocomplete="username" placeholder="请输入用户名" required />
        </label>

        <label v-if="isRegisterMode" class="form-field" for="displayName">
          <span>昵称</span>
          <input id="displayName" v-model.trim="form.displayName" type="text" autocomplete="name" placeholder="请输入昵称" />
        </label>

        <label v-if="isRegisterMode" class="form-field" for="email">
          <span>邮箱</span>
          <input id="email" v-model.trim="form.email" type="email" autocomplete="email" placeholder="请输入邮箱" />
        </label>

        <label class="form-field" for="password">
          <span>密码</span>
          <input id="password" v-model="form.password" type="password" autocomplete="current-password" placeholder="请输入密码" required />
        </label>

        <p v-if="errorMessage" class="error-message">{{ errorMessage }}</p>

        <button class="primary-button" type="submit" :disabled="loading">{{ loading ? '处理中...' : (isRegisterMode ? '注册并登录' : '登录') }}</button>

        <button class="ghost-button" type="button" :disabled="loading" @click="toggleMode">
          {{ isRegisterMode ? '已有账号，去登录' : '没有账号，先注册' }}
        </button>
      </form>
    </section>
  </main>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useHead } from '@vueuse/head'
import { login, register } from '../stores/userStore'

useHead({
  title: '登录 - AI超级智能体应用平台',
  meta: [
    {
      name: 'description',
      content: 'AI超级智能体应用平台登录页面'
    }
  ]
})

const router = useRouter()
const errorMessage = ref('')
const loading = ref(false)
const isRegisterMode = ref(false)
const form = reactive({
  username: '',
  password: '',
  displayName: '',
  email: ''
})

const getErrorMessage = (error) => {
  return error?.response?.data?.message || error?.response?.data || error?.message || '请求失败，请确认后端服务已启动'
}

const handleSubmit = async () => {
  if (!form.username || !form.password) {
    errorMessage.value = '请输入用户名和密码'
    return
  }

  loading.value = true
  errorMessage.value = ''
  try {
    if (isRegisterMode.value) {
      await register({
        username: form.username,
        password: form.password,
        displayName: form.displayName || form.username,
        email: form.email
      })
    }
    await login({
      username: form.username,
      password: form.password
    })
    router.push('/')
  } catch (error) {
    errorMessage.value = getErrorMessage(error)
  } finally {
    loading.value = false
  }
}

const toggleMode = () => {
  isRegisterMode.value = !isRegisterMode.value
  errorMessage.value = ''
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: grid;
  place-items: center;
  padding: 32px 20px;
  color: #eef7ff;
  background:
    linear-gradient(135deg, rgba(3, 8, 22, 0.96), rgba(14, 18, 42, 0.94)),
    radial-gradient(circle at 16% 20%, rgba(0, 178, 255, 0.24), transparent 28%),
    radial-gradient(circle at 84% 75%, rgba(255, 77, 141, 0.2), transparent 30%);
}

.login-shell {
  width: min(1040px, 100%);
  display: grid;
  grid-template-columns: 1.05fr 0.95fr;
  gap: 28px;
  align-items: stretch;
}

.brand-panel,
.login-card {
  border: 1px solid rgba(255, 255, 255, 0.14);
  background: rgba(10, 17, 36, 0.78);
  box-shadow: 0 24px 80px rgba(0, 0, 0, 0.32);
  backdrop-filter: blur(16px);
}

.brand-panel {
  min-height: 520px;
  padding: 48px;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.brand-mark {
  width: 66px;
  height: 66px;
  display: grid;
  place-items: center;
  border-radius: 18px;
  margin-bottom: 28px;
  color: #06111f;
  font-weight: 900;
  letter-spacing: 0;
  background: linear-gradient(135deg, #00f0ff, #7c3aed 58%, #ff4d8d);
}

.eyebrow {
  margin: 0 0 10px;
  color: #68e8ff;
  font-size: 0.9rem;
  letter-spacing: 0;
  text-transform: uppercase;
}

.brand-panel h1,
.form-heading h2 {
  margin: 0;
  letter-spacing: 0;
}

.brand-panel h1 {
  max-width: 520px;
  font-size: 3rem;
  line-height: 1.12;
}

.brand-copy {
  max-width: 520px;
  margin: 22px 0 0;
  color: rgba(238, 247, 255, 0.72);
  font-size: 1rem;
  line-height: 1.8;
}

.signal-list {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin-top: 34px;
}

.signal-list span {
  padding: 8px 12px;
  border: 1px solid rgba(104, 232, 255, 0.28);
  color: #bcefff;
  background: rgba(0, 178, 255, 0.08);
  font-size: 0.9rem;
}

.login-card {
  padding: 42px;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.form-heading {
  margin-bottom: 28px;
}

.form-heading h2 {
  font-size: 2rem;
}

.form-heading p {
  margin: 10px 0 0;
  color: rgba(238, 247, 255, 0.66);
  line-height: 1.7;
}

.form-field {
  display: grid;
  gap: 8px;
  margin-bottom: 18px;
  color: rgba(238, 247, 255, 0.82);
  text-align: left;
}

.form-field input {
  width: 100%;
  min-height: 48px;
  border: 1px solid rgba(255, 255, 255, 0.16);
  background: rgba(255, 255, 255, 0.08);
  color: #fff;
  padding: 0 14px;
  font-size: 1rem;
  outline: none;
  transition: border-color 0.2s, box-shadow 0.2s;
}

.form-field input:focus {
  border-color: #68e8ff;
  box-shadow: 0 0 0 3px rgba(104, 232, 255, 0.16);
}

.error-message {
  margin: 0 0 16px;
  color: #ff9ab7;
}

.primary-button,
.ghost-button {
  min-height: 48px;
  border: 0;
  font-size: 1rem;
  font-weight: 700;
  cursor: pointer;
  transition: transform 0.2s, box-shadow 0.2s, border-color 0.2s;
}

.primary-button {
  color: #06111f;
  background: linear-gradient(90deg, #00f0ff, #7cdbff);
  box-shadow: 0 12px 34px rgba(0, 178, 255, 0.26);
}

.ghost-button {
  margin-top: 12px;
  color: #bcefff;
  background: transparent;
  border: 1px solid rgba(104, 232, 255, 0.24);
}

.primary-button:hover,
.ghost-button:hover {
  transform: translateY(-2px);
}

.primary-button:focus-visible,
.ghost-button:focus-visible {
  outline: 3px solid rgba(104, 232, 255, 0.36);
  outline-offset: 2px;
}

@media (max-width: 820px) {
  .login-shell {
    grid-template-columns: 1fr;
  }

  .brand-panel {
    min-height: auto;
    padding: 34px;
  }

  .brand-panel h1 {
    font-size: 2.25rem;
  }

  .login-card {
    padding: 30px;
  }
}

@media (max-width: 480px) {
  .login-page {
    padding: 18px;
  }

  .brand-panel,
  .login-card {
    padding: 24px;
  }

  .brand-panel h1 {
    font-size: 1.85rem;
  }
}
</style>

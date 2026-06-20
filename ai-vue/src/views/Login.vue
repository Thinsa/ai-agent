<template>
  <main class="login-page">
    <CosmicBackground density="high" :overlay="true" />

    <div class="layout-grid">
      <!-- ═══ 左侧：品牌殿堂 ═══ -->
      <div class="brand-temple">
        <!-- 桥形徽标 -->
        <div class="brand-icon-box">
          <svg class="brand-icon-main" viewBox="0 0 80 80" fill="none">
            <!-- 拱桥 -->
            <path d="M12 62 C12 38, 22 18, 52 18 C42 30, 38 40, 52 64 C34 64, 22 62, 12 62Z"
                  fill="url(#bridgeGrad)" opacity="0.95"/>
            <!-- 桥塔 -->
            <rect x="46" y="18" width="5" height="20" rx="2" fill="url(#bridgeGrad)" opacity="0.6"/>
            <rect x="38" y="18" width="5" height="14" rx="2" fill="url(#bridgeGrad)" opacity="0.4"/>
            <!-- 吊索 -->
            <line x1="48" y1="18" x2="30" y2="44" stroke="url(#bridgeGrad)" stroke-width="1" opacity="0.35"/>
            <line x1="48" y1="18" x2="52" y2="46" stroke="url(#bridgeGrad)" stroke-width="1" opacity="0.35"/>
            <line x1="48" y1="18" x2="38" y2="38" stroke="url(#bridgeGrad)" stroke-width="0.8" opacity="0.25"/>
            <!-- 星光终点 -->
            <circle cx="52" cy="64" r="3.5" fill="var(--color-glow)" opacity="0.9"/>
            <circle cx="12" cy="62" r="2.5" fill="var(--color-glow)" opacity="0.6"/>
            <defs>
              <linearGradient id="bridgeGrad" x1="0" y1="0" x2="1" y2="1">
                <stop offset="0%"   stop-color="var(--color-aurora-3)"/>
                <stop offset="50%"  stop-color="var(--color-glow)"/>
                <stop offset="100%" stop-color="var(--color-aurora-1)"/>
              </linearGradient>
            </defs>
          </svg>
        </div>

        <p class="brand-pre">LinkMind</p>
        <h1 class="brand-title-main">灵 桥</h1>
        <p class="brand-slogan">心与智之间，搭一座桥</p>

        <!-- 底部品牌理念 -->
        <div class="brand-belief">
          <div class="belief-item">
            <span class="belief-dot"></span>
            <span>知心 Soul · 情感陪伴</span>
          </div>
          <div class="belief-item">
            <span class="belief-dot" style="--dot-color: var(--color-aurora-2);"></span>
            <span>灵语 Spark · 互动叙事</span>
          </div>
          <div class="belief-item">
            <span class="belief-dot" style="--dot-color: var(--color-aurora-1);"></span>
            <span>极智 Core · 全能助手</span>
          </div>
        </div>

        <!-- 底部光弧 -->
        <div class="temple-arc"></div>
      </div>

      <!-- ═══ 右侧：登录泊口 ═══ -->
      <div class="dock-panel">
        <div class="dock-inner">
          <!-- 泊口标志 -->
          <div class="dock-badge">
            <div class="badge-light"></div>
            <span>{{ isRegisterMode ? 'REGISTER' : 'LOGIN' }}</span>
          </div>

          <h2 class="dock-title">{{ isRegisterMode ? '创建你的身份' : '欢迎回到灵桥' }}</h2>
          <p class="dock-subtitle">
            {{ isRegisterMode ? '注册后即可使用全部智能体服务' : '连接智慧，点亮灵感' }}
          </p>

          <form class="dock-form" @submit.prevent="handleSubmit">
            <div class="dock-field">
              <span class="dock-field-label">
                <svg viewBox="0 0 18 18" fill="none" stroke="currentColor" stroke-width="1.4"><circle cx="9" cy="6" r="3"/><path d="M2 16c0-3.3 2.7-6 7-6s7 2.7 7 6"/></svg>
                用户名
              </span>
              <div class="dock-input-wrap">
                <input id="username" v-model.trim="form.username" type="text" autocomplete="username" placeholder="输入你的用户名" required />
                <span class="input-underline"></span>
              </div>
            </div>

            <template v-if="isRegisterMode">
              <div class="dock-field">
                <span class="dock-field-label">
                  <svg viewBox="0 0 18 18" fill="none" stroke="currentColor" stroke-width="1.4"><path d="M9 2a2.5 2.5 0 1 0 0 5 2.5 2.5 0 0 0 0-5ZM3 16v-.5A6 6 0 0 1 15 15.5V16"/></svg>
                  昵称
                </span>
                <div class="dock-input-wrap">
                  <input id="displayName" v-model.trim="form.displayName" type="text" autocomplete="name" placeholder="你希望别人怎么称呼你" />
                  <span class="input-underline"></span>
                </div>
              </div>

              <div class="dock-field">
                <span class="dock-field-label">
                  <svg viewBox="0 0 18 18" fill="none" stroke="currentColor" stroke-width="1.4"><rect x="2" y="3" width="14" height="12" rx="2"/><path d="m2 5 7 5 7-5"/></svg>
                  邮箱
                </span>
                <div class="dock-input-wrap">
                  <input id="email" v-model.trim="form.email" type="email" autocomplete="email" placeholder="your@email.com" />
                  <span class="input-underline"></span>
                </div>
              </div>
            </template>

            <div class="dock-field">
              <span class="dock-field-label">
                <svg viewBox="0 0 18 18" fill="none" stroke="currentColor" stroke-width="1.4"><rect x="2.5" y="8" width="13" height="8" rx="2"/><path d="M5.5 8V4.5a3.5 3.5 0 0 1 7 0V8"/><circle cx="9" cy="12.5" r="1.2"/></svg>
                密码
              </span>
              <div class="dock-input-wrap">
                <input id="password" v-model="form.password" type="password" autocomplete="current-password" placeholder="······" required />
                <span class="input-underline"></span>
              </div>
            </div>

            <!-- 错误提示 -->
            <div v-if="errorMessage" class="dock-error">
              <svg viewBox="0 0 18 18" fill="currentColor"><path d="M9 1.5a7.5 7.5 0 1 0 0 15 7.5 7.5 0 0 0 0-15Zm0 2.5a1 1 0 0 1 1 1v4a1 1 0 0 1-2 0V5a1 1 0 0 1 1-1Zm0 8a1 1 0 1 1 0-2 1 1 0 0 1 0 2Z"/></svg>
              {{ errorMessage }}
            </div>

            <!-- 提交按钮 -->
            <button class="dock-submit" type="submit" :disabled="loading">
              <span v-if="loading" class="btn-loading">
                <i class="spinner"></i>处理中...
              </span>
              <span v-else class="btn-content">
                {{ isRegisterMode ? '注册并进入' : '进入灵桥' }}
                <svg viewBox="0 0 20 20" fill="none" stroke="currentColor" stroke-width="2"><path d="M4 10h12M11 4l6 6-6 6"/></svg>
              </span>
            </button>
          </form>

          <!-- 切换模式 -->
          <button class="dock-switch" type="button" :disabled="loading" @click="toggleMode">
            {{ isRegisterMode ? '已有账号？去登录' : '新用户？创建账号' }}
            <svg viewBox="0 0 16 16" fill="none" stroke="currentColor" stroke-width="1.5" class="switch-icon"><path d="M6 3l5 5-5 5"/></svg>
          </button>

          <!-- 底部装饰线 -->
          <div class="dock-footer">
            <span></span><span>LinkMind © 2026</span><span></span>
          </div>
        </div>
      </div>
    </div>
  </main>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useHead } from '@vueuse/head'
import { login, register } from '../stores/userStore'
import CosmicBackground from '../components/CosmicBackground.vue'

useHead({
  title: '登录 - LinkMind 灵桥',
  meta: [
    {
      name: 'description',
      content: 'LinkMind 灵桥 — 心与智之间，搭一座桥。'
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
    await login({ username: form.username, password: form.password })
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
/* ═══════════════════════════════════════════════
   LOGIN — COSMIC PORTAL LAYOUT
   ═══════════════════════════════════════════════ */

.login-page {
  min-height: 100vh;
  display: flex;
  position: relative;
  overflow: hidden;
  background: #080c16;
}

/* ═══════ 双栏布局 ═══════ */
.layout-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  width: 100%;
  min-height: 100vh;
  position: relative;
  z-index: 2;
}

/* ═══════════════════════════════════
   左侧：品牌殿堂
   ═══════════════════════════════════ */
.brand-temple {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  position: relative;
  padding: 60px 40px;
  overflow: hidden;
  background:
    radial-gradient(ellipse at 35% 50%, rgba(180,160,232,0.06), transparent 60%),
    radial-gradient(ellipse at 65% 50%, rgba(126,200,224,0.04), transparent 60%);
}

/* 品牌图标 */
.brand-icon-box {
  width: 120px; height: 120px;
  display: flex; align-items: center; justify-content: center;
  position: relative;
  z-index: 3;
  margin-bottom: 24px;
  filter: drop-shadow(0 0 40px rgba(240,192,96,0.10));
  animation: icon-float 5s var(--ease-in-out) infinite;
}
@keyframes icon-float {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-6px); }
}
.brand-icon-main {
  width: 100%; height: 100%;
}

.brand-pre {
  font-size: 0.75rem;
  letter-spacing: 0.35em;
  text-transform: uppercase;
  color: var(--color-text-2);
  margin: 0 0 4px;
  position: relative; z-index: 3;
  font-weight: 500;
}

.brand-title-main {
  font-family: var(--font-display);
  font-size: 5.5rem;
  font-weight: 800;
  background: linear-gradient(135deg, var(--color-aurora-3), var(--color-glow), var(--color-aurora-1));
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  margin: 0 0 10px;
  letter-spacing: 0.12em;
  line-height: 1;
  position: relative; z-index: 3;
}

.brand-slogan {
  font-size: 0.95rem;
  color: var(--color-text-2);
  letter-spacing: 0.15em;
  margin: 0 0 48px;
  position: relative; z-index: 3;
  font-weight: 400;
}

/* 品牌理念列表 */
.brand-belief {
  display: flex;
  flex-direction: column;
  gap: 12px;
  position: relative; z-index: 3;
}
.belief-item {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 0.82rem;
  color: var(--color-text-2);
  letter-spacing: 0.03em;
}
.belief-dot {
  --dot-color: var(--color-aurora-3);
  width: 6px; height: 6px;
  border-radius: 50%;
  background: var(--dot-color);
  opacity: 0.6;
  flex-shrink: 0;
}

.temple-arc {
  position: absolute;
  bottom: 0; left: 0; right: 0;
  height: 1px;
  background: linear-gradient(90deg, transparent, rgba(255,255,255,0.04), transparent);
}

/* ═══════════════════════════════════
   右侧：登录泊口
   ═══════════════════════════════════ */
.dock-panel {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px;
  position: relative;
  background:
    radial-gradient(ellipse at 40% 50%, rgba(240,192,96,0.03), transparent 50%);
}
.dock-panel::before {
  content: '';
  position: absolute;
  left: 0; top: 10%; bottom: 10%;
  width: 1px;
  background: linear-gradient(180deg, transparent, rgba(255,255,255,0.04), transparent);
}

.dock-inner {
  width: min(420px, 100%);
  display: flex;
  flex-direction: column;
}

.dock-badge {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 5px 14px;
  border: 1px solid rgba(255,255,255,0.06);
  border-radius: 999px;
  font-size: 0.65rem;
  letter-spacing: 0.25em;
  color: var(--color-text-2);
  background: rgba(255,255,255,0.02);
  margin-bottom: 28px;
  align-self: flex-start;
}
.badge-light {
  width: 5px; height: 5px;
  border-radius: 50%;
  background: #7ec8a0;
  animation: badge-pulse 2s var(--ease-in-out) infinite;
}
@keyframes badge-pulse {
  0%, 100% { opacity: 0.4; box-shadow: 0 0 4px #7ec8a0; }
  50% { opacity: 1; box-shadow: 0 0 8px #7ec8a0; }
}

.dock-title {
  font-family: var(--font-display);
  font-size: 1.6rem;
  font-weight: 700;
  color: var(--color-text-1);
  margin: 0 0 8px;
  letter-spacing: 0.02em;
}
.dock-subtitle {
  font-size: 0.85rem;
  color: var(--color-text-2);
  margin: 0 0 36px;
  line-height: 1.5;
}

/* ── 表单 ── */
.dock-form {
  display: flex;
  flex-direction: column;
  gap: 0;
}

/* 字段 */
.dock-field {
  margin-bottom: 20px;
}
.dock-field-label {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 0.78rem;
  color: var(--color-text-2);
  margin-bottom: 6px;
  font-weight: 500;
}
.dock-field-label svg {
  width: 14px; height: 14px;
  opacity: 0.4;
}

.dock-input-wrap {
  position: relative;
}
.dock-input-wrap input {
  width: 100%;
  height: 50px;
  padding: 0 0 0 2px;
  font-size: 1rem;
  font-family: var(--font-body);
  color: var(--color-text-1);
  background: transparent;
  border: none;
  border-bottom: 1px solid rgba(255,255,255,0.06);
  outline: none;
  transition: border-color 0.3s var(--ease-out);
  letter-spacing: 0.02em;
}
.dock-input-wrap input::placeholder {
  color: var(--color-text-3);
  font-weight: 300;
  font-size: 0.92rem;
}
.dock-input-wrap input:focus {
  border-bottom-color: rgba(240,192,96,0.20);
}

/* 底部发光条 */
.input-underline {
  position: absolute;
  bottom: 0; left: 50%;
  width: 0; height: 1px;
  background: linear-gradient(90deg, var(--color-glow), var(--color-aurora-1));
  border-radius: 1px;
  transition: width 0.4s var(--ease-out), left 0.4s var(--ease-out);
  pointer-events: none;
  opacity: 0;
}
.dock-input-wrap input:focus ~ .input-underline {
  width: 100%;
  left: 0;
  opacity: 0.6;
}

/* 调试模式下占位符计数修复 */
.dock-input-wrap input:-webkit-autofill,
.dock-input-wrap input:-webkit-autofill:hover,
.dock-input-wrap input:-webkit-autofill:focus {
  -webkit-text-fill-color: var(--color-text-1);
  -webkit-box-shadow: 0 0 0 30px #080c16 inset !important;
  transition: background-color 5000s ease-in-out 0s;
}

/* ── 错误 ── */
.dock-error {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 14px;
  margin: 4px 0 16px;
  font-size: 0.82rem;
  color: #f0a0a0;
  background: rgba(240,120,120,0.04);
  border: 1px solid rgba(240,120,120,0.08);
  border-radius: 8px;
  line-height: 1.4;
}
.dock-error svg {
  width: 16px; height: 16px;
  flex-shrink: 0;
  opacity: 0.6;
}

/* ── 提交按钮 ── */
.dock-submit {
  width: 100%;
  height: 50px;
  border: none;
  border-radius: 10px;
  font-size: 0.95rem;
  font-weight: 600;
  font-family: var(--font-body);
  color: #080c16;
  background: linear-gradient(135deg, #f0c060 30%, var(--color-aurora-1));
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: transform 0.2s var(--ease-out),
              box-shadow 0.2s var(--ease-out),
              filter 0.2s var(--ease-out);
}
.dock-submit::before {
  content: '';
  position: absolute;
  inset: 0;
  border-radius: inherit;
  background: linear-gradient(135deg, rgba(255,255,255,0.15), transparent 60%);
  pointer-events: none;
}
.dock-submit::after {
  content: '';
  position: absolute;
  top: 0; left: -75%;
  width: 50%; height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255,255,255,0.10), transparent);
  transform: skewX(-20deg);
  transition: left 0.5s var(--ease-out);
  pointer-events: none;
}
.dock-submit:hover:not(:disabled)::after { left: 150%; }
.dock-submit:hover:not(:disabled) {
  transform: translateY(-1px);
  box-shadow: 0 8px 32px rgba(240,192,96,0.18), 0 0 60px rgba(240,192,96,0.04);
  filter: brightness(1.05);
}
.dock-submit:active:not(:disabled) { transform: translateY(0); }
.dock-submit:disabled { opacity: 0.45; cursor: not-allowed; }
.dock-submit:focus-visible {
  outline: 2px solid rgba(240,192,96,0.25);
  outline-offset: 2px;
}

.btn-content {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}
.btn-content svg {
  width: 16px; height: 16px;
  transition: transform 0.25s var(--ease-out);
}
.dock-submit:hover:not(:disabled) .btn-content svg {
  transform: translateX(4px);
}

.btn-loading {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}
.spinner {
  width: 16px; height: 16px;
  border: 2px solid rgba(8,12,22,0.15);
  border-top: 2px solid #080c16;
  border-radius: 50%;
  animation: spin 0.7s linear infinite;
}
@keyframes spin { to { transform: rotate(360deg); } }

/* ── 切换按钮 ── */
.dock-switch {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  margin-top: 20px;
  padding: 10px;
  background: none;
  border: none;
  font-size: 0.82rem;
  font-family: var(--font-body);
  color: var(--color-text-2);
  cursor: pointer;
  transition: color 0.25s var(--ease-out), gap 0.25s var(--ease-out);
  align-self: flex-start;
}
.dock-switch:hover:not(:disabled) {
  color: var(--color-glow);
  gap: 10px;
}
.dock-switch:disabled { opacity: 0.35; cursor: not-allowed; }
.dock-switch:focus-visible {
  outline: 2px solid rgba(240,192,96,0.15);
  outline-offset: 2px;
  border-radius: 4px;
}
.switch-icon {
  width: 14px; height: 14px;
  transition: transform 0.25s var(--ease-out);
}
.dock-switch:hover:not(:disabled) .switch-icon {
  transform: translateX(3px);
}

/* ── 底部 ── */
.dock-footer {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-top: 40px;
}
.dock-footer span:first-child,
.dock-footer span:last-child {
  flex: 1;
  height: 1px;
  background: rgba(255,255,255,0.03);
}
.dock-footer span:nth-child(2) {
  font-size: 0.65rem;
  color: var(--color-text-3);
  white-space: nowrap;
  letter-spacing: 0.08em;
}

/* ═══════════════════════════════════
   响应式
   ═══════════════════════════════════ */
@media (max-width: 860px) {
  .layout-grid {
    grid-template-columns: 1fr;
  }
  .brand-temple {
    padding: 50px 30px 30px;
    min-height: auto;
  }
  .brand-title-main {
    font-size: 3.8rem;
  }
  .brand-belief { display: none; }
  .temple-arc { display: none; }
  .dock-panel::before { display: none; }
  .dock-panel { padding: 30px; }
}

@media (max-width: 520px) {
  .brand-temple { padding: 40px 20px 20px; }
  .brand-title-main { font-size: 2.8rem; letter-spacing: 0.08em; }
  .brand-icon-box { width: 90px; height: 90px; margin-bottom: 16px; }
  .brand-slogan { font-size: 0.82rem; margin-bottom: 0; }
  .dock-panel { padding: 24px 20px; }
  .dock-title { font-size: 1.3rem; }
}
</style>

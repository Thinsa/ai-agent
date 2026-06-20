<template>
  <div class="home-container">
    <CosmicBackground density="medium" />

    <!-- ═══ 顶部栏 ═══ -->
    <header class="topbar">
      <div class="topbar-left">
        <div class="brand-dot"></div>
        <span class="brand-mini">LinkMind</span>
      </div>
      <div class="topbar-center">
        <div class="topbar-pulse"></div>
        <span class="topbar-status">系统在线</span>
      </div>
      <div class="topbar-right">
        <button class="topbar-btn settings-btn" type="button" @click="navigateTo('/background-settings')" title="背景设置" aria-label="背景设置">
          <svg viewBox="0 0 20 20" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round">
            <rect x="2.5" y="2.5" width="15" height="15" rx="3" />
            <circle cx="10" cy="10" r="2.5" />
            <path d="M7 10h6M10 7v6" />
          </svg>
        </button>
        <button class="topbar-btn profile-btn" type="button" @click="navigateTo('/profile')" aria-label="用户信息">
          <span class="profile-avatar" :style="currentUser?.avatarUrl ? {} : { background: currentUser?.avatar?.color || 'var(--color-base-4)' }">
            <img v-if="currentUser?.avatarUrl" :src="currentUser.avatarUrl" :alt="`${currentUser?.displayName || ''}头像`" />
            <span v-else>{{ currentUser?.avatar?.text || 'U' }}</span>
          </span>
          <span class="profile-name">{{ currentUser?.displayName || '用户' }}</span>
        </button>
      </div>
    </header>

    <!-- ═══ 品牌区 ═══ -->
    <section class="hero">
      <div class="hero-glow"></div>
      <!-- 桥形装饰 -->
      <svg class="hero-bridge" viewBox="0 0 200 60" preserveAspectRatio="none" aria-hidden="true">
        <path d="M0 55 Q50 5 100 20 Q150 35 200 10" fill="none" stroke="url(#bridgeLine)" stroke-width="0.5" opacity="0.3"/>
        <path d="M0 50 Q50 10 100 22 Q150 38 200 15" fill="none" stroke="url(#bridgeLine)" stroke-width="0.3" opacity="0.15"/>
        <defs>
          <linearGradient id="bridgeLine" x1="0" y1="0" x2="1" y2="0">
            <stop offset="0%" stop-color="var(--color-aurora-3)" stop-opacity="0"/>
            <stop offset="30%" stop-color="var(--color-aurora-3)"/>
            <stop offset="50%" stop-color="var(--color-glow)"/>
            <stop offset="70%" stop-color="var(--color-aurora-1)"/>
            <stop offset="100%" stop-color="var(--color-aurora-1)" stop-opacity="0"/>
          </linearGradient>
        </defs>
      </svg>
      <h1 class="hero-title">LinkMind 灵桥</h1>
      <p class="hero-sub">心与智之间，搭一座桥</p>
    </section>

    <!-- ═══ 三大智能体 ═══ -->
    <section class="agents-strip">
      <AgentCard v-for="agent in agents" :key="agent.id" v-bind="agent" />
    </section>

    <!-- ═══ 底部 ═══ -->
    <footer class="home-footer">
      <div class="footer-line"></div>
      <div class="footer-content">
        <span class="footer-copy">LinkMind © 2026</span>
        <span class="footer-sep">·</span>
        <span class="footer-tag">连接智慧，点亮灵感</span>
      </div>
    </footer>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { useHead } from '@vueuse/head'
import { currentUser } from '../stores/userStore'
import CosmicBackground from '../components/CosmicBackground.vue'
import AgentCard from '../components/AgentCard.vue'

useHead({
  title: 'LinkMind 灵桥 — 智能 AI 对话平台',
  meta: [
    {
      name: 'description',
      content: 'LinkMind 灵桥：知心 Soul 情感伴侣 + 灵语 Spark 互动叙事 + 极智 Core 全能助手。'
    }
  ]
})

const router = useRouter()
const navigateTo = (path) => { router.push(path) }

const agents = [
  { id: 'soul', title: '知心 Soul', tagline: '温柔陪伴，共情倾听', description: '情感咨询 · 关系建议 · 心灵树洞', actionText: '进入心灵空间', theme: 'soul', to: '/love-master' },
  { id: 'spark', title: '灵语 Spark', tagline: '分支叙事，互动创作', description: '互动剧本 · 分支结局 · AI 叙事', actionText: '开启故事之旅', theme: 'spark', to: '/chat' },
  { id: 'core', title: '极智 Core', tagline: '工具调用，全能解决', description: '联网搜索 · 图片生成 · ReAct 推理', actionText: '启动极智引擎', theme: 'core', to: '/super-agent' },
]
</script>

<style scoped>
/* ═══════════════════════════════════════════════
   HOME — COSMIC COMMAND CENTER
   ═══════════════════════════════════════════════ */
.home-container {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: #080c16;
  position: relative;
  overflow: hidden;
}

/* ═══════════════════════════════════
   顶部栏
   ═══════════════════════════════════ */
.topbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 28px;
  position: relative; z-index: 10;
}
.topbar-left { display: flex; align-items: center; gap: 10px; }
.brand-dot {
  width: 8px; height: 8px; border-radius: 50%;
  background: var(--color-glow);
  box-shadow: 0 0 8px rgba(240,192,96,0.3);
  animation: dot-pulse 2.5s var(--ease-in-out) infinite;
}
@keyframes dot-pulse {
  0%, 100% { opacity: 0.6; transform: scale(1); }
  50% { opacity: 1; transform: scale(1.3); }
}
.brand-mini {
  font-size: 0.8rem; letter-spacing: 0.15em;
  color: var(--color-text-2); font-weight: 500;
}

.topbar-center { display: flex; align-items: center; gap: 8px; }
.topbar-pulse {
  width: 5px; height: 5px; border-radius: 50%;
  background: #7ec8a0; animation: dot-pulse 2s var(--ease-in-out) infinite;
}
.topbar-status { font-size: 0.72rem; color: var(--color-text-3); letter-spacing: 0.05em; }

.topbar-right { display: flex; align-items: center; gap: 8px; }
.topbar-btn {
  display: inline-flex; align-items: center; gap: 8px;
  padding: 6px 12px 6px 6px;
  border: 1px solid rgba(255,255,255,0.04);
  border-radius: var(--radius-full);
  background: rgba(255,255,255,0.02);
  color: var(--color-text-2);
  cursor: pointer;
  transition: all 0.25s var(--ease-out);
}
.topbar-btn:hover {
  border-color: rgba(255,255,255,0.10);
  background: rgba(255,255,255,0.04);
  color: var(--color-text-1);
}
.settings-btn svg { width: 18px; height: 18px; }
.settings-btn { padding: 8px; border-radius: 50%; }

.profile-avatar {
  width: 30px; height: 30px;
  display: grid; place-items: center;
  border-radius: 50%; overflow: hidden;
  color: #fff; font-size: 0.78rem; font-weight: 800;
}
.profile-avatar img { width: 100%; height: 100%; object-fit: cover; }
.profile-name {
  max-width: 100px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap;
  font-size: 0.82rem; font-weight: 600;
}

/* ═══════════════════════════════════
   Hero 品牌区
   ═══════════════════════════════════ */
.hero {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 60px 20px 40px;
  position: relative; z-index: 2;
}

.hero-glow {
  position: absolute;
  width: 400px; height: 200px;
  top: 20px;
  background: radial-gradient(ellipse, rgba(240,192,96,0.04), transparent 70%);
  filter: blur(40px);
  animation: hero-glow-breathe 5s var(--ease-in-out) infinite;
}
@keyframes hero-glow-breathe {
  0%, 100% { transform: scaleY(0.8); opacity: 0.5; }
  50% { transform: scaleY(1.2); opacity: 1; }
}

.hero-bridge {
  width: 260px; height: 40px;
  margin-bottom: 18px;
  position: relative; z-index: 1;
}

.hero-title {
  font-family: var(--font-display);
  font-size: 3.2rem;
  font-weight: 800;
  background: linear-gradient(135deg, var(--color-aurora-3), var(--color-glow) 50%, var(--color-aurora-1));
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  margin: 0 0 10px;
  letter-spacing: 3px;
  position: relative; z-index: 1;
}
.hero-sub {
  font-size: 0.92rem;
  color: var(--color-text-2);
  letter-spacing: 4px;
  margin: 0;
  position: relative; z-index: 1;
  font-weight: 400;
}

/* ═══════════════════════════════════
   智能体面板条
   ═══════════════════════════════════ */
.agents-strip {
  display: flex;
  justify-content: center;
  gap: 24px;
  padding: 20px 24px 60px;
  flex: 1;
  position: relative; z-index: 2;
  flex-wrap: wrap;
}

/* ═══════════════════════════════════
   底部
   ═══════════════════════════════════ */
.home-footer {
  padding: 20px 28px 28px;
  position: relative; z-index: 2;
}
.footer-line {
  width: 100%; height: 1px;
  background: linear-gradient(90deg, transparent, rgba(255,255,255,0.03), transparent);
  margin-bottom: 16px;
}
.footer-content {
  display: flex; align-items: center; justify-content: center; gap: 10px;
}
.footer-copy { font-size: 0.7rem; color: var(--color-text-3); letter-spacing: 0.06em; }
.footer-sep { color: var(--color-text-3); opacity: 0.3; }
.footer-tag { font-size: 0.7rem; color: var(--color-text-3); letter-spacing: 0.08em; }

/* ═══════════════════════════════════
   响应式
   ═══════════════════════════════════ */
@media (max-width: 860px) {
  .agents-strip { flex-direction: column; align-items: center; gap: 20px; padding: 10px 16px 40px; }
  .hero-title { font-size: 2.4rem; }
  .topbar { padding: 10px 16px; }
}

@media (max-width: 520px) {
  .hero { padding: 40px 16px 24px; }
  .hero-title { font-size: 1.8rem; letter-spacing: 2px; }
  .hero-sub { font-size: 0.78rem; letter-spacing: 2px; }
  .hero-bridge { width: 180px; height: 28px; }
  .brand-mini { display: none; }
  .topbar-pulse, .topbar-status { display: none; }
  .profile-name { max-width: 70px; font-size: 0.75rem; }
}
</style>

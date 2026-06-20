<template>
  <div class="home-container">
    <!-- 宇宙星云背景 -->
    <div class="cosmic-bg">
      <div class="cosmic-nebula cn1"></div>
      <div class="cosmic-nebula cn2"></div>
      <div class="cosmic-nebula cn3"></div>
    </div>

    <!-- 星座连线 -->
    <div class="constellation" aria-hidden="true">
      <i v-for="n in 35" :key="n" class="cstar" :style="starStyle(n)"></i>
      <svg class="clines" viewBox="0 0 1440 900" preserveAspectRatio="none">
        <line x1="15%" y1="20%" x2="30%" y2="38%" />
        <line x1="30%" y1="38%" x2="50%" y2="25%" />
        <line x1="50%" y1="25%" x2="70%" y2="35%" />
        <line x1="70%" y1="35%" x2="85%" y2="20%" />
        <line x1="20%" y1="65%" x2="40%" y2="75%" />
        <line x1="60%" y1="78%" x2="80%" y2="68%" />
      </svg>
    </div>

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
      <!-- Soul -->
      <article class="agent-panel soul" @click="navigateTo('/love-master')">
        <div class="panel-aura"></div>
        <div class="panel-icon">
          <svg viewBox="0 0 48 48" fill="none">
            <path d="M40 17.5A8.5 8.5 0 0 0 24 14a8.5 8.5 0 0 0-16 3.5C8 27 24 38 24 38s16-11 16-20.5Z"
                  fill="currentColor" opacity="0.9"/>
            <path d="M24 14a8.5 8.5 0 0 1 16 3.5c0 9.5-16 20.5-16 20.5" fill="currentColor" opacity="0.3"/>
          </svg>
        </div>
        <div class="panel-body">
          <div class="panel-name">知心 Soul</div>
          <div class="panel-tagline">温柔陪伴，共情倾听</div>
          <div class="panel-desc">情感咨询 · 关系建议 · 心灵树洞</div>
          <div class="panel-action">
            <span>进入心灵空间</span>
            <svg viewBox="0 0 16 16" fill="none" stroke="currentColor" stroke-width="1.5"><path d="M4 8h8M9 5l3 3-3 3"/></svg>
          </div>
        </div>
      </article>

      <!-- Spark -->
      <article class="agent-panel spark" @click="navigateTo('/chat')">
        <div class="panel-aura"></div>
        <div class="panel-icon">
          <svg viewBox="0 0 48 48" fill="none">
            <rect x="6" y="6" width="36" height="28" rx="6" stroke="currentColor" stroke-width="2"/>
            <path d="M14 18h20M14 26h12" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
            <path d="M32 34l-4 8h-8l-4-8" fill="currentColor" opacity="0.3"/>
            <circle cx="22" cy="18" r="2" fill="currentColor" opacity="0.5"/>
            <circle cx="30" cy="18" r="2" fill="currentColor" opacity="0.5"/>
          </svg>
        </div>
        <div class="panel-body">
          <div class="panel-name">灵语 Spark</div>
          <div class="panel-tagline">分支叙事，互动创作</div>
          <div class="panel-desc">互动剧本 · 分支结局 · AI 叙事</div>
          <div class="panel-action">
            <span>开启故事之旅</span>
            <svg viewBox="0 0 16 16" fill="none" stroke="currentColor" stroke-width="1.5"><path d="M4 8h8M9 5l3 3-3 3"/></svg>
          </div>
        </div>
      </article>

      <!-- Core -->
      <article class="agent-panel core" @click="navigateTo('/super-agent')">
        <div class="panel-aura"></div>
        <div class="panel-icon">
          <svg viewBox="0 0 48 48" fill="none">
            <rect x="8" y="8" width="32" height="26" rx="6" stroke="currentColor" stroke-width="2"/>
            <path d="M20 30h8" stroke="currentColor" stroke-width="2.5" stroke-linecap="round"/>
            <circle cx="16" cy="18" r="3" fill="currentColor" opacity="0.4"/>
            <circle cx="32" cy="18" r="3" fill="currentColor" opacity="0.4"/>
            <path d="M16 24c1.33.8 3.2 1.5 8 1.5s6.67-.7 8-1.5" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" opacity="0.6"/>
            <!-- 小星环 -->
            <circle cx="24" cy="34" r="2" stroke="currentColor" stroke-width="1" opacity="0.4"/>
          </svg>
        </div>
        <div class="panel-body">
          <div class="panel-name">极智 Core</div>
          <div class="panel-tagline">工具调用，全能解决</div>
          <div class="panel-desc">联网搜索 · 图片生成 · ReAct 推理</div>
          <div class="panel-action">
            <span>启动极智引擎</span>
            <svg viewBox="0 0 16 16" fill="none" stroke="currentColor" stroke-width="1.5"><path d="M4 8h8M9 5l3 3-3 3"/></svg>
          </div>
        </div>
      </article>
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

const starStyle = (n) => {
  const angle = (n / 35) * 360
  const radius = 15 + Math.random() * 50
  const x = 50 + Math.cos(angle * Math.PI / 180) * radius
  const y = 50 + Math.sin(angle * Math.PI / 180) * radius
  const size = 1 + Math.random() * 2
  const delay = Math.random() * 10
  const duration = 3 + Math.random() * 5
  return {
    left: `${x}%`,
    top: `${y}%`,
    width: `${size}px`,
    height: `${size}px`,
    animationDelay: `${delay}s`,
    animationDuration: `${duration}s`,
    opacity: 0.15 + Math.random() * 0.5
  }
}
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

/* ═══════ 星云背景 ═══════ */
.cosmic-bg {
  position: fixed; inset: 0; z-index: 0; pointer-events: none;
}
.cosmic-nebula {
  position: absolute; border-radius: 50%; filter: blur(160px);
  animation: nebula-morph 28s var(--ease-in-out) infinite;
}
.cosmic-nebula.cn1 {
  width: 650px; height: 650px;
  background: radial-gradient(circle, rgba(240,144,160,0.12), transparent 70%);
  top: -12%; left: -5%;
}
.cosmic-nebula.cn2 {
  width: 550px; height: 550px;
  background: radial-gradient(circle, rgba(126,200,224,0.08), transparent 70%);
  bottom: -10%; right: -5%;
  animation-delay: -9s;
}
.cosmic-nebula.cn3 {
  width: 400px; height: 400px;
  background: radial-gradient(circle, rgba(180,160,232,0.06), transparent 70%);
  top: 45%; left: 50%;
  animation-delay: -18s;
}
@keyframes nebula-morph {
  0%, 100% { transform: translate(0,0) scale(1); }
  33% { transform: translate(25px,-20px) scale(1.06); }
  66% { transform: translate(-20px,15px) scale(0.94); }
}

/* ═══════ 星座 ═══════ */
.constellation {
  position: fixed; inset: 0; z-index: 1; pointer-events: none;
}
.cstar {
  position: absolute; background: #fff; border-radius: 50%;
  animation: c-twinkle 4s var(--ease-in-out) infinite;
}
@keyframes c-twinkle {
  0%, 100% { opacity: 0.1; transform: scale(0.5); }
  50% { opacity: 0.8; transform: scale(1.3); }
}
.clines {
  position: absolute; inset: 0; width: 100%; height: 100%; pointer-events: none;
}
.clines line { stroke: rgba(255,255,255,0.03); stroke-width: 0.5; }

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

/* ── 单个面板 ── */
.agent-panel {
  width: 320px;
  min-height: 340px;
  border-radius: 20px;
  padding: 0;
  cursor: pointer;
  display: flex;
  flex-direction: column;
  position: relative;
  overflow: hidden;
  border: 1px solid rgba(255,255,255,0.04);
  background: rgba(12, 18, 30, 0.60);
  backdrop-filter: blur(12px);
  transition: transform 0.4s var(--ease-out),
              box-shadow 0.4s var(--ease-out),
              border-color 0.4s var(--ease-out);
}
.agent-panel:hover {
  transform: translateY(-8px);
}

/* 面板光晕 */
.panel-aura {
  position: absolute;
  inset: 0;
  opacity: 0;
  transition: opacity 0.5s var(--ease-out);
  pointer-events: none;
}
.agent-panel:hover .panel-aura { opacity: 1; }

/* 角色色 */
/* Soul */
.soul { border-color: rgba(240,144,160,0.10); }
.soul:hover { border-color: rgba(240,144,160,0.25); box-shadow: 0 16px 64px rgba(240,144,160,0.08); }
.soul .panel-aura { background: radial-gradient(ellipse at 50% 0%, rgba(240,144,160,0.08), transparent 60%); }
.soul .panel-icon svg { color: var(--color-aurora-3); }
.soul .panel-action { color: var(--color-aurora-3); }
.soul .panel-action:hover { background: rgba(240,144,160,0.08); }
.panel-name { background: var(--gradient-soul); -webkit-background-clip: text; -webkit-text-fill-color: transparent; background-clip: text; }

/* Spark */
.spark { border-color: rgba(180,160,232,0.10); }
.spark:hover { border-color: rgba(180,160,232,0.25); box-shadow: 0 16px 64px rgba(180,160,232,0.08); }
.spark .panel-aura { background: radial-gradient(ellipse at 50% 0%, rgba(180,160,232,0.08), transparent 60%); }
.spark .panel-icon svg { color: var(--color-aurora-2); }
.spark .panel-action { color: var(--color-aurora-2); }
.spark .panel-action:hover { background: rgba(180,160,232,0.08); }
.spark .panel-name { background: var(--gradient-spark); -webkit-background-clip: text; -webkit-text-fill-color: transparent; background-clip: text; }

/* Core */
.core { border-color: rgba(126,200,224,0.10); }
.core:hover { border-color: rgba(126,200,224,0.25); box-shadow: 0 16px 64px rgba(126,200,224,0.08); }
.core .panel-aura { background: radial-gradient(ellipse at 50% 0%, rgba(126,200,224,0.08), transparent 60%); }
.core .panel-icon svg { color: var(--color-aurora-1); }
.core .panel-action { color: var(--color-aurora-1); }
.core .panel-action:hover { background: rgba(126,200,224,0.08); }
.core .panel-name { background: var(--gradient-core); -webkit-background-clip: text; -webkit-text-fill-color: transparent; background-clip: text; }

/* ── 面板内容 ── */
.panel-icon {
  width: 100%;
  height: 120px;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  z-index: 1;
  padding-top: 16px;
}
.panel-icon svg {
  width: 64px; height: 64px;
  filter: drop-shadow(0 0 20px currentColor);
  transition: transform 0.4s var(--ease-out);
}
.agent-panel:hover .panel-icon svg {
  transform: scale(1.08);
}

.panel-body {
  padding: 0 24px 24px;
  position: relative; z-index: 1;
  display: flex;
  flex-direction: column;
  flex: 1;
}

.panel-name {
  font-family: var(--font-display);
  font-size: 1.4rem;
  font-weight: 800;
  margin-bottom: 6px;
  letter-spacing: 0.04em;
  display: inline-block;
}
.panel-tagline {
  font-size: 0.85rem;
  color: var(--color-text-1);
  margin-bottom: 10px;
  font-weight: 500;
  letter-spacing: 0.02em;
}
.panel-desc {
  font-size: 0.75rem;
  color: var(--color-text-2);
  letter-spacing: 0.04em;
  margin-bottom: 20px;
  flex: 1;
}

.panel-action {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: 0.82rem;
  font-weight: 600;
  padding: 10px 18px;
  border-radius: 999px;
  border: 1px solid rgba(255,255,255,0.06);
  align-self: flex-start;
  cursor: pointer;
  transition: all 0.3s var(--ease-out);
  letter-spacing: 0.03em;
}
.panel-action svg {
  width: 14px; height: 14px;
  transition: transform 0.3s var(--ease-out);
}
.agent-panel:hover .panel-action svg {
  transform: translateX(4px);
}
.agent-panel:hover .panel-action {
  border-color: rgba(255,255,255,0.12);
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
@media (max-width: 1080px) {
  .agent-panel { width: 280px; min-height: 300px; }
  .panel-icon { height: 100px; }
  .panel-icon svg { width: 52px; height: 52px; }
}

@media (max-width: 860px) {
  .agents-strip { flex-direction: column; align-items: center; gap: 20px; padding: 10px 16px 40px; }
  .agent-panel { width: 100%; max-width: 420px; min-height: auto; flex-direction: row; align-items: center; }
  .panel-icon { width: 100px; height: 100px; flex-shrink: 0; padding: 0; }
  .panel-icon svg { width: 44px; height: 44px; }
  .panel-body { padding: 16px 20px; }
  .panel-name { font-size: 1.15rem; }
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
  .agent-panel { flex-direction: column; padding: 0; }
  .panel-icon { width: 100%; height: 80px; }
  .panel-body { padding: 0 18px 18px; }
  .profile-name { max-width: 70px; font-size: 0.75rem; }
}
</style>

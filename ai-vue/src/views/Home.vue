<template>
  <div class="home-container">
    <button class="user-entry" type="button" @click="navigateTo('/profile')" aria-label="查看用户信息">
      <span class="user-avatar" :style="currentUser?.avatarUrl ? {} : { background: currentUser?.avatar.color }">
        <img v-if="currentUser?.avatarUrl" :src="currentUser.avatarUrl" :alt="`${currentUser?.displayName || '用户'}头像`" />
        <span v-else>{{ currentUser?.avatar.text }}</span>
      </span>
      <span class="user-name">{{ currentUser?.displayName }}</span>
    </button>

    <div class="header">
      <div class="title-row">
        <h1 class="main-title">LinkMind 灵桥</h1>
        <button class="settings-entry" type="button" @click="navigateTo('/background-settings')" title="自定义聊天背景" aria-label="背景设置">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round">
            <rect x="3" y="3" width="18" height="18" rx="2" ry="2" />
            <circle cx="8.5" cy="8.5" r="1.5" />
            <polyline points="21 15 16 10 5 21" />
          </svg>
          <span>背景</span>
        </button>
      </div>
      <p class="subtitle">心与智之间，搭一座桥</p>
      <div class="divider"></div>
    </div>

    <div class="apps-container">
      <!-- 知心 Soul -->
      <div class="app-card love-card" @click="navigateTo('/love-master')">
        <div class="card-bg-pattern love-bg"></div>
        <div class="app-icon love-icon-wrapper">
          <svg class="heart-svg" viewBox="0 0 24 24" fill="currentColor">
            <path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z"/>
          </svg>
        </div>
        <div class="app-info">
          <div class="app-title love-title">知心 Soul</div>
          <div class="app-desc">
            <p>懂你心事的情感伴侣</p>
            <p>温柔倾听，共情陪伴</p>
            <p>解答情感困惑与关系难题</p>
          </div>
          <div class="app-tags">
            <span class="tag love-tag">情感咨询</span>
            <span class="tag love-tag">关系建议</span>
            <span class="tag love-tag">共情倾听</span>
          </div>
        </div>
        <div class="app-button love-btn">
          <span class="btn-text">💗 和知心聊聊</span>
        </div>
      </div>

      <!-- 右侧双卡片 -->
      <div class="right-stack">
        <!-- 灵语 Spark -->
        <div class="app-card spark-card" @click="navigateTo('/chat')">
          <div class="card-bg-pattern spark-bg"></div>
          <div class="app-icon spark-icon-wrapper">
            <svg class="spark-svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
              <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z" stroke-linecap="round" stroke-linejoin="round" />
              <path d="M8 9h.01" stroke-linecap="round" stroke-width="2" />
              <path d="M12 9h.01" stroke-linecap="round" stroke-width="2" />
              <path d="M16 9h.01" stroke-linecap="round" stroke-width="2" />
            </svg>
          </div>
          <div class="app-info">
            <div class="app-title spark-title">灵语 Spark</div>
            <div class="app-desc">
              <p>互动剧本，分支叙事</p>
              <p>每个选择都通往不同结局</p>
            </div>
            <div class="app-tags">
              <span class="tag spark-tag">互动剧本</span>
              <span class="tag spark-tag">分支结局</span>
            </div>
          </div>
          <div class="app-button spark-btn">
            <span class="btn-text">📖 开启剧本</span>
          </div>
        </div>

        <!-- 极智 Core -->
        <div class="app-card agent-card" @click="navigateTo('/super-agent')">
          <div class="card-bg-pattern agent-bg"></div>
          <div class="app-icon agent-icon-wrapper">
            <svg class="agent-svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
              <rect x="3" y="3" width="18" height="14" rx="3" />
              <path d="M7 21h10" stroke-linecap="round" />
              <path d="M12 17v4" stroke-linecap="round" />
              <circle cx="8.5" cy="9" r="1.5" />
              <circle cx="15.5" cy="9" r="1.5" />
              <path d="M8.5 12c.83.67 2 1 3.5 1s2.67-.33 3.5-1" stroke-linecap="round" />
            </svg>
          </div>
          <div class="app-info">
            <div class="app-title agent-title">极智 Core</div>
            <div class="app-desc">
              <p>调用工具解决复杂任务</p>
              <p>联网搜索 + 图片生成</p>
              <p>ReAct 推理引擎驱动</p>
            </div>
            <div class="app-tags">
              <span class="tag agent-tag">工具调用</span>
              <span class="tag agent-tag">联网搜索</span>
              <span class="tag agent-tag">ReAct 推理</span>
            </div>
          </div>
          <div class="app-button agent-btn">
            <span class="btn-text">⚡ 启动极智</span>
          </div>
        </div>
      </div>
    </div>
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
      content: 'LinkMind 灵桥：知心 Soul 情感伴侣 + 极智 Core 全能助手。连接智慧，点亮灵感。'
    },
    {
      name: 'keywords',
      content: 'LinkMind,灵桥,知心Soul,极智Core,AI智能体,AI助手,情感咨询,智能对话'
    }
  ]
})

const router = useRouter()

const navigateTo = (path) => {
  router.push(path)
}
</script>

<style scoped>
.home-container {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
  background: linear-gradient(160deg, #0f0c29 0%, #1a1a3e 30%, #24243e 60%, #0f0c29 100%);
  position: relative;
  overflow: hidden;
  isolation: isolate;
}

/* 背景粒子 */
.home-container::before {
  content: '';
  position: absolute;
  inset: 0;
  background:
    radial-gradient(circle at 20% 30%, rgba(255, 107, 139, 0.08) 0%, transparent 50%),
    radial-gradient(circle at 80% 70%, rgba(63, 81, 181, 0.08) 0%, transparent 50%),
    radial-gradient(circle at 50% 50%, rgba(0, 240, 255, 0.03) 0%, transparent 60%);
  z-index: 0;
  pointer-events: none;
}

/* 用户区 */
.user-entry {
  position: absolute;
  top: 22px;
  right: 24px;
  z-index: 5;
  display: inline-flex;
  align-items: center;
  gap: 10px;
  min-height: 46px;
  padding: 6px 14px 6px 6px;
  border: 1px solid rgba(255, 255, 255, 0.15);
  border-radius: 999px;
  color: #edf7ff;
  background: rgba(255, 255, 255, 0.06);
  backdrop-filter: blur(14px);
  cursor: pointer;
  transition: transform 0.2s, border-color 0.2s, background 0.2s;
}

.user-entry:hover {
  transform: translateY(-2px);
  border-color: rgba(255, 255, 255, 0.35);
  background: rgba(255, 255, 255, 0.1);
}

.user-avatar {
  width: 34px;
  height: 34px;
  display: grid;
  place-items: center;
  border-radius: 50%;
  color: #fff;
  font-size: 0.86rem;
  font-weight: 900;
  overflow: hidden;
}

.user-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.user-name {
  max-width: 120px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-size: 0.95rem;
  font-weight: 700;
}

/* 标题区 */
.header {
  padding: 90px 20px 40px;
  text-align: center;
  position: relative;
  z-index: 1;
}

.title-row {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 14px;
  margin-bottom: 12px;
}

.main-title {
  font-size: 2.6rem;
  font-weight: 800;
  color: #ffffff;
  letter-spacing: 3px;
  background: linear-gradient(135deg, #ff6b8b, #7c3aed, #00b2ff);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  margin: 0;
}

.settings-entry {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 6px 14px;
  min-height: 38px;
  border: 1px solid rgba(255, 255, 255, 0.18);
  border-radius: 20px;
  background: rgba(255, 255, 255, 0.06);
  color: rgba(255, 255, 255, 0.65);
  cursor: pointer;
  font-size: 0.85rem;
  font-weight: 600;
  transition: all 0.25s ease;
  flex-shrink: 0;
  backdrop-filter: blur(8px);
}

.settings-entry svg {
  width: 18px;
  height: 18px;
}

.settings-entry:hover {
  color: #ffffff;
  border-color: rgba(255, 255, 255, 0.45);
  background: rgba(255, 255, 255, 0.14);
  transform: translateY(-1px);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.2);
}

.subtitle {
  font-size: 1.1rem;
  color: rgba(255, 255, 255, 0.55);
  letter-spacing: 2px;
  margin-bottom: 24px;
}

.divider {
  width: 120px;
  height: 3px;
  margin: 0 auto;
  border-radius: 2px;
  background: linear-gradient(90deg, #ff6b8b, #7c3aed);
}

/* 卡片容器 */
.apps-container {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  align-items: stretch;
  gap: 40px;
  max-width: 1100px;
  margin: 40px auto 80px;
  padding: 0 20px;
  flex: 1;
  position: relative;
  z-index: 1;
}

.right-stack {
  display: flex;
  flex-direction: column;
  gap: 20px;
  width: 340px;
}

.right-stack .app-card {
  width: 100%;
  padding: 22px 18px 18px;
}

.right-stack .app-icon {
  width: 56px;
  height: 56px;
  margin-bottom: 14px;
}

.right-stack .app-icon svg {
  width: 28px;
  height: 28px;
}

.right-stack .app-title {
  font-size: 1.2rem;
  margin-bottom: 10px;
}

.right-stack .app-desc p {
  font-size: 0.82rem;
  line-height: 1.5;
}

.right-stack .app-tags {
  margin-bottom: 14px;
}

.right-stack .app-button {
  padding: 10px 24px;
}

.app-card {
  width: 340px;
  border-radius: 24px;
  padding: 36px 28px 28px;
  cursor: pointer;
  display: flex;
  flex-direction: column;
  align-items: center;
  position: relative;
  overflow: hidden;
  transition: transform 0.35s ease, box-shadow 0.35s ease;
  backdrop-filter: blur(10px);
}

.app-card:hover {
  transform: translateY(-6px);
}

.card-bg-pattern {
  position: absolute;
  inset: 0;
  z-index: 0;
  pointer-events: none;
}

/* ====== 恋爱大师卡片 ====== */
.love-card {
  background: linear-gradient(145deg, rgba(255, 107, 139, 0.12), rgba(255, 138, 128, 0.06));
  border: 1px solid rgba(255, 107, 139, 0.2);
  box-shadow: 0 4px 30px rgba(255, 107, 139, 0.1);
}

.love-card:hover {
  box-shadow: 0 16px 48px rgba(255, 107, 139, 0.22);
  border-color: rgba(255, 107, 139, 0.45);
}

.love-bg {
  background:
    radial-gradient(circle at 70% 10%, rgba(255, 107, 139, 0.12) 0%, transparent 50%),
    radial-gradient(circle at 30% 90%, rgba(255, 160, 140, 0.06) 0%, transparent 40%);
}

.love-icon-wrapper {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  background: linear-gradient(135deg, #ff6b8b, #ff8a80);
  box-shadow: 0 8px 32px rgba(255, 107, 139, 0.35);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1;
  margin-bottom: 24px;
}

.heart-svg {
  width: 42px;
  height: 42px;
  color: #ffffff;
  filter: drop-shadow(0 2px 4px rgba(0,0,0,0.2));
  animation: heartbeat 1.5s ease-in-out infinite;
}

.love-title {
  background: linear-gradient(135deg, #ff6b8b, #ffab91);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.love-btn {
  background: linear-gradient(135deg, #ff6b8b, #ff5252);
  box-shadow: 0 4px 16px rgba(255, 107, 139, 0.3);
}

.love-btn:hover {
  box-shadow: 0 8px 28px rgba(255, 107, 139, 0.5);
  background: linear-gradient(135deg, #ff7b9b, #ff6b6b);
}

.love-tag {
  background: rgba(255, 107, 139, 0.15);
  color: #ff6b8b;
  border: 1px solid rgba(255, 107, 139, 0.2);
}

/* ====== 超级智能体卡片 ====== */
.agent-card {
  background: linear-gradient(145deg, rgba(63, 81, 181, 0.12), rgba(0, 178, 255, 0.06));
  border: 1px solid rgba(63, 81, 181, 0.2);
  box-shadow: 0 4px 30px rgba(63, 81, 181, 0.1);
}

.agent-card:hover {
  box-shadow: 0 16px 48px rgba(63, 81, 181, 0.22);
  border-color: rgba(63, 81, 181, 0.45);
}

.agent-bg {
  background:
    radial-gradient(circle at 30% 10%, rgba(63, 81, 181, 0.12) 0%, transparent 50%),
    radial-gradient(circle at 70% 90%, rgba(0, 178, 255, 0.06) 0%, transparent 40%);
}

.agent-icon-wrapper {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  background: linear-gradient(135deg, #3f51b5, #00b2ff);
  box-shadow: 0 8px 32px rgba(63, 81, 181, 0.35);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1;
  margin-bottom: 24px;
}

.agent-svg {
  width: 42px;
  height: 42px;
  color: #ffffff;
  filter: drop-shadow(0 2px 4px rgba(0,0,0,0.2));
}

.agent-title {
  background: linear-gradient(135deg, #5c6bc0, #00b2ff);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.agent-btn {
  background: linear-gradient(135deg, #3f51b5, #0288d1);
  box-shadow: 0 4px 16px rgba(63, 81, 181, 0.3);
}

.agent-btn:hover {
  box-shadow: 0 8px 28px rgba(63, 81, 181, 0.5);
  background: linear-gradient(135deg, #536dfe, #039be5);
}

.agent-tag {
  background: rgba(63, 81, 181, 0.15);
  color: #7986cb;
  border: 1px solid rgba(63, 81, 181, 0.2);
}

/* ====== 灵语 Spark 卡片 ====== */
.spark-card {
  background: linear-gradient(145deg, rgba(124, 58, 237, 0.12), rgba(167, 139, 250, 0.06));
  border: 1px solid rgba(124, 58, 237, 0.2);
  box-shadow: 0 4px 30px rgba(124, 58, 237, 0.1);
}

.spark-card:hover {
  box-shadow: 0 12px 36px rgba(124, 58, 237, 0.2);
  border-color: rgba(124, 58, 237, 0.4);
}

.spark-bg {
  background:
    radial-gradient(circle at 50% 10%, rgba(124, 58, 237, 0.1) 0%, transparent 50%),
    radial-gradient(circle at 50% 90%, rgba(167, 139, 250, 0.05) 0%, transparent 40%);
}

.spark-icon-wrapper {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  background: linear-gradient(135deg, #7c3aed, #a78bfa);
  box-shadow: 0 8px 32px rgba(124, 58, 237, 0.35);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1;
  margin-bottom: 24px;
}

.spark-svg {
  width: 42px;
  height: 42px;
  color: #ffffff;
  filter: drop-shadow(0 2px 4px rgba(0,0,0,0.2));
}

.spark-title {
  background: linear-gradient(135deg, #7c3aed, #c4b5fd);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.spark-btn {
  background: linear-gradient(135deg, #7c3aed, #6d28d9);
  box-shadow: 0 4px 16px rgba(124, 58, 237, 0.3);
}

.spark-btn:hover {
  box-shadow: 0 8px 28px rgba(124, 58, 237, 0.5);
  background: linear-gradient(135deg, #8b5cf6, #7c3aed);
}

.spark-tag {
  background: rgba(124, 58, 237, 0.15);
  color: #a78bfa;
  border: 1px solid rgba(124, 58, 237, 0.2);
}

/* ====== 共用组件样式 ====== */
.app-icon {
  position: relative;
  z-index: 1;
}

.app-info {
  text-align: center;
  width: 100%;
  position: relative;
  z-index: 1;
}

.app-title {
  font-size: 1.5rem;
  font-weight: 800;
  margin-bottom: 16px;
  letter-spacing: 1px;
}

.app-desc {
  margin-bottom: 18px;
}

.app-desc p {
  font-size: 0.92rem;
  color: rgba(255, 255, 255, 0.6);
  line-height: 1.8;
  margin: 0;
}

.app-tags {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: 8px;
  margin-bottom: 24px;
}

.tag {
  font-size: 0.75rem;
  padding: 3px 10px;
  border-radius: 999px;
  font-weight: 600;
}

.app-button {
  color: white;
  padding: 14px 32px;
  border-radius: 30px;
  font-weight: 600;
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
  cursor: pointer;
  z-index: 1;
  width: 100%;
  text-align: center;
  box-sizing: border-box;
}

.app-button:hover {
  transform: scale(1.04);
}

.btn-text {
  font-size: 0.95rem;
  letter-spacing: 1px;
}

/* 动画 */
@keyframes heartbeat {
  0%, 100% { transform: scale(1); }
  14% { transform: scale(1.1); }
  28% { transform: scale(1); }
  42% { transform: scale(1.1); }
  56% { transform: scale(1); }
}

/* 响应式 */
@media (max-width: 768px) {
  .main-title {
    font-size: 2rem;
    letter-spacing: 2px;
  }

  .subtitle {
    font-size: 0.95rem;
  }

  .apps-container {
    gap: 28px;
    margin: 30px auto 60px;
  }

  .app-card {
    width: 100%;
    max-width: 420px;
    padding: 28px 22px 22px;
  }

  .app-title {
    font-size: 1.3rem;
  }
}

@media (max-width: 480px) {
  .user-entry {
    top: 14px;
    right: 14px;
    padding-right: 10px;
  }

  .user-name {
    max-width: 86px;
  }

  .header {
    padding: 70px 15px 30px;
  }

  .main-title {
    font-size: 1.7rem;
  }

  .subtitle {
    font-size: 0.85rem;
  }

  .apps-container {
    margin: 20px auto 50px;
    padding: 0 15px;
  }

  .app-card {
    padding: 24px 18px 18px;
  }

  .app-icon {
    width: 64px;
    height: 64px;
    margin-bottom: 18px;
  }

  .heart-svg, .agent-svg {
    width: 32px;
    height: 32px;
  }
}
</style>

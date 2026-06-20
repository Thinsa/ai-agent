<template>
  <aside class="history-sidebar" :class="[module]">
    <div class="history-head">
      <div>
        <div class="history-title">历史记录</div>
        <div class="history-subtitle">点击恢复会话</div>
      </div>
      <button class="new-button" type="button" @click="$emit('new-session')">新建</button>
    </div>

    <div v-if="loading" class="history-state">加载中...</div>
    <div v-else-if="sessions.length === 0" class="history-state">暂无历史</div>
    <button
      v-for="session in sessions"
      v-else
      :key="session.chatId"
      class="history-item"
      :class="{ active: session.chatId === activeChatId }"
      type="button"
      @click="$emit('select-session', session.chatId)"
    >
      <span class="item-title">{{ session.title || session.chatId }}</span>
      <span class="item-time">{{ formatTime(session.updatedAt) }}</span>
    </button>
  </aside>
</template>

<script setup>
defineProps({
  sessions: {
    type: Array,
    default: () => []
  },
  activeChatId: {
    type: String,
    default: ''
  },
  module: {
    type: String,
    default: 'love'
  },
  loading: {
    type: Boolean,
    default: false
  }
})

defineEmits(['select-session', 'new-session'])

const formatTime = (value) => {
  if (!value) return ''
  const date = new Date(value)
  return date.toLocaleString('zh-CN', {
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}
</script>

<style scoped>
.history-sidebar {
  width: 260px; min-width: 260px; height: 100%; min-height: 0;
  overflow-y: auto; display: flex; flex-direction: column;
  border-radius: var(--radius-lg);
  background: var(--glass-card);
  backdrop-filter: blur(var(--blur-card));
  border: var(--border-subtle);
  box-shadow: var(--shadow-card);
}

.history-head {
  display: flex; align-items: center; justify-content: space-between; gap: 12px;
  padding: 14px; color: #fff;
  border-radius: var(--radius-lg) var(--radius-lg) 0 0;
}
.history-sidebar.love .history-head { background: var(--gradient-soul); }
.history-sidebar.super .history-head { background: var(--gradient-core); }
/* spark/default fallback */
.history-sidebar:not(.love):not(.super) .history-head { background: var(--gradient-spark); }

.history-title { font-weight: 800; font-size: 16px; }
.history-subtitle { margin-top: 2px; font-size: 12px; opacity: 0.82; }

.new-button {
  border: 0; border-radius: var(--radius-sm); padding: 8px 10px;
  color: var(--color-base-0); background: rgba(255,255,255,0.9);
  cursor: pointer; font-weight: 700;
  transition: transform var(--duration-fast) var(--ease-out);
}
.new-button:hover { transform: scale(1.04); }

.history-state { padding: 18px 14px; color: var(--color-text-2); font-size: 14px; }

.history-item {
  display: flex; flex-direction: column; gap: 5px;
  width: 100%; padding: 12px 14px; border: 0;
  border-bottom: var(--border-subtle);
  background: transparent; text-align: left; cursor: pointer;
  transition: background var(--duration-fast) var(--ease-out);
}
.history-item:hover { background: rgba(255,255,255,0.04); }
.history-item.active { background: rgba(255,255,255,0.06); }

.item-title {
  color: var(--color-text-1); font-size: 14px; font-weight: 700;
  overflow: hidden; text-overflow: ellipsis; white-space: nowrap;
}
.item-time { color: var(--color-text-2); font-size: 12px; }

@media (max-width: 900px) {
  .history-sidebar { width: 100%; min-width: 0; height: auto; min-height: 0; max-height: 220px; }
}
</style>

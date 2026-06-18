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
  width: 260px;
  min-width: 260px;
  height: 100%;
  min-height: 0;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  border-radius: 8px;
  background: #ffffff;
  border: 1px solid rgba(0, 0, 0, 0.08);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.08);
}

.history-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 14px;
  color: #fff;
}

.history-sidebar.love .history-head {
  background: #ff6b8b;
}

.history-sidebar.super .history-head {
  background: #3f51b5;
}

.history-title {
  font-weight: 800;
  font-size: 16px;
}

.history-subtitle {
  margin-top: 2px;
  font-size: 12px;
  opacity: 0.82;
}

.new-button {
  border: 0;
  border-radius: 6px;
  padding: 8px 10px;
  color: #1f2937;
  background: rgba(255, 255, 255, 0.9);
  cursor: pointer;
  font-weight: 700;
}

.history-state {
  padding: 18px 14px;
  color: #667085;
  font-size: 14px;
}

.history-item {
  display: flex;
  flex-direction: column;
  gap: 5px;
  width: 100%;
  padding: 12px 14px;
  border: 0;
  border-bottom: 1px solid #eef0f3;
  background: #fff;
  text-align: left;
  cursor: pointer;
}

.history-item:hover,
.history-item.active {
  background: #f5f7fb;
}

.history-sidebar.love .history-item.active {
  background: #fff0f4;
}

.history-sidebar.super .history-item.active {
  background: #eef2ff;
}

.item-title {
  color: #1f2937;
  font-size: 14px;
  font-weight: 700;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.item-time {
  color: #7b8494;
  font-size: 12px;
}

@media (max-width: 900px) {
  .history-sidebar {
    width: 100%;
    min-width: 0;
    height: auto;
    min-height: 0;
    max-height: 220px;
  }
}
</style>

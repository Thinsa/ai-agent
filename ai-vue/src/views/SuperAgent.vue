<template>
  <div class="super-agent-container">
    <div class="header">
      <button class="back-button" type="button" @click="goBack">返回</button>
      <h1 class="title">AI超级智能体</h1>
      <div class="placeholder"></div>
    </div>

    <div class="content-wrapper" :class="{ 'settings-open': settingsOpen }">
      <button
        class="settings-toggle-button"
        type="button"
        :title="settingsOpen ? '关闭设置' : '打开设置'"
        :aria-label="settingsOpen ? '关闭设置' : '打开设置'"
        @click="settingsOpen = !settingsOpen"
      >
        <svg viewBox="0 0 24 24" aria-hidden="true">
          <path d="M12 15.5A3.5 3.5 0 1 0 12 8a3.5 3.5 0 0 0 0 7.5Z" />
          <path d="M19.4 15a1.8 1.8 0 0 0 .36 1.98l.05.05a2.15 2.15 0 0 1-3.04 3.04l-.05-.05A1.8 1.8 0 0 0 14.74 19a1.8 1.8 0 0 0-1.1 1.65V21a2.15 2.15 0 0 1-4.3 0v-.08A1.8 1.8 0 0 0 8.24 19a1.8 1.8 0 0 0-1.98.36l-.05.05a2.15 2.15 0 0 1-3.04-3.04l.05-.05A1.8 1.8 0 0 0 5 14.74a1.8 1.8 0 0 0-1.65-1.1H3a2.15 2.15 0 0 1 0-4.3h.08A1.8 1.8 0 0 0 5 8.24a1.8 1.8 0 0 0-.36-1.98l-.05-.05a2.15 2.15 0 0 1 3.04-3.04l.05.05A1.8 1.8 0 0 0 9.26 5a1.8 1.8 0 0 0 1.1-1.65V3a2.15 2.15 0 0 1 4.3 0v.08A1.8 1.8 0 0 0 15.76 5a1.8 1.8 0 0 0 1.98-.36l.05-.05a2.15 2.15 0 0 1 3.04 3.04l-.05.05A1.8 1.8 0 0 0 19 9.26a1.8 1.8 0 0 0 1.65 1.1H21a2.15 2.15 0 0 1 0 4.3h-.08A1.8 1.8 0 0 0 19.4 15Z" />
        </svg>
      </button>

      <AgentSettingsPanel
        v-if="settingsOpen"
        theme="super"
        :web-search-enabled="webSearchEnabled"
        :mcp-enabled="mcpEnabled"
        :custom-prompt="customPrompt"
        prompt-placeholder="你可以在这里添加超级智能体本轮对话的额外目标。"
        @toggle-open="settingsOpen = false"
        @update:web-search-enabled="webSearchEnabled = $event"
        @update:mcp-enabled="mcpEnabled = $event"
        @update:custom-prompt="customPrompt = $event"
      />

      <ChatHistorySidebar
        v-else
        :sessions="historySessions"
        :active-chat-id="chatId"
        :loading="historyLoading"
        module="super"
        @select-session="loadHistory"
        @new-session="startNewSession"
      />

      <div class="chat-area">
        <ChatRoom
          :messages="messages"
          :connection-status="connectionStatus"
          ai-type="super"
          @send-message="sendMessage"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { onBeforeUnmount, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useHead } from '@vueuse/head'
import AgentSettingsPanel from '../components/AgentSettingsPanel.vue'
import ChatHistorySidebar from '../components/ChatHistorySidebar.vue'
import ChatRoom from '../components/ChatRoom.vue'
import { chatWithManus, chatWithManusMcp, getChatHistory, listChatSessions } from '../api'

useHead({
  title: 'AI超级智能体 - AI超级智能体应用平台',
  meta: [
    {
      name: 'description',
      content: 'AI超级智能体是AI超级智能体应用平台的全能助手'
    },
    {
      name: 'keywords',
      content: 'AI超级智能体,智能助手,专业问答,AI问答'
    }
  ]
})

const router = useRouter()
const messages = ref([])
const chatId = ref('')
const settingsOpen = ref(false)
const webSearchEnabled = ref(false)
const mcpEnabled = ref(false)
const customPrompt = ref('')
const connectionStatus = ref('disconnected')
const historySessions = ref([])
const historyLoading = ref(false)
let eventSource = null

const generateChatId = () => `super_${Math.random().toString(36).slice(2, 10)}`

const activeModeLabel = isUser => {
  if (mcpEnabled.value) {
    return isUser ? 'MCP 请求' : 'MCP 回复'
  }
  return webSearchEnabled.value ? '联网搜索' : ''
}

const addMessage = (content, isUser, extra = {}) => {
  messages.value.push({
    content,
    isUser,
    type: isUser ? 'user-question' : 'ai-answer',
    time: Date.now(),
    ...extra
  })
}

const toPageMessage = message => ({
  content: message.content,
  isUser: message.role === 'user',
  type: message.role === 'user' ? 'user-question' : 'ai-answer',
  time: new Date(message.createdAt).getTime()
})

const closeStream = () => {
  if (eventSource) {
    eventSource.close()
    eventSource = null
  }
}

const loadSessions = async () => {
  historyLoading.value = true
  try {
    historySessions.value = await listChatSessions('super')
  } catch (error) {
    console.error('Load history sessions error:', error)
  } finally {
    historyLoading.value = false
  }
}

const loadHistory = async targetChatId => {
  if (!targetChatId || targetChatId === chatId.value) {
    return
  }
  closeStream()
  connectionStatus.value = 'disconnected'
  const detail = await getChatHistory(targetChatId)
  chatId.value = detail.chatId
  messages.value = detail.messages.map(toPageMessage)
}

const startNewSession = () => {
  closeStream()
  connectionStatus.value = 'disconnected'
  chatId.value = generateChatId()
  messages.value = []
  addMessage('你好，我是AI超级智能体。我可以解答各类问题，提供专业建议，也可以在开启能力后使用联网搜索和 MCP 服务。', false)
}

const buildPrompt = message => {
  const parts = [
    webSearchEnabled.value
      ? '允许在需要时使用联网搜索工具回答。'
      : '不要使用联网搜索工具，请基于已有知识和本地工具回答。'
  ]
  if (customPrompt.value.trim()) {
    parts.push(`额外提示词：${customPrompt.value.trim()}`)
  }
  parts.push(`用户问题：${message}`)
  return parts.join('\n')
}

const runStreamChat = (message, useMcp) => {
  const aiMessageIndex = messages.value.length
  addMessage('', false, { modeLabel: activeModeLabel(false) })
  connectionStatus.value = 'connecting'

  const onMessage = data => {
    if (data && data !== '[DONE]' && aiMessageIndex < messages.value.length) {
      messages.value[aiMessageIndex].content += data
    }

    if (data === '[DONE]') {
      connectionStatus.value = 'disconnected'
      closeStream()
      loadSessions()
    }
  }

  const onError = error => {
    console.error('SSE Error:', error)
    connectionStatus.value = 'error'
    if (aiMessageIndex < messages.value.length && !messages.value[aiMessageIndex].content) {
      messages.value[aiMessageIndex].content = useMcp
        ? 'MCP 请求失败，请确认 MCP Server 已启动。'
        : '请求失败，请稍后重试。'
      messages.value[aiMessageIndex].type = 'ai-error'
    }
    closeStream()
  }

  eventSource = useMcp
    ? chatWithManusMcp(message, chatId.value, onMessage, onError)
    : chatWithManus(message, chatId.value)

  if (!useMcp) {
    eventSource.onmessage = event => onMessage(event.data)
    eventSource.onerror = onError
  }
}

const sendMessage = message => {
  const useMcp = mcpEnabled.value
  addMessage(message, true, { modeLabel: activeModeLabel(true) })
  closeStream()
  runStreamChat(buildPrompt(message), useMcp)
}

const goBack = () => {
  router.push('/')
}

onMounted(() => {
  startNewSession()
  loadSessions()
})

onBeforeUnmount(() => {
  closeStream()
})
</script>

<style scoped>
.super-agent-container {
  display: flex;
  flex-direction: column;
  height: 100vh;
  min-height: 0;
  overflow: hidden;
  background-color: #f5f6fb;
}

.header {
  display: grid;
  flex: 0 0 auto;
  grid-template-columns: 1fr auto 1fr;
  align-items: center;
  padding: 16px 24px;
  background-color: #3f51b5;
  color: #ffffff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  z-index: 10;
}

.back-button {
  display: inline-flex;
  align-items: center;
  justify-self: start;
  border: 0;
  background: transparent;
  color: inherit;
  cursor: pointer;
  font-size: 16px;
  transition: opacity 0.2s;
}

.back-button:hover {
  opacity: 0.8;
}

.back-button::before {
  content: '<';
  margin-right: 8px;
}

.title {
  justify-self: center;
  margin: 0;
  text-align: center;
  font-size: 20px;
  font-weight: bold;
}

.placeholder {
  width: 1px;
  justify-self: end;
}

.content-wrapper {
  position: relative;
  display: flex;
  flex: 1;
  min-height: 0;
  flex-direction: row;
  gap: 16px;
  padding: 16px;
  overflow: hidden;
}

.chat-area {
  position: relative;
  display: flex;
  flex: 1;
  min-width: 0;
  min-height: 0;
  height: 100%;
  overflow: hidden;
}

.settings-open .chat-area {
  flex: 0 0 calc(50% - 8px);
}

.settings-toggle-button {
  position: absolute;
  top: 24px;
  right: 24px;
  z-index: 20;
  width: 44px;
  height: 44px;
  border: 1px solid rgba(15, 23, 42, 0.12);
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.96);
  color: #0f172a;
  cursor: pointer;
  font-size: 20px;
  line-height: 1;
  box-shadow: 0 6px 18px rgba(15, 23, 42, 0.12);
}

.settings-toggle-button svg {
  width: 21px;
  height: 21px;
  fill: none;
  stroke: currentColor;
  stroke-linecap: round;
  stroke-linejoin: round;
  stroke-width: 1.8;
}

@media (max-width: 768px) {
  .header {
    padding: 12px 16px;
  }

  .title {
    font-size: 18px;
  }

  .content-wrapper {
    flex-direction: column;
    gap: 12px;
    padding: 12px;
  }

  .settings-open .chat-area {
    flex: 1;
  }

  .settings-toggle-button {
    top: 18px;
    right: 18px;
  }
}

@media (max-width: 480px) {
  .header {
    padding: 10px 12px;
  }

  .back-button {
    font-size: 14px;
  }

  .title {
    font-size: 16px;
  }

  .content-wrapper {
    padding: 8px;
  }
}
</style>

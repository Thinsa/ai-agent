<template>
  <div class="super-agent-container">
    <div class="header">
      <button class="back-button" type="button" @click="goBack">返回</button>
      <h1 class="title">极智 Core</h1>
      <div class="header-actions">
        <button
          class="settings-toggle-button"
          type="button"
          :class="{ active: settingsOpen }"
          :title="settingsOpen ? '关闭设置' : '打开设置'"
          :aria-label="settingsOpen ? '关闭设置' : '打开设置'"
          @click="settingsOpen = !settingsOpen"
        >
          <svg viewBox="0 0 24 24" aria-hidden="true">
            <path d="M12 15.5A3.5 3.5 0 1 0 12 8a3.5 3.5 0 0 0 0 7.5Z" />
            <path d="M19.4 15a1.8 1.8 0 0 0 .36 1.98l.05.05a2.15 2.15 0 0 1-3.04 3.04l-.05-.05A1.8 1.8 0 0 0 14.74 19a1.8 1.8 0 0 0-1.1 1.65V21a2.15 2.15 0 0 1-4.3 0v-.08A1.8 1.8 0 0 0 8.24 19a1.8 1.8 0 0 0-1.98.36l-.05.05a2.15 2.15 0 0 1-3.04-3.04l.05-.05A1.8 1.8 0 0 0 5 14.74a1.8 1.8 0 0 0-1.65-1.1H3a2.15 2.15 0 0 1 0-4.3h.08A1.8 1.8 0 0 0 5 8.24a1.8 1.8 0 0 0-.36-1.98l-.05-.05a2.15 2.15 0 0 1 3.04-3.04l.05.05A1.8 1.8 0 0 0 9.26 5a1.8 1.8 0 0 0 1.1-1.65V3a2.15 2.15 0 0 1 4.3 0v.08A1.8 1.8 0 0 0 15.76 5a1.8 1.8 0 0 0 1.98-.36l.05-.05a2.15 2.15 0 0 1 3.04 3.04l-.05.05A1.8 1.8 0 0 0 19 9.26a1.8 1.8 0 0 0 1.65 1.1H21a2.15 2.15 0 0 1 0 4.3h-.08A1.8 1.8 0 0 0 19.4 15Z" />
          </svg>
          <span>设置</span>
        </button>
      </div>
    </div>

    <div class="content-wrapper" :class="{ 'settings-open': settingsOpen }">
      <AgentSettingsPanel
        v-if="settingsOpen"
        theme="super"
        :web-search-enabled="webSearchEnabled"
        :mcp-enabled="mcpEnabled"
        :vision-enabled="visionEnabled"
        :custom-prompt="customPrompt"
        prompt-placeholder="你可以在这里添加超级智能体本轮对话的额外目标。"
        @toggle-open="settingsOpen = false"
        @update:web-search-enabled="webSearchEnabled = $event"
        @update:mcp-enabled="mcpEnabled = $event"
        @update:vision-enabled="visionEnabled = $event"
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
          :stream-paused="streamPaused"
          ai-type="super"
          @send-message="sendMessage"
          @stop-generation="stopGeneration"
          @resume-generation="resumeGeneration"
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
  title: '极智 Core - LinkMind 灵桥',
  meta: [
    {
      name: 'description',
      content: '极智 Core 是 LinkMind 灵桥的全能助手，调用工具、联网搜索、ReAct 推理，解决各类专业问题。'
    },
    {
      name: 'keywords',
      content: '极智Core,LinkMind,灵桥,AI智能体,工具调用,联网搜索,ReAct推理'
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
const visionEnabled = ref(false)
const connectionStatus = ref('disconnected')
const streamPaused = ref(false)
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

const toPageMessage = message => {
  let content = message.content
  if (message.role === 'assistant') {
    const imageMatch = content?.match(/(?:图片生成成功|图片生成失败)[：:]\s*(.+)/)
    if (imageMatch) {
      content = imageMatch[1]
    }
  }
  return {
    content,
    isUser: message.role === 'user',
    type: message.role === 'user' ? 'user-question' : 'ai-answer',
    time: new Date(message.createdAt).getTime()
  }
}

const closeStream = () => {
  if (eventSource) {
    eventSource.close()
    eventSource = null
  }
}

const stopGeneration = () => {
  closeStream()
  connectionStatus.value = 'disconnected'
  streamPaused.value = true
}

const resumeGeneration = () => {
  streamPaused.value = false
  const useMcp = mcpEnabled.value
  closeStream()
  runStreamChat('请继续完成上面的回答', useMcp)
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
  const detail = await getChatHistory(targetChatId, 'super')
  chatId.value = detail.chatId
  messages.value = detail.messages.map(toPageMessage)
}

const startNewSession = () => {
  closeStream()
  connectionStatus.value = 'disconnected'
  streamPaused.value = false
  chatId.value = generateChatId()
  messages.value = []
  addMessage('你好，我是极智 Core，LinkMind 灵桥的全能助手。我可以调用工具、联网搜索、生成图片，解决各类复杂问题。', false)
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
      const msg = messages.value[aiMessageIndex]
      if (msg) {
        const imageMatch = msg.content?.match(/(?:图片生成成功|图片生成失败)[：:]\s*(.+)/)
        if (imageMatch) {
          msg.content = imageMatch[1]
        }
      }
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

const sendMessage = payload => {
  const text = typeof payload === 'string' ? payload : payload.text
  const useMcp = mcpEnabled.value
  streamPaused.value = false
  addMessage(text, true, { modeLabel: activeModeLabel(true) })
  closeStream()
  runStreamChat(buildPrompt(text), useMcp)
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
  padding: 12px 18px;
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

.header-actions {
  display: flex;
  align-items: center;
  justify-self: end;
  gap: 12px;
  min-width: 0;
}

.content-wrapper {
  position: relative;
  display: flex;
  flex: 1;
  min-height: 0;
  flex-direction: row;
  gap: 0;
  padding: 0;
  overflow: hidden;
}

.chat-area {
  position: relative;
  display: flex;
  flex: 1;
  min-width: 0;
  min-height: 0;
  overflow: hidden;
}

.settings-open .chat-area {
  flex: 0 0 50%;
}

.settings-toggle-button {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  min-width: 88px;
  min-height: 40px;
  border: 1px solid rgba(255, 255, 255, 0.38);
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.16);
  color: #ffffff;
  cursor: pointer;
  font-size: 14px;
  font-weight: 700;
  line-height: 1;
  transition: background-color 0.2s ease, border-color 0.2s ease;
}

.settings-toggle-button:hover,
.settings-toggle-button.active {
  border-color: rgba(255, 255, 255, 0.72);
  background: rgba(255, 255, 255, 0.28);
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
    gap: 0;
    padding: 0;
  }

  .settings-open .chat-area {
    flex: 1;
  }

  .settings-toggle-button span {
    display: none;
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

  .settings-toggle-button {
    min-width: 44px;
    min-height: 36px;
  }
}
</style>

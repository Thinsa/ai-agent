<template>
  <div class="love-master-container">
    <PageHeader title="知心 Soul" back-to="/" gradient="var(--gradient-soul)">
      <template #actions>
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
      </template>
    </PageHeader>

    <div class="content-wrapper" :class="{ 'settings-open': settingsOpen }">
      <AgentSettingsPanel
        v-if="settingsOpen"
        theme="love"
        :web-search-enabled="webSearchEnabled"
        :mcp-enabled="mcpEnabled"
        :knowledge-enabled="knowledgeEnabled"
        :vision-enabled="visionEnabled"
        :custom-prompt="customPrompt"
        :show-knowledge="true"
        :knowledge-doc-count="knowledgeDocCount"
        prompt-placeholder="你是一位恋爱大师，可以根据我的需求给出相关建议&#10;# 知识库&#10;请记住以下材料，它们可能对回答问题有帮助。&#10;${documents}"
        @toggle-open="settingsOpen = false"
        @update:web-search-enabled="webSearchEnabled = $event"
        @update:mcp-enabled="updateMcpEnabled"
        @update:knowledge-enabled="updateKnowledgeEnabled"
        @update:vision-enabled="visionEnabled = $event"
        @update:custom-prompt="customPrompt = $event"
        @open-knowledge-config="showKnowledgeManager = true"
      />

      <ChatHistorySidebar
        v-else
        :sessions="historySessions"
        :active-chat-id="chatId"
        :loading="historyLoading"
        module="love"
        @select-session="loadHistory"
        @new-session="startNewSession"
        @delete-session="handleDeleteSession"
      />

      <div class="chat-area">
        <ChatRoom
          :messages="messages"
          :connection-status="connectionStatus"
          :stream-paused="streamPaused"
          ai-type="love"
          @send-message="sendMessage"
          @stop-generation="stopGeneration"
          @resume-generation="resumeGeneration"
        />
      </div>
    </div>

    <KnowledgeDocManager
      :visible="showKnowledgeManager"
      @close="showKnowledgeManager = false"
      @documents-changed="handleDocumentsChanged"
    />
  </div>
</template>

<script setup>
import { onBeforeUnmount, onMounted, ref } from 'vue'
import { useHead } from '@vueuse/head'
import PageHeader from '../components/PageHeader.vue'
import AgentSettingsPanel from '../components/AgentSettingsPanel.vue'
import ChatHistorySidebar from '../components/ChatHistorySidebar.vue'
import ChatRoom from '../components/ChatRoom.vue'
import KnowledgeDocManager from '../components/KnowledgeDocManager.vue'
import { useSseChat } from '../composables/useSseChat'
import { chatWithLoveApp, chatWithLoveAppMcp, chatWithLoveAppRag, deleteChatSession, getChatHistory, listChatSessions, getKnowledgeDocumentCount } from '../api'

useHead({
  title: '知心 Soul - LinkMind 灵桥',
  meta: [
    {
      name: 'description',
      content: '知心 Soul 是 LinkMind 灵桥的情感伴侣，懂你心事，陪你聊天，解答各种情感困惑。'
    },
    {
      name: 'keywords',
      content: '知心Soul,LinkMind,灵桥,情感伴侣,恋爱咨询,AI聊天,共情倾听'
    }
  ]
})

const { connectionStatus, streamPaused } = useSseChat()

const messages = ref([])
const chatId = ref('')
const settingsOpen = ref(false)
const showKnowledgeManager = ref(false)
const knowledgeDocCount = ref(0)
const webSearchEnabled = ref(false)
const mcpEnabled = ref(false)
const knowledgeEnabled = ref(false)
const visionEnabled = ref(false)
const customPrompt = ref('')
const historySessions = ref([])
const historyLoading = ref(false)
let eventSource = null

const generateChatId = () => `love_${Math.random().toString(36).slice(2, 10)}`

const activeModeLabel = isUser => {
  if (mcpEnabled.value) {
    return isUser ? 'MCP 请求' : 'MCP 回复'
  }
  if (knowledgeEnabled.value) {
    return isUser ? '知识库请求' : '知识库回复'
  }
  return webSearchEnabled.value ? '联网搜索' : ''
}

const addMessage = (content, isUser, extra = {}) => {
  messages.value.push({
    content,
    isUser,
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
  closeStream()
  if (mcpEnabled.value) {
    runMcpChat('请继续完成上面的回答')
  } else if (knowledgeEnabled.value) {
    runRagChat('请继续完成上面的回答')
  } else {
    runStandardChat('请继续完成上面的回答', null)
  }
}

const loadSessions = async () => {
  historyLoading.value = true
  try {
    historySessions.value = await listChatSessions('love')
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
  const detail = await getChatHistory(targetChatId, 'love')
  chatId.value = detail.chatId
  messages.value = detail.messages.map(toPageMessage)
  await loadSessions()
}

const handleDeleteSession = async (session) => {
  const title = session.title || session.chatId
  if (!confirm(`确定删除会话「${title}」？删除后不可恢复。`)) return
  try {
    await deleteChatSession(session.chatId, 'love')
    historySessions.value = historySessions.value.filter(s => s.chatId !== session.chatId)
    if (session.chatId === chatId.value) {
      startNewSession()
    }
  } catch (e) {
    console.error('Delete session failed:', e)
  }
}

const startNewSession = async () => {
  closeStream()
  connectionStatus.value = 'disconnected'
  streamPaused.value = false
  chatId.value = generateChatId()
  messages.value = []
  addMessage('你好，我是知心 Soul，LinkMind 灵桥的情感伴侣。请告诉我你的心事，我会温柔倾听，给你贴心的建议。', false)
  await loadSessions()
  // 乐观插入：侧边栏立即可见，不写 DB，首条消息发出时由后端懒创建
  if (!historySessions.value.some(s => s.chatId === chatId.value)) {
    historySessions.value.unshift({
      module: 'love',
      chatId: chatId.value,
      title: '新会话',
      createdAt: new Date().toISOString(),
      updatedAt: new Date().toISOString()
    })
  }
}

const updateMcpEnabled = enabled => {
  mcpEnabled.value = enabled
  if (enabled) {
    knowledgeEnabled.value = false
  }
}

const updateKnowledgeEnabled = enabled => {
  knowledgeEnabled.value = enabled
  if (enabled) {
    mcpEnabled.value = false
  }
}

const buildPrompt = message => {
  const parts = [
    webSearchEnabled.value
      ? '允许在需要时结合联网搜索信息回答。'
      : '不要使用联网搜索，请基于已有知识回答。'
  ]
  if (customPrompt.value.trim()) {
    parts.push(`额外提示词：${customPrompt.value.trim()}`)
  }
  parts.push(`用户问题：${message}`)
  return parts.join('\n')
}

const runStandardChat = (message, imageUrl) => {
  const aiMessageIndex = messages.value.length
  addMessage('', false, { modeLabel: activeModeLabel(false) })
  connectionStatus.value = 'connecting'
  eventSource = chatWithLoveApp(buildPrompt(message), chatId.value, imageUrl)

  eventSource.onmessage = event => {
    const data = event.data
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

  eventSource.onerror = error => {
    console.error('SSE Error:', error)
    connectionStatus.value = 'error'
    if (aiMessageIndex < messages.value.length && !messages.value[aiMessageIndex].content) {
      messages.value[aiMessageIndex].content = '请求失败，请稍后重试。'
      messages.value[aiMessageIndex].type = 'ai-error'
    }
    closeStream()
  }
}

const runMcpChat = async message => {
  connectionStatus.value = 'connecting'
  try {
    const result = await chatWithLoveAppMcp(buildPrompt(message), chatId.value)
    addMessage(result, false, { modeLabel: activeModeLabel(false) })
    connectionStatus.value = 'disconnected'
    await loadSessions()
  } catch (error) {
    console.error('MCP chat error:', error)
    addMessage('MCP 请求失败，请确认 MCP Server 已启动。', false, {
      type: 'ai-error',
      modeLabel: activeModeLabel(false)
    })
    connectionStatus.value = 'error'
  }
}

const runRagChat = async message => {
  connectionStatus.value = 'connecting'
  try {
    const result = await chatWithLoveAppRag(buildPrompt(message), chatId.value)
    addMessage(result, false, { modeLabel: activeModeLabel(false) })
    connectionStatus.value = 'disconnected'
    await loadSessions()
  } catch (error) {
    console.error('RAG chat error:', error)
    addMessage('知识库请求失败，请稍后重试。', false, {
      type: 'ai-error',
      modeLabel: activeModeLabel(false)
    })
    connectionStatus.value = 'error'
  }
}

const sendMessage = payload => {
  const text = typeof payload === 'string' ? payload : payload.text
  const imageUrl = typeof payload === 'string' ? null : payload.imageUrl

  streamPaused.value = false
  addMessage(text, true, { modeLabel: activeModeLabel(true) })
  closeStream()

  if (mcpEnabled.value) {
    runMcpChat(text)
    return
  }
  if (knowledgeEnabled.value) {
    runRagChat(text)
    return
  }

  runStandardChat(text, visionEnabled.value ? imageUrl : null)
}

const fetchKnowledgeDocCount = async () => {
  try {
    const count = await getKnowledgeDocumentCount()
    knowledgeDocCount.value = count.enabledCount ?? count.totalCount ?? 0
  } catch (e) {
    // silently ignore
  }
}

const handleDocumentsChanged = () => {
  fetchKnowledgeDocCount()
}

onMounted(() => {
  startNewSession()
  loadSessions()
  fetchKnowledgeDocCount()
})

onBeforeUnmount(() => {
  closeStream()
})
</script>

<style scoped>
.love-master-container {
  display: flex; flex-direction: column; height: 100vh; min-height: 0; overflow: hidden;
  background: var(--color-base-0);
}
.content-wrapper { position: relative; display: flex; flex: 1; min-height: 0; flex-direction: row; gap: 0; padding: 0; overflow: hidden; }
.chat-area { position: relative; display: flex; flex: 1; min-width: 0; min-height: 0; overflow: hidden; }
.settings-open .chat-area { flex: 0 0 50%; }

.settings-toggle-button {
  display: inline-flex; align-items: center; justify-content: center; gap: 6px;
  min-width: 88px; min-height: 40px;
  border: var(--border-subtle); border-radius: var(--radius-sm);
  background: rgba(240,144,160,0.08); color: var(--color-aurora-3);
  cursor: pointer; font-size: 14px; font-weight: 700; line-height: 1;
  transition: background var(--duration-fast) var(--ease-out),
              border-color var(--duration-fast) var(--ease-out);
}
.settings-toggle-button:hover, .settings-toggle-button.active {
  border-color: rgba(240,144,160,0.35);
  background: rgba(240,144,160,0.16);
}
.settings-toggle-button svg {
  width: 21px; height: 21px; fill: none; stroke: currentColor;
  stroke-linecap: round; stroke-linejoin: round; stroke-width: 1.8;
}

@media (max-width: 768px) {
  .content-wrapper { flex-direction: column; padding: 0; }
  .settings-open .chat-area { flex: 1; }
  .settings-toggle-button span { display: none; }
}
@media (max-width: 480px) {
  .settings-toggle-button { min-width: 44px; min-height: 36px; }
}
</style>

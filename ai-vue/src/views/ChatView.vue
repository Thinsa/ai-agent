<template>
  <div class="spark-chat-page">
    <PageHeader title="灵语 Spark" back-to="/">
      <template #actions>
        <button class="mode-toggle" type="button" :class="{ active: currentMode === 'story' }" @click="switchMode('story')">🤖 AI剧本</button>
        <button class="mode-toggle" type="button" :class="{ active: currentMode === 'fixed' }" @click="switchMode('fixed')">📖 {{ scriptTitle }}</button>
      </template>
    </PageHeader>

    <div class="content-wrapper" :class="{ 'guide-open': currentMode === 'fixed' && guideOpen }">
      <ChatHistorySidebar
        :sessions="historySessions"
        :active-chat-id="chatId"
        :loading="historyLoading"
        module="chat"
        @select-session="loadHistory"
        @new-session="startNewSession"
      />

      <StoryGuidePanel
        v-if="currentMode === 'fixed' && guideOpen"
        :enabled="guideEnabled"
        :endings="storyEndings"
        :target-ending-id="targetEndingId"
        :status="guideStatus"
        @toggle-open="guideOpen = false"
        @update:enabled="setGuideEnabled"
        @update:targetEndingId="setTargetEndingId"
      />

      <div class="chat-area">
        <ChatRoom
          :messages="messages"
          :connection-status="connectionStatus"
          :stream-paused="streamPaused"
          ai-type="spark"
          @send-message="sendMessage"
          @stop-generation="stopGeneration"
          @resume-generation="resumeGeneration"
        >
          <template v-if="currentMode === 'fixed'" #inputActions>
            <button
              class="guide-toggle-button"
              type="button"
              :class="{ active: guideOpen || guideEnabled }"
              :title="guideOpen ? '关闭结局引导' : '打开结局引导'"
              :aria-label="guideOpen ? '关闭结局引导' : '打开结局引导'"
              @click="guideOpen = !guideOpen"
            >
              <svg viewBox="0 0 24 24" aria-hidden="true">
                <path d="M12 2.25 5.25 5.1v5.25c0 4.35 2.76 8.46 6.75 9.9 3.99-1.44 6.75-5.55 6.75-9.9V5.1L12 2.25Z" />
                <path d="M12 7.5v5.25l3.15 1.8" />
              </svg>
              <span>引导</span>
            </button>
          </template>
        </ChatRoom>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useHead } from '@vueuse/head'
import ChatHistorySidebar from '../components/ChatHistorySidebar.vue'
import ChatRoom from '../components/ChatRoom.vue'
import StoryGuidePanel from '../components/StoryGuidePanel.vue'
import PageHeader from '../components/PageHeader.vue'
import { useSseChat } from '../composables/useSseChat'
import { useFixedStory } from '../composables/useFixedStory'
import { useStoryGuide } from '../composables/useStoryGuide'
import { createStorySave, listStorySaves, getChatHistory, listChatSessions } from '../api'
import { getStoredToken } from '../api'
import { startStory, chooseOption, getSaveData, loadFromSave, resetStory, isAtEnding } from '../stores/fixedStoryStore'
import { getGuideStatus } from '../stores/storyGuide'

useHead({
  title: '灵语 Spark 互动剧本 - LinkMind 灵桥',
  meta: [
    {
      name: 'description',
      content: '灵语 Spark 是 LinkMind 灵桥的互动剧本主持人，为你展开分支故事，每个选择都通往不同的结局。'
    },
    {
      name: 'keywords',
      content: '灵语Spark,LinkMind,灵桥,互动剧本,分支故事,选择结局,AI叙事'
    }
  ]
})

// SSE management
const { connectionStatus, streamPaused, startStream, closeStream, stopGeneration } = useSseChat()

// Fixed story
const fixedStory = useFixedStory()
const scriptTitle = fixedStory.scriptTitle

// Story guide — destructure refs for template auto-unwrapping
const {
  guideEnabled,
  targetEndingId,
  guideOpen,
  storyEndings,
  selectedEndingTitle,
  setGuideEnabled: setGuideEnabledRaw,
  setTargetEndingId: setTargetEndingIdRaw,
  getHint,
} = useStoryGuide(() => fixedStory.getCurrentScene()?.id)

const messages = ref([])
const chatId = ref('')
const historySessions = ref([])
const historyLoading = ref(false)
const currentMode = ref('story') // 'story' | 'fixed'

const guideStatus = computed(() => {
  if (currentMode.value !== 'fixed') {
    return { state: 'not_started' }
  }
  return getGuideStatus(fixedStory.getCurrentScene()?.id, targetEndingId.value)
})

const generateChatId = () => 'spark_' + Math.random().toString(36).slice(2, 10)

const addMessage = (content, isUser, extra = {}) => {
  messages.value.push({
    content,
    isUser,
    type: isUser ? 'user-question' : 'ai-answer',
    time: Date.now(),
    ...extra
  })
}

const addGuideMessage = content => {
  if (content) {
    addMessage(content, false, { modeLabel: '结局引导', keepsStoryChoices: true })
  }
}

const addGuideHintForCurrentScene = () => {
  if (currentMode.value !== 'fixed' || fixedStory.isAtEnding()) {
    return
  }
  const hint = getHint()
  if (hint) addGuideMessage(hint)
}

const addEndingGuideResult = () => {
  if (currentMode.value !== 'fixed' || !guideEnabled.value || !targetEndingId.value) {
    return
  }
  if (selectedEndingTitle.value && fixedStory.getEndingTitle() !== selectedEndingTitle.value) {
    addGuideMessage(`灵语 Spark 轻轻按灭桥灯：你已抵达「${fixedStory.getEndingTitle() || '其他结局'}」，不是目标「${selectedEndingTitle.value}」。可重新开始再试。`)
  }
}

const setGuideEnabled = enabled => {
  setGuideEnabledRaw(enabled)
  if (enabled) {
    if (!targetEndingId.value) {
      addGuideMessage('灵语 Spark 低声提醒：先在引导面板中选择想抵达的结局，我再替你拨亮下一盏桥灯。')
      return
    }
    addGuideHintForCurrentScene()
  }
}

const setTargetEndingId = endingId => {
  setTargetEndingIdRaw(endingId)
  addGuideHintForCurrentScene()
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

const buildSseUrl = (path, params = {}) => {
  const token = getStoredToken()
  const mergedParams = token ? { ...params, token } : params
  const queryString = Object.keys(mergedParams)
    .filter(key => mergedParams[key] !== undefined && mergedParams[key] !== null)
    .map(key => `${encodeURIComponent(key)}=${encodeURIComponent(mergedParams[key])}`)
    .join('&')
  const baseUrl = import.meta.env.PROD ? '/api' : 'http://localhost:8123/api'
  return `${baseUrl}${path}${queryString ? `?${queryString}` : ''}`
}

const resumeGeneration = () => {
  streamPaused.value = false
  closeStream()
  runChat('请继续完成上面的回答')
}

const loadSessions = async () => {
  historyLoading.value = true
  try {
    historySessions.value = await listChatSessions('chat')
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
  const detail = await getChatHistory(targetChatId, 'chat')
  chatId.value = detail.chatId
  messages.value = detail.messages.map(toPageMessage)
}

const startNewSession = () => {
  closeStream()
  connectionStatus.value = 'disconnected'
  streamPaused.value = false
  chatId.value = generateChatId()
  messages.value = []
  if (currentMode.value === 'fixed') {
    resetStory()
    fixedStory._storySaves.value = []
    addMessage('这是一个关于灵桥、命运与选择的奇幻故事，拥有 4 种不同结局。\n\n输入「开始」进入故事，输入「存档」保存进度，输入「读档」读取存档。', false)
  } else {
    addMessage('你好，我是灵语 Spark 的互动剧本主持人！给我一个主题（如"武侠""科幻""悬疑""末日"），或者直接说"开始"，我会为你展开一段分支故事。在每个关键节点，你可以从选项中选择下一步行动，决定故事的最终结局。', false)
  }
}

const runChat = message => {
  const aiMessageIndex = messages.value.length
  addMessage('', false)

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
      messages.value[aiMessageIndex].content = '请求失败，请稍后重试。'
      messages.value[aiMessageIndex].type = 'ai-error'
    }
    closeStream()
  }

  const url = buildSseUrl('/ai/story/sse', { message, chatId: chatId.value })
  const es = startStream(url)
  connectionStatus.value = 'connecting'
  es.onmessage = event => onMessage(event.data)
  es.onerror = onError
}

const switchMode = mode => {
  if (currentMode.value === mode) return
  currentMode.value = mode
  closeStream()
  connectionStatus.value = 'disconnected'
  streamPaused.value = false
  if (mode === 'fixed') {
    resetStory()
    fixedStory._storySaves.value = []
  }
  startNewSession()
}

const sendMessage = payload => {
  const text = typeof payload === 'string' ? payload : payload.text
  streamPaused.value = false

  if (currentMode.value === 'fixed') {
    handleFixedStoryMessage(text)
    return
  }

  // AI 剧本模式
  addMessage(text, true)
  closeStream()
  runChat(text)
}

const handleFixedStoryMessage = message => {
  const trimmed = message.trim()

  // 如果在读档列表中做了选择
  if (fixedStory._storySaves.value.length) {
    const saveIdx = parseInt(trimmed) - 1
    if (!isNaN(saveIdx) && saveIdx >= 0) {
      addMessage(trimmed, true)
      handleLoadChoice(saveIdx)
      return
    }
  }

  // 存档指令
  if (trimmed === '存档') {
    addMessage('存档', true)
    saveCurrentProgress()
    return
  }

  // 读档指令
  if (trimmed === '读档') {
    addMessage('读档', true)
    showLoadPanel()
    return
  }

  // 开始新故事
  if (trimmed === '开始' || trimmed === '重新开始') {
    addMessage(trimmed, true)
    const output = startStory()
    if (output) addMessage(output, false)
    addGuideHintForCurrentScene()
    return
  }

  // 数字选择
  addMessage(trimmed, true)
  const choiceIdx = parseInt(trimmed) - 1
  if (!isNaN(choiceIdx) && choiceIdx >= 0) {
    const output = chooseOption(choiceIdx)
    if (output) {
      addMessage(output, false)
      if (isAtEnding()) {
        addEndingGuideResult()
        addMessage('输入「重新开始」开启新剧本，输入「存档」保存进度。', false, { keepsStoryChoices: true })
      } else {
        addGuideHintForCurrentScene()
      }
      return
    }
  }
  addMessage('请输入选项编号（如 1、2、3），或输入「存档」保存、「读档」读取存档。', false)
}

const saveCurrentProgress = async () => {
  try {
    const data = getSaveData()
    await createStorySave(data.storyId, data.sceneId, data.choiceHistory)
    addMessage('✅ 已存档。随时可以输入「读档」来恢复进度。', false)
  } catch (e) {
    addMessage('❌ 存档失败，请稍后重试。', false)
  }
}

const showLoadPanel = async () => {
  try {
    const saves = await listStorySaves()
    if (!saves || saves.length === 0) {
      addMessage('暂无存档记录。在剧本中输入「存档」来创建存档。', false)
      return
    }
    let text = '📂 存档列表（点击数字读取）：\n\n'
    text += '【选项】\n'
    saves.forEach((s, i) => {
      const time = new Date(s.savedAt).toLocaleString('zh-CN')
      text += `${i + 1}. ${time} — 场景: ${s.sceneId}\n`
    })
    text += `${saves.length + 1}. 取消`
    fixedStory._storySaves.value = saves
    addMessage(text, false)
  } catch (e) {
    addMessage('❌ 读取存档列表失败。', false)
  }
}

const handleLoadChoice = idx => {
  const saves = fixedStory._storySaves.value
  if (!saves.length || idx >= saves.length) {
    addMessage('已取消读档。', false)
    fixedStory._storySaves.value = []
    return
  }
  const save = saves[idx]
  const output = loadFromSave(save)
  if (output) {
    addMessage('✅ 已读取存档。', false)
    addMessage(output, false)
    if (isAtEnding()) {
      addEndingGuideResult()
    } else {
      addGuideHintForCurrentScene()
    }
  }
  fixedStory._storySaves.value = []
}

onMounted(() => {
  startNewSession()
  loadSessions()
})
</script>

<style scoped>
.spark-chat-page {
  display: flex; flex-direction: column; height: 100vh; min-height: 0; overflow: hidden;
  background: var(--color-base-0);
}
.content-wrapper { position: relative; display: flex; flex: 1; min-height: 0; overflow: hidden; }
.chat-area { position: relative; display: flex; flex: 1; min-width: 0; min-height: 0; overflow: hidden; }
.guide-open .chat-area { flex: 0 0 50%; }

.guide-toggle-button {
  display: inline-flex; align-items: center; justify-content: center; gap: 6px;
  min-height: 34px; border: 1px solid rgba(180,160,232,0.22);
  border-radius: var(--radius-sm); padding: 0 12px;
  background: rgba(180,160,232,0.06); color: var(--color-aurora-2);
  cursor: pointer; font-size: 14px; font-weight: 800; white-space: nowrap;
  transition: background var(--duration-fast) var(--ease-out),
              border-color var(--duration-fast) var(--ease-out),
              transform var(--duration-fast) var(--ease-out);
}
.guide-toggle-button:hover, .guide-toggle-button.active {
  border-color: rgba(180,160,232,0.45);
  background: rgba(180,160,232,0.13);
}
.guide-toggle-button:hover { transform: translateY(-1px); }
.guide-toggle-button svg {
  width: 18px; height: 18px; fill: none; stroke: currentColor;
  stroke-linecap: round; stroke-linejoin: round; stroke-width: 1.8;
}

.mode-toggle {
  display: inline-flex; align-items: center; gap: 4px;
  padding: 5px 12px;
  border: var(--border-subtle); border-radius: var(--radius-full);
  background: rgba(255,255,255,0.04); color: var(--color-text-2);
  cursor: pointer; font-size: 13px; font-weight: 600;
  transition: all var(--duration-fast) var(--ease-out); white-space: nowrap;
}
.mode-toggle:hover { background: rgba(255,255,255,0.08); color: var(--color-text-1); }
.mode-toggle.active {
  background: rgba(180,160,232,0.14);
  border-color: rgba(180,160,232,0.40);
  color: var(--color-aurora-2);
}

@media (max-width: 768px) {
  .content-wrapper { flex-direction: column; }
  .guide-open .chat-area { flex: 1; }
}
@media (max-width: 480px) {
  .guide-toggle-button span { display: none; }
}
</style>

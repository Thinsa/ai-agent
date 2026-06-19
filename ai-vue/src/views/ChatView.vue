<template>
  <div class="spark-chat-page">
    <div class="header">
      <button class="back-button" type="button" @click="goBack">返回</button>
      <h1 class="title">灵语 Spark</h1>
      <div class="header-actions">
        <button class="mode-toggle" type="button" :class="{ active: currentMode === 'story' }" @click="switchMode('story')">🤖 AI剧本</button>
        <button class="mode-toggle" type="button" :class="{ active: currentMode === 'fixed' }" @click="switchMode('fixed')">📖 {{ scriptTitle }}</button>
      </div>
    </div>

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
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useHead } from '@vueuse/head'
import ChatHistorySidebar from '../components/ChatHistorySidebar.vue'
import ChatRoom from '../components/ChatRoom.vue'
import StoryGuidePanel from '../components/StoryGuidePanel.vue'
import { chatStory, createStorySave, listStorySaves, deleteStorySave, getChatHistory, listChatSessions } from '../api'
import { startStory, chooseOption, getSaveData, loadFromSave, resetStory, isAtEnding, getEndingTitle, getScriptMeta, getCurrentScene } from '../stores/fixedStoryStore'
import { createGuideHint, getGuideStatus, getStoryEndings } from '../stores/storyGuide'

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

const router = useRouter()
const messages = ref([])
const chatId = ref('')
const connectionStatus = ref('disconnected')
const streamPaused = ref(false)
const historySessions = ref([])
const historyLoading = ref(false)
let eventSource = null
const currentMode = ref('story') // 'story' | 'fixed'
const scriptTitle = computed(() => getScriptMeta().title)
const guideOpen = ref(false)
const guideEnabled = ref(false)
const targetEndingId = ref('')
const storyEndings = getStoryEndings()
const guideStatus = computed(() => {
  if (currentMode.value !== 'fixed') {
    return { state: 'not_started' }
  }
  return getGuideStatus(getCurrentScene()?.id, targetEndingId.value)
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
  if (currentMode.value !== 'fixed' || !guideEnabled.value || isAtEnding()) {
    return
  }
  addGuideMessage(createGuideHint(getCurrentScene()?.id, targetEndingId.value))
}

const selectedEndingTitle = computed(() => {
  return storyEndings.find(ending => ending.id === targetEndingId.value)?.title || ''
})

const addEndingGuideResult = () => {
  if (currentMode.value !== 'fixed' || !guideEnabled.value || !targetEndingId.value) {
    return
  }
  if (selectedEndingTitle.value && getEndingTitle() !== selectedEndingTitle.value) {
    addGuideMessage(`灵语 Spark 轻轻按灭桥灯：你已抵达「${getEndingTitle() || '其他结局'}」，不是目标「${selectedEndingTitle.value}」。可重新开始再试。`)
  }
}

const setGuideEnabled = enabled => {
  guideEnabled.value = enabled
  if (enabled) {
    if (!targetEndingId.value) {
      addGuideMessage('灵语 Spark 低声提醒：先在引导面板中选择想抵达的结局，我再替你拨亮下一盏桥灯。')
      return
    }
    addGuideHintForCurrentScene()
  }
}

const setTargetEndingId = endingId => {
  targetEndingId.value = endingId
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
    window.__storySaves = null
    addMessage('这是一个关于灵桥、命运与选择的奇幻故事，拥有 4 种不同结局。\n\n输入「开始」进入故事，输入「存档」保存进度，输入「读档」读取存档。', false)
  } else {
    addMessage('你好，我是灵语 Spark 的互动剧本主持人！给我一个主题（如"武侠""科幻""悬疑""末日"），或者直接说"开始"，我会为你展开一段分支故事。在每个关键节点，你可以从选项中选择下一步行动，决定故事的最终结局。', false)
  }
}

const runChat = message => {
  const aiMessageIndex = messages.value.length
  addMessage('', false)
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
      messages.value[aiMessageIndex].content = '请求失败，请稍后重试。'
      messages.value[aiMessageIndex].type = 'ai-error'
    }
    closeStream()
  }

  eventSource = chatStory(message, chatId.value, onMessage, onError)
  eventSource.onmessage = event => onMessage(event.data)
  eventSource.onerror = onError
}

const switchMode = mode => {
  if (currentMode.value === mode) return
  currentMode.value = mode
  closeStream()
  connectionStatus.value = 'disconnected'
  streamPaused.value = false
  if (mode === 'fixed') {
    resetStory()
    window.__storySaves = null
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
  if (window.__storySaves) {
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
    window.__storySaves = saves
    addMessage(text, false)
  } catch (e) {
    addMessage('❌ 读取存档列表失败。', false)
  }
}

const handleLoadChoice = idx => {
  const saves = window.__storySaves
  if (!saves || idx >= saves.length) {
    addMessage('已取消读档。', false)
    window.__storySaves = null
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
  window.__storySaves = null
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
.spark-chat-page {
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
  background-color: #7c3aed;
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

.back-button:hover { opacity: 0.8; }
.back-button::before { content: '<'; margin-right: 8px; }

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
}

.content-wrapper {
  position: relative;
  display: flex;
  flex: 1;
  min-height: 0;
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

.guide-open .chat-area {
  flex: 0 0 50%;
}

.guide-toggle-button {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  min-height: 34px;
  border: 1px solid rgba(124, 58, 237, 0.24);
  border-radius: 8px;
  padding: 0 12px;
  background: rgba(124, 58, 237, 0.07);
  color: #6d28d9;
  cursor: pointer;
  font-size: 14px;
  font-weight: 800;
  transition: background-color 0.2s ease, border-color 0.2s ease, transform 0.2s ease;
  white-space: nowrap;
}

.guide-toggle-button:hover,
.guide-toggle-button.active {
  border-color: rgba(124, 58, 237, 0.55);
  background: rgba(124, 58, 237, 0.13);
}

.guide-toggle-button:hover {
  transform: translateY(-1px);
}

.guide-toggle-button svg {
  width: 18px;
  height: 18px;
  fill: none;
  stroke: currentColor;
  stroke-linecap: round;
  stroke-linejoin: round;
  stroke-width: 1.8;
}

@media (max-width: 768px) {
  .header { padding: 12px 16px; }
  .title { font-size: 18px; }

  .content-wrapper {
    flex-direction: column;
  }

  .guide-open .chat-area {
    flex: 1;
  }
}

@media (max-width: 480px) {
  .header { padding: 10px 12px; }
  .back-button { font-size: 14px; }
  .title { font-size: 16px; }
  .guide-toggle-button span { display: none; }
}

.mode-toggle {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 5px 12px;
  border: 1px solid rgba(255, 255, 255, 0.3);
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.08);
  color: rgba(255, 255, 255, 0.7);
  cursor: pointer;
  font-size: 13px;
  font-weight: 600;
  transition: all 0.2s ease;
  white-space: nowrap;
}
.mode-toggle:hover {
  background: rgba(255, 255, 255, 0.16);
  color: #ffffff;
}
.mode-toggle.active {
  background: rgba(255, 255, 255, 0.24);
  border-color: rgba(255, 255, 255, 0.7);
  color: #ffffff;
  box-shadow: 0 0 10px rgba(255, 255, 255, 0.15);
}

</style>

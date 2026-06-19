<template>
  <div class="chat-container">
    <div class="chat-bg" :style="bgStyle"></div>
    <div class="chat-messages" ref="messagesContainer">
      <div v-for="(msg, index) in messages" :key="index" class="message-wrapper">
        <div v-if="!msg.isUser" class="message ai-message" :class="[msg.type]">
          <div class="avatar ai-avatar">
            <AiAvatarFallback :type="aiType" />
          </div>
          <div class="message-bubble">
            <div v-if="msg.modeLabel" class="message-mode">{{ msg.modeLabel }}</div>
            <div class="message-content">
              {{ getStoryText(msg.content) }}
              <span
                v-if="connectionStatus === 'connecting' && index === messages.length - 1"
                class="typing-indicator"
              >
                think...
              </span>
            </div>
            <!-- 选项按钮：仅最后一条 AI 消息且连接空闲时显示 -->
            <div
              v-if="getStoryChoices(msg.content).length && connectionStatus !== 'connecting' && index === latestChoiceMessageIndex"
              class="story-choices"
            >
              <button
                v-for="choice in getStoryChoices(msg.content)"
                :key="choice.number"
                type="button"
                class="choice-btn"
                :class="{ 'custom-choice': choice.number === '5' }"
                @click="selectChoice(choice.number)"
              >
                {{ choice.number }}. {{ choice.label }}
              </button>
            </div>
            <!-- 结局标记 -->
            <div
              v-if="isStoryEnding(msg.content) && connectionStatus !== 'connecting' && index === latestChoiceMessageIndex"
              class="story-ending"
            >
              <span class="ending-badge">📖 故事完结</span>
              <button type="button" class="restart-btn" @click="$emit('send-message', '开始')">
                开始新故事
              </button>
            </div>
            <div v-if="extractImageUrls(msg.content).length" class="image-grid">
              <a
                v-for="url in extractImageUrls(msg.content)"
                :key="url"
                :href="url"
                target="_blank"
                rel="noreferrer"
                class="image-link"
              >
                <img :src="url" alt="preview" class="image-preview">
              </a>
            </div>
            <div class="message-footer">
              <div class="message-time">{{ formatTime(msg.time) }}</div>
              <button
                v-if="connectionStatus === 'connecting' && !streamPaused && index === messages.length - 1"
                type="button"
                class="stream-control-btn stop-btn"
                @click.stop="$emit('stop-generation')"
              >
                ⏸ 停止
              </button>
              <button
                v-if="streamPaused && index === messages.length - 1"
                type="button"
                class="stream-control-btn resume-btn"
                @click.stop="$emit('resume-generation')"
              >
                ▶ 继续
              </button>
            </div>
          </div>
        </div>

        <div v-else class="message user-message" :class="[msg.type]">
          <div class="message-bubble">
            <div v-if="msg.modeLabel" class="message-mode user-mode">{{ msg.modeLabel }}</div>
            <div class="message-content">{{ msg.content }}</div>
            <div class="message-time">{{ formatTime(msg.time) }}</div>
          </div>
          <div class="avatar user-avatar">
            <div class="avatar-placeholder">U</div>
          </div>
        </div>
      </div>
    </div>

    <div class="chat-input-container">
      <div v-if="$slots.inputActions" class="input-actions">
        <slot name="inputActions"></slot>
      </div>
      <!-- 已上传文件提示 -->
      <div v-if="attachedFile" class="file-attached">
        <img v-if="attachedFile.isImage === 'true'" :src="attachedFile.imageUrl" class="file-thumb" />
        <span v-else class="file-icon">📄</span>
        <span class="file-name">{{ attachedFile.fileName }}</span>
        <button type="button" class="file-remove-btn" @click="removeFile">✕</button>
      </div>
      <div class="chat-input">
        <input
          ref="fileInput"
          type="file"
          class="file-input-hidden"
          :disabled="uploading || connectionStatus === 'connecting'"
          @change="handleFileSelect"
        />
        <button
          type="button"
          class="attach-btn"
          :disabled="uploading || connectionStatus === 'connecting'"
          :title="uploading ? '上传中...' : '上传文件'"
          @click="fileInput?.click()"
        >
          {{ uploading ? '⏳' : '📎' }}
        </button>
        <textarea
          v-model="inputMessage"
          class="input-box"
          :disabled="connectionStatus === 'connecting'"
          placeholder="请输入消息..."
          @keydown.enter.prevent="sendMessage"
        />
        <button
          type="button"
          class="send-button"
          :disabled="connectionStatus === 'connecting' || (!inputMessage.trim() && !attachedFile)"
          @click="sendMessage"
        >
          发送
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, nextTick, onMounted, ref, watch } from 'vue'
import AiAvatarFallback from './AiAvatarFallback.vue'
import { getBackground } from '../stores/bgStore'
import { uploadFile } from '../api'

const props = defineProps({
  messages: {
    type: Array,
    default: () => []
  },
  connectionStatus: {
    type: String,
    default: 'disconnected'
  },
  aiType: {
    type: String,
    default: 'default'
  },
  streamPaused: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['send-message', 'stop-generation', 'resume-generation'])

const agentKey = computed(() => {
  return props.aiType === 'spark' ? 'chat' : props.aiType
})

const bgStyle = computed(() => {
  const bg = getBackground(agentKey.value)
  if (!bg.url) return { display: 'none' }
  return {
    backgroundImage: `url(${bg.url})`,
    backgroundSize: 'cover',
    backgroundPosition: 'center',
    backgroundRepeat: 'no-repeat',
    opacity: bg.opacity || 0.15
  }
})

const inputMessage = ref('')
const messagesContainer = ref(null)
const uploading = ref(false)
const attachedFile = ref(null)
const fileInput = ref(null)

const latestChoiceMessageIndex = computed(() => {
  for (let i = props.messages.length - 1; i >= 0; i--) {
    const message = props.messages[i]
    if (message?.isUser) {
      return -1
    }
    if (getStoryChoices(message?.content).length || isStoryEnding(message?.content)) {
      return i
    }
    if (!message?.keepsStoryChoices) {
      return -1
    }
  }
  return -1
})

const extractImageUrls = (content) => {
  if (!content) return []
  const imageUrlPattern = /https?:\/\/\S+\.(?:png|jpe?g|webp|gif)(?:\?\S*)?/gi
  const matches = content.match(imageUrlPattern) || []
  return [...new Set(matches)]
}

const extractStoryMeta = (content) => {
  if (!content) return { text: '', choices: [], isEnding: false }
  const choiceIdx = content.indexOf('【选项】')
  const endingIdx = content.indexOf('【结局】')
  const isEnding = endingIdx >= 0

  // 分离正文和选项
  let text = content
  let choices = []

  // 处理【结局】标记：移除标记本身，保留结局文字
  if (isEnding) {
    text = content.replace('【结局】', '').trim()
    return { text, choices: [], isEnding: true }
  }

  // 处理【选项】标记：提取正文和选项列表
  if (choiceIdx >= 0) {
    text = content.substring(0, choiceIdx).trim()
    const choicesSection = content.substring(choiceIdx + '【选项】'.length)
    // 解析编号选项：匹配 "1. 选项描述" 格式
    const choiceLines = choicesSection.split('\n')
    for (const line of choiceLines) {
      const match = line.match(/^(\d+)\.\s*(.+)/)
      if (match) {
        choices.push({ number: match[1], label: match[2].trim() })
      }
    }
  }

  return { text, choices, isEnding }
}

const getStoryText = (content) => {
  return extractStoryMeta(content).text
}

const getStoryChoices = (content) => {
  return extractStoryMeta(content).choices
}

const isStoryEnding = (content) => {
  return extractStoryMeta(content).isEnding
}

const selectChoice = (number) => {
  emit('send-message', number)
}

const handleFileSelect = async (event) => {
  const file = event.target.files?.[0]
  if (!file) return
  uploading.value = true
  try {
    const result = await uploadFile(file)
    attachedFile.value = { ...result, size: file.size }
  } catch (e) {
    alert('文件上传失败：' + (e?.response?.data?.message || e.message))
  } finally {
    uploading.value = false
  }
  event.target.value = ''
}

const removeFile = () => {
  attachedFile.value = null
}

const sendMessage = () => {
  if (!inputMessage.value.trim() && !attachedFile.value) {
    return
  }
  let message = inputMessage.value.trim()
  let imageUrl = null
  if (attachedFile.value) {
    const file = attachedFile.value
    if (file.isImage === 'true') {
      imageUrl = file.imageUrl
      if (!message) message = '请描述这张图片'
    } else {
      const fileInfo = `[上传文件: ${file.fileName}`
      const preview = file.preview
      if (preview) {
        message = `${fileInfo}]\n文件内容:\n${preview}\n\n${message || '请帮我分析这个文件'}`
      } else {
        message = `${fileInfo} (${file.contentType})]\n${message || '请帮我处理这个文件'}`
      }
    }
    attachedFile.value = null
  }
  emit('send-message', { text: message, imageUrl: imageUrl })
  inputMessage.value = ''
}

const formatTime = (timestamp) => {
  const date = new Date(timestamp)
  return date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
}

const scrollToBottom = async () => {
  await nextTick()
  if (messagesContainer.value) {
    messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
  }
}

watch(() => props.messages.length, () => {
  scrollToBottom()
})

watch(() => props.messages.map(message => message.content).join(''), () => {
  scrollToBottom()
})

onMounted(() => {
  scrollToBottom()
})
</script>

<style scoped>
.chat-container {
  display: flex;
  flex-direction: column;
  flex: 1;
  width: 100%;
  min-height: 0;
  background: linear-gradient(180deg, #f8fafc 0%, #eef2f7 100%);
  overflow: hidden;
  position: relative;
}

.chat-bg {
  position: absolute;
  inset: 0;
  z-index: 0;
  pointer-events: none;
}

.chat-messages {
  flex: 1;
  min-height: 0;
  overflow-y: auto;
  padding: 16px;
  display: flex;
  flex-direction: column;
  position: relative;
  z-index: 1;
}

.message-wrapper {
  margin-bottom: 16px;
  display: flex;
  flex-direction: column;
  width: 100%;
}

.message {
  display: flex;
  align-items: flex-start;
  max-width: 85%;
  margin-bottom: 8px;
}

.user-message {
  margin-left: auto;
  flex-direction: row;
}

.ai-message {
  margin-right: auto;
}

.avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  overflow: hidden;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
}

.user-avatar {
  margin-left: 8px;
}

.ai-avatar {
  margin-right: 8px;
}

.avatar-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #0f172a;
  color: #ffffff;
  font-weight: bold;
}

.message-bubble {
  padding: 12px;
  border-radius: 18px;
  position: relative;
  word-wrap: break-word;
  min-width: 100px;
}

.user-message .message-bubble {
  background-color: #0f172a;
  color: #ffffff;
  border-bottom-right-radius: 4px;
  text-align: left;
}

.ai-message .message-bubble {
  background-color: #ffffff;
  color: #1f2937;
  border: 1px solid rgba(148, 163, 184, 0.18);
  border-bottom-left-radius: 4px;
  text-align: left;
}

.message-mode {
  display: inline-flex;
  margin-bottom: 8px;
  padding: 3px 8px;
  border-radius: 999px;
  font-size: 11px;
  font-weight: 700;
  background: rgba(15, 23, 42, 0.08);
  color: #475569;
}

.user-mode {
  background: rgba(255, 255, 255, 0.18);
  color: #e2e8f0;
}

.message-content {
  font-size: 16px;
  line-height: 1.5;
  white-space: pre-wrap;
}

.image-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(120px, 1fr));
  gap: 8px;
  margin-top: 10px;
}

.image-link {
  display: block;
}

.image-preview {
  width: 100%;
  aspect-ratio: 1 / 1;
  object-fit: cover;
  border-radius: 8px;
  border: 1px solid rgba(148, 163, 184, 0.32);
  background: #ffffff;
}

.message-footer {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 8px;
  margin-top: 4px;
}

.message-time {
  font-size: 12px;
  opacity: 0.7;
}

.stream-control-btn {
  border: 1px solid rgba(148, 163, 184, 0.4);
  border-radius: 6px;
  padding: 2px 10px;
  font-size: 12px;
  cursor: pointer;
  transition: all 0.2s ease;
  background: rgba(255, 255, 255, 0.7);
  color: #475569;
  white-space: nowrap;
}

.stream-control-btn:hover {
  border-color: rgba(148, 163, 184, 0.7);
  background: rgba(255, 255, 255, 0.95);
}

.stop-btn {
  color: #dc2626;
  border-color: rgba(220, 38, 38, 0.3);
}

.stop-btn:hover {
  background: #fef2f2;
  border-color: #dc2626;
}

.resume-btn {
  color: #059669;
  border-color: rgba(5, 150, 105, 0.3);
}

.resume-btn:hover {
  background: #ecfdf5;
  border-color: #059669;
}

.chat-input-container {
  flex: 0 0 auto;
  background-color: #ffffff;
  border-top: 1px solid #e0e0e0;
  z-index: 100;
  position: relative;
  box-shadow: 0 -2px 10px rgba(0, 0, 0, 0.05);
}

.file-attached {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 6px 16px;
  background: #f0fdf4;
  border-bottom: 1px solid #dcfce7;
}

.file-name {
  font-size: 12px;
  color: #166534;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  flex: 1;
  min-width: 0;
}

.file-remove-btn {
  border: 0;
  background: transparent;
  color: #dc2626;
  cursor: pointer;
  font-size: 14px;
  padding: 2px 6px;
  border-radius: 4px;
}

.file-remove-btn:hover {
  background: #fef2f2;
}

.file-thumb {
  width: 40px;
  height: 40px;
  object-fit: cover;
  border-radius: 6px;
  flex-shrink: 0;
}

.file-icon {
  font-size: 18px;
  flex-shrink: 0;
}

.file-input-hidden {
  display: none;
}

.attach-btn {
  border: 0;
  background: transparent;
  font-size: 18px;
  cursor: pointer;
  padding: 4px 6px;
  border-radius: 8px;
  transition: all 0.2s;
  flex-shrink: 0;
  line-height: 1;
}

.attach-btn:hover:not(:disabled) {
  background: #f1f5f9;
  transform: scale(1.1);
}

.attach-btn:disabled {
  cursor: not-allowed;
  opacity: 0.4;
}

.input-actions {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 16px 0;
}

.chat-input {
  display: flex;
  padding: 12px 16px;
  box-sizing: border-box;
  align-items: center;
  min-height: 56px;
}

.input-box {
  flex-grow: 1;
  border: 1px solid #d0d7e2;
  border-radius: 20px;
  padding: 10px 16px;
  font-size: 16px;
  resize: none;
  min-height: 20px;
  max-height: 40px;
  outline: none;
  transition: border-color 0.3s;
  overflow-y: auto;
  scrollbar-width: none;
  -ms-overflow-style: none;
}

.input-box::-webkit-scrollbar {
  display: none;
}

.input-box:focus {
  border-color: #0f172a;
}

.send-button {
  margin-left: 12px;
  background-color: #0f172a;
  color: #ffffff;
  border: none;
  border-radius: 20px;
  padding: 0 20px;
  font-size: 16px;
  cursor: pointer;
  transition: background-color 0.3s;
  height: 40px;
  align-self: center;
}

.send-button:hover:not(:disabled) {
  background-color: #1e293b;
}

.typing-indicator {
  display: inline-block;
  margin-left: 4px;
  animation: blink 0.7s infinite;
}

@keyframes blink {
  0% { opacity: 0; }
  50% { opacity: 1; }
  100% { opacity: 0; }
}

.input-box:disabled,
.send-button:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.ai-answer {
  animation: fadeIn 0.3s ease-in-out;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(4px); }
  to { opacity: 1; transform: translateY(0); }
}

.ai-error {
  opacity: 0.8;
}

.ai-message + .ai-message {
  margin-top: 4px;
}

.ai-message + .ai-message .avatar {
  visibility: hidden;
}

.ai-message + .ai-message .message-bubble {
  border-top-left-radius: 10px;
}

@media (max-width: 768px) {
  .message {
    max-width: 95%;
  }

  .message-content {
    font-size: 15px;
  }

  .chat-input {
    padding: 12px;
  }

  .input-box {
    padding: 8px 12px;
  }

  .send-button {
    padding: 0 15px;
    font-size: 14px;
  }
}

@media (max-width: 480px) {
  .avatar {
    width: 32px;
    height: 32px;
  }

  .message-bubble {
    padding: 10px;
  }

  .message-content {
    font-size: 14px;
  }

  .chat-input-container {
    min-height: 64px;
  }
}

.story-choices {
  display: flex;
  flex-direction: column;
  gap: 6px;
  margin-top: 12px;
}

.choice-btn {
  display: block;
  width: 100%;
  padding: 10px 14px;
  border: 1px solid rgba(124, 58, 237, 0.25);
  border-radius: 10px;
  background: rgba(124, 58, 237, 0.06);
  color: #5b21b6;
  font-size: 14px;
  text-align: left;
  cursor: pointer;
  transition: all 0.2s ease;
}

.choice-btn:hover {
  background: rgba(124, 58, 237, 0.14);
  border-color: rgba(124, 58, 237, 0.5);
  transform: translateX(4px);
}

.choice-btn.custom-choice {
  border-color: rgba(148, 163, 184, 0.3);
  background: rgba(148, 163, 184, 0.06);
  color: #64748b;
  font-style: italic;
}

.choice-btn.custom-choice:hover {
  background: rgba(148, 163, 184, 0.14);
  border-color: rgba(148, 163, 184, 0.5);
}

.story-ending {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-top: 10px;
  padding: 10px 14px;
  background: linear-gradient(135deg, rgba(124, 58, 237, 0.08), rgba(167, 139, 250, 0.05));
  border-radius: 10px;
  border: 1px solid rgba(124, 58, 237, 0.15);
}

.ending-badge {
  font-size: 14px;
  font-weight: 700;
  color: #7c3aed;
}

.restart-btn {
  padding: 6px 14px;
  border: 0;
  border-radius: 8px;
  background: linear-gradient(135deg, #7c3aed, #6d28d9);
  color: #ffffff;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s ease;
}

.restart-btn:hover {
  background: linear-gradient(135deg, #8b5cf6, #7c3aed);
  transform: translateY(-1px);
}
</style>

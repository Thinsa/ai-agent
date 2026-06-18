<template>
  <div class="settings-page">
    <div class="header">
      <button class="back-button" type="button" @click="goBack">返回</button>
      <h1 class="title">背景设置</h1>
      <div class="header-actions" />
    </div>

    <div class="content">
      <div class="agent-tabs">
        <button
          v-for="agent in agents"
          :key="agent.key"
          type="button"
          class="agent-tab"
          :class="{ active: activeKey === agent.key }"
          :style="{ '--agent-color': agent.color }"
          @click="selectAgent(agent.key)"
        >
          {{ agent.name }}
        </button>
      </div>

      <div class="settings-panel">
        <div class="preview-section">
          <div class="preview-box" :style="previewStyle">
            <div class="preview-overlay" :class="{ 'has-bg': currentBg.url }">
              <div class="preview-msg-bubble">
                <span class="preview-msg-text">{{ currentBg.url ? '聊天消息会显示在这里...' : '上传背景图片后预览效果' }}</span>
              </div>
              <div v-if="!currentBg.url" class="preview-placeholder">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.4" stroke-linecap="round" stroke-linejoin="round">
                  <rect x="3" y="3" width="18" height="18" rx="2" ry="2" />
                  <circle cx="8.5" cy="8.5" r="1.5" />
                  <polyline points="21 15 16 10 5 21" />
                </svg>
                <span>未设置背景</span>
              </div>
            </div>
          </div>
          <p class="preview-hint" :class="{ set: currentBg.url }">{{ currentBg.url ? '✓ 已设置背景图片' : '点击下方按钮上传背景图片' }}</p>
        </div>

        <div class="control-group">
          <label class="control-label">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round" class="label-icon">
              <rect x="3" y="3" width="18" height="18" rx="2" ry="2" />
              <circle cx="8.5" cy="8.5" r="1.5" />
              <polyline points="21 15 16 10 5 21" />
            </svg>
            背景图片
          </label>
          <div class="image-controls">
            <label class="upload-btn" :class="{ disabled: uploading }">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round" class="btn-icon">
                <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4" />
                <polyline points="17 8 12 3 7 8" />
                <line x1="12" y1="3" x2="12" y2="15" />
              </svg>
              <input
                type="file"
                accept="image/*"
                class="file-input"
                :disabled="uploading"
                @change="handleFile"
              />
              <span>{{ uploading ? '上传中...' : '上传图片' }}</span>
            </label>
            <button
              v-if="currentBg.url"
              type="button"
              class="remove-btn"
              :disabled="uploading"
              @click="removeBg"
            >
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round" class="btn-icon">
                <polyline points="3 6 5 6 21 6" />
                <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2" />
              </svg>
              <span>移除背景</span>
            </button>
          </div>
          <p class="control-hint">支持 JPG、PNG、WebP，图片将安全存储至云端</p>
        </div>

        <div class="control-group">
          <label class="control-label">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round" class="label-icon">
              <circle cx="12" cy="12" r="4" />
              <path d="M12 2v2" />
              <path d="M12 20v2" />
              <path d="m4.93 4.93 1.41 1.41" />
              <path d="m17.66 17.66 1.41 1.41" />
              <path d="M2 12h2" />
              <path d="M20 12h2" />
              <path d="m6.34 17.66-1.41 1.41" />
              <path d="m19.07 4.93-1.41 1.41" />
            </svg>
            透明度
            <span class="opacity-value">{{ Math.round(currentBg.opacity * 100) }}%</span>
          </label>
          <input
            type="range"
            class="opacity-slider"
            min="5"
            max="80"
            :value="Math.round(currentBg.opacity * 100)"
            @input="handleOpacity($event.target.value / 100)"
          />
          <div class="slider-labels">
            <span>淡 (5%)</span>
            <span>浓 (80%)</span>
          </div>
        </div>

        <button type="button" class="save-btn" @click="goBack">
          返回首页
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useHead } from '@vueuse/head'
import { getBackground, refreshBackgrounds } from '../stores/bgStore'
import { saveBackground, updateBackgroundOpacity, deleteBackground } from '../api'

useHead({
  title: '背景设置 - LinkMind 灵桥'
})

const router = useRouter()

const agents = [
  { key: 'love', name: '知心 Soul', color: '#e91e63' },
  { key: 'chat', name: '灵语 Spark', color: '#7c3aed' },
  { key: 'super', name: '极智 Core', color: '#3f51b5' }
]

const activeKey = ref('love')
const currentOpacity = ref(0.15)
const uploading = ref(false)

const currentBg = computed(() => getBackground(activeKey.value))

const previewStyle = computed(() => {
  const bg = currentBg.value
  if (!bg.url) {
    return {
      background: 'linear-gradient(135deg, #e0e7ff, #f0e6ff)'
    }
  }
  return {
    backgroundImage: `url(${bg.url})`,
    backgroundSize: 'cover',
    backgroundPosition: 'center',
    opacity: bg.opacity || 0.15
  }
})

const selectAgent = key => {
  activeKey.value = key
  currentOpacity.value = getBackground(key).opacity
}

const handleFile = async event => {
  const file = event.target.files?.[0]
  if (!file) return

  if (!file.type.startsWith('image/')) {
    alert('请选择图片文件')
    event.target.value = ''
    return
  }

  uploading.value = true
  try {
    const formData = new FormData()
    formData.append('file', file)
    formData.append('opacity', currentOpacity.value)
    await saveBackground(activeKey.value, formData)
    await refreshBackgrounds()
  } catch (e) {
    const msg = e?.response?.data?.message || e?.response?.data?.error || e.message
    alert('上传失败：' + msg)
  } finally {
    uploading.value = false
  }
  event.target.value = ''
}

const handleOpacity = async value => {
  currentOpacity.value = value
  try {
    await updateBackgroundOpacity(activeKey.value, value)
    await refreshBackgrounds()
  } catch (e) {
    console.error('Update opacity failed:', e)
  }
}

const removeBg = async () => {
  try {
    await deleteBackground(activeKey.value)
    await refreshBackgrounds()
    currentOpacity.value = 0.15
  } catch (e) {
    console.error('Delete background failed:', e)
  }
}

const goBack = () => {
  router.push('/')
}
</script>

<style scoped>
.settings-page {
  display: flex;
  flex-direction: column;
  height: 100vh;
  background: #f5f6fb;
  overflow: hidden;
}

.header {
  display: grid;
  flex: 0 0 auto;
  grid-template-columns: 1fr auto 1fr;
  align-items: center;
  padding: 12px 18px;
  background: linear-gradient(135deg, #1e293b, #334155);
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
  font-size: 20px;
  font-weight: bold;
}

.header-actions {
  display: flex;
  justify-self: end;
}

.content {
  flex: 1;
  overflow-y: auto;
  padding: 24px;
  max-width: 600px;
  margin: 0 auto;
  width: 100%;
  box-sizing: border-box;
}

.agent-tabs {
  display: flex;
  gap: 8px;
  margin-bottom: 28px;
  background: #ffffff;
  border-radius: 14px;
  padding: 6px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}

.agent-tab {
  flex: 1;
  min-height: 44px;
  padding: 10px 8px;
  border: 0;
  border-radius: 10px;
  background: transparent;
  color: #64748b;
  cursor: pointer;
  font-size: 14px;
  font-weight: 600;
  transition: all 0.25s ease;
}

.agent-tab.active {
  background: var(--agent-color);
  color: #ffffff;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.settings-panel {
  background: #ffffff;
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 2px 16px rgba(0, 0, 0, 0.06);
}

.preview-section {
  margin-bottom: 24px;
}

.preview-box {
  height: 160px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  overflow: hidden;
  border: 2px dashed rgba(0, 0, 0, 0.12);
  background: #f8fafc;
}

.preview-overlay {
  position: relative;
  z-index: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
}

.preview-overlay.has-bg {
  width: 100%;
  height: 100%;
  justify-content: flex-end;
  padding-bottom: 16px;
  background: linear-gradient(to top, rgba(0,0,0,0.3) 0%, transparent 60%);
}

.preview-msg-bubble {
  padding: 10px 16px;
  background: rgba(255, 255, 255, 0.92);
  border-radius: 14px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.08);
  max-width: 80%;
}

.preview-msg-text {
  font-size: 13px;
  color: #475569;
  line-height: 1.5;
}

.preview-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  color: #94a3b8;
}

.preview-placeholder svg {
  width: 36px;
  height: 36px;
  opacity: 0.5;
}

.preview-placeholder span {
  font-size: 13px;
}

.preview-hint {
  text-align: center;
  margin-top: 8px;
  font-size: 13px;
  font-weight: 500;
  color: #94a3b8;
}

.preview-hint.set {
  color: #059669;
}

.control-group {
  margin-bottom: 22px;
}

.control-label {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  font-weight: 700;
  color: #1e293b;
  margin-bottom: 12px;
}

.label-icon {
  width: 18px;
  height: 18px;
  color: #64748b;
  flex-shrink: 0;
}

.opacity-value {
  font-weight: 600;
  color: #7c3aed;
  margin-left: auto;
  font-size: 14px;
}

.image-controls {
  display: flex;
  gap: 10px;
  align-items: center;
  flex-wrap: wrap;
}

.upload-btn {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 11px 20px;
  border-radius: 12px;
  background: linear-gradient(135deg, #7c3aed, #6d28d9);
  color: #ffffff;
  font-size: 14px;
  font-weight: 700;
  cursor: pointer;
  transition: all 0.25s ease;
  border: 0;
  box-shadow: 0 2px 12px rgba(124, 58, 237, 0.25);
}

.upload-btn:hover {
  background: linear-gradient(135deg, #8b5cf6, #7c3aed);
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(124, 58, 237, 0.35);
}

.upload-btn:active {
  transform: translateY(0);
}

.upload-btn.disabled {
  cursor: not-allowed;
  opacity: 0.7;
  transform: none;
}

.file-input {
  display: none;
}

.remove-btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 11px 20px;
  border-radius: 12px;
  border: 1px solid rgba(220, 38, 38, 0.25);
  background: #ffffff;
  color: #dc2626;
  cursor: pointer;
  font-size: 14px;
  font-weight: 600;
  transition: all 0.2s ease;
}

.remove-btn:hover:not(:disabled) {
  background: #fef2f2;
  border-color: #dc2626;
}

.remove-btn:disabled {
  cursor: not-allowed;
  opacity: 0.5;
}

.btn-icon {
  width: 16px;
  height: 16px;
  flex-shrink: 0;
}

.control-hint {
  margin-top: 8px;
  font-size: 12px;
  color: #94a3b8;
  line-height: 1.5;
}

.opacity-slider {
  width: 100%;
  height: 6px;
  -webkit-appearance: none;
  appearance: none;
  border-radius: 3px;
  background: linear-gradient(90deg, #e2e8f0 0%, #334155 100%);
  outline: none;
  cursor: pointer;
}

.opacity-slider::-webkit-slider-thumb {
  -webkit-appearance: none;
  width: 22px;
  height: 22px;
  border-radius: 50%;
  background: #ffffff;
  border: 3px solid #334155;
  cursor: pointer;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.15);
}

.slider-labels {
  display: flex;
  justify-content: space-between;
  font-size: 11px;
  color: #94a3b8;
  margin-top: 4px;
}

.save-btn {
  width: 100%;
  padding: 14px;
  border: 1px solid rgba(0, 0, 0, 0.08);
  border-radius: 12px;
  background: #f1f5f9;
  color: #475569;
  font-size: 15px;
  font-weight: 700;
  cursor: pointer;
  transition: all 0.2s ease;
  margin-top: 8px;
}

.save-btn:hover {
  background: #e2e8f0;
  color: #334155;
}

@media (max-width: 480px) {
  .content { padding: 16px; }
  .settings-panel { padding: 18px; }
  .agent-tab { font-size: 12px; }
}
</style>

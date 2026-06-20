<template>
  <div class="settings-page">
    <PageHeader title="背景设置" back-to="/" />

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

        <button type="button" class="save-btn" @click="$router.push('/')">
          返回首页
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, ref } from 'vue'
import { useHead } from '@vueuse/head'
import PageHeader from '../components/PageHeader.vue'
import { getBackground, refreshBackgrounds } from '../stores/bgStore'
import { saveBackground, updateBackgroundOpacity, deleteBackground } from '../api'

useHead({
  title: '背景设置 - LinkMind 灵桥'
})

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
</script>

<style scoped>
.settings-page {
  display: flex; flex-direction: column; height: 100vh;
  background: var(--color-base-0); overflow: hidden;
}

.content {
  flex: 1; overflow-y: auto; padding: 24px;
  max-width: 600px; margin: 0 auto; width: 100%; box-sizing: border-box;
}

.agent-tabs {
  display: flex; gap: 8px; margin-bottom: 28px;
  background: var(--glass-card);
  backdrop-filter: blur(var(--blur-card));
  border: var(--border-subtle);
  border-radius: var(--radius-lg); padding: 6px;
}
.agent-tab {
  flex: 1; min-height: 44px; padding: 10px 8px;
  border: 0; border-radius: var(--radius-sm);
  background: transparent; color: var(--color-text-2);
  cursor: pointer; font-size: 14px; font-weight: 600;
  transition: all var(--duration-fast) var(--ease-out);
}
.agent-tab.active {
  background: var(--gradient-spark);
  color: #fff;
  box-shadow: var(--shadow-card);
}

.settings-panel {
  background: var(--glass-card);
  backdrop-filter: blur(var(--blur-card));
  border: var(--border-subtle);
  border-radius: var(--radius-xl); padding: 24px;
  box-shadow: var(--shadow-card);
}
.preview-section { margin-bottom: 24px; }
.preview-box {
  height: 160px; border-radius: var(--radius-lg);
  display: flex; align-items: center; justify-content: center;
  position: relative; overflow: hidden;
  border: 1px dashed rgba(255,255,255,0.10);
  background: var(--color-base-1);
}
.preview-overlay {
  position: relative; z-index: 1;
  display: flex; flex-direction: column; align-items: center; gap: 10px;
}
.preview-overlay.has-bg {
  width: 100%; height: 100%; justify-content: flex-end; padding-bottom: 16px;
  background: linear-gradient(to top, rgba(0,0,0,0.4), transparent 60%);
}
.preview-msg-bubble {
  padding: 10px 16px; background: var(--glass-card);
  backdrop-filter: blur(var(--blur-card));
  border: var(--border-subtle);
  border-radius: var(--radius-lg); max-width: 80%;
}
.preview-msg-text { font-size: 13px; color: var(--color-text-1); line-height: 1.5; }
.preview-placeholder { display: flex; flex-direction: column; align-items: center; gap: 8px; color: var(--color-text-2); }
.preview-placeholder svg { width: 36px; height: 36px; opacity: 0.4; }
.preview-placeholder span { font-size: 13px; }
.preview-hint { text-align: center; margin-top: 8px; font-size: 13px; font-weight: 500; color: var(--color-text-2); }
.preview-hint.set { color: #7ec8a0; }

.control-group { margin-bottom: 22px; }
.control-label {
  display: flex; align-items: center; gap: 8px;
  font-size: 14px; font-weight: 700; color: var(--color-text-1); margin-bottom: 12px;
}
.label-icon { width: 18px; height: 18px; color: var(--color-text-2); flex-shrink: 0; }
.opacity-value { font-weight: 600; color: var(--color-glow); margin-left: auto; font-size: 14px; }

.image-controls { display: flex; gap: 10px; align-items: center; flex-wrap: wrap; }
.upload-btn {
  display: inline-flex; align-items: center; gap: 8px;
  padding: 11px 20px; border-radius: var(--radius-md);
  background: var(--gradient-spark); color: #fff;
  font-size: 14px; font-weight: 700; cursor: pointer;
  transition: all var(--duration-fast) var(--ease-out);
  border: 0; box-shadow: 0 2px 12px rgba(180,160,232,0.20);
}
.upload-btn:hover { filter: brightness(1.1); transform: translateY(-2px); box-shadow: 0 6px 20px rgba(180,160,232,0.30); }
.upload-btn:active { transform: translateY(0); }
.upload-btn.disabled { cursor: not-allowed; opacity: 0.6; transform: none; }
.file-input { display: none; }
.remove-btn {
  display: inline-flex; align-items: center; gap: 6px;
  padding: 11px 20px; border-radius: var(--radius-md);
  border: 1px solid rgba(240,160,160,0.22);
  background: transparent; color: #f0a0a0;
  cursor: pointer; font-size: 14px; font-weight: 600;
  transition: all var(--duration-fast) var(--ease-out);
}
.remove-btn:hover:not(:disabled) { background: rgba(240,160,160,0.08); border-color: rgba(240,160,160,0.45); }
.remove-btn:disabled { cursor: not-allowed; opacity: 0.4; }
.btn-icon { width: 16px; height: 16px; flex-shrink: 0; }
.control-hint { margin-top: 8px; font-size: 12px; color: var(--color-text-3); line-height: 1.5; }

.opacity-slider {
  width: 100%; height: 6px; -webkit-appearance: none; appearance: none;
  border-radius: 3px;
  background: linear-gradient(90deg, var(--color-base-3), var(--color-glow));
  outline: none; cursor: pointer;
}
.opacity-slider::-webkit-slider-thumb {
  -webkit-appearance: none; width: 22px; height: 22px; border-radius: 50%;
  background: var(--color-base-1); border: 2px solid var(--color-glow);
  cursor: pointer; box-shadow: var(--shadow-card);
}
.slider-labels { display: flex; justify-content: space-between; font-size: 11px; color: var(--color-text-3); margin-top: 4px; }

.save-btn {
  width: 100%; padding: 14px; border: var(--border-subtle);
  border-radius: var(--radius-md);
  background: var(--glass-card); color: var(--color-text-1);
  font-size: 15px; font-weight: 700; cursor: pointer;
  transition: all var(--duration-fast) var(--ease-out);
  margin-top: 8px;
}
.save-btn:hover { background: rgba(255,255,255,0.06); color: var(--color-glow); }

@media (max-width: 480px) {
  .content { padding: 16px; }
  .settings-panel { padding: 18px; }
  .agent-tab { font-size: 12px; }
}
</style>

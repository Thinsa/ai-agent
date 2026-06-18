<template>
  <aside class="settings-panel" :class="[theme]">
    <div class="settings-scroll">
      <section class="panel-block command-block">
        <div class="panel-header">
          <div>
            <h2>指令</h2>
            <p>提示词</p>
          </div>
          <button class="close-button" type="button" title="关闭设置" @click="$emit('toggle-open')">
            收起
          </button>
        </div>

        <label class="prompt-editor">
          <div class="prompt-toolbar">
            <span>可引入1项变量（可通过点击添加）：</span>
            <button class="variable-chip" type="button" @click="appendDocumentsVariable">${documents}</button>
          </div>
          <textarea
            :value="customPrompt"
            :placeholder="promptPlaceholder"
            maxlength="30720"
            @input="$emit('update:customPrompt', $event.target.value)"
          />
          <span class="char-count">{{ customPrompt.length }} / 30720</span>
        </label>
      </section>

      <section class="panel-block">
        <div class="toggle-row">
          <div class="row-label">
            <span>视觉</span>
            <small>暂未启用</small>
          </div>
          <button class="switch disabled" type="button" disabled aria-label="视觉暂不可用"></button>
        </div>
      </section>

      <section class="panel-block">
        <div class="group-title">
          <span>知识</span>
          <span class="group-actions">
            <span v-if="showKnowledge" class="group-count">{{ knowledgeEnabled ? '1' : '0' }}/5</span>
            <button v-if="showKnowledge" type="button" class="text-action">配置</button>
          </span>
        </div>

        <div v-if="showKnowledge" class="ability-row">
          <div class="ability-main">
            <span class="ability-icon" aria-hidden="true">
              <svg viewBox="0 0 24 24">
                <path d="M4 8.5h16v11H4z" />
                <path d="M7 5h10v3.5H7z" />
                <path d="M8 12h8" />
                <path d="M8 15.5h5" />
              </svg>
            </span>
            <div>
              <span class="ability-name">恋爱大师</span>
              <span class="ability-meta">知识库检索</span>
            </div>
          </div>
          <button
            class="switch"
            type="button"
            :class="{ active: knowledgeEnabled }"
            :aria-label="knowledgeEnabled ? '关闭知识库' : '开启知识库'"
            @click="$emit('update:knowledgeEnabled', !knowledgeEnabled)"
          ></button>
        </div>

        <div v-if="showWebSearch" class="toggle-row">
          <div class="row-label">
            <span>联网搜索</span>
            <small>需要最新信息时启用</small>
          </div>
          <button
            class="switch"
            type="button"
            :class="{ active: webSearchEnabled }"
            :aria-label="webSearchEnabled ? '关闭联网搜索' : '开启联网搜索'"
            @click="$emit('update:webSearchEnabled', !webSearchEnabled)"
          ></button>
        </div>

        <div class="toggle-row muted">
          <div class="row-label">
            <span>样例库</span>
            <small>暂未启用</small>
          </div>
          <button class="switch disabled" type="button" disabled aria-label="样例库暂不可用"></button>
        </div>
      </section>

      <section class="panel-block">
        <div class="group-title">
          <span>技能</span>
          <span v-if="showMcp" class="group-count">{{ mcpEnabled ? '1' : '0' }}/5</span>
        </div>

        <div v-if="showMcp" class="ability-row">
          <div class="ability-main">
            <span class="ability-icon" aria-hidden="true">
              <svg viewBox="0 0 24 24">
                <path d="M13 2 4 14h7l-1 8 10-13h-7z" />
              </svg>
            </span>
            <div>
              <span class="ability-name">MCP 服务</span>
              <span class="ability-meta">{{ mcpEnabled ? '已启用' : '未启用' }}</span>
            </div>
          </div>
          <button
            class="switch"
            type="button"
            :class="{ active: mcpEnabled }"
            :aria-label="mcpEnabled ? '关闭 MCP 模式' : '开启 MCP 模式'"
            @click="$emit('update:mcpEnabled', !mcpEnabled)"
          ></button>
        </div>
      </section>
    </div>
  </aside>
</template>

<script setup>
const props = defineProps({
  theme: {
    type: String,
    default: 'super'
  },
  showWebSearch: {
    type: Boolean,
    default: true
  },
  showMcp: {
    type: Boolean,
    default: true
  },
  showKnowledge: {
    type: Boolean,
    default: false
  },
  webSearchEnabled: {
    type: Boolean,
    default: false
  },
  mcpEnabled: {
    type: Boolean,
    default: false
  },
  knowledgeEnabled: {
    type: Boolean,
    default: false
  },
  customPrompt: {
    type: String,
    default: ''
  },
  promptPlaceholder: {
    type: String,
    default: '你可以在这里添加本轮对话的额外要求。'
  }
})

const emit = defineEmits([
  'toggle-open',
  'update:webSearchEnabled',
  'update:mcpEnabled',
  'update:knowledgeEnabled',
  'update:customPrompt'
])

const appendDocumentsVariable = () => {
  if (props.customPrompt.includes('${documents}')) {
    return
  }
  const nextPrompt = props.customPrompt
    ? `${props.customPrompt.trimEnd()}\n${'${documents}'}`
    : '${documents}'
  emit('update:customPrompt', nextPrompt)
}
</script>

<style scoped>
.settings-panel {
  flex: 0 0 calc(50% - 8px);
  min-width: 0;
  height: 100%;
  min-height: 0;
  overflow: hidden;
  border-right: 1px solid #e4e7f0;
  background: #fbfcff;
  color: #323653;
}

.settings-scroll {
  height: 100%;
  overflow-y: auto;
  padding: 18px 28px 28px;
}

.panel-block {
  padding: 20px 0;
  border-bottom: 1px solid #eceff7;
}

.panel-block:first-child {
  padding-top: 0;
}

.panel-header,
.group-title,
.toggle-row,
.ability-row,
.ability-main,
.group-actions {
  display: flex;
  align-items: center;
}

.panel-header,
.group-title,
.toggle-row,
.ability-row {
  justify-content: space-between;
  gap: 12px;
}

.panel-header h2 {
  margin: 0;
  color: #393f62;
  font-size: 19px;
  font-weight: 800;
}

.panel-header p {
  margin: 18px 0 8px;
  color: #59607d;
  font-size: 15px;
  font-weight: 800;
}

.close-button,
.text-action {
  border: 0;
  background: transparent;
  color: #6d63ff;
  cursor: pointer;
  font-weight: 700;
}

.close-button {
  min-height: 36px;
  border-radius: 6px;
  padding: 0 10px;
  background: #f0f1ff;
}

.prompt-editor {
  position: relative;
  display: block;
  border: 1px solid #d8ddeb;
  border-radius: 7px;
  background: #ffffff;
  overflow: hidden;
  box-shadow: 0 1px 2px rgba(15, 23, 42, 0.04);
}

.prompt-toolbar {
  display: flex;
  align-items: center;
  gap: 8px;
  min-height: 44px;
  padding: 0 14px;
  border-bottom: 1px solid #edf0f7;
  color: #6b728b;
  font-size: 14px;
}

.variable-chip {
  border: 0;
  border-radius: 5px;
  padding: 5px 10px;
  background: #f0f1f8;
  color: #7669ff;
  cursor: pointer;
  font-weight: 800;
}

.prompt-editor textarea {
  width: 100%;
  min-height: 176px;
  resize: vertical;
  border: 0;
  padding: 18px 18px 38px;
  color: #343b5f;
  font-size: 15px;
  line-height: 1.7;
  outline: none;
}

.prompt-editor textarea::placeholder {
  color: #8d94ac;
}

.char-count {
  position: absolute;
  right: 14px;
  bottom: 10px;
  color: #c3c7d5;
  font-size: 13px;
}

.group-title {
  margin-bottom: 14px;
  color: #343b5f;
  font-size: 16px;
  font-weight: 800;
}

.group-actions {
  gap: 18px;
}

.group-count {
  color: #9aa1b6;
  font-size: 13px;
  font-weight: 700;
}

.toggle-row {
  min-height: 46px;
}

.toggle-row + .toggle-row,
.ability-row + .toggle-row,
.toggle-row + .ability-row,
.ability-row + .ability-row {
  margin-top: 10px;
}

.row-label span,
.row-label small,
.ability-name,
.ability-meta {
  display: block;
}

.row-label span {
  color: #4f5676;
  font-size: 15px;
  font-weight: 800;
}

.row-label small,
.ability-meta {
  margin-top: 3px;
  color: #9ca3b8;
  font-size: 12px;
}

.muted .row-label span {
  color: #8a91aa;
}

.ability-row {
  min-height: 52px;
  padding: 0 12px;
  border-radius: 7px;
  background: #f8f9fc;
}

.ability-main {
  min-width: 0;
  gap: 10px;
}

.ability-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 24px;
  height: 24px;
  color: #686f90;
}

.ability-icon svg {
  width: 20px;
  height: 20px;
  fill: none;
  stroke: currentColor;
  stroke-linecap: round;
  stroke-linejoin: round;
  stroke-width: 1.8;
}

.ability-name {
  color: #343b5f;
  font-size: 15px;
  font-weight: 800;
}

.switch {
  position: relative;
  flex: 0 0 auto;
  width: 38px;
  height: 22px;
  border: 0;
  border-radius: 999px;
  background: #d5d8e3;
  cursor: pointer;
  transition: background-color 0.2s ease;
}

.switch::after {
  content: '';
  position: absolute;
  top: 3px;
  left: 3px;
  width: 16px;
  height: 16px;
  border-radius: 50%;
  background: #ffffff;
  box-shadow: 0 2px 6px rgba(28, 35, 70, 0.2);
  transition: transform 0.2s ease;
}

.switch.active {
  background: #7669ff;
}

.switch.active::after {
  transform: translateX(16px);
}

.switch.disabled {
  cursor: not-allowed;
  opacity: 0.78;
}

@media (max-width: 768px) {
  .settings-panel {
    flex: 0 0 auto;
    width: 100%;
    height: auto;
    max-height: 420px;
  }

  .settings-scroll {
    padding: 14px;
  }
}
</style>

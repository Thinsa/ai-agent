<template>
  <aside class="guide-panel">
    <div class="guide-scroll">
      <section class="guide-block">
        <div class="panel-header">
          <div>
            <h2>灵桥引导</h2>
            <p>作弊模式</p>
          </div>
          <button class="close-button" type="button" title="关闭引导" @click="$emit('toggle-open')">
            收起
          </button>
        </div>

        <div class="toggle-row">
          <div class="row-label">
            <span>结局引导</span>
            <small>{{ enabled ? 'Spark 会提示下一步' : '关闭后不再提示路线' }}</small>
          </div>
          <button
            class="switch"
            type="button"
            :class="{ active: enabled }"
            :aria-label="enabled ? '关闭结局引导' : '开启结局引导'"
            @click="$emit('update:enabled', !enabled)"
          ></button>
        </div>
      </section>

      <section class="guide-block">
        <div class="group-title">目标结局</div>
        <div class="ending-list">
          <button
            v-for="ending in endings"
            :key="ending.id"
            type="button"
            class="ending-option"
            :class="{ active: ending.id === targetEndingId }"
            @click="$emit('update:targetEndingId', ending.id)"
          >
            <span class="ending-name">{{ ending.title }}</span>
            <span class="ending-type">{{ endingTypeLabel(ending.type) }}</span>
          </button>
        </div>
      </section>

      <section class="guide-block">
        <div class="group-title">当前状态</div>
        <div class="status-box" :class="[status.state]">
          <span class="status-title">{{ statusTitle }}</span>
          <span class="status-text">{{ statusText }}</span>
        </div>
      </section>
    </div>
  </aside>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  enabled: {
    type: Boolean,
    default: false
  },
  endings: {
    type: Array,
    default: () => []
  },
  targetEndingId: {
    type: String,
    default: ''
  },
  status: {
    type: Object,
    default: () => ({ state: 'not_started' })
  }
})

defineEmits(['toggle-open', 'update:enabled', 'update:targetEndingId'])

const selectedEnding = computed(() => {
  return props.endings.find(ending => ending.id === props.targetEndingId)
})

const statusTitle = computed(() => {
  if (!props.enabled) return '未启用'
  if (props.status.state === 'not_started') return '等待开局'
  if (props.status.state === 'no_target') return '未选择目标'
  if (props.status.state === 'unreachable') return '当前不可达'
  if (props.status.state === 'completed') return '已抵达'
  return '可抵达'
})

const statusText = computed(() => {
  if (!props.enabled) return '开启后，Spark 会根据目标结局提示下一步。'
  if (props.status.state === 'not_started') return '输入「开始」后即可计算第一步。'
  if (props.status.state === 'no_target') return '请选择一个目标结局。'
  if (props.status.state === 'unreachable') return `从当前分支已无法抵达「${selectedEnding.value?.title || '目标结局'}」。`
  if (props.status.state === 'completed') return `你已经抵达「${selectedEnding.value?.title || '目标结局'}」。`
  const recommendation = props.status.recommendation
  return recommendation?.choiceText ? `建议下一步：${recommendation.choiceText}` : 'Spark 正在观察桥灯的方向。'
})

const endingTypeLabel = type => {
  const labels = {
    waiting: '等待',
    good: '好结局',
    bittersweet: '中性',
    true: '真结局'
  }
  return labels[type] || type
}
</script>

<style scoped>
.guide-panel {
  flex: 0 0 50%;
  min-width: 0;
  height: 100%;
  min-height: 0;
  overflow: hidden;
  border-right: 1px solid #e4e7f0;
  border-left: 1px solid #eef1f7;
  background: #fbfcff;
  color: #323653;
}

.guide-scroll {
  height: 100%;
  overflow-y: auto;
  padding: 18px 28px 28px;
}

.guide-block {
  padding: 20px 0;
  border-bottom: 1px solid #eceff7;
}

.guide-block:first-child {
  padding-top: 0;
}

.panel-header,
.toggle-row {
  display: flex;
  align-items: center;
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

.close-button {
  min-height: 36px;
  border: 0;
  border-radius: 6px;
  padding: 0 10px;
  background: #f0f1ff;
  color: #6d63ff;
  cursor: pointer;
  font-weight: 700;
}

.row-label span,
.row-label small {
  display: block;
}

.row-label span {
  color: #4f5676;
  font-size: 15px;
  font-weight: 800;
}

.row-label small {
  margin-top: 3px;
  color: #9ca3b8;
  font-size: 12px;
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
  background: #7c3aed;
}

.switch.active::after {
  transform: translateX(16px);
}

.group-title {
  margin-bottom: 14px;
  color: #343b5f;
  font-size: 16px;
  font-weight: 800;
}

.ending-list {
  display: grid;
  gap: 10px;
}

.ending-option {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  min-height: 50px;
  border: 1px solid #e4e7f0;
  border-radius: 8px;
  padding: 0 12px;
  background: #ffffff;
  color: #343b5f;
  cursor: pointer;
  text-align: left;
  transition: background-color 0.2s ease, border-color 0.2s ease;
}

.ending-option:hover,
.ending-option.active {
  border-color: rgba(124, 58, 237, 0.55);
  background: rgba(124, 58, 237, 0.08);
}

.ending-name {
  min-width: 0;
  font-size: 14px;
  font-weight: 800;
}

.ending-type {
  flex: 0 0 auto;
  border-radius: 999px;
  padding: 4px 8px;
  background: rgba(124, 58, 237, 0.1);
  color: #6d28d9;
  font-size: 12px;
  font-weight: 800;
}

.status-box {
  display: grid;
  gap: 6px;
  border: 1px solid #e4e7f0;
  border-radius: 8px;
  padding: 14px;
  background: #ffffff;
}

.status-box.reachable {
  border-color: rgba(5, 150, 105, 0.35);
  background: #ecfdf5;
}

.status-box.unreachable {
  border-color: rgba(220, 38, 38, 0.28);
  background: #fef2f2;
}

.status-title {
  color: #343b5f;
  font-size: 15px;
  font-weight: 800;
}

.status-text {
  color: #59607d;
  font-size: 13px;
  line-height: 1.5;
}

@media (max-width: 768px) {
  .guide-panel {
    flex: 0 0 auto;
    width: 100%;
    height: auto;
    max-height: 420px;
  }

  .guide-scroll {
    padding: 14px;
  }
}
</style>

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

        <ToggleSwitch
          :model-value="enabled"
          @update:model-value="$emit('update:enabled', $event)"
          label="结局引导"
          :description="enabled ? 'Spark 会提示下一步' : '关闭后不再提示路线'"
        />
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
import ToggleSwitch from './ToggleSwitch.vue'

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
  flex: 0 0 50%; min-width: 0; height: 100%; min-height: 0; overflow: hidden;
  background: var(--glass-card);
  backdrop-filter: blur(var(--blur-card));
  border-right: var(--border-subtle); border-left: var(--border-subtle);
  color: var(--color-text-1);
}
.guide-scroll { height: 100%; overflow-y: auto; padding: 18px 28px 28px; }
.guide-block { padding: 20px 0; border-bottom: var(--border-subtle); }
.guide-block:first-child { padding-top: 0; }

.panel-header, .toggle-row { display: flex; align-items: center; justify-content: space-between; gap: 12px; }
.panel-header h2 { margin: 0; color: var(--color-text-1); font-size: 19px; font-weight: 800; }
.panel-header p { margin: 18px 0 8px; color: var(--color-text-2); font-size: 15px; font-weight: 800; }
.close-button {
  min-height: 36px; border: 0; border-radius: var(--radius-sm);
  padding: 0 10px; background: rgba(180,160,232,0.10); color: var(--color-aurora-2);
  cursor: pointer; font-weight: 700;
}

.row-label span, .row-label small { display: block; }
.row-label span { color: var(--color-text-1); font-size: 15px; font-weight: 800; }
.row-label small { margin-top: 3px; color: var(--color-text-2); font-size: 12px; }

.group-title { margin-bottom: 14px; color: var(--color-text-1); font-size: 16px; font-weight: 800; }
.ending-list { display: grid; gap: 10px; }

.ending-option {
  display: flex; align-items: center; justify-content: space-between; gap: 10px;
  min-height: 50px; border: var(--border-subtle); border-radius: var(--radius-sm);
  padding: 0 12px; background: var(--color-base-1); color: var(--color-text-1);
  cursor: pointer; text-align: left;
  transition: background var(--duration-fast) var(--ease-out),
              border-color var(--duration-fast) var(--ease-out);
}
.ending-option:hover, .ending-option.active {
  border-color: rgba(180,160,232,0.40);
  background: rgba(180,160,232,0.08);
}
.ending-name { min-width: 0; font-size: 14px; font-weight: 800; }
.ending-type {
  flex: 0 0 auto; border-radius: var(--radius-full); padding: 4px 8px;
  background: rgba(180,160,232,0.10); color: var(--color-aurora-2);
  font-size: 12px; font-weight: 800;
}

.status-box {
  display: grid; gap: 6px;
  border: var(--border-subtle); border-radius: var(--radius-sm);
  padding: 14px; background: var(--color-base-1);
}
.status-box.reachable {
  border-color: rgba(126,200,160,0.30);
  background: rgba(126,200,160,0.06);
}
.status-box.unreachable {
  border-color: rgba(240,160,160,0.25);
  background: rgba(240,160,160,0.06);
}
.status-title { color: var(--color-text-1); font-size: 15px; font-weight: 800; }
.status-text { color: var(--color-text-2); font-size: 13px; line-height: 1.5; }

@media (max-width: 768px) {
  .guide-panel { flex: 0 0 auto; width: 100%; height: auto; max-height: 420px; }
  .guide-scroll { padding: 14px; }
}
</style>

<template>
  <label v-if="label" class="toggle-row">
    <div class="row-label">
      <span>{{ label }}</span>
      <small v-if="description">{{ description }}</small>
    </div>
    <button
      class="switch"
      type="button"
      :class="{ active: modelValue, disabled }"
      :aria-label="ariaLabel"
      :disabled="disabled"
      @click="toggle"
    ></button>
  </label>
  <button
    v-else
    class="switch"
    type="button"
    :class="{ active: modelValue, disabled }"
    :aria-label="ariaLabel"
    :disabled="disabled"
    @click="toggle"
  ></button>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  },
  label: {
    type: String,
    default: ''
  },
  description: {
    type: String,
    default: ''
  },
  color: {
    type: String,
    default: 'var(--color-glow)'
  },
  disabled: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['update:modelValue'])

const ariaLabel = computed(() => {
  return props.modelValue
    ? props.label ? `关闭 ${props.label}` : '关闭'
    : props.label ? `开启 ${props.label}` : '开启'
})

const toggle = () => {
  if (!props.disabled) {
    emit('update:modelValue', !props.modelValue)
  }
}
</script>

<style scoped>
.switch {
  position: relative;
  flex: 0 0 auto;
  width: 38px;
  height: 22px;
  border: 0;
  border-radius: var(--radius-full);
  background: var(--color-base-4);
  cursor: pointer;
  transition: background-color var(--duration-fast) var(--ease-out);
}
.switch::after {
  content: '';
  position: absolute;
  top: 3px;
  left: 3px;
  width: 16px;
  height: 16px;
  border-radius: 50%;
  background: var(--color-base-1);
  box-shadow: 0 2px 6px rgba(0,0,0,0.3);
  transition: transform var(--duration-fast) var(--ease-out);
}
.switch.active {
  background: v-bind(color);
}
.switch.active::after {
  transform: translateX(16px);
  background: var(--color-base-0);
}
.switch.disabled {
  cursor: not-allowed;
  opacity: 0.45;
}

.toggle-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  min-height: 46px;
  cursor: pointer;
}

.row-label span,
.row-label small {
  display: block;
}
.row-label span {
  color: var(--color-text-1);
  font-size: 15px;
  font-weight: 800;
}
.row-label small {
  margin-top: 3px;
  color: var(--color-text-2);
  font-size: 12px;
}
</style>

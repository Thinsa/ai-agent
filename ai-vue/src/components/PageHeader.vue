<template>
  <header class="page-header">
    <button v-if="showBack" class="back-button" type="button" @click="handleBack">
      <span class="back-text">返回</span>
    </button>
    <div v-else class="back-placeholder"></div>

    <h1 v-if="!$slots.default" class="title">{{ title }}</h1>
    <div v-else class="title title-slot">
      <slot></slot>
    </div>

    <div class="header-actions">
      <slot name="actions"></slot>
    </div>
  </header>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'

const props = defineProps({
  title: {
    type: String,
    default: ''
  },
  showBack: {
    type: Boolean,
    default: true
  },
  backTo: {
    type: String,
    default: '/'
  },
  gradient: {
    type: String,
    default: 'var(--gradient-spark)'
  }
})

const router = useRouter()

const handleBack = () => {
  router.push(props.backTo)
}
</script>

<style scoped>
.page-header {
  display: grid;
  flex: 0 0 auto;
  grid-template-columns: 1fr auto 1fr;
  align-items: center;
  padding: 12px 18px;
  background: var(--glass-card);
  backdrop-filter: blur(var(--blur-header));
  border-bottom: var(--border-subtle);
  color: var(--color-text-1);
  z-index: 10;
}

.back-button {
  display: inline-flex;
  align-items: center;
  justify-self: start;
  border: 0;
  background: transparent;
  color: var(--color-text-2);
  cursor: pointer;
  font-size: 16px;
  font-family: var(--font-body);
  transition: color var(--duration-fast) var(--ease-out);
  padding: 0;
}
.back-button:hover {
  color: var(--color-glow);
}
.back-text::before {
  content: '<';
  margin-right: 8px;
}

.back-placeholder {
  justify-self: start;
}

.title {
  justify-self: center;
  margin: 0;
  text-align: center;
  font-size: 20px;
  font-weight: bold;
  font-family: var(--font-display);
  background: v-bind(gradient);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.title-slot {
  display: flex;
  align-items: center;
  justify-content: center;
}

.header-actions {
  display: flex;
  align-items: center;
  justify-self: end;
  gap: 12px;
  min-width: 0;
}

@media (max-width: 768px) {
  .page-header {
    padding: 12px 16px;
  }
  .title {
    font-size: 18px;
  }
}

@media (max-width: 480px) {
  .page-header {
    padding: 10px 12px;
  }
  .back-button {
    font-size: 14px;
  }
  .title {
    font-size: 16px;
  }
}
</style>

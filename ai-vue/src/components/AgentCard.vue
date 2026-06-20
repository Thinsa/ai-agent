<template>
  <article
    class="agent-panel"
    :class="themeClass"
    :role="to ? 'button' : undefined"
    :tabindex="to ? 0 : undefined"
    @click="handleClick"
    @keydown.enter="handleClick"
    @keydown.space.prevent="handleClick"
  >
    <div class="panel-aura"></div>

    <div class="panel-icon">
      <slot name="icon">
        <!-- 默认占位 SVG -->
        <svg viewBox="0 0 48 48" fill="none">
          <circle cx="24" cy="24" r="20" stroke="currentColor" stroke-width="2" opacity="0.5"/>
        </svg>
      </slot>
    </div>

    <div class="panel-body">
      <div class="panel-name">{{ title }}</div>
      <div class="panel-tagline">{{ tagline }}</div>
      <div class="panel-desc">{{ description }}</div>
      <div class="panel-action">
        <span>{{ actionText }}</span>
        <svg viewBox="0 0 16 16" fill="none" stroke="currentColor" stroke-width="1.5">
          <path d="M4 8h8M9 5l3 3-3 3"/>
        </svg>
      </div>
    </div>
  </article>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'

const props = defineProps({
  title: {
    type: String,
    required: true
  },
  tagline: {
    type: String,
    default: ''
  },
  description: {
    type: String,
    default: ''
  },
  actionText: {
    type: String,
    default: '进入'
  },
  theme: {
    type: String,
    default: 'core',
    validator: (v) => ['soul', 'spark', 'core'].includes(v)
  },
  to: {
    type: String,
    default: ''
  }
})

const router = useRouter()

const themeClass = computed(() => props.theme)

const handleClick = () => {
  if (props.to) {
    router.push(props.to)
  }
}
</script>

<style scoped>
/* ── 面板容器 ── */
.agent-panel {
  width: 320px;
  min-height: 340px;
  border-radius: 20px;
  padding: 0;
  cursor: pointer;
  display: flex;
  flex-direction: column;
  position: relative;
  overflow: hidden;
  border: 1px solid rgba(255,255,255,0.04);
  background: rgba(12, 18, 30, 0.60);
  backdrop-filter: blur(12px);
  transition: transform 0.4s var(--ease-out),
              box-shadow 0.4s var(--ease-out),
              border-color 0.4s var(--ease-out);
}
.agent-panel:hover {
  transform: translateY(-8px);
}

/* ── 面板光晕 ── */
.panel-aura {
  position: absolute;
  inset: 0;
  opacity: 0;
  transition: opacity 0.5s var(--ease-out);
  pointer-events: none;
}
.agent-panel:hover .panel-aura {
  opacity: 1;
}

/* ── 面板内容区 ── */
.panel-icon {
  width: 100%;
  height: 120px;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  z-index: 1;
  padding-top: 16px;
}
.panel-icon :slotted(svg),
.panel-icon > svg {
  width: 64px;
  height: 64px;
  filter: drop-shadow(0 0 20px currentColor);
  transition: transform 0.4s var(--ease-out);
}
.agent-panel:hover .panel-icon :slotted(svg),
.agent-panel:hover .panel-icon > svg {
  transform: scale(1.08);
}

.panel-body {
  padding: 0 24px 24px;
  position: relative;
  z-index: 1;
  display: flex;
  flex-direction: column;
  flex: 1;
}

.panel-name {
  font-family: var(--font-display);
  font-size: 1.4rem;
  font-weight: 800;
  margin-bottom: 6px;
  letter-spacing: 0.04em;
  display: inline-block;
  background: var(--card-gradient);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.panel-tagline {
  font-size: 0.85rem;
  color: var(--color-text-1);
  margin-bottom: 10px;
  font-weight: 500;
  letter-spacing: 0.02em;
}

.panel-desc {
  font-size: 0.75rem;
  color: var(--color-text-2);
  letter-spacing: 0.04em;
  margin-bottom: 20px;
  flex: 1;
}

.panel-action {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: 0.82rem;
  font-weight: 600;
  padding: 10px 18px;
  border-radius: 999px;
  border: 1px solid rgba(255,255,255,0.06);
  align-self: flex-start;
  cursor: pointer;
  transition: all 0.3s var(--ease-out);
  letter-spacing: 0.03em;
}
.panel-action svg {
  width: 14px;
  height: 14px;
  transition: transform 0.3s var(--ease-out);
}
.agent-panel:hover .panel-action svg {
  transform: translateX(4px);
}
.agent-panel:hover .panel-action {
  border-color: rgba(255,255,255,0.12);
}

/* ═══════ 主题：Soul ═══════ */
.soul {
  --card-border: rgba(240,144,160,0.10);
  --card-border-hover: rgba(240,144,160,0.25);
  --card-shadow: rgba(240,144,160,0.08);
  --card-aura: rgba(240,144,160,0.08);
  --card-accent: var(--color-aurora-3);
  --card-gradient: var(--gradient-soul);
  --card-action-hover: rgba(240,144,160,0.08);
  border-color: var(--card-border);
}
.soul:hover {
  border-color: var(--card-border-hover);
  box-shadow: 0 16px 64px var(--card-shadow);
}
.soul .panel-aura {
  background: radial-gradient(ellipse at 50% 0%, var(--card-aura), transparent 60%);
}
.soul .panel-icon :slotted(svg),
.soul .panel-icon > svg {
  color: var(--card-accent);
}
.soul .panel-action {
  color: var(--card-accent);
}
.soul .panel-action:hover {
  background: var(--card-action-hover);
}

/* ═══════ 主题：Spark ═══════ */
.spark {
  --card-border: rgba(180,160,232,0.10);
  --card-border-hover: rgba(180,160,232,0.25);
  --card-shadow: rgba(180,160,232,0.08);
  --card-aura: rgba(180,160,232,0.08);
  --card-accent: var(--color-aurora-2);
  --card-gradient: var(--gradient-spark);
  --card-action-hover: rgba(180,160,232,0.08);
  border-color: var(--card-border);
}
.spark:hover {
  border-color: var(--card-border-hover);
  box-shadow: 0 16px 64px var(--card-shadow);
}
.spark .panel-aura {
  background: radial-gradient(ellipse at 50% 0%, var(--card-aura), transparent 60%);
}
.spark .panel-icon :slotted(svg),
.spark .panel-icon > svg {
  color: var(--card-accent);
}
.spark .panel-action {
  color: var(--card-accent);
}
.spark .panel-action:hover {
  background: var(--card-action-hover);
}

/* ═══════ 主题：Core ═══════ */
.core {
  --card-border: rgba(126,200,224,0.10);
  --card-border-hover: rgba(126,200,224,0.25);
  --card-shadow: rgba(126,200,224,0.08);
  --card-aura: rgba(126,200,224,0.08);
  --card-accent: var(--color-aurora-1);
  --card-gradient: var(--gradient-core);
  --card-action-hover: rgba(126,200,224,0.08);
  border-color: var(--card-border);
}
.core:hover {
  border-color: var(--card-border-hover);
  box-shadow: 0 16px 64px var(--card-shadow);
}
.core .panel-aura {
  background: radial-gradient(ellipse at 50% 0%, var(--card-aura), transparent 60%);
}
.core .panel-icon :slotted(svg),
.core .panel-icon > svg {
  color: var(--card-accent);
}
.core .panel-action {
  color: var(--card-accent);
}
.core .panel-action:hover {
  background: var(--card-action-hover);
}

/* ── 响应式 ── */
@media (max-width: 1080px) {
  .agent-panel {
    width: 280px;
    min-height: 300px;
  }
  .panel-icon {
    height: 100px;
  }
  .panel-icon :slotted(svg),
  .panel-icon > svg {
    width: 52px;
    height: 52px;
  }
}

@media (max-width: 860px) {
  .agent-panel {
    width: 100%;
    max-width: 420px;
    min-height: auto;
    flex-direction: row;
    align-items: center;
  }
  .panel-icon {
    width: 100px;
    height: 100px;
    flex-shrink: 0;
    padding: 0;
  }
  .panel-icon :slotted(svg),
  .panel-icon > svg {
    width: 44px;
    height: 44px;
  }
  .panel-body {
    padding: 16px 20px;
  }
  .panel-name {
    font-size: 1.15rem;
  }
}

@media (max-width: 520px) {
  .agent-panel {
    flex-direction: column;
    padding: 0;
  }
  .panel-icon {
    width: 100%;
    height: 80px;
  }
  .panel-body {
    padding: 0 18px 18px;
  }
}
</style>

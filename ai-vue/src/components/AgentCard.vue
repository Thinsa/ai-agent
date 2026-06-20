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
    <div class="panel-ambient"></div>
    <div class="panel-orbit"></div>
    <div class="panel-sheen"></div>

    <div class="panel-icon">
      <slot name="icon">
        <svg v-if="theme === 'soul'" class="agent-glyph" viewBox="0 0 64 64" fill="none" aria-hidden="true">
          <path class="glyph-soft" d="M18 36c-4-5.2-4.5-12.6.3-17.1 4.4-4.2 10.6-2.9 13.7 1.1 3.1-4 9.3-5.3 13.7-1.1 4.8 4.5 4.3 11.9.3 17.1-3.2 4.1-8.7 8.3-14 12-5.3-3.7-10.8-7.9-14-12Z"/>
          <path d="M20 40c-4.8-5.5-5.6-14 .1-19.4 4.7-4.5 11.4-2.8 14.2 1.8 2.8-4.6 9.5-6.3 14.2-1.8 5.7 5.4 4.9 13.9.1 19.4-3.5 4-8.9 8.1-14.3 11.8C28.9 48.1 23.5 44 20 40Z" stroke="currentColor" stroke-width="3" stroke-linejoin="round"/>
          <path d="M18 30c-4.5.8-7.5 3-8.4 6.2-.8 2.9.7 5.8 3.7 7.7" stroke="currentColor" stroke-width="2" stroke-linecap="round" opacity="0.5"/>
          <path d="M46 30c4.5.8 7.5 3 8.4 6.2.8 2.9-.7 5.8-3.7 7.7" stroke="currentColor" stroke-width="2" stroke-linecap="round" opacity="0.5"/>
        </svg>
        <svg v-else-if="theme === 'spark'" class="agent-glyph" viewBox="0 0 64 64" fill="none" aria-hidden="true">
          <path class="glyph-soft" d="M32 9l5.5 15.5L53 30l-15.5 5.5L32 51l-5.5-15.5L11 30l15.5-5.5L32 9Z"/>
          <path d="M32 7l5.8 16.2L54 29l-16.2 5.8L32 51l-5.8-16.2L10 29l16.2-5.8L32 7Z" stroke="currentColor" stroke-width="3" stroke-linejoin="round"/>
          <path d="M18 47h10c5.2 0 8-2.8 8-8v-3" stroke="currentColor" stroke-width="2" stroke-linecap="round" opacity="0.58"/>
          <path d="M46 46l-7-7 7-7" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" opacity="0.58"/>
          <circle cx="17" cy="47" r="3" fill="currentColor" opacity="0.7"/>
          <circle cx="46" cy="32" r="3" fill="currentColor" opacity="0.7"/>
        </svg>
        <svg v-else class="agent-glyph" viewBox="0 0 64 64" fill="none" aria-hidden="true">
          <path class="glyph-soft" d="M32 10l18 10.5v21L32 52 14 41.5v-21L32 10Z"/>
          <path d="M32 8l20 11.7v24.6L32 56 12 44.3V19.7L32 8Z" stroke="currentColor" stroke-width="3" stroke-linejoin="round"/>
          <circle cx="32" cy="32" r="8" stroke="currentColor" stroke-width="3"/>
          <path d="M20 32H8M56 32H44M32 20V8M32 56V44" stroke="currentColor" stroke-width="2" stroke-linecap="round" opacity="0.58"/>
          <path d="M21.5 21.5 13 13M51 51l-8.5-8.5M42.5 21.5 51 13M13 51l8.5-8.5" stroke="currentColor" stroke-width="2" stroke-linecap="round" opacity="0.38"/>
        </svg>
      </slot>
    </div>

    <div class="panel-body">
      <div v-if="taskLabel" class="panel-task">{{ taskLabel }}</div>
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
  taskLabel: {
    type: String,
    default: ''
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
  isolation: isolate;
  animation: panel-rise 0.7s var(--ease-out) both;
  animation-delay: calc(var(--panel-index, 0) * 90ms);
  transition: transform 0.4s var(--ease-out),
              box-shadow 0.4s var(--ease-out),
              border-color 0.4s var(--ease-out),
              background 0.4s var(--ease-out);
}
.agent-panel:hover,
.agent-panel:focus-visible {
  transform: translateY(-8px);
}
.agent-panel:focus-visible {
  outline: 2px solid color-mix(in srgb, var(--card-accent) 65%, white);
  outline-offset: 4px;
}

@keyframes panel-rise {
  from {
    opacity: 0;
    transform: translateY(18px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* ── 面板光晕 ── */
.panel-aura {
  position: absolute;
  inset: 0;
  opacity: 0.55;
  transition: opacity 0.5s var(--ease-out);
  pointer-events: none;
  z-index: 0;
}
.agent-panel:hover .panel-aura,
.agent-panel:focus-visible .panel-aura {
  opacity: 1;
}
.panel-ambient {
  position: absolute;
  inset: -1px;
  z-index: 0;
  pointer-events: none;
  background:
    radial-gradient(circle at 50% 0%, color-mix(in srgb, var(--card-accent) 18%, transparent), transparent 38%),
    linear-gradient(90deg, transparent, color-mix(in srgb, var(--card-accent) 26%, transparent), transparent);
  background-size: 100% 100%, 220% 1px;
  background-position: 50% 0, -120% 0;
  background-repeat: no-repeat;
  opacity: 0.5;
  animation: ambient-scan 5.6s var(--ease-in-out) infinite;
}
.panel-orbit {
  position: absolute;
  inset: 18px;
  z-index: 1;
  border-radius: 18px;
  pointer-events: none;
  opacity: 0.32;
  background:
    linear-gradient(90deg, transparent 0 46%, color-mix(in srgb, var(--card-accent) 34%, transparent) 50%, transparent 54% 100%) top / 220% 1px no-repeat,
    linear-gradient(90deg, transparent 0 46%, color-mix(in srgb, var(--card-accent) 20%, transparent) 50%, transparent 54% 100%) bottom / 220% 1px no-repeat,
    linear-gradient(180deg, transparent 0 46%, color-mix(in srgb, var(--card-accent) 18%, transparent) 50%, transparent 54% 100%) left / 1px 220% no-repeat,
    linear-gradient(180deg, transparent 0 46%, color-mix(in srgb, var(--card-accent) 18%, transparent) 50%, transparent 54% 100%) right / 1px 220% no-repeat;
  animation: orbit-flow 7s linear infinite;
}
.agent-panel:hover .panel-ambient,
.agent-panel:focus-visible .panel-ambient {
  opacity: 0.78;
}
.agent-panel:hover .panel-orbit,
.agent-panel:focus-visible .panel-orbit {
  opacity: 0.54;
  animation-duration: 3.8s;
}

@keyframes ambient-scan {
  0%, 100% {
    background-position: 50% 0, -120% 0;
  }
  50% {
    background-position: 50% 0, 120% 0;
  }
}

@keyframes orbit-flow {
  from {
    background-position: -120% top, 120% bottom, left -120%, right 120%;
  }
  to {
    background-position: 120% top, -120% bottom, left 120%, right -120%;
  }
}
.panel-sheen {
  position: absolute;
  inset: 0;
  z-index: 1;
  pointer-events: none;
  background: linear-gradient(115deg, transparent 0%, transparent 34%, rgba(255,255,255,0.12) 46%, transparent 58%, transparent 100%);
  transform: translateX(-120%);
  opacity: 0;
}
.agent-panel:hover .panel-sheen,
.agent-panel:focus-visible .panel-sheen {
  animation: panel-sheen 0.9s var(--ease-out);
}

@keyframes panel-sheen {
  0% {
    opacity: 0;
    transform: translateX(-120%);
  }
  20% {
    opacity: 1;
  }
  100% {
    opacity: 0;
    transform: translateX(120%);
  }
}

/* ── 面板内容区 ── */
.panel-icon {
  width: 100%;
  height: 120px;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  z-index: 2;
  padding-top: 16px;
}
.panel-icon::before,
.panel-icon::after {
  content: '';
  position: absolute;
  border-radius: 50%;
  pointer-events: none;
}
.panel-icon::before {
  width: 86px;
  height: 86px;
  border: 1px solid color-mix(in srgb, var(--card-accent) 22%, transparent);
  background: radial-gradient(circle, color-mix(in srgb, var(--card-accent) 11%, transparent), transparent 68%);
  animation: icon-halo 4.8s var(--ease-in-out) infinite;
}
.panel-icon::after {
  width: 56px;
  height: 56px;
  border: 1px dashed color-mix(in srgb, var(--card-accent) 28%, transparent);
  animation: icon-orbit 12s linear infinite;
}
.panel-icon :slotted(svg),
.panel-icon > svg {
  width: 64px;
  height: 64px;
  filter: drop-shadow(0 0 20px currentColor);
  opacity: 0.9;
  position: relative;
  z-index: 1;
  animation: glyph-breathe 4.2s var(--ease-in-out) infinite;
  transition: transform 0.4s var(--ease-out),
              opacity 0.4s var(--ease-out),
              filter 0.4s var(--ease-out);
}
.agent-glyph .glyph-soft {
  fill: currentColor;
  opacity: 0.13;
}
.agent-panel:hover .panel-icon :slotted(svg),
.agent-panel:hover .panel-icon > svg,
.agent-panel:focus-visible .panel-icon :slotted(svg),
.agent-panel:focus-visible .panel-icon > svg {
  opacity: 1;
  transform: translateY(-4px) scale(1.08) rotate(-2deg);
  filter: drop-shadow(0 0 28px currentColor);
}

@keyframes icon-halo {
  0%, 100% {
    opacity: 0.5;
    transform: scale(0.95);
  }
  50% {
    opacity: 0.95;
    transform: scale(1.05);
  }
}

@keyframes icon-orbit {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

@keyframes glyph-breathe {
  0%, 100% {
    opacity: 0.84;
    filter: drop-shadow(0 0 16px currentColor);
  }
  50% {
    opacity: 1;
    filter: drop-shadow(0 0 24px currentColor);
  }
}

.panel-body {
  padding: 0 24px 24px;
  position: relative;
  z-index: 2;
  display: flex;
  flex-direction: column;
  flex: 1;
}

.panel-task {
  width: fit-content;
  margin-bottom: 12px;
  padding: 5px 10px;
  border: 1px solid color-mix(in srgb, var(--card-accent) 32%, transparent);
  border-radius: var(--radius-full);
  background: color-mix(in srgb, var(--card-accent) 10%, transparent);
  color: var(--card-accent);
  font-size: 0.68rem;
  font-weight: 700;
  letter-spacing: 0.08em;
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
  transition: transform 0.3s var(--ease-out);
}
.agent-panel:hover .panel-name,
.agent-panel:focus-visible .panel-name {
  transform: translateX(2px);
}

.panel-tagline {
  font-size: 0.85rem;
  color: var(--color-text-1);
  margin-bottom: 10px;
  font-weight: 500;
  letter-spacing: 0.02em;
}

.panel-desc {
  font-size: 0.78rem;
  color: var(--color-text-2);
  line-height: 1.75;
  letter-spacing: 0.02em;
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
  background: rgba(255,255,255,0.02);
  transition: transform 0.3s var(--ease-out),
              background 0.3s var(--ease-out),
              border-color 0.3s var(--ease-out),
              box-shadow 0.3s var(--ease-out);
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
.agent-panel:hover .panel-action,
.agent-panel:focus-visible .panel-action {
  transform: translateY(-1px);
  border-color: rgba(255,255,255,0.12);
  box-shadow: 0 8px 24px var(--card-shadow);
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
.soul:hover,
.soul:focus-visible {
  border-color: var(--card-border-hover);
  box-shadow: 0 16px 64px var(--card-shadow);
  background: linear-gradient(180deg, rgba(240,144,160,0.08), rgba(12,18,30,0.70));
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
.spark:hover,
.spark:focus-visible {
  border-color: var(--card-border-hover);
  box-shadow: 0 16px 64px var(--card-shadow);
  background: linear-gradient(180deg, rgba(180,160,232,0.08), rgba(12,18,30,0.70));
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
.core:hover,
.core:focus-visible {
  border-color: var(--card-border-hover);
  box-shadow: 0 16px 64px var(--card-shadow);
  background: linear-gradient(180deg, rgba(126,200,224,0.08), rgba(12,18,30,0.70));
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

@media (prefers-reduced-motion: reduce) {
  .agent-panel,
  .panel-sheen,
  .panel-ambient,
  .panel-orbit,
  .panel-icon::before,
  .panel-icon::after,
  .panel-icon :slotted(svg),
  .panel-icon > svg,
  .panel-name,
  .panel-action,
  .panel-action svg {
    animation: none;
    transition-duration: 0.01ms;
  }

  .agent-panel:hover,
  .agent-panel:focus-visible {
    transform: none;
  }
}
</style>

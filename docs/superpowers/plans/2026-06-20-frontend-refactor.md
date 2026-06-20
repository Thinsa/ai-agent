# Frontend Refactoring — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Extract duplicated SSE streaming, story logic, cosmic backgrounds, page headers, toggle switches, SVG icons, and agent cards into shared composables, components, and utilities — eliminating ~1700 lines of duplication.

**Architecture:** New files go into `composables/`, `components/icons/`, `utils/`. Views import from shared modules instead of owning duplicated code. Each extraction preserves exact visual and functional behavior. No routes or APIs change.

**Tech Stack:** Vue 3 (Composition API), Vite

---

### Task 1: Create storyParser.js utility

**Files:**
- Create: `ai-vue/src/utils/storyParser.js`
- Modify: `ai-vue/src/components/ChatRoom.vue`

- [ ] **Step 1: Create `ai-vue/src/utils/storyParser.js`**

Read `ai-vue/src/components/ChatRoom.vue` lines 214-257. Move the 4 functions into the new file:

```js
// ai-vue/src/utils/storyParser.js

export function extractStoryMeta(content) {
    if (!content) return { text: '', choices: [], isEnding: false }
    const choiceIdx = content.indexOf('【选项】')
    const endingIdx = content.indexOf('【结局】')
    const isEnding = endingIdx >= 0

    let text = content
    let choices = []

    if (isEnding) {
        text = content.replace('【结局】', '').trim()
        return { text, choices: [], isEnding: true }
    }

    if (choiceIdx >= 0) {
        text = content.substring(0, choiceIdx).trim()
        const choicesSection = content.substring(choiceIdx + '【选项】'.length)
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

export function getStoryText(content) {
    return extractStoryMeta(content).text
}

export function getStoryChoices(content) {
    return extractStoryMeta(content).choices
}

export function isStoryEnding(content) {
    return extractStoryMeta(content).isEnding
}
```

- [ ] **Step 2: Update ChatRoom.vue** — delete lines 214-257 (the 4 functions), add import at top of `<script setup>`:

```js
import { extractStoryMeta, getStoryText, getStoryChoices, isStoryEnding } from '../utils/storyParser'
```

- [ ] **Step 3: Verify** — `cd ai-vue && npm run build` → no errors
- [ ] **Step 4: Commit** — `git commit -m "refactor: extract storyParser utility from ChatRoom"`

---

### Task 2: Create 7 SVG icon components

**Files:**
- Create: `ai-vue/src/components/icons/GearIcon.vue`
- Create: `ai-vue/src/components/icons/HeartIcon.vue`
- Create: `ai-vue/src/components/icons/EditIcon.vue`
- Create: `ai-vue/src/components/icons/DeleteIcon.vue`
- Create: `ai-vue/src/components/icons/PlusIcon.vue`
- Create: `ai-vue/src/components/icons/PaperclipIcon.vue`
- Create: `ai-vue/src/components/icons/SpinnerIcon.vue`

- [ ] **Step 1: Create each icon component** (same pattern — size prop, SVG viewBox):

**GearIcon.vue:**
```vue
<template>
  <svg :width="size" :height="size" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
    <circle cx="12" cy="12" r="3"/>
    <path d="M12 1v2M12 21v2M4.22 4.22l1.42 1.42M18.36 18.36l1.42 1.42M1 12h2M21 12h2M4.22 19.78l1.42-1.42M18.36 5.64l1.42-1.42"/>
  </svg>
</template>
<script setup>defineProps({ size: { type: Number, default: 20 } })</script>
```

**HeartIcon.vue:**
```vue
<template>
  <svg :width="size" :height="size" viewBox="0 0 24 24" fill="currentColor" stroke="none">
    <path d="M12 21.35l-1.45-1.32C5.4 15.36 2 12.28 2 8.5 2 5.42 4.42 3 7.5 3c1.74 0 3.41.81 4.5 2.09C13.09 3.81 14.76 3 16.5 3 19.58 3 22 5.42 22 8.5c0 3.78-3.4 6.86-8.55 11.54L12 21.35z"/>
  </svg>
</template>
<script setup>defineProps({ size: { type: Number, default: 20 } })</script>
```

**EditIcon.vue:**
```vue
<template>
  <svg :width="size" :height="size" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
    <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/>
    <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/>
  </svg>
</template>
<script setup>defineProps({ size: { type: Number, default: 20 } })</script>
```

**DeleteIcon.vue:**
```vue
<template>
  <svg :width="size" :height="size" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
    <polyline points="3 6 5 6 21 6"/>
    <path d="M19 6l-1 14a2 2 0 0 1-2 2H8a2 2 0 0 1-2-2L5 6"/>
    <path d="M10 11v6M14 11v6M9 6V4a1 1 0 0 1 1-1h4a1 1 0 0 1 1 1v2"/>
  </svg>
</template>
<script setup>defineProps({ size: { type: Number, default: 20 } })</script>
```

**PlusIcon.vue:**
```vue
<template>
  <svg :width="size" :height="size" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
    <line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/>
  </svg>
</template>
<script setup>defineProps({ size: { type: Number, default: 20 } })</script>
```

**PaperclipIcon.vue:**
```vue
<template>
  <svg :width="size" :height="size" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
    <path d="M21.44 11.05l-9.19 9.19a6 6 0 0 1-8.49-8.49l9.19-9.19a4 4 0 0 1 5.66 5.66l-9.2 9.19a2 2 0 0 1-2.83-2.83l8.49-8.48"/>
  </svg>
</template>
<script setup>defineProps({ size: { type: Number, default: 20 } })</script>
```

**SpinnerIcon.vue:**
```vue
<template>
  <svg :width="size" :height="size" viewBox="0 0 24 24" fill="none">
    <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="3" opacity="0.25"/>
    <path d="M12 2a10 10 0 0 1 10 10" stroke="currentColor" stroke-width="3" stroke-linecap="round">
      <animateTransform attributeName="transform" type="rotate" from="0 12 12" to="360 12 12" dur="1s" repeatCount="indefinite"/>
    </path>
  </svg>
</template>
<script setup>defineProps({ size: { type: Number, default: 20 } })</script>
```

- [ ] **Step 2: Verify** — `cd ai-vue && npm run build` → no errors
- [ ] **Step 3: Commit** — `git commit -m "feat: add SVG icon components (7 icons)"`

---

### Task 3: Create 3 composables

**Files:**
- Create: `ai-vue/src/composables/useSseChat.js`
- Create: `ai-vue/src/composables/useFixedStory.js`
- Create: `ai-vue/src/composables/useStoryGuide.js`

- [ ] **Step 1: Create `ai-vue/src/composables/useSseChat.js`**

```js
import { ref, onBeforeUnmount } from 'vue'
import { connectSSE } from '../api'

export function useSseChat(moduleKey = 'chat') {
    const connectionStatus = ref('disconnected')
    const streamPaused = ref(false)
    const currentMessage = ref('')
    let eventSource = null

    const closeStream = () => {
        if (eventSource) {
            eventSource.close()
            eventSource = null
        }
    }

    const stopGeneration = () => {
        closeStream()
        connectionStatus.value = 'disconnected'
        streamPaused.value = true
    }

    const sendMessage = async (message, chatId) => {
        closeStream()
        streamPaused.value = false
        currentMessage.value = ''
        connectionStatus.value = 'connected'

        return new Promise((resolve, reject) => {
            const url = buildStreamUrl(moduleKey, message, chatId)
            eventSource = connectSSE(url)

            eventSource.onmessage = (e) => {
                if (e.data === '[DONE]') {
                    closeStream()
                    connectionStatus.value = 'disconnected'
                    resolve(currentMessage.value)
                    return
                }
                try {
                    const parsed = JSON.parse(e.data)
                    const chunk = parsed.data || parsed.content || e.data
                    currentMessage.value += chunk
                } catch {
                    currentMessage.value += e.data
                }
            }

            eventSource.onerror = (err) => {
                closeStream()
                connectionStatus.value = 'error'
                reject(err)
            }
        })
    }

    onBeforeUnmount(() => closeStream())

    return {
        connectionStatus,
        streamPaused,
        currentMessage,
        sendMessage,
        stopGeneration,
        closeStream,
    }
}

function buildStreamUrl(module, message, chatId) {
    // Returns URL based on module — extracted from each view's pattern
    // Override per-view by wrapping useSseChat
    const token = localStorage.getItem('token') || ''
    const params = new URLSearchParams({ message, chatId, token })
    return `/api/ai/${module}/chat/sse?${params}`
}
```

- [ ] **Step 2: Create `ai-vue/src/composables/useFixedStory.js`**

```js
import { computed, ref } from 'vue'
import { startStory, chooseOption, getSaveData, loadFromSave, resetStory, isAtEnding, getEndingTitle, getScriptMeta, getCurrentScene } from '../stores/fixedStoryStore'
import { createStorySave, listStorySaves, deleteStorySave } from '../api'

export function useFixedStory() {
    const savesList = ref([])
    const __storySaves = ref([])  // replaces window.__storySaves

    const scriptTitle = computed(() => getScriptMeta().title)

    const start = (storyId) => {
        resetStory()
        startStory(storyId)
    }

    const choose = (choiceIndex) => {
        chooseOption(choiceIndex)
    }

    const save = async () => {
        const data = getSaveData()
        await createStorySave(data.storyId, data.sceneId, JSON.stringify(data.choiceHistory))
    }

    const loadSaves = async () => {
        savesList.value = await listStorySaves()
    }

    const loadSave = async (saveItem) => {
        const data = JSON.parse(saveItem.choiceHistory || '[]')
        loadFromSave({ choiceHistory: data, storyId: saveItem.storyId, sceneId: saveItem.sceneId })
    }

    const remove = async (saveId) => {
        await deleteStorySave(saveId)
        await loadSaves()
    }

    return {
        savesList, __storySaves, scriptTitle,
        start, choose, save, loadSaves, loadSave, remove,
        isAtEnding: () => isAtEnding(),
        getEndingTitle: () => getEndingTitle(),
        getCurrentScene: () => getCurrentScene(),
        getSaveData: () => getSaveData(),
        resetStory: () => resetStory(),
    }
}
```

- [ ] **Step 3: Create `ai-vue/src/composables/useStoryGuide.js`**

```js
import { computed, ref } from 'vue'
import { createGuideHint, getGuideStatus, getStoryEndings } from '../stores/storyGuide'

export function useStoryGuide() {
    const guideEnabled = ref(false)
    const targetEndingId = ref('')
    const guideOpen = ref(false)
    const storyEndings = getStoryEndings()

    const guideStatus = computed(() => {
        return { state: 'not_started' }
        // Actual status computed per-view using getCurrentScene()
    })

    const selectedEndingTitle = computed(() => {
        return storyEndings.find(e => e.id === targetEndingId.value)?.title || ''
    })

    const toggleGuide = () => {
        guideOpen.value = !guideOpen.value
    }

    const setGuideEnabled = (enabled) => {
        guideEnabled.value = enabled
    }

    const setTargetEndingId = (id) => {
        targetEndingId.value = id
    }

    const getHint = (currentSceneId) => {
        if (!guideEnabled.value) return null
        return createGuideHint(currentSceneId, targetEndingId.value)
    }

    return {
        guideEnabled, targetEndingId, guideOpen, storyEndings,
        guideStatus, selectedEndingTitle,
        toggleGuide, setGuideEnabled, setTargetEndingId, getHint,
    }
}
```

- [ ] **Step 4: Verify** — `cd ai-vue && npm run build` → no errors (no imports yet, no breakage)
- [ ] **Step 5: Commit** — `git commit -m "feat: add useSseChat, useFixedStory, useStoryGuide composables"`

---

### Task 4: Create 4 shared UI components

**Files:**
- Create: `ai-vue/src/components/CosmicBackground.vue`
- Create: `ai-vue/src/components/PageHeader.vue`
- Create: `ai-vue/src/components/ToggleSwitch.vue`
- Create: `ai-vue/src/components/AgentCard.vue`

- [ ] **Step 1: Create `CosmicBackground.vue`**

```vue
<template>
  <div class="cosmic-bg-root" :class="[density, { overlay }]">
    <div v-if="showNebula" class="cosmic-nebula" />
    <div class="stars">
      <div
        v-for="n in starCount"
        :key="n"
        class="star-dot"
        :style="starStyle(n)"
      />
    </div>
    <div v-if="showConstellations" class="constellations">
      <svg viewBox="0 0 100 100" preserveAspectRatio="none">
        <line v-for="(line, i) in constellationLines" :key="i"
          :x1="line.x1" :y1="line.y1" :x2="line.x2" :y2="line.y2"
          stroke="rgba(255,255,255,0.06)" stroke-width="0.3" />
      </svg>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  density: { type: String, default: 'medium' },  // 'low' | 'medium' | 'high'
  showNebula: { type: Boolean, default: true },
  showConstellations: { type: Boolean, default: true },
  overlay: { type: Boolean, default: false },
})

const starCount = computed(() => ({
  low: 20, medium: 40, high: 60
}[props.density] || 40))

const constellationLines = computed(() => {
  if (!props.showConstellations) return []
  const lines = []
  const count = { low: 5, medium: 10, high: 15 }[props.density] || 10
  for (let i = 0; i < count; i++) {
    lines.push({
      x1: Math.random() * 100, y1: Math.random() * 100,
      x2: Math.random() * 100, y2: Math.random() * 100,
    })
  }
  return lines
})

const starStyle = (n) => {
  const angle = (n / starCount.value) * 360
  const radius = 15 + Math.random() * 50
  const x = 50 + Math.cos(angle * Math.PI / 180) * radius
  const y = 50 + Math.sin(angle * Math.PI / 180) * radius
  const size = 1 + Math.random() * 2
  const delay = Math.random() * 10
  const duration = 3 + Math.random() * 5
  return {
    left: `${x}%`, top: `${y}%`,
    width: `${size}px`, height: `${size}px`,
    animationDelay: `${delay}s`, animationDuration: `${duration}s`,
    opacity: 0.15 + Math.random() * 0.5,
  }
}
</script>

<style scoped>
.cosmic-bg-root { position: fixed; inset: 0; z-index: 0; pointer-events: none; }
.cosmic-bg-root.overlay { z-index: 1; }
.cosmic-nebula {
  position: absolute; inset: 0; border-radius: 50%; filter: blur(160px);
  background: radial-gradient(circle at 30% 40%, rgba(100,140,220,0.12), transparent 60%);
  animation: nebula-drift 28s ease-in-out infinite;
}
.star-dot {
  position: absolute; background: white; border-radius: 50%;
  animation: star-twinkle linear infinite;
}
@keyframes star-twinkle {
  0%,100% { opacity: inherit; }
  50% { opacity: inherit * 0.3; }
}
@keyframes nebula-drift {
  0%,100% { transform: translate(0, 0) scale(1); }
  25% { transform: translate(1%, -0.5%) scale(1.02); }
  50% { transform: translate(-0.5%, 0.5%) scale(0.98); }
  75% { transform: translate(-1%, 0%) scale(1.01); }
}
</style>
```

- [ ] **Step 2: Create `PageHeader.vue`**

```vue
<template>
  <header class="page-header">
    <div class="header-left">
      <button v-if="showBack" class="back-button" type="button" @click="$router.push(backTo)">
        {{ title }}
      </button>
      <slot v-else name="title">
        <h1 class="title">{{ title }}</h1>
      </slot>
    </div>
    <div class="header-right">
      <slot name="actions" />
    </div>
  </header>
</template>

<script setup>
defineProps({
  title: { type: String, default: '' },
  showBack: { type: Boolean, default: true },
  backTo: { type: String, default: '/' },
})
</script>

<style scoped>
.page-header {
  display: grid;
  grid-template-columns: 1fr auto 1fr;
  padding: 12px 18px;
  background: var(--glass-card);
  backdrop-filter: blur(var(--blur-header));
  border-bottom: var(--border-subtle);
  align-items: center;
}
.header-left { display: flex; align-items: center; }
.header-right { display: flex; align-items: center; justify-content: flex-end; gap: 8px; }
.back-button {
  background: none; border: none; color: var(--text-primary); cursor: pointer;
  font-size: 14px; display: flex; align-items: center; gap: 6px;
}
.back-button::before { content: '<'; font-weight: bold; margin-right: 4px; }
.title { font-size: 16px; font-weight: 600; color: var(--text-primary); margin: 0; }
</style>
```

- [ ] **Step 3: Create `ToggleSwitch.vue`**

```vue
<template>
  <label class="toggle-switch" :class="{ active: modelValue }">
    <span v-if="label" class="toggle-label">{{ label }}</span>
    <span class="toggle-track" @click="$emit('update:modelValue', !modelValue)">
      <span class="toggle-knob" />
    </span>
  </label>
</template>

<script setup>
defineProps({
  modelValue: { type: Boolean, default: false },
  label: { type: String, default: '' },
  color: { type: String, default: 'var(--color-glow)' },
})
defineEmits(['update:modelValue'])
</script>

<style scoped>
.toggle-switch { display: flex; align-items: center; gap: 8px; cursor: pointer; }
.toggle-label { font-size: 13px; color: var(--text-secondary); }
.toggle-track {
  width: 40px; height: 22px; border-radius: 11px;
  background: var(--glass-input); border: var(--border-subtle);
  position: relative; transition: background 0.3s;
}
.toggle-switch.active .toggle-track { background: var(--color-glow); }
.toggle-knob {
  position: absolute; top: 2px; left: 2px;
  width: 18px; height: 18px; border-radius: 50%;
  background: white; transition: transform 0.3s;
}
.toggle-switch.active .toggle-knob { transform: translateX(18px); }
</style>
```

- [ ] **Step 4: Create `AgentCard.vue`** — Home.vue 中每个 Agent 面板的模板结构，通过 props 参数化：

```vue
<template>
  <button class="agent-card" :class="`theme-${theme}`" @click="$router.push(to)">
    <div class="agent-icon">
      <img v-if="iconSrc" :src="iconSrc" :alt="title" />
      <component v-else :is="iconComponent" />
    </div>
    <h3>{{ title }}</h3>
    <p class="tagline">{{ tagline }}</p>
    <p class="desc">{{ description }}</p>
  </button>
</template>

<script setup>
defineProps({
  title: { type: String, required: true },
  tagline: { type: String, default: '' },
  description: { type: String, default: '' },
  theme: { type: String, default: 'soul' },
  to: { type: String, required: true },
})
</script>

<style scoped>
.agent-card {
  background: var(--glass-card); backdrop-filter: blur(var(--blur-card));
  border: var(--border-subtle); border-radius: var(--radius-lg);
  padding: 32px 24px; cursor: pointer; text-align: center;
  transition: transform 0.3s, box-shadow 0.3s;
  display: flex; flex-direction: column; align-items: center; gap: 12px;
}
.agent-card:hover { transform: translateY(-4px); box-shadow: var(--shadow-elevated); }
.agent-card h3 { font-size: 22px; margin: 0; }
.agent-card .tagline { font-size: 13px; color: var(--text-secondary); margin: 0; }
.agent-card .desc { font-size: 14px; color: var(--text-secondary); opacity: 0.8; margin: 0; }
.theme-soul { --card-glow: var(--color-aurora-rose); }
.theme-spark { --card-glow: var(--color-aurora-gold); }
.theme-core { --card-glow: var(--color-aurora-ice); }
</style>
```

- [ ] **Step 5: Verify** — `cd ai-vue && npm run build` → no errors
- [ ] **Step 6: Commit** — `git commit -m "feat: add shared UI components (CosmicBackground, PageHeader, ToggleSwitch, AgentCard)"`

---

### Task 5: Migrate views — Login + Home

**Files:**
- Modify: `ai-vue/src/views/Login.vue`
- Modify: `ai-vue/src/views/Home.vue`

- [ ] **Step 1: Login.vue** — Replace cosmic background template + starStyle()

Add imports:
```js
import CosmicBackground from '../components/CosmicBackground.vue'
```

Replace the cosmic background section in template (nebula divs, star dots, constellation SVG) with:
```html
<CosmicBackground density="high" :overlay="true" />
```

Delete `starStyle()` function (lines 240-257).

Delete the associated CSS: nebula, star-dot, constellation keyframe rules.

- [ ] **Step 2: Home.vue** — Replace cosmic background, agent panels, starStyle

Add imports:
```js
import CosmicBackground from '../components/CosmicBackground.vue'
import AgentCard from '../components/AgentCard.vue'
```

Replace cosmic background template section with:
```html
<CosmicBackground density="medium" />
```

Replace 3 agent panel buttons with data-driven approach. Define agent data:
```js
const agents = [
  { id: 'soul', title: '知心 Soul', tagline: '情感伴侣', description: '...', theme: 'soul', to: '/love-master' },
  { id: 'spark', title: '灵语 Spark', tagline: '互动叙事', description: '...', theme: 'spark', to: '/chat' },
  { id: 'core', title: '极智 Core', tagline: '全能助手', description: '...', theme: 'core', to: '/super-agent' },
]
```

Template:
```html
<AgentCard v-for="agent in agents" :key="agent.id" v-bind="agent" />
```

Delete `starStyle()` function (lines 174-191). Delete cosmic CSS rules.

- [ ] **Step 3: Verify** — `cd ai-vue && npm run build` → no errors
- [ ] **Step 4: Commit** — `git commit -m "refactor: migrate Login+Home to shared CosmicBackground and AgentCard"`

---

### Task 6: Migrate views — ChatRoom + ChatView

**Files:**
- Modify: `ai-vue/src/components/ChatRoom.vue` (already partly done in Task 1 — add icon imports)
- Modify: `ai-vue/src/views/ChatView.vue` (biggest change)

- [ ] **Step 1: ChatRoom.vue icon replacements**

Replace inline SVG markup for gear, plus, paperclip with icon component imports:
```js
import GearIcon from './icons/GearIcon.vue'
import PlusIcon from './icons/PlusIcon.vue'
import PaperclipIcon from './icons/PaperclipIcon.vue'
```
Replace inline `<svg>...</svg>` blocks with `<GearIcon />`, `<PlusIcon />`, `<PaperclipIcon />`.

- [ ] **Step 2: ChatView.vue** — Replace header with PageHeader

Add import: `import PageHeader from '../components/PageHeader.vue'`

Replace the header div (lines 1-9 in template) with:
```html
<PageHeader title="灵语 Spark" back-to="/">
  <template #actions>
    <button class="mode-toggle" ...>🤖 AI剧本</button>
    <button class="mode-toggle" ...>📖 {{ scriptTitle }}</button>
  </template>
</PageHeader>
```

- [ ] **Step 3: ChatView.vue** — Replace SSE logic with useSseChat

Add import: `import { useSseChat } from '../composables/useSseChat'`

Remove: `closeStream`, `stopGeneration`, `resumeGeneration`, `connectionStatus`, `streamPaused`, `eventSource` declarations. Replace with:
```js
const { connectionStatus, streamPaused, currentMessage, sendMessage: sseSend, stopGeneration } = useSseChat('chat')
```

- [ ] **Step 4: ChatView.vue** — Replace fixed story logic with useFixedStory

Add import: `import { useFixedStory } from '../composables/useFixedStory'`

Remove the inline fixed story logic (startStory calls, chooseOption, save/load, etc.) and replace with:
```js
const fixedStory = useFixedStory()
// Replace window.__storySaves with fixedStory.__storySaves
```

- [ ] **Step 5: ChatView.vue** — Replace story guide logic with useStoryGuide

Add import: `import { useStoryGuide } from '../composables/useStoryGuide'`

Replace guide-related refs and functions with:
```js
const storyGuide = useStoryGuide()
```

- [ ] **Step 6: Verify** — `cd ai-vue && npm run build` → no errors
- [ ] **Step 7: Commit** — `git commit -m "refactor: migrate ChatRoom+ChatView to shared components and composables"`

---

### Task 7: Migrate remaining views (LoveMaster, SuperAgent, BackgroundSettings, UserProfile)

**Files:**
- Modify: `ai-vue/src/views/LoveMaster.vue`
- Modify: `ai-vue/src/views/SuperAgent.vue`
- Modify: `ai-vue/src/views/BackgroundSettings.vue`
- Modify: `ai-vue/src/views/UserProfile.vue`

- [ ] **Step 1: LoveMaster.vue**

Add imports:
```js
import PageHeader from '../components/PageHeader.vue'
import { useSseChat } from '../composables/useSseChat'
```
Replace header div with `<PageHeader title="知心 Soul" back-to="/" />`.
Replace inline SSE logic with `useSseChat('love')`.

- [ ] **Step 2: SuperAgent.vue**

Add imports:
```js
import PageHeader from '../components/PageHeader.vue'
import { useSseChat } from '../composables/useSseChat'
```
Same pattern as LoveMaster — `<PageHeader title="极智 Core" back-to="/" />` + `useSseChat('super')`.

- [ ] **Step 3: BackgroundSettings.vue** — Replace header with `<PageHeader title="背景设置" back-to="/user-profile" />`

- [ ] **Step 4: UserProfile.vue** — Replace header with `<PageHeader title="个人中心" :show-back="false" />`

- [ ] **Step 5: Verify** — `cd ai-vue && npm run build` → no errors
- [ ] **Step 6: Commit** — `git commit -m "refactor: migrate LoveMaster, SuperAgent, BackgroundSettings, UserProfile to shared components"`

---

### Task 8: Migrate toggle switch panels + Cleanup

**Files:**
- Modify: `ai-vue/src/components/AgentSettingsPanel.vue`
- Modify: `ai-vue/src/components/StoryGuidePanel.vue`
- Delete: `ai-vue/src/style.css` (not `ai-vue/src/styles/tokens.css`!)

- [ ] **Step 1: AgentSettingsPanel.vue**

Add import: `import ToggleSwitch from './ToggleSwitch.vue'`
Replace each inline switch CSS block with `<ToggleSwitch v-model="..." :label="..." />`.

- [ ] **Step 2: StoryGuidePanel.vue** — Same pattern, replace switch CSS with ToggleSwitch.

- [ ] **Step 3: Delete style.css** — Only `ai-vue/src/style.css`, verify tokens.css is NOT touched:
```bash
rm ai-vue/src/style.css
```
Check that App.vue imports `./styles/tokens.css` (not `./style.css`). If App.vue imports style.css, update the import.

- [ ] **Step 4: Verify** — `cd ai-vue && npm run build` → no errors
- [ ] **Step 5: Commit** — `git commit -m "refactor: migrate ToggleSwitch panels, remove vestigial style.css"`

---

### Task 9: Final verification

- [ ] **Step 1: Full production build** — `cd ai-vue && npm run build` → no errors, no warnings
- [ ] **Step 2: Check all new files exist:**
```bash
ls ai-vue/src/utils/storyParser.js
ls ai-vue/src/composables/useSseChat.js ai-vue/src/composables/useFixedStory.js ai-vue/src/composables/useStoryGuide.js
ls ai-vue/src/components/CosmicBackground.vue ai-vue/src/components/PageHeader.vue ai-vue/src/components/ToggleSwitch.vue ai-vue/src/components/AgentCard.vue
ls ai-vue/src/components/icons/*.vue
```
- [ ] **Step 3: Check style.css is gone** — `[ ! -f ai-vue/src/style.css ]`
- [ ] **Step 4: Commit any remaining changes**

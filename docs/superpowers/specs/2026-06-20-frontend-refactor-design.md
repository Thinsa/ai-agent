# 前端重构 — 设计规范

**时间：** 2026-06-20  
**状态：** 已批准  
**范围：** 阶段 3 — 提取共享组件和组合式函数，消除重复，清理残留代码

## 目标

1. 提取 `utils/storyParser.js` — 从 ChatRoom.vue 迁移故事解析函数
2. 提取 `composables/useSseChat.js` — SSE 流逻辑消除 3 处重复
3. 提取 `composables/useFixedStory.js` — 固定叙事引擎从 ChatView 提取
4. 提取 `composables/useStoryGuide.js` — 故事引导从 ChatView 提取
5. 提取 `components/CosmicBackground.vue` — 宇宙背景消除 Login+Home 重复
6. 提取 `components/PageHeader.vue` — 页面头部消除 5 个视图重复
7. 提取 `components/ToggleSwitch.vue` — 开关消除 2 个组件重复
8. 提取 `components/AgentCard.vue` — Agent 卡片从 Home.vue 提取
9. 提取 7 个 SVG 图标组件 — 内联 SVG 去重
10. 删除 `style.css` — 残留 Vite 样板代码
11. 替换 `window.__storySaves` → 本地 ref

## 非目标

- 路由变更（所有路由保持不变）
- API 变更（所有端点调用保持不变）
- 视觉变更（UI 完全不变）
- 响应格式变更
- CSS 设计系统改动（tokens.css 保持不变）

---

## 1. 工具模块 + 组合式函数

### 1a. `utils/storyParser.js` — 新增

从 `ChatRoom.vue` 第 214-257 行迁移，导出纯函数：

```js
export function extractStoryMeta(content) { ... }  // 提取故事元数据
export function getStoryText(content) { ... }       // 获取故事文本（移除【选项】标记）
export function getStoryChoices(content) { ... }     // 解析选项列表
export function isStoryEnding(content) { ... }       // 检测【结局】标记
```

ChatRoom.vue 改为导入调用，删除内联实现。

### 1b. `composables/useSseChat.js` — 新增

```js
export function useSseChat(endpointBuilder, options = {}) {
    // endpointBuilder: (message, chatId) => string (URL)
    return {
        connectionStatus,   // ref: 'connected' | 'disconnected' | 'error'
        streamPaused,       // ref: boolean
        currentMessage,     // ref: string — 正在累积的消息
        sendMessage,        // async function(message, chatId)
        stopGeneration,     // function — 关闭 EventSource
    }
}
```

替换位置：`ChatView.vue`、`LoveMaster.vue`、`SuperAgent.vue`

### 1c. `composables/useFixedStory.js` — 新增

从 ChatView.vue 提取固定分支叙事逻辑：

```js
export function useFixedStory(storyScript) {
    return {
        currentScene, startStory, makeChoice,
        saveProgress, loadProgress, savesList,
        isEnding, choiceHistory
    }
}
```

### 1d. `composables/useStoryGuide.js` — 新增

从 ChatView.vue 提取故事结局引导逻辑：

```js
export function useStoryGuide(storyCore) {
    return {
        guideEnabled, targetEnding, recommendedChoice,
        showGuidePanel, toggleGuide, selectTargetEnding
    }
}
```

---

## 2. UI 共享组件

### 2a. `CosmicBackground.vue`

```vue
<template>
  <div class="cosmic-bg" :class="[density, { overlay }]">
    <div v-if="showNebula" class="nebula" />
    <div v-if="showConstellations" class="constellations">
      <svg><!-- 星座连线 --></svg>
    </div>
    <div class="stars">
      <div v-for="star in stars" :key="star.id" :style="star.style" class="star-dot" />
    </div>
  </div>
</template>

props: { density: 'low' | 'medium' | 'high', showConstellations: boolean, showNebula: boolean, overlay: boolean }
```

### 2b. `PageHeader.vue`

```vue
<template>
  <header class="page-header">
    <div class="header-left">
      <button v-if="showBack" @click="$router.push(backTo)" class="back-button">{{ title }}</button>
      <slot v-else name="title">{{ title }}</slot>
    </div>
    <div class="header-right"><slot name="actions" /></div>
  </header>
</template>

props: { title: string, showBack: boolean, backTo: string }
```

### 2c. `ToggleSwitch.vue`

```vue
props: { modelValue: boolean, label: string, color: string }
emits: ['update:modelValue']
// v-model 支持
```

### 2d. `AgentCard.vue`

```vue
props: { icon: string, title: string, tagline: string, description: string, theme: 'soul'|'spark'|'core', to: string }
```

Home.vue 中改为数据驱动：
```vue
<AgentCard v-for="agent in agents" :key="agent.id" v-bind="agent" />
```

### 2e. SVG 图标（7 个文件）

| 文件 | 用途 |
|------|------|
| `icons/GearIcon.vue` | 设置齿轮 |
| `icons/HeartIcon.vue` | 收藏心形 |
| `icons/EditIcon.vue` | 编辑铅笔 |
| `icons/DeleteIcon.vue` | 删除垃圾桶 |
| `icons/PlusIcon.vue` | 添加加号 |
| `icons/PaperclipIcon.vue` | 附件回形针 |
| `icons/SpinnerIcon.vue` | 加载旋转器 |

每个组件接收可选的 `size` prop（默认 20），返回内联 SVG。

---

## 3. 视图迁移

| 视图 | 变更 |
|------|------|
| `ChatRoom.vue` | 删除故事解析函数 → 导入 storyParser；替换内联 SVG → 图标组件 |
| `ChatView.vue` | 采用 useSseChat + useFixedStory + useStoryGuide；替换 PageHeader；修复 window.__storySaves |
| `Login.vue` | 替换宇宙背景模板+CSS → CosmicBackground；替换内联 SVG → 图标 |
| `Home.vue` | 替换宇宙背景 → CosmicBackground；替换 3 个 Agent 面板 → AgentCard v-for；删除 starStyle() |
| `LoveMaster.vue` | 替换 PageHeader；采用 useSseChat；替换内联 SVG → 图标 |
| `SuperAgent.vue` | 同上 |
| `BackgroundSettings.vue` | 替换 PageHeader |
| `UserProfile.vue` | 替换 PageHeader |
| `AgentSettingsPanel.vue` | 替换开关 CSS → ToggleSwitch |
| `StoryGuidePanel.vue` | 替换开关 CSS → ToggleSwitch |

---

## 4. 清理

- 删除 `style.css`
- ChatView 中 `window.__storySaves` → 本地 `ref`

---

## 文件变更

| 文件 | 操作 |
|------|------|
| `utils/storyParser.js` | **新建** |
| `composables/useSseChat.js` | **新建** |
| `composables/useFixedStory.js` | **新建** |
| `composables/useStoryGuide.js` | **新建** |
| `components/CosmicBackground.vue` | **新建** |
| `components/PageHeader.vue` | **新建** |
| `components/ToggleSwitch.vue` | **新建** |
| `components/AgentCard.vue` | **新建** |
| `components/icons/GearIcon.vue` | **新建** |
| `components/icons/HeartIcon.vue` | **新建** |
| `components/icons/EditIcon.vue` | **新建** |
| `components/icons/DeleteIcon.vue` | **新建** |
| `components/icons/PlusIcon.vue` | **新建** |
| `components/icons/PaperclipIcon.vue` | **新建** |
| `components/icons/SpinnerIcon.vue` | **新建** |
| `style.css` | **删除** |
| `views/ChatView.vue` | **修改** |
| `views/Login.vue` | **修改** |
| `views/Home.vue` | **修改** |
| `views/LoveMaster.vue` | **修改** |
| `views/SuperAgent.vue` | **修改** |
| `views/BackgroundSettings.vue` | **修改** |
| `views/UserProfile.vue` | **修改** |
| `components/ChatRoom.vue` | **修改** |
| `components/AgentSettingsPanel.vue` | **修改** |
| `components/StoryGuidePanel.vue` | **修改** |

## 验证

1. `npm run build` 无错误
2. 所有路由手动冒烟测试：视觉效果不变
3. 登录、主页、聊天、故事、恋爱大师、超级代理均正常渲染
4. SSE 流在 ChatView、LoveMaster、SuperAgent 中正常运作

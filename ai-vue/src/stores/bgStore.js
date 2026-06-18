import { reactive } from 'vue'
import { fetchBackgrounds } from '../api'

const EMPTY_BG = Object.freeze({ url: '', opacity: 0.15 })

const state = reactive({
  backgrounds: {
    love: { ...EMPTY_BG },
    chat: { ...EMPTY_BG },
    super: { ...EMPTY_BG }
  },
  loaded: false
})

export async function loadBackgrounds() {
  if (state.loaded) return
  try {
    const data = await fetchBackgrounds()
    for (const key of Object.keys(state.backgrounds)) {
      const item = data[key]
      state.backgrounds[key] = item
        ? { url: item.imageData || '', opacity: item.opacity ?? 0.15 }
        : { ...EMPTY_BG }
    }
  } catch (e) {
    console.error('Load backgrounds failed:', e)
  } finally {
    state.loaded = true
  }
}

export function getBackground(agentKey) {
  return state.backgrounds[agentKey] ?? { ...EMPTY_BG }
}

export async function refreshBackgrounds() {
  state.loaded = false
  return loadBackgrounds()
}

export function resetBackgrounds() {
  state.loaded = false
  state.backgrounds = {
    love: { ...EMPTY_BG },
    chat: { ...EMPTY_BG },
    super: { ...EMPTY_BG }
  }
}

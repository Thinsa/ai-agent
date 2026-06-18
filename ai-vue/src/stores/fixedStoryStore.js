import { reactive } from 'vue'
import scriptData from '../assets/story-script.json'

const state = reactive({
  currentSceneId: null,
  choiceHistory: [],
  isEnding: false,
  endingTitle: ''
})

export function getScriptMeta() {
  return {
    title: scriptData.title,
    description: scriptData.description
  }
}

export function getCurrentScene() {
  if (!state.currentSceneId) return null
  return scriptData.scenes[state.currentSceneId] || null
}

export function getSceneText() {
  const scene = getCurrentScene()
  return scene ? scene.text : ''
}

export function getCurrentChoices() {
  const scene = getCurrentScene()
  if (!scene || !scene.choices) return []
  return scene.choices.map((c, i) => ({
    number: String(i + 1),
    label: c.text
  }))
}

export function isAtEnding() {
  return state.isEnding
}

export function getEndingTitle() {
  return state.endingTitle
}

export function startStory() {
  state.currentSceneId = 'start'
  state.choiceHistory = []
  state.isEnding = false
  state.endingTitle = ''
  const scene = getCurrentScene()
  return scene ? formatSceneOutput(scene) : ''
}

export function chooseOption(choiceIndex) {
  const scene = getCurrentScene()
  if (!scene || !scene.choices) return null
  const choice = scene.choices[choiceIndex]
  if (!choice) return null

  state.choiceHistory.push({
    from: state.currentSceneId,
    choice: choice.text,
    index: choiceIndex
  })

  state.currentSceneId = choice.next
  const nextScene = getCurrentScene()
  if (!nextScene) return null

  if (nextScene.isEnding) {
    state.isEnding = true
    state.endingTitle = nextScene.endingTitle || ''
  }

  return formatSceneOutput(nextScene)
}

export function loadFromSave(saveData) {
  state.currentSceneId = saveData.sceneId
  state.choiceHistory = JSON.parse(saveData.choiceHistory || '[]')
  state.isEnding = false
  state.endingTitle = ''
  const scene = getCurrentScene()
  if (scene && scene.isEnding) {
    state.isEnding = true
    state.endingTitle = scene.endingTitle || ''
  }
  return scene ? formatSceneOutput(scene) : ''
}

export function getSaveData() {
  return {
    storyId: 'lingqiao',
    sceneId: state.currentSceneId,
    choiceHistory: JSON.stringify(state.choiceHistory)
  }
}

export function resetStory() {
  state.currentSceneId = null
  state.choiceHistory = []
  state.isEnding = false
  state.endingTitle = ''
}

function formatSceneOutput(scene) {
  let text = scene.text
  if (scene.choices && scene.choices.length > 0) {
    text += '\n\n【选项】\n'
    text += scene.choices.map((c, i) => `${i + 1}. ${c.text}`).join('\n')
  }
  if (scene.isEnding) {
    text += '\n\n【结局】'
  }
  return text
}

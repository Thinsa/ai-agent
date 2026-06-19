import scriptData from '../assets/story-script.json'
import {
  createGuideHint as createGuideHintForScript,
  getGuideRecommendation as getGuideRecommendationForScript,
  getGuideStatus as getGuideStatusForScript,
  getStoryEndings as getStoryEndingsForScript
} from './storyGuideCore'

export function getStoryEndings() {
  return getStoryEndingsForScript(scriptData)
}

export function getGuideRecommendation(sceneId, targetEndingId) {
  return getGuideRecommendationForScript(scriptData, sceneId, targetEndingId)
}

export function getGuideStatus(sceneId, targetEndingId) {
  return getGuideStatusForScript(scriptData, sceneId, targetEndingId)
}

export function createGuideHint(sceneId, targetEndingId) {
  return createGuideHintForScript(scriptData, sceneId, targetEndingId)
}

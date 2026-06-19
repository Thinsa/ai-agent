import test from 'node:test'
import assert from 'node:assert/strict'
import { readFileSync } from 'node:fs'
import { fileURLToPath } from 'node:url'
import {
  getStoryEndings,
  getGuideRecommendation,
  getGuideStatus
} from './storyGuideCore.js'

const scriptData = JSON.parse(readFileSync(
  fileURLToPath(new URL('../assets/story-script.json', import.meta.url)),
  'utf8'
))

test('lists all Lingqiao endings', () => {
  assert.deepEqual(
    getStoryEndings(scriptData).map(ending => ending.title),
    ['等待的结局', '桥行者的归宿（好结局）', '遗忘的英雄（中性结局）', '三界之桥（真结局）']
  )
})

test('recommends a first step from start for every ending', () => {
  for (const ending of getStoryEndings(scriptData)) {
    const recommendation = getGuideRecommendation(scriptData, 'start', ending.id)
    assert.equal(recommendation.reachable, true)
    assert.equal(recommendation.choiceIndex >= 0, true)
    assert.equal(typeof recommendation.choiceText, 'string')
  }
})

test('recalculates recommendation after the player deviates', () => {
  const recommendation = getGuideRecommendation(scriptData, 'village', 'ending_true')

  assert.equal(recommendation.reachable, true)
  assert.equal(recommendation.nextSceneId, 'village_leave')
  assert.equal(recommendation.choiceText, '感觉不对劲，悄然离开')
})

test('reports unreachable targets from terminal endings', () => {
  const recommendation = getGuideRecommendation(scriptData, 'reject_mission', 'ending_true')

  assert.equal(recommendation.reachable, false)
  assert.equal(getGuideStatus(scriptData, 'reject_mission', 'ending_true').state, 'unreachable')
})

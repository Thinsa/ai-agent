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
  const recommendation = getGuideRecommendation(scriptData, 'village', 'true_eternal')

  assert.equal(recommendation.reachable, true)
  assert.equal(recommendation.nextSceneId, 'village_stay')
  assert.equal(recommendation.choiceText, '接受村民的款待，深入了解')
})

test('reports unreachable targets from terminal endings', () => {
  const recommendation = getGuideRecommendation(scriptData, 'reject_mission', 'true_eternal')

  assert.equal(recommendation.reachable, false)
  assert.equal(getGuideStatus(scriptData, 'reject_mission', 'true_eternal').state, 'unreachable')
})

test('expanded NPC scenes stay connected to endings', () => {
  const requiredNpcScenes = [
    'lantern_keeper_ayan',
    'herbalist_tang',
    'captive_cenbai',
    'scribe_xinghuai',
    'mirror_lake_lanyin'
  ]

  for (const sceneId of requiredNpcScenes) {
    assert.ok(scriptData.scenes[sceneId], `${sceneId} should exist`)
    const reachesSomeEnding = getStoryEndings(scriptData).some(ending => (
      getGuideRecommendation(scriptData, sceneId, ending.id).reachable
    ))
    assert.equal(reachesSomeEnding, true, `${sceneId} should be able to reach an ending`)
  }
})

test('fixed script has enough narrative density for meaningful play', () => {
  const scenes = Object.values(scriptData.scenes)
  const totalTextLength = scenes.reduce((sum, scene) => sum + scene.text.length, 0)
  const averageTextLength = totalTextLength / scenes.length
  const coreNpcScenes = [
    'lantern_keeper_ayan',
    'herbalist_tang',
    'captive_cenbai',
    'scribe_xinghuai',
    'mirror_lake_lanyin'
  ]

  assert.equal(scenes.length >= 90, true, 'script should include at least 90 scenes after expansion')
  assert.equal(totalTextLength >= 30000, true, 'script should include at least 30000 text characters')
  assert.equal(averageTextLength >= 250, true, 'average scene text should be at least 250 characters')
  for (const sceneId of coreNpcScenes) {
    assert.equal(scriptData.scenes[sceneId].text.length >= 260, true, `${sceneId} should feel like a full scene`)
  }
})

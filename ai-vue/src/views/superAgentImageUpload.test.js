import { readFileSync } from 'node:fs'
import { test } from 'node:test'
import assert from 'node:assert/strict'

const apiSource = readFileSync(new URL('../api/index.js', import.meta.url), 'utf8')
const superAgentSource = readFileSync(new URL('./SuperAgent.vue', import.meta.url), 'utf8')

test('SuperAgent forwards uploaded imageUrl to the Manus SSE request', () => {
  assert.match(
    apiSource,
    /export const chatWithManus = \(message, chatId, imageUrl\) => \{[\s\S]*if \(imageUrl\) params\.imageUrl = imageUrl[\s\S]*connectSSE\('\/ai\/manus\/chat', params\)/,
  )
  assert.match(
    superAgentSource,
    /const imageUrl = typeof payload === 'string' \? null : payload\.imageUrl/,
  )
  assert.match(
    superAgentSource,
    /chatWithManus\(message, chatId\.value, imageUrl\)/,
  )
  assert.match(
    superAgentSource,
    /runStreamChat\(buildPrompt\(text\), useMcp, visionEnabled\.value \? imageUrl : null\)/,
  )
})

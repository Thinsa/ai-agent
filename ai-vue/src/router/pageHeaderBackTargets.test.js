import test from 'node:test'
import assert from 'node:assert/strict'
import { readFileSync, readdirSync } from 'node:fs'
import { fileURLToPath } from 'node:url'

const srcDir = fileURLToPath(new URL('../', import.meta.url))
const viewsDir = fileURLToPath(new URL('../views/', import.meta.url))
const routerSource = readFileSync(
  fileURLToPath(new URL('./index.js', import.meta.url)),
  'utf8'
)

const routePaths = new Set(
  [...routerSource.matchAll(/path:\s*['"]([^'"]+)['"]/g)].map(match => match[1])
)

test('PageHeader back targets point to registered routes', () => {
  for (const fileName of readdirSync(viewsDir).filter(name => name.endsWith('.vue'))) {
    const filePath = new URL(`../views/${fileName}`, import.meta.url)
    const source = readFileSync(fileURLToPath(filePath), 'utf8')

    for (const match of source.matchAll(/<PageHeader\b[^>]*\bback-to=['"]([^'"]+)['"]/g)) {
      const backTo = match[1]
      assert.equal(
        routePaths.has(backTo),
        true,
        `${fileName} PageHeader back-to="${backTo}" is not registered in ${srcDir}router/index.js`
      )
    }
  }
})

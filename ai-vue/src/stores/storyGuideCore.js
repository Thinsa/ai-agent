const ENDING_ORDER = ['waiting', 'good', 'bittersweet', 'true']

export function getStoryEndings(scriptData) {
  return Object.values(scriptData.scenes)
    .filter(scene => scene.isEnding)
    .sort((a, b) => ENDING_ORDER.indexOf(a.endingType) - ENDING_ORDER.indexOf(b.endingType))
    .map(scene => ({
      id: scene.id,
      type: scene.endingType,
      title: scene.endingTitle
    }))
}

export function getGuideRecommendation(scriptData, sceneId, targetEndingId) {
  const scenes = scriptData.scenes
  if (!sceneId || !targetEndingId || !scenes[sceneId] || !scenes[targetEndingId]) {
    return unreachable(scriptData, targetEndingId)
  }
  if (sceneId === targetEndingId) {
    return {
      reachable: true,
      completed: true,
      targetEnding: endingInfo(scriptData, targetEndingId),
      distance: 0
    }
  }

  const scene = scenes[sceneId]
  if (!scene.choices || scene.choices.length === 0) {
    return unreachable(scriptData, targetEndingId)
  }

  const visited = new Set([sceneId])
  const queue = scene.choices.map((choice, choiceIndex) => ({
    sceneId: choice.next,
    firstChoiceIndex: choiceIndex,
    firstChoiceText: choice.text,
    distance: 1
  }))

  while (queue.length > 0) {
    const current = queue.shift()
    if (!current.sceneId || visited.has(current.sceneId)) {
      continue
    }
    visited.add(current.sceneId)

    if (current.sceneId === targetEndingId) {
      const firstChoice = scene.choices[current.firstChoiceIndex]
      return {
        reachable: true,
        completed: false,
        targetEnding: endingInfo(scriptData, targetEndingId),
        choiceIndex: current.firstChoiceIndex,
        choiceNumber: String(current.firstChoiceIndex + 1),
        choiceText: current.firstChoiceText,
        nextSceneId: firstChoice.next,
        distance: current.distance
      }
    }

    const nextScene = scenes[current.sceneId]
    for (const choice of nextScene?.choices || []) {
      queue.push({
        sceneId: choice.next,
        firstChoiceIndex: current.firstChoiceIndex,
        firstChoiceText: current.firstChoiceText,
        distance: current.distance + 1
      })
    }
  }

  return unreachable(scriptData, targetEndingId)
}

export function getGuideStatus(scriptData, sceneId, targetEndingId) {
  if (!sceneId) {
    return { state: 'not_started' }
  }
  if (!targetEndingId) {
    return { state: 'no_target' }
  }

  const recommendation = getGuideRecommendation(scriptData, sceneId, targetEndingId)
  if (!recommendation.reachable) {
    return { state: 'unreachable', recommendation }
  }
  if (recommendation.completed) {
    return { state: 'completed', recommendation }
  }
  return { state: 'reachable', recommendation }
}

export function createGuideHint(scriptData, sceneId, targetEndingId) {
  const status = getGuideStatus(scriptData, sceneId, targetEndingId)
  const targetTitle = endingInfo(scriptData, targetEndingId)?.title

  if (status.state === 'not_started') {
    return '灵语 Spark 轻敲桥栏：先输入「开始」，桥灯才会照出命运的方向。'
  }
  if (status.state === 'no_target') {
    return '灵语 Spark 低声提醒：先在引导面板中选择想抵达的结局，我再替你拨亮下一盏桥灯。'
  }
  if (status.state === 'unreachable') {
    return `灵语 Spark 合上命簿：从当前分支已无法抵达「${targetTitle}」，若仍想追寻它，可以重新开始。`
  }
  if (status.state === 'completed') {
    return `灵语 Spark 轻轻一笑：桥灯已经把你送到「${targetTitle}」。`
  }

  const recommendation = status.recommendation
  return `灵语 Spark 悄悄拨亮桥灯：若你想走向「${targetTitle}」，下一步更适合选择「${recommendation.choiceText}」。`
}

function endingInfo(scriptData, endingId) {
  const scene = scriptData.scenes[endingId]
  if (!scene?.isEnding) {
    return null
  }
  return {
    id: scene.id,
    type: scene.endingType,
    title: scene.endingTitle
  }
}

function unreachable(scriptData, targetEndingId) {
  return {
    reachable: false,
    completed: false,
    targetEnding: endingInfo(scriptData, targetEndingId)
  }
}

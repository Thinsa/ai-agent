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

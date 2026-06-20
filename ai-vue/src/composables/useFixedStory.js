import { ref, computed } from 'vue'
import { startStory, chooseOption, getSaveData, loadFromSave, resetStory, isAtEnding, getEndingTitle, getScriptMeta, getCurrentScene } from '../stores/fixedStoryStore'
import { createStorySave, listStorySaves, deleteStorySave } from '../api'

export function useFixedStory() {
    const savesList = ref([])
    const _storySaves = ref([])

    const scriptTitle = computed(() => getScriptMeta().title)

    const start = (storyId) => {
        resetStory()
        startStory(storyId)
    }

    const choose = (choiceIndex) => {
        chooseOption(choiceIndex)
    }

    const save = async () => {
        const data = getSaveData()
        // getSaveData() already returns choiceHistory as a JSON string
        await createStorySave(data.storyId, data.sceneId, data.choiceHistory)
    }

    const loadSaves = async () => {
        savesList.value = await listStorySaves()
    }

    const loadSave = async (saveItem) => {
        loadFromSave(saveItem)
    }

    const remove = async (saveId) => {
        await deleteStorySave(saveId)
        await loadSaves()
    }

    return {
        savesList,
        _storySaves,
        scriptTitle,
        start,
        choose,
        save,
        loadSaves,
        loadSave,
        remove,
        isAtEnding: () => isAtEnding(),
        getEndingTitle: () => getEndingTitle(),
        getCurrentScene: () => getCurrentScene(),
        getSaveData: () => getSaveData(),
        resetStory: () => resetStory(),
    }
}

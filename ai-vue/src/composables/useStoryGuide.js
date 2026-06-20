import { computed, ref } from 'vue'
import { createGuideHint, getStoryEndings } from '../stores/storyGuide'

export function useStoryGuide(getCurrentSceneId) {
    const guideEnabled = ref(false)
    const targetEndingId = ref('')
    const guideOpen = ref(false)
    const storyEndings = getStoryEndings()

    const selectedEndingTitle = computed(() => {
        return storyEndings.find(e => e.id === targetEndingId.value)?.title || ''
    })

    const toggleGuide = () => {
        guideOpen.value = !guideOpen.value
    }

    const setGuideEnabled = (enabled) => {
        guideEnabled.value = enabled
    }

    const setTargetEndingId = (id) => {
        targetEndingId.value = id
    }

    const getHint = () => {
        if (!guideEnabled.value || !getCurrentSceneId) return null
        const sceneId = getCurrentSceneId()
        if (!sceneId) return null
        return createGuideHint(sceneId, targetEndingId.value)
    }

    return {
        guideEnabled,
        targetEndingId,
        guideOpen,
        storyEndings,
        selectedEndingTitle,
        toggleGuide,
        setGuideEnabled,
        setTargetEndingId,
        getHint,
    }
}

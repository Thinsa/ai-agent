import { ref, onBeforeUnmount } from 'vue'

export function useSseChat() {
    const connectionStatus = ref('disconnected')
    const streamPaused = ref(false)
    const currentMessage = ref('')
    let eventSource = null

    const closeStream = () => {
        if (eventSource) {
            eventSource.close()
            eventSource = null
        }
    }

    const stopGeneration = () => {
        closeStream()
        connectionStatus.value = 'disconnected'
        streamPaused.value = true
    }

    /**
     * Start SSE stream. Returns the EventSource so the caller can attach
     * custom onmessage/onerror handlers (each view has different parsing logic).
     * @param {string} url - the SSE endpoint URL (caller builds it)
     * @returns {EventSource}
     */
    const startStream = (url) => {
        closeStream()
        streamPaused.value = false
        currentMessage.value = ''
        connectionStatus.value = 'connected'
        eventSource = new EventSource(url)
        return eventSource
    }

    onBeforeUnmount(() => closeStream())

    return {
        connectionStatus,
        streamPaused,
        currentMessage,
        startStream,
        stopGeneration,
        closeStream,
    }
}

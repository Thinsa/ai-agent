import axios from 'axios'

const API_BASE_URL = import.meta.env.PROD
  ? '/api'
  : 'http://localhost:8123/api'

const TOKEN_KEY = 'yun_ai_token'
const TOKEN_EXPIRES_AT_KEY = 'yun_ai_token_expires_at'
const USER_KEY = 'yun_ai_user'

export const getStoredToken = () => localStorage.getItem(TOKEN_KEY)
export const getStoredTokenExpiresAt = () => {
  const value = localStorage.getItem(TOKEN_EXPIRES_AT_KEY)
  return value ? Number(value) : 0
}
export const isStoredTokenExpired = () => {
  const expiresAt = getStoredTokenExpiresAt()
  return Boolean(expiresAt && Date.now() >= expiresAt)
}
export const setStoredToken = (token, expiresAt) => {
  if (token) {
    localStorage.setItem(TOKEN_KEY, token)
    if (expiresAt) {
      localStorage.setItem(TOKEN_EXPIRES_AT_KEY, String(expiresAt))
    }
  } else {
    localStorage.removeItem(TOKEN_KEY)
    localStorage.removeItem(TOKEN_EXPIRES_AT_KEY)
  }
}
export const clearStoredAuth = () => {
  setStoredToken(null)
  localStorage.removeItem(USER_KEY)
}

const request = axios.create({
  baseURL: API_BASE_URL,
  timeout: 60000
})

request.interceptors.request.use(config => {
  const token = getStoredToken()
  if (token && isStoredTokenExpired()) {
    setStoredToken(null)
  } else if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

request.interceptors.response.use(
  response => response,
  error => {
    if (error.response && error.response.status === 401) {
      clearStoredAuth()
    }
    return Promise.reject(error)
  }
)

export const register = (payload) => request.post('/user/register', payload).then(res => res.data)
export const login = (payload) => request.post('/user/login', payload).then(res => res.data)
export const validateToken = () => request.get('/user/token/validate').then(res => res.data)
export const getCurrentUser = () => request.get('/user/current').then(res => res.data)
export const updateProfile = (payload) => request.put('/user/profile', payload).then(res => res.data)
export const uploadAvatar = (file) => {
  const formData = new FormData()
  formData.append('file', file)
  return request.post('/user/avatar', formData).then(res => res.data)
}
export const logout = () => request.post('/user/logout').then(res => res.data)

// 背景管理
export const fetchBackgrounds = () => request.get('/backgrounds').then(res => res.data)
export const saveBackground = (agentKey, formData) =>
  request.post(`/backgrounds/${agentKey}`, formData).then(res => res.data)
export const updateBackgroundOpacity = (agentKey, opacity) =>
  request.post(`/backgrounds/${agentKey}/opacity`, { opacity }).then(res => res.data)
export const deleteBackground = (agentKey) =>
  request.delete(`/backgrounds/${agentKey}`).then(res => res.data)

// 固定剧本存档
export const createStorySave = (storyId, sceneId, choiceHistory) =>
  request.post('/story-saves', { storyId, sceneId, choiceHistory }).then(res => res.data)
export const listStorySaves = () =>
  request.get('/story-saves').then(res => res.data)
export const deleteStorySave = (saveId) =>
  request.delete(`/story-saves/${saveId}`).then(res => res.data)

// 文件上传
export const uploadFile = (file) => {
  const formData = new FormData()
  formData.append('file', file)
  return request.post('/ai/upload', formData).then(res => res.data)
}

// 知识库文档管理
export const listKnowledgeDocuments = (enabledOnly = false) =>
  request.get('/knowledge-documents', { params: { enabledOnly } }).then(res => res.data)

export const getKnowledgeDocumentCount = () =>
  request.get('/knowledge-documents/count').then(res => res.data)

export const getKnowledgeCategories = () =>
  request.get('/knowledge-documents/categories').then(res => res.data)

export const createKnowledgeDocument = (data) =>
  request.post('/knowledge-documents', data).then(res => res.data)

export const updateKnowledgeDocument = (id, data) =>
  request.put(`/knowledge-documents/${id}`, data).then(res => res.data)

export const deleteKnowledgeDocument = (id) =>
  request.delete(`/knowledge-documents/${id}`).then(res => res.data)

export const reindexKnowledgeDocuments = () =>
  request.post('/knowledge-documents/reindex').then(res => res.data)

export const connectSSE = (url, params = {}, onMessage, onError) => {
  const token = getStoredToken()
  const mergedParams = token ? { ...params, token } : params
  const queryString = Object.keys(mergedParams)
    .filter(key => mergedParams[key] !== undefined && mergedParams[key] !== null)
    .map(key => `${encodeURIComponent(key)}=${encodeURIComponent(mergedParams[key])}`)
    .join('&')

  const fullUrl = `${API_BASE_URL}${url}${queryString ? `?${queryString}` : ''}`
  const eventSource = new EventSource(fullUrl)

  eventSource.onmessage = event => {
    if (onMessage) onMessage(event.data)
  }

  eventSource.onerror = error => {
    if (onError) onError(error)
    eventSource.close()
  }

  return eventSource
}

export const chatWithLoveApp = (message, chatId, imageUrl) => {
  const params = { message, chatId }
  if (imageUrl) params.imageUrl = imageUrl
  return connectSSE('/ai/love_app/chat/sse', params)
}

export const chatWithLoveAppMcp = (message, chatId) => {
  return request.get('/ai/love_app/chat/mcp', { params: { message, chatId } }).then(res => res.data)
}

export const chatWithLoveAppRag = (message, chatId) => {
  return request.get('/ai/love_app/chat/rag', { params: { message, chatId } }).then(res => res.data)
}

export const chatBasic = (message, chatId, onMessage, onError) => {
  return connectSSE('/ai/chat/sse', { message, chatId }, onMessage, onError)
}

export const chatStory = (message, chatId, onMessage, onError) => {
  return connectSSE('/ai/story/sse', { message, chatId }, onMessage, onError)
}

export const chatWithManus = (message, chatId, imageUrl) => {
  const params = { message, chatId }
  if (imageUrl) params.imageUrl = imageUrl
  return connectSSE('/ai/manus/chat', params)
}

export const chatWithManusMcp = (message, chatId, onMessage, onError) => {
  return connectSSE('/ai/manus/chat/mcp', { message, chatId }, onMessage, onError)
}

export const createChatSession = (module, chatId) => {
  return request.post('/ai/history/sessions', { module, chatId }).then(res => res.data)
}

export const listChatSessions = (module) => {
  return request.get('/ai/history/sessions', { params: { module } }).then(res => res.data)
}

export const getChatHistory = (chatId, module) => {
  return request.get(`/ai/history/sessions/${encodeURIComponent(chatId)}`, {
    params: module ? { module } : {}
  }).then(res => res.data)
}

export const deleteChatSession = (chatId, module) => {
  return request.delete(`/ai/history/sessions/${encodeURIComponent(chatId)}`, {
    params: { module }
  }).then(res => res.data)
}

export default {
  request,
  register,
  login,
  getStoredTokenExpiresAt,
  isStoredTokenExpired,
  clearStoredAuth,
  validateToken,
  getCurrentUser,
  updateProfile,
  uploadAvatar,
  logout,
  chatWithLoveApp,
  chatWithLoveAppMcp,
  chatWithLoveAppRag,
  chatWithManus,
  chatWithManusMcp,
  chatStory,
  createChatSession,
  listChatSessions,
  getChatHistory,
  deleteChatSession,
  createStorySave,
  listStorySaves,
  deleteStorySave,
  uploadFile,
  listKnowledgeDocuments,
  getKnowledgeDocumentCount,
  getKnowledgeCategories,
  createKnowledgeDocument,
  updateKnowledgeDocument,
  deleteKnowledgeDocument,
  reindexKnowledgeDocuments
}

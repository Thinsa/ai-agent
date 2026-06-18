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
  return request.post('/user/avatar', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  }).then(res => res.data)
}
export const logout = () => request.post('/user/logout').then(res => res.data)

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

export const chatWithLoveApp = (message, chatId) => {
  return connectSSE('/ai/love_app/chat/sse', { message, chatId })
}

export const chatWithLoveAppMcp = (message, chatId) => {
  return request.get('/ai/love_app/chat/mcp', { params: { message, chatId } }).then(res => res.data)
}

export const chatWithLoveAppRag = (message, chatId) => {
  return request.get('/ai/love_app/chat/rag', { params: { message, chatId } }).then(res => res.data)
}

export const chatWithManus = (message, chatId) => {
  return connectSSE('/ai/manus/chat', { message, chatId })
}

export const chatWithManusMcp = (message, chatId, onMessage, onError) => {
  return connectSSE('/ai/manus/chat/mcp', { message, chatId }, onMessage, onError)
}

export const listChatSessions = (module) => {
  return request.get('/ai/history/sessions', { params: { module } }).then(res => res.data)
}

export const getChatHistory = (chatId) => {
  return request.get(`/ai/history/sessions/${encodeURIComponent(chatId)}`).then(res => res.data)
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
  listChatSessions,
  getChatHistory
}

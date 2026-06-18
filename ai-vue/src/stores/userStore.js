import { computed, reactive } from 'vue'
import { resetBackgrounds } from './bgStore'
import {
  clearStoredAuth,
  getStoredToken,
  getStoredTokenExpiresAt,
  isStoredTokenExpired,
  login as loginApi,
  logout as logoutApi,
  register as registerApi,
  setStoredToken,
  updateProfile,
  uploadAvatar as uploadAvatarApi,
  validateToken
} from '../api'

const USER_KEY = 'yun_ai_user'

const getStoredUser = () => {
  try {
    const value = localStorage.getItem(USER_KEY)
    return value ? JSON.parse(value) : null
  } catch (error) {
    localStorage.removeItem(USER_KEY)
    return null
  }
}

const setStoredUser = (user) => {
  if (user) {
    localStorage.setItem(USER_KEY, JSON.stringify(user))
  } else {
    localStorage.removeItem(USER_KEY)
  }
}

const createAvatar = (name) => {
  const text = (name || '用户').trim().slice(0, 2) || '用户'
  const colors = ['#00b2ff', '#7c3aed', '#ff4d8d', '#14b8a6']
  const color = colors[text.charCodeAt(0) % colors.length]
  return {
    text,
    color
  }
}

const normalizeUser = (user) => {
  if (!user) return null
  const displayName = user.displayName || user.username || 'AI 用户'
  return {
    ...user,
    displayName,
    role: user.role || 'AI 应用体验官',
    bio: user.bio || '',
    avatarUrl: user.avatarUrl || '',
    avatar: createAvatar(displayName)
  }
}

const clearState = () => {
  clearStoredAuth()
  resetBackgrounds()
  state.token = null
  state.tokenExpiresAt = 0
  state.user = null
}

const state = reactive({
  token: getStoredToken(),
  tokenExpiresAt: getStoredTokenExpiresAt(),
  user: getStoredToken() && !isStoredTokenExpired() ? normalizeUser(getStoredUser()) : null,
  initialized: false
})

export const currentUser = computed(() => state.user)
export const isLoggedIn = computed(() => Boolean(state.token && !isStoredTokenExpired()))
export const authInitialized = computed(() => state.initialized)

export const restoreSession = async () => {
  if (!state.token) {
    state.initialized = true
    return null
  }
  if (isStoredTokenExpired()) {
    clearState()
    state.initialized = true
    return null
  }
  try {
    const validation = await validateToken()
    if (!validation.valid) {
      clearState()
      return null
    }
    state.tokenExpiresAt = validation.expiresAt || state.tokenExpiresAt
    state.user = normalizeUser(validation.user)
    if (validation.expiresAt) {
      setStoredToken(state.token, validation.expiresAt)
    }
    setStoredUser(validation.user)
    return state.user
  } catch (error) {
    const status = error?.response?.status
    if (status === 401 || status === 403) {
      clearState()
    }
    return null
  } finally {
    state.initialized = true
  }
}

export const register = async (payload) => {
  return registerApi(payload)
}

export const login = async ({ username, password }) => {
  const result = await loginApi({ username, password })
  if (!result?.token || !result?.user) {
    throw new Error('登录接口未返回有效令牌，请确认前端 /api 已代理到后端服务')
  }
  state.token = result.token
  state.tokenExpiresAt = result.expiresAt || 0
  setStoredToken(result.token, result.expiresAt)
  state.user = normalizeUser(result.user)
  setStoredUser(result.user)
  state.initialized = true
  return state.user
}

export const updateUser = async (profile) => {
  const updated = await updateProfile(profile)
  state.user = normalizeUser(updated)
  setStoredUser(updated)
  return state.user
}

export const uploadAvatar = async (file) => {
  const updated = await uploadAvatarApi(file)
  state.user = normalizeUser(updated)
  setStoredUser(updated)
  return state.user
}

export const logout = async () => {
  try {
    if (state.token) {
      await logoutApi()
    }
  } finally {
    clearState()
    state.initialized = true
  }
}

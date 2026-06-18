import { createRouter, createWebHistory } from 'vue-router'
import { authInitialized, currentUser, isLoggedIn, restoreSession } from '../stores/userStore'
import { loadBackgrounds } from '../stores/bgStore'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/Login.vue'),
    meta: {
      title: '登录 - LinkMind 灵桥',
      guestOnly: true
    }
  },
  {
    path: '/',
    name: 'Home',
    component: () => import('../views/Home.vue'),
    meta: {
      title: 'LinkMind 灵桥 — 智能 AI 对话平台',
      description: 'LinkMind 灵桥：知心 Soul 情感伴侣 + 极智 Core 全能助手。连接智慧，点亮灵感。',
      requiresAuth: true
    }
  },
  {
    path: '/background-settings',
    name: 'BackgroundSettings',
    component: () => import('../views/BackgroundSettings.vue'),
    meta: {
      title: '背景设置 - LinkMind 灵桥',
      requiresAuth: true
    }
  },
  {
    path: '/profile',
    name: 'UserProfile',
    component: () => import('../views/UserProfile.vue'),
    meta: {
      title: '用户信息 - LinkMind 灵桥',
      requiresAuth: true
    }
  },
  {
    path: '/love-master',
    name: 'LoveMaster',
    component: () => import('../views/LoveMaster.vue'),
    meta: {
      title: '知心 Soul - LinkMind 灵桥',
      description: '知心 Soul 是 LinkMind 灵桥的情感伴侣，懂你心事，陪你聊天，解答各种情感困惑。',
      requiresAuth: true
    }
  },
  {
    path: '/chat',
    name: 'ChatView',
    component: () => import('../views/ChatView.vue'),
    meta: {
      title: '灵语 Spark 互动剧本 - LinkMind 灵桥',
      description: '灵语 Spark 是 LinkMind 灵桥的互动剧本主持人，为你展开分支故事，每个选择都通往不同的结局。',
      requiresAuth: true
    }
  },
  {
    path: '/super-agent',
    name: 'SuperAgent',
    component: () => import('../views/SuperAgent.vue'),
    meta: {
      title: '极智 Core - LinkMind 灵桥',
      description: '极智 Core 是 LinkMind 灵桥的全能助手，调用工具、联网搜索、ReAct 推理，解决各类专业问题。',
      requiresAuth: true
    }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach(async (to, from, next) => {
  if (to.meta.title) {
    document.title = to.meta.title
  }

  if (!authInitialized.value) {
    await restoreSession()
  }

  if (to.meta.requiresAuth && !isLoggedIn.value) {
    next('/login')
    return
  }

  if (isLoggedIn.value) {
    loadBackgrounds()
  }

  if (to.meta.guestOnly && isLoggedIn.value && currentUser.value) {
    next('/')
    return
  }

  next()
})

export default router

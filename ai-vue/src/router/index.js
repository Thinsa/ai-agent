import { createRouter, createWebHistory } from 'vue-router'
import { authInitialized, currentUser, isLoggedIn, restoreSession } from '../stores/userStore'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/Login.vue'),
    meta: {
      title: '登录 - AI超级智能体应用平台',
      guestOnly: true
    }
  },
  {
    path: '/',
    name: 'Home',
    component: () => import('../views/Home.vue'),
    meta: {
      title: '首页 - AI超级智能体应用平台',
      description: 'AI超级智能体应用平台提供AI恋爱大师和AI超级智能体服务，满足您的各种AI对话需求',
      requiresAuth: true
    }
  },
  {
    path: '/profile',
    name: 'UserProfile',
    component: () => import('../views/UserProfile.vue'),
    meta: {
      title: '用户信息 - AI超级智能体应用平台',
      requiresAuth: true
    }
  },
  {
    path: '/love-master',
    name: 'LoveMaster',
    component: () => import('../views/LoveMaster.vue'),
    meta: {
      title: 'AI恋爱大师 - AI超级智能体应用平台',
      description: 'AI恋爱大师是AI超级智能体应用平台的专业情感顾问，帮你解答各种恋爱问题，提供情感建议',
      requiresAuth: true
    }
  },
  {
    path: '/super-agent',
    name: 'SuperAgent',
    component: () => import('../views/SuperAgent.vue'),
    meta: {
      title: 'AI超级智能体 - AI超级智能体应用平台',
      description: 'AI超级智能体是AI超级智能体应用平台的全能助手，能解答各类专业问题，提供精准建议和解决方案',
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

  if (to.meta.guestOnly && isLoggedIn.value && currentUser.value) {
    next('/')
    return
  }

  next()
})

export default router

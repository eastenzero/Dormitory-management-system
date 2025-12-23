import { createRouter, createWebHistory } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '../stores/auth'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/',
      redirect: '/dashboard',
    },
    {
      path: '/login',
      component: () => import('../views/LoginView.vue'),
    },
    {
      path: '/register',
      component: () => import('../views/RegisterView.vue'),
    },
    {
      path: '/',
      component: () => import('../layout/MainLayout.vue'),
      meta: { requiresAuth: true },
      children: [
        {
          path: 'dashboard',
          component: () => import('../views/dashboard/index.vue'),
        },
        {
          path: 'users',
          component: () => import('../views/UsersView.vue'),
          meta: { permission: 'sys:user:read' },
        },
        {
          path: 'dorm/buildings',
          component: () => import('../views/DormBuildingsView.vue'),
          meta: { permission: 'dorm:building:read' },
        },
        {
          path: 'dorm/rooms',
          component: () => import('../views/DormRoomsView.vue'),
          meta: { permission: 'dorm:room:read' },
        },
        {
          path: 'dorm/beds',
          component: () => import('../views/DormBedsView.vue'),
          meta: { permission: 'dorm:bed:read' },
        },
        {
          path: 'students',
          component: () => import('../views/StudentsView.vue'),
          meta: { permission: 'student:read' },
        },
        {
          path: 'assignments',
          component: () => import('../views/DormAssignmentsView.vue'),
          meta: { permission: 'dorm:assignment:read' },
        },
        {
          path: 'repairs',
          component: () => import('../views/RepairsView.vue'),
          meta: { permission: 'repair:order:read' },
        },
        {
          path: 'visitors',
          component: () => import('../views/VisitorsView.vue'),
          meta: { permission: 'visitor:record:read' },
        },
        {
          path: '403',
          component: () => import('../views/ForbiddenView.vue'),
        },
      ]
    },
  ],
})

router.beforeEach(async (to) => {
  const auth = useAuthStore()

  if (to.path === '/login' || to.path === '/register') {
    if (auth.isLoggedIn) {
      return { path: '/dashboard' }
    }
    return true
  }

  const requiresAuth = Boolean(to.meta.requiresAuth)
  if (requiresAuth && !auth.isLoggedIn) {
    return { path: '/login' }
  }

  if (requiresAuth && auth.isLoggedIn && !auth.me) {
    try {
      await Promise.race([
        auth.ensureMe(),
        new Promise((_, reject) => setTimeout(() => reject(new Error('fetchMe timeout')), 8000)),
      ])
    } catch (e) {
      auth.logout()
      return { path: '/login' }
    }
  }

  const permission = to.meta.permission as string | undefined
  if (permission && !auth.hasPerm(permission)) {
    ElMessage.warning('无权限访问该页面')
    return { path: '/403', query: { from: to.fullPath } }
  }

  return true
})

export default router

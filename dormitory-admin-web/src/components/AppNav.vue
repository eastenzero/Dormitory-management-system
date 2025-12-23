<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import {
  House,
  User,
  School,
  OfficeBuilding,
  Reading,
  ScaleToOriginal,
  Tools,
  Postcard
} from '@element-plus/icons-vue'

type NavItem = {
  path: string
  label: string
  permission?: string
  icon?: any
}

const router = useRouter()
const route = useRoute()
const auth = useAuthStore()

const items = computed<NavItem[]>(() => {
  const list: NavItem[] = [
    { path: '/dashboard', label: 'Dashboard', icon: House }
  ]

  if (auth.hasPerm('sys:user:read')) {
    list.push({ path: '/users', label: '用户管理', permission: 'sys:user:read', icon: User })
  }
  if (auth.hasPerm('dorm:building:read')) {
    list.push({ path: '/dorm/buildings', label: '楼栋管理', permission: 'dorm:building:read', icon: OfficeBuilding })
  }
  if (auth.hasPerm('dorm:room:read')) {
    list.push({ path: '/dorm/rooms', label: '房间管理', permission: 'dorm:room:read', icon: School })
  }
  if (auth.hasPerm('dorm:bed:read')) {
    list.push({ path: '/dorm/beds', label: '床位管理', permission: 'dorm:bed:read', icon: Reading })
  }
  if (auth.hasPerm('dorm:assignment:read')) {
    list.push({ path: '/assignments', label: '入住管理', permission: 'dorm:assignment:read', icon: ScaleToOriginal })
  }
  if (auth.hasPerm('repair:order:read')) {
    list.push({ path: '/repairs', label: '报修工单', permission: 'repair:order:read', icon: Tools })
  }
  if (auth.hasPerm('visitor:record:read')) {
    list.push({ path: '/visitors', label: '访客登记', permission: 'visitor:record:read', icon: Postcard })
  }

  return list
})

const activePath = computed(() => route.path)

const navTo = (path: string) => {
  router.push(path)
}
</script>

<template>
  <div class="app-nav">
    <div class="logo">
      🍏 宿管中心
    </div>
    <div class="menu-items">
      <div 
        v-for="item in items" 
        :key="item.path"
        class="nav-item"
        :class="{ active: activePath === item.path }"
        @click="navTo(item.path)"
      >
        <el-icon v-if="item.icon" size="18"><component :is="item.icon" /></el-icon>
        <span>{{ item.label }}</span>
      </div>
    </div>
  </div>
</template>

<style scoped>
.app-nav {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 100%;
}

.logo {
  font-size: 1.5rem;
  font-weight: 800;
  color: var(--color-primary);
  display: flex;
  align-items: center;
  gap: 8px;
}

.menu-items {
  display: flex;
  gap: 12px;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  border-radius: 20px;
  cursor: pointer;
  font-weight: 600;
  color: var(--color-text-light);
  transition: all 0.3s cubic-bezier(0.175, 0.885, 0.32, 1.275);
  border: 2px solid transparent;
}

.nav-item:hover {
  background-color: var(--el-color-primary-light-9);
  transform: translateY(-2px);
  color: var(--color-primary);
}

.nav-item.active {
  background-color: var(--color-primary);
  color: white;
  box-shadow: 0 4px 10px rgba(126, 217, 87, 0.4);
  transform: scale(1.05);
}

.nav-item.active:hover {
  transform: scale(1.05) translateY(-2px);
}
</style>


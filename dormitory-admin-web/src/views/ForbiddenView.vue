<script setup lang="ts">
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const router = useRouter()
const route = useRoute()
const auth = useAuthStore()

const back = async () => {
  await router.replace('/dashboard')
}
</script>

<template>
  <div style="padding: 16px;">
    <el-card style="margin-top: 16px;">
      <template #header>
        <div style="display:flex; align-items:center; justify-content: space-between;">
          <div>无权限</div>
          <el-button @click="back">返回首页</el-button>
        </div>
      </template>

      <el-result icon="warning" title="无权限访问该页面" sub-title="请联系管理员开通权限后再试。">
        <template #extra>
          <div style="display:flex; gap: 8px; align-items:center;">
            <el-tag type="info">当前用户：{{ auth.me?.username }}</el-tag>
            <el-tag v-if="route.query.from" type="info">from: {{ route.query.from }}</el-tag>
          </div>
        </template>
      </el-result>
    </el-card>
  </div>
</template>

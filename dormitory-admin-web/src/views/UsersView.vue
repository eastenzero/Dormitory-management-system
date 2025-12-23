<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import * as sysApi from '../api/sys'

const loading = ref(false)
const users = ref<sysApi.SysUser[]>([])

onMounted(async () => {
  loading.value = true
  try {
    const resp = await sysApi.listUsers()
    if (resp.code !== 0) {
      ElMessage.error(resp.message || '加载失败')
      return
    }
    users.value = resp.data
  } catch (e) {
    return
  } finally {
    loading.value = false
  }
})
</script>

<template>
  <div class="page-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <div class="title">用户管理</div>
          <!-- <el-button @click="$router.push('/dashboard')">返回</el-button> -->
        </div>
      </template>

      <el-table :data="users" v-loading="loading" style="width: 100%" stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="username" label="用户名" />
        <el-table-column prop="realName" label="姓名" />
        <el-table-column prop="status" label="状态" width="120">
            <template #default="scope">
                <el-tag :type="scope.row.status === 'enable' ? 'success' : 'danger'" effect="dark" round>
                    {{ scope.row.status }}
                </el-tag>
            </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<style scoped>
.page-container {
    animation: fadeIn 0.5s ease-in-out;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.title {
    font-size: 1.1rem;
    font-weight: 700;
    color: var(--color-primary-dark);
}

@keyframes fadeIn {
    from { opacity: 0; transform: translateY(10px); }
    to { opacity: 1; transform: translateY(0); }
}
</style>


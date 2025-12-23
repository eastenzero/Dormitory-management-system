<script setup lang="ts">
import type { TodoItem } from '../../../api/dashboard'

const props = defineProps<{
  todos: TodoItem[]
  loading: boolean
}>()

const getPriorityTag = (p: string) => {
    switch(p) {
        case 'High': return 'danger'
        case 'Medium': return 'warning'
        case 'Low': return 'info'
        default: return ''
    }
}

const getPriorityLabel = (p: string) => {
    switch (p) {
        case 'High': return '高'
        case 'Medium': return '中'
        case 'Low': return '低'
        default: return p
    }
}
</script>

<template>
  <div class="dashboard-list-card">
    <div class="list-header">
        <div class="title">待办事项</div>
        <el-button link type="primary" size="small">查看全部</el-button>
    </div>
    
    <el-table :data="todos" style="width: 100%" :show-header="true" size="small">
      <el-table-column prop="title" label="任务" min-width="150" />
      <el-table-column prop="priority" label="优先级" width="100">
        <template #default="scope">
            <el-tag :type="getPriorityTag(scope.row.priority)" size="small" effect="light">{{ getPriorityLabel(scope.row.priority) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="age" label="等待时长" width="100">
          <template #default="scope">
              <span :class="{ 'text-danger': scope.row.age.includes('d') }">{{ scope.row.age }}</span>
          </template>
      </el-table-column>
       <el-table-column label="操作" width="80" align="center">
           <template #default>
               <el-button link type="primary" size="small">处理</el-button>
           </template>
       </el-table-column>
    </el-table>
  </div>
</template>

<style scoped>
.dashboard-list-card {
     background: white;
    border-radius: 8px;
    padding: 16px;
    height: 100%;
    box-shadow: 0 1px 2px 0 rgba(0, 0, 0, 0.05);
}

.list-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 12px;
}

.title {
    font-weight: 600;
}

.text-danger {
    color: var(--el-color-danger);
    font-weight: 600;
}
</style>

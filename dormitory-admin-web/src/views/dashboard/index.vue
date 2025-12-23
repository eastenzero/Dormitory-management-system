<script setup lang="ts">
import { onMounted, ref } from 'vue'
import DashboardKPI from './components/DashboardKPI.vue'
import DashboardTrends from './components/DashboardTrends.vue'
import DashboardDistribution from './components/DashboardDistribution.vue'
import DashboardTodos from './components/DashboardTodos.vue'
import DashboardAlerts from './components/DashboardAlerts.vue'
import { 
    getDashboardSummary, 
    getDashboardTrends, 
    getDashboardDistribution,
    getDashboardTodos,
    getDashboardAlerts, 
    getDashboardNotices,
    type DashboardSummary,
    type DashboardTrends as TrendsType,
    type DashboardDistribution as DistType,
    type TodoItem,
    type AlertItem,
    type NoticeItem
} from '../../api/dashboard'

const loading = ref(true)
const summary = ref<DashboardSummary | null>(null)
const trends = ref<TrendsType | null>(null)
const distribution = ref<DistType | null>(null)
const todos = ref<TodoItem[]>([])
const alerts = ref<AlertItem[]>([])
const notices = ref<NoticeItem[]>([])
const dateRange = ref('7d')

const fetchData = async () => {
    loading.value = true
    try {
        const [sumRes, trRes, distRes, todoRes, alertRes, noteRes] = await Promise.all([
            getDashboardSummary(),
            getDashboardTrends(dateRange.value),
            getDashboardDistribution(),
            getDashboardTodos(),
            getDashboardAlerts(),
            getDashboardNotices()
        ])
        
        summary.value = sumRes
        trends.value = trRes
        distribution.value = distRes
        todos.value = todoRes
        alerts.value = alertRes
        notices.value = noteRes
    } finally {
        loading.value = false
    }
}

onMounted(() => {
    fetchData()
})
</script>

<template>
  <div class="dashboard-container">
      <!-- Header -->
      <div class="dashboard-header">
          <div class="left">
              <h2 class="page-title">仪表盘</h2>
              <span class="update-time">最后更新：{{ new Date().toLocaleString() }}</span>
          </div>
          <div class="right">
              <el-radio-group v-model="dateRange" @change="fetchData">
                  <el-radio-button label="today">今日</el-radio-button>
                  <el-radio-button label="7d">近7天</el-radio-button>
                  <el-radio-button label="30d">近30天</el-radio-button>
              </el-radio-group>
              <el-button type="primary" :loading="loading" @click="fetchData" circle icon="Refresh" />
          </div>
      </div>

      <!-- Row 1: KPI Cards -->
      <div class="grid-row kpi-row" v-if="summary">
          <DashboardKPI 
            title="入住率" 
            :value="summary.occupancyRate * 100 + '%'" 
            :sub-value="summary.activeAssignments + '/' + summary.totalBeds"
            sub-label="在住/总床位"
            trend="up" trend-value="1.2%"
            type="primary"
          />
          <DashboardKPI 
             title="今日办理"
             :value="summary.todayCheckinCount"
             sub-label="今日退宿"
             :sub-value="summary.todayCheckoutCount"
             trend="neutral" trend-value="-"
             type="success"
          />
          <DashboardKPI 
             title="维修工单"
             :value="summary.repairOpenCount"
             sub-label="处理中"
             :sub-value="summary.repairInProgress"
             trend="down" trend-value="2"
             type="warning"
          />
          <DashboardKPI 
             title="风险提醒"
             :value="summary.alertsCount"
             sub-label="高优先级"
             sub-value="1"
             trend="up" trend-value="1"
             type="danger"
          />
      </div>

      <div class="grid-row kpi-row" v-else>
          <div v-for="i in 4" :key="i" class="dashboard-kpi-skeleton">
              <el-skeleton animated>
                  <template #template>
                      <el-skeleton-item variant="text" style="width: 55%" />
                      <el-skeleton-item variant="h1" style="width: 35%; margin-top: 10px;" />
                      <el-skeleton-item variant="text" style="width: 80%; margin-top: 14px;" />
                  </template>
              </el-skeleton>
          </div>
      </div>

      <!-- Row 2: Charts -->
      <div class="grid-row charts-row">
          <div class="col-8">
              <DashboardTrends :data="trends" :loading="loading" />
          </div>
          <div class="col-4">
              <DashboardDistribution :data="distribution" :loading="loading" />
          </div>
      </div>

      <!-- Row 3: Lists -->
      <div class="grid-row lists-row">
          <div class="col-8">
              <DashboardTodos :todos="todos" :loading="loading" />
          </div>
          <div class="col-4">
              <DashboardAlerts :alerts="alerts" :notices="notices" />
          </div>
      </div>
  </div>
</template>

<style scoped>
.dashboard-container {
    display: flex;
    flex-direction: column;
    gap: 20px;
    max-width: 100%;
    width: 100%;
    box-sizing: border-box;
    padding: 16px 24px;
}

.dashboard-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    gap: 12px;
    flex-wrap: wrap;
}

.left {
    display: flex;
    align-items: baseline;
    gap: 10px;
}

.page-title {
    margin: 0;
    font-size: 1.5rem;
    color: var(--el-text-color-primary);
}

.update-time {
    font-size: 0.8rem;
    color: var(--el-text-color-secondary);
    margin-left: 0;
}

.right {
    display: flex;
    gap: 12px;
    align-items: center;
    flex-wrap: wrap;
}

/* Grid System Simulation */
.grid-row {
    display: grid;
    gap: 20px;
    align-items: stretch;
}

.charts-row {
    grid-template-columns: minmax(0, 2fr) minmax(0, 1fr);
}

.lists-row {
    grid-template-columns: minmax(0, 2fr) minmax(0, 1fr);
}

.col-8 {
    min-width: 0;
    display: flex;
    flex-direction: column;
}

.col-4 {
    min-width: 0;
    display: flex;
    flex-direction: column;
}

.kpi-row {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
    gap: 20px;
    align-items: start;
}

.dashboard-kpi-skeleton {
    background: white;
    border-radius: 8px;
    padding: 16px;
    border: 1px solid var(--el-border-color-lighter);
    box-shadow: 0 1px 2px 0 rgba(0, 0, 0, 0.05);
    min-height: 132px;
    display: flex;
    flex-direction: column;
    justify-content: center;
}

@media (max-width: 1000px) {
    .charts-row { grid-template-columns: 1fr; }
    .lists-row { grid-template-columns: 1fr; }
}
</style>

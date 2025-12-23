<script setup lang="ts">
import { computed } from 'vue'

const props = defineProps<{
  title: string
  value: number | string
  unit?: string
  subValue?: string | number
  subLabel?: string
  trend?: 'up' | 'down' | 'neutral'
  trendValue?: string
  type?: 'primary' | 'success' | 'warning' | 'danger'
}>()

const cardClass = computed(() => {
    return {
        'dashboard-kpi-card': true,
        [`type-${props.type || 'primary'}`]: true
    }
})
</script>

<template>
  <div :class="cardClass">
    <div class="kpi-header">
      <span class="kpi-title">{{ title }}</span>
      <span v-if="unit" class="kpi-unit">{{ unit }}</span>
    </div>
    <div class="kpi-body">
      <div class="kpi-value">{{ value }}</div>
      <div v-if="trend" class="kpi-trend" :class="trend">
        <span v-if="trend === 'up'">↑</span>
        <span v-if="trend === 'down'">↓</span>
        {{ trendValue }}
      </div>
    </div>
    <div class="kpi-footer" v-if="subLabel">
      <span class="kpi-sub-label">{{ subLabel }}:</span>
      <span class="kpi-sub-value">{{ subValue }}</span>
    </div>
  </div>
</template>

<style scoped>
.dashboard-kpi-card {
  background: white;
  border-radius: 8px;
  padding: 16px;
  border: 1px solid var(--el-border-color-lighter);
  /* shadow-sm */
  box-shadow: 0 1px 2px 0 rgba(0, 0, 0, 0.05);
  transition: all 0.2s ease;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  height: auto;
  min-height: 132px;
  overflow: hidden;
}

.dashboard-kpi-card:hover {
    box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1), 0 2px 4px -1px rgba(0, 0, 0, 0.06);
    transform: translateY(-2px);
}

.kpi-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
  color: var(--el-text-color-secondary);
  font-size: 0.875rem;
}

.kpi-value {
    font-size: 1.8rem;
    font-weight: 700;
    color: var(--el-text-color-primary);
    line-height: 1.2;
}

.kpi-body {
    display: flex;
    align-items: flex-end;
    gap: 8px;
    margin-bottom: 8px;
}

.kpi-trend {
    font-size: 0.8rem;
    font-weight: 500;
    margin-bottom: 4px;
}

.kpi-trend.up { color: var(--el-color-success); }
.kpi-trend.down { color: var(--el-color-danger); }
.kpi-trend.neutral { color: var(--el-text-color-secondary); }

.kpi-footer {
    display: flex;
    align-items: center;
    flex-wrap: wrap;
    gap: 6px;
    font-size: 0.8rem;
    color: var(--el-text-color-secondary);
    border-top: 1px solid var(--el-border-color-lighter);
    padding-top: 8px;
}

.kpi-sub-label {
    min-width: 0;
}

.kpi-sub-value {
    margin-left: auto;
    min-width: 0;
    max-width: 100%;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
}

/* Accent Colors based on Type */
.type-primary .kpi-value { color: var(--el-color-primary); }
.type-danger .kpi-value { color: var(--el-color-danger); }
.type-warning .kpi-value { color: var(--el-color-warning); }
.type-success .kpi-value { color: var(--el-color-success); }

</style>

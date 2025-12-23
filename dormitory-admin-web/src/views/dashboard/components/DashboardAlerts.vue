<script setup lang="ts">
import type { AlertItem, NoticeItem } from '../../../api/dashboard'

defineProps<{
  alerts: AlertItem[]
  notices: NoticeItem[]
}>()

const getAlertColor = (severity: string) => {
    if (severity === 'error') {
        return 'var(--el-color-danger)'
    }
    if (severity === 'warning') {
        return 'var(--el-color-warning)'
    }
    return 'var(--el-color-info)'
}
</script>

<template>
  <div class="dashboard-right-col">
      <!-- Alerts -->
      <div class="section-card alerts-section">
          <div class="section-header">风险提醒</div>
          <div class="alert-list">
              <div v-for="alert in alerts" :key="alert.id" class="alert-item">
                  <div class="alert-icon" :style="{ color: getAlertColor(alert.severity) }">●</div>
                  <div class="alert-content">
                      <div class="alert-title">{{ alert.title }} <span class="badge" v-if="alert.count > 1">{{ alert.count }}</span></div>
                      <div class="alert-desc">{{ alert.description }}</div>
                  </div>
                   <el-icon class="arrow"><ArrowRight /></el-icon>
              </div>
          </div>
      </div>

      <!-- Notices -->
      <div class="section-card notice-section">
          <div class="section-header">
              <span>公告</span>
              <el-button link type="primary" size="small">新建</el-button>
          </div>
          <div class="notice-list">
              <div v-for="notice in notices" :key="notice.id" class="notice-item">
                  <el-tag size="small" :type="notice.pinned ? 'danger' : 'info'" effect="plain">
                      {{ notice.pinned ? '置顶' : '通知' }}
                  </el-tag>
                  <span class="notice-title">{{ notice.title }}</span>
                  <span class="notice-date">{{ notice.publishAt.slice(5) }}</span>
              </div>
          </div>
      </div>
  </div>
</template>

<style scoped>
.dashboard-right-col {
    display: flex;
    flex-direction: column;
    gap: 16px;
    height: 100%;
}

.section-card {
    background: white;
    border-radius: 8px;
    padding: 16px;
    box-shadow: 0 1px 2px 0 rgba(0, 0, 0, 0.05);
    flex: 1;
}

.section-header {
    font-weight: 600;
    margin-bottom: 12px;
    display: flex;
    justify-content: space-between;
}

.alert-item {
    display: flex;
    align-items: center;
    gap: 10px;
    padding: 8px;
    border-radius: 6px;
    background-color: var(--el-fill-color-light);
    margin-bottom: 8px;
    cursor: pointer;
    transition: background 0.2s;
}

.alert-item:hover {
    background-color: var(--el-fill-color);
}

.alert-content {
    flex: 1;
}

.alert-title {
    font-size: 0.9rem;
    font-weight: 500;
    display: flex;
    align-items: center;
    gap: 6px;
}

.badge {
    background: var(--el-color-danger);
    color: white;
    border-radius: 10px;
    padding: 0 6px;
    font-size: 0.7rem;
}

.alert-desc {
    font-size: 0.8rem;
    color: var(--el-text-color-secondary);
}

.notice-list {
    display: flex;
    flex-direction: column;
    gap: 10px;
}

.notice-item {
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: 0.9rem;
}

.notice-title {
    flex: 1;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
}

.notice-date {
    color: var(--el-text-color-secondary);
    font-size: 0.8rem;
}
</style>

<script setup lang="ts">
import { ref, onBeforeUnmount, onMounted, watch, nextTick } from 'vue'
import * as echarts from 'echarts'
import type { DashboardTrends } from '../../../api/dashboard'

const props = defineProps<{
  data: DashboardTrends | null
  loading: boolean
}>()

const chartRef = ref<HTMLElement | null>(null)
let chartInstance: echarts.ECharts | null = null
let resizeObserver: ResizeObserver | null = null

const activeTab = ref<'occupancy' | 'repair' | 'visitor'>('occupancy')

const initChart = () => {
    if (!chartRef.value) return

    chartInstance = echarts.init(chartRef.value)
    updateChart()
    chartInstance.resize()
    resizeObserver = new ResizeObserver(() => {
        chartInstance?.resize()
    })
    resizeObserver.observe(chartRef.value)
}

const updateChart = () => {
    if (!chartInstance || !props.data) return

    const option: any = {
        tooltip: {
            trigger: 'axis'
        },
        legend: {
            bottom: 0,
            icon: 'circle'
        },
        grid: {
            top: '10%',
            left: '3%',
            right: '4%',
            bottom: '15%',
            containLabel: true
        },
        xAxis: {
            type: 'category',
            boundaryGap: false,
            data: [], // To be filled
            axisLine: { lineStyle: { color: '#eee' } },
            axisLabel: { color: '#666' }
        },
        yAxis: {
            type: 'value',
            splitLine: { lineStyle: { color: '#f5f5f5' } }
        },
        series: []
    }

    if (activeTab.value === 'occupancy') {
        const d = props.data.occupancy
        option.xAxis.data = d.active.map(i => i.date)
        option.series = [
            {
                name: '在住人数',
                type: 'line',
                data: d.active.map(i => i.value),
                smooth: true,
                showSymbol: false,
                itemStyle: { color: '#7ed957' },
                areaStyle: {
                    color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                        { offset: 0, color: 'rgba(126, 217, 87, 0.5)' },
                        { offset: 1, color: 'rgba(126, 217, 87, 0.0)' }
                    ])
                }
            }
        ]
    } else if (activeTab.value === 'repair') {
        const d = props.data.repair
        option.xAxis.data = d.created.map(i => i.date)
        option.series = [
            {
                name: '新建',
                type: 'line',
                data: d.created.map(i => i.value),
                smooth: true,
                 itemStyle: { color: '#409EFF' }
            },
            {
                name: '完成',
                type: 'line',
                data: d.completed.map(i => i.value),
                 smooth: true,
                 itemStyle: { color: '#67C23A' }
            }
        ]
    } else if (activeTab.value === 'visitor') {
        const d = props.data.visitor
        option.xAxis.data = d.total.map(i => i.date)
        option.series = [
            {
                name: '访客量',
                type: 'line',
                data: d.total.map(i => i.value),
                smooth: true,
                itemStyle: { color: '#E6A23C' },
                 areaStyle: {
                    color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                        { offset: 0, color: 'rgba(230, 162, 60, 0.5)' },
                        { offset: 1, color: 'rgba(230, 162, 60, 0.0)' }
                    ])
                }
            }
        ]
    }

    chartInstance.setOption(option, true)
}

watch(() => props.data, () => {
    updateChart()
    nextTick(() => {
        chartInstance?.resize()
    })
})

watch(activeTab, () => {
    updateChart()
    nextTick(() => {
        chartInstance?.resize()
    })
})

onMounted(() => {
    nextTick(() => {
        initChart()
    })
})

onBeforeUnmount(() => {
    resizeObserver?.disconnect()
    resizeObserver = null
    chartInstance?.dispose()
    chartInstance = null
})
</script>

<template>
  <div class="dashboard-chart-card">
    <div class="chart-header">
        <div class="chart-title">趋势分析</div>
        <div class="chart-actions">
            <el-radio-group v-model="activeTab" size="small">
                <el-radio-button label="occupancy">入住</el-radio-button>
                <el-radio-button label="repair">维修</el-radio-button>
                <el-radio-button label="visitor">访客</el-radio-button>
            </el-radio-group>
        </div>
    </div>
    <div class="chart-content" ref="chartRef"></div>
  </div>
</template>

<style scoped>
.dashboard-chart-card {
    background: white;
    border-radius: 8px;
    padding: 16px;
    height: 100%;
    min-height: 350px;
    box-shadow: 0 1px 2px 0 rgba(0, 0, 0, 0.05);
    display: flex;
    flex-direction: column;
}

.chart-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 16px;
    gap: 12px;
    flex-wrap: wrap;
}

.chart-title {
    font-size: 1rem;
    font-weight: 600;
    color: var(--el-text-color-primary);
}

.chart-content {
    flex: 1;
    width: 100%;
    min-height: 300px;
    min-width: 0;
}
</style>

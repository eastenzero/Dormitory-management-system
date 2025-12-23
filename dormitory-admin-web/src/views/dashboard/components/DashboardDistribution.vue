<script setup lang="ts">
import { ref, onBeforeUnmount, onMounted, watch, nextTick } from 'vue'
import * as echarts from 'echarts'
import type { DashboardDistribution } from '../../../api/dashboard'

const props = defineProps<{
  data: DashboardDistribution | null
  loading: boolean
}>()

const pieChartRef = ref<HTMLElement | null>(null)
const barChartRef = ref<HTMLElement | null>(null)

let pieInstance: echarts.ECharts | null = null
let barInstance: echarts.ECharts | null = null
let pieResizeObserver: ResizeObserver | null = null
let barResizeObserver: ResizeObserver | null = null

const initCharts = () => {
    if (pieChartRef.value) {
        pieInstance = echarts.init(pieChartRef.value)
    }
    if (barChartRef.value) {
        barInstance = echarts.init(barChartRef.value)
    }
    updateCharts()
    pieInstance?.resize()
    barInstance?.resize()

    if (pieChartRef.value) {
        pieResizeObserver = new ResizeObserver(() => {
            pieInstance?.resize()
        })
        pieResizeObserver.observe(pieChartRef.value)
    }
    if (barChartRef.value) {
        barResizeObserver = new ResizeObserver(() => {
            barInstance?.resize()
        })
        barResizeObserver.observe(barChartRef.value)
    }
}

const updateCharts = () => {
    if (!props.data) return

    // Pie Chart (Gender)
    if (pieInstance) {
        pieInstance.setOption({
            tooltip: { trigger: 'item' },
            legend: { top: '0%', left: 'center' },
            series: [{
                name: '性别',
                type: 'pie',
                radius: ['40%', '70%'],
                center: ['50%', '60%'],
                avoidLabelOverlap: false,
                itemStyle: {
                    borderRadius: 5,
                    borderColor: '#fff',
                    borderWidth: 2
                },
                label: { show: false },
                data: [
                    { value: props.data.gender.male, name: '男生', itemStyle: { color: '#409EFF' } },
                    { value: props.data.gender.female, name: '女生', itemStyle: { color: '#F56C6C' } }
                ]
            }]
        })
    }

    // Bar Chart (Top Buildings)
    if (barInstance) {
        // Sort specifically for bar chart display (bottom to top usually)
        const sorted = [...props.data.topBuildings].reverse() 
        barInstance.setOption({
            tooltip: {
                trigger: 'axis',
                axisPointer: { type: 'shadow' }
            },
            grid: {
                top: '5%',
                left: '2%',
                right: '10%',
                bottom: '5%',
                containLabel: true
            },
            xAxis: {
                type: 'value',
                max: 1 // Rate is 0-1
            },
            yAxis: {
                type: 'category',
                data: sorted.map(i => i.buildingName),
                 axisLine: { show: false },
                 axisTick: { show: false }
            },
            series: [{
                name: '入住率',
                type: 'bar',
                data: sorted.map(i => i.occupancyRate),
                itemStyle: {
                    color: (params: any) => {
                        return params.value > 0.9 ? '#F56C6C' : params.value > 0.7 ? '#E6A23C' : '#67C23A'
                    },
                    borderRadius: [0, 4, 4, 0]
                },
                label: {
                    show: true,
                    position: 'right',
                    formatter: (p: any) => (p.value * 100).toFixed(0) + '%'
                }
            }]
        })
    }
}

watch(() => props.data, () => {
    updateCharts()
    nextTick(() => {
        pieInstance?.resize()
        barInstance?.resize()
    })
})

onMounted(() => {
    nextTick(() => {
        initCharts()
    })
})

onBeforeUnmount(() => {
    pieResizeObserver?.disconnect()
    barResizeObserver?.disconnect()
    pieResizeObserver = null
    barResizeObserver = null
    pieInstance?.dispose()
    barInstance?.dispose()
    pieInstance = null
    barInstance = null
})
</script>

<template>
    <div class="dist-container">
        <!-- Card 1: Top Buildings -->
        <div class="dashboard-chart-card">
            <div class="chart-header">
                <span class="chart-title">楼栋入住率（Top）</span>
            </div>
            <div class="chart-content" ref="barChartRef"></div>
        </div>
        
        <!-- Card 2: Gender Struct -->
        <div class="dashboard-chart-card">
            <div class="chart-header">
                 <span class="chart-title">性别分布</span>
            </div>
            <div class="chart-content" ref="pieChartRef"></div>
        </div>
    </div>
</template>

<style scoped>
.dist-container {
    display: flex;
    flex-direction: column;
    gap: 16px;
    height: 100%;
    min-width: 0;
}

.dashboard-chart-card {
    background: white;
    border-radius: 8px;
    padding: 16px;
    flex: 1;
    min-height: 200px;
    box-shadow: 0 1px 2px 0 rgba(0, 0, 0, 0.05);
    display: flex;
    flex-direction: column;
    min-width: 0;
}

.chart-header {
    margin-bottom: 10px;
    font-size: 0.9rem;
    font-weight: 600;
}

.chart-content {
    flex: 1;
    width: 100%;
    min-height: 150px;
    min-width: 0;
}
</style>

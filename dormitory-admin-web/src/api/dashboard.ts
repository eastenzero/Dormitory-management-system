export interface DashboardSummary {
    // Card 1
    totalBeds: number
    availableBeds: number
    occupiedBeds: number
    // Card 2
    activeAssignments: number
    occupancyRate: number // 0-1
    // Card 3
    todayCheckinCount: number
    todayCheckoutCount: number
    // Card 4
    repairOpenCount: number
    repairSubmitted: number
    repairInProgress: number
    // Card 5
    visitorTodayCount: number
    visitorInCount: number
    // Card 6
    alertsCount: number
}

export interface DateSeries {
    date: string
    value: number
}

export interface DashboardTrends {
    occupancy: {
        active: DateSeries[]
        rate: DateSeries[]
    }
    repair: {
        created: DateSeries[]
        completed: DateSeries[]
        backlog: DateSeries[]
    }
    visitor: {
        total: DateSeries[]
        active: DateSeries[]
    }
}

export interface BuildingOccupancy {
    buildingName: string
    occupancyRate: number
    totalBeds: number
    occupied: number
}

export interface GenderDistribution {
    male: number
    female: number
    unlimited: number
}

export interface DashboardDistribution {
    topBuildings: BuildingOccupancy[]
    gender: GenderDistribution
}

export interface TodoItem {
    id: string
    title: string
    priority: 'High' | 'Medium' | 'Low'
    status: string
    createdAt: string
    age: string // "2h", "1d"
    location: string
}

export interface AlertItem {
    id: string
    title: string
    description: string
    count: number
    severity: 'error' | 'warning' | 'info'
}

export interface NoticeItem {
    id: string
    title: string
    publishAt: string
    pinned: boolean
    status: 'PUBLISHED' | 'DRAFT'
}

export const getDashboardSummary = async (): Promise<DashboardSummary> => {
    // Simulate API delay
    await new Promise(r => setTimeout(r, 500))
    return {
        totalBeds: 2500,
        availableBeds: 450,
        occupiedBeds: 2050,
        activeAssignments: 2050,
        occupancyRate: 0.82,
        todayCheckinCount: 15,
        todayCheckoutCount: 8,
        repairOpenCount: 23,
        repairSubmitted: 10,
        repairInProgress: 13,
        visitorTodayCount: 42,
        visitorInCount: 5,
        alertsCount: 3
    }
}

export const getDashboardTrends = async (range: string = '7d'): Promise<DashboardTrends> => {
    await new Promise(r => setTimeout(r, 600))
    const days = range === '30d' ? 30 : range === 'today' ? 1 : 7
    const dates = Array.from({ length: days }, (_, i) => {
        const d = new Date()
        d.setDate(d.getDate() - (days - 1 - i))
        return d.toISOString().slice(0, 10)
    })

    return {
        occupancy: {
            active: dates.map(d => ({ date: d, value: 2000 + Math.floor(Math.random() * 100) })),
            rate: dates.map(d => ({ date: d, value: 0.8 + Math.random() * 0.1 }))
        },
        repair: {
            created: dates.map(d => ({ date: d, value: Math.floor(Math.random() * 10) })),
            completed: dates.map(d => ({ date: d, value: Math.floor(Math.random() * 8) })),
            backlog: dates.map(d => ({ date: d, value: Math.floor(Math.random() * 20) }))
        },
        visitor: {
            total: dates.map(d => ({ date: d, value: Math.floor(Math.random() * 50) + 20 })),
            active: dates.map(d => ({ date: d, value: Math.floor(Math.random() * 10) }))
        }
    }
}

export const getDashboardDistribution = async (): Promise<DashboardDistribution> => {
    await new Promise(r => setTimeout(r, 550))
    return {
        topBuildings: [
            { buildingName: 'A栋', occupancyRate: 0.95, totalBeds: 500, occupied: 475 },
            { buildingName: 'B栋', occupancyRate: 0.88, totalBeds: 500, occupied: 440 },
            { buildingName: 'C栋', occupancyRate: 0.85, totalBeds: 400, occupied: 340 },
            { buildingName: 'D栋', occupancyRate: 0.72, totalBeds: 600, occupied: 432 },
            { buildingName: 'E栋', occupancyRate: 0.65, totalBeds: 300, occupied: 195 },
            { buildingName: 'F栋', occupancyRate: 0.40, totalBeds: 200, occupied: 80 }
        ],
        gender: {
            male: 1200,
            female: 850,
            unlimited: 0
        }
    }
}

export const getDashboardTodos = async (): Promise<TodoItem[]> => {
    return [
        { id: '1', title: 'A栋-302 空调漏水', priority: 'High', status: 'Pending', createdAt: '2025-12-23 09:00', age: '10h', location: 'A-302' },
        { id: '2', title: 'B栋-101 门锁故障', priority: 'High', status: 'Pending', createdAt: '2025-12-23 10:30', age: '8h', location: 'B-101' },
        { id: '3', title: '新生入住审核', priority: 'Medium', status: 'Reviewing', createdAt: '2025-12-23 14:00', age: '4h', location: 'Office' },
        { id: '4', title: 'C栋-505 椅子损坏', priority: 'Low', status: 'Pending', createdAt: '2025-12-22 16:00', age: '1d', location: 'C-505' },
        { id: '5', title: '系统数据备份异常', priority: 'Medium', status: 'Investigating', createdAt: '2025-12-21', age: '2d', location: 'System' },
    ]
}

export const getDashboardAlerts = async (): Promise<AlertItem[]> => {
    return [
        { id: '1', title: '床位冲突警告', description: 'A-302-1 床位有2名活跃用户', count: 2, severity: 'error' },
        { id: '2', title: '工单积压', description: '超过48小时未处理工单 > 5', count: 5, severity: 'warning' },
        { id: '3', title: '资源数据需核对', description: 'E栋房间无床位信息', count: 12, severity: 'info' }
    ]
}

export const getDashboardNotices = async (): Promise<NoticeItem[]> => {
    return [
        { id: '1', title: '关于寒假封楼的通知', publishAt: '2025-12-20', pinned: true, status: 'PUBLISHED' },
        { id: '2', title: '12月安全卫生大检查结果公示', publishAt: '2025-12-18', pinned: false, status: 'PUBLISHED' },
        { id: '3', title: '系统维护停机公告', publishAt: '2025-12-25', pinned: false, status: 'DRAFT' }
    ]
}

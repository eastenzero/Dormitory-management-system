import http from './http'
import type { ApiResponse } from './auth'
import type { PageResult } from './dorm'

export type RepairPriority = 'LOW' | 'MEDIUM' | 'HIGH'
export type RepairStatus = 'SUBMITTED' | 'IN_PROGRESS' | 'DONE' | 'REJECTED'

export type RepairOrder = {
  id: number
  title: string
  priority: RepairPriority
  status: RepairStatus
  assigneeUserId?: number | null
  assigneeUsername?: string | null
  assigneeRealName?: string | null
  createdAt: string
}

export type RepairOrderDetail = {
  id: number
  studentId?: number | null
  studentNo?: string | null
  studentName?: string | null

  buildingId?: number | null
  buildingCode?: string | null
  buildingName?: string | null

  roomId?: number | null
  roomNo?: string | null

  title: string
  description?: string | null
  priority: RepairPriority
  status: RepairStatus

  assigneeUserId?: number | null
  assigneeUsername?: string | null
  assigneeRealName?: string | null

  createdAt: string
}

export type RepairLog = {
  id: number
  repairOrderId: number
  action: string
  content?: string | null
  createdAt: string
  createdBy?: number | null
}

export async function listRepairs(params: {
  status?: string
  priority?: string
  assigneeUserId?: number
  keyword?: string
  page?: number
  pageSize?: number
  sortBy?: string
  sortOrder?: string
}) {
  const res = await http.get<ApiResponse<PageResult<RepairOrder>>>('/api/v1/repairs', { params })
  return res.data
}

export async function getRepair(id: number) {
  const res = await http.get<ApiResponse<RepairOrderDetail>>(`/api/v1/repairs/${id}`)
  return res.data
}

export async function createRepair(payload: {
  title: string
  description?: string | null
  priority?: string | null
  studentId?: number | null
  buildingId?: number | null
  roomId?: number | null
}) {
  const res = await http.post<ApiResponse<RepairOrderDetail>>('/api/v1/repairs', payload)
  return res.data
}

export async function assignRepair(id: number, payload: { assigneeUserId: number; content?: string | null }) {
  const res = await http.post<ApiResponse<RepairOrderDetail>>(`/api/v1/repairs/${id}/assign`, payload)
  return res.data
}

export async function transitionRepair(id: number, payload: { status: string; content?: string | null }) {
  const res = await http.post<ApiResponse<RepairOrderDetail>>(`/api/v1/repairs/${id}/transition`, payload)
  return res.data
}

export async function listRepairLogs(id: number) {
  const res = await http.get<ApiResponse<RepairLog[]>>(`/api/v1/repairs/${id}/logs`)
  return res.data
}

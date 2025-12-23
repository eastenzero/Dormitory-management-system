import http from './http'
import type { ApiResponse } from './auth'
import type { PageResult } from './dorm'

export type VisitorStatus = 'IN' | 'OUT'

export type VisitorRecord = {
  id: number
  studentId?: number | null
  studentNo?: string | null
  studentName?: string | null

  visitorName: string
  idNo?: string | null
  phone?: string | null
  visitReason?: string | null

  visitAt: string
  leaveAt?: string | null
  status: VisitorStatus | string

  createdAt?: string | null
}

export async function listVisitors(params: {
  status?: string
  keyword?: string
  fromAt?: string
  toAt?: string
  studentId?: number
  page?: number
  pageSize?: number
  sortBy?: string
  sortOrder?: string
}) {
  const res = await http.get<ApiResponse<PageResult<VisitorRecord>>>('/api/v1/visitors', { params })
  return res.data
}

export async function getVisitor(id: number) {
  const res = await http.get<ApiResponse<VisitorRecord>>(`/api/v1/visitors/${id}`)
  return res.data
}

export async function createVisitor(payload: {
  studentId?: number | null
  visitorName: string
  idNo?: string | null
  phone?: string | null
  visitReason?: string | null
  visitAt?: string | null
}) {
  const res = await http.post<ApiResponse<VisitorRecord>>('/api/v1/visitors', payload)
  return res.data
}

export async function leaveVisitor(id: number) {
  const res = await http.post<ApiResponse<VisitorRecord>>(`/api/v1/visitors/${id}/leave`)
  return res.data
}

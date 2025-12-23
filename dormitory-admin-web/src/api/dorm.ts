import http from './http'
import type { ApiResponse } from './auth'

export type PageResult<T> = {
  list: T[]
  page: number
  pageSize: number
  total: number
}

export type DormBuilding = {
  id: number
  code: string
  name: string
  genderLimit: string
  address?: string | null
  status: string
}

export type DormRoom = {
  id: number
  buildingId: number
  buildingCode: string
  buildingName: string
  floorNo: number
  roomNo: string
  roomType?: string | null
  genderLimit: string
  status: string
}

export type DormBed = {
  id: number
  roomId: number
  roomNo: string
  buildingId: number
  buildingCode: string
  buildingName: string
  bedNo: string
  status: string
}

export type DormAssignment = {
  id: number
  studentId: number
  studentNo: string
  studentName: string
  bedId: number
  bedNo: string
  roomId: number
  roomNo: string
  buildingId: number
  buildingCode: string
  buildingName: string
  startAt: string
  endAt?: string | null
  status: string
  reason?: string | null
}

export async function listBuildings(params: {
  keyword?: string
  genderLimit?: string
  page?: number
  pageSize?: number
  sortBy?: string
  sortOrder?: string
}) {
  const res = await http.get<ApiResponse<PageResult<DormBuilding>>>('/api/v1/dorm/buildings', { params })
  return res.data
}

export async function getBuilding(id: number) {
  const res = await http.get<ApiResponse<DormBuilding>>(`/api/v1/dorm/buildings/${id}`)
  return res.data
}

export async function createBuilding(payload: {
  code: string
  name: string
  genderLimit: string
  address?: string | null
  status?: string | null
}) {
  const res = await http.post<ApiResponse<DormBuilding>>('/api/v1/dorm/buildings', payload)
  return res.data
}

export async function updateBuilding(
  id: number,
  payload: { code: string; name: string; genderLimit: string; address?: string | null; status?: string | null },
) {
  const res = await http.put<ApiResponse<DormBuilding>>(`/api/v1/dorm/buildings/${id}`, payload)
  return res.data
}

export async function deleteBuilding(id: number) {
  const res = await http.delete<ApiResponse<null>>(`/api/v1/dorm/buildings/${id}`)
  return res.data
}

export async function listRooms(params: {
  buildingId?: number
  floorNo?: number
  roomNo?: string
  status?: string
  keyword?: string
  page?: number
  pageSize?: number
  sortBy?: string
  sortOrder?: string
}) {
  const res = await http.get<ApiResponse<PageResult<DormRoom>>>('/api/v1/dorm/rooms', { params })
  return res.data
}

export async function getRoom(id: number) {
  const res = await http.get<ApiResponse<DormRoom>>(`/api/v1/dorm/rooms/${id}`)
  return res.data
}

export async function createRoom(payload: {
  buildingId: number
  floorNo: number
  roomNo: string
  roomType?: string | null
  genderLimit?: string | null
  status?: string | null
}) {
  const res = await http.post<ApiResponse<DormRoom>>('/api/v1/dorm/rooms', payload)
  return res.data
}

export async function updateRoom(
  id: number,
  payload: {
    buildingId: number
    floorNo: number
    roomNo: string
    roomType?: string | null
    genderLimit?: string | null
    status?: string | null
  },
) {
  const res = await http.put<ApiResponse<DormRoom>>(`/api/v1/dorm/rooms/${id}`, payload)
  return res.data
}

export async function deleteRoom(id: number) {
  const res = await http.delete<ApiResponse<null>>(`/api/v1/dorm/rooms/${id}`)
  return res.data
}

export async function listBeds(params: {
  buildingId?: number
  roomId?: number
  status?: string
  keyword?: string
  page?: number
  pageSize?: number
  sortBy?: string
  sortOrder?: string
}) {
  const res = await http.get<ApiResponse<PageResult<DormBed>>>('/api/v1/dorm/beds', { params })
  return res.data
}

export async function getBed(id: number) {
  const res = await http.get<ApiResponse<DormBed>>(`/api/v1/dorm/beds/${id}`)
  return res.data
}

export async function createBed(payload: { roomId: number; bedNo: string; status?: string | null }) {
  const res = await http.post<ApiResponse<DormBed>>('/api/v1/dorm/beds', payload)
  return res.data
}

export async function updateBed(id: number, payload: { roomId: number; bedNo: string; status?: string | null }) {
  const res = await http.put<ApiResponse<DormBed>>(`/api/v1/dorm/beds/${id}`, payload)
  return res.data
}

export async function deleteBed(id: number) {
  const res = await http.delete<ApiResponse<null>>(`/api/v1/dorm/beds/${id}`)
  return res.data
}

export async function listAssignments(params: {
  studentId?: number
  buildingId?: number
  roomId?: number
  bedId?: number
  status?: string
  page?: number
  pageSize?: number
  sortBy?: string
  sortOrder?: string
}) {
  const res = await http.get<ApiResponse<PageResult<DormAssignment>>>('/api/v1/dorm/assignments', { params })
  return res.data
}

export async function getAssignment(id: number) {
  const res = await http.get<ApiResponse<DormAssignment>>(`/api/v1/dorm/assignments/${id}`)
  return res.data
}

export async function createAssignment(payload: { studentId: number; bedId: number; reason?: string | null }) {
  const res = await http.post<ApiResponse<DormAssignment>>('/api/v1/dorm/assignments', payload)
  return res.data
}

export async function endAssignment(id: number, payload?: { reason?: string | null }) {
  const res = await http.post<ApiResponse<DormAssignment>>(`/api/v1/dorm/assignments/${id}/end`, payload || {})
  return res.data
}

import http from './http'
import type { ApiResponse } from './auth'
import type { PageResult } from './dorm'

export type Student = {
  id: number
  studentNo: string
  name: string
  gender: string
  college?: string | null
  major?: string | null
  className?: string | null
  phone?: string | null
  status: string
}

export async function listStudents(params: {
  gender?: string
  status?: string
  keyword?: string
  page?: number
  pageSize?: number
  sortBy?: string
  sortOrder?: string
}) {
  const res = await http.get<ApiResponse<PageResult<Student>>>('/api/v1/students', { params })
  return res.data
}

export async function getStudent(id: number) {
  const res = await http.get<ApiResponse<Student>>(`/api/v1/students/${id}`)
  return res.data
}

export async function createStudent(payload: {
  studentNo: string
  name: string
  gender: string
  college?: string | null
  major?: string | null
  className?: string | null
  phone?: string | null
  status?: string | null
}) {
  const res = await http.post<ApiResponse<Student>>('/api/v1/students', payload)
  return res.data
}

export async function updateStudent(
  id: number,
  payload: {
    studentNo: string
    name: string
    gender: string
    college?: string | null
    major?: string | null
    className?: string | null
    phone?: string | null
    status?: string | null
  },
) {
  const res = await http.put<ApiResponse<Student>>(`/api/v1/students/${id}`, payload)
  return res.data
}

export async function deleteStudent(id: number) {
  const res = await http.delete<ApiResponse<null>>(`/api/v1/students/${id}`)
  return res.data
}

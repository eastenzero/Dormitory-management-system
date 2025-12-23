import http from './http'
import type { ApiResponse } from './auth'

export type SysUser = {
  id: number
  username: string
  realName?: string | null
  status: string
}

export async function listUsers() {
  const res = await http.get<ApiResponse<SysUser[]>>('/api/v1/sys/users')
  return res.data
}

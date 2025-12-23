import http from './http'

export type ApiResponse<T> = {
  code: number
  message: string
  data: T
  traceId?: string | null
}

export type LoginResponse = {
  token: string
}

export type RegisterRequest = {
  username: string
  password: string
  realName?: string
}

export type MeResponse = {
  id: number
  username: string
  realName?: string | null
  roles: string[]
  permissions: string[]
}

export async function login(username: string, password: string) {
  const res = await http.post<ApiResponse<LoginResponse>>('/api/v1/auth/login', {
    username,
    password,
  })
  return res.data
}

export async function register(req: RegisterRequest) {
  const res = await http.post<ApiResponse<LoginResponse>>('/api/v1/auth/register', req)
  return res.data
}

export async function me() {
  const res = await http.get<ApiResponse<MeResponse>>('/api/v1/auth/me')
  return res.data
}

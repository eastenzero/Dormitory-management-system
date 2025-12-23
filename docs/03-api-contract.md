# 接口契约（建议）

## 1. 通用
- BasePath：`/api/v1`
- Auth：`Authorization: Bearer <jwt>`

## 2. 统一响应结构（建议）
```json
{
  "code": 0,
  "message": "ok",
  "data": {},
  "traceId": "..."
}
```
- `code=0` 成功，其它为业务错误码

## 3. 分页参数（建议）
- Query：`page`（从1开始）、`pageSize`、`sortBy`、`sortOrder`

## 4. 认证接口（示例）
- `POST /auth/login`：账号密码登录
- `POST /auth/logout`：登出（可选）
- `GET /auth/me`：当前用户信息 + 角色 + 权限列表

## 5. 核心资源接口（示例清单）
- 楼栋
  - `GET /dorm/buildings`
  - `POST /dorm/buildings`
  - `PUT /dorm/buildings/{id}`
  - `DELETE /dorm/buildings/{id}`
- 房间/床位
  - `GET /dorm/rooms`
  - `GET /dorm/beds?status=AVAILABLE`
- 学生
  - `GET /students`
  - `POST /students`
- 入住/分配
  - `POST /assignments`（分配床位）
  - `POST /assignments/{id}/transfer`
  - `POST /assignments/{id}/checkout`
  - `GET /assignments/history?studentId=...`
- 报修
  - `POST /repairs`
  - `GET /repairs`
  - `POST /repairs/{id}/assign`
  - `POST /repairs/{id}/close`
  - `POST /repairs/{id}/rate`

## 6. 权限返回（用于前端路由与按钮）
- `GET /auth/me` 返回：
  - `roles: string[]`
  - `permissions: string[]`（如 `dorm:building:write`）

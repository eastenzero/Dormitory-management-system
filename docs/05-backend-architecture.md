# 后端架构设计（Spring Boot 3 / Java 17）

## 1. 分层与模块
建议按业务域分包：
- `com.xxx.dormitory`
  - `common`（统一响应、异常、审计、工具）
  - `security`（JWT、鉴权、用户上下文）
  - `sys`（用户/角色/权限）
  - `dorm`（楼栋/房间/床位/分配）
  - `student`（学生档案）
  - `repair`（报修工单）
  - `visitor`（访客）
  - `notice`（公告）
  - `report`（统计）

每个域内：
- `controller`：REST 接口
- `service`：业务逻辑（事务、校验、编排）
- `mapper`：数据访问（MyBatis-Plus Mapper / XML 可选）
- `dto`：入参
- `vo`：出参

## 2. 认证与权限
- 登录：`POST /api/v1/auth/login` -> 返回 JWT
- 鉴权：Spring Security + JWT Filter
- 授权：
  - 接口级：`@PreAuthorize("hasAuthority('dorm:building:write')")`
  - 菜单/按钮：前端根据 `GET /auth/me` 的 `permissions` 控制

## 3. 统一规范
- 统一响应：`ApiResponse<T>`
- 统一异常：`@RestControllerAdvice` 转换为错误码
- 参数校验：`jakarta.validation` 注解 + 全局处理
- 日志：业务关键点记录 + `audit_log` 落库（可异步）

## 4. 事务边界（示例）
- 分配床位：一个事务内
  - 校验床位可用
  - 校验学生无有效分配
  - 写入 `dorm_assignment`（ACTIVE）
  - 更新 `dorm_bed.status=OCCUPIED`
  - 写入 `dorm_assignment_event`

## 5. 安全与审计
- 密码：BCrypt
- JWT：短期 access token（MVP 可不做 refresh）
- 关键操作审计：分配/调宿/退宿/删除基础数据/权限变更

## 6. OpenAPI
- 使用 springdoc 暴露 `/swagger-ui.html`（或 `/swagger-ui/index.html`）
- DTO/VO 使用清晰字段名与枚举

## 7. 错误码建议
- `0`：成功
- `40001`：参数校验失败
- `40101`：未登录
- `40301`：无权限
- `40901`：床位已占用
- `40902`：学生已有有效床位
- `50000`：系统错误

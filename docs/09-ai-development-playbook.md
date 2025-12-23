# AI 开发操作手册（如何使用 docs/ 与 prompts/ 并持续同步进度）

本文件用于：当你把“同一份提示词”发给 AI 让其持续开发时，AI 能知道：
- 本项目现状（已完成什么）
- 下一步按什么顺序开发（里程碑拆分）
- 每个里程碑需要阅读哪些 `docs/` 与 `prompts/`
- 每次开发结束如何同步进度
- 实在需要更换 AI 时如何交接（当前总结 + 下一个 AI 的提示词）

---

## 0. 核心约定（AI 必须遵守）
- 不使用 Docker（项目内已移除容器方案）。
- 前端：`dormitory-admin-web/`（Vue3 + TS + Vite + Element Plus）。
- 后端：`dormitory-server/`（Spring Boot 3，Java 17）。
- API 基础路径：`/api/v1`。
- 统一响应：`ApiResponse<T>`（成功 `code=0`）。
- 数据库：MySQL 8 + Flyway 迁移。
- MySQL 连接（当前约定）：
  - host：`127.0.0.1`
  - 端口：`3306`
  - 用户名：`root`
  - 密码：`123456`
  - 数据库：`dormitory`（主库）、`dormitory_test`（测试库）

---

## 1. `docs/` 与 `prompts/` 的关系（别混用）
- `docs/`：项目“标准答案/裁判”（需求、流程、接口契约、数据模型、架构约束）。用于对齐口径与验收。
- `prompts/`：你实际复制给 AI 的“可执行指令”。每个岗位 prompt 会指示 AI 应阅读哪些 `docs/`，并约束产出格式。

### 推荐发给 AI 的最省脑方式
每次会话只发：
1) `prompts/general/prompt.md`（通用规则：统一约束、产出要求）
2) 一个岗位 prompt（backend/frontend/database/qa/...）
3) 你本次的明确目标（例如“只做 auth/login + auth/me，不做其他模块”）

---

## 2. 当前项目已完成进度（由 AI 维护）

### 2.1 已完成
- 去除 Docker 相关内容（目录、文档、提示词）。
- MySQL 已在系统服务方式运行。
- 已初始化数据库与用户/权限：
  - `dormitory`、`dormitory_test` 两个库已创建。
- 后端已配置使用真实 MySQL（当前账号已改为 `root/123456`）：
  - `dormitory-server/src/main/resources/application.yml`
  - `dormitory-server/src/test/resources/application-test.yml`
- Flyway 已能正常执行迁移（`V1__init.sql` 已应用）。
- 冒烟验证通过：
  - 后端 `mvn test` 通过
  - 后端运行后 `GET /api/v1/ping` 返回 `{"code":0,"message":"ok","data":"pong"}`
- 前端已完成基础骨架：路由（`/login`、`/dashboard`）、Vite 代理 `/api -> http://localhost:8080`、Dashboard 调用 `/api/v1/ping`。

### 2.2 运行命令速查
- 启动后端（示例）：在 `dormitory-server/` 执行 `mvn spring-boot:run`
- 启动前端：在 `dormitory-admin-web/` 执行 `npm run dev`

---

## 3. 全项目里程碑拆分（推荐开发顺序）

> 说明：目录顺序不代表开发顺序；以“依赖关系”决定先后。

### M0：工程可运行（已完成）
- **验收**：前端 Dashboard 能 ping 后端；后端 Flyway 正常。

### M1：认证与权限闭环（优先级最高）
- **目标**：先把“能登录 + 权限生效”做出来，再做业务。
- **后端产出**：
  - `POST /api/v1/auth/login`
  - `GET /api/v1/auth/me`（返回 roles/permissions）
  - RBAC 表结构与种子数据（至少 admin）
- **前端产出**：
  - 登录页对接 `auth/login`
  - token 存储 + Axios 注入
  - 路由守卫 + 菜单/按钮权限
- **验收**：不同角色看到不同菜单；无权限接口返回 403。

### M2：宿舍资源基础数据（楼栋/房间/床位）
- **目标**：形成第一个稳定 CRUD 域。
- **验收**：能创建/查询/修改/删除；支持“空床查询”。

### M3：学生档案 + 分配/调宿/退宿（核心业务）
- **目标**：一人一床、分配/调宿/退宿全流程。
- **验收**：并发下不会双占；历史记录完整。

### M4：报修工单闭环
- **目标**：提交 -> 指派 -> 处理 -> 关闭 -> 评价。

### M5：公告 + 报表（MVP 收尾）
- **目标**：公告发布/置顶/定向可见；基础统计报表。

### M6：测试、加固、上线准备
- **目标**：用例覆盖、权限覆盖、安全 P0、备份恢复方案。

---

## 4. 每个里程碑需要 AI 阅读哪些提示词/文档

### 4.1 通用（每次会话都要先读）
- prompts：`prompts/general/prompt.md`
- docs：
  - `docs/00-project-overview.md`
  - `docs/01-requirements-scope.md`
  - `docs/03-api-contract.md`

### 4.2 M1（认证与权限）
- prompts：
  - 后端：`prompts/backend/prompt.md`
  - 前端：`prompts/frontend/prompt.md`
  - 数据库（如需要建表/种子数据）：`prompts/database/prompt.md`
  - 安全评审（P0）：`prompts/security/prompt.md`
- docs：
  - `docs/05-backend-architecture.md`
  - `docs/06-frontend-architecture.md`

### 4.3 M2/M3（宿舍资源 + 分配链路）
- prompts：
  - 后端：`prompts/backend/prompt.md`
  - 前端：`prompts/frontend/prompt.md`
  - 数据库：`prompts/database/prompt.md`
  - QA：`prompts/qa/prompt.md`
  - UI/UX（关键页面，如床位分配页）：`prompts/uiux/prompt.md`
- docs：
  - `docs/02-business-flows.md`
  - `docs/04-data-model.md`

### 4.4 M4/M5（报修/公告/报表）
- prompts：
  - 后端：`prompts/backend/prompt.md`
  - 前端：`prompts/frontend/prompt.md`
  - QA：`prompts/qa/prompt.md`
  - UI/UX：`prompts/uiux/prompt.md`
  - 安全：`prompts/security/prompt.md`
- docs：按模块补充阅读 `docs/01`、`docs/02`、`docs/04`。

### 4.5 M6（上线准备）
- prompts：
  - QA：`prompts/qa/prompt.md`
  - 安全：`prompts/security/prompt.md`
  - 运维：`prompts/devops/prompt.md`

---

## 5. AI 进度同步机制（每次开发后必须更新）

### 5.1 AI 的工作节奏要求
- 单次开发时间尽可能长：优先把一个里程碑/一个子模块做完整闭环（后端 + DB + 基础联调）。
- 只有在以下情况才切换 AI：
  - 明确卡在权限/环境且无法继续
  - 需要不同岗位并行且你要人为拆分

### 5.2 每次开发结束，AI 必须输出并同步到本文件
AI 在结束时必须：
- 更新本文件的 **“进度记录”**（见 5.3）
- 输出一段“本次总结 + 下次计划”

### 5.3 进度记录（由 AI 维护）
> 规则：每次会话在最下面追加一条记录；不要改旧记录。

#### 进度记录（追加区）
- [2025-12-23] 初始化：M0 完成（MySQL/Flyway/后端 ping/前端骨架）。
- [2025-12-23] M1 后端完成：JWT 登录（POST /api/v1/auth/login）+ 当前用户信息（GET /api/v1/auth/me，含 roles/permissions）+ /api/v1/** 默认需登录（除 login/ping）+ 权限示例接口（GET /api/v1/sys/users，admin=200，user=403）+ Flyway RBAC 表与种子数据（admin/user，权限 sys:user:read）+ V3 修正 admin/user 密码为 123456（BCrypt）。
- [2025-12-23] M2-2 后端完成：宿舍资源与入住后端 CRUD（/api/v1/dorm/buildings|rooms|beds|assignments + /{id}/end；/api/v1/students）+ V4 外键/索引/权限点/种子数据（B1/305/A-D + S0001）+ 权限控制（dorm:*:read/write、student:*）+ BizException/全局异常映射（409xx->409，AccessDenied->40301）。
- [2025-12-23] M2-3 前端完成：M2 页面与联调（楼栋/房间/床位/入住/学生）+ 顶部菜单组件按 permission 动态显示（AppNav）+ 路由 meta.permission + /403 友好页 + axios 复用（401/403 统一处理）+ /api 代理对齐后端端口 + build 通过。
- [2025-12-23] M3-2 前端完成：报修工单页面与联调（/repairs 列表分页/筛选/排序 + 创建弹窗 + 详情抽屉含日志 + 指派弹窗 + 状态流转）+ 新增 api/repairs.ts 封装 + AppNav 按 repair:order:read 显示入口 + 路由 meta.permission 无权限跳 /403 + 按钮/日志区域按权限显示 + 409 冲突友好提示 + npm run build 通过。
- [2025-12-23] M4-1 后端完成：访客登记 visitor_record 闭环（Flyway V6 约束/索引 + 权限 visitor:record:read/write 并赋 SUPER_ADMIN；API /api/v1/visitors 列表分页筛选+详情+登记进入+登记离开；离开幂等冲突 BizException(40901)->HTTP409；安全语义 40101/40301；避免返回 HTTP 500）。

---

## 6. 换 AI 交接规范（不得已才用）
当需要切换到下一个 AI 时，当前 AI 必须输出两部分：

### 6.1 交接总结（必须包含）
- 当前进行到哪个里程碑/模块（例如：M1-auth）
- 已完成文件列表（精确到路径）
- 未完成清单（按优先级）
- 当前阻塞点（如果有）
- 如何本地验证（命令 + 期望输出）

### 6.2 给“下一个 AI”的提示词（直接可复制）
必须按顺序给出：
1) `prompts/general/prompt.md` 全文
2) 本次需要的岗位 prompt（例如 `prompts/backend/prompt.md`）全文
3) 一句明确目标（例如“只完成 auth/login + auth/me + RBAC 种子数据，不做其他模块”）

---

## 7. AI 自检清单（每个里程碑结束都要做）
- 后端：能启动；关键接口可 curl；测试可跑（至少 `mvn test`）。
- 前端：能启动；关键页面可访问；能拿到真实接口数据。
- DB：所有变更都有 Flyway 脚本；关键约束（唯一性/外键/状态）落到库层。
- 安全：敏感信息不硬编码进代码仓库；权限点有覆盖。

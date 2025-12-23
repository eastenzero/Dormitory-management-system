# 前端架构设计（Vue 3 / Vite / TS）

## 1. 应用形态
- 管理端（Web）：宿舍管理员、学校管理员、维修人员等
- 学生端：
  - MVP 可先用同一套 Web（基于角色展示不同菜单）
  - 或二期拆分独立学生端

## 2. 目录建议
- `src/`
  - `api/`：按域封装请求
  - `router/`：静态路由 + 动态路由（按权限注入）
  - `stores/`：Pinia（auth、app、dict）
  - `views/`：页面
  - `components/`：通用组件
  - `layouts/`：管理台布局
  - `utils/`：工具（token、permission、format）

## 3. 登录与权限
- 登录页获取 token，保存到 `localStorage`
- `router.beforeEach`：
  - 未登录跳转 `/login`
  - 登录后拉取 `me`（roles/permissions）
  - 根据 permissions 生成动态路由与侧边菜单

## 4. 页面清单（MVP）
- `/login`
- `/dashboard`：概览
- `/sys/users` `/sys/roles`（学校管理员）
- `/dorm/buildings`：楼栋管理
- `/dorm/rooms`：房间管理
- `/dorm/beds`：床位管理/空床查询
- `/students`：学生档案
- `/assignments`：分配/入住/调宿/退宿
- `/repairs`：报修工单
- `/visitors`：访客登记
- `/notices`：公告管理
- `/audit`：操作日志（管理员）

## 5. UI 组件建议
- Element Plus：表格、表单、弹窗
- 统一表格组件：支持分页、查询、列配置（后续优化）

## 6. 接口对接
- Axios 拦截器：自动带 token，统一错误提示
- 约定：后端 `code != 0` 统一提示 `message`

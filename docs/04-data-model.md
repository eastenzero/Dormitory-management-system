# 数据库模型（MySQL 8，MVP）

## 0. 约定
- 字段：主键 `id`（bigint），审计字段：`created_at` `created_by` `updated_at` `updated_by`，软删除：`deleted`（tinyint）
- 时间统一：UTC 或服务器时区统一（建议 UTC），前后端以 ISO8601 传递。

## 1. 系统权限（RBAC）
### 1.1 `sys_user`
- `id`
- `username`（唯一）
- `password_hash`
- `real_name`
- `phone`
- `email`
- `status`（ACTIVE/LOCKED）

### 1.2 `sys_role`
- `id`
- `code`（唯一，如 SUPER_ADMIN）
- `name`

### 1.3 `sys_permission`
- `id`
- `code`（唯一，如 dorm:building:write）
- `name`

### 1.4 关联表
- `sys_user_role(user_id, role_id)`（唯一联合索引）
- `sys_role_permission(role_id, permission_id)`（唯一联合索引）

## 2. 宿舍基础数据
### 2.1 `dorm_building`
- `id`
- `code`（唯一，如 1A）
- `name`
- `gender_limit`（MALE/FEMALE/MIXED/UNLIMITED）
- `address`（可选）

### 2.2 `dorm_room`
- `id`
- `building_id`（FK）
- `floor_no`
- `room_no`（如 305）
- `room_type`（如 4人间）
- `gender_limit`（默认继承 building，可覆盖）
- `status`（ACTIVE/MAINTENANCE）
- 唯一约束建议：`(building_id, room_no)`

### 2.3 `dorm_bed`
- `id`
- `room_id`（FK）
- `bed_no`（如 A/B/C/D）
- `status`（AVAILABLE/OCCUPIED/DISABLED）
- 唯一约束建议：`(room_id, bed_no)`

## 3. 学生与入住
### 3.1 `student`
- `id`
- `student_no`（学号，唯一）
- `name`
- `gender`（MALE/FEMALE）
- `college`（可选）
- `major`（可选）
- `class_name`（可选）
- `phone`（可选）
- `status`（IN_SCHOOL/GRADUATED/LEFT）

### 3.2 `dorm_assignment`（床位分配/入住主表）
- `id`
- `student_id`（FK）
- `bed_id`（FK）
- `start_at`
- `end_at`（为空表示当前有效）
- `status`（ACTIVE/ENDED）
- `reason`（调宿/退宿原因）
- 关键约束：同一学生只能有一条 `status=ACTIVE` 记录（可用应用层保证 + 索引辅助）

### 3.3 `dorm_assignment_event`（分配事件流水，可选但建议）
- `id`
- `student_id`
- `from_bed_id`（可空）
- `to_bed_id`（可空）
- `event_type`（CHECKIN/TRANSFER/CHECKOUT）
- `event_at`
- `note`

## 4. 报修
### 4.1 `repair_order`
- `id`
- `student_id`（可空：也允许管理员代报）
- `building_id`（可空）
- `room_id`（可空）
- `title`
- `description`
- `priority`（LOW/MEDIUM/HIGH）
- `status`（SUBMITTED/ASSIGNED/IN_PROGRESS/RESOLVED/CLOSED）
- `assignee_user_id`（维修人员，可空）

### 4.2 `repair_log`
- `id`
- `repair_order_id`
- `action`（ASSIGN/UPDATE_STATUS/COMMENT/CLOSE 等）
- `content`

### 4.3 `repair_attachment`（可选）
- `id`
- `repair_order_id`
- `url`
- `file_name`

### 4.4 `repair_rating`（可选）
- `id`
- `repair_order_id`
- `rater_student_id`
- `score`（1-5）
- `comment`

## 5. 访客
### 5.1 `visitor_record`
- `id`
- `student_id`（可空）
- `visitor_name`
- `id_no`（可选）
- `phone`（可选）
- `visit_reason`
- `visit_at`
- `leave_at`（可空）
- `status`（PENDING/APPROVED/REJECTED/IN/OUT）

## 6. 通知公告
### 6.1 `notice`
- `id`
- `title`
- `content`（富文本/markdown，可二选一）
- `pinned`（置顶）
- `publish_status`（DRAFT/PUBLISHED）
- `publish_at`

### 6.2 `notice_visible_role`（可选）
- `id`
- `notice_id`
- `role_code`

## 7. 审计
### 7.1 `audit_log`
- `id`
- `action`
- `resource_type`
- `resource_id`
- `operator_user_id`
- `operator_username`
- `success`
- `detail`（json/text）
- `ip`
- `user_agent`
- `created_at`

## 8. 索引建议（摘要）
- `student(student_no)` unique
- `dorm_room(building_id, room_no)` unique
- `dorm_bed(room_id, bed_no)` unique
- `dorm_assignment(student_id, status)` index
- `repair_order(status, assignee_user_id)` index

# 演示数据（大规模，可幂等）

## 入口文件
- `dormitory-server/src/main/resources/db/migration/V7__seed_big_demo_data.sql`
  - 通过 MySQL 变量集中配置规模参数（脚本头部 **Config zone**）
  - 使用 `INSERT IGNORE` 或 `NOT EXISTS` 保证可重复执行
  - 使用业务前缀/标签避免与现有数据冲突：
    - 楼栋 `code`：`DEMO_Bxx`
    - 学号：`SDEMO00001` 这种形式
    - 工单标题：`DEMO_V7_RO_000001`
    - 访客证件：`DEMO_V7_VID_000001`
    - 公告标题：`DEMO_V7_NOTICE_0001`
    - 审计动作：`DEMO_V7_ACTION_00001`

- 可选清理脚本（不会被 Flyway 自动执行）：
  - `dormitory-server/src/main/resources/db/demo/cleanup_demo_data.sql`

## 建议规模（V7 默认值：LARGE）
- **楼栋**：10 栋（按 `gender_limit` 分配：男/女/不限混合）
- **楼层/房间**：每栋 10 层，每层 30 间（约 3000 间房）
- **床位**：按房型分配
  - 70% 4人间
  - 20% 6人间
  - 10% 2人间
- **学生**：4000
- **入住记录**：ACTIVE 约 3200，ENDED 约 900
- **报修工单**：1200（状态/优先级有分布，含 1 条种子日志）
- **访客登记**：2000（`IN/OUT` 分布，满足 `leave_at`/时间约束）
- **公告**：80（DRAFT/PUBLISHED 混合，少量置顶）
- **审计日志**：800

如果需要更小规模：修改 `V7__seed_big_demo_data.sql` 顶部变量区（提供了 SMALL 示例）。

## 分布策略（“看起来真实”的非纯随机）
- **确定性伪随机**：用 `CRC32(DEMO_TAG + key)` 做分布映射，使得数据看起来随机但结果稳定。
- **男女与楼栋限制**：楼栋 `gender_limit` 决定房间性别限制；ACTIVE 入住按男楼/女楼/不限楼分别匹配学生性别。
- **学院/专业/年级/班级**：按 `n % 6`/`n % 3` 等映射生成，使分布均匀且有结构。
- **入住率高低**：通过 ACTIVE 入住目标数控制总体入住率，并保留空床。
- **报修与访客**：按固定比例生成不同状态/优先级/原因，时间分布覆盖多个日期。

## 幂等与不破坏现有数据
- 所有 demo 数据使用独立前缀/标签，避免占用你已有的业务编码。
- 有唯一键的表：`INSERT IGNORE`（楼栋/房间/床位/学生）。
- 无唯一键的表：使用 `NOT EXISTS` 以“业务键”去重（如工单 `title`、访客 `id_no`、公告 `title`、审计 `action`）。
- 入住记录额外避免违反业务约束：
  - 不会给同一学生/床位重复创建 ACTIVE

## 验证方法（SQL 校验）
在 MySQL 执行：

```sql
-- 楼栋/房间/床位规模
SELECT COUNT(*) FROM dorm_building WHERE code LIKE 'DEMO_B%';
SELECT COUNT(*) FROM dorm_room r JOIN dorm_building b ON b.id=r.building_id WHERE b.code LIKE 'DEMO_B%';
SELECT COUNT(*) FROM dorm_bed bed
JOIN dorm_room r ON r.id=bed.room_id
JOIN dorm_building b ON b.id=r.building_id
WHERE b.code LIKE 'DEMO_B%';

-- 学生规模与性别
SELECT gender, COUNT(*) FROM student WHERE student_no LIKE 'SDEMO%' AND deleted=0 GROUP BY gender;

-- 入住状态分布
SELECT status, COUNT(*) FROM dorm_assignment WHERE reason LIKE 'DEMO_V7%' AND deleted=0 GROUP BY status;

-- 报修与访客状态分布
SELECT status, COUNT(*) FROM repair_order WHERE title LIKE 'DEMO_V7_RO_%' AND deleted=0 GROUP BY status;
SELECT status, COUNT(*) FROM visitor_record WHERE id_no LIKE 'DEMO_V7_VID_%' AND deleted=0 GROUP BY status;

-- 访客约束快速检查（应为 0）
SELECT COUNT(*) AS bad
FROM visitor_record
WHERE id_no LIKE 'DEMO_V7_VID_%'
  AND deleted=0
  AND NOT ((status='IN' AND leave_at IS NULL) OR (status='OUT' AND leave_at IS NOT NULL));
```

页面检查点（如果你有后台页面/接口联调）：
- 楼栋列表：能看到 `DEMO_Bxx`
- 房间/床位列表：房型、床位号（A-F）合理
- 学生列表：学院/专业/年级班级字段有分布
- 入住记录：ACTIVE/ENDED 均有
- 报修工单：不同优先级/状态均有
- 访客登记：IN/OUT 均有且 leaveAt 逻辑正确

## 回滚/重置（可选）
- 执行：`dormitory-server/src/main/resources/db/demo/cleanup_demo_data.sql`
  - 以软删除为主，不会自动删除你已有的非 demo 数据
  - `audit_log` 无 `deleted` 字段，会按 `action` 前缀做物理删除

注意：`V7__seed_big_demo_data.sql` 是 Flyway 迁移，正常情况下只会执行一次；若你想在同一库中“再生成一次”，建议手工执行 seed/cleanup 脚本（或在新库中重新跑 Flyway）。

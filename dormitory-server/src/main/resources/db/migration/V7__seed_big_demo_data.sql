-- Big demo data seeding for dormitory system (idempotent)
--
-- =========================
-- Config zone (edit here)
-- =========================
-- NOTE:
-- - This migration is written to be *idempotent* (safe to re-run manually), but Flyway will run it only once.
-- - If you want a smaller dataset, adjust the variables below BEFORE first apply in a fresh DB.
--
-- Preset: LARGE (default)
SET @DEMO_SEED_TAG := 'DEMO_V7';
SET @DEMO_BUILDING_PREFIX := 'DEMO_B';
SET @DEMO_BUILDING_COUNT := 10;          -- buildings
SET @DEMO_FLOORS_PER_BUILDING := 10;      -- floors per building
SET @DEMO_ROOMS_PER_FLOOR := 30;          -- rooms per floor
SET @DEMO_STUDENT_COUNT := 4000;          -- students
SET @DEMO_ACTIVE_ASSIGN_TARGET := 3200;   -- active assignments target
SET @DEMO_ENDED_ASSIGN_TARGET := 900;     -- ended assignments target
SET @DEMO_REPAIR_ORDER_COUNT := 1200;     -- repair orders
SET @DEMO_VISITOR_RECORD_COUNT := 2000;   -- visitor records
SET @DEMO_NOTICE_COUNT := 80;             -- notices
SET @DEMO_AUDIT_LOG_COUNT := 800;         -- audit logs

-- Preset: SMALL (example)
-- SET @DEMO_BUILDING_COUNT := 3;
-- SET @DEMO_FLOORS_PER_BUILDING := 6;
-- SET @DEMO_ROOMS_PER_FLOOR := 20;
-- SET @DEMO_STUDENT_COUNT := 800;
-- SET @DEMO_ACTIVE_ASSIGN_TARGET := 500;
-- SET @DEMO_ENDED_ASSIGN_TARGET := 150;
-- SET @DEMO_REPAIR_ORDER_COUNT := 200;
-- SET @DEMO_VISITOR_RECORD_COUNT := 300;
-- SET @DEMO_NOTICE_COUNT := 30;
-- SET @DEMO_AUDIT_LOG_COUNT := 150;

SET SESSION cte_max_recursion_depth = 100000;

SET @DEMO_ASSIGN_BASE_START := TIMESTAMP('2025-09-01 08:00:00');
SET @DEMO_VISIT_BASE := TIMESTAMP('2025-11-01 08:00:00');
SET @DEMO_REPAIR_BASE := TIMESTAMP('2025-10-01 09:00:00');
SET @DEMO_NOTICE_BASE := TIMESTAMP('2025-09-15 10:00:00');

DROP TEMPORARY TABLE IF EXISTS demo_seq;
CREATE TEMPORARY TABLE demo_seq (
  n INT NOT NULL PRIMARY KEY
) ENGINE=MEMORY;

INSERT INTO demo_seq (n)
SELECT
  (d1.d + d2.d * 10 + d3.d * 100 + d4.d * 1000 + 1) AS n
FROM (
  SELECT 0 AS d UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4
  UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9
) d1
CROSS JOIN (
  SELECT 0 AS d UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4
  UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9
) d2
CROSS JOIN (
  SELECT 0 AS d UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4
  UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9
) d3
CROSS JOIN (
  SELECT 0 AS d UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4
  UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9
) d4
WHERE (d1.d + d2.d * 10 + d3.d * 100 + d4.d * 1000 + 1) <= GREATEST(
  @DEMO_STUDENT_COUNT,
  @DEMO_ACTIVE_ASSIGN_TARGET,
  @DEMO_ENDED_ASSIGN_TARGET,
  @DEMO_REPAIR_ORDER_COUNT,
  @DEMO_VISITOR_RECORD_COUNT,
  @DEMO_NOTICE_COUNT,
  @DEMO_AUDIT_LOG_COUNT,
  @DEMO_BUILDING_COUNT,
  @DEMO_FLOORS_PER_BUILDING,
  @DEMO_ROOMS_PER_FLOOR
);

DROP TEMPORARY TABLE IF EXISTS demo_bseq;
CREATE TEMPORARY TABLE demo_bseq (
  n INT NOT NULL PRIMARY KEY
) ENGINE=MEMORY;
INSERT INTO demo_bseq (n)
SELECT n FROM demo_seq WHERE n <= @DEMO_BUILDING_COUNT;

DROP TEMPORARY TABLE IF EXISTS demo_fseq;
CREATE TEMPORARY TABLE demo_fseq (
  n INT NOT NULL PRIMARY KEY
) ENGINE=MEMORY;
INSERT INTO demo_fseq (n)
SELECT n FROM demo_seq WHERE n <= @DEMO_FLOORS_PER_BUILDING;

DROP TEMPORARY TABLE IF EXISTS demo_rseq;
CREATE TEMPORARY TABLE demo_rseq (
  n INT NOT NULL PRIMARY KEY
) ENGINE=MEMORY;
INSERT INTO demo_rseq (n)
SELECT n FROM demo_seq WHERE n <= @DEMO_ROOMS_PER_FLOOR;

-- =========================
-- 1) Buildings
-- =========================
INSERT IGNORE INTO dorm_building (code, name, gender_limit, address, status, deleted)
SELECT
  CONCAT(@DEMO_BUILDING_PREFIX, LPAD(seq.n, 2, '0')) AS code,
  CONCAT('演示-', LPAD(seq.n, 2, '0'), '号楼') AS name,
  CASE
    WHEN MOD(seq.n, 10) IN (1,2,3,4) THEN 'MALE'
    WHEN MOD(seq.n, 10) IN (5,6,7,8) THEN 'FEMALE'
    ELSE 'UNLIMITED'
  END AS gender_limit,
  CONCAT('演示校区A - ', LPAD(seq.n, 2, '0'), '号楼') AS address,
  'ACTIVE' AS status,
  0 AS deleted
FROM demo_seq seq
WHERE seq.n <= @DEMO_BUILDING_COUNT;

-- =========================
-- 2) Rooms
-- =========================
INSERT IGNORE INTO dorm_room (building_id, floor_no, room_no, room_type, gender_limit, status, deleted)
SELECT
  b.id AS building_id,
  fseq.n AS floor_no,
  CAST(fseq.n * 100 + rseq.n AS CHAR) AS room_no,
  CASE
    WHEN MOD(CRC32(CONCAT(@DEMO_SEED_TAG, '_RT_', b.code, '_', fseq.n, '_', rseq.n)), 100) < 70 THEN '4人间'
    WHEN MOD(CRC32(CONCAT(@DEMO_SEED_TAG, '_RT_', b.code, '_', fseq.n, '_', rseq.n)), 100) < 90 THEN '6人间'
    ELSE '2人间'
  END AS room_type,
  b.gender_limit AS gender_limit,
  'ACTIVE' AS status,
  0 AS deleted
FROM demo_bseq bseq
JOIN dorm_building b
  ON b.code = CONCAT(@DEMO_BUILDING_PREFIX, LPAD(bseq.n, 2, '0'))
JOIN demo_fseq fseq
JOIN demo_rseq rseq
WHERE b.deleted = 0;

-- =========================
-- 3) Beds
-- =========================
INSERT IGNORE INTO dorm_bed (room_id, bed_no, status, deleted)
SELECT
  r.id AS room_id,
  l.bed_no,
  'AVAILABLE' AS status,
  0 AS deleted
FROM dorm_room r
JOIN dorm_building b ON b.id = r.building_id
JOIN (
  SELECT 'A' AS bed_no, 1 AS ord
  UNION ALL SELECT 'B', 2
  UNION ALL SELECT 'C', 3
  UNION ALL SELECT 'D', 4
  UNION ALL SELECT 'E', 5
  UNION ALL SELECT 'F', 6
) l
WHERE b.code LIKE CONCAT(@DEMO_BUILDING_PREFIX, '%')
  AND b.deleted = 0
  AND r.deleted = 0
  AND l.ord <= CASE r.room_type
    WHEN '2人间' THEN 2
    WHEN '6人间' THEN 6
    ELSE 4
  END;

-- =========================
-- 4) Students
-- =========================
INSERT IGNORE INTO student (student_no, name, gender, college, major, class_name, phone, status, deleted)
SELECT
  CONCAT('SDEMO', LPAD(seq.n, 5, '0')) AS student_no,
  CONCAT(
    CASE MOD(seq.n, 20)
      WHEN 0 THEN '张' WHEN 1 THEN '王' WHEN 2 THEN '李' WHEN 3 THEN '赵' WHEN 4 THEN '刘'
      WHEN 5 THEN '陈' WHEN 6 THEN '杨' WHEN 7 THEN '黄' WHEN 8 THEN '周' WHEN 9 THEN '吴'
      WHEN 10 THEN '徐' WHEN 11 THEN '孙' WHEN 12 THEN '胡' WHEN 13 THEN '朱' WHEN 14 THEN '高'
      WHEN 15 THEN '林' WHEN 16 THEN '何' WHEN 17 THEN '郭' WHEN 18 THEN '马' ELSE '罗'
    END,
    CASE MOD(CRC32(CONCAT(@DEMO_SEED_TAG, '_GN_', seq.n)), 30)
      WHEN 0 THEN '子涵' WHEN 1 THEN '宇轩' WHEN 2 THEN '梓萱' WHEN 3 THEN '浩然' WHEN 4 THEN '欣怡'
      WHEN 5 THEN '思雨' WHEN 6 THEN '明哲' WHEN 7 THEN '俊杰' WHEN 8 THEN '雨桐' WHEN 9 THEN '佳琪'
      WHEN 10 THEN '博文' WHEN 11 THEN '一诺' WHEN 12 THEN '晨曦' WHEN 13 THEN '若曦' WHEN 14 THEN '嘉懿'
      WHEN 15 THEN '子墨' WHEN 16 THEN '可欣' WHEN 17 THEN '星辰' WHEN 18 THEN '雅婷' WHEN 19 THEN '天佑'
      WHEN 20 THEN '梓豪' WHEN 21 THEN '诗涵' WHEN 22 THEN '子琪' WHEN 23 THEN '雨欣' WHEN 24 THEN '佳宁'
      WHEN 25 THEN '亦凡' WHEN 26 THEN '梦琪' WHEN 27 THEN '俊熙' WHEN 28 THEN '紫涵' ELSE '子睿'
    END
  ) AS name,
  CASE WHEN MOD(CRC32(CONCAT(@DEMO_SEED_TAG, '_G_', seq.n)), 100) < 55 THEN 'MALE' ELSE 'FEMALE' END AS gender,
  CASE MOD(seq.n, 6)
    WHEN 0 THEN '计算机学院'
    WHEN 1 THEN '电子信息学院'
    WHEN 2 THEN '外国语学院'
    WHEN 3 THEN '经济管理学院'
    WHEN 4 THEN '土木工程学院'
    ELSE '艺术学院'
  END AS college,
  CASE MOD(seq.n, 6)
    WHEN 0 THEN CASE MOD(seq.n, 3) WHEN 0 THEN '软件工程' WHEN 1 THEN '计算机科学与技术' ELSE '数据科学' END
    WHEN 1 THEN CASE MOD(seq.n, 3) WHEN 0 THEN '通信工程' WHEN 1 THEN '电子信息工程' ELSE '自动化' END
    WHEN 2 THEN CASE MOD(seq.n, 3) WHEN 0 THEN '英语' WHEN 1 THEN '日语' ELSE '翻译' END
    WHEN 3 THEN CASE MOD(seq.n, 3) WHEN 0 THEN '会计学' WHEN 1 THEN '工商管理' ELSE '金融学' END
    WHEN 4 THEN CASE MOD(seq.n, 3) WHEN 0 THEN '土木工程' WHEN 1 THEN '工程管理' ELSE '建筑学' END
    ELSE CASE MOD(seq.n, 3) WHEN 0 THEN '视觉传达' WHEN 1 THEN '环境设计' ELSE '数字媒体艺术' END
  END AS major,
  CONCAT('20', 22 + MOD(seq.n, 4), '级', LPAD(1 + MOD(seq.n, 8), 2, '0'), '班') AS class_name,
  CONCAT('13', MOD(seq.n, 10), LPAD(MOD(CRC32(CONCAT(@DEMO_SEED_TAG, '_P_', seq.n)), 100000000), 8, '0')) AS phone,
  'IN_SCHOOL' AS status,
  0 AS deleted
FROM (SELECT n FROM demo_seq WHERE n <= @DEMO_STUDENT_COUNT) seq;

-- =========================
-- 5) Assignments (ACTIVE)
-- =========================
-- Target split across gender-limited buildings.
SET @DEMO_ACTIVE_MALE_TARGET := FLOOR(@DEMO_ACTIVE_ASSIGN_TARGET * 0.45);
SET @DEMO_ACTIVE_FEMALE_TARGET := FLOOR(@DEMO_ACTIVE_ASSIGN_TARGET * 0.45);
SET @DEMO_ACTIVE_UNLIMITED_TARGET := @DEMO_ACTIVE_ASSIGN_TARGET - @DEMO_ACTIVE_MALE_TARGET - @DEMO_ACTIVE_FEMALE_TARGET;

-- Male buildings -> male students
INSERT INTO dorm_assignment (student_id, bed_id, start_at, end_at, status, reason, deleted)
SELECT
  st.student_id,
  be.bed_id,
  TIMESTAMPADD(DAY, MOD(be.rn, 120), @DEMO_ASSIGN_BASE_START) AS start_at,
  NULL AS end_at,
  'ACTIVE' AS status,
  CONCAT(@DEMO_SEED_TAG, '_ACTIVE') AS reason,
  0 AS deleted
FROM (
  SELECT
    bed.id AS bed_id,
    ROW_NUMBER() OVER (ORDER BY bed.id) AS rn
  FROM dorm_bed bed
  JOIN dorm_room r ON r.id = bed.room_id
  JOIN dorm_building b ON b.id = r.building_id
  WHERE bed.deleted = 0
    AND r.deleted = 0
    AND b.deleted = 0
    AND b.code LIKE CONCAT(@DEMO_BUILDING_PREFIX, '%')
    AND b.gender_limit = 'MALE'
) be
JOIN (
  SELECT
    s.id AS student_id,
    ROW_NUMBER() OVER (ORDER BY s.id) AS rn
  FROM student s
  WHERE s.deleted = 0
    AND s.student_no LIKE 'SDEMO%'
    AND s.gender = 'MALE'
) st ON st.rn = be.rn
LEFT JOIN dorm_assignment a1 ON a1.student_id = st.student_id AND a1.status = 'ACTIVE' AND a1.deleted = 0
LEFT JOIN dorm_assignment a2 ON a2.bed_id = be.bed_id AND a2.status = 'ACTIVE' AND a2.deleted = 0
WHERE be.rn <= @DEMO_ACTIVE_MALE_TARGET
  AND a1.id IS NULL
  AND a2.id IS NULL
  AND NOT EXISTS (
    SELECT 1 FROM dorm_assignment x
    WHERE x.student_id = st.student_id
      AND x.bed_id = be.bed_id
      AND x.status = 'ACTIVE'
      AND x.reason = CONCAT(@DEMO_SEED_TAG, '_ACTIVE')
      AND x.deleted = 0
  );

-- Female buildings -> female students
INSERT INTO dorm_assignment (student_id, bed_id, start_at, end_at, status, reason, deleted)
SELECT
  st.student_id,
  be.bed_id,
  TIMESTAMPADD(DAY, MOD(be.rn, 120), @DEMO_ASSIGN_BASE_START) AS start_at,
  NULL AS end_at,
  'ACTIVE' AS status,
  CONCAT(@DEMO_SEED_TAG, '_ACTIVE') AS reason,
  0 AS deleted
FROM (
  SELECT
    bed.id AS bed_id,
    ROW_NUMBER() OVER (ORDER BY bed.id) AS rn
  FROM dorm_bed bed
  JOIN dorm_room r ON r.id = bed.room_id
  JOIN dorm_building b ON b.id = r.building_id
  WHERE bed.deleted = 0
    AND r.deleted = 0
    AND b.deleted = 0
    AND b.code LIKE CONCAT(@DEMO_BUILDING_PREFIX, '%')
    AND b.gender_limit = 'FEMALE'
) be
JOIN (
  SELECT
    s.id AS student_id,
    ROW_NUMBER() OVER (ORDER BY s.id) AS rn
  FROM student s
  WHERE s.deleted = 0
    AND s.student_no LIKE 'SDEMO%'
    AND s.gender = 'FEMALE'
) st ON st.rn = be.rn
LEFT JOIN dorm_assignment a1 ON a1.student_id = st.student_id AND a1.status = 'ACTIVE' AND a1.deleted = 0
LEFT JOIN dorm_assignment a2 ON a2.bed_id = be.bed_id AND a2.status = 'ACTIVE' AND a2.deleted = 0
WHERE be.rn <= @DEMO_ACTIVE_FEMALE_TARGET
  AND a1.id IS NULL
  AND a2.id IS NULL
  AND NOT EXISTS (
    SELECT 1 FROM dorm_assignment x
    WHERE x.student_id = st.student_id
      AND x.bed_id = be.bed_id
      AND x.status = 'ACTIVE'
      AND x.reason = CONCAT(@DEMO_SEED_TAG, '_ACTIVE')
      AND x.deleted = 0
  );

-- Unlimited buildings -> any demo students
INSERT INTO dorm_assignment (student_id, bed_id, start_at, end_at, status, reason, deleted)
SELECT
  st.student_id,
  be.bed_id,
  TIMESTAMPADD(DAY, MOD(be.rn, 120), @DEMO_ASSIGN_BASE_START) AS start_at,
  NULL AS end_at,
  'ACTIVE' AS status,
  CONCAT(@DEMO_SEED_TAG, '_ACTIVE') AS reason,
  0 AS deleted
FROM (
  SELECT
    bed.id AS bed_id,
    ROW_NUMBER() OVER (ORDER BY bed.id) AS rn
  FROM dorm_bed bed
  JOIN dorm_room r ON r.id = bed.room_id
  JOIN dorm_building b ON b.id = r.building_id
  WHERE bed.deleted = 0
    AND r.deleted = 0
    AND b.deleted = 0
    AND b.code LIKE CONCAT(@DEMO_BUILDING_PREFIX, '%')
    AND b.gender_limit = 'UNLIMITED'
) be
JOIN (
  SELECT
    s.id AS student_id,
    ROW_NUMBER() OVER (ORDER BY s.id) AS rn
  FROM student s
  WHERE s.deleted = 0
    AND s.student_no LIKE 'SDEMO%'
) st ON st.rn = be.rn
LEFT JOIN dorm_assignment a1 ON a1.student_id = st.student_id AND a1.status = 'ACTIVE' AND a1.deleted = 0
LEFT JOIN dorm_assignment a2 ON a2.bed_id = be.bed_id AND a2.status = 'ACTIVE' AND a2.deleted = 0
WHERE be.rn <= @DEMO_ACTIVE_UNLIMITED_TARGET
  AND a1.id IS NULL
  AND a2.id IS NULL
  AND NOT EXISTS (
    SELECT 1 FROM dorm_assignment x
    WHERE x.student_id = st.student_id
      AND x.bed_id = be.bed_id
      AND x.status = 'ACTIVE'
      AND x.reason = CONCAT(@DEMO_SEED_TAG, '_ACTIVE')
      AND x.deleted = 0
  );

-- =========================
-- 6) Assignments (ENDED, history)
-- =========================
INSERT INTO dorm_assignment (student_id, bed_id, start_at, end_at, status, reason, deleted)
SELECT
  st.student_id,
  be.bed_id,
  TIMESTAMPADD(DAY, -360 + MOD(CAST(be.rn AS SIGNED), 120), @DEMO_ASSIGN_BASE_START) AS start_at,
  TIMESTAMPADD(DAY, -300 + MOD(CAST(be.rn AS SIGNED), 120), @DEMO_ASSIGN_BASE_START) AS end_at,
  'ENDED' AS status,
  CONCAT(@DEMO_SEED_TAG, '_ENDED_', LPAD(be.rn, 6, '0')) AS reason,
  0 AS deleted
FROM (
  SELECT
    bed.id AS bed_id,
    ROW_NUMBER() OVER (ORDER BY bed.id) AS rn
  FROM dorm_bed bed
  JOIN dorm_room r ON r.id = bed.room_id
  JOIN dorm_building b ON b.id = r.building_id
  WHERE bed.deleted = 0
    AND r.deleted = 0
    AND b.deleted = 0
    AND b.code LIKE CONCAT(@DEMO_BUILDING_PREFIX, '%')
) be
JOIN (
  SELECT
    s.id AS student_id,
    ROW_NUMBER() OVER (ORDER BY s.id) AS rn
  FROM student s
  WHERE s.deleted = 0
    AND s.student_no LIKE 'SDEMO%'
) st ON st.rn = be.rn
WHERE be.rn <= @DEMO_ENDED_ASSIGN_TARGET
  AND NOT EXISTS (
    SELECT 1 FROM dorm_assignment x
    WHERE x.reason = CONCAT(@DEMO_SEED_TAG, '_ENDED_', LPAD(be.rn, 6, '0'))
      AND x.deleted = 0
  );

-- =========================
-- 7) Bed status sync for demo ACTIVE assignments only
-- =========================
UPDATE dorm_bed bed
JOIN dorm_assignment a
  ON a.bed_id = bed.id
 AND a.status = 'ACTIVE'
 AND a.deleted = 0
 AND a.reason = CONCAT(@DEMO_SEED_TAG, '_ACTIVE')
SET bed.status = 'OCCUPIED'
WHERE bed.deleted = 0
  AND bed.status <> 'OCCUPIED';

-- =========================
-- 8) Repair orders
-- =========================
SET @DEMO_ADMIN_ID := (SELECT id FROM sys_user WHERE username = 'admin' AND deleted = 0 ORDER BY id LIMIT 1);

INSERT INTO repair_order (student_id, building_id, room_id, title, description, priority, status, assignee_user_id, deleted, created_at, updated_at)
SELECT
  ds.student_id,
  dr.building_id,
  dr.room_id,
  CONCAT(@DEMO_SEED_TAG, '_RO_', LPAD(seq.n, 6, '0')) AS title,
  CONCAT('演示报修：',
    CASE MOD(CRC32(CONCAT(@DEMO_SEED_TAG, '_RO_D_', seq.n)), 8)
      WHEN 0 THEN '空调不制冷'
      WHEN 1 THEN '热水器漏水'
      WHEN 2 THEN '灯管闪烁'
      WHEN 3 THEN '水龙头滴水'
      WHEN 4 THEN '门锁损坏'
      WHEN 5 THEN '下水道堵塞'
      WHEN 6 THEN '床架异响'
      ELSE '网络故障'
    END
  ) AS description,
  CASE MOD(CRC32(CONCAT(@DEMO_SEED_TAG, '_RO_P_', seq.n)), 100)
    WHEN 0 THEN 'HIGH'
    WHEN 1 THEN 'HIGH'
    WHEN 2 THEN 'HIGH'
    WHEN 3 THEN 'LOW'
    WHEN 4 THEN 'LOW'
    ELSE 'MEDIUM'
  END AS priority,
  CASE
    WHEN MOD(CRC32(CONCAT(@DEMO_SEED_TAG, '_RO_S_', seq.n)), 100) < 50 THEN 'SUBMITTED'
    WHEN MOD(CRC32(CONCAT(@DEMO_SEED_TAG, '_RO_S_', seq.n)), 100) < 75 THEN 'IN_PROGRESS'
    WHEN MOD(CRC32(CONCAT(@DEMO_SEED_TAG, '_RO_S_', seq.n)), 100) < 95 THEN 'DONE'
    ELSE 'REJECTED'
  END AS status,
  CASE
    WHEN MOD(CRC32(CONCAT(@DEMO_SEED_TAG, '_RO_S_', seq.n)), 100) < 50 THEN NULL
    WHEN @DEMO_ADMIN_ID IS NULL THEN NULL
    ELSE @DEMO_ADMIN_ID
  END AS assignee_user_id,
  0 AS deleted,
  TIMESTAMPADD(HOUR, MOD(seq.n, 24), TIMESTAMPADD(DAY, MOD(seq.n, 45), @DEMO_REPAIR_BASE)) AS created_at,
  TIMESTAMPADD(HOUR, MOD(seq.n, 24), TIMESTAMPADD(DAY, MOD(seq.n, 45), @DEMO_REPAIR_BASE)) AS updated_at
FROM (SELECT n FROM demo_seq WHERE n <= @DEMO_REPAIR_ORDER_COUNT) seq
CROSS JOIN (
  SELECT COUNT(*) AS c
  FROM student s
  WHERE s.deleted = 0 AND s.student_no LIKE 'SDEMO%'
) st_cnt
CROSS JOIN (
  SELECT COUNT(*) AS c
  FROM dorm_room r
  JOIN dorm_building b ON b.id = r.building_id
  WHERE r.deleted = 0 AND b.deleted = 0 AND b.code LIKE CONCAT(@DEMO_BUILDING_PREFIX, '%')
) rm_cnt
JOIN (
  SELECT s.id AS student_id, ROW_NUMBER() OVER (ORDER BY s.id) AS rn
  FROM student s
  WHERE s.deleted = 0 AND s.student_no LIKE 'SDEMO%'
) ds
  ON ds.rn = 1 + MOD(seq.n - 1, st_cnt.c)
JOIN (
  SELECT r.id AS room_id, r.building_id, ROW_NUMBER() OVER (ORDER BY r.id) AS rn
  FROM dorm_room r
  JOIN dorm_building b ON b.id = r.building_id
  WHERE r.deleted = 0 AND b.deleted = 0 AND b.code LIKE CONCAT(@DEMO_BUILDING_PREFIX, '%')
) dr
  ON dr.rn = 1 + MOD(seq.n - 1, rm_cnt.c)
WHERE NOT EXISTS (
  SELECT 1 FROM repair_order ro
  WHERE ro.title = CONCAT(@DEMO_SEED_TAG, '_RO_', LPAD(seq.n, 6, '0'))
    AND ro.deleted = 0
);

-- =========================
-- 9) Repair logs (simple 1 log per order)
-- =========================
INSERT INTO repair_log (repair_order_id, action, content, deleted, created_at, created_by)
SELECT
  ro.id,
  'SEED',
  CONCAT(@DEMO_SEED_TAG, '_LOG_', ro.title) AS content,
  0 AS deleted,
  ro.created_at,
  NULL AS created_by
FROM repair_order ro
WHERE ro.deleted = 0
  AND ro.title LIKE CONCAT(@DEMO_SEED_TAG, '_RO_%')
  AND NOT EXISTS (
    SELECT 1 FROM repair_log rl
    WHERE rl.repair_order_id = ro.id
      AND rl.action = 'SEED'
      AND rl.content = CONCAT(@DEMO_SEED_TAG, '_LOG_', ro.title)
      AND rl.deleted = 0
  );

-- =========================
-- 10) Visitor records
-- =========================
INSERT INTO visitor_record (student_id, visitor_name, id_no, phone, visit_reason, visit_at, leave_at, status, deleted, created_at, updated_at)
SELECT
  ds.student_id,
  CONCAT('演示访客', LPAD(seq.n, 5, '0')) AS visitor_name,
  CONCAT(@DEMO_SEED_TAG, '_VID_', LPAD(seq.n, 6, '0')) AS id_no,
  CONCAT('15', MOD(seq.n, 10), LPAD(MOD(CRC32(CONCAT(@DEMO_SEED_TAG, '_VP_', seq.n)), 100000000), 8, '0')) AS phone,
  CASE MOD(CRC32(CONCAT(@DEMO_SEED_TAG, '_VR_', seq.n)), 5)
    WHEN 0 THEN '探望同学'
    WHEN 1 THEN '送物品'
    WHEN 2 THEN '家长来访'
    WHEN 3 THEN '快递取件'
    ELSE '校内参观'
  END AS visit_reason,
  TIMESTAMPADD(MINUTE, MOD(seq.n * 17, 1440), TIMESTAMPADD(DAY, MOD(seq.n, 60), @DEMO_VISIT_BASE)) AS visit_at,
  CASE
    WHEN MOD(CRC32(CONCAT(@DEMO_SEED_TAG, '_VS_', seq.n)), 100) < 45 THEN NULL
    ELSE TIMESTAMPADD(MINUTE, 30 + MOD(seq.n * 13, 360), TIMESTAMPADD(MINUTE, MOD(seq.n * 17, 1440), TIMESTAMPADD(DAY, MOD(seq.n, 60), @DEMO_VISIT_BASE)))
  END AS leave_at,
  CASE
    WHEN MOD(CRC32(CONCAT(@DEMO_SEED_TAG, '_VS_', seq.n)), 100) < 45 THEN 'IN'
    ELSE 'OUT'
  END AS status,
  0 AS deleted,
  TIMESTAMPADD(MINUTE, MOD(seq.n * 17, 1440), TIMESTAMPADD(DAY, MOD(seq.n, 60), @DEMO_VISIT_BASE)) AS created_at,
  TIMESTAMPADD(MINUTE, MOD(seq.n * 17, 1440), TIMESTAMPADD(DAY, MOD(seq.n, 60), @DEMO_VISIT_BASE)) AS updated_at
FROM (SELECT n FROM demo_seq WHERE n <= @DEMO_VISITOR_RECORD_COUNT) seq
CROSS JOIN (
  SELECT COUNT(*) AS c
  FROM student s
  WHERE s.deleted = 0 AND s.student_no LIKE 'SDEMO%'
) st_cnt
JOIN (
  SELECT s.id AS student_id, ROW_NUMBER() OVER (ORDER BY s.id) AS rn
  FROM student s
  WHERE s.deleted = 0 AND s.student_no LIKE 'SDEMO%'
) ds ON ds.rn = 1 + MOD(seq.n - 1, st_cnt.c)
WHERE NOT EXISTS (
  SELECT 1 FROM visitor_record vr
  WHERE vr.id_no = CONCAT(@DEMO_SEED_TAG, '_VID_', LPAD(seq.n, 6, '0'))
    AND vr.deleted = 0
);

-- =========================
-- 11) Notices
-- =========================
INSERT INTO notice (title, content, pinned, publish_status, publish_at, deleted, created_at, updated_at)
SELECT
  CONCAT(@DEMO_SEED_TAG, '_NOTICE_', LPAD(seq.n, 4, '0')) AS title,
  CONCAT('这是一条演示公告（', @DEMO_SEED_TAG, '）#', LPAD(seq.n, 4, '0'), '\n\n',
         '内容要点：\n',
         '- 宿舍卫生检查安排\n',
         '- 水电检修通知\n',
         '- 门禁管理提示\n') AS content,
  CASE WHEN MOD(seq.n, 20) = 0 THEN 1 ELSE 0 END AS pinned,
  CASE WHEN MOD(seq.n, 10) < 7 THEN 'PUBLISHED' ELSE 'DRAFT' END AS publish_status,
  CASE WHEN MOD(seq.n, 10) < 7 THEN TIMESTAMPADD(DAY, MOD(seq.n, 60), @DEMO_NOTICE_BASE) ELSE NULL END AS publish_at,
  0 AS deleted,
  TIMESTAMPADD(DAY, MOD(seq.n, 60), @DEMO_NOTICE_BASE) AS created_at,
  TIMESTAMPADD(DAY, MOD(seq.n, 60), @DEMO_NOTICE_BASE) AS updated_at
FROM (SELECT n FROM demo_seq WHERE n <= @DEMO_NOTICE_COUNT) seq
WHERE NOT EXISTS (
  SELECT 1 FROM notice n2
  WHERE n2.title = CONCAT(@DEMO_SEED_TAG, '_NOTICE_', LPAD(seq.n, 4, '0'))
    AND n2.deleted = 0
);

-- =========================
-- 12) Audit logs
-- =========================
INSERT INTO audit_log (action, resource_type, resource_id, operator_user_id, operator_username, success, detail, ip, user_agent, created_at)
SELECT
  CONCAT(@DEMO_SEED_TAG, '_ACTION_', LPAD(seq.n, 5, '0')) AS action,
  CASE MOD(seq.n, 5)
    WHEN 0 THEN 'dorm_building'
    WHEN 1 THEN 'dorm_room'
    WHEN 2 THEN 'dorm_bed'
    WHEN 3 THEN 'student'
    ELSE 'repair_order'
  END AS resource_type,
  CAST(seq.n AS CHAR) AS resource_id,
  @DEMO_ADMIN_ID AS operator_user_id,
  'admin' AS operator_username,
  CASE WHEN MOD(CRC32(CONCAT(@DEMO_SEED_TAG, '_AL_', seq.n)), 100) < 97 THEN 1 ELSE 0 END AS success,
  CONCAT('演示审计日志：', @DEMO_SEED_TAG, ' #', LPAD(seq.n, 5, '0')) AS detail,
  CONCAT('10.0.', MOD(seq.n, 255), '.', MOD(seq.n * 7, 255)) AS ip,
  'demo-seeder/1.0' AS user_agent,
  TIMESTAMPADD(MINUTE, MOD(seq.n * 11, 1440), TIMESTAMPADD(DAY, MOD(seq.n, 30), TIMESTAMP('2025-12-01 00:00:00'))) AS created_at
FROM (SELECT n FROM demo_seq WHERE n <= @DEMO_AUDIT_LOG_COUNT) seq
WHERE NOT EXISTS (
  SELECT 1 FROM audit_log al
  WHERE al.action = CONCAT(@DEMO_SEED_TAG, '_ACTION_', LPAD(seq.n, 5, '0'))
);

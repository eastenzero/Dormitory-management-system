-- Optional cleanup script for demo data seeded by V7__seed_big_demo_data.sql
--
-- IMPORTANT:
-- - This script is NOT managed by Flyway (not under db/migration).
-- - It only targets rows marked by the demo prefixes/tags.
-- - It performs SOFT DELETE where tables support `deleted`.
-- - It avoids touching non-demo data as much as possible, but you should run it only in dev/test.

SET @DEMO_SEED_TAG := 'DEMO_V7';
SET @DEMO_BUILDING_PREFIX := 'DEMO_B';

-- 1) assignments seeded by this demo
UPDATE dorm_assignment
SET deleted = 1
WHERE deleted = 0
  AND (
    reason = CONCAT(@DEMO_SEED_TAG, '_ACTIVE')
    OR reason LIKE CONCAT(@DEMO_SEED_TAG, '_ENDED_%')
  );

-- 2) reset bed status for beds that no longer have ACTIVE assignments
UPDATE dorm_bed bed
LEFT JOIN dorm_assignment a
  ON a.bed_id = bed.id
 AND a.status = 'ACTIVE'
 AND a.deleted = 0
SET bed.status = 'AVAILABLE'
WHERE bed.deleted = 0
  AND a.id IS NULL;

-- 3) soft delete demo repair logs/orders
UPDATE repair_log rl
JOIN repair_order ro ON ro.id = rl.repair_order_id
SET rl.deleted = 1
WHERE rl.deleted = 0
  AND ro.title LIKE CONCAT(@DEMO_SEED_TAG, '_RO_%')
  AND ro.deleted = 0;

UPDATE repair_order
SET deleted = 1
WHERE deleted = 0
  AND title LIKE CONCAT(@DEMO_SEED_TAG, '_RO_%');

-- 4) soft delete demo visitor records
UPDATE visitor_record
SET deleted = 1
WHERE deleted = 0
  AND id_no LIKE CONCAT(@DEMO_SEED_TAG, '_VID_%');

-- 5) soft delete demo notices
UPDATE notice
SET deleted = 1
WHERE deleted = 0
  AND title LIKE CONCAT(@DEMO_SEED_TAG, '_NOTICE_%');

-- 6) delete demo audit logs (audit_log has no deleted column)
DELETE FROM audit_log
WHERE action LIKE CONCAT(@DEMO_SEED_TAG, '_ACTION_%');

-- 7) soft delete demo students ONLY if they have no remaining non-deleted assignments
UPDATE student s
LEFT JOIN dorm_assignment a
  ON a.student_id = s.id
 AND a.deleted = 0
SET s.deleted = 1
WHERE s.deleted = 0
  AND s.student_no LIKE 'SDEMO%'
  AND a.id IS NULL;

-- 8) soft delete demo beds/rooms/buildings ONLY if no remaining non-deleted assignments reference them
UPDATE dorm_bed bed
JOIN dorm_room r ON r.id = bed.room_id
JOIN dorm_building b ON b.id = r.building_id
LEFT JOIN dorm_assignment a
  ON a.bed_id = bed.id
 AND a.deleted = 0
SET bed.deleted = 1
WHERE bed.deleted = 0
  AND b.code LIKE CONCAT(@DEMO_BUILDING_PREFIX, '%')
  AND a.id IS NULL;

UPDATE dorm_room r
JOIN dorm_building b ON b.id = r.building_id
LEFT JOIN dorm_bed bed
  ON bed.room_id = r.id
 AND bed.deleted = 0
SET r.deleted = 1
WHERE r.deleted = 0
  AND b.code LIKE CONCAT(@DEMO_BUILDING_PREFIX, '%')
  AND bed.id IS NULL;

UPDATE dorm_building b
LEFT JOIN dorm_room r
  ON r.building_id = b.id
 AND r.deleted = 0
SET b.deleted = 1
WHERE b.deleted = 0
  AND b.code LIKE CONCAT(@DEMO_BUILDING_PREFIX, '%')
  AND r.id IS NULL;

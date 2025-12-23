ALTER TABLE dorm_room
  ADD CONSTRAINT fk_dorm_room_building
    FOREIGN KEY (building_id) REFERENCES dorm_building(id);

ALTER TABLE dorm_bed
  ADD CONSTRAINT fk_dorm_bed_room
    FOREIGN KEY (room_id) REFERENCES dorm_room(id);

ALTER TABLE dorm_assignment
  ADD CONSTRAINT fk_dorm_assignment_student
    FOREIGN KEY (student_id) REFERENCES student(id);

ALTER TABLE dorm_assignment
  ADD CONSTRAINT fk_dorm_assignment_bed
    FOREIGN KEY (bed_id) REFERENCES dorm_bed(id);

CREATE INDEX idx_assignment_bed_status ON dorm_assignment (bed_id, status);

INSERT IGNORE INTO sys_permission (code, name) VALUES ('dorm:building:read', '查看楼栋');
INSERT IGNORE INTO sys_permission (code, name) VALUES ('dorm:building:write', '编辑楼栋');
INSERT IGNORE INTO sys_permission (code, name) VALUES ('dorm:room:read', '查看房间');
INSERT IGNORE INTO sys_permission (code, name) VALUES ('dorm:room:write', '编辑房间');
INSERT IGNORE INTO sys_permission (code, name) VALUES ('dorm:bed:read', '查看床位');
INSERT IGNORE INTO sys_permission (code, name) VALUES ('dorm:bed:write', '编辑床位');
INSERT IGNORE INTO sys_permission (code, name) VALUES ('dorm:assignment:read', '查看入住记录');
INSERT IGNORE INTO sys_permission (code, name) VALUES ('dorm:assignment:write', '办理入住/调宿/退宿');
INSERT IGNORE INTO sys_permission (code, name) VALUES ('student:read', '查看学生');
INSERT IGNORE INTO sys_permission (code, name) VALUES ('student:write', '编辑学生');

INSERT IGNORE INTO sys_role_permission (role_id, permission_id)
SELECT r.id, p.id
FROM sys_role r
JOIN sys_permission p
WHERE r.code = 'SUPER_ADMIN'
  AND p.code IN (
    'dorm:building:read','dorm:building:write',
    'dorm:room:read','dorm:room:write',
    'dorm:bed:read','dorm:bed:write',
    'dorm:assignment:read','dorm:assignment:write',
    'student:read','student:write'
  );

INSERT IGNORE INTO dorm_building (code, name, gender_limit, address, status, deleted)
VALUES ('B1', '一号楼', 'UNLIMITED', NULL, 'ACTIVE', 0);

INSERT IGNORE INTO dorm_room (building_id, floor_no, room_no, room_type, gender_limit, status, deleted)
SELECT b.id, 3, '305', '4人间', 'UNLIMITED', 'ACTIVE', 0
FROM dorm_building b
WHERE b.code = 'B1';

INSERT IGNORE INTO dorm_bed (room_id, bed_no, status, deleted)
SELECT r.id, 'A', 'AVAILABLE', 0
FROM dorm_room r
JOIN dorm_building b ON b.id = r.building_id
WHERE b.code = 'B1' AND r.room_no = '305';

INSERT IGNORE INTO dorm_bed (room_id, bed_no, status, deleted)
SELECT r.id, 'B', 'AVAILABLE', 0
FROM dorm_room r
JOIN dorm_building b ON b.id = r.building_id
WHERE b.code = 'B1' AND r.room_no = '305';

INSERT IGNORE INTO dorm_bed (room_id, bed_no, status, deleted)
SELECT r.id, 'C', 'AVAILABLE', 0
FROM dorm_room r
JOIN dorm_building b ON b.id = r.building_id
WHERE b.code = 'B1' AND r.room_no = '305';

INSERT IGNORE INTO dorm_bed (room_id, bed_no, status, deleted)
SELECT r.id, 'D', 'AVAILABLE', 0
FROM dorm_room r
JOIN dorm_building b ON b.id = r.building_id
WHERE b.code = 'B1' AND r.room_no = '305';

INSERT IGNORE INTO student (student_no, name, gender, status, deleted)
VALUES ('S0001', '张三', 'MALE', 'IN_SCHOOL', 0);

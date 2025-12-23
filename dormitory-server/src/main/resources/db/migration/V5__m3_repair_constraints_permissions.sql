ALTER TABLE repair_order
  ADD CONSTRAINT fk_repair_order_student
    FOREIGN KEY (student_id) REFERENCES student(id);

ALTER TABLE repair_order
  ADD CONSTRAINT fk_repair_order_building
    FOREIGN KEY (building_id) REFERENCES dorm_building(id);

ALTER TABLE repair_order
  ADD CONSTRAINT fk_repair_order_room
    FOREIGN KEY (room_id) REFERENCES dorm_room(id);

ALTER TABLE repair_order
  ADD CONSTRAINT fk_repair_order_assignee_user
    FOREIGN KEY (assignee_user_id) REFERENCES sys_user(id);

ALTER TABLE repair_log
  ADD CONSTRAINT fk_repair_log_order
    FOREIGN KEY (repair_order_id) REFERENCES repair_order(id);

UPDATE repair_order SET priority = UPPER(priority) WHERE priority IS NOT NULL;
UPDATE repair_order SET status = UPPER(status) WHERE status IS NOT NULL;
UPDATE repair_order SET status = 'IN_PROGRESS' WHERE status = 'ASSIGNED';
UPDATE repair_order SET status = 'DONE' WHERE status IN ('RESOLVED', 'CLOSED');

ALTER TABLE repair_order
  ADD CONSTRAINT chk_repair_order_priority
    CHECK (priority IN ('LOW','MEDIUM','HIGH')),
  ADD CONSTRAINT chk_repair_order_status
    CHECK (status IN ('SUBMITTED','IN_PROGRESS','DONE','REJECTED'));

CREATE INDEX idx_repair_status_assignee_created_at ON repair_order (status, assignee_user_id, created_at);
CREATE INDEX idx_repair_assignee_created_at ON repair_order (assignee_user_id, created_at);
CREATE INDEX idx_repair_priority ON repair_order (priority);
CREATE INDEX idx_repair_created_at ON repair_order (created_at);

INSERT IGNORE INTO sys_permission (code, name) VALUES ('repair:order:read', '查看报修工单');
INSERT IGNORE INTO sys_permission (code, name) VALUES ('repair:order:write', '编辑报修工单');
INSERT IGNORE INTO sys_permission (code, name) VALUES ('repair:order:assign', '指派报修工单');
INSERT IGNORE INTO sys_permission (code, name) VALUES ('repair:log:read', '查看报修日志');

INSERT IGNORE INTO sys_role_permission (role_id, permission_id)
SELECT r.id, p.id
FROM sys_role r
JOIN sys_permission p
WHERE r.code = 'SUPER_ADMIN'
  AND p.code IN (
    'repair:order:read','repair:order:write','repair:order:assign','repair:log:read'
  );

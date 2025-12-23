ALTER TABLE visitor_record
  ADD CONSTRAINT fk_visitor_record_student
    FOREIGN KEY (student_id) REFERENCES student(id);

ALTER TABLE visitor_record
  ADD CONSTRAINT chk_visitor_record_status
    CHECK (status IN ('IN','OUT')),
  ADD CONSTRAINT chk_visitor_record_leave_at
    CHECK ((status='IN' AND leave_at IS NULL) OR (status='OUT' AND leave_at IS NOT NULL)),
  ADD CONSTRAINT chk_visitor_record_time_order
    CHECK (leave_at IS NULL OR leave_at >= visit_at);

CREATE INDEX idx_visitor_status_visit_at ON visitor_record (status, visit_at);
CREATE INDEX idx_visitor_visit_at ON visitor_record (visit_at);
CREATE INDEX idx_visitor_student_visit_at ON visitor_record (student_id, visit_at);
CREATE INDEX idx_visitor_name ON visitor_record (visitor_name);

INSERT IGNORE INTO sys_permission (code, name) VALUES ('visitor:record:read', '查看访客登记');
INSERT IGNORE INTO sys_permission (code, name) VALUES ('visitor:record:write', '登记访客进出');

INSERT IGNORE INTO sys_role_permission (role_id, permission_id)
SELECT r.id, p.id
FROM sys_role r
JOIN sys_permission p
WHERE r.code = 'SUPER_ADMIN'
  AND p.code IN (
    'visitor:record:read','visitor:record:write'
  );

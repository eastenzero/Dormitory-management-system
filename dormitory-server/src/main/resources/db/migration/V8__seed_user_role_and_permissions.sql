INSERT IGNORE INTO sys_role (code, name) VALUES ('USER', '普通用户');

-- 给普通用户只读权限（可访问主要列表页）
INSERT IGNORE INTO sys_role_permission (role_id, permission_id)
SELECT r.id, p.id
FROM sys_role r
JOIN sys_permission p
WHERE r.code = 'USER'
  AND p.code IN (
    'dorm:building:read',
    'dorm:room:read',
    'dorm:bed:read',
    'dorm:assignment:read',
    'student:read',
    'repair:order:read',
    'repair:log:read',
    'visitor:record:read'
  );

-- 将默认用户 user 绑定为普通用户
INSERT IGNORE INTO sys_user_role (user_id, role_id)
SELECT u.id, r.id
FROM sys_user u
JOIN sys_role r
WHERE u.username = 'user'
  AND r.code = 'USER';

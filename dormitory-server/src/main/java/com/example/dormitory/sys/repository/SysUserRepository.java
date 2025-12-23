package com.example.dormitory.sys.repository;

import com.example.dormitory.sys.model.SysUser;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;

@Repository
public class SysUserRepository {

    private final JdbcTemplate jdbcTemplate;

    public SysUserRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public SysUser findByUsername(String username) {
        List<SysUser> list = jdbcTemplate.query(
                "SELECT id, username, password_hash, real_name, status, deleted FROM sys_user WHERE username=? LIMIT 1",
                userRowMapper(),
                username
        );
        return list.isEmpty() ? null : list.get(0);
    }

    public SysUser findById(Long id) {
        List<SysUser> list = jdbcTemplate.query(
                "SELECT id, username, password_hash, real_name, status, deleted FROM sys_user WHERE id=? LIMIT 1",
                userRowMapper(),
                id
        );
        return list.isEmpty() ? null : list.get(0);
    }

    public List<String> findRoleCodesByUserId(Long userId) {
        return jdbcTemplate.query(
                "SELECT r.code FROM sys_role r JOIN sys_user_role ur ON ur.role_id=r.id WHERE ur.user_id=? ORDER BY r.id",
                (rs, rowNum) -> rs.getString("code"),
                userId
        );
    }

    public List<String> findPermissionCodesByUserId(Long userId) {
        return jdbcTemplate.query(
                "SELECT DISTINCT p.code " +
                        "FROM sys_permission p " +
                        "JOIN sys_role_permission rp ON rp.permission_id=p.id " +
                        "JOIN sys_user_role ur ON ur.role_id=rp.role_id " +
                        "WHERE ur.user_id=? " +
                        "ORDER BY p.code",
                (rs, rowNum) -> rs.getString("code"),
                userId
        );
    }

    public List<SysUser> listUsers() {
        return jdbcTemplate.query(
                "SELECT id, username, password_hash, real_name, status, deleted FROM sys_user WHERE deleted=0 ORDER BY id LIMIT 200",
                userRowMapper()
        );
    }

    public Long insert(SysUser user) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(con -> {
            PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO sys_user (username, password_hash, real_name, status, deleted) VALUES (?,?,?,?,?)",
                    Statement.RETURN_GENERATED_KEYS
            );
            ps.setString(1, user.getUsername());
            ps.setString(2, user.getPasswordHash());
            ps.setString(3, user.getRealName());
            ps.setString(4, user.getStatus());
            ps.setInt(5, user.getDeleted() == null ? 0 : user.getDeleted());
            return ps;
        }, keyHolder);
        Number key = keyHolder.getKey();
        return key == null ? null : key.longValue();
    }

    public void bindRoleByCode(Long userId, String roleCode) {
        jdbcTemplate.update(
                "INSERT IGNORE INTO sys_user_role (user_id, role_id) " +
                        "SELECT ?, r.id FROM sys_role r WHERE r.code=?",
                userId,
                roleCode
        );
    }

    private RowMapper<SysUser> userRowMapper() {
        return (rs, rowNum) -> {
            SysUser u = new SysUser();
            u.setId(rs.getLong("id"));
            u.setUsername(rs.getString("username"));
            u.setPasswordHash(rs.getString("password_hash"));
            u.setRealName(rs.getString("real_name"));
            u.setStatus(rs.getString("status"));
            u.setDeleted(rs.getInt("deleted"));
            return u;
        };
    }
}

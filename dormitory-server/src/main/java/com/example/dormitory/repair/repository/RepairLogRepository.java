package com.example.dormitory.repair.repository;

import com.example.dormitory.repair.model.RepairLog;
import com.example.dormitory.repair.vo.RepairLogVo;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.List;

@Repository
public class RepairLogRepository {

    private final JdbcTemplate jdbcTemplate;

    public RepairLogRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<RepairLogVo> listByOrderId(Long repairOrderId) {
        return jdbcTemplate.query(
                "SELECT id, repair_order_id, action, content, created_at, created_by " +
                        "FROM repair_log WHERE repair_order_id=? AND deleted=0 ORDER BY id ASC",
                (rs, rowNum) -> {
                    RepairLogVo vo = new RepairLogVo();
                    vo.setId(rs.getLong("id"));
                    vo.setRepairOrderId(rs.getLong("repair_order_id"));
                    vo.setAction(rs.getString("action"));
                    vo.setContent(rs.getString("content"));
                    Timestamp c = rs.getTimestamp("created_at");
                    vo.setCreatedAt(c == null ? null : c.toLocalDateTime());
                    long cb = rs.getLong("created_by");
                    vo.setCreatedBy(rs.wasNull() ? null : cb);
                    return vo;
                },
                repairOrderId
        );
    }

    public Long insert(RepairLog log) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(con -> {
            PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO repair_log(repair_order_id, action, content, deleted, created_by) VALUES (?,?,?,0,?)",
                    Statement.RETURN_GENERATED_KEYS
            );
            ps.setLong(1, log.getRepairOrderId());
            ps.setString(2, log.getAction());
            ps.setString(3, log.getContent());
            if (log.getCreatedBy() == null) {
                ps.setObject(4, null);
            } else {
                ps.setLong(4, log.getCreatedBy());
            }
            return ps;
        }, keyHolder);
        Number key = keyHolder.getKey();
        return key == null ? null : key.longValue();
    }
}

package com.example.dormitory.visitor.repository;

import com.example.dormitory.visitor.model.VisitorRecord;
import com.example.dormitory.visitor.vo.VisitorRecordVo;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Repository
public class VisitorRecordRepository {

    private final JdbcTemplate jdbcTemplate;

    public VisitorRecordRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public VisitorRecord findById(Long id) {
        List<VisitorRecord> list = jdbcTemplate.query(
                "SELECT id, student_id, visitor_name, id_no, phone, visit_reason, visit_at, leave_at, status, deleted, created_by, updated_by " +
                        "FROM visitor_record WHERE id=? AND deleted=0 LIMIT 1",
                (rs, rowNum) -> {
                    VisitorRecord r = new VisitorRecord();
                    r.setId(rs.getLong("id"));
                    long sid = rs.getLong("student_id");
                    r.setStudentId(rs.wasNull() ? null : sid);
                    r.setVisitorName(rs.getString("visitor_name"));
                    r.setIdNo(rs.getString("id_no"));
                    r.setPhone(rs.getString("phone"));
                    r.setVisitReason(rs.getString("visit_reason"));
                    Timestamp va = rs.getTimestamp("visit_at");
                    r.setVisitAt(va == null ? null : va.toLocalDateTime());
                    Timestamp la = rs.getTimestamp("leave_at");
                    r.setLeaveAt(la == null ? null : la.toLocalDateTime());
                    r.setStatus(rs.getString("status"));
                    r.setDeleted(rs.getInt("deleted"));
                    long cb = rs.getLong("created_by");
                    r.setCreatedBy(rs.wasNull() ? null : cb);
                    long ub = rs.getLong("updated_by");
                    r.setUpdatedBy(rs.wasNull() ? null : ub);
                    return r;
                },
                id
        );
        return list.isEmpty() ? null : list.get(0);
    }

    public VisitorRecordVo findVoById(Long id) {
        List<VisitorRecordVo> list = jdbcTemplate.query(
                "SELECT v.id, v.student_id, s.student_no, s.name AS student_name, v.visitor_name, v.id_no, v.phone, v.visit_reason, v.visit_at, v.leave_at, v.status, v.created_at " +
                        "FROM visitor_record v " +
                        "LEFT JOIN student s ON s.id=v.student_id " +
                        "WHERE v.id=? AND v.deleted=0 LIMIT 1",
                (rs, rowNum) -> {
                    VisitorRecordVo vo = new VisitorRecordVo();
                    vo.setId(rs.getLong("id"));
                    long sid = rs.getLong("student_id");
                    vo.setStudentId(rs.wasNull() ? null : sid);
                    vo.setStudentNo(rs.getString("student_no"));
                    vo.setStudentName(rs.getString("student_name"));
                    vo.setVisitorName(rs.getString("visitor_name"));
                    vo.setIdNo(rs.getString("id_no"));
                    vo.setPhone(rs.getString("phone"));
                    vo.setVisitReason(rs.getString("visit_reason"));
                    Timestamp va = rs.getTimestamp("visit_at");
                    vo.setVisitAt(va == null ? null : va.toLocalDateTime());
                    Timestamp la = rs.getTimestamp("leave_at");
                    vo.setLeaveAt(la == null ? null : la.toLocalDateTime());
                    vo.setStatus(rs.getString("status"));
                    Timestamp ca = rs.getTimestamp("created_at");
                    vo.setCreatedAt(ca == null ? null : ca.toLocalDateTime());
                    return vo;
                },
                id
        );
        return list.isEmpty() ? null : list.get(0);
    }

    public long count(String status, String keyword, LocalDateTime fromAt, LocalDateTime toAt, Long studentId) {
        StringBuilder sql = new StringBuilder(
                "SELECT COUNT(*) FROM visitor_record v " +
                        "LEFT JOIN student s ON s.id=v.student_id " +
                        "WHERE v.deleted=0"
        );
        List<Object> args = new ArrayList<>();

        if (status != null && !status.isBlank()) {
            sql.append(" AND v.status=?");
            args.add(status.trim());
        }
        if (studentId != null) {
            sql.append(" AND v.student_id=?");
            args.add(studentId);
        }
        if (fromAt != null) {
            sql.append(" AND v.visit_at>=?");
            args.add(Timestamp.valueOf(fromAt));
        }
        if (toAt != null) {
            sql.append(" AND v.visit_at<=?");
            args.add(Timestamp.valueOf(toAt));
        }
        if (keyword != null && !keyword.isBlank()) {
            sql.append(" AND (v.visitor_name LIKE ? OR v.id_no LIKE ? OR v.phone LIKE ? OR v.visit_reason LIKE ? OR s.student_no LIKE ? OR s.name LIKE ?)");
            String like = "%" + keyword.trim() + "%";
            args.add(like);
            args.add(like);
            args.add(like);
            args.add(like);
            args.add(like);
            args.add(like);
        }

        Long cnt = jdbcTemplate.queryForObject(sql.toString(), args.toArray(), Long.class);
        return cnt == null ? 0 : cnt;
    }

    public List<VisitorRecordVo> list(String status, String keyword, LocalDateTime fromAt, LocalDateTime toAt, Long studentId,
                                     String orderBy, int limit, int offset) {
        StringBuilder sql = new StringBuilder(
                "SELECT v.id, v.student_id, s.student_no, s.name AS student_name, v.visitor_name, v.id_no, v.phone, v.visit_reason, v.visit_at, v.leave_at, v.status, v.created_at " +
                        "FROM visitor_record v " +
                        "LEFT JOIN student s ON s.id=v.student_id " +
                        "WHERE v.deleted=0"
        );
        List<Object> args = new ArrayList<>();

        if (status != null && !status.isBlank()) {
            sql.append(" AND v.status=?");
            args.add(status.trim());
        }
        if (studentId != null) {
            sql.append(" AND v.student_id=?");
            args.add(studentId);
        }
        if (fromAt != null) {
            sql.append(" AND v.visit_at>=?");
            args.add(Timestamp.valueOf(fromAt));
        }
        if (toAt != null) {
            sql.append(" AND v.visit_at<=?");
            args.add(Timestamp.valueOf(toAt));
        }
        if (keyword != null && !keyword.isBlank()) {
            sql.append(" AND (v.visitor_name LIKE ? OR v.id_no LIKE ? OR v.phone LIKE ? OR v.visit_reason LIKE ? OR s.student_no LIKE ? OR s.name LIKE ?)");
            String like = "%" + keyword.trim() + "%";
            args.add(like);
            args.add(like);
            args.add(like);
            args.add(like);
            args.add(like);
            args.add(like);
        }

        sql.append(" ORDER BY ").append(orderBy).append(" LIMIT ? OFFSET ?");
        args.add(limit);
        args.add(offset);

        return jdbcTemplate.query(sql.toString(), (rs, rowNum) -> {
            VisitorRecordVo vo = new VisitorRecordVo();
            vo.setId(rs.getLong("id"));
            long sid = rs.getLong("student_id");
            vo.setStudentId(rs.wasNull() ? null : sid);
            vo.setStudentNo(rs.getString("student_no"));
            vo.setStudentName(rs.getString("student_name"));
            vo.setVisitorName(rs.getString("visitor_name"));
            vo.setIdNo(rs.getString("id_no"));
            vo.setPhone(rs.getString("phone"));
            vo.setVisitReason(rs.getString("visit_reason"));
            Timestamp va = rs.getTimestamp("visit_at");
            vo.setVisitAt(va == null ? null : va.toLocalDateTime());
            Timestamp la = rs.getTimestamp("leave_at");
            vo.setLeaveAt(la == null ? null : la.toLocalDateTime());
            vo.setStatus(rs.getString("status"));
            Timestamp ca = rs.getTimestamp("created_at");
            vo.setCreatedAt(ca == null ? null : ca.toLocalDateTime());
            return vo;
        }, args.toArray());
    }

    public Long insert(VisitorRecord r) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(con -> {
            PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO visitor_record(student_id, visitor_name, id_no, phone, visit_reason, visit_at, leave_at, status, deleted, created_by, updated_by) " +
                            "VALUES (?,?,?,?,?,?,?,?,0,?,?)",
                    Statement.RETURN_GENERATED_KEYS
            );
            if (r.getStudentId() == null) {
                ps.setObject(1, null);
            } else {
                ps.setLong(1, r.getStudentId());
            }
            ps.setString(2, r.getVisitorName());
            ps.setString(3, r.getIdNo());
            ps.setString(4, r.getPhone());
            ps.setString(5, r.getVisitReason());
            ps.setTimestamp(6, Timestamp.valueOf(r.getVisitAt()));
            if (r.getLeaveAt() == null) {
                ps.setTimestamp(7, null);
            } else {
                ps.setTimestamp(7, Timestamp.valueOf(r.getLeaveAt()));
            }
            ps.setString(8, r.getStatus());
            if (r.getCreatedBy() == null) {
                ps.setObject(9, null);
            } else {
                ps.setLong(9, r.getCreatedBy());
            }
            if (r.getUpdatedBy() == null) {
                ps.setObject(10, null);
            } else {
                ps.setLong(10, r.getUpdatedBy());
            }
            return ps;
        }, keyHolder);
        Number key = keyHolder.getKey();
        return key == null ? null : key.longValue();
    }

    public int leaveIfIn(Long id, LocalDateTime leaveAt, Long operatorUserId) {
        return jdbcTemplate.update(
                "UPDATE visitor_record SET status='OUT', leave_at=?, updated_by=? WHERE id=? AND deleted=0 AND status='IN' AND leave_at IS NULL",
                ps -> {
                    ps.setTimestamp(1, Timestamp.valueOf(leaveAt));
                    if (operatorUserId == null) {
                        ps.setObject(2, null);
                    } else {
                        ps.setLong(2, operatorUserId);
                    }
                    ps.setLong(3, id);
                }
        );
    }
}

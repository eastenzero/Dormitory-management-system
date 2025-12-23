package com.example.dormitory.dorm.repository;

import com.example.dormitory.dorm.model.DormAssignment;
import com.example.dormitory.dorm.vo.DormAssignmentVo;
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
public class DormAssignmentRepository {

    private final JdbcTemplate jdbcTemplate;

    public DormAssignmentRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public DormAssignment findById(Long id) {
        List<DormAssignment> list = jdbcTemplate.query(
                "SELECT id, student_id, bed_id, start_at, end_at, status, reason, deleted FROM dorm_assignment WHERE id=? AND deleted=0 LIMIT 1",
                (rs, rowNum) -> {
                    DormAssignment a = new DormAssignment();
                    a.setId(rs.getLong("id"));
                    a.setStudentId(rs.getLong("student_id"));
                    a.setBedId(rs.getLong("bed_id"));
                    Timestamp s = rs.getTimestamp("start_at");
                    a.setStartAt(s == null ? null : s.toLocalDateTime());
                    Timestamp e = rs.getTimestamp("end_at");
                    a.setEndAt(e == null ? null : e.toLocalDateTime());
                    a.setStatus(rs.getString("status"));
                    a.setReason(rs.getString("reason"));
                    a.setDeleted(rs.getInt("deleted"));
                    return a;
                },
                id
        );
        return list.isEmpty() ? null : list.get(0);
    }

    public DormAssignmentVo findVoById(Long id) {
        List<DormAssignmentVo> list = jdbcTemplate.query(
                "SELECT a.id, a.student_id, s.student_no, s.name AS student_name, a.bed_id, bed.bed_no, r.id AS room_id, r.room_no, b.id AS building_id, b.code AS building_code, b.name AS building_name, a.start_at, a.end_at, a.status, a.reason " +
                        "FROM dorm_assignment a " +
                        "JOIN student s ON s.id=a.student_id " +
                        "JOIN dorm_bed bed ON bed.id=a.bed_id " +
                        "JOIN dorm_room r ON r.id=bed.room_id " +
                        "JOIN dorm_building b ON b.id=r.building_id " +
                        "WHERE a.id=? AND a.deleted=0 LIMIT 1",
                (rs, rowNum) -> {
                    DormAssignmentVo vo = new DormAssignmentVo();
                    vo.setId(rs.getLong("id"));
                    vo.setStudentId(rs.getLong("student_id"));
                    vo.setStudentNo(rs.getString("student_no"));
                    vo.setStudentName(rs.getString("student_name"));
                    vo.setBedId(rs.getLong("bed_id"));
                    vo.setBedNo(rs.getString("bed_no"));
                    vo.setRoomId(rs.getLong("room_id"));
                    vo.setRoomNo(rs.getString("room_no"));
                    vo.setBuildingId(rs.getLong("building_id"));
                    vo.setBuildingCode(rs.getString("building_code"));
                    vo.setBuildingName(rs.getString("building_name"));
                    Timestamp s = rs.getTimestamp("start_at");
                    vo.setStartAt(s == null ? null : s.toLocalDateTime());
                    Timestamp e = rs.getTimestamp("end_at");
                    vo.setEndAt(e == null ? null : e.toLocalDateTime());
                    vo.setStatus(rs.getString("status"));
                    vo.setReason(rs.getString("reason"));
                    return vo;
                },
                id
        );
        return list.isEmpty() ? null : list.get(0);
    }

    public long count(Long studentId, Long buildingId, Long roomId, Long bedId, String status) {
        StringBuilder sql = new StringBuilder(
                "SELECT COUNT(*) FROM dorm_assignment a " +
                        "JOIN dorm_bed bed ON bed.id=a.bed_id " +
                        "JOIN dorm_room r ON r.id=bed.room_id " +
                        "WHERE a.deleted=0"
        );
        List<Object> args = new ArrayList<>();
        if (studentId != null) {
            sql.append(" AND a.student_id=?");
            args.add(studentId);
        }
        if (buildingId != null) {
            sql.append(" AND r.building_id=?");
            args.add(buildingId);
        }
        if (roomId != null) {
            sql.append(" AND bed.room_id=?");
            args.add(roomId);
        }
        if (bedId != null) {
            sql.append(" AND a.bed_id=?");
            args.add(bedId);
        }
        if (status != null && !status.isBlank()) {
            sql.append(" AND a.status=?");
            args.add(status.trim());
        }
        Long cnt = jdbcTemplate.queryForObject(sql.toString(), args.toArray(), Long.class);
        return cnt == null ? 0 : cnt;
    }

    public List<DormAssignmentVo> list(Long studentId, Long buildingId, Long roomId, Long bedId, String status, String orderBy, int limit, int offset) {
        StringBuilder sql = new StringBuilder(
                "SELECT a.id, a.student_id, s.student_no, s.name AS student_name, a.bed_id, bed.bed_no, r.id AS room_id, r.room_no, b.id AS building_id, b.code AS building_code, b.name AS building_name, a.start_at, a.end_at, a.status, a.reason " +
                        "FROM dorm_assignment a " +
                        "JOIN student s ON s.id=a.student_id " +
                        "JOIN dorm_bed bed ON bed.id=a.bed_id " +
                        "JOIN dorm_room r ON r.id=bed.room_id " +
                        "JOIN dorm_building b ON b.id=r.building_id " +
                        "WHERE a.deleted=0"
        );
        List<Object> args = new ArrayList<>();
        if (studentId != null) {
            sql.append(" AND a.student_id=?");
            args.add(studentId);
        }
        if (buildingId != null) {
            sql.append(" AND r.building_id=?");
            args.add(buildingId);
        }
        if (roomId != null) {
            sql.append(" AND bed.room_id=?");
            args.add(roomId);
        }
        if (bedId != null) {
            sql.append(" AND a.bed_id=?");
            args.add(bedId);
        }
        if (status != null && !status.isBlank()) {
            sql.append(" AND a.status=?");
            args.add(status.trim());
        }

        sql.append(" ORDER BY ").append(orderBy).append(" LIMIT ? OFFSET ?");
        args.add(limit);
        args.add(offset);

        return jdbcTemplate.query(sql.toString(), (rs, rowNum) -> {
            DormAssignmentVo vo = new DormAssignmentVo();
            vo.setId(rs.getLong("id"));
            vo.setStudentId(rs.getLong("student_id"));
            vo.setStudentNo(rs.getString("student_no"));
            vo.setStudentName(rs.getString("student_name"));
            vo.setBedId(rs.getLong("bed_id"));
            vo.setBedNo(rs.getString("bed_no"));
            vo.setRoomId(rs.getLong("room_id"));
            vo.setRoomNo(rs.getString("room_no"));
            vo.setBuildingId(rs.getLong("building_id"));
            vo.setBuildingCode(rs.getString("building_code"));
            vo.setBuildingName(rs.getString("building_name"));
            Timestamp s = rs.getTimestamp("start_at");
            vo.setStartAt(s == null ? null : s.toLocalDateTime());
            Timestamp e = rs.getTimestamp("end_at");
            vo.setEndAt(e == null ? null : e.toLocalDateTime());
            vo.setStatus(rs.getString("status"));
            vo.setReason(rs.getString("reason"));
            return vo;
        }, args.toArray());
    }

    public boolean existsActiveForStudent(Long studentId) {
        Long cnt = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM dorm_assignment WHERE student_id=? AND status='ACTIVE' AND deleted=0",
                new Object[]{studentId},
                Long.class
        );
        return cnt != null && cnt > 0;
    }

    public boolean existsActiveForBed(Long bedId) {
        Long cnt = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM dorm_assignment WHERE bed_id=? AND status='ACTIVE' AND deleted=0",
                new Object[]{bedId},
                Long.class
        );
        return cnt != null && cnt > 0;
    }

    public Long insert(DormAssignment a) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(con -> {
            PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO dorm_assignment(student_id, bed_id, start_at, end_at, status, reason, deleted) VALUES (?,?,?,?,?,?,0)",
                    Statement.RETURN_GENERATED_KEYS
            );
            ps.setLong(1, a.getStudentId());
            ps.setLong(2, a.getBedId());
            ps.setTimestamp(3, Timestamp.valueOf(a.getStartAt()));
            if (a.getEndAt() == null) {
                ps.setTimestamp(4, null);
            } else {
                ps.setTimestamp(4, Timestamp.valueOf(a.getEndAt()));
            }
            ps.setString(5, a.getStatus());
            ps.setString(6, a.getReason());
            return ps;
        }, keyHolder);
        Number key = keyHolder.getKey();
        return key == null ? null : key.longValue();
    }

    public int endAssignment(Long id, LocalDateTime endAt, String reason) {
        return jdbcTemplate.update(
                "UPDATE dorm_assignment SET status='ENDED', end_at=?, reason=? WHERE id=? AND status='ACTIVE' AND deleted=0",
                ps -> {
                    ps.setTimestamp(1, Timestamp.valueOf(endAt));
                    ps.setString(2, reason);
                    ps.setLong(3, id);
                }
        );
    }
}

package com.example.dormitory.repair.repository;

import com.example.dormitory.repair.model.RepairOrder;
import com.example.dormitory.repair.vo.RepairOrderDetailVo;
import com.example.dormitory.repair.vo.RepairOrderVo;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

@Repository
public class RepairOrderRepository {

    private final JdbcTemplate jdbcTemplate;

    public RepairOrderRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public RepairOrder findById(Long id) {
        List<RepairOrder> list = jdbcTemplate.query(
                "SELECT id, student_id, building_id, room_id, title, description, priority, status, assignee_user_id, deleted, created_at " +
                        "FROM repair_order WHERE id=? AND deleted=0 LIMIT 1",
                (rs, rowNum) -> {
                    RepairOrder o = new RepairOrder();
                    o.setId(rs.getLong("id"));
                    long sid = rs.getLong("student_id");
                    o.setStudentId(rs.wasNull() ? null : sid);
                    long bid = rs.getLong("building_id");
                    o.setBuildingId(rs.wasNull() ? null : bid);
                    long rid = rs.getLong("room_id");
                    o.setRoomId(rs.wasNull() ? null : rid);
                    o.setTitle(rs.getString("title"));
                    o.setDescription(rs.getString("description"));
                    o.setPriority(rs.getString("priority"));
                    o.setStatus(rs.getString("status"));
                    long au = rs.getLong("assignee_user_id");
                    o.setAssigneeUserId(rs.wasNull() ? null : au);
                    o.setDeleted(rs.getInt("deleted"));
                    Timestamp c = rs.getTimestamp("created_at");
                    o.setCreatedAt(c == null ? null : c.toLocalDateTime());
                    return o;
                },
                id
        );
        return list.isEmpty() ? null : list.get(0);
    }

    public RepairOrderDetailVo findDetailById(Long id) {
        List<RepairOrderDetailVo> list = jdbcTemplate.query(
                "SELECT r.id, r.student_id, s.student_no, s.name AS student_name, " +
                        "r.building_id, b.code AS building_code, b.name AS building_name, " +
                        "r.room_id, room.room_no, " +
                        "r.title, r.description, r.priority, r.status, " +
                        "r.assignee_user_id, u.username AS assignee_username, u.real_name AS assignee_real_name, " +
                        "r.created_at " +
                        "FROM repair_order r " +
                        "LEFT JOIN student s ON s.id=r.student_id " +
                        "LEFT JOIN dorm_building b ON b.id=r.building_id " +
                        "LEFT JOIN dorm_room room ON room.id=r.room_id " +
                        "LEFT JOIN sys_user u ON u.id=r.assignee_user_id " +
                        "WHERE r.id=? AND r.deleted=0 LIMIT 1",
                (rs, rowNum) -> {
                    RepairOrderDetailVo vo = new RepairOrderDetailVo();
                    vo.setId(rs.getLong("id"));

                    long sid = rs.getLong("student_id");
                    vo.setStudentId(rs.wasNull() ? null : sid);
                    vo.setStudentNo(rs.getString("student_no"));
                    vo.setStudentName(rs.getString("student_name"));

                    long bid = rs.getLong("building_id");
                    vo.setBuildingId(rs.wasNull() ? null : bid);
                    vo.setBuildingCode(rs.getString("building_code"));
                    vo.setBuildingName(rs.getString("building_name"));

                    long rid = rs.getLong("room_id");
                    vo.setRoomId(rs.wasNull() ? null : rid);
                    vo.setRoomNo(rs.getString("room_no"));

                    vo.setTitle(rs.getString("title"));
                    vo.setDescription(rs.getString("description"));
                    vo.setPriority(rs.getString("priority"));
                    vo.setStatus(rs.getString("status"));

                    long au = rs.getLong("assignee_user_id");
                    vo.setAssigneeUserId(rs.wasNull() ? null : au);
                    vo.setAssigneeUsername(rs.getString("assignee_username"));
                    vo.setAssigneeRealName(rs.getString("assignee_real_name"));

                    Timestamp c = rs.getTimestamp("created_at");
                    vo.setCreatedAt(c == null ? null : c.toLocalDateTime());
                    return vo;
                },
                id
        );
        return list.isEmpty() ? null : list.get(0);
    }

    public long count(String status, String priority, Long assigneeUserId, String keyword) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM repair_order r WHERE r.deleted=0");
        List<Object> args = new ArrayList<>();

        if (status != null && !status.isBlank()) {
            sql.append(" AND r.status=?");
            args.add(status.trim());
        }
        if (priority != null && !priority.isBlank()) {
            sql.append(" AND r.priority=?");
            args.add(priority.trim());
        }
        if (assigneeUserId != null) {
            sql.append(" AND r.assignee_user_id=?");
            args.add(assigneeUserId);
        }
        if (keyword != null && !keyword.isBlank()) {
            sql.append(" AND (r.title LIKE ? OR r.description LIKE ?)");
            String like = "%" + keyword.trim() + "%";
            args.add(like);
            args.add(like);
        }

        Long cnt = jdbcTemplate.queryForObject(sql.toString(), args.toArray(), Long.class);
        return cnt == null ? 0 : cnt;
    }

    public List<RepairOrderVo> list(String status, String priority, Long assigneeUserId, String keyword, String orderBy, int limit, int offset) {
        StringBuilder sql = new StringBuilder(
                "SELECT r.id, r.title, r.priority, r.status, r.assignee_user_id, u.username AS assignee_username, u.real_name AS assignee_real_name, r.created_at " +
                        "FROM repair_order r " +
                        "LEFT JOIN sys_user u ON u.id=r.assignee_user_id " +
                        "WHERE r.deleted=0"
        );
        List<Object> args = new ArrayList<>();

        if (status != null && !status.isBlank()) {
            sql.append(" AND r.status=?");
            args.add(status.trim());
        }
        if (priority != null && !priority.isBlank()) {
            sql.append(" AND r.priority=?");
            args.add(priority.trim());
        }
        if (assigneeUserId != null) {
            sql.append(" AND r.assignee_user_id=?");
            args.add(assigneeUserId);
        }
        if (keyword != null && !keyword.isBlank()) {
            sql.append(" AND (r.title LIKE ? OR r.description LIKE ?)");
            String like = "%" + keyword.trim() + "%";
            args.add(like);
            args.add(like);
        }

        sql.append(" ORDER BY ").append(orderBy).append(" LIMIT ? OFFSET ?");
        args.add(limit);
        args.add(offset);

        return jdbcTemplate.query(sql.toString(), (rs, rowNum) -> {
            RepairOrderVo vo = new RepairOrderVo();
            vo.setId(rs.getLong("id"));
            vo.setTitle(rs.getString("title"));
            vo.setPriority(rs.getString("priority"));
            vo.setStatus(rs.getString("status"));
            long au = rs.getLong("assignee_user_id");
            vo.setAssigneeUserId(rs.wasNull() ? null : au);
            vo.setAssigneeUsername(rs.getString("assignee_username"));
            vo.setAssigneeRealName(rs.getString("assignee_real_name"));
            Timestamp c = rs.getTimestamp("created_at");
            vo.setCreatedAt(c == null ? null : c.toLocalDateTime());
            return vo;
        }, args.toArray());
    }

    public Long insert(RepairOrder order) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(con -> {
            PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO repair_order(student_id, building_id, room_id, title, description, priority, status, assignee_user_id, deleted) " +
                            "VALUES (?,?,?,?,?,?,?,?,0)",
                    Statement.RETURN_GENERATED_KEYS
            );
            if (order.getStudentId() == null) {
                ps.setObject(1, null);
            } else {
                ps.setLong(1, order.getStudentId());
            }
            if (order.getBuildingId() == null) {
                ps.setObject(2, null);
            } else {
                ps.setLong(2, order.getBuildingId());
            }
            if (order.getRoomId() == null) {
                ps.setObject(3, null);
            } else {
                ps.setLong(3, order.getRoomId());
            }
            ps.setString(4, order.getTitle());
            ps.setString(5, order.getDescription());
            ps.setString(6, order.getPriority());
            ps.setString(7, order.getStatus());
            if (order.getAssigneeUserId() == null) {
                ps.setObject(8, null);
            } else {
                ps.setLong(8, order.getAssigneeUserId());
            }
            return ps;
        }, keyHolder);
        Number key = keyHolder.getKey();
        return key == null ? null : key.longValue();
    }

    public int updateAssigneeIfNotFinal(Long id, Long assigneeUserId) {
        return jdbcTemplate.update(
                "UPDATE repair_order SET assignee_user_id=? WHERE id=? AND deleted=0 AND status NOT IN ('DONE','REJECTED')",
                assigneeUserId,
                id
        );
    }

    public int updateStatusIfCurrent(Long id, String currentStatus, String newStatus) {
        return jdbcTemplate.update(
                "UPDATE repair_order SET status=? WHERE id=? AND status=? AND deleted=0",
                newStatus,
                id,
                currentStatus
        );
    }
}

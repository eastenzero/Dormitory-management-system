package com.example.dormitory.dorm.repository;

import com.example.dormitory.dorm.model.DormRoom;
import com.example.dormitory.dorm.vo.DormRoomVo;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

@Repository
public class DormRoomRepository {

    private final JdbcTemplate jdbcTemplate;

    public DormRoomRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public DormRoomVo findVoById(Long id) {
        List<DormRoomVo> list = jdbcTemplate.query(
                "SELECT r.id, r.building_id, b.code AS building_code, b.name AS building_name, r.floor_no, r.room_no, r.room_type, r.gender_limit, r.status " +
                        "FROM dorm_room r JOIN dorm_building b ON b.id=r.building_id " +
                        "WHERE r.id=? AND r.deleted=0 LIMIT 1",
                (rs, rowNum) -> {
                    DormRoomVo vo = new DormRoomVo();
                    vo.setId(rs.getLong("id"));
                    vo.setBuildingId(rs.getLong("building_id"));
                    vo.setBuildingCode(rs.getString("building_code"));
                    vo.setBuildingName(rs.getString("building_name"));
                    vo.setFloorNo(rs.getInt("floor_no"));
                    vo.setRoomNo(rs.getString("room_no"));
                    vo.setRoomType(rs.getString("room_type"));
                    vo.setGenderLimit(rs.getString("gender_limit"));
                    vo.setStatus(rs.getString("status"));
                    return vo;
                },
                id
        );
        return list.isEmpty() ? null : list.get(0);
    }

    public DormRoom findById(Long id) {
        List<DormRoom> list = jdbcTemplate.query(
                "SELECT id, building_id, floor_no, room_no, room_type, gender_limit, status, deleted FROM dorm_room WHERE id=? AND deleted=0 LIMIT 1",
                (rs, rowNum) -> {
                    DormRoom r = new DormRoom();
                    r.setId(rs.getLong("id"));
                    r.setBuildingId(rs.getLong("building_id"));
                    r.setFloorNo(rs.getInt("floor_no"));
                    r.setRoomNo(rs.getString("room_no"));
                    r.setRoomType(rs.getString("room_type"));
                    r.setGenderLimit(rs.getString("gender_limit"));
                    r.setStatus(rs.getString("status"));
                    r.setDeleted(rs.getInt("deleted"));
                    return r;
                },
                id
        );
        return list.isEmpty() ? null : list.get(0);
    }

    public long count(Long buildingId, Integer floorNo, String roomNo, String status, String keyword) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM dorm_room r JOIN dorm_building b ON b.id=r.building_id WHERE r.deleted=0");
        List<Object> args = new ArrayList<>();
        if (buildingId != null) {
            sql.append(" AND r.building_id=?");
            args.add(buildingId);
        }
        if (floorNo != null) {
            sql.append(" AND r.floor_no=?");
            args.add(floorNo);
        }
        if (roomNo != null && !roomNo.isBlank()) {
            sql.append(" AND r.room_no=?");
            args.add(roomNo.trim());
        }
        if (status != null && !status.isBlank()) {
            sql.append(" AND r.status=?");
            args.add(status.trim());
        }
        if (keyword != null && !keyword.isBlank()) {
            sql.append(" AND (r.room_no LIKE ? OR r.room_type LIKE ? OR b.code LIKE ? OR b.name LIKE ?)");
            String like = "%" + keyword.trim() + "%";
            args.add(like);
            args.add(like);
            args.add(like);
            args.add(like);
        }
        Long cnt = jdbcTemplate.queryForObject(sql.toString(), args.toArray(), Long.class);
        return cnt == null ? 0 : cnt;
    }

    public List<DormRoomVo> list(Long buildingId, Integer floorNo, String roomNo, String status, String keyword, String orderBy, int limit, int offset) {
        StringBuilder sql = new StringBuilder(
                "SELECT r.id, r.building_id, b.code AS building_code, b.name AS building_name, r.floor_no, r.room_no, r.room_type, r.gender_limit, r.status " +
                        "FROM dorm_room r JOIN dorm_building b ON b.id=r.building_id WHERE r.deleted=0"
        );
        List<Object> args = new ArrayList<>();
        if (buildingId != null) {
            sql.append(" AND r.building_id=?");
            args.add(buildingId);
        }
        if (floorNo != null) {
            sql.append(" AND r.floor_no=?");
            args.add(floorNo);
        }
        if (roomNo != null && !roomNo.isBlank()) {
            sql.append(" AND r.room_no=?");
            args.add(roomNo.trim());
        }
        if (status != null && !status.isBlank()) {
            sql.append(" AND r.status=?");
            args.add(status.trim());
        }
        if (keyword != null && !keyword.isBlank()) {
            sql.append(" AND (r.room_no LIKE ? OR r.room_type LIKE ? OR b.code LIKE ? OR b.name LIKE ?)");
            String like = "%" + keyword.trim() + "%";
            args.add(like);
            args.add(like);
            args.add(like);
            args.add(like);
        }
        sql.append(" ORDER BY ").append(orderBy).append(" LIMIT ? OFFSET ?");
        args.add(limit);
        args.add(offset);

        return jdbcTemplate.query(sql.toString(), (rs, rowNum) -> {
            DormRoomVo vo = new DormRoomVo();
            vo.setId(rs.getLong("id"));
            vo.setBuildingId(rs.getLong("building_id"));
            vo.setBuildingCode(rs.getString("building_code"));
            vo.setBuildingName(rs.getString("building_name"));
            vo.setFloorNo(rs.getInt("floor_no"));
            vo.setRoomNo(rs.getString("room_no"));
            vo.setRoomType(rs.getString("room_type"));
            vo.setGenderLimit(rs.getString("gender_limit"));
            vo.setStatus(rs.getString("status"));
            return vo;
        }, args.toArray());
    }

    public Long insert(DormRoom room) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(con -> {
            PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO dorm_room(building_id, floor_no, room_no, room_type, gender_limit, status, deleted) VALUES (?,?,?,?,?,?,0)",
                    Statement.RETURN_GENERATED_KEYS
            );
            ps.setLong(1, room.getBuildingId());
            ps.setInt(2, room.getFloorNo());
            ps.setString(3, room.getRoomNo());
            ps.setString(4, room.getRoomType());
            ps.setString(5, room.getGenderLimit());
            ps.setString(6, room.getStatus());
            return ps;
        }, keyHolder);
        Number key = keyHolder.getKey();
        return key == null ? null : key.longValue();
    }

    public int update(Long id, DormRoom room) {
        return jdbcTemplate.update(
                "UPDATE dorm_room SET building_id=?, floor_no=?, room_no=?, room_type=?, gender_limit=?, status=? WHERE id=? AND deleted=0",
                room.getBuildingId(),
                room.getFloorNo(),
                room.getRoomNo(),
                room.getRoomType(),
                room.getGenderLimit(),
                room.getStatus(),
                id
        );
    }

    public int softDelete(Long id) {
        return jdbcTemplate.update("UPDATE dorm_room SET deleted=1 WHERE id=? AND deleted=0", id);
    }
}

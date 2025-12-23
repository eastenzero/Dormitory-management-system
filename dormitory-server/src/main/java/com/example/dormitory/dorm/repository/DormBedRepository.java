package com.example.dormitory.dorm.repository;

import com.example.dormitory.dorm.model.DormBed;
import com.example.dormitory.dorm.vo.DormBedVo;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

@Repository
public class DormBedRepository {

    private final JdbcTemplate jdbcTemplate;

    public DormBedRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public DormBed findById(Long id) {
        List<DormBed> list = jdbcTemplate.query(
                "SELECT id, room_id, bed_no, status, deleted FROM dorm_bed WHERE id=? AND deleted=0 LIMIT 1",
                (rs, rowNum) -> {
                    DormBed b = new DormBed();
                    b.setId(rs.getLong("id"));
                    b.setRoomId(rs.getLong("room_id"));
                    b.setBedNo(rs.getString("bed_no"));
                    b.setStatus(rs.getString("status"));
                    b.setDeleted(rs.getInt("deleted"));
                    return b;
                },
                id
        );
        return list.isEmpty() ? null : list.get(0);
    }

    public DormBedVo findVoById(Long id) {
        List<DormBedVo> list = jdbcTemplate.query(
                "SELECT bed.id, bed.room_id, r.room_no, r.building_id, b.code AS building_code, b.name AS building_name, bed.bed_no, bed.status " +
                        "FROM dorm_bed bed " +
                        "JOIN dorm_room r ON r.id=bed.room_id " +
                        "JOIN dorm_building b ON b.id=r.building_id " +
                        "WHERE bed.id=? AND bed.deleted=0 LIMIT 1",
                (rs, rowNum) -> {
                    DormBedVo vo = new DormBedVo();
                    vo.setId(rs.getLong("id"));
                    vo.setRoomId(rs.getLong("room_id"));
                    vo.setRoomNo(rs.getString("room_no"));
                    vo.setBuildingId(rs.getLong("building_id"));
                    vo.setBuildingCode(rs.getString("building_code"));
                    vo.setBuildingName(rs.getString("building_name"));
                    vo.setBedNo(rs.getString("bed_no"));
                    vo.setStatus(rs.getString("status"));
                    return vo;
                },
                id
        );
        return list.isEmpty() ? null : list.get(0);
    }

    public long count(Long buildingId, Long roomId, String status, String keyword) {
        StringBuilder sql = new StringBuilder(
                "SELECT COUNT(*) FROM dorm_bed bed " +
                        "JOIN dorm_room r ON r.id=bed.room_id " +
                        "JOIN dorm_building b ON b.id=r.building_id " +
                        "WHERE bed.deleted=0"
        );
        List<Object> args = new ArrayList<>();
        if (buildingId != null) {
            sql.append(" AND r.building_id=?");
            args.add(buildingId);
        }
        if (roomId != null) {
            sql.append(" AND bed.room_id=?");
            args.add(roomId);
        }
        if (status != null && !status.isBlank()) {
            sql.append(" AND bed.status=?");
            args.add(status.trim());
        }
        if (keyword != null && !keyword.isBlank()) {
            sql.append(" AND (r.room_no LIKE ? OR b.code LIKE ? OR b.name LIKE ? OR bed.bed_no LIKE ?)");
            String like = "%" + keyword.trim() + "%";
            args.add(like);
            args.add(like);
            args.add(like);
            args.add(like);
        }
        Long cnt = jdbcTemplate.queryForObject(sql.toString(), args.toArray(), Long.class);
        return cnt == null ? 0 : cnt;
    }

    public List<DormBedVo> list(Long buildingId, Long roomId, String status, String keyword, String orderBy, int limit, int offset) {
        StringBuilder sql = new StringBuilder(
                "SELECT bed.id, bed.room_id, r.room_no, r.building_id, b.code AS building_code, b.name AS building_name, bed.bed_no, bed.status " +
                        "FROM dorm_bed bed " +
                        "JOIN dorm_room r ON r.id=bed.room_id " +
                        "JOIN dorm_building b ON b.id=r.building_id " +
                        "WHERE bed.deleted=0"
        );
        List<Object> args = new ArrayList<>();
        if (buildingId != null) {
            sql.append(" AND r.building_id=?");
            args.add(buildingId);
        }
        if (roomId != null) {
            sql.append(" AND bed.room_id=?");
            args.add(roomId);
        }
        if (status != null && !status.isBlank()) {
            sql.append(" AND bed.status=?");
            args.add(status.trim());
        }
        if (keyword != null && !keyword.isBlank()) {
            sql.append(" AND (r.room_no LIKE ? OR b.code LIKE ? OR b.name LIKE ? OR bed.bed_no LIKE ?)");
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
            DormBedVo vo = new DormBedVo();
            vo.setId(rs.getLong("id"));
            vo.setRoomId(rs.getLong("room_id"));
            vo.setRoomNo(rs.getString("room_no"));
            vo.setBuildingId(rs.getLong("building_id"));
            vo.setBuildingCode(rs.getString("building_code"));
            vo.setBuildingName(rs.getString("building_name"));
            vo.setBedNo(rs.getString("bed_no"));
            vo.setStatus(rs.getString("status"));
            return vo;
        }, args.toArray());
    }

    public Long insert(DormBed bed) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(con -> {
            PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO dorm_bed(room_id, bed_no, status, deleted) VALUES (?,?,?,0)",
                    Statement.RETURN_GENERATED_KEYS
            );
            ps.setLong(1, bed.getRoomId());
            ps.setString(2, bed.getBedNo());
            ps.setString(3, bed.getStatus());
            return ps;
        }, keyHolder);
        Number key = keyHolder.getKey();
        return key == null ? null : key.longValue();
    }

    public int update(Long id, DormBed bed) {
        return jdbcTemplate.update(
                "UPDATE dorm_bed SET room_id=?, bed_no=?, status=? WHERE id=? AND deleted=0",
                bed.getRoomId(),
                bed.getBedNo(),
                bed.getStatus(),
                id
        );
    }

    public int updateStatusIfCurrent(Long id, String currentStatus, String nextStatus) {
        return jdbcTemplate.update(
                "UPDATE dorm_bed SET status=? WHERE id=? AND status=? AND deleted=0",
                nextStatus,
                id,
                currentStatus
        );
    }

    public int updateStatus(Long id, String nextStatus) {
        return jdbcTemplate.update(
                "UPDATE dorm_bed SET status=? WHERE id=? AND deleted=0",
                nextStatus,
                id
        );
    }

    public int softDelete(Long id) {
        return jdbcTemplate.update("UPDATE dorm_bed SET deleted=1 WHERE id=? AND deleted=0", id);
    }
}

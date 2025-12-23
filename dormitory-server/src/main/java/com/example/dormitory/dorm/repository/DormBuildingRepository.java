package com.example.dormitory.dorm.repository;

import com.example.dormitory.dorm.model.DormBuilding;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

@Repository
public class DormBuildingRepository {

    private final JdbcTemplate jdbcTemplate;

    public DormBuildingRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public DormBuilding findById(Long id) {
        List<DormBuilding> list = jdbcTemplate.query(
                "SELECT id, code, name, gender_limit, address, status, deleted FROM dorm_building WHERE id=? AND deleted=0 LIMIT 1",
                rowMapper(),
                id
        );
        return list.isEmpty() ? null : list.get(0);
    }

    public long count(String keyword, String genderLimit) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM dorm_building WHERE deleted=0");
        List<Object> args = new ArrayList<>();
        if (keyword != null && !keyword.isBlank()) {
            sql.append(" AND (code LIKE ? OR name LIKE ?)");
            String like = "%" + keyword.trim() + "%";
            args.add(like);
            args.add(like);
        }
        if (genderLimit != null && !genderLimit.isBlank()) {
            sql.append(" AND gender_limit=?");
            args.add(genderLimit.trim());
        }
        Long cnt = jdbcTemplate.queryForObject(sql.toString(), args.toArray(), Long.class);
        return cnt == null ? 0 : cnt;
    }

    public List<DormBuilding> list(String keyword, String genderLimit, String orderBy, int limit, int offset) {
        StringBuilder sql = new StringBuilder("SELECT id, code, name, gender_limit, address, status, deleted FROM dorm_building WHERE deleted=0");
        List<Object> args = new ArrayList<>();
        if (keyword != null && !keyword.isBlank()) {
            sql.append(" AND (code LIKE ? OR name LIKE ?)");
            String like = "%" + keyword.trim() + "%";
            args.add(like);
            args.add(like);
        }
        if (genderLimit != null && !genderLimit.isBlank()) {
            sql.append(" AND gender_limit=?");
            args.add(genderLimit.trim());
        }
        sql.append(" ORDER BY ").append(orderBy).append(" LIMIT ? OFFSET ?");
        args.add(limit);
        args.add(offset);
        return jdbcTemplate.query(sql.toString(), rowMapper(), args.toArray());
    }

    public Long insert(DormBuilding building) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(con -> {
            PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO dorm_building(code, name, gender_limit, address, status, deleted) VALUES (?,?,?,?,?,0)",
                    Statement.RETURN_GENERATED_KEYS
            );
            ps.setString(1, building.getCode());
            ps.setString(2, building.getName());
            ps.setString(3, building.getGenderLimit());
            ps.setString(4, building.getAddress());
            ps.setString(5, building.getStatus());
            return ps;
        }, keyHolder);
        Number key = keyHolder.getKey();
        return key == null ? null : key.longValue();
    }

    public int update(Long id, DormBuilding building) {
        return jdbcTemplate.update(
                "UPDATE dorm_building SET code=?, name=?, gender_limit=?, address=?, status=? WHERE id=? AND deleted=0",
                building.getCode(),
                building.getName(),
                building.getGenderLimit(),
                building.getAddress(),
                building.getStatus(),
                id
        );
    }

    public int softDelete(Long id) {
        return jdbcTemplate.update("UPDATE dorm_building SET deleted=1 WHERE id=? AND deleted=0", id);
    }

    private RowMapper<DormBuilding> rowMapper() {
        return (rs, rowNum) -> {
            DormBuilding b = new DormBuilding();
            b.setId(rs.getLong("id"));
            b.setCode(rs.getString("code"));
            b.setName(rs.getString("name"));
            b.setGenderLimit(rs.getString("gender_limit"));
            b.setAddress(rs.getString("address"));
            b.setStatus(rs.getString("status"));
            b.setDeleted(rs.getInt("deleted"));
            return b;
        };
    }
}

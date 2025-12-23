package com.example.dormitory.student.repository;

import com.example.dormitory.student.model.Student;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

@Repository
public class StudentRepository {

    private final JdbcTemplate jdbcTemplate;

    public StudentRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Student findById(Long id) {
        List<Student> list = jdbcTemplate.query(
                "SELECT id, student_no, name, gender, college, major, class_name, phone, status, deleted FROM student WHERE id=? AND deleted=0 LIMIT 1",
                (rs, rowNum) -> {
                    Student s = new Student();
                    s.setId(rs.getLong("id"));
                    s.setStudentNo(rs.getString("student_no"));
                    s.setName(rs.getString("name"));
                    s.setGender(rs.getString("gender"));
                    s.setCollege(rs.getString("college"));
                    s.setMajor(rs.getString("major"));
                    s.setClassName(rs.getString("class_name"));
                    s.setPhone(rs.getString("phone"));
                    s.setStatus(rs.getString("status"));
                    s.setDeleted(rs.getInt("deleted"));
                    return s;
                },
                id
        );
        return list.isEmpty() ? null : list.get(0);
    }

    public long count(String gender, String status, String keyword) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM student WHERE deleted=0");
        List<Object> args = new ArrayList<>();
        if (gender != null && !gender.isBlank()) {
            sql.append(" AND gender=?");
            args.add(gender.trim());
        }
        if (status != null && !status.isBlank()) {
            sql.append(" AND status=?");
            args.add(status.trim());
        }
        if (keyword != null && !keyword.isBlank()) {
            sql.append(" AND (student_no LIKE ? OR name LIKE ? OR phone LIKE ?)");
            String like = "%" + keyword.trim() + "%";
            args.add(like);
            args.add(like);
            args.add(like);
        }
        Long cnt = jdbcTemplate.queryForObject(sql.toString(), args.toArray(), Long.class);
        return cnt == null ? 0 : cnt;
    }

    public List<Student> list(String gender, String status, String keyword, String orderBy, int limit, int offset) {
        StringBuilder sql = new StringBuilder("SELECT id, student_no, name, gender, college, major, class_name, phone, status, deleted FROM student WHERE deleted=0");
        List<Object> args = new ArrayList<>();
        if (gender != null && !gender.isBlank()) {
            sql.append(" AND gender=?");
            args.add(gender.trim());
        }
        if (status != null && !status.isBlank()) {
            sql.append(" AND status=?");
            args.add(status.trim());
        }
        if (keyword != null && !keyword.isBlank()) {
            sql.append(" AND (student_no LIKE ? OR name LIKE ? OR phone LIKE ?)");
            String like = "%" + keyword.trim() + "%";
            args.add(like);
            args.add(like);
            args.add(like);
        }
        sql.append(" ORDER BY ").append(orderBy).append(" LIMIT ? OFFSET ?");
        args.add(limit);
        args.add(offset);

        return jdbcTemplate.query(sql.toString(), (rs, rowNum) -> {
            Student s = new Student();
            s.setId(rs.getLong("id"));
            s.setStudentNo(rs.getString("student_no"));
            s.setName(rs.getString("name"));
            s.setGender(rs.getString("gender"));
            s.setCollege(rs.getString("college"));
            s.setMajor(rs.getString("major"));
            s.setClassName(rs.getString("class_name"));
            s.setPhone(rs.getString("phone"));
            s.setStatus(rs.getString("status"));
            s.setDeleted(rs.getInt("deleted"));
            return s;
        }, args.toArray());
    }

    public Long insert(Student s) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(con -> {
            PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO student(student_no, name, gender, college, major, class_name, phone, status, deleted) VALUES (?,?,?,?,?,?,?,?,0)",
                    Statement.RETURN_GENERATED_KEYS
            );
            ps.setString(1, s.getStudentNo());
            ps.setString(2, s.getName());
            ps.setString(3, s.getGender());
            ps.setString(4, s.getCollege());
            ps.setString(5, s.getMajor());
            ps.setString(6, s.getClassName());
            ps.setString(7, s.getPhone());
            ps.setString(8, s.getStatus());
            return ps;
        }, keyHolder);
        Number key = keyHolder.getKey();
        return key == null ? null : key.longValue();
    }

    public int update(Long id, Student s) {
        return jdbcTemplate.update(
                "UPDATE student SET student_no=?, name=?, gender=?, college=?, major=?, class_name=?, phone=?, status=? WHERE id=? AND deleted=0",
                s.getStudentNo(),
                s.getName(),
                s.getGender(),
                s.getCollege(),
                s.getMajor(),
                s.getClassName(),
                s.getPhone(),
                s.getStatus(),
                id
        );
    }

    public int softDelete(Long id) {
        return jdbcTemplate.update("UPDATE student SET deleted=1 WHERE id=? AND deleted=0", id);
    }
}

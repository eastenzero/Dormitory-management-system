package com.example.dormitory.student.service;

import com.example.dormitory.common.BizException;
import com.example.dormitory.common.PageResult;
import com.example.dormitory.student.dto.CreateStudentRequest;
import com.example.dormitory.student.dto.UpdateStudentRequest;
import com.example.dormitory.student.model.Student;
import com.example.dormitory.student.repository.StudentRepository;
import com.example.dormitory.student.vo.StudentVo;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StudentService {

    private final StudentRepository repository;

    public StudentService(StudentRepository repository) {
        this.repository = repository;
    }

    public PageResult<StudentVo> list(String gender, String status, String keyword,
                                     Integer page, Integer pageSize, String sortBy, String sortOrder) {
        int p = page == null || page < 1 ? 1 : page;
        int ps = pageSize == null || pageSize < 1 ? 20 : Math.min(pageSize, 200);
        int offset = (p - 1) * ps;

        String orderBy = resolveOrderBy(sortBy, sortOrder);
        long total = repository.count(gender, status, keyword);
        List<Student> list = repository.list(gender, status, keyword, orderBy, ps, offset);
        List<StudentVo> voList = list.stream().map(this::toVo).toList();
        return new PageResult<>(voList, p, ps, total);
    }

    public StudentVo get(Long id) {
        Student s = repository.findById(id);
        if (s == null) {
            throw new BizException(40004, "student not found");
        }
        return toVo(s);
    }

    public StudentVo create(CreateStudentRequest req) {
        Student s = new Student();
        s.setStudentNo(req.getStudentNo().trim());
        s.setName(req.getName().trim());
        s.setGender(req.getGender().trim());
        s.setCollege(req.getCollege());
        s.setMajor(req.getMajor());
        s.setClassName(req.getClassName());
        s.setPhone(req.getPhone());
        s.setStatus(req.getStatus() == null || req.getStatus().isBlank() ? "IN_SCHOOL" : req.getStatus().trim());

        Long id = repository.insert(s);
        if (id == null) {
            throw new BizException(50000, "create student failed");
        }
        return get(id);
    }

    public StudentVo update(Long id, UpdateStudentRequest req) {
        Student s = new Student();
        s.setStudentNo(req.getStudentNo().trim());
        s.setName(req.getName().trim());
        s.setGender(req.getGender().trim());
        s.setCollege(req.getCollege());
        s.setMajor(req.getMajor());
        s.setClassName(req.getClassName());
        s.setPhone(req.getPhone());
        s.setStatus(req.getStatus() == null || req.getStatus().isBlank() ? "IN_SCHOOL" : req.getStatus().trim());

        int affected = repository.update(id, s);
        if (affected == 0) {
            throw new BizException(40004, "student not found");
        }
        return get(id);
    }

    public void delete(Long id) {
        int affected = repository.softDelete(id);
        if (affected == 0) {
            throw new BizException(40004, "student not found");
        }
    }

    private StudentVo toVo(Student s) {
        StudentVo vo = new StudentVo();
        vo.setId(s.getId());
        vo.setStudentNo(s.getStudentNo());
        vo.setName(s.getName());
        vo.setGender(s.getGender());
        vo.setCollege(s.getCollege());
        vo.setMajor(s.getMajor());
        vo.setClassName(s.getClassName());
        vo.setPhone(s.getPhone());
        vo.setStatus(s.getStatus());
        return vo;
    }

    private String resolveOrderBy(String sortBy, String sortOrder) {
        String column;
        if ("studentNo".equalsIgnoreCase(sortBy) || "student_no".equalsIgnoreCase(sortBy)) {
            column = "student_no";
        } else if ("name".equalsIgnoreCase(sortBy)) {
            column = "name";
        } else if ("gender".equalsIgnoreCase(sortBy)) {
            column = "gender";
        } else if ("status".equalsIgnoreCase(sortBy)) {
            column = "status";
        } else {
            column = "id";
        }

        String order = "desc";
        if ("asc".equalsIgnoreCase(sortOrder)) {
            order = "asc";
        } else if ("desc".equalsIgnoreCase(sortOrder)) {
            order = "desc";
        }
        return column + " " + order;
    }
}

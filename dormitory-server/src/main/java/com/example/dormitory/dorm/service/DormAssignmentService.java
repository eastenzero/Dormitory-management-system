package com.example.dormitory.dorm.service;

import com.example.dormitory.common.BizException;
import com.example.dormitory.common.PageResult;
import com.example.dormitory.dorm.dto.CreateDormAssignmentRequest;
import com.example.dormitory.dorm.dto.EndDormAssignmentRequest;
import com.example.dormitory.dorm.model.DormAssignment;
import com.example.dormitory.dorm.model.DormBed;
import com.example.dormitory.dorm.repository.DormAssignmentRepository;
import com.example.dormitory.dorm.repository.DormBedRepository;
import com.example.dormitory.dorm.vo.DormAssignmentVo;
import com.example.dormitory.student.model.Student;
import com.example.dormitory.student.repository.StudentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class DormAssignmentService {

    private final DormAssignmentRepository assignmentRepository;
    private final DormBedRepository bedRepository;
    private final StudentRepository studentRepository;

    public DormAssignmentService(DormAssignmentRepository assignmentRepository, DormBedRepository bedRepository, StudentRepository studentRepository) {
        this.assignmentRepository = assignmentRepository;
        this.bedRepository = bedRepository;
        this.studentRepository = studentRepository;
    }

    public PageResult<DormAssignmentVo> list(Long studentId, Long buildingId, Long roomId, Long bedId, String status,
                                            Integer page, Integer pageSize, String sortBy, String sortOrder) {
        int p = page == null || page < 1 ? 1 : page;
        int ps = pageSize == null || pageSize < 1 ? 20 : Math.min(pageSize, 200);
        int offset = (p - 1) * ps;

        String orderBy = resolveOrderBy(sortBy, sortOrder);
        long total = assignmentRepository.count(studentId, buildingId, roomId, bedId, status);
        List<DormAssignmentVo> list = assignmentRepository.list(studentId, buildingId, roomId, bedId, status, orderBy, ps, offset);
        return new PageResult<>(list, p, ps, total);
    }

    public DormAssignmentVo get(Long id) {
        DormAssignmentVo vo = assignmentRepository.findVoById(id);
        if (vo == null) {
            throw new BizException(40004, "assignment not found");
        }
        return vo;
    }

    @Transactional
    public DormAssignmentVo create(CreateDormAssignmentRequest req) {
        Student student = studentRepository.findById(req.getStudentId());
        if (student == null) {
            throw new BizException(40004, "student not found");
        }
        DormBed bed = bedRepository.findById(req.getBedId());
        if (bed == null) {
            throw new BizException(40004, "bed not found");
        }

        if (assignmentRepository.existsActiveForStudent(req.getStudentId())) {
            throw new BizException(40901, "student already has active assignment");
        }
        if (assignmentRepository.existsActiveForBed(req.getBedId())) {
            throw new BizException(40902, "bed already occupied");
        }

        int affected = bedRepository.updateStatusIfCurrent(req.getBedId(), "AVAILABLE", "OCCUPIED");
        if (affected == 0) {
            throw new BizException(40902, "bed already occupied");
        }

        DormAssignment a = new DormAssignment();
        a.setStudentId(req.getStudentId());
        a.setBedId(req.getBedId());
        a.setStartAt(LocalDateTime.now());
        a.setEndAt(null);
        a.setStatus("ACTIVE");
        a.setReason(req.getReason());

        Long id = assignmentRepository.insert(a);
        if (id == null) {
            throw new BizException(50000, "create assignment failed");
        }
        return get(id);
    }

    @Transactional
    public DormAssignmentVo end(Long id, EndDormAssignmentRequest req) {
        DormAssignment a = assignmentRepository.findById(id);
        if (a == null) {
            throw new BizException(40004, "assignment not found");
        }
        if (!"ACTIVE".equalsIgnoreCase(a.getStatus())) {
            throw new BizException(40903, "assignment not active");
        }

        LocalDateTime endAt = LocalDateTime.now();
        int affected = assignmentRepository.endAssignment(id, endAt, req == null ? null : req.getReason());
        if (affected == 0) {
            throw new BizException(40903, "assignment not active");
        }

        bedRepository.updateStatus(a.getBedId(), "AVAILABLE");
        return get(id);
    }

    private String resolveOrderBy(String sortBy, String sortOrder) {
        String column;
        if ("startAt".equalsIgnoreCase(sortBy) || "start_at".equalsIgnoreCase(sortBy)) {
            column = "a.start_at";
        } else if ("endAt".equalsIgnoreCase(sortBy) || "end_at".equalsIgnoreCase(sortBy)) {
            column = "a.end_at";
        } else if ("id".equalsIgnoreCase(sortBy)) {
            column = "a.id";
        } else {
            column = "a.id";
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

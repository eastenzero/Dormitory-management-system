package com.example.dormitory.visitor.service;

import com.example.dormitory.common.BizException;
import com.example.dormitory.common.PageResult;
import com.example.dormitory.security.SecurityUtils;
import com.example.dormitory.security.UserPrincipal;
import com.example.dormitory.student.repository.StudentRepository;
import com.example.dormitory.visitor.dto.CreateVisitorRecordRequest;
import com.example.dormitory.visitor.model.VisitorRecord;
import com.example.dormitory.visitor.repository.VisitorRecordRepository;
import com.example.dormitory.visitor.vo.VisitorRecordVo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;

@Service
public class VisitorRecordService {

    private final VisitorRecordRepository repository;
    private final StudentRepository studentRepository;

    public VisitorRecordService(VisitorRecordRepository repository, StudentRepository studentRepository) {
        this.repository = repository;
        this.studentRepository = studentRepository;
    }

    public PageResult<VisitorRecordVo> list(String status, String keyword, String fromAt, String toAt, Long studentId,
                                           Integer page, Integer pageSize, String sortBy, String sortOrder) {
        int p = page == null || page < 1 ? 1 : page;
        int ps = pageSize == null || pageSize < 1 ? 20 : Math.min(pageSize, 200);
        int offset = (p - 1) * ps;

        String st = normalizeStatus(status);
        LocalDateTime from = parseDateTime(fromAt, "fromAt");
        LocalDateTime to = parseDateTime(toAt, "toAt");

        String orderBy = resolveOrderBy(sortBy, sortOrder);
        long total = repository.count(st, keyword, from, to, studentId);
        List<VisitorRecordVo> list = repository.list(st, keyword, from, to, studentId, orderBy, ps, offset);
        return new PageResult<>(list, p, ps, total);
    }

    public VisitorRecordVo get(Long id) {
        VisitorRecordVo vo = repository.findVoById(id);
        if (vo == null) {
            throw new BizException(40004, "visitor record not found");
        }
        return vo;
    }

    @Transactional
    public VisitorRecordVo create(CreateVisitorRecordRequest req) {
        String visitorName = req.getVisitorName() == null ? null : req.getVisitorName().trim();
        if (visitorName == null || visitorName.isBlank()) {
            throw new BizException(40001, "visitorName required");
        }
        if (req.getStudentId() != null && studentRepository.findById(req.getStudentId()) == null) {
            throw new BizException(40004, "student not found");
        }

        UserPrincipal principal = SecurityUtils.currentUser();
        Long operatorId = principal == null ? null : principal.getId();

        VisitorRecord r = new VisitorRecord();
        r.setStudentId(req.getStudentId());
        r.setVisitorName(visitorName);
        r.setIdNo(req.getIdNo());
        r.setPhone(req.getPhone());
        r.setVisitReason(req.getVisitReason());
        r.setVisitAt(req.getVisitAt() == null ? LocalDateTime.now() : req.getVisitAt());
        r.setLeaveAt(null);
        r.setStatus("IN");
        r.setCreatedBy(operatorId);
        r.setUpdatedBy(operatorId);

        Long id = repository.insert(r);
        if (id == null) {
            throw new BizException(50000, "create visitor record failed");
        }
        return get(id);
    }

    @Transactional
    public VisitorRecordVo leave(Long id) {
        VisitorRecord r = repository.findById(id);
        if (r == null) {
            throw new BizException(40004, "visitor record not found");
        }
        if (r.getLeaveAt() != null || "OUT".equalsIgnoreCase(r.getStatus())) {
            throw new BizException(40901, "visitor already left");
        }

        UserPrincipal principal = SecurityUtils.currentUser();
        Long operatorId = principal == null ? null : principal.getId();

        LocalDateTime leaveAt = LocalDateTime.now();
        int affected = repository.leaveIfIn(id, leaveAt, operatorId);
        if (affected == 0) {
            VisitorRecord latest = repository.findById(id);
            if (latest == null) {
                throw new BizException(40004, "visitor record not found");
            }
            throw new BizException(40901, "visitor already left");
        }
        return get(id);
    }

    private String normalizeStatus(String status) {
        if (status == null || status.isBlank()) {
            return null;
        }
        String s = status.trim().toUpperCase();
        if (!"IN".equals(s) && !"OUT".equals(s)) {
            throw new BizException(40001, "invalid status");
        }
        return s;
    }

    private LocalDateTime parseDateTime(String s, String fieldName) {
        if (s == null || s.isBlank()) {
            return null;
        }
        try {
            return LocalDateTime.parse(s.trim());
        } catch (DateTimeParseException ex) {
            throw new BizException(40001, "invalid " + fieldName);
        }
    }

    private String resolveOrderBy(String sortBy, String sortOrder) {
        String column;
        if ("visitAt".equalsIgnoreCase(sortBy) || "visit_at".equalsIgnoreCase(sortBy)) {
            column = "v.visit_at";
        } else if ("leaveAt".equalsIgnoreCase(sortBy) || "leave_at".equalsIgnoreCase(sortBy)) {
            column = "v.leave_at";
        } else if ("createdAt".equalsIgnoreCase(sortBy) || "created_at".equalsIgnoreCase(sortBy)) {
            column = "v.created_at";
        } else if ("id".equalsIgnoreCase(sortBy)) {
            column = "v.id";
        } else {
            column = "v.id";
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

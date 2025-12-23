package com.example.dormitory.repair.service;

import com.example.dormitory.common.BizException;
import com.example.dormitory.common.PageResult;
import com.example.dormitory.dorm.repository.DormBuildingRepository;
import com.example.dormitory.dorm.repository.DormRoomRepository;
import com.example.dormitory.repair.dto.AssignRepairRequest;
import com.example.dormitory.repair.dto.CreateRepairRequest;
import com.example.dormitory.repair.dto.TransitionRepairRequest;
import com.example.dormitory.repair.model.RepairLog;
import com.example.dormitory.repair.model.RepairOrder;
import com.example.dormitory.repair.repository.RepairLogRepository;
import com.example.dormitory.repair.repository.RepairOrderRepository;
import com.example.dormitory.repair.vo.RepairLogVo;
import com.example.dormitory.repair.vo.RepairOrderDetailVo;
import com.example.dormitory.repair.vo.RepairOrderVo;
import com.example.dormitory.security.SecurityUtils;
import com.example.dormitory.security.UserPrincipal;
import com.example.dormitory.student.repository.StudentRepository;
import com.example.dormitory.sys.model.SysUser;
import com.example.dormitory.sys.service.SysUserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class RepairService {

    private final RepairOrderRepository orderRepository;
    private final RepairLogRepository logRepository;
    private final StudentRepository studentRepository;
    private final DormBuildingRepository buildingRepository;
    private final DormRoomRepository roomRepository;
    private final SysUserService sysUserService;

    public RepairService(
            RepairOrderRepository orderRepository,
            RepairLogRepository logRepository,
            StudentRepository studentRepository,
            DormBuildingRepository buildingRepository,
            DormRoomRepository roomRepository,
            SysUserService sysUserService
    ) {
        this.orderRepository = orderRepository;
        this.logRepository = logRepository;
        this.studentRepository = studentRepository;
        this.buildingRepository = buildingRepository;
        this.roomRepository = roomRepository;
        this.sysUserService = sysUserService;
    }

    public PageResult<RepairOrderVo> list(String status, String priority, Long assigneeUserId, String keyword,
                                         Integer page, Integer pageSize, String sortBy, String sortOrder) {
        int p = page == null || page < 1 ? 1 : page;
        int ps = pageSize == null || pageSize < 1 ? 20 : Math.min(pageSize, 200);
        int offset = (p - 1) * ps;

        String orderBy = resolveOrderBy(sortBy, sortOrder);
        long total = orderRepository.count(status, priority, assigneeUserId, keyword);
        List<RepairOrderVo> list = orderRepository.list(status, priority, assigneeUserId, keyword, orderBy, ps, offset);
        return new PageResult<>(list, p, ps, total);
    }

    public RepairOrderDetailVo get(Long id) {
        RepairOrderDetailVo vo = orderRepository.findDetailById(id);
        if (vo == null) {
            throw new BizException(40004, "repair not found");
        }
        return vo;
    }

    @Transactional
    public RepairOrderDetailVo create(CreateRepairRequest req) {
        String title = req.getTitle() == null ? null : req.getTitle().trim();
        if (title == null || title.isBlank()) {
            throw new BizException(40001, "title required");
        }

        String priority = normalizePriority(req.getPriority());

        if (req.getStudentId() != null && studentRepository.findById(req.getStudentId()) == null) {
            throw new BizException(40004, "student not found");
        }
        if (req.getBuildingId() != null && buildingRepository.findById(req.getBuildingId()) == null) {
            throw new BizException(40004, "building not found");
        }
        if (req.getRoomId() != null && roomRepository.findById(req.getRoomId()) == null) {
            throw new BizException(40004, "room not found");
        }

        RepairOrder o = new RepairOrder();
        o.setStudentId(req.getStudentId());
        o.setBuildingId(req.getBuildingId());
        o.setRoomId(req.getRoomId());
        o.setTitle(title);
        o.setDescription(req.getDescription());
        o.setPriority(priority);
        o.setStatus("SUBMITTED");
        o.setAssigneeUserId(null);

        Long id = orderRepository.insert(o);
        if (id == null) {
            throw new BizException(50000, "create repair failed");
        }
        return get(id);
    }

    @Transactional
    public RepairOrderDetailVo assign(Long id, AssignRepairRequest req) {
        RepairOrder order = orderRepository.findById(id);
        if (order == null) {
            throw new BizException(40004, "repair not found");
        }
        if (isFinal(order.getStatus())) {
            throw new BizException(40901, "repair already finished");
        }

        SysUser assignee = sysUserService.getById(req.getAssigneeUserId());
        if (assignee == null || (assignee.getDeleted() != null && assignee.getDeleted() == 1)
                || (assignee.getStatus() != null && !"ACTIVE".equalsIgnoreCase(assignee.getStatus()))) {
            throw new BizException(40004, "assignee user not found");
        }

        int affected = orderRepository.updateAssigneeIfNotFinal(id, req.getAssigneeUserId());
        if (affected == 0) {
            RepairOrder latest = orderRepository.findById(id);
            if (latest == null) {
                throw new BizException(40004, "repair not found");
            }
            throw new BizException(40901, "repair already finished");
        }

        writeLog(id, "ASSIGN", buildAssignLogContent(req.getAssigneeUserId(), req.getContent()));
        return get(id);
    }

    @Transactional
    public RepairOrderDetailVo transition(Long id, TransitionRepairRequest req) {
        RepairOrder order = orderRepository.findById(id);
        if (order == null) {
            throw new BizException(40004, "repair not found");
        }

        String current = normalizeStatus(order.getStatus());
        String target = normalizeStatus(req.getStatus());

        if (!"IN_PROGRESS".equals(target) && !"DONE".equals(target) && !"REJECTED".equals(target)) {
            throw new BizException(40001, "invalid status");
        }
        if (target.equals(current)) {
            throw new BizException(40902, "status already " + target);
        }
        if (isFinal(current)) {
            throw new BizException(40901, "repair already finished");
        }
        if (!isAllowedTransition(current, target)) {
            throw new BizException(40903, "invalid transition");
        }

        int affected = orderRepository.updateStatusIfCurrent(id, current, target);
        if (affected == 0) {
            RepairOrder latest = orderRepository.findById(id);
            if (latest == null) {
                throw new BizException(40004, "repair not found");
            }
            if (isFinal(latest.getStatus())) {
                throw new BizException(40901, "repair already finished");
            }
            throw new BizException(40904, "status changed");
        }

        String content = req.getContent();
        String msg = "to=" + target;
        if (content != null && !content.isBlank()) {
            msg = msg + "; " + content.trim();
        }
        writeLog(id, "UPDATE_STATUS", msg);
        return get(id);
    }

    public List<RepairLogVo> listLogs(Long id) {
        if (orderRepository.findById(id) == null) {
            throw new BizException(40004, "repair not found");
        }
        return logRepository.listByOrderId(id);
    }

    private void writeLog(Long repairOrderId, String action, String content) {
        UserPrincipal principal = SecurityUtils.currentUser();
        RepairLog log = new RepairLog();
        log.setRepairOrderId(repairOrderId);
        log.setAction(action);
        log.setContent(content);
        log.setCreatedBy(principal == null ? null : principal.getId());
        Long id = logRepository.insert(log);
        if (id == null) {
            throw new BizException(50000, "write repair log failed");
        }
    }

    private boolean isFinal(String status) {
        String s = normalizeStatus(status);
        return "DONE".equals(s) || "REJECTED".equals(s);
    }

    private boolean isAllowedTransition(String current, String target) {
        if ("SUBMITTED".equals(current)) {
            return "IN_PROGRESS".equals(target) || "REJECTED".equals(target);
        }
        if ("IN_PROGRESS".equals(current)) {
            return "DONE".equals(target) || "REJECTED".equals(target);
        }
        return false;
    }

    private String normalizePriority(String priority) {
        String p = priority == null || priority.isBlank() ? "MEDIUM" : priority.trim().toUpperCase();
        if (!"LOW".equals(p) && !"MEDIUM".equals(p) && !"HIGH".equals(p)) {
            throw new BizException(40001, "invalid priority");
        }
        return p;
    }

    private String normalizeStatus(String status) {
        return status == null ? "" : status.trim().toUpperCase();
    }

    private String buildAssignLogContent(Long assigneeUserId, String content) {
        if (content != null && !content.isBlank()) {
            return content.trim();
        }
        return "assigneeUserId=" + assigneeUserId;
    }

    private String resolveOrderBy(String sortBy, String sortOrder) {
        String column;
        if ("createdAt".equalsIgnoreCase(sortBy) || "created_at".equalsIgnoreCase(sortBy)) {
            column = "r.created_at";
        } else if ("id".equalsIgnoreCase(sortBy)) {
            column = "r.id";
        } else {
            column = "r.id";
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

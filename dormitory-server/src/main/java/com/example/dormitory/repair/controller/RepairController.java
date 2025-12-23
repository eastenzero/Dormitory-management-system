package com.example.dormitory.repair.controller;

import com.example.dormitory.common.ApiResponse;
import com.example.dormitory.common.PageResult;
import com.example.dormitory.repair.dto.AssignRepairRequest;
import com.example.dormitory.repair.dto.CreateRepairRequest;
import com.example.dormitory.repair.dto.TransitionRepairRequest;
import com.example.dormitory.repair.service.RepairService;
import com.example.dormitory.repair.vo.RepairLogVo;
import com.example.dormitory.repair.vo.RepairOrderDetailVo;
import com.example.dormitory.repair.vo.RepairOrderVo;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/repairs")
public class RepairController {

    private final RepairService repairService;

    public RepairController(RepairService repairService) {
        this.repairService = repairService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('repair:order:read')")
    public ApiResponse<PageResult<RepairOrderVo>> list(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String priority,
            @RequestParam(required = false) Long assigneeUserId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer pageSize,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String sortOrder
    ) {
        return ApiResponse.ok(repairService.list(status, priority, assigneeUserId, keyword, page, pageSize, sortBy, sortOrder));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('repair:order:read')")
    public ApiResponse<RepairOrderDetailVo> get(@PathVariable Long id) {
        return ApiResponse.ok(repairService.get(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('repair:order:write')")
    public ApiResponse<RepairOrderDetailVo> create(@Valid @RequestBody CreateRepairRequest req) {
        return ApiResponse.ok(repairService.create(req));
    }

    @PostMapping("/{id}/assign")
    @PreAuthorize("hasAuthority('repair:order:assign')")
    public ApiResponse<RepairOrderDetailVo> assign(@PathVariable Long id, @Valid @RequestBody AssignRepairRequest req) {
        return ApiResponse.ok(repairService.assign(id, req));
    }

    @PostMapping("/{id}/transition")
    @PreAuthorize("hasAuthority('repair:order:write')")
    public ApiResponse<RepairOrderDetailVo> transition(@PathVariable Long id, @Valid @RequestBody TransitionRepairRequest req) {
        return ApiResponse.ok(repairService.transition(id, req));
    }

    @GetMapping("/{id}/logs")
    @PreAuthorize("hasAuthority('repair:log:read')")
    public ApiResponse<List<RepairLogVo>> logs(@PathVariable Long id) {
        return ApiResponse.ok(repairService.listLogs(id));
    }
}

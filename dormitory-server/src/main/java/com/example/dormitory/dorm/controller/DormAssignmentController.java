package com.example.dormitory.dorm.controller;

import com.example.dormitory.common.ApiResponse;
import com.example.dormitory.common.PageResult;
import com.example.dormitory.dorm.dto.CreateDormAssignmentRequest;
import com.example.dormitory.dorm.dto.EndDormAssignmentRequest;
import com.example.dormitory.dorm.service.DormAssignmentService;
import com.example.dormitory.dorm.vo.DormAssignmentVo;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/dorm/assignments")
public class DormAssignmentController {

    private final DormAssignmentService dormAssignmentService;

    public DormAssignmentController(DormAssignmentService dormAssignmentService) {
        this.dormAssignmentService = dormAssignmentService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('dorm:assignment:read')")
    public ApiResponse<PageResult<DormAssignmentVo>> list(
            @RequestParam(required = false) Long studentId,
            @RequestParam(required = false) Long buildingId,
            @RequestParam(required = false) Long roomId,
            @RequestParam(required = false) Long bedId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer pageSize,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String sortOrder
    ) {
        return ApiResponse.ok(dormAssignmentService.list(studentId, buildingId, roomId, bedId, status, page, pageSize, sortBy, sortOrder));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('dorm:assignment:read')")
    public ApiResponse<DormAssignmentVo> get(@PathVariable Long id) {
        return ApiResponse.ok(dormAssignmentService.get(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('dorm:assignment:write')")
    public ApiResponse<DormAssignmentVo> create(@Valid @RequestBody CreateDormAssignmentRequest req) {
        return ApiResponse.ok(dormAssignmentService.create(req));
    }

    @PostMapping("/{id}/end")
    @PreAuthorize("hasAuthority('dorm:assignment:write')")
    public ApiResponse<DormAssignmentVo> end(@PathVariable Long id, @RequestBody(required = false) EndDormAssignmentRequest req) {
        return ApiResponse.ok(dormAssignmentService.end(id, req));
    }
}

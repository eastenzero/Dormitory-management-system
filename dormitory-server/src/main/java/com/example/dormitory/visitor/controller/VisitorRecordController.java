package com.example.dormitory.visitor.controller;

import com.example.dormitory.common.ApiResponse;
import com.example.dormitory.common.PageResult;
import com.example.dormitory.visitor.dto.CreateVisitorRecordRequest;
import com.example.dormitory.visitor.service.VisitorRecordService;
import com.example.dormitory.visitor.vo.VisitorRecordVo;
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
@RequestMapping("/api/v1/visitors")
public class VisitorRecordController {

    private final VisitorRecordService service;

    public VisitorRecordController(VisitorRecordService service) {
        this.service = service;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('visitor:record:read')")
    public ApiResponse<PageResult<VisitorRecordVo>> list(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String fromAt,
            @RequestParam(required = false) String toAt,
            @RequestParam(required = false) Long studentId,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer pageSize,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String sortOrder
    ) {
        return ApiResponse.ok(service.list(status, keyword, fromAt, toAt, studentId, page, pageSize, sortBy, sortOrder));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('visitor:record:read')")
    public ApiResponse<VisitorRecordVo> get(@PathVariable Long id) {
        return ApiResponse.ok(service.get(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('visitor:record:write')")
    public ApiResponse<VisitorRecordVo> create(@Valid @RequestBody CreateVisitorRecordRequest req) {
        return ApiResponse.ok(service.create(req));
    }

    @PostMapping("/{id}/leave")
    @PreAuthorize("hasAuthority('visitor:record:write')")
    public ApiResponse<VisitorRecordVo> leave(@PathVariable Long id) {
        return ApiResponse.ok(service.leave(id));
    }
}

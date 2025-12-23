package com.example.dormitory.dorm.controller;

import com.example.dormitory.common.ApiResponse;
import com.example.dormitory.common.PageResult;
import com.example.dormitory.dorm.dto.CreateDormBedRequest;
import com.example.dormitory.dorm.dto.UpdateDormBedRequest;
import com.example.dormitory.dorm.service.DormBedService;
import com.example.dormitory.dorm.vo.DormBedVo;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/dorm/beds")
public class DormBedController {

    private final DormBedService dormBedService;

    public DormBedController(DormBedService dormBedService) {
        this.dormBedService = dormBedService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('dorm:bed:read')")
    public ApiResponse<PageResult<DormBedVo>> list(
            @RequestParam(required = false) Long buildingId,
            @RequestParam(required = false) Long roomId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer pageSize,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String sortOrder
    ) {
        return ApiResponse.ok(dormBedService.list(buildingId, roomId, status, keyword, page, pageSize, sortBy, sortOrder));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('dorm:bed:read')")
    public ApiResponse<DormBedVo> get(@PathVariable Long id) {
        return ApiResponse.ok(dormBedService.get(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('dorm:bed:write')")
    public ApiResponse<DormBedVo> create(@Valid @RequestBody CreateDormBedRequest req) {
        return ApiResponse.ok(dormBedService.create(req));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('dorm:bed:write')")
    public ApiResponse<DormBedVo> update(@PathVariable Long id, @Valid @RequestBody UpdateDormBedRequest req) {
        return ApiResponse.ok(dormBedService.update(id, req));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('dorm:bed:write')")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        dormBedService.delete(id);
        return ApiResponse.ok(null);
    }
}

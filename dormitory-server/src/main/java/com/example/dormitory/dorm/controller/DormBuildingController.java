package com.example.dormitory.dorm.controller;

import com.example.dormitory.common.ApiResponse;
import com.example.dormitory.common.PageResult;
import com.example.dormitory.dorm.dto.CreateDormBuildingRequest;
import com.example.dormitory.dorm.dto.UpdateDormBuildingRequest;
import com.example.dormitory.dorm.service.DormBuildingService;
import com.example.dormitory.dorm.vo.DormBuildingVo;
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
@RequestMapping("/api/v1/dorm/buildings")
public class DormBuildingController {

    private final DormBuildingService dormBuildingService;

    public DormBuildingController(DormBuildingService dormBuildingService) {
        this.dormBuildingService = dormBuildingService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('dorm:building:read')")
    public ApiResponse<PageResult<DormBuildingVo>> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String genderLimit,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer pageSize,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String sortOrder
    ) {
        return ApiResponse.ok(dormBuildingService.list(keyword, genderLimit, page, pageSize, sortBy, sortOrder));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('dorm:building:read')")
    public ApiResponse<DormBuildingVo> get(@PathVariable Long id) {
        return ApiResponse.ok(dormBuildingService.get(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('dorm:building:write')")
    public ApiResponse<DormBuildingVo> create(@Valid @RequestBody CreateDormBuildingRequest req) {
        return ApiResponse.ok(dormBuildingService.create(req));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('dorm:building:write')")
    public ApiResponse<DormBuildingVo> update(@PathVariable Long id, @Valid @RequestBody UpdateDormBuildingRequest req) {
        return ApiResponse.ok(dormBuildingService.update(id, req));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('dorm:building:write')")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        dormBuildingService.delete(id);
        return ApiResponse.ok(null);
    }
}

package com.example.dormitory.dorm.controller;

import com.example.dormitory.common.ApiResponse;
import com.example.dormitory.common.PageResult;
import com.example.dormitory.dorm.dto.CreateDormRoomRequest;
import com.example.dormitory.dorm.dto.UpdateDormRoomRequest;
import com.example.dormitory.dorm.service.DormRoomService;
import com.example.dormitory.dorm.vo.DormRoomVo;
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
@RequestMapping("/api/v1/dorm/rooms")
public class DormRoomController {

    private final DormRoomService dormRoomService;

    public DormRoomController(DormRoomService dormRoomService) {
        this.dormRoomService = dormRoomService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('dorm:room:read')")
    public ApiResponse<PageResult<DormRoomVo>> list(
            @RequestParam(required = false) Long buildingId,
            @RequestParam(required = false) Integer floorNo,
            @RequestParam(required = false) String roomNo,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer pageSize,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String sortOrder
    ) {
        return ApiResponse.ok(dormRoomService.list(buildingId, floorNo, roomNo, status, keyword, page, pageSize, sortBy, sortOrder));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('dorm:room:read')")
    public ApiResponse<DormRoomVo> get(@PathVariable Long id) {
        return ApiResponse.ok(dormRoomService.get(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('dorm:room:write')")
    public ApiResponse<DormRoomVo> create(@Valid @RequestBody CreateDormRoomRequest req) {
        return ApiResponse.ok(dormRoomService.create(req));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('dorm:room:write')")
    public ApiResponse<DormRoomVo> update(@PathVariable Long id, @Valid @RequestBody UpdateDormRoomRequest req) {
        return ApiResponse.ok(dormRoomService.update(id, req));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('dorm:room:write')")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        dormRoomService.delete(id);
        return ApiResponse.ok(null);
    }
}

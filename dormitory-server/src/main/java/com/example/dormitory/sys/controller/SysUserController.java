package com.example.dormitory.sys.controller;

import com.example.dormitory.common.ApiResponse;
import com.example.dormitory.sys.model.SysUser;
import com.example.dormitory.sys.service.SysUserService;
import com.example.dormitory.sys.vo.SysUserVo;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/sys/users")
public class SysUserController {

    private final SysUserService sysUserService;

    public SysUserController(SysUserService sysUserService) {
        this.sysUserService = sysUserService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('sys:user:read')")
    public ApiResponse<List<SysUserVo>> listUsers() {
        List<SysUser> list = sysUserService.listUsers();
        List<SysUserVo> voList = list.stream().map(u -> {
            SysUserVo vo = new SysUserVo();
            vo.setId(u.getId());
            vo.setUsername(u.getUsername());
            vo.setRealName(u.getRealName());
            vo.setStatus(u.getStatus());
            return vo;
        }).toList();
        return ApiResponse.ok(voList);
    }
}

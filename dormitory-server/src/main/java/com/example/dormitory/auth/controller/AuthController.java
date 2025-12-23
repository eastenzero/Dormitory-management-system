package com.example.dormitory.auth.controller;

import com.example.dormitory.auth.dto.LoginRequest;
import com.example.dormitory.auth.dto.RegisterRequest;
import com.example.dormitory.auth.vo.LoginResponse;
import com.example.dormitory.auth.vo.MeResponse;
import com.example.dormitory.common.ApiResponse;
import com.example.dormitory.security.JwtService;
import com.example.dormitory.security.SecurityUtils;
import com.example.dormitory.security.UserPrincipal;
import com.example.dormitory.sys.model.SysUser;
import com.example.dormitory.sys.service.SysUserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final SysUserService sysUserService;
    private final JwtService jwtService;

    public AuthController(SysUserService sysUserService, JwtService jwtService) {
        this.sysUserService = sysUserService;
        this.jwtService = jwtService;
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest req) {
        SysUser user = sysUserService.authenticate(req.getUsername(), req.getPassword());
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.fail(40101, "unauthorized"));
        }
        String token = jwtService.createToken(user.getId(), user.getUsername());
        return ResponseEntity.ok(ApiResponse.ok(new LoginResponse(token)));
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<LoginResponse>> register(@Valid @RequestBody RegisterRequest req) {
        SysUser user = sysUserService.register(req.getUsername(), req.getPassword(), req.getRealName());
        String token = jwtService.createToken(user.getId(), user.getUsername());
        return ResponseEntity.ok(ApiResponse.ok(new LoginResponse(token)));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<MeResponse>> me() {
        UserPrincipal principal = SecurityUtils.currentUser();
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.fail(40101, "unauthorized"));
        }

        List<String> roles = sysUserService.getRoleCodes(principal.getId());
        List<String> permissions = sysUserService.getPermissionCodes(principal.getId());

        MeResponse resp = new MeResponse();
        resp.setId(principal.getId());
        resp.setUsername(principal.getUsername());
        resp.setRealName(principal.getRealName());
        resp.setRoles(roles);
        resp.setPermissions(permissions);
        return ResponseEntity.ok(ApiResponse.ok(resp));
    }
}

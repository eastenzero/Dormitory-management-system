package com.example.dormitory.sys.service;

import com.example.dormitory.common.BizException;
import com.example.dormitory.security.UserPrincipal;
import com.example.dormitory.sys.model.SysUser;
import com.example.dormitory.sys.repository.SysUserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SysUserService {

    private final SysUserRepository repository;
    private final PasswordEncoder passwordEncoder;

    public SysUserService(SysUserRepository repository, PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
    }

    public SysUser authenticate(String username, String password) {
        SysUser user = repository.findByUsername(username);
        if (user == null) {
            return null;
        }
        if (user.getDeleted() != null && user.getDeleted() == 1) {
            return null;
        }
        if (user.getStatus() != null && !"ACTIVE".equalsIgnoreCase(user.getStatus())) {
            return null;
        }
        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            return null;
        }
        return user;
    }

    public SysUser register(String username, String password, String realName) {
        if (username == null) {
            throw new BizException(40001, "username required");
        }
        String u = username.trim();
        if (u.isEmpty()) {
            throw new BizException(40001, "username required");
        }

        SysUser existing = repository.findByUsername(u);
        if (existing != null) {
            throw new BizException(40901, "username already exists");
        }

        SysUser user = new SysUser();
        user.setUsername(u);
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setRealName(realName == null || realName.trim().isEmpty() ? null : realName.trim());
        user.setStatus("ACTIVE");
        user.setDeleted(0);

        Long id = repository.insert(user);
        if (id == null) {
            throw new BizException(50000, "register failed");
        }
        repository.bindRoleByCode(id, "USER");
        user.setId(id);
        return user;
    }

    public UserPrincipal getPrincipal(Long userId, String usernameFromToken) {
        SysUser user = repository.findById(userId);
        if (user == null) {
            return null;
        }
        if (user.getDeleted() != null && user.getDeleted() == 1) {
            return null;
        }
        if (user.getStatus() != null && !"ACTIVE".equalsIgnoreCase(user.getStatus())) {
            return null;
        }
        if (usernameFromToken != null && !usernameFromToken.equals(user.getUsername())) {
            return null;
        }
        return new UserPrincipal(user.getId(), user.getUsername(), user.getRealName());
    }

    public SysUser getById(Long userId) {
        return repository.findById(userId);
    }

    public List<String> getRoleCodes(Long userId) {
        return repository.findRoleCodesByUserId(userId);
    }

    public List<String> getPermissionCodes(Long userId) {
        return repository.findPermissionCodesByUserId(userId);
    }

    public List<SysUser> listUsers() {
        return repository.listUsers();
    }
}

package com.example.dormitory.security;

import com.example.dormitory.sys.service.SysUserService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtService jwtService;
    private final SysUserService sysUserService;

    public JwtAuthenticationFilter(JwtService jwtService, SysUserService sysUserService) {
        this.jwtService = jwtService;
        this.sysUserService = sysUserService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ") && SecurityContextHolder.getContext().getAuthentication() == null) {
            String token = header.substring("Bearer ".length());
            try {
                Jws<Claims> jws = jwtService.parseToken(token);
                Claims claims = jws.getPayload();
                Long userId = Long.valueOf(claims.getSubject());
                String username = claims.get("username", String.class);

                UserPrincipal principal = sysUserService.getPrincipal(userId, username);
                if (principal != null) {
                    List<String> permissions = sysUserService.getPermissionCodes(userId);
                    List<SimpleGrantedAuthority> authorities = permissions.stream()
                            .map(SimpleGrantedAuthority::new)
                            .toList();
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(principal, null, authorities);
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    log.debug("JWT authenticated: userId={}, username={}, perms={}", userId, username, authorities.size());
                } else {
                    log.warn("JWT token accepted but principal not found/invalid: userId={}, username={}", userId, username);
                }
            } catch (Exception e) {
                log.warn("JWT authentication failed: {} {}", e.getClass().getSimpleName(), e.getMessage());
                SecurityContextHolder.clearContext();
            }
        }

        filterChain.doFilter(request, response);
    }
}

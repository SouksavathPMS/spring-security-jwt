package com.kyedev.spring_security_jwt.controller;

import com.kyedev.spring_security_jwt.dto.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
public class UserController {

    // Accessible by any authenticated user
    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getProfile() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        Map<String, Object> profile = new HashMap<>();
        profile.put("username", auth.getName());
        profile.put("authorities", auth.getAuthorities());
        profile.put("authenticated", auth.isAuthenticated());

        return ResponseEntity.ok(ApiResponse.success("Profile retrieved successfully", profile));
    }

    // Only users with ROLE_USER can access
    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<String>> getDashboard() {
        return ResponseEntity.ok(ApiResponse.success("Dashboard retrieved successfully", "Welcome to user dashboard"));
    }

    // Only users with ROLE_USER or ROLE_ADMIN
    @GetMapping("/data")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<ApiResponse<String>> getUserData() {
        return ResponseEntity.ok(ApiResponse.success("Data retrieved successfully", "Welcome to user data"));
    }
}

package com.kyedev.spring_security_jwt.controller;

import com.kyedev.spring_security_jwt.dto.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/public")
public class PublicController {

    @GetMapping("/health")
    public ResponseEntity<ApiResponse<Map<String, Object>>> health() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("timestamp", LocalDateTime.now());
        health.put("service", "Spring Boot JWT Authentication");

        return ResponseEntity.ok(
                ApiResponse.success("Service is running", health)
        );
    }

    @GetMapping("/info")
    public ResponseEntity<ApiResponse<Map<String, String>>> info() {
        Map<String, String> info = new HashMap<>();
        info.put("application", "Spring Boot JWT Auth");
        info.put("version", "1.0.0");
        info.put("description", "JWT Authentication with Role-Based Access Control");

        return ResponseEntity.ok(
                ApiResponse.success("Application info", info)
        );
    }

    @GetMapping("/welcome")
    public ResponseEntity<ApiResponse<String>> welcome() {
        return ResponseEntity.ok(
                ApiResponse.success("Welcome!", "This is a public endpoint")
        );
    }
}
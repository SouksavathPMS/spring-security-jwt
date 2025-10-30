package com.kyedev.spring_security_jwt.controller;

import com.kyedev.spring_security_jwt.dto.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/moderator")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('MODERATOR', 'ADMIN')") // MODERATOR or ADMIN can access
public class ModeratorController {

    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<String>> getModeratorDashboard() {
        return ResponseEntity.ok(
                ApiResponse.success("Moderator dashboard accessed successfully",
                        "Welcome to moderator dashboard")
        );
    }

    @PostMapping("/moderate")
    public ResponseEntity<ApiResponse<String>> moderateContent(@RequestBody String content) {
        // Moderate content logic here
        return ResponseEntity.ok(
                ApiResponse.success("Content moderated successfully",
                        "Content has been reviewed")
        );
    }
}

package com.issuetracker.controller;

import com.issuetracker.dto.ApiResponse;
import com.issuetracker.model.User;
import com.issuetracker.model.enums.Role;
import com.issuetracker.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

// All endpoints here require ROLE_ADMIN — enforced by @PreAuthorize.
// If a non-admin calls these, Spring Security returns 403 Forbidden automatically.
@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    @Autowired private UserService userService;

    // List all users in the system
    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    // Change a user's role (promote to ADMIN, demote to REPORTER, etc.)
    @PutMapping("/users/{id}/role")
    public ResponseEntity<?> updateRole(@PathVariable Long id, @RequestBody Map<String, String> body) {
        try {
            Role role = Role.valueOf(body.get("role"));
            User updated = userService.updateRole(id, role);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "Invalid role. Use ROLE_ADMIN, ROLE_DEVELOPER, or ROLE_REPORTER"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage()));
        }
    }
}

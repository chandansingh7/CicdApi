package com.pos.controller;

import com.pos.dto.request.AdminUpdateUserRequest;
import com.pos.dto.request.ChangePasswordRequest;
import com.pos.dto.request.UpdateProfileRequest;
import com.pos.dto.response.ApiResponse;
import com.pos.dto.response.UserResponse;
import com.pos.service.AuthService;
import com.pos.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final AuthService authService;
    private final UserService userService;

    // ── Self-service (any authenticated user) ─────────────────────────────────

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> getProfile(Authentication auth) {
        return ResponseEntity.ok(ApiResponse.ok(userService.getProfile(auth.getName())));
    }

    @PutMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> updateProfile(
            @Valid @RequestBody UpdateProfileRequest request,
            Authentication auth) {
        return ResponseEntity.ok(ApiResponse.ok("Profile updated", userService.updateProfile(auth.getName(), request)));
    }

    @PutMapping("/me/password")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @Valid @RequestBody ChangePasswordRequest request,
            Authentication auth) {
        authService.changePassword(auth.getName(), request);
        return ResponseEntity.ok(ApiResponse.ok("Password updated successfully", null));
    }

    // ── Admin-only ────────────────────────────────────────────────────────────

    @GetMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getAllUsers() {
        return ResponseEntity.ok(ApiResponse.ok(userService.getAllUsers()));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<ApiResponse<UserResponse>> adminUpdateUser(
            @PathVariable Long id,
            @Valid @RequestBody AdminUpdateUserRequest request,
            Authentication auth) {
        return ResponseEntity.ok(ApiResponse.ok("User updated", userService.adminUpdateUser(id, request, auth.getName())));
    }

    @PatchMapping("/{id}/toggle-active")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<ApiResponse<UserResponse>> toggleActive(
            @PathVariable Long id,
            Authentication auth) {
        return ResponseEntity.ok(ApiResponse.ok("User status updated", userService.toggleActive(id, auth.getName())));
    }
}

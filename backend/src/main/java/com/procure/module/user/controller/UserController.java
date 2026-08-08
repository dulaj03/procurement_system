package com.procure.module.user.controller;

import com.procure.common.response.ApiResponse;
import com.procure.module.user.dto.UserDtos.UserCreateRequest;
import com.procure.module.user.dto.UserDtos.UserResponse;
import com.procure.module.user.dto.UserDtos.UserUpdateRequest;
import com.procure.module.user.dto.UserDtos.UserProfileResponse;
import com.procure.module.user.service.UserService;
import com.procure.security.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "Employee and user profile management APIs")
public class UserController {

    private final UserService userService;

    @PostMapping
    @PreAuthorize("hasAuthority('USER:WRITE')")
    @Operation(summary = "Register a new user / employee")
    public ResponseEntity<ApiResponse<UserResponse>> createUser(
            @Valid @RequestBody UserCreateRequest request) {
        UserResponse response = userService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("User registered successfully", response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('USER:WRITE')")
    @Operation(summary = "Update employee details / roles")
    public ResponseEntity<ApiResponse<UserResponse>> updateUser(
            @PathVariable UUID id,
            @Valid @RequestBody UserUpdateRequest request) {
        UserResponse response = userService.updateUser(id, request);
        return ResponseEntity.ok(ApiResponse.success("Employee details updated", response));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('USER:READ')")
    @Operation(summary = "Get employee details by ID")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(@PathVariable UUID id) {
        UserResponse response = userService.getUserById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('USER:READ')")
    @Operation(summary = "Get all registered employees")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getAllUsers() {
        List<UserResponse> response = userService.getAllUsers();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('USER:DELETE')")
    @Operation(summary = "Soft delete employee record")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable UUID id) {
        userService.deleteUser(id);
        return ResponseEntity.ok(ApiResponse.success("User record deleted", null));
    }

    @GetMapping("/profile")
    @Operation(summary = "Get current authenticated user profile details")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getCurrentUserProfile() {
        String email = SecurityUtils.getCurrentUserEmail();
        if (email == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Unauthorized: no active session found"));
        }
        UserProfileResponse response = userService.getUserProfile(email);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}

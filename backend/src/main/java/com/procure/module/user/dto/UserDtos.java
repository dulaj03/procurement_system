package com.procure.module.user.dto;

import com.procure.module.user.entity.User.UserStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;
import java.util.UUID;

public class UserDtos {

    @Data
    public static class UserCreateRequest {
        @NotBlank(message = "First name is required")
        private String firstName;

        @NotBlank(message = "Last name is required")
        private String lastName;

        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        private String email;

        @NotBlank(message = "Password is required")
        @Size(min = 6, message = "Password must be at least 6 characters")
        private String password;

        private String phone;
        private String employeeCode;

        @NotEmpty(message = "At least one role is required")
        private List<String> roles; // List of Role Names (e.g. ROLE_ADMIN, ROLE_EMPLOYEE)

        private UUID companyId;
        private UUID branchId;
    }

    @Data
    public static class UserUpdateRequest {
        @NotBlank(message = "First name is required")
        private String firstName;

        @NotBlank(message = "Last name is required")
        private String lastName;

        private String phone;
        private String employeeCode;

        @NotEmpty(message = "At least one role is required")
        private List<String> roles;

        private UUID companyId;
        private UUID branchId;
        private UserStatus status;
    }

    @Data
    public static class UserResponse {
        private UUID id;
        private String firstName;
        private String lastName;
        private String email;
        private String phone;
        private String employeeCode;
        private UserStatus status;
        private UUID companyId;
        private String companyName;
        private UUID branchId;
        private String branchName;
        private List<String> roles;
    }

    @Data
    public static class UserProfileResponse {
        private UUID id;
        private String firstName;
        private String lastName;
        private String email;
        private String phone;
        private String employeeCode;
        private List<String> roles;
        private List<String> permissions;
        private UUID companyId;
        private String companyName;
        private UUID branchId;
        private String branchName;
    }
}

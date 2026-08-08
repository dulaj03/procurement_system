package com.procure.module.company.dto;

import com.procure.module.company.entity.Branch.BranchStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.UUID;

public class BranchDtos {

    @Data
    public static class BranchRequest {
        @NotBlank(message = "Branch name is required")
        @Size(max = 255)
        private String name;

        @NotBlank(message = "Branch code is required")
        @Size(min = 2, max = 50, message = "Code must be between 2 and 50 characters")
        private String code;

        private String address;
        private String city;
        private String country;
        private String phone;

        @Email(message = "Invalid email format")
        private String email;
    }

    @Data
    public static class BranchResponse {
        private UUID id;
        private String name;
        private String code;
        private String address;
        private String city;
        private String country;
        private String phone;
        private String email;
        private BranchStatus status;
        private UUID companyId;
    }
}

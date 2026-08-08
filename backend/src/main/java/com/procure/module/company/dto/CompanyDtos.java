package com.procure.module.company.dto;

import com.procure.module.company.entity.Company.CompanyStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

public class CompanyDtos {

    @Data
    public static class CompanyRequest {
        @NotBlank(message = "Company name is required")
        @Size(max = 255)
        private String name;

        @NotBlank(message = "Company code is required")
        @Size(min = 3, max = 50, message = "Code must be between 3 and 50 characters")
        private String code;

        @Size(max = 100)
        private String registrationNumber;

        @Size(max = 100)
        private String taxNumber;

        private String address;
        private String city;
        private String country;

        @Email(message = "Invalid email format")
        private String email;

        private String phone;
        private String logoUrl;
    }

    @Data
    public static class CompanyResponse {
        private UUID id;
        private String name;
        private String code;
        private String registrationNumber;
        private String taxNumber;
        private String address;
        private String city;
        private String country;
        private String email;
        private String phone;
        private String logoUrl;
        private CompanyStatus status;
        private LocalDateTime createdAt;
    }
}

package com.procure.module.supplier.dto;

import com.procure.module.supplier.entity.Supplier.SupplierStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class SupplierDtos {

    // ── Request DTOs ─────────────────────────────────────────────

    public record SupplierRequest(
            @NotBlank(message = "Supplier name is required")
            @Size(max = 255)
            String name,

            @NotBlank(message = "Supplier code is required")
            @Size(max = 50)
            String code,

            @Email(message = "Invalid email format")
            @Size(max = 255)
            String email,

            @Size(max = 50)
            String phone,

            String address,

            @Size(max = 100)
            String city,

            @Size(max = 100)
            String country,

            @Size(max = 100)
            String taxNumber,

            @Size(max = 100)
            String registrationNumber,

            @Size(max = 255)
            String website,

            @Size(max = 100)
            String paymentTerms,

            @DecimalMin(value = "0.0", inclusive = true)
            BigDecimal creditLimit,

            SupplierStatus status,

            UUID companyId,

            @Valid
            List<ContactRequest> contacts
    ) {}

    public record ContactRequest(
            UUID id,  // null for new, non-null for update

            @NotBlank(message = "Contact name is required")
            @Size(max = 255)
            String name,

            @Size(max = 100)
            String designation,

            @Email(message = "Invalid email format")
            @Size(max = 255)
            String email,

            @Size(max = 50)
            String phone,

            boolean primary
    ) {}

    public record RatingRequest(
            @NotNull(message = "Rating is required")
            @Min(value = 1, message = "Rating must be at least 1")
            @Max(value = 5, message = "Rating must be at most 5")
            Integer rating
    ) {}

    // ── Response DTOs ────────────────────────────────────────────

    public record SupplierResponse(
            UUID id,
            String name,
            String code,
            String email,
            String phone,
            String address,
            String city,
            String country,
            String taxNumber,
            String registrationNumber,
            String website,
            String paymentTerms,
            BigDecimal creditLimit,
            Integer rating,
            SupplierStatus status,
            UUID companyId,
            List<ContactResponse> contacts,
            LocalDateTime createdAt,
            LocalDateTime updatedAt,
            String createdBy
    ) {}

    public record ContactResponse(
            UUID id,
            String name,
            String designation,
            String email,
            String phone,
            boolean primary
    ) {}

    public record SupplierSummary(
            UUID id,
            String name,
            String code,
            String email,
            String phone,
            String city,
            String country,
            Integer rating,
            SupplierStatus status,
            int contactCount
    ) {}
}

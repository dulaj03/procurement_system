package com.procure.module.purchase.dto;

import com.procure.module.purchase.entity.PurchaseRequest.PRPriority;
import com.procure.module.purchase.entity.PurchaseRequest.PRStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class PurchaseRequestDtos {

    public record PRItemRequest(
            @NotNull UUID productId,
            @NotNull @DecimalMin("0.01") BigDecimal quantity,
            String unitOfMeasure,
            BigDecimal estimatedUnitPrice,
            String specifications,
            String notes
    ) {}

    public record PRCreateRequest(
            @NotBlank @Size(max = 255) String title,
            String description,
            LocalDate requiredDate,
            PRPriority priority,
            @NotNull UUID branchId,
            @NotEmpty @Valid List<PRItemRequest> items
    ) {}

    public record PRRejectRequest(
            @NotBlank(message = "Reason is required when rejecting a PR") String reason
    ) {}

    public record PRItemResponse(
            UUID id,
            UUID productId,
            String productName,
            String sku,
            BigDecimal quantity,
            String unitOfMeasure,
            BigDecimal estimatedUnitPrice,
            BigDecimal estimatedTotalPrice,
            String specifications,
            String notes
    ) {}

    public record PRResponse(
            UUID id,
            String prNumber,
            String title,
            String description,
            LocalDate requiredDate,
            BigDecimal totalAmount,
            PRStatus status,
            PRPriority priority,
            String rejectionReason,
            UUID requestedById,
            String requestedByName,
            UUID approvedById,
            String approvedByName,
            LocalDateTime approvedAt,
            UUID branchId,
            String branchName,
            List<PRItemResponse> items,
            LocalDateTime createdAt
    ) {}

    public record PRSummary(
            UUID id,
            String prNumber,
            String title,
            LocalDate requiredDate,
            BigDecimal totalAmount,
            PRStatus status,
            PRPriority priority,
            String requestedByName,
            String branchName,
            int itemCount,
            LocalDateTime createdAt
    ) {}
}

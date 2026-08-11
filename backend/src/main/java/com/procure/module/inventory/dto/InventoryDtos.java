package com.procure.module.inventory.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class InventoryDtos {

    public record InventoryResponse(
            UUID id,
            UUID productId,
            String productName,
            String sku,
            String unitOfMeasure,
            UUID branchId,
            String branchName,
            BigDecimal quantityOnHand,
            BigDecimal quantityReserved,
            BigDecimal quantityOnOrder,
            BigDecimal availableQuantity,
            BigDecimal averageCost,
            Integer reorderLevel,
            boolean lowStock,
            LocalDateTime updatedAt
    ) {}

    public record StockAdjustRequest(
            @NotNull UUID productId,
            @NotNull UUID branchId,
            @NotNull(message = "Quantity is required") BigDecimal quantity,
            @NotBlank(message = "Reason is required") String reason,
            String referenceNumber
    ) {}

    public record StockTransferRequest(
            @NotNull UUID productId,
            @NotNull UUID fromBranchId,
            @NotNull UUID toBranchId,
            @NotNull @DecimalMin("0.01") BigDecimal quantity,
            String notes
    ) {}

    public record LowStockAlert(
            UUID productId,
            String productName,
            String sku,
            UUID branchId,
            String branchName,
            BigDecimal quantityOnHand,
            int reorderLevel
    ) {}

    public record StockMovementResponse(
            UUID id,
            String productName,
            String sku,
            String movementType,
            BigDecimal quantity,
            String fromBranch,
            String toBranch,
            String referenceNumber,
            String notes,
            LocalDateTime createdAt,
            String createdBy
    ) {}
}

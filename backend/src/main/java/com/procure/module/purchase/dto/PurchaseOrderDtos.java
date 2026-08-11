package com.procure.module.purchase.dto;

import com.procure.module.purchase.entity.PurchaseOrder.POStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class PurchaseOrderDtos {

    public record POItemRequest(
            @NotNull UUID productId,
            @NotNull @DecimalMin("0.01") BigDecimal quantityOrdered,
            @NotNull @DecimalMin("0.0") BigDecimal unitPrice,
            BigDecimal discountPercent,
            BigDecimal taxPercent,
            String unitOfMeasure,
            String notes
    ) {}

    public record POCreateRequest(
            UUID purchaseRequestId,
            @NotNull UUID supplierId,
            @NotNull UUID branchId,
            @NotNull LocalDate orderDate,
            LocalDate expectedDeliveryDate,
            String deliveryAddress,
            String paymentTerms,
            String currency,
            String notes,
            @NotEmpty @Valid List<POItemRequest> items
    ) {}

    public record POItemResponse(
            UUID id,
            UUID productId,
            String productName,
            String sku,
            BigDecimal quantityOrdered,
            BigDecimal quantityReceived,
            BigDecimal unitPrice,
            BigDecimal discountPercent,
            BigDecimal taxPercent,
            BigDecimal totalPrice,
            String unitOfMeasure
    ) {}

    public record POResponse(
            UUID id,
            String poNumber,
            UUID purchaseRequestId,
            String prNumber,
            UUID supplierId,
            String supplierName,
            UUID branchId,
            String branchName,
            LocalDate orderDate,
            LocalDate expectedDeliveryDate,
            String deliveryAddress,
            BigDecimal subtotal,
            BigDecimal taxAmount,
            BigDecimal discountAmount,
            BigDecimal totalAmount,
            String currency,
            String paymentTerms,
            POStatus status,
            List<POItemResponse> items,
            LocalDateTime createdAt
    ) {}

    public record POSummary(
            UUID id,
            String poNumber,
            String supplierName,
            String branchName,
            LocalDate orderDate,
            BigDecimal totalAmount,
            String currency,
            POStatus status,
            int itemCount,
            LocalDateTime createdAt
    ) {}
}

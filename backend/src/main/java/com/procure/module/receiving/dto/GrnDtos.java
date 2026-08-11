package com.procure.module.receiving.dto;

import com.procure.module.receiving.entity.GoodsReceiptNote.GRNStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class GrnDtos {

    public record GRNItemRequest(
            @NotNull UUID poItemId,
            @NotNull UUID productId,
            @NotNull @DecimalMin("0.0") BigDecimal quantityReceived,
            @NotNull @DecimalMin("0.0") BigDecimal quantityAccepted,
            BigDecimal quantityRejected,
            BigDecimal unitCost,
            String rejectionReason,
            String batchNumber,
            LocalDate expiryDate,
            String notes
    ) {}

    public record GRNCreateRequest(
            @NotNull UUID purchaseOrderId,
            @NotNull UUID branchId,
            @NotNull LocalDate receiptDate,
            String supplierInvoiceNumber,
            String deliveryNoteNumber,
            String notes,
            @NotEmpty @Valid List<GRNItemRequest> items
    ) {}

    public record GRNItemResponse(
            UUID id,
            UUID poItemId,
            UUID productId,
            String productName,
            String sku,
            BigDecimal quantityReceived,
            BigDecimal quantityAccepted,
            BigDecimal quantityRejected,
            BigDecimal unitCost,
            String rejectionReason,
            String batchNumber,
            LocalDate expiryDate
    ) {}

    public record GRNResponse(
            UUID id,
            String grnNumber,
            UUID purchaseOrderId,
            String poNumber,
            String supplierName,
            UUID branchId,
            String branchName,
            LocalDate receiptDate,
            String supplierInvoiceNumber,
            String deliveryNoteNumber,
            GRNStatus status,
            String receivedByName,
            List<GRNItemResponse> items,
            LocalDateTime createdAt
    ) {}
}

package com.procure.module.invoice.dto;

import com.procure.module.invoice.entity.Invoice.InvoiceStatus;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public class InvoiceDtos {

    public record InvoiceCreateRequest(
            @NotNull UUID supplierId,
            UUID purchaseOrderId,
            String supplierInvoiceNumber,
            @NotNull LocalDate invoiceDate,
            LocalDate dueDate,
            @NotNull @DecimalMin("0.01") BigDecimal totalAmount,
            BigDecimal taxAmount,
            String currency,
            String notes
    ) {}

    public record PaymentRecordRequest(
            @NotNull @DecimalMin("0.01") BigDecimal amount,
            @NotNull LocalDate paymentDate,
            String referenceNumber
    ) {}

    public record InvoiceResponse(
            UUID id,
            String invoiceNumber,
            String supplierInvoiceNumber,
            UUID supplierId,
            String supplierName,
            UUID purchaseOrderId,
            String poNumber,
            LocalDate invoiceDate,
            LocalDate dueDate,
            BigDecimal subtotal,
            BigDecimal taxAmount,
            BigDecimal totalAmount,
            BigDecimal paidAmount,
            BigDecimal outstandingAmount,
            String currency,
            InvoiceStatus status,
            boolean overdue,
            LocalDate paymentDate,
            String notes,
            LocalDateTime createdAt
    ) {}
}

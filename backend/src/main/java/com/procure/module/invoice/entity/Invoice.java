package com.procure.module.invoice.entity;

import com.procure.common.audit.BaseEntity;
import com.procure.module.purchase.entity.PurchaseOrder;
import com.procure.module.supplier.entity.Supplier;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "invoices")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Invoice extends BaseEntity {

    @Column(name = "invoice_number", nullable = false, unique = true)
    private String invoiceNumber;

    @Column(name = "supplier_invoice_number")
    private String supplierInvoiceNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id", nullable = false)
    private Supplier supplier;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "purchase_order_id")
    private PurchaseOrder purchaseOrder;

    @Column(name = "invoice_date", nullable = false)
    private LocalDate invoiceDate;

    @Column(name = "due_date")
    private LocalDate dueDate;

    @Column(name = "subtotal", precision = 18, scale = 4)
    private BigDecimal subtotal;

    @Column(name = "tax_amount", precision = 18, scale = 4)
    @Builder.Default
    private BigDecimal taxAmount = BigDecimal.ZERO;

    @Column(name = "total_amount", precision = 18, scale = 4, nullable = false)
    private BigDecimal totalAmount;

    @Column(name = "paid_amount", precision = 18, scale = 4)
    @Builder.Default
    private BigDecimal paidAmount = BigDecimal.ZERO;

    @Column(name = "currency")
    @Builder.Default
    private String currency = "USD";

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private InvoiceStatus status = InvoiceStatus.DRAFT;

    @Column(name = "payment_date")
    private LocalDate paymentDate;

    @Column(name = "notes")
    private String notes;

    public BigDecimal getOutstandingAmount() {
        return totalAmount.subtract(paidAmount);
    }

    public boolean isOverdue() {
        return dueDate != null &&
               LocalDate.now().isAfter(dueDate) &&
               status != InvoiceStatus.PAID;
    }

    public enum InvoiceStatus {
        DRAFT, SUBMITTED, APPROVED, PARTIALLY_PAID, PAID, OVERDUE, CANCELLED
    }
}

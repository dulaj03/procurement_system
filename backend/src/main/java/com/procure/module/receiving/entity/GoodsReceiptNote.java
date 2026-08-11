package com.procure.module.receiving.entity;

import com.procure.common.audit.BaseEntity;
import com.procure.module.company.entity.Branch;
import com.procure.module.purchase.entity.PurchaseOrder;
import com.procure.module.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

/**
 * Goods Receipt Note (GRN) — records physical receipt of goods from supplier.
 */
@Entity
@Table(name = "goods_receipt_notes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GoodsReceiptNote extends BaseEntity {

    @Column(name = "grn_number", nullable = false, unique = true)
    private String grnNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "purchase_order_id", nullable = false)
    private PurchaseOrder purchaseOrder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id", nullable = false)
    private Branch branch;

    @Column(name = "receipt_date", nullable = false)
    private LocalDate receiptDate;

    @Column(name = "supplier_invoice_number")
    private String supplierInvoiceNumber;

    @Column(name = "delivery_note_number")
    private String deliveryNoteNumber;

    @Column(name = "notes")
    private String notes;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private GRNStatus status = GRNStatus.DRAFT;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "received_by", nullable = false)
    private User receivedBy;

    @OneToMany(mappedBy = "grn", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<GRNItem> items;

    public enum GRNStatus {
        DRAFT, CONFIRMED, POSTED  // POSTED = stock updated in inventory
    }
}

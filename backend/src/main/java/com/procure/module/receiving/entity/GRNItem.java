package com.procure.module.receiving.entity;

import com.procure.common.audit.BaseEntity;
import com.procure.module.product.entity.Product;
import com.procure.module.purchase.entity.PurchaseOrderItem;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "grn_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GRNItem extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "grn_id", nullable = false)
    private GoodsReceiptNote grn;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "po_item_id", nullable = false)
    private PurchaseOrderItem poItem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "quantity_received", nullable = false, precision = 18, scale = 4)
    private BigDecimal quantityReceived;

    @Column(name = "quantity_accepted", precision = 18, scale = 4)
    private BigDecimal quantityAccepted;

    @Column(name = "quantity_rejected", precision = 18, scale = 4)
    @Builder.Default
    private BigDecimal quantityRejected = BigDecimal.ZERO;

    @Column(name = "unit_cost", precision = 18, scale = 4)
    private BigDecimal unitCost;

    @Column(name = "rejection_reason")
    private String rejectionReason;

    @Column(name = "batch_number")
    private String batchNumber;

    @Column(name = "expiry_date")
    private java.time.LocalDate expiryDate;

    @Column(name = "notes")
    private String notes;
}

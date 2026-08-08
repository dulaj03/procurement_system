package com.procure.module.inventory.entity;

import com.procure.common.audit.BaseEntity;
import com.procure.module.company.entity.Branch;
import com.procure.module.product.entity.Product;
import com.procure.module.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * Immutable ledger of all stock movements (receipts, transfers, adjustments).
 */
@Entity
@Table(name = "stock_movements")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockMovement extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_branch_id")
    private Branch fromBranch;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_branch_id")
    private Branch toBranch;

    @Enumerated(EnumType.STRING)
    @Column(name = "movement_type", nullable = false)
    private MovementType movementType;

    @Column(name = "quantity", nullable = false, precision = 18, scale = 4)
    private BigDecimal quantity;

    @Column(name = "unit_cost", precision = 18, scale = 4)
    private BigDecimal unitCost;

    @Column(name = "reference_number")
    private String referenceNumber;   // e.g., GRN-001, TR-001

    @Column(name = "reference_type")
    private String referenceType;     // e.g., GRN, TRANSFER, ADJUSTMENT

    @Column(name = "notes")
    private String notes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "performed_by")
    private User performedBy;

    public enum MovementType {
        RECEIPT,        // Stock received from supplier
        ISSUE,          // Stock issued/consumed
        TRANSFER_OUT,   // Sent to another branch
        TRANSFER_IN,    // Received from another branch
        ADJUSTMENT_IN,  // Manual positive adjustment
        ADJUSTMENT_OUT, // Manual negative adjustment
        RETURN          // Returned to supplier
    }
}

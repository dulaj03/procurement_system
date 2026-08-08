package com.procure.module.inventory.entity;

import com.procure.common.audit.BaseEntity;
import com.procure.module.company.entity.Branch;
import com.procure.module.product.entity.Product;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * Represents the current stock level of a product in a specific branch/warehouse.
 * One record per (product, branch) pair.
 */
@Entity
@Table(name = "inventory",
       uniqueConstraints = @UniqueConstraint(columnNames = {"product_id", "branch_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Inventory extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id", nullable = false)
    private Branch branch;

    @Column(name = "quantity_on_hand", nullable = false)
    @Builder.Default
    private BigDecimal quantityOnHand = BigDecimal.ZERO;

    @Column(name = "quantity_reserved", nullable = false)
    @Builder.Default
    private BigDecimal quantityReserved = BigDecimal.ZERO;

    @Column(name = "quantity_on_order", nullable = false)
    @Builder.Default
    private BigDecimal quantityOnOrder = BigDecimal.ZERO;

    @Column(name = "average_cost", precision = 18, scale = 4)
    private BigDecimal averageCost;

    /**
     * Available = OnHand - Reserved
     */
    public BigDecimal getAvailableQuantity() {
        return quantityOnHand.subtract(quantityReserved);
    }

    /**
     * Returns true if stock is below the product's reorder level
     */
    public boolean isLowStock() {
        Integer reorderLevel = product.getReorderLevel();
        if (reorderLevel == null) return false;
        return quantityOnHand.compareTo(BigDecimal.valueOf(reorderLevel)) <= 0;
    }
}

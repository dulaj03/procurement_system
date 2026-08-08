package com.procure.module.purchase.request.entity;

import com.procure.common.audit.BaseEntity;
import com.procure.module.product.entity.Product;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "purchase_request_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PurchaseRequestItem extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "purchase_request_id", nullable = false)
    private PurchaseRequest purchaseRequest;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "quantity", nullable = false, precision = 18, scale = 4)
    private BigDecimal quantity;

    @Column(name = "unit_of_measure")
    private String unitOfMeasure;

    @Column(name = "estimated_unit_price", precision = 18, scale = 4)
    private BigDecimal estimatedUnitPrice;

    @Column(name = "estimated_total_price", precision = 18, scale = 4)
    private BigDecimal estimatedTotalPrice;

    @Column(name = "specifications")
    private String specifications;

    @Column(name = "notes")
    private String notes;
}

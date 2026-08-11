package com.procure.module.purchase.entity;

import com.procure.common.audit.BaseEntity;
import com.procure.module.company.entity.Branch;
import com.procure.module.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "purchase_requests")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PurchaseRequest extends BaseEntity {

    @Column(name = "pr_number", nullable = false, unique = true)
    private String prNumber;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "description")
    private String description;

    @Column(name = "required_date")
    private LocalDate requiredDate;

    @Column(name = "total_amount", precision = 18, scale = 4)
    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private PRStatus status = PRStatus.DRAFT;

    @Enumerated(EnumType.STRING)
    @Column(name = "priority", nullable = false)
    @Builder.Default
    private PRPriority priority = PRPriority.MEDIUM;

    @Column(name = "rejection_reason")
    private String rejectionReason;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requested_by", nullable = false)
    private User requestedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by")
    private User approvedBy;

    @Column(name = "approved_at")
    private java.time.LocalDateTime approvedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id", nullable = false)
    private Branch branch;

    @OneToMany(mappedBy = "purchaseRequest", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<PurchaseRequestItem> items;

    public enum PRStatus {
        DRAFT, SUBMITTED, APPROVED, REJECTED, CANCELLED, CONVERTED
    }

    public enum PRPriority {
        LOW, MEDIUM, HIGH, URGENT
    }
}

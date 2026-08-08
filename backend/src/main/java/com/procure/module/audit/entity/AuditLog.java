package com.procure.module.audit.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Immutable;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Immutable audit log entry. Never updated or deleted — append-only.
 */
@Entity
@Table(name = "audit_logs")
@Immutable
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "entity_type", nullable = false)
    private String entityType;    // e.g., "PurchaseOrder"

    @Column(name = "entity_id", nullable = false)
    private String entityId;

    @Column(name = "action", nullable = false)
    private String action;        // CREATE, UPDATE, DELETE, APPROVE, REJECT

    @Column(name = "performed_by", nullable = false)
    private String performedBy;   // user email

    @Column(name = "performed_at", nullable = false)
    private LocalDateTime performedAt;

    @Column(name = "old_values", columnDefinition = "TEXT")
    private String oldValues;     // JSON snapshot

    @Column(name = "new_values", columnDefinition = "TEXT")
    private String newValues;     // JSON snapshot

    @Column(name = "ip_address")
    private String ipAddress;

    @Column(name = "user_agent")
    private String userAgent;

    @Column(name = "notes")
    private String notes;
}

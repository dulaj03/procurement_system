package com.procure.module.user.entity;

import com.procure.common.audit.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "permissions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Permission extends BaseEntity {

    /**
     * Permission name format: RESOURCE:ACTION
     * Examples: USER:READ, USER:WRITE, PURCHASE_ORDER:APPROVE
     */
    @Column(name = "name", nullable = false, unique = true)
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "module")
    private String module;
}

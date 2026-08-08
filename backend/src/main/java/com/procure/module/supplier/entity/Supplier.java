package com.procure.module.supplier.entity;

import com.procure.common.audit.BaseEntity;
import com.procure.module.company.entity.Company;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "suppliers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Supplier extends BaseEntity {

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "code", nullable = false, unique = true)
    private String code;

    @Column(name = "email")
    private String email;

    @Column(name = "phone")
    private String phone;

    @Column(name = "address")
    private String address;

    @Column(name = "city")
    private String city;

    @Column(name = "country")
    private String country;

    @Column(name = "tax_number")
    private String taxNumber;

    @Column(name = "registration_number")
    private String registrationNumber;

    @Column(name = "website")
    private String website;

    @Column(name = "payment_terms")
    private String paymentTerms;

    @Column(name = "credit_limit")
    private java.math.BigDecimal creditLimit;

    @Column(name = "rating")
    private Integer rating;  // 1-5 stars

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private SupplierStatus status = SupplierStatus.ACTIVE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id")
    private Company company;

    @OneToMany(mappedBy = "supplier", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<SupplierContact> contacts;

    public enum SupplierStatus {
        ACTIVE, INACTIVE, BLACKLISTED
    }
}

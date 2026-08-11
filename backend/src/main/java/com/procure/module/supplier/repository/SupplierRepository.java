package com.procure.module.supplier.repository;

import com.procure.module.supplier.entity.Supplier;
import com.procure.module.supplier.entity.Supplier.SupplierStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface SupplierRepository extends JpaRepository<Supplier, UUID> {

    boolean existsByCodeAndIsDeletedFalse(String code);

    boolean existsByCodeAndIsDeletedFalseAndIdNot(String code, UUID id);

    Optional<Supplier> findByIdAndIsDeletedFalse(UUID id);

    @Query("""
            SELECT s FROM Supplier s
            WHERE s.isDeleted = false
              AND s.company.id = :companyId
              AND (:search IS NULL OR
                   LOWER(s.name)  LIKE LOWER(CONCAT('%', :search, '%')) OR
                   LOWER(s.code)  LIKE LOWER(CONCAT('%', :search, '%')) OR
                   LOWER(s.email) LIKE LOWER(CONCAT('%', :search, '%')))
              AND (:status IS NULL OR s.status = :status)
            """)
    Page<Supplier> searchByCompany(
            @Param("companyId") UUID companyId,
            @Param("search") String search,
            @Param("status") SupplierStatus status,
            Pageable pageable
    );

    @Query("""
            SELECT COUNT(s) FROM Supplier s
            WHERE s.isDeleted = false
              AND s.company.id = :companyId
              AND s.status = 'ACTIVE'
            """)
    long countActiveByCompany(@Param("companyId") UUID companyId);
}

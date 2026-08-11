package com.procure.module.purchase.repository;

import com.procure.module.purchase.entity.PurchaseOrder;
import com.procure.module.purchase.entity.PurchaseOrder.POStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, UUID> {

    Optional<PurchaseOrder> findByIdAndIsDeletedFalse(UUID id);

    @Query("""
            SELECT po FROM PurchaseOrder po
            JOIN FETCH po.supplier s
            JOIN FETCH po.branch b
            WHERE po.isDeleted = false
              AND b.company.id = :companyId
              AND (:supplierId IS NULL OR s.id = :supplierId)
              AND (:status IS NULL OR po.status = :status)
              AND (:search IS NULL OR
                   LOWER(po.poNumber) LIKE LOWER(CONCAT('%', :search, '%')) OR
                   LOWER(s.name)      LIKE LOWER(CONCAT('%', :search, '%')))
            ORDER BY po.createdAt DESC
            """)
    Page<PurchaseOrder> searchPOs(
            @Param("companyId")  UUID companyId,
            @Param("supplierId") UUID supplierId,
            @Param("status")     POStatus status,
            @Param("search")     String search,
            Pageable pageable
    );

    List<PurchaseOrder> findByBranchCompanyIdAndStatusIn(UUID companyId, List<POStatus> statuses);

    @Query("SELECT COUNT(po) FROM PurchaseOrder po WHERE po.poNumber LIKE CONCAT('PO-', :year, '-%')")
    long countByYear(@Param("year") int year);
}

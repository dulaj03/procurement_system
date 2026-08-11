package com.procure.module.purchase.repository;

import com.procure.module.purchase.entity.PurchaseRequest;
import com.procure.module.purchase.entity.PurchaseRequest.PRStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PurchaseRequestRepository extends JpaRepository<PurchaseRequest, UUID> {

    Optional<PurchaseRequest> findByIdAndIsDeletedFalse(UUID id);

    @Query("""
            SELECT pr FROM PurchaseRequest pr
            JOIN FETCH pr.branch b
            JOIN FETCH pr.requestedBy u
            WHERE pr.isDeleted = false
              AND b.company.id = :companyId
              AND (:branchId IS NULL OR b.id = :branchId)
              AND (:status IS NULL OR pr.status = :status)
              AND (:search IS NULL OR
                   LOWER(pr.prNumber) LIKE LOWER(CONCAT('%', :search, '%')) OR
                   LOWER(pr.title)    LIKE LOWER(CONCAT('%', :search, '%')))
            ORDER BY pr.createdAt DESC
            """)
    Page<PurchaseRequest> searchPRs(
            @Param("companyId") UUID companyId,
            @Param("branchId")  UUID branchId,
            @Param("status")    PRStatus status,
            @Param("search")    String search,
            Pageable pageable
    );

    @Query("SELECT COUNT(pr) FROM PurchaseRequest pr WHERE pr.prNumber LIKE CONCAT('PR-', :year, '-%')")
    long countByYear(@Param("year") int year);
}

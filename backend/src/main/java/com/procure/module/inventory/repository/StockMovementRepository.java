package com.procure.module.inventory.repository;

import com.procure.module.inventory.entity.StockMovement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface StockMovementRepository extends JpaRepository<StockMovement, UUID> {

    @Query("""
            SELECT sm FROM StockMovement sm
            JOIN FETCH sm.product
            WHERE sm.isDeleted = false
              AND (:productId IS NULL OR sm.product.id = :productId)
              AND (:branchId IS NULL OR
                   sm.fromBranch.id = :branchId OR sm.toBranch.id = :branchId)
            ORDER BY sm.createdAt DESC
            """)
    Page<StockMovement> findMovements(
            @Param("productId") UUID productId,
            @Param("branchId")  UUID branchId,
            Pageable pageable
    );
}

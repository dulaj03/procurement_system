package com.procure.module.inventory.repository;

import com.procure.module.inventory.entity.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, UUID> {

    Optional<Inventory> findByProductIdAndBranchId(UUID productId, UUID branchId);

    @Query("""
            SELECT i FROM Inventory i
            JOIN FETCH i.product p
            JOIN FETCH i.branch b
            WHERE i.isDeleted = false
              AND b.company.id = :companyId
              AND (:branchId IS NULL OR b.id = :branchId)
              AND (:lowStockOnly = false OR
                   (p.reorderLevel IS NOT NULL AND i.quantityOnHand <= p.reorderLevel))
            ORDER BY p.name ASC
            """)
    List<Inventory> findByCompany(
            @Param("companyId")    UUID companyId,
            @Param("branchId")     UUID branchId,
            @Param("lowStockOnly") boolean lowStockOnly
    );

    @Query("""
            SELECT i FROM Inventory i
            JOIN FETCH i.product p
            JOIN FETCH i.branch b
            WHERE i.isDeleted = false
              AND b.company.id = :companyId
              AND p.reorderLevel IS NOT NULL
              AND i.quantityOnHand <= p.reorderLevel
            """)
    List<Inventory> findLowStockByCompany(@Param("companyId") UUID companyId);
}

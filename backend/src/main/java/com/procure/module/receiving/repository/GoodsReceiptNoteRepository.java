package com.procure.module.receiving.repository;

import com.procure.module.receiving.entity.GoodsReceiptNote;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface GoodsReceiptNoteRepository extends JpaRepository<GoodsReceiptNote, UUID> {

    Optional<GoodsReceiptNote> findByIdAndIsDeletedFalse(UUID id);

    @Query("""
            SELECT grn FROM GoodsReceiptNote grn
            JOIN FETCH grn.purchaseOrder po
            JOIN FETCH grn.branch b
            WHERE grn.isDeleted = false
              AND b.company.id = :companyId
              AND (:search IS NULL OR
                   LOWER(grn.grnNumber) LIKE LOWER(CONCAT('%', :search, '%')) OR
                   LOWER(po.poNumber)   LIKE LOWER(CONCAT('%', :search, '%')))
            ORDER BY grn.createdAt DESC
            """)
    Page<GoodsReceiptNote> searchGRNs(
            @Param("companyId") UUID companyId,
            @Param("search")    String search,
            Pageable pageable
    );

    @Query("SELECT COUNT(g) FROM GoodsReceiptNote g WHERE g.grnNumber LIKE CONCAT('GRN-', :year, '-%')")
    long countByYear(@Param("year") int year);
}

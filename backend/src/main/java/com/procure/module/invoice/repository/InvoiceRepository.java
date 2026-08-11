package com.procure.module.invoice.repository;

import com.procure.module.invoice.entity.Invoice;
import com.procure.module.invoice.entity.Invoice.InvoiceStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, UUID> {

    Optional<Invoice> findByIdAndIsDeletedFalse(UUID id);

    @Query("""
            SELECT inv FROM Invoice inv
            JOIN FETCH inv.supplier s
            WHERE inv.isDeleted = false
              AND s.company.id = :companyId
              AND (:supplierId IS NULL OR s.id = :supplierId)
              AND (:status IS NULL OR inv.status = :status)
              AND (:search IS NULL OR
                   LOWER(inv.invoiceNumber) LIKE LOWER(CONCAT('%', :search, '%')) OR
                   LOWER(s.name)            LIKE LOWER(CONCAT('%', :search, '%')))
            ORDER BY inv.createdAt DESC
            """)
    Page<Invoice> searchInvoices(
            @Param("companyId")  UUID companyId,
            @Param("supplierId") UUID supplierId,
            @Param("status")     InvoiceStatus status,
            @Param("search")     String search,
            Pageable pageable
    );

    @Query("SELECT COUNT(inv) FROM Invoice inv WHERE inv.invoiceNumber LIKE CONCAT('INV-', :year, '-%')")
    long countByYear(@Param("year") int year);
}

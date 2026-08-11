package com.procure.module.supplier.repository;

import com.procure.module.supplier.entity.SupplierContact;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SupplierContactRepository extends JpaRepository<SupplierContact, UUID> {

    List<SupplierContact> findBySupplierIdAndIsDeletedFalse(UUID supplierId);

    @Modifying
    @Query("UPDATE SupplierContact c SET c.primary = false WHERE c.supplier.id = :supplierId AND c.isDeleted = false")
    void clearPrimaryForSupplier(@Param("supplierId") UUID supplierId);
}

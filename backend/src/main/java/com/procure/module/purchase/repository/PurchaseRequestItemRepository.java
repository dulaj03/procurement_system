package com.procure.module.purchase.repository;

import com.procure.module.purchase.entity.PurchaseRequestItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PurchaseRequestItemRepository extends JpaRepository<PurchaseRequestItem, UUID> {
    List<PurchaseRequestItem> findByPurchaseRequestIdAndIsDeletedFalse(UUID purchaseRequestId);
}

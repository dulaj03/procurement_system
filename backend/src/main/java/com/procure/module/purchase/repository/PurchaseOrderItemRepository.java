package com.procure.module.purchase.repository;

import com.procure.module.purchase.entity.PurchaseOrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PurchaseOrderItemRepository extends JpaRepository<PurchaseOrderItem, UUID> {
    List<PurchaseOrderItem> findByPurchaseOrderIdAndIsDeletedFalse(UUID purchaseOrderId);
}

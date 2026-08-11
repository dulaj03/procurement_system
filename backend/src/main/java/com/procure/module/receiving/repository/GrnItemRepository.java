package com.procure.module.receiving.repository;

import com.procure.module.receiving.entity.GRNItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface GrnItemRepository extends JpaRepository<GRNItem, UUID> {
    List<GRNItem> findByGrnIdAndIsDeletedFalse(UUID grnId);
}

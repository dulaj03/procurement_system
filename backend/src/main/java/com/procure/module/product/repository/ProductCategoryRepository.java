package com.procure.module.product.repository;

import com.procure.module.product.entity.ProductCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProductCategoryRepository extends JpaRepository<ProductCategory, UUID> {
    List<ProductCategory> findByIsDeletedFalseOrderByNameAsc();
    boolean existsByCodeAndIsDeletedFalse(String code);
    boolean existsByCodeAndIsDeletedFalseAndIdNot(String code, UUID id);
    Optional<ProductCategory> findByIdAndIsDeletedFalse(UUID id);
}

package com.procure.module.product.repository;

import com.procure.module.product.entity.Product;
import com.procure.module.product.entity.Product.ProductStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProductRepository extends JpaRepository<Product, UUID> {

    boolean existsBySkuAndIsDeletedFalse(String sku);
    boolean existsBySkuAndIsDeletedFalseAndIdNot(String sku, UUID id);
    Optional<Product> findByIdAndIsDeletedFalse(UUID id);

    @Query("""
            SELECT p FROM Product p
            LEFT JOIN FETCH p.category
            WHERE p.isDeleted = false
              AND p.company.id = :companyId
              AND (:search IS NULL OR
                   LOWER(p.name) LIKE LOWER(CONCAT('%', :search, '%')) OR
                   LOWER(p.sku)  LIKE LOWER(CONCAT('%', :search, '%')))
              AND (:status IS NULL OR p.status = :status)
              AND (:categoryId IS NULL OR p.category.id = :categoryId)
            """)
    Page<Product> search(
            @Param("companyId")  UUID companyId,
            @Param("search")     String search,
            @Param("status")     ProductStatus status,
            @Param("categoryId") UUID categoryId,
            Pageable pageable
    );
}

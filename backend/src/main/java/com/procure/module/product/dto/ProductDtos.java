package com.procure.module.product.dto;

import com.procure.module.product.entity.Product.ProductStatus;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class ProductDtos {

    // ── Category DTOs ─────────────────────────────────────────────

    public record CategoryRequest(
            @NotBlank(message = "Category name is required") @Size(max = 255) String name,
            @NotBlank(message = "Category code is required") @Size(max = 50)  String code,
            String description,
            UUID parentId
    ) {}

    public record CategoryResponse(
            UUID id,
            String name,
            String code,
            String description,
            UUID parentId,
            String parentName
    ) {}

    // ── Product Request ───────────────────────────────────────────

    public record ProductRequest(
            @NotBlank(message = "Product name is required") @Size(max = 255) String name,
            @NotBlank(message = "SKU is required") @Size(max = 100)          String sku,
            @Size(max = 100) String barcode,
            String description,
            UUID categoryId,
            @NotBlank(message = "Unit of measure is required") String unitOfMeasure,
            @DecimalMin("0.0") BigDecimal unitPrice,
            @Min(0) Integer reorderLevel,
            @Min(0) Integer reorderQuantity,
            String imageUrl,
            ProductStatus status,
            @NotNull(message = "Company is required") UUID companyId
    ) {}

    // ── Product Response ──────────────────────────────────────────

    public record ProductResponse(
            UUID id,
            String name,
            String sku,
            String barcode,
            String description,
            UUID categoryId,
            String categoryName,
            String unitOfMeasure,
            BigDecimal unitPrice,
            Integer reorderLevel,
            Integer reorderQuantity,
            String imageUrl,
            ProductStatus status,
            UUID companyId,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {}

    public record ProductSummary(
            UUID id,
            String name,
            String sku,
            String categoryName,
            String unitOfMeasure,
            BigDecimal unitPrice,
            Integer reorderLevel,
            ProductStatus status,
            String imageUrl
    ) {}
}

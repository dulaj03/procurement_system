package com.procure.module.product.service;

import com.procure.common.exception.ResourceNotFoundException;
import com.procure.module.company.repository.CompanyRepository;
import com.procure.module.product.dto.ProductDtos.*;
import com.procure.module.product.entity.Product;
import com.procure.module.product.entity.Product.ProductStatus;
import com.procure.module.product.entity.ProductCategory;
import com.procure.module.product.repository.ProductCategoryRepository;
import com.procure.module.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository         productRepository;
    private final ProductCategoryRepository categoryRepository;
    private final CompanyRepository         companyRepository;

    // ── CATEGORIES ────────────────────────────────────────────────

    @Transactional
    public CategoryResponse createCategory(CategoryRequest request) {
        if (categoryRepository.existsByCodeAndIsDeletedFalse(request.code()))
            throw new IllegalArgumentException("Category code '" + request.code() + "' already exists");

        ProductCategory parent = null;
        if (request.parentId() != null)
            parent = categoryRepository.findByIdAndIsDeletedFalse(request.parentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category", request.parentId()));

        ProductCategory cat = ProductCategory.builder()
                .name(request.name())
                .code(request.code().toUpperCase())
                .description(request.description())
                .parent(parent)
                .build();

        cat = categoryRepository.save(cat);
        return toCategoryResponse(cat);
    }

    @Transactional(readOnly = true)
    public List<CategoryResponse> getAllCategories() {
        return categoryRepository.findByIsDeletedFalseOrderByNameAsc()
                .stream().map(this::toCategoryResponse).toList();
    }

    @Transactional
    public void deleteCategory(UUID id) {
        ProductCategory cat = categoryRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", id));
        cat.setDeleted(true);
        categoryRepository.save(cat);
    }

    // ── PRODUCTS ──────────────────────────────────────────────────

    @Transactional
    public ProductResponse createProduct(ProductRequest request) {
        if (productRepository.existsBySkuAndIsDeletedFalse(request.sku()))
            throw new IllegalArgumentException("SKU '" + request.sku() + "' already exists");

        var company = companyRepository.findByIdAndIsDeletedFalse(request.companyId())
                .orElseThrow(() -> new ResourceNotFoundException("Company", request.companyId()));

        ProductCategory category = null;
        if (request.categoryId() != null)
            category = categoryRepository.findByIdAndIsDeletedFalse(request.categoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category", request.categoryId()));

        Product product = Product.builder()
                .name(request.name())
                .sku(request.sku().toUpperCase())
                .barcode(request.barcode())
                .description(request.description())
                .category(category)
                .unitOfMeasure(request.unitOfMeasure())
                .unitPrice(request.unitPrice())
                .reorderLevel(request.reorderLevel())
                .reorderQuantity(request.reorderQuantity())
                .imageUrl(request.imageUrl())
                .status(request.status() != null ? request.status() : ProductStatus.ACTIVE)
                .company(company)
                .build();

        product = productRepository.save(product);
        log.info("Product created: {} [{}]", product.getName(), product.getId());
        return toResponse(product);
    }

    @Transactional
    public ProductResponse updateProduct(UUID id, ProductRequest request) {
        Product product = productRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", id));

        if (!product.getSku().equalsIgnoreCase(request.sku()) &&
                productRepository.existsBySkuAndIsDeletedFalseAndIdNot(request.sku(), id))
            throw new IllegalArgumentException("SKU '" + request.sku() + "' already in use");

        ProductCategory category = null;
        if (request.categoryId() != null)
            category = categoryRepository.findByIdAndIsDeletedFalse(request.categoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category", request.categoryId()));

        product.setName(request.name());
        product.setSku(request.sku().toUpperCase());
        product.setBarcode(request.barcode());
        product.setDescription(request.description());
        product.setCategory(category);
        product.setUnitOfMeasure(request.unitOfMeasure());
        product.setUnitPrice(request.unitPrice());
        product.setReorderLevel(request.reorderLevel());
        product.setReorderQuantity(request.reorderQuantity());
        product.setImageUrl(request.imageUrl());
        if (request.status() != null) product.setStatus(request.status());

        productRepository.save(product);
        return toResponse(product);
    }

    @Transactional(readOnly = true)
    public ProductResponse getById(UUID id) {
        return toResponse(productRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", id)));
    }

    @Transactional(readOnly = true)
    public Page<ProductSummary> search(UUID companyId, String search,
                                       ProductStatus status, UUID categoryId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("name").ascending());
        return productRepository.search(companyId, search, status, categoryId, pageable)
                .map(this::toSummary);
    }

    @Transactional
    public void deleteProduct(UUID id) {
        Product p = productRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", id));
        p.setDeleted(true);
        productRepository.save(p);
    }

    // ── MAPPERS ───────────────────────────────────────────────────

    private ProductResponse toResponse(Product p) {
        return new ProductResponse(
                p.getId(), p.getName(), p.getSku(), p.getBarcode(), p.getDescription(),
                p.getCategory() != null ? p.getCategory().getId() : null,
                p.getCategory() != null ? p.getCategory().getName() : null,
                p.getUnitOfMeasure(), p.getUnitPrice(),
                p.getReorderLevel(), p.getReorderQuantity(),
                p.getImageUrl(), p.getStatus(),
                p.getCompany() != null ? p.getCompany().getId() : null,
                p.getCreatedAt(), p.getUpdatedAt()
        );
    }

    private ProductSummary toSummary(Product p) {
        return new ProductSummary(
                p.getId(), p.getName(), p.getSku(),
                p.getCategory() != null ? p.getCategory().getName() : null,
                p.getUnitOfMeasure(), p.getUnitPrice(),
                p.getReorderLevel(), p.getStatus(), p.getImageUrl()
        );
    }

    private CategoryResponse toCategoryResponse(ProductCategory c) {
        return new CategoryResponse(
                c.getId(), c.getName(), c.getCode(), c.getDescription(),
                c.getParent() != null ? c.getParent().getId() : null,
                c.getParent() != null ? c.getParent().getName() : null
        );
    }
}

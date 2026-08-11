package com.procure.module.product.controller;

import com.procure.common.response.ApiResponse;
import com.procure.module.product.dto.ProductDtos.*;
import com.procure.module.product.entity.Product.ProductStatus;
import com.procure.module.product.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Tag(name = "Products", description = "Product catalog and category management APIs")
public class ProductController {

    private final ProductService productService;

    // ── Categories ────────────────────────────────────────────────

    @PostMapping("/categories")
    @PreAuthorize("hasAuthority('PRODUCT:WRITE')")
    @Operation(summary = "Create a product category")
    public ResponseEntity<ApiResponse<CategoryResponse>> createCategory(
            @Valid @RequestBody CategoryRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Category created", productService.createCategory(request)));
    }

    @GetMapping("/categories")
    @PreAuthorize("hasAuthority('PRODUCT:READ')")
    @Operation(summary = "List all product categories")
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> listCategories() {
        return ResponseEntity.ok(ApiResponse.success(productService.getAllCategories()));
    }

    @DeleteMapping("/categories/{id}")
    @PreAuthorize("hasAuthority('PRODUCT:WRITE')")
    @Operation(summary = "Delete a product category")
    public ResponseEntity<ApiResponse<Void>> deleteCategory(@PathVariable UUID id) {
        productService.deleteCategory(id);
        return ResponseEntity.ok(ApiResponse.success("Category deleted", null));
    }

    // ── Products ──────────────────────────────────────────────────

    @PostMapping("/products")
    @PreAuthorize("hasAuthority('PRODUCT:WRITE')")
    @Operation(summary = "Create a new product")
    public ResponseEntity<ApiResponse<ProductResponse>> createProduct(
            @Valid @RequestBody ProductRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Product created", productService.createProduct(request)));
    }

    @PutMapping("/products/{id}")
    @PreAuthorize("hasAuthority('PRODUCT:WRITE')")
    @Operation(summary = "Update product details")
    public ResponseEntity<ApiResponse<ProductResponse>> updateProduct(
            @PathVariable UUID id, @Valid @RequestBody ProductRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Product updated", productService.updateProduct(id, request)));
    }

    @GetMapping("/products/{id}")
    @PreAuthorize("hasAuthority('PRODUCT:READ')")
    @Operation(summary = "Get product by ID")
    public ResponseEntity<ApiResponse<ProductResponse>> getProduct(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(productService.getById(id)));
    }

    @GetMapping("/products")
    @PreAuthorize("hasAuthority('PRODUCT:READ')")
    @Operation(summary = "Search products with filters and pagination")
    public ResponseEntity<ApiResponse<Page<ProductSummary>>> listProducts(
            @RequestParam UUID companyId,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) ProductStatus status,
            @RequestParam(required = false) UUID categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(
                productService.search(companyId, search, status, categoryId, page, size)));
    }

    @DeleteMapping("/products/{id}")
    @PreAuthorize("hasAuthority('PRODUCT:WRITE')")
    @Operation(summary = "Soft delete a product")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(@PathVariable UUID id) {
        productService.deleteProduct(id);
        return ResponseEntity.ok(ApiResponse.success("Product deleted", null));
    }
}

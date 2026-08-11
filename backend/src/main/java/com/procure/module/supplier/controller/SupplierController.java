package com.procure.module.supplier.controller;

import com.procure.common.response.ApiResponse;
import com.procure.module.supplier.dto.SupplierDtos.*;
import com.procure.module.supplier.entity.Supplier.SupplierStatus;
import com.procure.module.supplier.service.SupplierService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/suppliers")
@RequiredArgsConstructor
@Tag(name = "Suppliers", description = "Supplier profile management and ratings APIs")
public class SupplierController {

    private final SupplierService supplierService;

    // ── CRUD ──────────────────────────────────────────────────────

    @PostMapping
    @PreAuthorize("hasAuthority('SUPPLIER:WRITE')")
    @Operation(summary = "Register a new supplier")
    public ResponseEntity<ApiResponse<SupplierResponse>> createSupplier(
            @Valid @RequestBody SupplierRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Supplier registered successfully",
                        supplierService.createSupplier(request)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('SUPPLIER:WRITE')")
    @Operation(summary = "Update supplier profile")
    public ResponseEntity<ApiResponse<SupplierResponse>> updateSupplier(
            @PathVariable UUID id,
            @Valid @RequestBody SupplierRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Supplier updated successfully",
                supplierService.updateSupplier(id, request)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('SUPPLIER:READ')")
    @Operation(summary = "Get supplier details with contacts")
    public ResponseEntity<ApiResponse<SupplierResponse>> getSupplier(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(supplierService.getSupplierById(id)));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('SUPPLIER:READ')")
    @Operation(summary = "Search and list suppliers with pagination")
    public ResponseEntity<ApiResponse<Page<SupplierSummary>>> listSuppliers(
            @RequestParam UUID companyId,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) SupplierStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(
                supplierService.searchSuppliers(companyId, search, status, page, size)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('SUPPLIER:WRITE')")
    @Operation(summary = "Soft delete a supplier")
    public ResponseEntity<ApiResponse<Void>> deleteSupplier(@PathVariable UUID id) {
        supplierService.deleteSupplier(id);
        return ResponseEntity.ok(ApiResponse.success("Supplier deleted", null));
    }

    // ── RATING ────────────────────────────────────────────────────

    @PatchMapping("/{id}/rate")
    @PreAuthorize("hasAuthority('SUPPLIER:WRITE')")
    @Operation(summary = "Update supplier star rating (1–5)")
    public ResponseEntity<ApiResponse<SupplierResponse>> rateSupplier(
            @PathVariable UUID id,
            @Valid @RequestBody RatingRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Rating updated",
                supplierService.rateSupplier(id, request)));
    }

    // ── CONTACTS ──────────────────────────────────────────────────

    @PostMapping("/{supplierId}/contacts")
    @PreAuthorize("hasAuthority('SUPPLIER:WRITE')")
    @Operation(summary = "Add a contact person to a supplier")
    public ResponseEntity<ApiResponse<SupplierResponse>> addContact(
            @PathVariable UUID supplierId,
            @Valid @RequestBody ContactRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Contact added",
                        supplierService.addContact(supplierId, request)));
    }

    @DeleteMapping("/{supplierId}/contacts/{contactId}")
    @PreAuthorize("hasAuthority('SUPPLIER:WRITE')")
    @Operation(summary = "Remove a contact from a supplier")
    public ResponseEntity<ApiResponse<Void>> deleteContact(
            @PathVariable UUID supplierId,
            @PathVariable UUID contactId) {
        supplierService.deleteContact(supplierId, contactId);
        return ResponseEntity.ok(ApiResponse.success("Contact removed", null));
    }
}

package com.procure.module.purchase.controller;

import com.procure.common.response.ApiResponse;
import com.procure.module.purchase.dto.PurchaseOrderDtos.*;
import com.procure.module.purchase.entity.PurchaseOrder.POStatus;
import com.procure.module.purchase.service.PurchaseOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/purchase-orders")
@RequiredArgsConstructor
@Tag(name = "Purchase Orders", description = "Purchase order issuance and tracking APIs")
public class PurchaseOrderController {

    private final PurchaseOrderService poService;

    @PostMapping
    @PreAuthorize("hasAuthority('PO:WRITE')")
    @Operation(summary = "Create a purchase order (standalone or from approved PR)")
    public ResponseEntity<ApiResponse<POResponse>> createPO(
            @Valid @RequestBody POCreateRequest request, Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Purchase order created",
                        poService.createPO(request, authentication.getName())));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAuthority('PO:WRITE')")
    @Operation(summary = "Update purchase order status")
    public ResponseEntity<ApiResponse<POResponse>> updateStatus(
            @PathVariable UUID id, @RequestParam POStatus status) {
        return ResponseEntity.ok(ApiResponse.success("PO status updated", poService.updateStatus(id, status)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('PO:READ')")
    @Operation(summary = "Get purchase order by ID")
    public ResponseEntity<ApiResponse<POResponse>> getPO(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(poService.getById(id)));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('PO:READ')")
    @Operation(summary = "Search purchase orders with pagination")
    public ResponseEntity<ApiResponse<Page<POSummary>>> listPOs(
            @RequestParam UUID companyId,
            @RequestParam(required = false) UUID supplierId,
            @RequestParam(required = false) POStatus status,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(
                poService.searchPOs(companyId, supplierId, status, search, page, size)));
    }
}

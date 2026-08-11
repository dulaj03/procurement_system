package com.procure.module.inventory.controller;

import com.procure.common.response.ApiResponse;
import com.procure.module.inventory.dto.InventoryDtos.*;
import com.procure.module.inventory.service.InventoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/inventory")
@RequiredArgsConstructor
@Tag(name = "Inventory", description = "Stock levels, adjustments, transfers and movement ledger APIs")
public class InventoryController {

    private final InventoryService inventoryService;

    @GetMapping
    @PreAuthorize("hasAuthority('INVENTORY:READ')")
    @Operation(summary = "List inventory levels by company/branch")
    public ResponseEntity<ApiResponse<List<InventoryResponse>>> getInventory(
            @RequestParam UUID companyId,
            @RequestParam(required = false) UUID branchId,
            @RequestParam(defaultValue = "false") boolean lowStockOnly) {
        return ResponseEntity.ok(ApiResponse.success(
                inventoryService.getInventory(companyId, branchId, lowStockOnly)));
    }

    @GetMapping("/low-stock")
    @PreAuthorize("hasAuthority('INVENTORY:READ')")
    @Operation(summary = "Get all low-stock alerts for a company")
    public ResponseEntity<ApiResponse<List<LowStockAlert>>> getLowStockAlerts(
            @RequestParam UUID companyId) {
        return ResponseEntity.ok(ApiResponse.success(
                inventoryService.getLowStockAlerts(companyId)));
    }

    @PostMapping("/adjust")
    @PreAuthorize("hasAuthority('INVENTORY:WRITE')")
    @Operation(summary = "Manual stock adjustment (positive or negative quantity)")
    public ResponseEntity<ApiResponse<InventoryResponse>> adjustStock(
            @Valid @RequestBody StockAdjustRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Stock adjusted",
                inventoryService.adjustStock(request)));
    }

    @PostMapping("/transfer")
    @PreAuthorize("hasAuthority('INVENTORY:WRITE')")
    @Operation(summary = "Transfer stock between branches")
    public ResponseEntity<ApiResponse<Void>> transferStock(
            @Valid @RequestBody StockTransferRequest request) {
        inventoryService.transferStock(request);
        return ResponseEntity.ok(ApiResponse.success("Stock transferred", null));
    }

    @GetMapping("/movements")
    @PreAuthorize("hasAuthority('INVENTORY:READ')")
    @Operation(summary = "Get stock movement ledger with pagination")
    public ResponseEntity<ApiResponse<Page<StockMovementResponse>>> getMovements(
            @RequestParam(required = false) UUID productId,
            @RequestParam(required = false) UUID branchId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        return ResponseEntity.ok(ApiResponse.success(
                inventoryService.getMovements(productId, branchId, page, size)));
    }
}

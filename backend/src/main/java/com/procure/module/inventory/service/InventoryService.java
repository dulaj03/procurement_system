package com.procure.module.inventory.service;

import com.procure.common.exception.ResourceNotFoundException;
import com.procure.module.company.repository.BranchRepository;
import com.procure.module.inventory.dto.InventoryDtos.*;
import com.procure.module.inventory.entity.Inventory;
import com.procure.module.inventory.entity.StockMovement;
import com.procure.module.inventory.entity.StockMovement.MovementType;
import com.procure.module.inventory.repository.InventoryRepository;
import com.procure.module.inventory.repository.StockMovementRepository;
import com.procure.module.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class InventoryService {

    private final InventoryRepository     inventoryRepository;
    private final StockMovementRepository movementRepository;
    private final ProductRepository       productRepository;
    private final BranchRepository        branchRepository;

    // ── READ ──────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<InventoryResponse> getInventory(UUID companyId, UUID branchId, boolean lowStockOnly) {
        return inventoryRepository.findByCompany(companyId, branchId, lowStockOnly)
                .stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<LowStockAlert> getLowStockAlerts(UUID companyId) {
        return inventoryRepository.findLowStockByCompany(companyId)
                .stream().map(i -> new LowStockAlert(
                        i.getProduct().getId(), i.getProduct().getName(), i.getProduct().getSku(),
                        i.getBranch().getId(), i.getBranch().getName(),
                        i.getQuantityOnHand(), i.getProduct().getReorderLevel()
                )).toList();
    }

    @Transactional(readOnly = true)
    public Page<StockMovementResponse> getMovements(UUID productId, UUID branchId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return movementRepository.findMovements(productId, branchId, pageable)
                .map(this::toMovementResponse);
    }

    // ── STOCK ADJUST ──────────────────────────────────────────────

    @Transactional
    public InventoryResponse adjustStock(StockAdjustRequest request) {
        var product = productRepository.findByIdAndIsDeletedFalse(request.productId())
                .orElseThrow(() -> new ResourceNotFoundException("Product", request.productId()));
        var branch = branchRepository.findById(request.branchId())
                .orElseThrow(() -> new ResourceNotFoundException("Branch", request.branchId()));

        Inventory inv = inventoryRepository
                .findByProductIdAndBranchId(request.productId(), request.branchId())
                .orElseGet(() -> Inventory.builder().product(product).branch(branch).build());

        boolean isPositive = request.quantity().compareTo(BigDecimal.ZERO) > 0;
        inv.setQuantityOnHand(inv.getQuantityOnHand().add(request.quantity()));
        inventoryRepository.save(inv);

        // Record movement
        StockMovement movement = StockMovement.builder()
                .product(product)
                .toBranch(isPositive ? branch : null)
                .fromBranch(isPositive ? null : branch)
                .movementType(isPositive ? MovementType.ADJUSTMENT_IN : MovementType.ADJUSTMENT_OUT)
                .quantity(request.quantity().abs())
                .referenceNumber(request.referenceNumber())
                .referenceType("ADJUSTMENT")
                .notes(request.reason())
                .build();
        movementRepository.save(movement);

        log.info("Stock adjusted: product={} branch={} qty={}", product.getSku(), branch.getName(), request.quantity());
        return toResponse(inv);
    }

    // ── STOCK TRANSFER ────────────────────────────────────────────

    @Transactional
    public void transferStock(StockTransferRequest request) {
        var product = productRepository.findByIdAndIsDeletedFalse(request.productId())
                .orElseThrow(() -> new ResourceNotFoundException("Product", request.productId()));
        var fromBranch = branchRepository.findById(request.fromBranchId())
                .orElseThrow(() -> new ResourceNotFoundException("Branch", request.fromBranchId()));
        var toBranch = branchRepository.findById(request.toBranchId())
                .orElseThrow(() -> new ResourceNotFoundException("Branch", request.toBranchId()));

        // Deduct from source
        Inventory fromInv = inventoryRepository
                .findByProductIdAndBranchId(request.productId(), request.fromBranchId())
                .orElseThrow(() -> new IllegalArgumentException("No inventory record found at source branch"));

        if (fromInv.getQuantityOnHand().compareTo(request.quantity()) < 0)
            throw new IllegalArgumentException("Insufficient stock: available="
                    + fromInv.getQuantityOnHand() + ", requested=" + request.quantity());

        fromInv.setQuantityOnHand(fromInv.getQuantityOnHand().subtract(request.quantity()));
        inventoryRepository.save(fromInv);

        // Add to destination
        Inventory toInv = inventoryRepository
                .findByProductIdAndBranchId(request.productId(), request.toBranchId())
                .orElseGet(() -> Inventory.builder().product(product).branch(toBranch).build());
        toInv.setQuantityOnHand(toInv.getQuantityOnHand().add(request.quantity()));
        inventoryRepository.save(toInv);

        // Record movement ledger
        String ref = "TR-" + System.currentTimeMillis();
        movementRepository.save(StockMovement.builder()
                .product(product).fromBranch(fromBranch)
                .movementType(MovementType.TRANSFER_OUT)
                .quantity(request.quantity()).referenceNumber(ref)
                .referenceType("TRANSFER").notes(request.notes()).build());

        movementRepository.save(StockMovement.builder()
                .product(product).toBranch(toBranch)
                .movementType(MovementType.TRANSFER_IN)
                .quantity(request.quantity()).referenceNumber(ref)
                .referenceType("TRANSFER").notes(request.notes()).build());

        log.info("Transfer: {} qty={} {} → {}", product.getSku(), request.quantity(),
                fromBranch.getName(), toBranch.getName());
    }

    // ── INTERNAL: called by GrnService on receipt ─────────────────

    @Transactional
    public void receiveStock(UUID productId, UUID branchId, BigDecimal quantity, BigDecimal unitCost,
                             String referenceNumber) {
        var product = productRepository.findByIdAndIsDeletedFalse(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", productId));
        var branch = branchRepository.findById(branchId)
                .orElseThrow(() -> new ResourceNotFoundException("Branch", branchId));

        Inventory inv = inventoryRepository
                .findByProductIdAndBranchId(productId, branchId)
                .orElseGet(() -> Inventory.builder().product(product).branch(branch).build());

        // Weighted average cost update
        if (unitCost != null && unitCost.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal totalCost = inv.getQuantityOnHand()
                    .multiply(inv.getAverageCost() != null ? inv.getAverageCost() : BigDecimal.ZERO)
                    .add(quantity.multiply(unitCost));
            BigDecimal newTotal = inv.getQuantityOnHand().add(quantity);
            inv.setAverageCost(newTotal.compareTo(BigDecimal.ZERO) > 0
                    ? totalCost.divide(newTotal, 4, java.math.RoundingMode.HALF_UP) : BigDecimal.ZERO);
        }

        inv.setQuantityOnHand(inv.getQuantityOnHand().add(quantity));
        inventoryRepository.save(inv);

        movementRepository.save(StockMovement.builder()
                .product(product).toBranch(branch)
                .movementType(MovementType.RECEIPT)
                .quantity(quantity).unitCost(unitCost)
                .referenceNumber(referenceNumber).referenceType("GRN").build());
    }

    // ── MAPPERS ───────────────────────────────────────────────────

    private InventoryResponse toResponse(Inventory i) {
        return new InventoryResponse(
                i.getId(),
                i.getProduct().getId(), i.getProduct().getName(), i.getProduct().getSku(),
                i.getProduct().getUnitOfMeasure(),
                i.getBranch().getId(), i.getBranch().getName(),
                i.getQuantityOnHand(), i.getQuantityReserved(), i.getQuantityOnOrder(),
                i.getAvailableQuantity(), i.getAverageCost(),
                i.getProduct().getReorderLevel(), i.isLowStock(),
                i.getUpdatedAt()
        );
    }

    private StockMovementResponse toMovementResponse(StockMovement sm) {
        return new StockMovementResponse(
                sm.getId(), sm.getProduct().getName(), sm.getProduct().getSku(),
                sm.getMovementType().name(), sm.getQuantity(),
                sm.getFromBranch() != null ? sm.getFromBranch().getName() : null,
                sm.getToBranch()   != null ? sm.getToBranch().getName()   : null,
                sm.getReferenceNumber(), sm.getNotes(), sm.getCreatedAt(), sm.getCreatedBy()
        );
    }
}

package com.procure.module.receiving.service;

import com.procure.common.exception.ResourceNotFoundException;
import com.procure.module.company.repository.BranchRepository;
import com.procure.module.inventory.service.InventoryService;
import com.procure.module.product.repository.ProductRepository;
import com.procure.module.purchase.entity.PurchaseOrder;
import com.procure.module.purchase.entity.PurchaseOrder.POStatus;
import com.procure.module.purchase.repository.PurchaseOrderItemRepository;
import com.procure.module.purchase.repository.PurchaseOrderRepository;
import com.procure.module.receiving.dto.GrnDtos.*;
import com.procure.module.receiving.entity.GRNItem;
import com.procure.module.receiving.entity.GoodsReceiptNote;
import com.procure.module.receiving.entity.GoodsReceiptNote.GRNStatus;
import com.procure.module.receiving.repository.GoodsReceiptNoteRepository;
import com.procure.module.receiving.repository.GrnItemRepository;
import com.procure.module.user.entity.User;
import com.procure.module.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class GrnService {

    private final GoodsReceiptNoteRepository  grnRepository;
    private final GrnItemRepository           grnItemRepository;
    private final PurchaseOrderRepository     poRepository;
    private final PurchaseOrderItemRepository poItemRepository;
    private final BranchRepository            branchRepository;
    private final ProductRepository           productRepository;
    private final UserRepository              userRepository;
    private final InventoryService            inventoryService;

    @Transactional
    public GRNResponse createAndPostGRN(GRNCreateRequest request, String currentUsername) {
        User receiver = userRepository.findByEmail(currentUsername)
                .orElseThrow(() -> new ResourceNotFoundException("User", currentUsername));
        PurchaseOrder po = poRepository.findByIdAndIsDeletedFalse(request.purchaseOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("PurchaseOrder", request.purchaseOrderId()));
        var branch = branchRepository.findById(request.branchId())
                .orElseThrow(() -> new ResourceNotFoundException("Branch", request.branchId()));

        String grnNum = generateGRNNumber();

        GoodsReceiptNote grn = GoodsReceiptNote.builder()
                .grnNumber(grnNum)
                .purchaseOrder(po)
                .branch(branch)
                .receiptDate(request.receiptDate())
                .supplierInvoiceNumber(request.supplierInvoiceNumber())
                .deliveryNoteNumber(request.deliveryNoteNumber())
                .notes(request.notes())
                .status(GRNStatus.POSTED)
                .receivedBy(receiver)
                .build();

        grn = grnRepository.save(grn);

        List<GRNItem> grnItems = new ArrayList<>();

        for (GRNItemRequest itemReq : request.items()) {
            var poItem = poItemRepository.findById(itemReq.poItemId())
                    .orElseThrow(() -> new ResourceNotFoundException("POItem", itemReq.poItemId()));
            var product = productRepository.findByIdAndIsDeletedFalse(itemReq.productId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product", itemReq.productId()));

            BigDecimal accepted = itemReq.quantityAccepted() != null ? itemReq.quantityAccepted() : itemReq.quantityReceived();
            BigDecimal rejected = itemReq.quantityRejected() != null ? itemReq.quantityRejected() : BigDecimal.ZERO;

            GRNItem item = GRNItem.builder()
                    .grn(grn)
                    .poItem(poItem)
                    .product(product)
                    .quantityReceived(itemReq.quantityReceived())
                    .quantityAccepted(accepted)
                    .quantityRejected(rejected)
                    .unitCost(itemReq.unitCost() != null ? itemReq.unitCost() : poItem.getUnitPrice())
                    .rejectionReason(itemReq.rejectionReason())
                    .batchNumber(itemReq.batchNumber())
                    .expiryDate(itemReq.expiryDate())
                    .notes(itemReq.notes())
                    .build();

            grnItems.add(grnItemRepository.save(item));

            // Update PO Item quantity received
            poItem.setQuantityReceived(poItem.getQuantityReceived().add(accepted));
            poItemRepository.save(poItem);

            // Post inventory stock update
            if (accepted.compareTo(BigDecimal.ZERO) > 0) {
                inventoryService.receiveStock(
                        product.getId(), branch.getId(), accepted, item.getUnitCost(), grnNum
                );
            }
        }

        // Update PO status to RECEIVED / PARTIALLY_RECEIVED
        po.setStatus(POStatus.RECEIVED);
        poRepository.save(po);

        grn.setItems(grnItems);
        log.info("GRN posted & Inventory updated: {} for PO {}", grnNum, po.getPoNumber());
        return toResponse(grn);
    }

    @Transactional(readOnly = true)
    public GRNResponse getById(UUID id) {
        return toResponse(grnRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("GoodsReceiptNote", id)));
    }

    @Transactional(readOnly = true)
    public Page<GRNResponse> searchGRNs(UUID companyId, String search, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return grnRepository.searchGRNs(companyId, search, pageable).map(this::toResponse);
    }

    private synchronized String generateGRNNumber() {
        int year = LocalDate.now().getYear();
        long count = grnRepository.countByYear(year) + 1;
        return String.format("GRN-%d-%04d", year, count);
    }

    private GRNResponse toResponse(GoodsReceiptNote g) {
        List<GRNItemResponse> items = (g.getItems() != null ? g.getItems() : grnItemRepository.findByGrnIdAndIsDeletedFalse(g.getId()))
                .stream().map(i -> new GRNItemResponse(
                        i.getId(), i.getPoItem().getId(), i.getProduct().getId(), i.getProduct().getName(), i.getProduct().getSku(),
                        i.getQuantityReceived(), i.getQuantityAccepted(), i.getQuantityRejected(),
                        i.getUnitCost(), i.getRejectionReason(), i.getBatchNumber(), i.getExpiryDate()
                )).toList();

        return new GRNResponse(
                g.getId(), g.getGrnNumber(), g.getPurchaseOrder().getId(), g.getPurchaseOrder().getPoNumber(),
                g.getPurchaseOrder().getSupplier().getName(),
                g.getBranch().getId(), g.getBranch().getName(),
                g.getReceiptDate(), g.getSupplierInvoiceNumber(), g.getDeliveryNoteNumber(),
                g.getStatus(),
                g.getReceivedBy() != null ? g.getReceivedBy().getFirstName() + " " + g.getReceivedBy().getLastName() : null,
                items, g.getCreatedAt()
        );
    }
}

package com.procure.module.purchase.service;

import com.procure.common.exception.ResourceNotFoundException;
import com.procure.module.company.repository.BranchRepository;
import com.procure.module.product.repository.ProductRepository;
import com.procure.module.purchase.dto.PurchaseOrderDtos.*;
import com.procure.module.purchase.entity.PurchaseOrder;
import com.procure.module.purchase.entity.PurchaseOrder.POStatus;
import com.procure.module.purchase.entity.PurchaseOrderItem;
import com.procure.module.purchase.entity.PurchaseRequest;
import com.procure.module.purchase.entity.PurchaseRequest.PRStatus;
import com.procure.module.purchase.repository.PurchaseOrderItemRepository;
import com.procure.module.purchase.repository.PurchaseOrderRepository;
import com.procure.module.purchase.repository.PurchaseRequestRepository;
import com.procure.module.supplier.repository.SupplierRepository;
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
public class PurchaseOrderService {

    private final PurchaseOrderRepository     poRepository;
    private final PurchaseOrderItemRepository poItemRepository;
    private final PurchaseRequestRepository   prRepository;
    private final SupplierRepository          supplierRepository;
    private final BranchRepository            branchRepository;
    private final ProductRepository           productRepository;
    private final UserRepository              userRepository;

    @Transactional
    public POResponse createPO(POCreateRequest request, String currentUsername) {
        User creator = userRepository.findByEmail(currentUsername)
                .orElseThrow(() -> new ResourceNotFoundException("User", currentUsername));
        var supplier = supplierRepository.findByIdAndIsDeletedFalse(request.supplierId())
                .orElseThrow(() -> new ResourceNotFoundException("Supplier", request.supplierId()));
        var branch = branchRepository.findById(request.branchId())
                .orElseThrow(() -> new ResourceNotFoundException("Branch", request.branchId()));

        PurchaseRequest pr = null;
        if (request.purchaseRequestId() != null) {
            pr = prRepository.findByIdAndIsDeletedFalse(request.purchaseRequestId())
                    .orElseThrow(() -> new ResourceNotFoundException("PurchaseRequest", request.purchaseRequestId()));
            pr.setStatus(PRStatus.CONVERTED);
            prRepository.save(pr);
        }

        String poNum = generatePONumber();

        PurchaseOrder po = PurchaseOrder.builder()
                .poNumber(poNum)
                .purchaseRequest(pr)
                .supplier(supplier)
                .branch(branch)
                .orderDate(request.orderDate())
                .expectedDeliveryDate(request.expectedDeliveryDate())
                .deliveryAddress(request.deliveryAddress())
                .paymentTerms(request.paymentTerms() != null ? request.paymentTerms() : supplier.getPaymentTerms())
                .currency(request.currency() != null ? request.currency() : "USD")
                .notes(request.notes())
                .status(POStatus.DRAFT)
                .createdByUser(creator)
                .build();

        po = poRepository.save(po);

        BigDecimal subtotal = BigDecimal.ZERO;
        List<PurchaseOrderItem> items = new ArrayList<>();

        for (POItemRequest itemReq : request.items()) {
            var product = productRepository.findByIdAndIsDeletedFalse(itemReq.productId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product", itemReq.productId()));

            BigDecimal linePrice = itemReq.unitPrice().multiply(itemReq.quantityOrdered());
            subtotal = subtotal.add(linePrice);

            PurchaseOrderItem item = PurchaseOrderItem.builder()
                    .purchaseOrder(po)
                    .product(product)
                    .quantityOrdered(itemReq.quantityOrdered())
                    .unitPrice(itemReq.unitPrice())
                    .discountPercent(itemReq.discountPercent())
                    .taxPercent(itemReq.taxPercent())
                    .totalPrice(linePrice)
                    .unitOfMeasure(itemReq.unitOfMeasure() != null ? itemReq.unitOfMeasure() : product.getUnitOfMeasure())
                    .notes(itemReq.notes())
                    .build();

            items.add(poItemRepository.save(item));
        }

        po.setSubtotal(subtotal);
        po.setTotalAmount(subtotal);
        po.setItems(items);
        poRepository.save(po);

        log.info("Purchase Order created: {} [{}]", po.getPoNumber(), po.getId());
        return toResponse(po);
    }

    @Transactional
    public POResponse updateStatus(UUID id, POStatus newStatus) {
        PurchaseOrder po = poRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("PurchaseOrder", id));
        po.setStatus(newStatus);
        poRepository.save(po);
        log.info("PO {} status updated to {}", po.getPoNumber(), newStatus);
        return toResponse(po);
    }

    @Transactional(readOnly = true)
    public POResponse getById(UUID id) {
        return toResponse(poRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("PurchaseOrder", id)));
    }

    @Transactional(readOnly = true)
    public Page<POSummary> searchPOs(UUID companyId, UUID supplierId, POStatus status, String search, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return poRepository.searchPOs(companyId, supplierId, status, search, pageable)
                .map(this::toSummary);
    }

    private synchronized String generatePONumber() {
        int year = LocalDate.now().getYear();
        long count = poRepository.countByYear(year) + 1;
        return String.format("PO-%d-%04d", year, count);
    }

    private POResponse toResponse(PurchaseOrder po) {
        List<POItemResponse> itemResponses = (po.getItems() != null ? po.getItems() : poItemRepository.findByPurchaseOrderIdAndIsDeletedFalse(po.getId()))
                .stream().map(i -> new POItemResponse(
                        i.getId(), i.getProduct().getId(), i.getProduct().getName(), i.getProduct().getSku(),
                        i.getQuantityOrdered(), i.getQuantityReceived(), i.getUnitPrice(),
                        i.getDiscountPercent(), i.getTaxPercent(), i.getTotalPrice(), i.getUnitOfMeasure()
                )).toList();

        return new POResponse(
                po.getId(), po.getPoNumber(),
                po.getPurchaseRequest() != null ? po.getPurchaseRequest().getId() : null,
                po.getPurchaseRequest() != null ? po.getPurchaseRequest().getPrNumber() : null,
                po.getSupplier().getId(), po.getSupplier().getName(),
                po.getBranch().getId(), po.getBranch().getName(),
                po.getOrderDate(), po.getExpectedDeliveryDate(), po.getDeliveryAddress(),
                po.getSubtotal(), po.getTaxAmount(), po.getDiscountAmount(), po.getTotalAmount(),
                po.getCurrency(), po.getPaymentTerms(), po.getStatus(),
                itemResponses, po.getCreatedAt()
        );
    }

    private POSummary toSummary(PurchaseOrder po) {
        int count = po.getItems() != null ? po.getItems().size() : poItemRepository.findByPurchaseOrderIdAndIsDeletedFalse(po.getId()).size();
        return new POSummary(
                po.getId(), po.getPoNumber(), po.getSupplier().getName(), po.getBranch().getName(),
                po.getOrderDate(), po.getTotalAmount(), po.getCurrency(), po.getStatus(),
                count, po.getCreatedAt()
        );
    }
}

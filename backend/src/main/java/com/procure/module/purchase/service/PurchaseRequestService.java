package com.procure.module.purchase.service;

import com.procure.common.exception.ResourceNotFoundException;
import com.procure.module.company.repository.BranchRepository;
import com.procure.module.product.repository.ProductRepository;
import com.procure.module.purchase.dto.PurchaseRequestDtos.*;
import com.procure.module.purchase.entity.PurchaseRequest;
import com.procure.module.purchase.entity.PurchaseRequest.PRPriority;
import com.procure.module.purchase.entity.PurchaseRequest.PRStatus;
import com.procure.module.purchase.entity.PurchaseRequestItem;
import com.procure.module.purchase.repository.PurchaseRequestItemRepository;
import com.procure.module.purchase.repository.PurchaseRequestRepository;
import com.procure.module.user.entity.User;
import com.procure.module.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PurchaseRequestService {

    private final PurchaseRequestRepository     prRepository;
    private final PurchaseRequestItemRepository prItemRepository;
    private final ProductRepository             productRepository;
    private final BranchRepository              branchRepository;
    private final UserRepository                userRepository;

    @Transactional
    public PRResponse createPR(PRCreateRequest request, String currentUsername) {
        User requester = userRepository.findByEmail(currentUsername)
                .orElseThrow(() -> new ResourceNotFoundException("User", currentUsername));

        var branch = branchRepository.findById(request.branchId())
                .orElseThrow(() -> new ResourceNotFoundException("Branch", request.branchId()));

        String prNum = generatePRNumber();

        PurchaseRequest pr = PurchaseRequest.builder()
                .prNumber(prNum)
                .title(request.title())
                .description(request.description())
                .requiredDate(request.requiredDate())
                .priority(request.priority() != null ? request.priority() : PRPriority.MEDIUM)
                .status(PRStatus.DRAFT)
                .requestedBy(requester)
                .branch(branch)
                .build();

        pr = prRepository.save(pr);

        BigDecimal total = BigDecimal.ZERO;
        List<PurchaseRequestItem> items = new ArrayList<>();

        for (PRItemRequest itemReq : request.items()) {
            var product = productRepository.findByIdAndIsDeletedFalse(itemReq.productId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product", itemReq.productId()));

            BigDecimal price = itemReq.estimatedUnitPrice() != null ? itemReq.estimatedUnitPrice() : product.getUnitPrice();
            if (price == null) price = BigDecimal.ZERO;
            BigDecimal lineTotal = price.multiply(itemReq.quantity());

            total = total.add(lineTotal);

            PurchaseRequestItem item = PurchaseRequestItem.builder()
                    .purchaseRequest(pr)
                    .product(product)
                    .quantity(itemReq.quantity())
                    .unitOfMeasure(itemReq.unitOfMeasure() != null ? itemReq.unitOfMeasure() : product.getUnitOfMeasure())
                    .estimatedUnitPrice(price)
                    .estimatedTotalPrice(lineTotal)
                    .specifications(itemReq.specifications())
                    .notes(itemReq.notes())
                    .build();

            items.add(prItemRepository.save(item));
        }

        pr.setTotalAmount(total);
        pr.setItems(items);
        prRepository.save(pr);

        log.info("Purchase Request created: {} [{}]", pr.getPrNumber(), pr.getId());
        return toResponse(pr);
    }

    @Transactional
    public PRResponse submitPR(UUID id) {
        PurchaseRequest pr = prRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("PurchaseRequest", id));

        if (pr.getStatus() != PRStatus.DRAFT)
            throw new IllegalArgumentException("Only DRAFT requests can be submitted");

        pr.setStatus(PRStatus.SUBMITTED);
        prRepository.save(pr);
        log.info("PR submitted: {}", pr.getPrNumber());
        return toResponse(pr);
    }

    @Transactional
    public PRResponse approvePR(UUID id, String managerUsername) {
        PurchaseRequest pr = prRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("PurchaseRequest", id));

        if (pr.getStatus() != PRStatus.SUBMITTED)
            throw new IllegalArgumentException("Only SUBMITTED requests can be approved");

        User manager = userRepository.findByEmail(managerUsername)
                .orElseThrow(() -> new ResourceNotFoundException("User", managerUsername));

        pr.setStatus(PRStatus.APPROVED);
        pr.setApprovedBy(manager);
        pr.setApprovedAt(LocalDateTime.now());
        prRepository.save(pr);

        log.info("PR approved: {} by {}", pr.getPrNumber(), managerUsername);
        return toResponse(pr);
    }

    @Transactional
    public PRResponse rejectPR(UUID id, PRRejectRequest req, String managerUsername) {
        PurchaseRequest pr = prRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("PurchaseRequest", id));

        if (pr.getStatus() != PRStatus.SUBMITTED)
            throw new IllegalArgumentException("Only SUBMITTED requests can be rejected");

        User manager = userRepository.findByEmail(managerUsername)
                .orElseThrow(() -> new ResourceNotFoundException("User", managerUsername));

        pr.setStatus(PRStatus.REJECTED);
        pr.setRejectionReason(req.reason());
        pr.setApprovedBy(manager);
        pr.setApprovedAt(LocalDateTime.now());
        prRepository.save(pr);

        log.info("PR rejected: {} by {}", pr.getPrNumber(), managerUsername);
        return toResponse(pr);
    }

    @Transactional(readOnly = true)
    public PRResponse getById(UUID id) {
        return toResponse(prRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("PurchaseRequest", id)));
    }

    @Transactional(readOnly = true)
    public Page<PRSummary> searchPRs(UUID companyId, UUID branchId, PRStatus status, String search, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return prRepository.searchPRs(companyId, branchId, status, search, pageable)
                .map(this::toSummary);
    }

    private synchronized String generatePRNumber() {
        int year = LocalDate.now().getYear();
        long count = prRepository.countByYear(year) + 1;
        return String.format("PR-%d-%04d", year, count);
    }

    private PRResponse toResponse(PurchaseRequest pr) {
        List<PRItemResponse> itemResponses = (pr.getItems() != null ? pr.getItems() : prItemRepository.findByPurchaseRequestIdAndIsDeletedFalse(pr.getId()))
                .stream().map(i -> new PRItemResponse(
                        i.getId(), i.getProduct().getId(), i.getProduct().getName(), i.getProduct().getSku(),
                        i.getQuantity(), i.getUnitOfMeasure(), i.getEstimatedUnitPrice(), i.getEstimatedTotalPrice(),
                        i.getSpecifications(), i.getNotes()
                )).toList();

        return new PRResponse(
                pr.getId(), pr.getPrNumber(), pr.getTitle(), pr.getDescription(),
                pr.getRequiredDate(), pr.getTotalAmount(), pr.getStatus(), pr.getPriority(),
                pr.getRejectionReason(),
                pr.getRequestedBy() != null ? pr.getRequestedBy().getId() : null,
                pr.getRequestedBy() != null ? pr.getRequestedBy().getFirstName() + " " + pr.getRequestedBy().getLastName() : null,
                pr.getApprovedBy()  != null ? pr.getApprovedBy().getId() : null,
                pr.getApprovedBy()  != null ? pr.getApprovedBy().getFirstName() + " " + pr.getApprovedBy().getLastName() : null,
                pr.getApprovedAt(),
                pr.getBranch() != null ? pr.getBranch().getId() : null,
                pr.getBranch() != null ? pr.getBranch().getName() : null,
                itemResponses,
                pr.getCreatedAt()
        );
    }

    private PRSummary toSummary(PurchaseRequest pr) {
        int count = pr.getItems() != null ? pr.getItems().size() : prItemRepository.findByPurchaseRequestIdAndIsDeletedFalse(pr.getId()).size();
        return new PRSummary(
                pr.getId(), pr.getPrNumber(), pr.getTitle(), pr.getRequiredDate(),
                pr.getTotalAmount(), pr.getStatus(), pr.getPriority(),
                pr.getRequestedBy() != null ? pr.getRequestedBy().getFirstName() + " " + pr.getRequestedBy().getLastName() : null,
                pr.getBranch() != null ? pr.getBranch().getName() : null,
                count, pr.getCreatedAt()
        );
    }
}

package com.procure.module.invoice.service;

import com.procure.common.exception.ResourceNotFoundException;
import com.procure.module.invoice.dto.InvoiceDtos.*;
import com.procure.module.invoice.entity.Invoice;
import com.procure.module.invoice.entity.Invoice.InvoiceStatus;
import com.procure.module.invoice.repository.InvoiceRepository;
import com.procure.module.purchase.entity.PurchaseOrder;
import com.procure.module.purchase.repository.PurchaseOrderRepository;
import com.procure.module.supplier.repository.SupplierRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class InvoiceService {

    private final InvoiceRepository       invoiceRepository;
    private final SupplierRepository      supplierRepository;
    private final PurchaseOrderRepository poRepository;

    @Transactional
    public InvoiceResponse createInvoice(InvoiceCreateRequest request) {
        var supplier = supplierRepository.findByIdAndIsDeletedFalse(request.supplierId())
                .orElseThrow(() -> new ResourceNotFoundException("Supplier", request.supplierId()));

        PurchaseOrder po = null;
        if (request.purchaseOrderId() != null) {
            po = poRepository.findByIdAndIsDeletedFalse(request.purchaseOrderId())
                    .orElseThrow(() -> new ResourceNotFoundException("PurchaseOrder", request.purchaseOrderId()));
        }

        String invNum = generateInvoiceNumber();

        BigDecimal tax = request.taxAmount() != null ? request.taxAmount() : BigDecimal.ZERO;
        BigDecimal subtotal = request.totalAmount().subtract(tax);

        Invoice invoice = Invoice.builder()
                .invoiceNumber(invNum)
                .supplierInvoiceNumber(request.supplierInvoiceNumber())
                .supplier(supplier)
                .purchaseOrder(po)
                .invoiceDate(request.invoiceDate())
                .dueDate(request.dueDate() != null ? request.dueDate() : request.invoiceDate().plusDays(30))
                .subtotal(subtotal)
                .taxAmount(tax)
                .totalAmount(request.totalAmount())
                .paidAmount(BigDecimal.ZERO)
                .currency(request.currency() != null ? request.currency() : "USD")
                .status(InvoiceStatus.APPROVED)
                .notes(request.notes())
                .build();

        invoice = invoiceRepository.save(invoice);
        log.info("Invoice recorded: {} [{}]", invNum, invoice.getId());
        return toResponse(invoice);
    }

    @Transactional
    public InvoiceResponse recordPayment(UUID id, PaymentRecordRequest request) {
        Invoice invoice = invoiceRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice", id));

        BigDecimal newPaid = invoice.getPaidAmount().add(request.amount());
        if (newPaid.compareTo(invoice.getTotalAmount()) > 0)
            throw new IllegalArgumentException("Payment amount exceeds total outstanding balance");

        invoice.setPaidAmount(newPaid);
        invoice.setPaymentDate(request.paymentDate());

        if (newPaid.compareTo(invoice.getTotalAmount()) == 0) {
            invoice.setStatus(InvoiceStatus.PAID);
        } else {
            invoice.setStatus(InvoiceStatus.PARTIALLY_PAID);
        }

        invoiceRepository.save(invoice);
        log.info("Payment recorded for Invoice {}: amount={}", invoice.getInvoiceNumber(), request.amount());
        return toResponse(invoice);
    }

    @Transactional(readOnly = true)
    public InvoiceResponse getById(UUID id) {
        return toResponse(invoiceRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice", id)));
    }

    @Transactional(readOnly = true)
    public Page<InvoiceResponse> searchInvoices(UUID companyId, UUID supplierId, InvoiceStatus status, String search, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return invoiceRepository.searchInvoices(companyId, supplierId, status, search, pageable)
                .map(this::toResponse);
    }

    private synchronized String generateInvoiceNumber() {
        int year = LocalDate.now().getYear();
        long count = invoiceRepository.countByYear(year) + 1;
        return String.format("INV-%d-%04d", year, count);
    }

    private InvoiceResponse toResponse(Invoice inv) {
        return new InvoiceResponse(
                inv.getId(), inv.getInvoiceNumber(), inv.getSupplierInvoiceNumber(),
                inv.getSupplier().getId(), inv.getSupplier().getName(),
                inv.getPurchaseOrder() != null ? inv.getPurchaseOrder().getId() : null,
                inv.getPurchaseOrder() != null ? inv.getPurchaseOrder().getPoNumber() : null,
                inv.getInvoiceDate(), inv.getDueDate(),
                inv.getSubtotal(), inv.getTaxAmount(), inv.getTotalAmount(),
                inv.getPaidAmount(), inv.getOutstandingAmount(),
                inv.getCurrency(), inv.getStatus(), inv.isOverdue(),
                inv.getPaymentDate(), inv.getNotes(), inv.getCreatedAt()
        );
    }
}

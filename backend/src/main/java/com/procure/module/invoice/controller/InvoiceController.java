package com.procure.module.invoice.controller;

import com.procure.common.response.ApiResponse;
import com.procure.module.invoice.dto.InvoiceDtos.*;
import com.procure.module.invoice.entity.Invoice.InvoiceStatus;
import com.procure.module.invoice.service.InvoiceService;
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
@RequestMapping("/invoices")
@RequiredArgsConstructor
@Tag(name = "Invoices", description = "Supplier invoice entry and payment processing APIs")
public class InvoiceController {

    private final InvoiceService invoiceService;

    @PostMapping
    @PreAuthorize("hasAuthority('INVOICE:WRITE')")
    @Operation(summary = "Record a supplier invoice")
    public ResponseEntity<ApiResponse<InvoiceResponse>> createInvoice(@Valid @RequestBody InvoiceCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Invoice recorded", invoiceService.createInvoice(request)));
    }

    @PostMapping("/{id}/pay")
    @PreAuthorize("hasAuthority('INVOICE:WRITE')")
    @Operation(summary = "Record a payment towards an invoice")
    public ResponseEntity<ApiResponse<InvoiceResponse>> recordPayment(
            @PathVariable UUID id, @Valid @RequestBody PaymentRecordRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Payment recorded", invoiceService.recordPayment(id, request)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('INVOICE:READ')")
    @Operation(summary = "Get invoice details")
    public ResponseEntity<ApiResponse<InvoiceResponse>> getInvoice(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(invoiceService.getById(id)));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('INVOICE:READ')")
    @Operation(summary = "Search invoices with pagination")
    public ResponseEntity<ApiResponse<Page<InvoiceResponse>>> listInvoices(
            @RequestParam UUID companyId,
            @RequestParam(required = false) UUID supplierId,
            @RequestParam(required = false) InvoiceStatus status,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(
                invoiceService.searchInvoices(companyId, supplierId, status, search, page, size)));
    }
}

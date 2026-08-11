package com.procure.module.purchase.controller;

import com.procure.common.response.ApiResponse;
import com.procure.module.purchase.dto.PurchaseRequestDtos.*;
import com.procure.module.purchase.entity.PurchaseRequest.PRStatus;
import com.procure.module.purchase.service.PurchaseRequestService;
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
@RequestMapping("/purchase-requests")
@RequiredArgsConstructor
@Tag(name = "Purchase Requests", description = "Requisition drafting, submission, and manager approval workflow APIs")
public class PurchaseRequestController {

    private final PurchaseRequestService prService;

    @PostMapping
    @PreAuthorize("hasAuthority('PR:WRITE')")
    @Operation(summary = "Create a draft purchase request with items")
    public ResponseEntity<ApiResponse<PRResponse>> createPR(
            @Valid @RequestBody PRCreateRequest request,
            Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Purchase request created",
                        prService.createPR(request, authentication.getName())));
    }

    @PatchMapping("/{id}/submit")
    @PreAuthorize("hasAuthority('PR:WRITE')")
    @Operation(summary = "Submit a draft purchase request for approval")
    public ResponseEntity<ApiResponse<PRResponse>> submitPR(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success("Purchase request submitted for approval",
                prService.submitPR(id)));
    }

    @PatchMapping("/{id}/approve")
    @PreAuthorize("hasAuthority('PR:APPROVE')")
    @Operation(summary = "Approve a submitted purchase request")
    public ResponseEntity<ApiResponse<PRResponse>> approvePR(
            @PathVariable UUID id, Authentication authentication) {
        return ResponseEntity.ok(ApiResponse.success("Purchase request approved",
                prService.approvePR(id, authentication.getName())));
    }

    @PatchMapping("/{id}/reject")
    @PreAuthorize("hasAuthority('PR:APPROVE')")
    @Operation(summary = "Reject a submitted purchase request")
    public ResponseEntity<ApiResponse<PRResponse>> rejectPR(
            @PathVariable UUID id, @Valid @RequestBody PRRejectRequest request, Authentication authentication) {
        return ResponseEntity.ok(ApiResponse.success("Purchase request rejected",
                prService.rejectPR(id, request, authentication.getName())));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('PR:READ')")
    @Operation(summary = "Get purchase request details")
    public ResponseEntity<ApiResponse<PRResponse>> getPR(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(prService.getById(id)));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('PR:READ')")
    @Operation(summary = "Search purchase requests with pagination")
    public ResponseEntity<ApiResponse<Page<PRSummary>>> listPRs(
            @RequestParam UUID companyId,
            @RequestParam(required = false) UUID branchId,
            @RequestParam(required = false) PRStatus status,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(
                prService.searchPRs(companyId, branchId, status, search, page, size)));
    }
}

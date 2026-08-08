package com.procure.module.company.controller;

import com.procure.common.response.ApiResponse;
import com.procure.module.company.dto.BranchDtos.BranchRequest;
import com.procure.module.company.dto.BranchDtos.BranchResponse;
import com.procure.module.company.service.BranchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/companies/{companyId}/branches")
@RequiredArgsConstructor
@Tag(name = "Branches", description = "Multi-branch profile management APIs")
public class BranchController {

    private final BranchService branchService;

    @PostMapping
    @PreAuthorize("hasAuthority('COMPANY:WRITE')")
    @Operation(summary = "Create a new branch under a company")
    public ResponseEntity<ApiResponse<BranchResponse>> createBranch(
            @PathVariable UUID companyId,
            @Valid @RequestBody BranchRequest request) {
        BranchResponse response = branchService.createBranch(companyId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Branch registered successfully", response));
    }

    @PutMapping("/{branchId}")
    @PreAuthorize("hasAuthority('COMPANY:WRITE')")
    @Operation(summary = "Update an existing branch profile")
    public ResponseEntity<ApiResponse<BranchResponse>> updateBranch(
            @PathVariable UUID companyId,
            @PathVariable UUID branchId,
            @Valid @RequestBody BranchRequest request) {
        BranchResponse response = branchService.updateBranch(companyId, branchId, request);
        return ResponseEntity.ok(ApiResponse.success("Branch profile updated", response));
    }

    @GetMapping("/{branchId}")
    @PreAuthorize("hasAuthority('COMPANY:READ')")
    @Operation(summary = "Get branch details by ID under a company")
    public ResponseEntity<ApiResponse<BranchResponse>> getBranchById(
            @PathVariable UUID companyId,
            @PathVariable UUID branchId) {
        BranchResponse response = branchService.getBranchById(companyId, branchId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('COMPANY:READ')")
    @Operation(summary = "Get all branches registered under a company")
    public ResponseEntity<ApiResponse<List<BranchResponse>>> getBranchesByCompany(
            @PathVariable UUID companyId) {
        List<BranchResponse> response = branchService.getBranchesByCompany(companyId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/{branchId}")
    @PreAuthorize("hasAuthority('COMPANY:WRITE')")
    @Operation(summary = "Soft delete a branch under a company")
    public ResponseEntity<ApiResponse<Void>> deleteBranch(
            @PathVariable UUID companyId,
            @PathVariable UUID branchId) {
        branchService.deleteBranch(companyId, branchId);
        return ResponseEntity.ok(ApiResponse.success("Branch deleted successfully", null));
    }
}

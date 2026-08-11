package com.procure.module.receiving.controller;

import com.procure.common.response.ApiResponse;
import com.procure.module.receiving.dto.GrnDtos.*;
import com.procure.module.receiving.service.GrnService;
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
@RequestMapping("/grns")
@RequiredArgsConstructor
@Tag(name = "Goods Receiving (GRN)", description = "Goods receipt notes and receiving inspection APIs")
public class GrnController {

    private final GrnService grnService;

    @PostMapping
    @PreAuthorize("hasAuthority('INVENTORY:WRITE')")
    @Operation(summary = "Post a Goods Receipt Note (GRN) and update stock levels")
    public ResponseEntity<ApiResponse<GRNResponse>> createAndPostGRN(
            @Valid @RequestBody GRNCreateRequest request, Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Goods Receipt Note posted and inventory updated",
                        grnService.createAndPostGRN(request, authentication.getName())));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('INVENTORY:READ')")
    @Operation(summary = "Get Goods Receipt Note details")
    public ResponseEntity<ApiResponse<GRNResponse>> getGRN(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(grnService.getById(id)));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('INVENTORY:READ')")
    @Operation(summary = "Search Goods Receipt Notes with pagination")
    public ResponseEntity<ApiResponse<Page<GRNResponse>>> listGRNs(
            @RequestParam UUID companyId,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(
                grnService.searchGRNs(companyId, search, page, size)));
    }
}

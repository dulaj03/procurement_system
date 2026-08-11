package com.procure.module.audit.controller;

import com.procure.common.response.ApiResponse;
import com.procure.module.audit.dto.DashboardDtos.DashboardData;
import com.procure.module.audit.service.DashboardAnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/analytics")
@RequiredArgsConstructor
@Tag(name = "Analytics & Dashboard", description = "Executive KPI metrics, spend charts, and dashboard summary APIs")
public class DashboardAnalyticsController {

    private final DashboardAnalyticsService analyticsService;

    @GetMapping("/dashboard")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get full executive dashboard analytics & KPI metrics")
    public ResponseEntity<ApiResponse<DashboardData>> getDashboardData(@RequestParam UUID companyId) {
        return ResponseEntity.ok(ApiResponse.success(analyticsService.getDashboardData(companyId)));
    }
}

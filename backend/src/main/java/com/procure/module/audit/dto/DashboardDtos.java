package com.procure.module.audit.dto;

import java.math.BigDecimal;
import java.util.List;

public class DashboardDtos {

    public record MetricSummary(
            BigDecimal totalSpendThisMonth,
            long pendingPRCount,
            long activePOCount,
            long lowStockAlertCount,
            long activeSupplierCount
    ) {}

    public record ChartSeries(
            String name,
            BigDecimal value
    ) {}

    public record DashboardData(
            MetricSummary summary,
            List<ChartSeries> monthlySpendTrend,
            List<ChartSeries> categorySpendDistribution,
            List<ChartSeries> prStatusDistribution
    ) {}
}

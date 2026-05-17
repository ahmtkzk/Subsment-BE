package com.reything.subsmentbe.dto.statistics;

import java.math.BigDecimal;
import java.util.List;

public record DashboardStatistics(
        BigDecimal totalMonthlyAmount,
        BigDecimal totalYearlyEstimate,
        long activeSubscriptionCount,
        long inactiveSubscriptionCount,
        long cancelledSubscriptionCount,
        List<UpcomingPayment> upcomingPayments
) {
}

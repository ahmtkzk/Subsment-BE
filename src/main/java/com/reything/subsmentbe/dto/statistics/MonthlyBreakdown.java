package com.reything.subsmentbe.dto.statistics;

import com.reything.subsmentbe.domain.enums.Currency;

import java.math.BigDecimal;

public record MonthlyBreakdown(
        String month,
        BigDecimal totalAmount,
        Currency currency,
        long subscriptionCount
) {
}

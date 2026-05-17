package com.reything.subsmentbe.dto.statistics;

import com.reything.subsmentbe.domain.enums.Currency;

import java.math.BigDecimal;

public record CategoryStatistic(
        String name,
        String emoji,
        String color,
        long count,
        BigDecimal totalAmount,
        Currency currency
) {
}

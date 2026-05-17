package com.reything.subsmentbe.dto.statistics;

import com.reything.subsmentbe.domain.enums.Currency;

import java.math.BigDecimal;

public record CurrencyStatistic(
        Currency currency,
        String symbol,
        BigDecimal totalAmount,
        long count,
        BigDecimal percentage
) {
}

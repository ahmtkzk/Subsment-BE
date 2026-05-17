package com.reything.subsmentbe.dto.statistics;

import com.reything.subsmentbe.domain.enums.Currency;

import java.math.BigDecimal;
import java.util.UUID;

public record TopSubscription(
        UUID id,
        String name,
        BigDecimal amount,
        Currency currency,
        String category,
        String emoji
) {
}

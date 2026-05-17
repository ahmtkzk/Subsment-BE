package com.reything.subsmentbe.dto.calendar;

import com.reything.subsmentbe.domain.enums.Currency;
import com.reything.subsmentbe.domain.enums.SubscriptionStatus;

import java.math.BigDecimal;
import java.util.UUID;

public record CalendarSubscription(
        UUID id,
        String name,
        BigDecimal amount,
        Currency currency,
        String emoji,
        String color,
        SubscriptionStatus status,
        String category
) {
}

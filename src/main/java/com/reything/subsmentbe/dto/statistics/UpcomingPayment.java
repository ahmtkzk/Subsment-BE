package com.reything.subsmentbe.dto.statistics;

import com.reything.subsmentbe.domain.enums.Currency;
import com.reything.subsmentbe.domain.enums.SubscriptionStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record UpcomingPayment(
        UUID id,
        String name,
        BigDecimal amount,
        Currency currency,
        LocalDate nextPaymentDate,
        long daysUntilPayment,
        SubscriptionStatus status
) {
}

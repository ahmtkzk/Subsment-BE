package com.reything.subsmentbe.dto.subscription;

import com.reything.subsmentbe.domain.enums.Currency;
import com.reything.subsmentbe.domain.enums.Period;
import com.reything.subsmentbe.domain.enums.SubscriptionStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

public record SubscriptionResponse(
        UUID id,
        String name,
        String category,
        BigDecimal amount,
        Currency currency,
        Period period,
        String customPeriodLabel,
        LocalDate firstPaymentDate,
        LocalDate nextPaymentDate,
        String paymentMethod,
        String cardLastFour,
        SubscriptionStatus status,
        LocalDate freeTrialEndDate,
        LocalDate cancelReminderDate,
        String color,
        String emoji,
        String notes,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}

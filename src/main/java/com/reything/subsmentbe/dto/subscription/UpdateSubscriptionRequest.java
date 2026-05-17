package com.reything.subsmentbe.dto.subscription;

import com.reything.subsmentbe.domain.enums.Currency;
import com.reything.subsmentbe.domain.enums.Period;
import com.reything.subsmentbe.domain.enums.SubscriptionStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Pattern;

import java.math.BigDecimal;
import java.time.LocalDate;

public record UpdateSubscriptionRequest(
        String name,
        String category,
        @DecimalMin(value = "0.0", inclusive = false) BigDecimal amount,
        Currency currency,
        Period period,
        String customPeriodLabel,
        LocalDate firstPaymentDate,
        LocalDate nextPaymentDate,
        String paymentMethod,
        @Pattern(regexp = "\\d{4}") String cardLastFour,
        SubscriptionStatus status,
        LocalDate freeTrialEndDate,
        LocalDate cancelReminderDate,
        @Pattern(regexp = "^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{8})$") String color,
        String emoji,
        String notes
) {
}

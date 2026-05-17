package com.reything.subsmentbe.dto.subscription;

import com.reything.subsmentbe.domain.enums.Currency;
import com.reything.subsmentbe.domain.enums.Period;
import com.reything.subsmentbe.domain.enums.SubscriptionStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CreateSubscriptionRequest(
        @NotBlank String name,
        @NotBlank String category,
        @NotNull @DecimalMin(value = "0.0", inclusive = false) BigDecimal amount,
        @NotNull Currency currency,
        @NotNull Period period,
        String customPeriodLabel,
        @NotNull LocalDate firstPaymentDate,
        LocalDate nextPaymentDate,
        @NotBlank String paymentMethod,
        @Pattern(regexp = "\\d{4}", message = "Kart son 4 hane sayısal olmalı") String cardLastFour,
        @NotNull SubscriptionStatus status,
        LocalDate freeTrialEndDate,
        LocalDate cancelReminderDate,
        @NotBlank @Pattern(regexp = "^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{8})$", message = "Geçerli hex renk kodu giriniz") String color,
        @NotBlank String emoji,
        String notes
) {
}

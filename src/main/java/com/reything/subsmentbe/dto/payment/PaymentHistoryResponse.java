package com.reything.subsmentbe.dto.payment;

import com.reything.subsmentbe.domain.enums.Currency;
import com.reything.subsmentbe.domain.enums.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

public record PaymentHistoryResponse(
        UUID id,
        UUID subscriptionId,
        LocalDate paymentDate,
        BigDecimal amount,
        Currency currency,
        PaymentStatus status,
        OffsetDateTime createdAt
) {
}

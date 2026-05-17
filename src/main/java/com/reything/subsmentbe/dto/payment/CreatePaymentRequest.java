package com.reything.subsmentbe.dto.payment;

import com.reything.subsmentbe.domain.enums.PaymentStatus;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record CreatePaymentRequest(
        @NotNull LocalDate paymentDate,
        @NotNull PaymentStatus status
) {
}
